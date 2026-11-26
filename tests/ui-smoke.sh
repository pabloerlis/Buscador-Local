#!/usr/bin/env bash
set -euo pipefail
APK="$1"
PKG=br.local.busca
adb install -r "$APK"
adb shell settings put secure show_ime_with_hard_keyboard 1
adb shell input keyevent 82
adb logcat -c
adb shell am start -W -n "$PKG/.MainActivity"
sleep 5
adb shell pidof "$PKG"
dump(){
  for i in 1 2 3 4 5; do
    if adb shell uiautomator dump /sdcard/buscalocal-ui.xml >/dev/null 2>&1 && adb pull /sdcard/buscalocal-ui.xml "$1" >/dev/null 2>&1; then return 0; fi
    sleep 1
  done
  return 1
}
trap 'adb logcat -d -s AndroidRuntime BuscaLocal > ui-smoke-log.txt || true; cat ui-smoke-log.txt; adb shell screencap -p /sdcard/buscalocal-screen.png || true; adb pull /sdcard/buscalocal-screen.png ui-smoke.png >/dev/null 2>&1 || true' EXIT
dump ui-before.xml
read -r X Y BOTTOM < <(python3 - ui-before.xml <<'PY'
import sys,re,xml.etree.ElementTree as E
nodes=list(E.parse(sys.argv[1]).iter('node'))
assert not any('Falha ao abrir o aplicativo' in x.get('text','') for x in nodes), 'Tela de diagnóstico indica falha de inicialização'
n=next(x for x in nodes if x.get('resource-id')=='br.local.busca:id/chat_message_input')
a,b,c,d=map(int,re.findall(r'\d+',n.get('bounds')))
print((a+c)//2,(b+d)//2,d)
PY
)
adb shell input tap "$X" "$Y"
sleep 3
adb shell input text KeyboardSmokeTest
sleep 2
dump ui-after.xml
python3 - "$BOTTOM" <<'PY'
import sys,re,xml.etree.ElementTree as E
n=next(x for x in E.parse('ui-after.xml').iter('node') if x.get('resource-id')=='br.local.busca:id/chat_message_input')
a,b,c,d=map(int,re.findall(r'\d+',n.get('bounds')))
assert 'KeyboardSmokeTest' in n.get('text',''), 'Texto não foi digitado no compositor'
assert d < int(sys.argv[1])-80, f'Campo não subiu acima do teclado: antes={sys.argv[1]} depois={d}'
print('Abertura, digitação e deslocamento do compositor aprovados.')
PY
adb shell pidof "$PKG"
adb shell input keyevent 4
sleep 2
dump ui-closed.xml
python3 - "$BOTTOM" <<'PY'
import sys,re,xml.etree.ElementTree as E
n=next(x for x in E.parse('ui-closed.xml').iter('node') if x.get('resource-id')=='br.local.busca:id/chat_message_input')
d=int(re.findall(r'\d+',n.get('bounds'))[-1])
assert abs(d-int(sys.argv[1]))<80, 'Compositor não retornou após fechar teclado'
print('Fechamento do teclado aprovado.')
PY
