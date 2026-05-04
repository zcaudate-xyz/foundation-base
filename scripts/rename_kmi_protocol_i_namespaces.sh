#!/usr/bin/env bash
set -euo pipefail

SCRIPT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET_ROOT="$SCRIPT_ROOT"
MODE="dry-run"

usage() {
  cat <<'EOF'
Usage: scripts/rename_kmi_protocol_i_namespaces.sh [--write] [--dry-run] [target-root]

Renames kmi.protocol.* interface namespaces from names like:
  kmi.protocol.assoc
to:
  kmi.protocol.iassoc

This script:
1. Renames files under src-lang/kmi/protocol/
2. Rewrites namespace declarations in those files
3. Rewrites repo references under src-lang/ and test-lang/

Options:
  --write    Apply the refactor in-place.
  --dry-run  Show the planned changes without editing files.
  --help     Show this help text.

Arguments:
  target-root  Repository root to rewrite. Defaults to this repository.
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
    /*)
      TARGET_ROOT="$1"
      ;;
    *)
      printf 'Unknown option: %s\n\n' "$1" >&2
      usage >&2
      exit 1
      ;;
  esac
  shift
done

cd "$TARGET_ROOT"

python3 - "$MODE" "$TARGET_ROOT" <<'PY'
import re
import sys
from pathlib import Path

mode = sys.argv[1]
root = Path(sys.argv[2])

mapping = {
    "assoc": "iassoc",
    "assoc-mutable": "iassoc-mutable",
    "associative": "iassociative",
    "associative-mutable": "iassociative-mutable",
    "coll": "icoll",
    "collection": "icollection",
    "conj": "iconj",
    "cons": "icons",
    "counted": "icounted",
    "dissoc": "idissoc",
    "dissoc-mutable": "idissoc-mutable",
    "edit": "iedit",
    "empty": "iempty",
    "eq": "ieq",
    "find": "ifind",
    "hash": "ihash",
    "indexed": "iindexed",
    "indexed-kv": "iindexed-kv",
    "invokable": "iinvokable",
    "lisp-associative": "ilisp-associative",
    "lisp-named": "ilisp-named",
    "lisp-persistent": "ilisp-persistent",
    "lisp-scalar": "ilisp-scalar",
    "lisp-sequential": "ilisp-sequential",
    "lookup": "ilookup",
    "lookupable": "ilookupable",
    "map-entry": "imap-entry",
    "meta": "imeta",
    "named": "inamed",
    "namespaced": "inamespaced",
    "nth": "inth",
    "pair": "ipair",
    "peek": "ipeek",
    "persistent": "ipersistent",
    "pop": "ipop",
    "pop-mutable": "ipop-mutable",
    "push": "ipush",
    "push-mutable": "ipush-mutable",
    "reduce": "ireduce",
    "seq": "iseq",
    "seqable": "iseqable",
    "sequential": "isequential",
    "show": "ishow",
    "size": "isize",
    "stack": "istack",
    "stack-mutable": "istack-mutable",
    "value": "ivalue",
}

protocol_dir = root / "src-lang" / "kmi" / "protocol"
search_dirs = [root / "src-lang", root / "test-lang"]
file_globs = ("*.clj", "*.cljc")

def iter_source_files():
    for base in search_dirs:
        if not base.exists():
            continue
        for pattern in file_globs:
            for path in base.rglob(pattern):
                if path.name.startswith(".#"):
                    continue
                if not path.is_file():
                    continue
                yield path

def replace_refs(text: str) -> str:
    for old, new in sorted(mapping.items(), key=lambda kv: len(kv[0]), reverse=True):
        text = re.sub(rf"\bkmi\.protocol\.{re.escape(old)}\b", f"kmi.protocol.{new}", text)
    return text

renames = []
for old, new in mapping.items():
    old_file = protocol_dir / f"{old.replace('-', '_')}.clj"
    new_file = protocol_dir / f"{new.replace('-', '_')}.clj"
    if old_file.exists():
        renames.append((old_file, new_file))

rewrites = []
for path in iter_source_files():
    try:
        original = path.read_text()
    except FileNotFoundError:
        continue
    updated = replace_refs(original)
    if updated != original:
        rewrites.append(path)
        if mode == "write":
            path.write_text(updated)

if mode == "dry-run":
    if renames:
        print("Files to rename:")
        for old_file, new_file in renames:
            print(f"  {old_file.relative_to(root)} -> {new_file.relative_to(root)}")
    else:
        print("No protocol files need renaming.")
    print()
    if rewrites:
        print("Files to rewrite:")
        for path in rewrites:
            print(f"  {path.relative_to(root)}")
    else:
        print("No source files need rewriting.")
    sys.exit(0)

for old_file, new_file in renames:
    old_file.rename(new_file)

print(f"Renamed {len(renames)} protocol files.")
print(f"Rewrote {len(rewrites)} source files.")
PY
