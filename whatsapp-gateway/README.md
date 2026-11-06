# 🇲🇦 Mihnati - Passerelle WhatsApp Cloud 24/7 (Baileys)

Passerelle WhatsApp autonome pour l'application **Mihnati Maroc** :
- **100% Gratuite & Illimitée** (Aucune limite de 3 contacts comme Green-API).
- **Fonctionne 24h/24 & 7j/7**, même quand votre PC est éteint.
- **Envoi des codes de vérification OTP à 6 chiffres** aux candidats marocains (`+212 6...` / `07...`).
- **Envoi des fichiers PDF officiels de convocations** (Épreuves écrites, orales, listes des admis).
- **Sécurisée par clé API** (`API_SECRET`).

---

## 🚀 Déploiement Gratuit en 2 Minutes sur Render.com

### Étape 1 : Créer un compte gratuit sur Render
1. Rendez-vous sur **[dashboard.render.com](https://dashboard.render.com)**.
2. Créez un compte gratuit (ou connectez-vous avec GitHub).

### Étape 2 : Créer un nouveau Web Service
1. Cliquez sur le bouton **New +** ➔ **Web Service**.
2. Connectez votre dépôt GitHub `mihnati-scraper`.
3. Remplissez les champs de configuration :
   - **Name :** `mihnati-whatsapp-gateway`
   - **Root Directory :** `whatsapp-gateway`
   - **Runtime :** `Node`
   - **Build Command :** `npm install`
   - **Start Command :** `node server.js`
   - **Instance Type :** `Free` ($0.00/mois)
4. Dans **Environment Variables**, ajoutez :
   - `API_SECRET` = `mihnati_secret_gateway_2026` (ou votre propre mot de passe)
5. Cliquez sur **Deploy Web Service**.

---

## 📱 Étape 3 : Synchroniser votre WhatsApp Business (Une seule fois)

1. Une fois le déploiement terminé, Render vous donne une URL publique (ex: `https://mihnati-whatsapp-gateway.onrender.com`).
2. Ouvrez cette adresse dans votre navigateur en ajoutant `/qr` :
   ```
   https://mihnati-whatsapp-gateway.onrender.com/qr
   ```
3. Prenez votre téléphone avec **WhatsApp Business** :
   - Allez dans **Paramètres** (ou 3 points) ➔ **Appareils connectés**.
   - Appuyez sur **Connecter un appareil**.
   - Scannez le QR Code affiché sur votre écran.
4. L'écran devient vert : **WhatsApp Connecté & Opérationnel ✓**.

---

## 📲 Étape 4 : Activer dans l'Application Mihnati

1. Ouvrez l'application Mihnati sur Android.
2. Ouvrez le dialogue de connexion ➔ Onglet **WhatsApp 💬** ➔ Appuyez sur **Configurer la passerelle WhatsApp 24/7**.
3. Sélectionnez l'onglet **Cloud Illimité ⚡** :
   - **URL Passerelle Cloud :** `https://mihnati-whatsapp-gateway.onrender.com`
   - **Clé Secrète API :** `mihnati_secret_gateway_2026`
4. Appuyez sur **Enregistrer**.

Votre passerelle est désormais en ligne 24h/24, prête à délivrer automatiquement tous les codes et convocations PDF !
