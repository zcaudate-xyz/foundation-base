#!/usr/bin/env bash
set -euo pipefail

find src-doc/documentation -name '*.clj' -print0 |
  while IFS= read -r -d '' file; do
    python3 -c 'import sys; sys.path.insert(0, ".github/scripts"); from format_docs import convert_text; sys.stdout.write(convert_text(sys.stdin.read()))' \
      < "$file" > "$file.tmp"
    mv "$file.tmp" "$file"
  done
