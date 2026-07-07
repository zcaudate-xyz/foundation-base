import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DOC_ROOT = ROOT / 'src-doc/documentation'
SOURCE_PREFIX = 'plans/slop/summary/'

BLOCK = re.compile(
    r'^;; BEGIN merged documentation: (' + re.escape(SOURCE_PREFIX) + r'[^\n]+)\n'
    r';; sha256: ([0-9a-f]+)\n'
    r'(\[\[:chapter[^\n]*\]\])\n'
    r'([^\n]+)\n'
    r';; END merged documentation: \1$',
    re.MULTILINE,
)
CHAPTER = re.compile(r'^\[\[:chapter\s+\{:title\s+("(?:\\.|[^"\\])*")\s+:link\s+("(?:\\.|[^"\\])*")\}\]\]$')
HEADING = re.compile(r'^(#{1,6})\s+(.+?)\s*#*\s*$')
FENCE = re.compile(r'^\s*(`{3,}|~{3,})\s*([^\s`]*)?.*$')
LIST_LINE = re.compile(r'^\s*(?:[-+*]|\d+[.)])\s+')
TABLE_DIVIDER = re.compile(r'^\s*\|?\s*:?-{3,}:?\s*(?:\|\s*:?-{3,}:?\s*)+\|?\s*$')
IMAGE = re.compile(r'^!\[([^]]*)\]\(([^ )]+)(?:\s+["\'](.*)["\'])?\)\s*$')


def quote(value: str) -> str:
    return json.dumps(value, ensure_ascii=False)


def clean_title(value: str) -> str:
    value = re.sub(r'!\[([^]]*)\]\([^)]*\)', r'\1', value)
    value = re.sub(r'\[([^]]+)\]\([^)]*\)', r'\1', value)
    return re.sub(r'[`*_]+', '', value).strip()


def slug(value: str) -> str:
    return re.sub(r'[^a-z0-9]+', '-', value.lower()).strip('-') or 'section'


def is_special(lines, index):
    line = lines[index]
    if not line.strip():
        return True
    if HEADING.match(line) or FENCE.match(line) or LIST_LINE.match(line):
        return True
    if line.lstrip().startswith('>') or IMAGE.match(line.strip()):
        return True
    if index + 1 < len(lines) and '|' in line and TABLE_DIVIDER.match(lines[index + 1]):
        return True
    return False


def tokenize(markdown: str):
    lines = markdown.replace('\r\n', '\n').replace('\r', '\n').split('\n')
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
            language = fence.group(2) or 'text'
            index += 1
            body = []
            while index < len(lines):
                if re.match(r'^\s*' + re.escape(marker[0]) + '{' + str(len(marker)) + r',}\s*$', lines[index]):
                    index += 1
                    break
                body.append(lines[index])
                index += 1
            out.append(('code', language, '\n'.join(body).rstrip()))
            continue

        heading = HEADING.match(line)
        if heading:
            out.append(('heading', len(heading.group(1)), clean_title(heading.group(2))))
            index += 1
            continue

        if line.lstrip().startswith('>'):
            body = []
            while index < len(lines) and (lines[index].lstrip().startswith('>') or not lines[index].strip()):
                body.append(re.sub(r'^\s*>\s?', '', lines[index]))
                index += 1
            text = '\n'.join(body).strip()
            alert = re.match(r'^\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\]\s*\n?(.*)$', text, re.I | re.S)
            if alert:
                out.append(('callout', alert.group(1).lower(), alert.group(2).strip()))
            else:
                out.append(('quote', text))
            continue

        image = IMAGE.match(line.strip())
        if image:
            out.append(('image', image.group(1) or image.group(3) or '', image.group(2)))
            index += 1
            continue

        if index + 1 < len(lines) and '|' in line and TABLE_DIVIDER.match(lines[index + 1]):
            body = [line, lines[index + 1]]
            index += 2
            while index < len(lines) and lines[index].strip() and '|' in lines[index]:
                body.append(lines[index])
                index += 1
            out.append(('paragraph', '\n'.join(body)))
            continue

        if LIST_LINE.match(line):
            body = []
            while index < len(lines):
                current = lines[index]
                if LIST_LINE.match(current) or current.startswith(('  ', '\t')) or not current.strip():
                    body.append(current)
                    index += 1
                    continue
                break
            out.append(('paragraph', '\n'.join(body).strip()))
            continue

        body = [line]
        index += 1
        while index < len(lines) and not is_special(lines, index):
            body.append(lines[index])
            index += 1
        out.append(('paragraph', '\n'.join(body).strip()))
    return out


def paragraph_form(text: str) -> str:
    stripped = text.strip()
    if stripped.startswith(('{', '[')):
        try:
            json.loads(stripped)
            return f'[[:code {{:lang "json"}} {quote(stripped)}]]'
        except json.JSONDecodeError:
            pass
    return quote(text)


def render(source: str, chapter_form: str, markdown: str) -> str:
    chapter_match = CHAPTER.match(chapter_form)
    if not chapter_match:
        raise ValueError(f'unsupported chapter form in {source}: {chapter_form}')
    chapter_title = json.loads(chapter_match.group(1))
    items = tokenize(markdown)

    removed_title_level = None
    for index, item in enumerate(items):
        if item[0] == 'heading':
            if clean_title(item[2]).casefold() == clean_title(chapter_title).casefold():
                removed_title_level = item[1]
                del items[index]
            break

    remaining_levels = [item[1] for item in items if item[0] == 'heading']
    section_level = (removed_title_level + 1) if removed_title_level is not None else (min(remaining_levels) if remaining_levels else 2)

    prefix = slug(source)
    seen = {}
    forms = [chapter_form]
    for item in items:
        kind = item[0]
        if kind == 'heading':
            level, title = item[1], item[2]
            depth = max(0, level - section_level)
            form_kind = 'section' if depth == 0 else 'subsection' if depth == 1 else 'subsubsection'
            key = f'merged-{prefix}-{slug(title)}'
            seen[key] = seen.get(key, 0) + 1
            link = key if seen[key] == 1 else f'{key}-{seen[key]}'
            forms.append(f'[[:{form_kind} {{:title {quote(title)} :link {quote(link)}}}]]')
        elif kind == 'paragraph':
            if item[1]:
                forms.append(paragraph_form(item[1]))
        elif kind == 'code':
            forms.append(f'[[:code {{:lang {quote(item[1])}}} {quote(item[2])}]]')
        elif kind == 'quote':
            forms.append(f'[[:quote {{:text {quote(item[1])}}}]]')
        elif kind == 'callout':
            tone = 'warning' if item[1] in {'warning', 'caution'} else 'success' if item[1] == 'tip' else 'info'
            forms.append(f'[[:callout {{:tone :{tone} :title {quote(item[1].title())} :content {quote(item[2])}}}]]')
        elif kind == 'image':
            forms.append(f'[[:image {{:src {quote(item[2])} :alt {quote(item[1])} :title {quote(item[1])}}}]]')
        else:
            raise ValueError((source, item))
    return '\n\n'.join(forms)


def replace(match):
    source, checksum, chapter_form, encoded_markdown = match.groups()
    markdown = json.loads(encoded_markdown)
    rendered = render(source, chapter_form, markdown)
    return '\n'.join([
        f';; BEGIN merged documentation: {source}',
        f';; sha256: {checksum}',
        rendered,
        f';; END merged documentation: {source}',
    ])


def validate_balanced(path: Path):
    text = path.read_text()
    stack = []
    pairs = {')': '(', ']': '[', '}': '{'}
    line = 1
    column = 0
    in_string = False
    escaped = False
    comment = False
    for char in text:
        column += 1
        if char == '\n':
            line += 1
            column = 0
            comment = False
            continue
        if comment:
            continue
        if in_string:
            if escaped:
                escaped = False
            elif char == '\\':
                escaped = True
            elif char == '"':
                in_string = False
            continue
        if char == ';':
            comment = True
        elif char == '"':
            in_string = True
        elif char in '([{':
            stack.append((char, line, column))
        elif char in ')]}':
            if not stack or stack[-1][0] != pairs[char]:
                raise ValueError(f'{path}:{line}:{column}: unmatched {char}')
            stack.pop()
    if in_string:
        raise ValueError(f'{path}: unterminated string')
    if stack:
        raise ValueError(f'{path}: unclosed delimiter {stack[-1]}')


def validate_rendered_blocks():
    block_pattern = re.compile(
        r';; BEGIN merged documentation: plans/slop/summary/.*?'
        r';; END merged documentation: plans/slop/summary/[^\n]+',
        re.DOTALL,
    )
    blocks = 0
    raw_markdown = []
    literal_pattern = re.compile(r'(?m)^"(?:\\.|[^"\\])*"$')
    for path in sorted(DOC_ROOT.rglob('*.clj')):
        text = path.read_text()
        validate_balanced(path)
        for match in block_pattern.finditer(text):
            blocks += 1
            for literal in literal_pattern.findall(match.group(0)):
                value = json.loads(literal)
                if re.search(r'(?m)^#{1,6}\s+', value) or re.search(r'(?m)^(```|~~~)', value):
                    raw_markdown.append((path, value[:80]))
    if blocks != 100:
        raise ValueError(f'expected 100 rendered summary blocks, found {blocks}')
    if raw_markdown:
        raise ValueError(f'raw Markdown remained in rendered blocks: {raw_markdown[:3]}')


def main():
    changed = []
    count = 0
    for path in sorted(DOC_ROOT.rglob('*.clj')):
        text = path.read_text()
        new_text, replacements = BLOCK.subn(replace, text)
        if replacements:
            path.write_text(new_text)
            changed.append(path)
            count += replacements
    if count != 100:
        raise SystemExit(f'expected 100 summary blocks, converted {count}')
    validate_rendered_blocks()
    print(f'converted {count} blocks across {len(changed)} files')
    for path in changed:
        print(path.relative_to(ROOT))


if __name__ == '__main__':
    main()
