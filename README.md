# Buscador Local v0.1.0
Android ARM64 (11+), DuckDuckGo HTML, Tor incorporado obrigatório e GGUF local. Motor reutilizado do LFM3Local no commit 0e196e8831220b782cc78535b6d6729940169373. Sem servidor de inferência, conta ou Orbot. Modelo não incluído no APK. Sem modelo, exibe resultados; com modelo, lê até 4 fontes e sintetiza com referências. Protótipo sem histórico, PDFs ou sites dependentes de JavaScript. Não é uma ferramenta de anonimato auditada: verificar vazamentos DNS/IP e conexão real no aparelho. CAPTCHA ou bloqueio interrompem a pesquisa, sem fallback direto. Consultas são enviadas ao DuckDuckGo. Fontes são dados não confiáveis; citações e respostas exigem conferência.

## Build
JDK 17, Gradle 8.14.3, Android SDK 36, NDK 29.0.13113456 e CMake 3.22.1.
```sh
bash scripts/bootstrap-engine.sh
sh tests/run.sh
gradle :app:assembleDebug
```
APK em app/build/outputs/apk/debug/app-debug.apk. GitHub Actions compila e publica artefato para teste.
Licenças: preservar os avisos do LFM3Local, llama.cpp e dependências.
