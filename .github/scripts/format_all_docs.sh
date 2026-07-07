#!/usr/bin/env bash
set -euo pipefail

find src-doc/documentation -name '*.clj' -print0 |
  while IFS= read -r -d '' file; do
    printf '%s\n' "$file"
  done
