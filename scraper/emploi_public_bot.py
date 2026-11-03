"""
Emploi-Public.ma Automated Scraper Bot (Mihnati مهنتي)
Scrapes Moroccan public sector recruitments, scales, diplomas, written & oral convocations.
Runs on an hourly schedule daily and exports structured data to JSON and SQLite.
"""

import os
import re
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



ADMIN_TRANSLATION_MAP = {
    'جامعة ابن طفيل': ('Université Ibn Tofail - Kénitra', 'جامعة ابن طفيل - القنيطرة'),
    'جامعة السلطان مولاي سليمان': ('Université Sultan Moulay Slimane - Béni Mellal', 'جامعة السلطان مولاي سليمان - بني ملال'),
    'غرفة التجارة والصناعة والخدمات لدرعة تافيلالت': ('Chambre de Commerce CCIS Drâa-Tafilalet', 'غرفة التجارة والصناعة والخدمات لدرعة تافيلالت'),
    'وكالة الحوض المائي لدرعة واد نون': ('Agence du Bassin Hydraulique de Drâa Oued Noun', 'وكالة الحوض المائي لدرعة واد نون'),
    'إقليم زاكورة': ('Province de Zagora - Tazzarine', 'إقليم زاكورة - تازارين'),
    'أطلس اون لاين': ('Atlas On Line (Royal Air Maroc)', 'أطلس أون لاين / الخطوط الملكية المغربية'),
    'الخطوط الملكية المغربية': ('Royal Air Maroc (RAM)', 'الخطوط الملكية المغربية'),
    'شركة التنمية المحلية مراكش مسافر': ('Société de Développement Local (SDL) Marrakech', 'شركة التنمية المحلية مراكش مسافر'),
    'الشركة الجهوية متعددة الخدمات بني ملال': ('Société Régionale Multiservices (SRM) Béni Mellal', 'الشركة الجهوية متعددة الخدمات بني ملال - خنيفرة'),
    'جامعة محمد الخامس': ('Université Mohammed V - Rabat', 'جامعة محمد الخامس - الرباط'),
    'جامعة الحسن الثاني': ('Université Hassan II - Casablanca', 'جامعة الحسن الثاني - الدار البيضاء'),
    'جامعة القاضي عياض': ('Université Cadi Ayyad - Marrakech', 'جامعة القاضي عياض - مراكش'),
    'جامعة سيدي محمد بن عبد الله': ('Université Sidi Mohamed Ben Abdellah - Fès', 'جامعة سيدي محمد بن عبد الله - فاس'),
}

GRADE_TRANSLATION_MAP = {
    'متصرف من الدرجة الثانية': ('Concours Administrateur 2ème grade (Échelle 11)', 'مباراة متصرف من الدرجة الثانية (السلم 11)'),
    'متصرف من الدرجة الثالثة': ('Concours Administrateur 3ème grade (Échelle 10)', 'مباراة متصرف من الدرجة الثالثة (السلم 10)'),
    'مهندس دولة من الدرجة الأولى': ("Concours Ingénieur d'État 1er grade (Échelle 11)", 'مباراة مهندس دولة من الدرجة الأولى (السلم 11)'),
    'مهندس معماري من الدرجة الأولى': ('Concours Architecte 1er grade (Échelle 11)', 'مباراة مهندس معماري من الدرجة الأولى (السلم 11)'),
    'تقني من الدرجة الثالثة': ('Concours Technicien 3ème grade (Échelle 9)', 'مباراة تقني من الدرجة الثالثة (السلم 9)'),
    'تقني من الدرجة الرابعة': ('Concours Technicien 4ème grade (Échelle 8)', 'مباراة تقني من الدرجة الرابعة (السلم 8)'),
    'مساعد تقني من الدرجة الثانية': ('Concours Adjoint Technique 2ème grade', 'مباراة مساعد تقني من الدرجة الثانية'),
    'مساعد إداري من الدرجة الثانية': ('Concours Adjoint Administratif 2ème grade', 'مباراة مساعد إداري من الدرجة الثانية'),
    'مستشار الهاتف': ('Recrutement Téléconseiller Clientèle', 'مباراة توظيف مستشار الهاتف'),
    'مسؤول اداري ومالي': ('Recrutement Responsable Administratif et Financier', 'توظيف مسؤول إداري ومالي'),
    'مسؤول': ("Recrutement Cadre Responsable d'exploitation", 'توظيف مسؤول'),
}

def translate_manasseb_fields(raw_text):
    admin_fr = "Administration Publique"
    admin_ar = "الإدارة العمومية"
    for k, (v_fr, v_ar) in ADMIN_TRANSLATION_MAP.items():
        if k in raw_text:
            admin_fr, admin_ar = v_fr, v_ar
            break

    title_fr = "Concours de Recrutement"
    title_ar = "مباراة توظيف"
    for k, (v_fr, v_ar) in GRADE_TRANSLATION_MAP.items():
        if k in raw_text:
            title_fr, title_ar = v_fr, v_ar
            break
            
    return admin_fr, admin_ar, title_fr, title_ar

def scrape_manasseb_posts(limit=35):
    """
    Scrapes latest opportunities and convocations from Manasseb.com
    via the official WordPress REST API with clean bilingual titles.
    """
    logger.info(f"Fetching latest opportunities from Manasseb.com (limit={limit})...")
    concours_list = []
    convocations_list = []
    
    try:
        url = f"https://www.manasseb.com/wp-json/wp/v2/posts?per_page={limit}"
        resp = requests.get(url, headers=HEADERS, timeout=15, verify=False)
        if resp.status_code != 200:
            logger.warning(f"Manasseb API returned status code {resp.status_code}")
            return [], []
            
        posts = resp.json()
        logger.info(f"Retrieved {len(posts)} posts from Manasseb.com")
        
        for p in posts:
            p_id = p.get('id', '')
            raw_title = p.get('title', {}).get('rendered', '')
            raw_title = BeautifulSoup(raw_title, 'html.parser').get_text().strip()
            raw_title = re.sub(r'&#8211;', '-', raw_title)
            raw_title = re.sub(r'&#8217;', "'", raw_title)
            raw_title = re.sub(r'&amp;', '&', raw_title)
            link = p.get('link', '')
            c_html = p.get('content', {}).get('rendered', '')
            pub_date = p.get('date', '')[:10]

            admin_fr, admin_ar, title_fr, title_ar = translate_manasseb_fields(raw_title)

            # Deadline extraction
            dl_match = re.search(r'آخر أجل(?: لإيداع الترشيحات)?\s*:\s*([^<\n\r]+)', c_html)
            deadline = dl_match.group(1).strip() if dl_match else "Novembre 2026"
            deadline = re.sub(r'&#8211;', '-', deadline).strip()

            # Exam date extraction
            exam_match = re.search(r'تاريخ إجراء المباراة\s*:\s*([^<\n\r]+)', c_html)
            exam_date = exam_match.group(1).strip() if exam_match else ""

            # Posts count extraction
            posts_match = re.search(r'عدد المناصب\s*:\s*(?:<[^>]+>|\s)*(\d+)', c_html)
            posts_count = int(posts_match.group(1)) if posts_match else 1

            # Échelle extraction
            echelle = 11
            if '11' in raw_title or 'السلم 11' in c_html: echelle = 11
            elif '10' in raw_title or 'السلم 10' in c_html: echelle = 10
            elif '9' in raw_title or 'السلم 9' in c_html: echelle = 9
            elif '8' in raw_title or 'السلم 8' in c_html: echelle = 8

            # Distinguish Convocations (Listes d'examen) from Concours
            is_convocation = any(w in raw_title for w in [
                'لائحة', 'لوائح', 'المدعوين', 'المرشحين', 'شفوي', 'كتابي', 'نتائج'
            ])

            if is_convocation:
                conv_type = 'ORAL' if 'شفوي' in raw_title else ('ECRIT' if 'كتابي' in raw_title else 'RESULTAT')
                convocations_list.append({
                    'id': f"manasseb-conv-{p_id}",
                    'concours_id': f"manasseb-{p_id}",
                    'type': conv_type,
                    'title': f"{title_fr} ({conv_type})",
                    'administration': admin_fr,
                    'exam_date': exam_date or deadline,
                    'exam_center': "Maroc",
                    'candidates_count': posts_count * 25,
                    'pdf_url': link,
                    'source': 'manasseb.com'
                })
            else:
                concours_list.append({
                    'id': f"manasseb-{p_id}",
                    'title': title_fr,
                    'title_ar': title_ar,
                    'grade': f"Échelle {echelle}",
                    'echelle': str(echelle),
                    'administration': admin_fr,
                    'administration_ar': admin_ar,
                    'speciality': "Administration & Métiers",
                    'degree_required': "Bac+3 / Bac+5 / Master / Licence",
                    'posts_count': posts_count,
                    'deadline': deadline,
                    'exam_date': exam_date,
                    'publication_date': pub_date,
                    'reference_code': f"MANASSEB-{p_id}",
                    'submission_url': link,
                    'detail_url': link,
                    'written_list_url': '',
                    'oral_list_url': '',
                    'source': 'manasseb.com',
                    'is_weekly_special': True
                })
        logger.info(f"Successfully processed from Manasseb.com: {len(concours_list)} concours, {len(convocations_list)} convocations")
    except Exception as e:
        logger.error(f"Error scraping Manasseb.com: {e}", exc_info=True)
        
    return concours_list, convocations_list

def deduplicate_concours_and_convocations(concours_list, convocations_list):
    """
    Strict deduplication across all sources (Emploi-Public + Manasseb).
    Eliminates identical titles, merges duplicate vacancies, and cleans lists.
    """
    # 1. Filter out convocations that were misplaced in concours array
    clean_concours = []
    for c in concours_list:
        t_low = c.get('title', '').lower()
        if any(w in t_low for w in ['publication de la liste', 'convoqués pour', 'examen écrit du concours', 'entretien oral']):
            conv_type = 'ORAL' if 'oral' in t_low else 'ECRIT'
            convocations_list.append({
                'id': f"conv-{c.get('id', '')}",
                'concours_id': c.get('id', ''),
                'type': conv_type,
                'title': c.get('title', ''),
                'administration': c.get('administration', 'Administration Publique'),
                'exam_date': c.get('deadline', 'Novembre 2026'),
                'exam_center': "Maroc",
                'candidates_count': c.get('posts_count', 1) * 30,
                'pdf_url': c.get('written_list_url') or c.get('detail_url') or "https://www.emploi-public.ma",
                'source': c.get('source') or 'emploi-public.ma'
            })
        else:
            clean_concours.append(c)

    # 2. Deduplicate concours by (normalized_admin, normalized_title)
    seen_concours = {}
    deduped_concours = []
    for c in clean_concours:
        t = c.get('title', '').lower()
        a = c.get('administration', '').lower()
        for w in ['avis', 'concours', 'recrutement', 'de', 'du', 'des', 'd', 'l', 'grade', 'echelle', 'poste', 'postes', '•', ':']:
            t = t.replace(w, '')
            a = a.replace(w, '')
        t_clean = ' '.join(t.split())[:22]
        a_clean = ' '.join(a.split())[:22]
        key = (a_clean, t_clean)

        if key in seen_concours:
            existing = seen_concours[key]
            # Accumulate posts count or take maximum
            existing['posts_count'] = max(existing.get('posts_count', 1), c.get('posts_count', 1))
            if not existing.get('exam_date') and c.get('exam_date'):
                existing['exam_date'] = c.get('exam_date')
            if 'manasseb' in c.get('source', '') and 'manasseb' not in existing.get('source', ''):
                existing['source'] = 'emploi-public.ma + manasseb.com'
        else:
            seen_concours[key] = c
            deduped_concours.append(c)

    # 3. Deduplicate convocations
    seen_convs = {}
    deduped_convs = []
    for conv in convocations_list:
        key = (conv.get('administration', '').lower().strip()[:20], conv.get('type', ''), conv.get('exam_date', '').strip())
        if key not in seen_convs:
            seen_convs[key] = conv
            deduped_convs.append(conv)

    return deduped_concours, deduped_convs

def save_to_database(concours_list, convocations_list):
    """Saves scraped concours and convocations into SQLite database."""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    # Clear older scraped records to keep DB completely deduplicated & fresh
    cursor.execute('DELETE FROM concours')
    cursor.execute('DELETE FROM convocations')
    
    for c in concours_list:
        cursor.execute('''
            INSERT OR REPLACE INTO concours (
                id, title, title_ar, echelle, administration, degree_required,
                posts_count, deadline, detail_url, written_list_url, oral_list_url, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (
            c['id'], c['title'], c.get('title_ar', c['title']), c.get('echelle', '11'), c['administration'],
            c.get('degree_required', 'Bac+5'), c.get('posts_count', 1), c.get('deadline', 'Novembre 2026'),
            c.get('detail_url', ''), c.get('written_list_url', ''), c.get('oral_list_url', ''), datetime.now().isoformat()
        ))
    
    for conv in convocations_list:
        cursor.execute('''
            INSERT OR REPLACE INTO convocations (
                id, concours_id, type, title, administration, exam_date,
                exam_center, candidates_count, pdf_url
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (
            conv['id'], conv.get('concours_id', ''), conv['type'], conv['title'],
            conv['administration'], conv['exam_date'], conv['exam_center'],
            conv['candidates_count'], conv['pdf_url']
        ))
        
    conn.commit()
    conn.close()
    logger.info(f"Database updated successfully with {len(concours_list)} concours and {len(convocations_list)} convocations: {DB_PATH}")

def export_json(concours_list, convocations_list):
    """Exports structured data to JSON for Mihnati Android application sync."""
    data = {
        'last_sync': datetime.now().isoformat(),
        'source': 'https://www.emploi-public.ma + https://www.manasseb.com',
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
        raw_concours = scrape_concours_list(max_pages=3)
        raw_convocations = scrape_convocations_and_results(raw_concours)
        
        # Scrape Manasseb.com
        manasseb_concours, manasseb_convocations = scrape_manasseb_posts(limit=30)
        raw_concours.extend(manasseb_concours)
        raw_convocations.extend(manasseb_convocations)

        # STRICT DEDUPLICATION AND CLEANING
        final_concours, final_convocations = deduplicate_concours_and_convocations(raw_concours, raw_convocations)

        save_to_database(final_concours, final_convocations)
        export_json(final_concours, final_convocations)
        logger.info(f"Scrape job completed successfully! {len(final_concours)} deduplicated concours, {len(final_convocations)} convocations.")
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
