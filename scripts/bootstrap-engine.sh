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
