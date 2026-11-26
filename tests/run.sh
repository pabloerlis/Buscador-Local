#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
tmp=$(mktemp -d); trap 'rm -rf "$tmp"' EXIT
javac -d "$tmp" app/src/main/java/br/local/busca/Policy.java app/src/main/java/br/local/busca/ContextSelector.java tests/PolicyTest.java tests/ContextSelectorTest.java app/src/main/java/br/local/busca/KeyboardLayout.java tests/KeyboardLayoutTest.java app/src/main/java/br/local/busca/ThoughtOutput.java tests/ThoughtOutputTest.java app/src/main/java/br/local/busca/ToolCallParser.java tests/ToolCallParserTest.java app/src/main/java/br/local/busca/AgentProfile.java app/src/main/java/br/local/busca/ConversationReplay.java tests/AgentProfileTest.java
java -cp "$tmp" PolicyTest

java -cp "$tmp" ContextSelectorTest

java -cp "$tmp" KeyboardLayoutTest

java -cp "$tmp" ThoughtOutputTest

java -cp "$tmp" ToolCallParserTest

java -cp "$tmp" AgentProfileTest
