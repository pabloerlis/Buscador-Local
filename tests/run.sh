#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
tmp=$(mktemp -d); trap 'rm -rf "$tmp"' EXIT
javac -d "$tmp" app/src/main/java/br/local/busca/Policy.java tests/PolicyTest.java
java -cp "$tmp" PolicyTest
