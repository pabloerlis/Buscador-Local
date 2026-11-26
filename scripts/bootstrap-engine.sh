#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
PIN=0e196e8831220b782cc78535b6d6729940169373
mkdir -p vendor
if [ ! -d vendor/LFM3Local/.git ]; then
  git clone https://github.com/pabloerlis/LFM3Local.git vendor/LFM3Local
fi
git -C vendor/LFM3Local fetch origin "$PIN"
git -C vendor/LFM3Local checkout --detach "$PIN"
bash vendor/LFM3Local/scripts/bootstrap-llama.sh

# Local, reproducible compatibility patch on the pinned engine.
python3 - <<'PY'
from pathlib import Path
base=Path('vendor/LFM3Local/android/lib/src/main')
cpp=base/'cpp/ai_chat.cpp'
s=cpp.read_text()
needle='const jsize count = env->GetArrayLength(jnames);'
assert s.count(needle)==1
s=s.replace(needle,needle+'\n    if (count == 0) return JNI_FALSE;')
cpp.write_text(s)
kt=base/'java/com/arm/aichat/internal/InferenceEngineImpl.kt'
s=kt.read_text()
a=s.index('    override fun sendToolResult(')
b=s.index('    override suspend fun bench(',a)
part=s[a:b]
needle='if (code != 0) throw IOException("Tool continuation rejected by native runtime: " + code)'
assert part.count(needle)==1
part=part.replace(needle,'if (code == 4 || code == 5) throw ContextWindowFullException()\n            '+needle)
needle='} catch (e: CancellationException) {'
assert part.count(needle)==1
part=part.replace(needle,'} catch (e: ContextWindowFullException) {\n            _state.value = InferenceEngine.State.ModelReady\n            throw e\n        '+needle)
kt.write_text(s[:a]+part+s[b:])
PY
