# Buscador Local v0.4.0
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

## v0.3.2 — correção de inicialização
- Remove desenho personalizado de botões e callbacks de animação/insets da v0.3.1. Utiliza ImageButton e vetores Android, tema compatível com targetSdk 35 e adjustResize nativo. Lista de conversas, SQLite, modelo e assinatura mantidos.
- Falhas síncronas de inicialização mostram tela de diagnóstico copiável sem apagar dados. Falhas assíncronas ainda exigem logcat. Causa original não confirmada sem stack trace.
- CI publica APK ARM64 e depois executa smoke test de interface em emulador API 35 com APK x86_64 separado, não distribuído. Verifica abertura, foco, digitação, deslocamento e fechamento do teclado. Não valida a inferência ARM64 nem o teclado Samsung.
- Os testes geométricos do KeyboardLayout pertencem à implementação anterior e não são evidência de funcionamento do novo layout.

## v0.3.3 — causa reproduzida em emulador
O teste API 35 da v0.3.2 registrou NullPointerException em PhoneWindow.getInsetsController, chamado em MainActivity.createChatUi antes de setContentView. Removido acesso prematuro ao controlador; o tema define a aparência das barras. setDecorFitsSystemWindows é chamado somente após criar o conteúdo. Distribuição ARM64 agora condicionada à aprovação do smoke test de interface x86_64. O APK é preservado separadamente antes de gerar a variante de teste.

## v0.4.0 — inferência residente e resposta limpa
- Pré-carrega GGUF salvo ao abrir, ou após importar, e mantém modelo residente enquanto o processo existir. Sem descarregar por turno. Contexto nativo da conversa ativa é reutilizado, fontes idênticas não são reenviadas; troca de conversa, erro, cancelamento ou limite de janela invalidam o contexto. Fallback reconstrói contexto e reduz evidências. Limites de evidências 2200/1100 caracteres, histórico frio 850, saída 384 tokens. Sem cache de várias conversas para limitar RAM.
- Streaming visual limitado a atualizações a cada 90 ms; parser separa tags think/thinking/analysis, inclusive tags parciais. Resposta final visível; detalhes recolhidos, disponíveis por toque. Dados brutos mantidos localmente para reabrir detalhes. Prévia da conversa usa resposta limpa. Tags sem abertura só podem ser reconhecidas quando chega fechamento; não há garantia para pensamento sem marcação.
- Menu Desempenho e detalhes mostram carregamentos, tempo de carga, preparação do contexto, primeira saída, primeira resposta visível e total. Contagem de fragmentos não equivale a tokens/s. Não há benchmark de inferência em ARM64 no CI; desempenho real precisa ser medido no aparelho. Android pode matar o processo sob pressão de memória.

## v0.5.0 — chat agêntico
- Remove botão Web e permite conversa sem fontes prévias. O modelo escolhe search_web ou search_sources; busca DDG/Tor exclusiva, até três páginas por chamada, fontes persistidas no SQLite.
- Registro local de ferramentas com JSON Schema e despacho JSON-RPC 2.0 com initialize, tools/list e tools/call (subconjunto MCP 2025-11-25, transporte em processo; sem servidores remotos/HTTP/stdio nesta versão). NativeToolSchema e retorno tool role se suportados pelo GGUF; fallback textual identificado para outros templates.
- Até três chamadas por turno, sem chamadas duplicadas; validação de nome e argumento, resultado limitado a 650 caracteres. Tags de chamadas ocultas da UI. Não executa chamadas dentro de tags de pensamento. Sem acesso arbitrário a rede, shell, arquivos ou URLs.
- Testes unitários do parser e smoke UI; escolha autônoma e chamadas nativas com GGUF real ainda exigem validação ARM64. Não afirma conformidade integral MCP nem suporte a servidores externos.
