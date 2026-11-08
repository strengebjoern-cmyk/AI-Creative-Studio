# AI Creative Studio – Android MVP 0.1

Native Kotlin / Jetpack Compose Android-App, komplett ohne kostenpflichtige APIs.

## Funktionen
- Drei offline erzeugte Storyboard-Vorlagen pro Thema (regelbasiert, keine generative KI)
- Optional echte Textgenerierung über eigenen Ollama-Server
- Storyboards bearbeiten, lokal speichern, löschen und über Android teilen
- Dunkles UI in deutscher Sprache

## Installation / Build
1. Android Studio (aktuelle Version mit JDK 17) installieren.
2. Dieses Verzeichnis als Projekt öffnen und Gradle synchronisieren lassen.
3. Android SDK Platform 35 installieren, falls angefordert.
4. Über Run auf Android-Gerät starten oder Build > Build APK(s) ausführen.
5. APK anschließend auf dem Smartphone installieren.

## Ollama (optional)
1. Ollama auf einem PC im gleichen vertrauenswürdigen Netzwerk installieren.
2. `ollama pull llama3.2` ausführen.
3. Ollama so konfigurieren, dass es im lokalen Netzwerk erreichbar ist (z. B. OLLAMA_HOST=0.0.0.0:11434; Firewall auf das eigene LAN beschränken).
4. In der App die LAN-IP des Rechners eintragen. Android-Emulator: http://10.0.2.2:11434.
5. Keine ungeschützte Ollama-Schnittstelle im Internet veröffentlichen.

## Grenzen
Dies ist ein Quellcode-Projekt, keine vorgebaute APK. Es wurde hier nicht mit Android SDK/Gradle kompiliert oder auf einem Gerät getestet. KI-Bilder, KI-Videos, Audio und MP4-Export sind noch nicht implementiert. Sie benötigen zusätzliche Modelle/Integrationen und weitere Entwicklung. Die lokale Speicherung erfolgt in App-Preferences und ist für einen frühen Prototyp gedacht.
