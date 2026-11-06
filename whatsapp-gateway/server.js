const express = require('express');
const cors = require('cors');
const qrcode = require('qrcode');
const axios = require('axios');
const fs = require('fs');
const path = require('path');
const pino = require('pino');
require('dotenv').config();

const {
    default: makeWASocket,
    useMultiFileAuthState,
    DisconnectReason,
    fetchLatestBaileysVersion,
    makeInMemoryStore
} = require('@whiskeysockets/baileys');

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 3000;
const API_SECRET = process.env.API_SECRET || 'mihnati_secret_gateway_2026';
const AUTH_DIR = process.env.AUTH_DIR || path.join(__dirname, 'auth_info_baileys');

if (!fs.existsSync(AUTH_DIR)) {
    fs.mkdirSync(AUTH_DIR, { recursive: true });
}

let sock = null;
let latestQrBase64 = null;
let isConnected = false;
let connectedUser = null;
let pairingCode = null;

// ==========================================
// 1. BAILEYS WHATSAPP CONNECTION LIFECYCLE
// ==========================================
async function startWhatsApp() {
    const { state, saveCreds } = await useMultiFileAuthState(AUTH_DIR);
    const { version } = await fetchLatestBaileysVersion();

    sock = makeWASocket({
        version,
        logger: pino({ level: 'silent' }),
        printQRInTerminal: true,
        auth: state,
        browser: ['Mihnati Dispatcher', 'Chrome', '1.0.0']
    });

    sock.ev.on('creds.update', saveCreds);

    sock.ev.on('connection.update', async (update) => {
        const { connection, lastDisconnect, qr } = update;

        if (qr) {
            try {
                latestQrBase64 = await qrcode.toDataURL(qr);
            } catch (err) {
                console.error('Failed generating QR code:', err);
            }
        }

        if (connection === 'close') {
            isConnected = false;
            connectedUser = null;
            latestQrBase64 = null;

            const statusCode = lastDisconnect?.error?.output?.statusCode;
            const shouldReconnect = statusCode !== DisconnectReason.loggedOut;
            console.log(`[WhatsApp] Connection closed (code: ${statusCode}). Reconnecting: ${shouldReconnect}`);

            if (shouldReconnect) {
                setTimeout(startWhatsApp, 3000);
            } else {
                console.log('[WhatsApp] Logged out. Clearing credentials directory...');
                try {
                    fs.rmSync(AUTH_DIR, { recursive: true, force: true });
                } catch (_) {}
                setTimeout(startWhatsApp, 2000);
            }
        } else if (connection === 'open') {
            isConnected = true;
            latestQrBase64 = null;
            pairingCode = null;
            connectedUser = sock.user?.id || 'Connected';
            console.log(`[WhatsApp] Successfully connected! Account: ${connectedUser}`);
        }
    });
}

startWhatsApp();

// ==========================================
// 2. HELPER FUNCTIONS
// ==========================================
function normalizePhone(raw) {
    if (!raw) return null;
    let clean = String(raw).replace(/[^0-9]/g, '');
    if (clean.startsWith('00212')) clean = clean.substring(2);
    if (clean.startsWith('0') && (clean.length === 10)) {
        clean = '212' + clean.substring(1);
    }
    if (!clean.startsWith('212') && clean.length === 9) {
        clean = '212' + clean;
    }
    return clean;
}

function authMiddleware(req, res, next) {
    const headerKey = req.headers['x-api-key'] || req.headers['authorization'];
    const queryKey = req.query.key;

    const providedKey = headerKey ? headerKey.replace(/^Bearer\s+/i, '') : queryKey;

    if (!API_SECRET || providedKey === API_SECRET) {
        return next();
    }
    return res.status(401).json({ error: 'Unauthorized: Invalid or missing API Key.' });
}

// ==========================================
// 3. WEB DASHBOARD FOR QR CODE & PAIRING
// ==========================================
app.get('/', (req, res) => {
    res.redirect('/qr');
});

app.get('/qr', (req, res) => {
    const html = `
    <!DOCTYPE html>
    <html lang="fr">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Mihnati - Passerelle WhatsApp 24/7</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <meta http-equiv="refresh" content="${isConnected ? '30' : '6'}">
    </head>
    <body class="bg-slate-900 text-slate-100 flex items-center justify-center min-h-screen p-4 font-sans">
        <div class="max-w-md w-full bg-slate-800 border border-slate-700 rounded-2xl p-6 shadow-2xl text-center">
            <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-xs font-semibold uppercase tracking-wider mb-4">
                🇲🇦 Mihnati Cloud Gateway
            </div>
            
            <h1 class="text-xl font-bold text-white mb-2">Connexion WhatsApp Business</h1>
            <p class="text-xs text-slate-400 mb-6">Passerelle automatique 24/7 pour codes OTP et convocations PDF</p>

            ${isConnected ? `
                <div class="bg-emerald-950/60 border border-emerald-500/40 rounded-xl p-6 mb-6">
                    <div class="w-12 h-12 bg-emerald-500/20 text-emerald-400 rounded-full flex items-center justify-center mx-auto mb-3 text-2xl">✓</div>
                    <h3 class="text-base font-bold text-emerald-300">WhatsApp Connecté & Opérationnel</h3>
                    <p class="text-xs text-slate-300 mt-1 font-mono break-all">${connectedUser}</p>
                    <p class="text-xs text-emerald-400/80 mt-3">Prêt à expédier les messages et fichiers PDF automatiquement.</p>
                </div>
            ` : latestQrBase64 ? `
                <div class="bg-white p-4 rounded-xl inline-block shadow-lg mb-4">
                    <img src="${latestQrBase64}" alt="QR Code" class="w-56 h-56 mx-auto" />
                </div>
                <div class="text-left bg-slate-900/60 p-4 rounded-xl text-xs space-y-2 border border-slate-700 text-slate-300 mb-4">
                    <p class="font-bold text-white mb-1">Pour synchroniser en 10 secondes :</p>
                    <p>1. Ouvrez <strong>WhatsApp Business</strong> sur votre téléphone.</p>
                    <p>2. Allez dans <strong>Paramètres</strong> ➔ <strong>Appareils connectés</strong>.</p>
                    <p>3. Appuyez sur <strong>Connecter un appareil</strong> et scannez ce code QR.</p>
                </div>
                <p class="text-[11px] text-slate-500">Cette page s'actualise automatiquement toutes les 6 secondes.</p>
            ` : `
                <div class="py-12">
                    <div class="w-10 h-10 border-4 border-emerald-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
                    <p class="text-sm text-slate-300">Initialisation de la session WhatsApp...</p>
                    <p class="text-xs text-slate-500 mt-2">Le QR code va apparaître d'un instant à l'autre.</p>
                </div>
            `}

            <div class="border-t border-slate-700/60 pt-4 mt-6 text-xs text-slate-500 flex justify-between">
                <span>Statut : <span class="${isConnected ? 'text-emerald-400 font-bold' : 'text-amber-400'}">${isConnected ? 'EN LIGNE' : 'EN ATTENTE'}</span></span>
                <span>Port : ${PORT}</span>
            </div>
        </div>
    </body>
    </html>
    `;
    res.send(html);
});

// ==========================================
// 4. REST API ENDPOINTS FOR MIHNATI APP
// ==========================================

// Health Check
app.get('/health', (req, res) => {
    res.json({
        status: isConnected ? 'online' : 'connecting',
        authenticated: isConnected,
        account: connectedUser || null,
        timestamp: new Date().toISOString()
    });
});

// Send 6-Digit Verification OTP / Text Message
app.post(['/send-otp', '/send-text', '/sendMessage'], authMiddleware, async (req, res) => {
    try {
        if (!isConnected || !sock) {
            return res.status(503).json({ error: 'WhatsApp gateway not connected yet. Please scan QR code first.' });
        }

        const body = req.body || {};
        const rawPhone = body.phone || body.chatId || '';
        const cleanPhone = normalizePhone(rawPhone);

        if (!cleanPhone) {
            return res.status(400).json({ error: 'Missing or invalid phone number (+212...)' });
        }

        const jid = `${cleanPhone}@s.whatsapp.net`;
        let textToSend = body.message;

        // If app sent otp parameter
        if (body.otp) {
            const name = body.name || 'Candidat';
            textToSend = `🇲🇦 *Mihnati Maroc*\n\nBonjour ${name},\nVotre code de vérification WhatsApp est :\n\n🔐 *${body.otp}*\n\nCe code expire dans 10 minutes. Ne le partagez avec personne.`;
        }

        if (!textToSend) {
            return res.status(400).json({ error: 'Message text or OTP is required.' });
        }

        const sent = await sock.sendMessage(jid, { text: textToSend });
        return res.json({
            success: true,
            phone: cleanPhone,
            messageId: sent?.key?.id || 'delivered',
            timestamp: Date.now()
        });
    } catch (err) {
        console.error('Error sending WhatsApp message:', err);
        return res.status(500).json({ error: err.message || 'Failed sending WhatsApp message.' });
    }
});

// Send PDF Convocation Document
app.post(['/send-pdf', '/send-document', '/sendFileByUrl'], authMiddleware, async (req, res) => {
    try {
        if (!isConnected || !sock) {
            return res.status(503).json({ error: 'WhatsApp gateway not connected yet. Please scan QR code first.' });
        }

        const body = req.body || {};
        const rawPhone = body.phone || body.chatId || '';
        const cleanPhone = normalizePhone(rawPhone);
        const pdfUrl = body.pdfUrl || body.urlFile;
        const fileName = body.fileName || 'Convocation_Mihnati.pdf';
        const caption = body.caption || '📄 Liste officielle et convocation officielle Mihnati';

        if (!cleanPhone || !pdfUrl) {
            return res.status(400).json({ error: 'Phone and pdfUrl (or urlFile) are required.' });
        }

        const jid = `${cleanPhone}@s.whatsapp.net`;

        // Download PDF document buffer
        const response = await axios.get(pdfUrl, {
            responseType: 'arraybuffer',
            timeout: 25000,
            headers: {
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'
            }
        });

        const buffer = Buffer.from(response.data);

        // Send via WhatsApp as official PDF document
        const sent = await sock.sendMessage(jid, {
            document: buffer,
            mimetype: 'application/pdf',
            fileName: fileName,
            caption: caption
        });

        return res.json({
            success: true,
            phone: cleanPhone,
            fileName: fileName,
            messageId: sent?.key?.id || 'delivered',
            timestamp: Date.now()
        });
    } catch (err) {
        console.error('Error sending PDF document:', err);
        return res.status(500).json({ error: err.message || 'Failed delivering PDF document to WhatsApp.' });
    }
});

app.listen(PORT, '0.0.0.0', () => {
    console.log(`===============================================`);
    console.log(`🚀 Mihnati WhatsApp Gateway running on port ${PORT}`);
    console.log(`📱 Scan QR Code at: http://localhost:${PORT}/qr`);
    console.log(`🔒 API Key Protected: ${API_SECRET ? 'YES' : 'NO'}`);
    console.log(`===============================================`);
});
