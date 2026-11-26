# Buscador Local v0.3.1
Android ARM64 (11+), DuckDuckGo HTML, Tor incorporado obrigatório e GGUF local. Motor reutilizado do LFM3Local no commit 0e196e8831220b782cc78535b6d6729940169373. Sem servidor de inferência, conta ou Orbot. Modelo não incluído no APK. Sem modelo, busca e salva resultados e fontes. Com modelo, permite síntese e perguntas subsequentes. Não suporta PDFs ou sites dependentes de JavaScript. Não é uma ferramenta de anonimato auditada: verificar vazamentos DNS/IP e conexão real no aparelho. CAPTCHA ou bloqueio interrompem a pesquisa, sem fallback direto. Consultas são enviadas ao DuckDuckGo. Fontes são dados não confiáveis; citações e respostas exigem conferência.

## Build
JDK 17, Gradle 9.5.0, Android SDK 36 e 37.2, NDK 29.0.13113456 e CMake 3.22.1.
```sh
bash scripts/bootstrap-engine.sh
sh tests/run.sh
gradle :app:assembleDebug
```
APK em app/build/outputs/apk/debug/app-debug.apk. GitHub Actions compila e publica artefato para teste.
Licenças: preservar os avisos do LFM3Local, llama.cpp e dependências.

## Correção de build
A biblioteca Tor 0.4.9.17 exige compileSdk 37. O app usa 37.2, mantendo o motor nativo com 36. AGP 9.3.3 e Gradle 9.5.0 suportam API 37; o pacote oficial é platforms;android-37.2. O Kotlin externo e DSL legada são mantidos para compatibilidade com o motor fixado. Não reduzimos a versão do Tor por segurança.

A compilação 03 confirmou disponibilidade de SDK 37.2 e identificou incompatibilidade de tipo no DNS do OkHttp. A correção implementa explicitamente Dns.lookup para recusar resolução local, preservando o SOCKS do Tor.

## Chat de pesquisa v0.2.0
- Conversas independentes em SQLite privado: mensagens, fontes, trechos, data e consulta de origem.
- Buscar envia apenas o texto digitado ao DuckDuckGo via Tor, registra até 10 resultados e tenta ler até quatro páginas. Conversar nunca acessa a rede e funciona com Tor desconectado.
- Novas buscas acumulam evidências no chat; fontes iguais são deduplicadas, alterações preservam novas versões e IDs de citações antigos.
- Recuperação local por termos com influência da consulta anterior, trechos limitados e histórico recente. Não envia toda a base ao modelo; não há embeddings, busca autônoma nem garantia de citações corretas. Trechos do buscador são identificados como não verificados.
- Se a janela estourar, repete uma vez com menos evidência e sem histórico. O chat completo continua em disco. Sem modelo, as buscas ainda alimentam a base.
- Botão Fontes mostra os textos salvos e URLs sem abrir navegador externo. Nova conversa não apaga anteriores.
- A chave Debug da v0.1.0 não foi persistida. Pode ser necessário reinstalar; guarde o GGUF original. A partir desta versão o CI tenta preservar a chave em cache (não é garantia permanente).
- Testes JVM incluem seleção, limite de contexto, recuperação de assuntos antigos e estabilidade de citações. Validar inferência e privacidade real no aparelho.

## v0.2.1 — rede e interface
- Corrige caminho provável de NetworkOnMainThreadException após salvar páginas: cancelamento e encerramento de conexões OkHttp passam para Dispatchers.IO, inclusive finally e lifecycle; checagem explícita impede close na thread principal. Consulta ao circuito também em IO. Não desativa StrictMode.
- Aguarda inicialização do motor antes de carregar GGUF e registra etapa, tipo e stack trace de falhas via adb logcat -s BuscaLocal.
- Cabeçalho compacto, bolhas alinhadas, compositor fixo, ações de envio local, busca Tor e parada; fontes e modelos acessíveis no cabeçalho. Mantém SQLite e package ID. Testar no aparelho, incluindo cancelar busca, reabrir conversa e conversar offline.
- Causa exata do erro reportado não confirmada sem stack trace de dispositivo; build não comprova execução em smartphone.

## v0.3.0 — interface de mensageiro
- Tela dedicada de conversas com prévia de mensagens, avatar e contagem de fontes; acesso pelo ícone voltar. Identificação visível da versão na tela de conversas e no menu.
- Cabeçalho branco, ícones desenhados em vetor, balões assimétricos, compositor em uma linha com globo de busca e envio circular. Indicadores técnicos aparecem durante o processamento e erros; modelo no menu. Resultados da busca condensados visualmente, mantendo fontes e histórico no SQLite.
- Insets de barras e teclado tratados explicitamente. Sem alteração do banco ou motor. CI verifica versionName do APK. Não há captura de tela real nem execução física nesta infraestrutura.

## v0.3.1 — teclado e compositor
- Insets do IME e animação, com fallback pela área visível e compensação do redimensionamento já aplicado à janela. Compositor permanece abaixo da lista de mensagens, acima do teclado; ajuste de altura ocorre na lista.
- Compositor compacto com campo sem contorno de foco, botão Web identificado, envio circular e paleta neutra. Mantém a lista de conversas da v0.3.0.
- Testes de geometria do teclado no JVM; validação com teclado físico/virtual no Android ainda necessária.
