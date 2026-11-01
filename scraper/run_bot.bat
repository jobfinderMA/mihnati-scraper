@echo off
title Mihnati - Emploi-Public.ma Hourly Scraper Bot
echo ===============================================================
echo     MIHNATI مهنتي - Bot de Veille Automatique emploi-public.ma
echo ===============================================================
echo [INFO] Demarrage du robot de scraping horaire quotidien...
echo [INFO] Les donnees seront extraites chaque heure et exportees vers
echo        SQLite et l'application Android Mihnati.
echo.
cd /d "%~dp0"
python emploi_public_bot.py --hourly
pause
