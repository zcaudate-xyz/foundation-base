#!/usr/bin/env bash
set -euo pipefail

script=/tmp/format_guides.py
git show 3b8b0d5d05d43a51a55b847683014d8ea3a475aa:.github/scripts/format_summary_docs.py > "$script"
sed -i 's|plans/slop/summary/|guides/|g; s|100|9|g; s|summary blocks|guide blocks|g' "$script"
python3 -c "code=open('$script').read(); exec(compile(code, '.github/scripts/format_guides.py', 'exec'), {'__file__': '.github/scripts/format_guides.py', '__name__': '__main__'})"

test ! -d guides
test "$(git diff --name-only -- src-doc/documentation | wc -l | tr -d ' ')" = "9"
git diff --check
