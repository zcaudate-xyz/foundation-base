#!/bin/bash
set -e

REF=${1:-HEAD~1}
echo "Detecting changes against $REF..."

# Get changed files, filter for .clj, replace newlines with spaces
FILES=$(git diff --name-only $REF | grep ".clj$" | tr '\n' ' ')

if [ -z "$FILES" ]; then
    echo "No .clj files changed."
    exit 0
fi

echo "Running tests for: $FILES"

# Invoke lein test with the list of files
./lein test :files "$FILES"
