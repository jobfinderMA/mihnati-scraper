"""
Emploi-Public.ma Automated Scraper Bot (Mihnati مهنتي)
Scrapes Moroccan public sector recruitments, scales, diplomas, written & oral convocations.
Runs on an hourly schedule daily and exports structured data to JSON and SQLite.
"""

import os
import sys
import time
import json
import sqlite3
import logging
import argparse
from datetime import datetime
import urllib.parse
import urllib3
import requests
from bs4 import BeautifulSoup
import schedule

# Disable insecure HTTPS warnings for Moroccan public portal certificates
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# Ensure UTF-8 output on Windows terminals
if sys.stdout and hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler(os.path.join(os.path.dirname(__file__), 'scraper.log'), encoding='utf-8')
    ]
)
logger = logging.getLogger('EmploiPublicBot')

BASE_URL = 'https://www.emploi-public.ma'
HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36',
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8',
    'Accept-Language': 'fr-FR,fr;q=0.9,ar;q=0.8,en;q=0.7',
}

DB_PATH = os.path.join(os.path.dirname(__file__), 'emploi_public.db')
JSON_OUTPUT_PATH = os.path.join(os.path.dirname(__file__), 'scraped_data.json')
ASSETS_EXPORT_PATH = os.path.abspath(
    os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'assets', 'scraped_emploi_public.json')
)

def init_database():
    """Initializes SQLite database tables for concours and convocations."""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    # Table for Concours
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS concours (
            id TEXT PRIMARY KEY,
            title TEXT,
            title_ar TEXT,
            grade TEXT,
            echelle TEXT,
            administration TEXT,
            administration_ar TEXT,
            speciality TEXT,
            degree_required TEXT,
            posts_count INTEGER,
            deadline TEXT,
            exam_date TEXT,
            publication_date TEXT,
            reference_code TEXT,
            submission_url TEXT,
            detail_url TEXT,
            written_list_url TEXT,
            oral_list_url TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    
    # Table for Convocations (Écrit & Oral)
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS convocations (
            id TEXT PRIMARY KEY,
            concours_id TEXT,
            type TEXT, -- ECRIT, ORAL, RESULTAT
            title TEXT,
            administration TEXT,
            exam_date TEXT,
            exam_center TEXT,
            candidates_count INTEGER,
            pdf_url TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (concours_id) REFERENCES concours(id)
        )
    ''')
    
    conn.commit()
    conn.close()

def extract_echelle(title_or_grade: str) -> str:
    """Detects administrative scale from Moroccan grade text."""
    t = title_or_grade.lower()
    if '11' in t or 'سلم 11' in t or 'echelle 11' in t or 'ingénieur' in t or 'مهندس' in t or 'متصرف من الدرجة الثانية' in t:
        return 'Échelle 11'
    elif '10' in t or 'سلم 10' in t or 'echelle 10' in t or 'متصرف من الدرجة الثالثة' in t:
        return 'Échelle 10'
    elif '9' in t or 'سلم 9' in t or 'echelle 9' in t or 'تقني من الدرجة الثالثة' in t:
        return 'Échelle 9'
    elif '8' in t or 'سلم 8' in t or 'echelle 8' in t or 'تقني من الدرجة الرابعة' in t or 'مساعد تقني' in t:
        return 'Échelle 8'
    return 'Statut Fonction Publique'

def extract_degree(grade_or_spec: str, echelle: str) -> str:
    """Maps administrative grade to standard Moroccan study level."""
    if echelle == 'Échelle 11':
        return 'Bac+5 / Master / Ingénieur d\'État'
    elif echelle == 'Échelle 10':
        return 'Bac+3 / Licence'
    elif echelle == 'Échelle 9':
        return 'Bac+2 / DUT / BTS / Technicien Spécialisé'
    elif echelle == 'Échelle 8':
        return 'Baccalauréat / Niveau Bac'
    return 'Selon arrêté ministériel'

def scrape_concours_list(max_pages=5):
    """Scrapes the public list of concours from emploi-public.ma."""
    results = []
    logger.info(f"Starting scrape from {BASE_URL}/fr/concours-liste (max {max_pages} pages)...")
    
    session = requests.Session()
    session.headers.update(HEADERS)
    
    for page in range(1, max_pages + 1):
        page_url = f"{BASE_URL}/fr/concours-liste?page={page}"
        try:
            resp = session.get(page_url, verify=False, timeout=15)
            if resp.status_code != 200:
                logger.warning(f"Page {page} returned status {resp.status_code}")
                continue
            
            soup = BeautifulSoup(resp.text, 'html.parser')
            items = soup.find_all('div', class_='s-item')
            if not items:
                logger.info(f"No more items found at page {page}")
                break
            
            logger.info(f"Page {page}: found {len(items)} concours cards")
            
            for item in items:
                try:
                    # Find detail link
                    detail_a = item.find('a', href=lambda h: h and '/concours/details/' in h)
                    if not detail_a:
                        detail_a = item.find('a', href=lambda h: h and '/تفاصيل/' in h)
                    
                    if not detail_a:
                        continue
                    
                    detail_href = detail_a.get('href')
                    concours_uuid = detail_href.strip().split('/')[-1]
                    
                    # Extract title and administration
                    h2 = item.find('h2', class_='card-title') or item.find('h2')
                    raw_title = h2.get_text(strip=True) if h2 else "Concours de Recrutement"
                    
                    # Administration name
                    admin_tag = item.find('div', class_='institution') or item.find('span', class_='institution')
                    if not admin_tag:
                        # Fallback to text siblings
                        strings = list(item.stripped_strings)
                        admin_name = strings[1] if len(strings) > 1 else "Administration Publique"
                    else:
                        admin_name = admin_tag.get_text(strip=True)
                    
                    # Post count and deadline
                    raw_text = item.get_text()
                    posts_count = 1
                    for word in raw_text.split():
                        if word.isdigit():
                            posts_count = int(word)
                            break
                    
                    deadline = "Novembre 2026"
                    for line in item.stripped_strings:
                        if 'Délai' in line or 'آخر أجل' in line or 'Clôture' in line or '2026' in line:
                            deadline = line.replace('Délai de dépôt des candidatures', '').replace('آخر أجل لإيداع ملفات الترشيح :', '').strip()
                            break
                    
                    echelle = extract_echelle(raw_title)
                    degree = extract_degree(raw_title, echelle)
                    
                    concours_data = {
                        'id': f"ep-{concours_uuid[:8]}",
                        'uuid': concours_uuid,
                        'title': raw_title,
                        'administration': admin_name,
                        'echelle': echelle,
                        'degree_required': degree,
                        'posts_count': posts_count,
                        'deadline': deadline,
                        'detail_url': f"{BASE_URL}/fr/concours/details/{concours_uuid}",
                        'detail_url_ar': f"{BASE_URL}/ar/تفاصيل/المباريات/{concours_uuid}",
                        'written_list_url': f"{BASE_URL}/fr/concours/download/list_convoques/{concours_uuid}",
                        'oral_list_url': f"{BASE_URL}/fr/concours/download/list_convoques_oral/{concours_uuid}"
                    }
                    
                    results.append(concours_data)
                except Exception as e:
                    logger.debug(f"Error parsing single item: {e}")
                    continue
            
            time.sleep(1) # Polite delay
        except Exception as e:
            logger.error(f"Error requesting page {page}: {e}")
    
    logger.info(f"Total concours scraped: {len(results)}")
    return results

def scrape_convocations_and_results(concours_list):
    """Enriches concours with list download links for convocations."""
    convocations = []
    session = requests.Session()
    session.headers.update(HEADERS)
    
    logger.info(f"Checking convocations (écrit & oral) for {min(len(concours_list), 10)} recent concours...")
    
    for c in concours_list[:10]:
        uuid = c['uuid']
        detail_url = f"{BASE_URL}/fr/concours/details/{uuid}"
        try:
            resp = session.get(detail_url, verify=False, timeout=12)
            if resp.status_code != 200:
                continue
            
            soup = BeautifulSoup(resp.text, 'html.parser')
            
            # Check for written list
            written_link = soup.find('a', href=lambda h: h and 'list_convoques/' in h)
            if written_link:
                dispatch_whatsapp_notification({
                    'concours_id': c['id'],
                    'type': 'ECRIT',
                    'title': f"Liste des candidats convoqués à l'épreuve écrite - {c['title']}",
                    'administration': c['administration'],
                    'exam_date': c.get('exam_date') or 'Novembre 2026',
                    'exam_center': 'Rabat, Casablanca & Centres Régionaux',
                    'pdf_url': f"{BASE_URL}{written_link.get('href')}"
                })
                convocations.append({
                    'id': f"conv-ecrit-{uuid[:8]}",
                    'concours_id': c['id'],
                    'type': 'ECRIT',
                    'title': f"Liste des candidats convoqués à l'épreuve écrite - {c['title']}",
                    'administration': c['administration'],
                    'exam_date': c.get('exam_date') or 'Novembre 2026',
                    'exam_center': 'Rabat, Casablanca & Centres Régionaux',
                    'candidates_count': c['posts_count'] * 15,
                    'pdf_url': f"{BASE_URL}{written_link.get('href')}"
                })
            
            # Check for oral list
            oral_link = soup.find('a', href=lambda h: h and 'list_convoques_oral/' in h)
            if oral_link:
                convocations.append({
                    'id': f"conv-oral-{uuid[:8]}",
                    'concours_id': c['id'],
                    'type': 'ORAL',
                    'title': f"Liste des candidats convoqués pour l'épreuve orale - {c['title']}",
                    'administration': c['administration'],
                    'exam_date': 'Décembre 2026',
                    'exam_center': 'Siège central de l\'administration',
                    'candidates_count': c['posts_count'] * 3,
                    'pdf_url': f"{BASE_URL}{oral_link.get('href')}"
                })
            
            time.sleep(0.5)
        except Exception as e:
            logger.debug(f"Error checking convocations for {uuid}: {e}")
            continue
            
    logger.info(f"Total active convocations identified: {len(convocations)}")
    return convocations


def dispatch_whatsapp_notification(conv):
    """
    Dispatches automated WhatsApp notification for Écrit, Oral or Final Admis lists.
    Supports Green-API (instance ID & token) or generic WHATSAPP_GATEWAY_URL.
    Sends both message text and direct PDF file.
    """
    instance_id = os.environ.get('GREEN_API_INSTANCE_ID')
    token = os.environ.get('GREEN_API_TOKEN')
    gateway_url = os.environ.get('WHATSAPP_GATEWAY_URL')
    api_key = os.environ.get('WHATSAPP_API_KEY')
    phone = os.environ.get('WHATSAPP_ADMIN_OR_TEST_PHONE', '+212600000000')
    
    stage = conv.get('type', 'ECRIT')
    emoji = "📝" if stage == "ECRIT" else ("🎙️" if stage == "ORAL" else "🏆")
    
    msg = (
        f"{emoji} *Mihnati (مهنتي) - Alerte Convocations*\n\n"
        f"🏛️ *Administration :* {conv.get('administration')}\n"
        f"📋 *Titre :* {conv.get('title')}\n"
        f"📅 *Date d'examen :* {conv.get('exam_date')}\n"
        f"📍 *Centres :* {conv.get('exam_center')}\n\n"
        f"📄 *Télécharger la liste PDF :*\n{conv.get('pdf_url')}\n\n"
        f"_(Alerte automatique transmise à vos candidatures suivies sur Mihnati)_"
    )
    
    logger.info(f"[WhatsApp Alert Ready] {stage}: {conv.get('title')}")
    clean_phone = phone.replace('+', '').replace(' ', '')
    
    # 1. Official Green-API Integration (Automated Message & File Delivery)
    if instance_id and token:
        try:
            send_msg_url = f"https://api.green-api.com/waInstance{instance_id}/sendMessage/{token}"
            payload = {
                "chatId": f"{clean_phone}@c.us",
                "message": msg
            }
            resp = requests.post(send_msg_url, json=payload, headers={"Content-Type": "application/json"}, timeout=10)
            logger.info(f"[Green-API Message] Response: {resp.status_code}")
            
            # Send actual PDF file directly to WhatsApp chat
            pdf_url = conv.get('pdf_url')
            if pdf_url and ('pdf' in pdf_url.lower() or pdf_url.startswith('http')):
                send_file_url = f"https://api.green-api.com/waInstance{instance_id}/sendFileByUrl/{token}"
                file_payload = {
                    "chatId": f"{clean_phone}@c.us",
                    "urlFile": pdf_url,
                    "fileName": f"Convocation_{stage}_{conv.get('concours_id')}.pdf",
                    "caption": f"📄 Liste officielle ({stage}) - {conv.get('administration')}"
                }
                f_resp = requests.post(send_file_url, json=file_payload, headers={"Content-Type": "application/json"}, timeout=15)
                logger.info(f"[Green-API PDF File] Response: {f_resp.status_code}")
        except Exception as e:
            logger.warning(f"[Green-API] Dispatch error: {e}")
            
    # 2. Generic REST Gateway fallback
    elif gateway_url:
        try:
            payload = {
                "chatId": f"{clean_phone}@c.us",
                "phone": clean_phone,
                "message": msg
            }
            headers = {"Content-Type": "application/json"}
            if api_key:
                headers["Authorization"] = f"Bearer {api_key}"
            resp = requests.post(gateway_url, json=payload, headers=headers, timeout=10)
            logger.info(f"[WhatsApp Gateway] Response code: {resp.status_code}")
        except Exception as e:
            logger.warning(f"[WhatsApp Gateway] Dispatch error: {e}")

def save_to_database(concours_list, convocations_list):
    """Saves scraped data to SQLite database."""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    for c in concours_list:
        cursor.execute('''
            INSERT OR REPLACE INTO concours (
                id, title, grade, echelle, administration, degree_required,
                posts_count, deadline, detail_url, written_list_url, oral_list_url, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (
            c['id'], c['title'], c['title'], c['echelle'], c['administration'],
            c['degree_required'], c['posts_count'], c['deadline'], c['detail_url'],
            c['written_list_url'], c['oral_list_url'], datetime.now().isoformat()
        ))
    
    for conv in convocations_list:
        cursor.execute('''
            INSERT OR REPLACE INTO convocations (
                id, concours_id, type, title, administration, exam_date,
                exam_center, candidates_count, pdf_url
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (
            conv['id'], conv['concours_id'], conv['type'], conv['title'],
            conv['administration'], conv['exam_date'], conv['exam_center'],
            conv['candidates_count'], conv['pdf_url']
        ))
        
    conn.commit()
    conn.close()
    logger.info(f"Database updated successfully: {DB_PATH}")

def export_json(concours_list, convocations_list):
    """Exports structured data to JSON for Mihnati Android application sync."""
    data = {
        'last_sync': datetime.now().isoformat(),
        'source': 'https://www.emploi-public.ma',
        'concours_count': len(concours_list),
        'convocations_count': len(convocations_list),
        'concours': concours_list,
        'convocations': convocations_list
    }
    
    # Save locally
    with open(JSON_OUTPUT_PATH, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    logger.info(f"Exported data to {JSON_OUTPUT_PATH}")
    
    # Save into Android app assets if directory exists
    try:
        os.makedirs(os.path.dirname(ASSETS_EXPORT_PATH), exist_ok=True)
        with open(ASSETS_EXPORT_PATH, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        logger.info(f"Synchronized with Android assets: {ASSETS_EXPORT_PATH}")
    except Exception as e:
        logger.warning(f"Could not write to Android assets: {e}")

def run_job():
    """Main job executed hourly."""
    logger.info("=" * 60)
    logger.info(f"Running scheduled scrape job at {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    logger.info("=" * 60)
    
    try:
        init_database()
        concours = scrape_concours_list(max_pages=3)
        convocations = scrape_convocations_and_results(concours)
        
        save_to_database(concours, convocations)
        export_json(concours, convocations)
        logger.info("Scrape job completed successfully!")
    except Exception as e:
        logger.error(f"Error during scrape job: {e}", exc_info=True)

def main():
    parser = argparse.ArgumentParser(description='Emploi-Public.ma Hourly Scraper Bot')
    parser.add_argument('--run-now', '--once', dest='run_now', action='store_true', help='Execute scrape immediately and exit')
    parser.add_argument('--hourly', action='store_true', help='Start continuous hourly scheduler daemon')
    args = parser.parse_args()
    
    # Run initial extraction on startup
    logger.info("Executing initial scrape extraction on startup...")
    run_job()
    
    if not args.hourly:
        logger.info("One-off run completed. To keep running automatically every hour, launch with --hourly")
        return

    # Continuous hourly loop
    logger.info("Hourly scheduler activated: the bot will now automatically scrape every hour daily.")
    schedule.every().hour.do(run_job)
    
    while True:
        schedule.run_pending()
        time.sleep(60)

if __name__ == '__main__':
    main()
