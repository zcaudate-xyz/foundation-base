#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

MODE="dry-run"

usage() {
  cat <<'EOF'
Usage: scripts/remove_hidden_metadata.sh [--write] [--dry-run]

Removes ^:hidden metadata tags from files under test/.

Options:
  --write    Apply the rewrite in-place.
  --dry-run  Show the files that would change without editing them.
  --help     Show this help text.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --write)
      MODE="write"
      ;;
    --dry-run)
      MODE="dry-run"
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      printf 'Unknown option: %s\n\n' "$1" >&2
      usage >&2
      exit 1
      ;;
  esac
  shift
done

mapfile -t FILES < <(rg -l '\^:hidden' test --glob '**/*.{clj,cljc}')

if [[ ${#FILES[@]} -eq 0 ]]; then
  echo "No ^:hidden tags found under test/."
  exit 0
fi

if [[ "$MODE" == "dry-run" ]]; then
  echo "Files containing ^:hidden under test/:"
  printf '  %s\n' "${FILES[@]}"
  echo
  echo "Match counts:"
  rg '\^:hidden' test --glob '**/*.{clj,cljc}' --count
  exit 0
fi

for file in "${FILES[@]}"; do
  perl -0pi -e '
    s/^[ \t]*\^:hidden[ \t]*\n//mg;
    s/[ \t]*\^:hidden(?![\w-])//g;
    s/[ \t]+\n/\n/g;
  ' "$file"
done

echo "Updated ${#FILES[@]} files."

REMAINING="$(rg '\^:hidden' test --glob '**/*.{clj,cljc}' --count || true)"
if [[ -n "$REMAINING" ]]; then
  echo
  echo "Remaining matches:"
  echo "$REMAINING"
  exit 1
fi

echo "No ^:hidden tags remain under test/."
