#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
tmp=$(mktemp -d); trap 'rm -rf "$tmp"' EXIT
javac -d "$tmp" app/src/main/java/br/local/busca/Policy.java app/src/main/java/br/local/busca/ContextSelector.java tests/PolicyTest.java tests/ContextSelectorTest.java app/src/main/java/br/local/busca/KeyboardLayout.java tests/KeyboardLayoutTest.java
java -cp "$tmp" PolicyTest

java -cp "$tmp" ContextSelectorTest

java -cp "$tmp" KeyboardLayoutTest
