#!/usr/bin/env python3
"""Merge guides/ and plans/slop/ prose into the authored src-doc pages.

The migration deliberately appends the original Markdown as Clojure string forms.
code.doc renders top-level strings as Markdown, so this preserves headings, lists,
code fences, examples, and explanatory prose without maintaining a second docs tree.
"""

from __future__ import annotations

import hashlib
import json
import re
import shutil
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
}

SPECIAL_PREFIX_TARGETS = (
    ("code_query_", "src-doc/documentation/code/code_query.clj"),
    ("std_block_", "src-doc/documentation/std/std_block.clj"),
    ("std_lang_base_", "src-doc/documentation/std_lang_introduction.clj"),
    ("xt_lang_base_", "src-doc/documentation/xt/xt_lang.clj"),
    ("rt_postgres", "src-doc/documentation/hara/hara_runtime.clj"),
)

FALLBACK_TARGETS = {
    "std_lib_": "src-doc/documentation/std/lib_index.clj",
    "std_": "src-doc/documentation/std/std_index.clj",
    "code_": "src-doc/documentation/main_code_tools.clj",
    "xt_": "src-doc/documentation/xt/xt_index.clj",
    "rt_": "src-doc/documentation/hara/hara_runtime.clj",
    "hara_": "src-doc/documentation/std_lang_index.clj",
}

SUFFIXES = (
    "_summary",
    "_tutorial",
    "_recommendations",
    "_recommendation",
    "_usage",
    "_guide",
    "_plan",
)


def rel(path: Path) -> str:
    return path.relative_to(ROOT).as_posix()


def normalise_stem(path: Path) -> str:
    stem = path.stem.lower().replace(".", "_").replace("-", "_")
    stem = re.sub(r"_+", "_", stem).strip("_")
    changed = True
    while changed:
        changed = False
        for suffix in SUFFIXES:
            if stem.endswith(suffix):
                stem = stem[: -len(suffix)].rstrip("_")
                changed = True
    return stem


def existing_target(relative_path: str) -> Path | None:
    candidate = ROOT / relative_path
    return candidate if candidate.is_file() else None


def route(source: Path) -> Path:
    source_rel = rel(source)
    explicit = EXPLICIT_TARGETS.get(source_rel)
    if explicit:
        target = existing_target(explicit)
        if target:
            return target
        raise RuntimeError(f"Explicit target does not exist: {explicit}")

    if source_rel.startswith("plans/slop/top-level/"):
        target = existing_target("src-doc/documentation/main_contributing.clj")
        assert target
        return target

    stem = normalise_stem(source)

    for prefix, target_rel in SPECIAL_PREFIX_TARGETS:
        if stem.startswith(prefix.rstrip("_")):
            target = existing_target(target_rel)
            if target:
                return target

    candidates: list[str] = []
    if stem.startswith("std_"):
        candidates.append(f"src-doc/documentation/std/{stem}.clj")
    if stem.startswith("code_"):
        candidates.append(f"src-doc/documentation/code/{stem}.clj")
    if stem.startswith("xt_"):
        candidates.append(f"src-doc/documentation/xt/{stem}.clj")
    if stem.startswith("hara_"):
        candidates.append(f"src-doc/documentation/hara/{stem}.clj")
    candidates.append(f"src-doc/documentation/{stem}.clj")

    for candidate_rel in candidates:
        target = existing_target(candidate_rel)
        if target:
            return target

    for prefix, target_rel in FALLBACK_TARGETS.items():
        if stem.startswith(prefix):
            target = existing_target(target_rel)
            if target:
                return target

    raise RuntimeError(f"No relevant src-doc target found for {source_rel} ({stem})")


def markdown_title(source: Path, text: str) -> str:
    for line in text.splitlines():
        match = re.match(r"^\s*#{1,6}\s+(.+?)\s*$", line)
        if match:
            title = match.group(1)
            title = title.replace("**", "").replace("`", "").strip()
            if title:
                return title
    return source.stem.replace("_", " ").replace(".", " ").title()


def link_slug(source: Path) -> str:
    slug = rel(source).lower()
    slug = re.sub(r"[^a-z0-9]+", "-", slug).strip("-")
    return f"merged-{slug}"


def clojure_string(value: str) -> str:
    # JSON string escaping is compatible with Clojure string literals for the
    # characters emitted by json.dumps.
    return json.dumps(value, ensure_ascii=False)


def append_markdown(target: Path, source: Path, text: str) -> None:
    source_rel = rel(source)
    marker = f";; BEGIN merged documentation: {source_rel}"
    current = target.read_text(encoding="utf-8")
    if marker in current:
        return

    digest = hashlib.sha256(text.encode("utf-8")).hexdigest()
    title = markdown_title(source, text)
    section = "\n".join(
        [
            "",
            "",
            marker,
            f";; sha256: {digest}",
            f'[[:chapter {{:title {clojure_string(title)} :link {clojure_string(link_slug(source))}}}]]',
            clojure_string(
                f"This explanation was consolidated from `{source_rel}` so that the "
                "narrative and generated API reference live on the same documentation page."
            ),
            clojure_string(text),
            f";; END merged documentation: {source_rel}",
            "",
        ]
    )
    target.write_text(current.rstrip() + section, encoding="utf-8")


def replace_stale_references(mapping: dict[str, str]) -> None:
    ignored_roots = {ROOT / ".git", ROOT / "guides", ROOT / "plans" / "slop"}
    allowed_suffixes = {
        ".clj", ".cljc", ".cljs", ".edn", ".md", ".txt", ".yml", ".yaml",
        ".json", ".html", ".css", ".js", ".ts", ".sh",
    }

    for path in ROOT.rglob("*"):
        if not path.is_file() or path.suffix.lower() not in allowed_suffixes:
            continue
        if any(root == path or root in path.parents for root in ignored_roots):
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
    target = DOC_ROOT / "main_contributing.clj"
    marker = ";; BEGIN merged documentation manifest"
    current = target.read_text(encoding="utf-8")
    if marker in current:
        return

    rows = [
        "# Consolidated documentation manifest",
        "",
        "The standalone `guides/` and `plans/slop/` trees were merged into the authored "
        "documentation pages below. The original Markdown is retained verbatim inside the "
        "target Clojure documentation source, next to its generated API sections.",
        "",
        "| Former source | Authored documentation target |",
        "|---|---|",
    ]
    for old, new in sorted(mapping.items()):
        rows.append(f"| `{old}` | `{new}` |")
    manifest = "\n".join(rows) + "\n"

    section = "\n".join(
        [
            "",
            "",
            marker,
            '[[:chapter {:title "Consolidated documentation manifest" :link "consolidated-documentation-manifest"}]]',
            clojure_string(manifest),
            ";; END merged documentation manifest",
            "",
        ]
    )
    target.write_text(current.rstrip() + section, encoding="utf-8")


def main() -> None:
    sources = sorted(
        path
        for source_root in SOURCE_ROOTS
        if source_root.exists()
        for path in source_root.rglob("*")
        if path.is_file()
    )
    if not sources:
        raise RuntimeError("No files found under guides/ or plans/slop/")

    unsupported = [rel(path) for path in sources if path.suffix.lower() not in {".md", ".txt"}]
    if unsupported:
        raise RuntimeError(f"Unsupported documentation files: {unsupported}")

    mapping = {rel(source): rel(route(source)) for source in sources}
    replace_stale_references(mapping)

    for source in sources:
        target = ROOT / mapping[rel(source)]
        text = source.read_text(encoding="utf-8")
        append_markdown(target, source, text)

    append_manifest(mapping)

    for source_root in SOURCE_ROOTS:
        if source_root.exists():
            shutil.rmtree(source_root)

    remaining = [rel(path) for root in SOURCE_ROOTS if root.exists() for path in root.rglob("*")]
    if remaining:
        raise RuntimeError(f"Documentation sources were not removed: {remaining}")

    for source_rel, target_rel in mapping.items():
        target_text = (ROOT / target_rel).read_text(encoding="utf-8")
        marker = f";; BEGIN merged documentation: {source_rel}"
        if marker not in target_text:
            raise RuntimeError(f"Missing merged marker for {source_rel} in {target_rel}")

    print(f"Merged {len(mapping)} documentation files into {len(set(mapping.values()))} src-doc pages.")
    for source_rel, target_rel in sorted(mapping.items()):
        print(f"  {source_rel} -> {target_rel}")


if __name__ == "__main__":
    main()
