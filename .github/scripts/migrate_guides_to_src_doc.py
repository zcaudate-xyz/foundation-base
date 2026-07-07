#!/usr/bin/env python3
"""Consolidate guides/ and plans/slop/ into authored code.doc pages.

Every source document is preserved verbatim as a Markdown string form in an
existing src-doc page. The old source trees are removed only after all files
have been routed and their merge markers have been verified.
"""

from __future__ import annotations

import hashlib
import json
import re
import shutil
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DOC_ROOT = ROOT / "src-doc" / "documentation"
SOURCE_ROOTS = (ROOT / "guides", ROOT / "plans" / "slop")

EXPLICIT_TARGETS = {
    "guides/DISPATCH_STRATEGIES.md": "src-doc/documentation/std/std_dispatch.clj",
    "guides/MANAGE_XTALK.md": "src-doc/documentation/hara/hara_model.clj",
    "guides/code.manage.md": "src-doc/documentation/code/code_manage.clj",
    "guides/code.query.md": "src-doc/documentation/code/code_query.clj",
    "guides/code.test.md": "src-doc/documentation/code/code_test.clj",
    "guides/std.block.md": "src-doc/documentation/std/std_block.clj",
    "guides/std.scheduler.md": "src-doc/documentation/std/std_scheduler.clj",
    "guides/std.task.md": "src-doc/documentation/std/std_task.clj",
    "guides/std.timeseries.md": "src-doc/documentation/std/std_timeseries.clj",
    "plans/slop/doc_pg.md": "src-doc/documentation/hara/hara_model.clj",
    "plans/slop/deftype_pg_usage.md": "src-doc/documentation/hara/hara_model.clj",
    "plans/slop/summary/documentation_helper.md": "src-doc/documentation/foundation_code_guides.clj",
}

SPECIAL_TARGETS = (
    ("code_query_", "src-doc/documentation/code/code_query.clj"),
    ("code_test_", "src-doc/documentation/code/code_test.clj"),
    ("code_manage_", "src-doc/documentation/code/code_manage.clj"),
    ("std_block_", "src-doc/documentation/std/std_block.clj"),
    ("std_lang_base_", "src-doc/documentation/std_lang_introduction.clj"),
    ("std_lang_", "src-doc/documentation/std_lang_introduction.clj"),
    ("xt_lang_base_", "src-doc/documentation/xt/xt_lang.clj"),
    ("xt_lang_", "src-doc/documentation/xt/xt_lang.clj"),
    ("rt_postgres", "src-doc/documentation/hara/hara_runtime.clj"),
    ("documentation_", "src-doc/documentation/foundation_code_guides.clj"),
)

FAMILY_TARGETS = (
    ("std_lib_", "src-doc/documentation/std/lib_index.clj"),
    ("std_", "src-doc/documentation/std/std_index.clj"),
    ("code_", "src-doc/documentation/main_code_tools.clj"),
    ("xt_", "src-doc/documentation/xt/xt_index.clj"),
    ("rt_", "src-doc/documentation/hara/hara_runtime.clj"),
    ("hara_", "src-doc/documentation/std_lang_index.clj"),
)

TRAILING_LABELS = (
    "_summary",
    "_tutorial",
    "_recommendations",
    "_recommendation",
    "_reference",
    "_usage",
    "_guide",
    "_plan",
)


def relative(path: Path) -> str:
    return path.relative_to(ROOT).as_posix()


def require_target(path: str) -> Path:
    target = ROOT / path
    if not target.is_file():
        raise RuntimeError(f"Documentation target does not exist: {path}")
    return target


def normalise_stem(source: Path) -> str:
    stem = re.sub(r"[^a-z0-9]+", "_", source.stem.lower()).strip("_")
    changed = True
    while changed:
        changed = False
        for suffix in TRAILING_LABELS:
            if stem.endswith(suffix):
                stem = stem[: -len(suffix)].rstrip("_")
                changed = True
    return stem


def route(source: Path) -> Path:
    source_path = relative(source)

    if source_path in EXPLICIT_TARGETS:
        return require_target(EXPLICIT_TARGETS[source_path])

    if source_path.startswith("plans/slop/top-level/"):
        return require_target("src-doc/documentation/main_contributing.clj")

    stem = normalise_stem(source)

    for prefix, target in SPECIAL_TARGETS:
        if stem.startswith(prefix.rstrip("_")):
            return require_target(target)

    exact_candidates = []
    if stem.startswith("std_"):
        exact_candidates.append(f"src-doc/documentation/std/{stem}.clj")
    if stem.startswith("code_"):
        exact_candidates.append(f"src-doc/documentation/code/{stem}.clj")
    if stem.startswith("xt_"):
        exact_candidates.append(f"src-doc/documentation/xt/{stem}.clj")
    if stem.startswith("hara_"):
        exact_candidates.append(f"src-doc/documentation/hara/{stem}.clj")
    exact_candidates.append(f"src-doc/documentation/{stem}.clj")

    for candidate in exact_candidates:
        target = ROOT / candidate
        if target.is_file():
            return target

    for prefix, target in FAMILY_TARGETS:
        if stem.startswith(prefix):
            return require_target(target)

    # Non-namespace process notes belong with contribution and workflow docs.
    return require_target("src-doc/documentation/main_contributing.clj")


def title_for(source: Path, markdown: str) -> str:
    for line in markdown.splitlines():
        match = re.match(r"^\s*#{1,6}\s+(.+?)\s*$", line)
        if match:
            title = match.group(1).replace("**", "").replace("`", "").strip()
            if title:
                return title
    return source.stem.replace("_", " ").replace(".", " ").title()


def slug_for(source: Path) -> str:
    return "merged-" + re.sub(r"[^a-z0-9]+", "-", relative(source).lower()).strip("-")


def clojure_string(value: str) -> str:
    return json.dumps(value, ensure_ascii=False)


def append_source(target: Path, source: Path, markdown: str) -> None:
    source_path = relative(source)
    marker = f";; BEGIN merged documentation: {source_path}"
    current = target.read_text(encoding="utf-8")
    if marker in current:
        return

    section = "\n".join(
        (
            "",
            "",
            marker,
            f";; sha256: {hashlib.sha256(markdown.encode('utf-8')).hexdigest()}",
            f'[[:chapter {{:title {clojure_string(title_for(source, markdown))} '
            f':link {clojure_string(slug_for(source))}}}]]',
            clojure_string(markdown),
            f";; END merged documentation: {source_path}",
            "",
        )
    )
    target.write_text(current.rstrip() + section, encoding="utf-8")


def replace_stale_references(mapping: dict[str, str]) -> None:
    ignored = {ROOT / ".git", *SOURCE_ROOTS}
    text_suffixes = {
        ".clj", ".cljc", ".cljs", ".edn", ".md", ".txt", ".yml", ".yaml",
        ".json", ".html", ".css", ".js", ".ts", ".sh", ".py",
    }

    for path in ROOT.rglob("*"):
        if not path.is_file() or path.suffix.lower() not in text_suffixes:
            continue
        if any(root == path or root in path.parents for root in ignored):
            continue
        try:
            text = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        updated = text
        for old, new in mapping.items():
            updated = updated.replace(old, new)
        if updated != text:
            path.write_text(updated, encoding="utf-8")


def append_manifest(mapping: dict[str, str]) -> None:
    target = require_target("src-doc/documentation/main_contributing.clj")
    marker = ";; BEGIN merged documentation manifest"
    current = target.read_text(encoding="utf-8")
    if marker in current:
        return

    grouped: dict[str, list[str]] = defaultdict(list)
    for source, destination in sorted(mapping.items()):
        grouped[destination].append(source)

    lines = [
        "# Consolidated documentation manifest",
        "",
        "The former `guides/` and `plans/slop/` sources are retained verbatim in the "
        "authored documentation pages listed below.",
        "",
    ]
    for destination, sources in sorted(grouped.items()):
        lines.extend((f"## `{destination}`", ""))
        lines.extend(f"- `{source}`" for source in sources)
        lines.append("")

    section = "\n".join(
        (
            "",
            "",
            marker,
            '[[:chapter {:title "Consolidated documentation manifest" '
            ':link "consolidated-documentation-manifest"}]]',
            clojure_string("\n".join(lines)),
            ";; END merged documentation manifest",
            "",
        )
    )
    target.write_text(current.rstrip() + section, encoding="utf-8")


def main() -> None:
    sources = sorted(
        file
        for source_root in SOURCE_ROOTS
        if source_root.exists()
        for file in source_root.rglob("*")
        if file.is_file()
    )
    if not sources:
        raise RuntimeError("No files found under guides/ or plans/slop/")

    unsupported = [relative(path) for path in sources if path.suffix.lower() not in {".md", ".txt"}]
    if unsupported:
        raise RuntimeError(f"Unsupported documentation files: {unsupported}")

    mapping = {relative(source): relative(route(source)) for source in sources}
    replace_stale_references(mapping)

    for source in sources:
        append_source(ROOT / mapping[relative(source)], source, source.read_text(encoding="utf-8"))

    append_manifest(mapping)

    for source_root in SOURCE_ROOTS:
        if source_root.exists():
            shutil.rmtree(source_root)

    for source_path, target_path in mapping.items():
        marker = f";; BEGIN merged documentation: {source_path}"
        if marker not in (ROOT / target_path).read_text(encoding="utf-8"):
            raise RuntimeError(f"Missing merged marker for {source_path} in {target_path}")

    if any(source_root.exists() for source_root in SOURCE_ROOTS):
        raise RuntimeError("Legacy documentation directories were not removed")

    print(f"Merged {len(mapping)} files into {len(set(mapping.values()))} authored pages.")
    for source_path, target_path in sorted(mapping.items()):
        print(f"  {source_path} -> {target_path}")


if __name__ == "__main__":
    main()
