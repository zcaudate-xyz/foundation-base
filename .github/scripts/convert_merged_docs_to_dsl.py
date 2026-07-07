import json
import re
from pathlib import Path

HEADING = re.compile(r"^(#{1,6})\s+(.+?)\s*#*\s*$")
FENCE = re.compile(r"^\s*(`{3,}|~{3,})\s*([^\s`]*)?.*$")
LIST_LINE = re.compile(r"^\s*(?:[-+*]|\d+[.)])\s+")
TABLE_DIVIDER = re.compile(r"^\s*\|?\s*:?-{3,}:?\s*(?:\|\s*:?-{3,}:?\s*)+\|?\s*$")
IMAGE = re.compile(r"^!\[([^]]*)\]\(([^ )]+)(?:\s+[\"'](.*)[\"'])?\)\s*$")


def quote(value):
    return json.dumps(value, ensure_ascii=False)


def clean_title(value):
    value = re.sub(r"!\[([^]]*)\]\([^)]*\)", r"\1", value)
    value = re.sub(r"\[([^]]+)\]\([^)]*\)", r"\1", value)
    return re.sub(r"[`*_]+", "", value).strip()


def slug(value):
    return re.sub(r"[^a-z0-9]+", "-", value.lower()).strip("-") or "section"


def special(lines, index):
    line = lines[index]
    if not line.strip():
        return True
    if HEADING.match(line) or FENCE.match(line) or LIST_LINE.match(line):
        return True
    if line.lstrip().startswith(">") or IMAGE.match(line.strip()):
        return True
    if index + 1 < len(lines) and "|" in line and TABLE_DIVIDER.match(lines[index + 1]):
        return True
    return False


def tokenize(markdown):
    lines = markdown.replace("\r\n", "\n").replace("\r", "\n").split("\n")
    out = []
    index = 0
    while index < len(lines):
        line = lines[index]
        if not line.strip():
            index += 1
            continue

        fence = FENCE.match(line)
        if fence:
            marker = fence.group(1)
            language = fence.group(2) or "text"
            index += 1
            body = []
            while index < len(lines):
                close = re.match(r"^\s*" + re.escape(marker[0]) + "{" + str(len(marker)) + r",}\s*$", lines[index])
                if close:
                    index += 1
                    break
                body.append(lines[index])
                index += 1
            out.append(("code", language, "\n".join(body).rstrip()))
            continue

        heading = HEADING.match(line)
        if heading:
            out.append(("heading", len(heading.group(1)), clean_title(heading.group(2))))
            index += 1
            continue

        if line.lstrip().startswith(">"):
            body = []
            while index < len(lines) and (lines[index].lstrip().startswith(">") or not lines[index].strip()):
                body.append(re.sub(r"^\s*>\s?", "", lines[index]))
                index += 1
            text = "\n".join(body).strip()
            alert = re.match(r"^\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\]\s*\n?(.*)$", text, re.I | re.S)
            if alert:
                out.append(("callout", alert.group(1).lower(), alert.group(2).strip()))
            else:
                out.append(("quote", text))
            continue

        image = IMAGE.match(line.strip())
        if image:
            out.append(("image", image.group(1) or image.group(3) or "", image.group(2)))
            index += 1
            continue

        if index + 1 < len(lines) and "|" in line and TABLE_DIVIDER.match(lines[index + 1]):
            body = [line, lines[index + 1]]
            index += 2
            while index < len(lines) and lines[index].strip() and "|" in lines[index]:
                body.append(lines[index])
                index += 1
            out.append(("paragraph", "\n".join(body)))
            continue

        if LIST_LINE.match(line):
            body = []
            while index < len(lines) and (LIST_LINE.match(lines[index]) or lines[index].startswith(("  ", "\t")) or not lines[index].strip()):
                body.append(lines[index])
                index += 1
            out.append(("paragraph", "\n".join(body).strip()))
            continue

        body = [line]
        index += 1
        while index < len(lines) and not special(lines, index):
            body.append(lines[index])
            index += 1
        out.append(("paragraph", "\n".join(body).strip()))
    return out


def source_title(source):
    return Path(source).stem.replace("_", " ").replace(".", " ").title()
