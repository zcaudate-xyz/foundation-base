import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DOC_ROOT = ROOT / "src-doc/documentation"
SOURCE_PREFIX = "plans/slop/summary/"

BLOCK_RE = re.compile(
    r"(;; BEGIN merged documentation: (" + re.escape(SOURCE_PREFIX) + r"[^\n]+)\n)"
    r"(.*?)"
    r"(;; END merged documentation: \2)",
    re.S,
)
CODE_RE = re.compile(r'\[\[:code \{:lang "clojure"\} ("(?:\\.|[^"\\])*")\]\]')
HEADING_RE = re.compile(
    r'\[\[:(?:chapter|section|subsection|subsubsection) '
    r'\{:title ("(?:\\.|[^"\\])*")(?: [^}]*)?\}\]\]'
)
COMMENT_LINE_RE = re.compile(r"^(\s*);; ?(.*)$")
ARROW_LINE_RE = re.compile(r"^(\s*);;\s*=>\s?(.*)$")


def slug(value):
    return re.sub(r"[^a-z0-9]+", "-", value.lower()).strip("-") or "example"


def title_from_source(source):
    stem = Path(source).stem
    return re.sub(r"[_\-.]+", " ", stem).strip().title()


def scan_state(text, state=None):
    if state is None:
        state = {"stack": [], "string": False, "escape": False}
    stack = state["stack"]
    in_string = state["string"]
    escaped = state["escape"]
    pairs = {")": "(", "]": "[", "}": "{"}
    index = 0
    while index < len(text):
        char = text[index]
        if in_string:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == '"':
                in_string = False
            index += 1
            continue
        if char == '"':
            in_string = True
        elif char == ";":
            break
        elif char in "([{":
            stack.append(char)
        elif char in ")]}":
            if stack and stack[-1] == pairs[char]:
                stack.pop()
        index += 1
    state["string"] = in_string
    state["escape"] = escaped
    return state


def incomplete(state):
    return state["string"] or bool(state["stack"])


def uncomment_pseudo_example(body):
    lines = body.splitlines()
    nonblank = [line for line in lines if line.strip()]
    if not nonblank or not all(COMMENT_LINE_RE.match(line) for line in nonblank):
        return body
    stripped = [COMMENT_LINE_RE.match(line).group(2) if line.strip() else ""
                for line in lines]
    has_example = any(
        re.match(r"^(?:\(|\[|\{|\'|`|~|@|#\(|#\{|=>)", line.strip())
        for line in stripped[1:]
    )
    if not has_example:
        return body
    output = []
    for index, line in enumerate(stripped):
        if index == 0 and line.lower().startswith("no direct test example"):
            output.append(";; " + line)
        else:
            output.append(line)
    return "\n".join(output)


def convert_arrow_comments(body):
    lines = body.splitlines()
    output = []
    index = 0
    while index < len(lines):
        line = lines[index]
        match = ARROW_LINE_RE.match(line)
        if not match:
            output.append(line.rstrip())
            index += 1
            continue
        indent, expected = match.groups()
        output.append(f"{indent}=> {expected}".rstrip())
        state = scan_state(expected)
        index += 1
        while incomplete(state) and index < len(lines):
            continuation = COMMENT_LINE_RE.match(lines[index])
            if not continuation:
                break
            continuation_text = continuation.group(2)
            output.append(f"{indent}   {continuation_text}".rstrip())
            state = scan_state(continuation_text, state)
            index += 1
    return "\n".join(output).rstrip()


def prose_from_comment_only(body):
    lines = body.splitlines()
    nonblank = [line for line in lines if line.strip()]
    if not nonblank or not all(COMMENT_LINE_RE.match(line) for line in nonblank):
        return None
    if any("=>" in line for line in nonblank):
        return None
    text = "\n".join(
        COMMENT_LINE_RE.match(line).group(2) if line.strip() else ""
        for line in lines
    ).strip()
    return text or None


def indent_body(body):
    lines = body.rstrip().splitlines()
    if not lines:
        return "  ;; Example intentionally contains no executable form."
    return "\n".join(("  " + line.rstrip()) if line else "" for line in lines)


def repair_body(body):
    body = body.replace(
        "(compile-section-base '{:element (if & _)}\n",
        "(compile-section-base '{:element (if & _)})\n",
    )
    body = body.replace(
        '(boundary? (first """))',
        '(boundary? (first "\\\""))',
    )
    if body.startswith(
        ';; No direct test example, but it would involve:\n'
        ';; (-> (parse-string "(#|)")\n'
        ';;     (insert-newline)'
    ):
        body = (
            ';; No direct test example, but it would involve:\n'
            '(-> (parse-string "(#|)")\n'
            '    (insert-newline)\n'
            '    str)\n'
            ';; => "<0,1> (|\\n)"'
        )
    return body


def render_fact(source, ordinal, heading, body):
    source_title = title_from_source(source)
    context = heading or source_title
    caption = f"{context} example"
    identifier = f"merged-{slug(source)}-example-{ordinal}"
    body = repair_body(body)
    body = uncomment_pseudo_example(body)
    body = convert_arrow_comments(body)
    return (
        f'^{{:id {identifier} :added "4.0"}}\n'
        f'(fact {json.dumps(caption, ensure_ascii=False)}\n'
        f'{indent_body(body)}\n'
        f')'
    )


def convert_block(match):
    begin, source, content, end = match.groups()
    parts = []
    position = 0
    current_heading = None
    ordinal = 0
    for code_match in CODE_RE.finditer(content):
        prefix = content[position:code_match.start()]
        for heading_match in HEADING_RE.finditer(prefix):
            current_heading = json.loads(heading_match.group(1))
        parts.append(prefix)
        ordinal += 1
        body = json.loads(code_match.group(1))
        prose = prose_from_comment_only(body)
        if prose is not None:
            parts.append(json.dumps(prose, ensure_ascii=False))
        else:
            parts.append(render_fact(source, ordinal, current_heading, body))
        position = code_match.end()
    parts.append(content[position:])
    return begin + "".join(parts) + end


def add_code_test_use(text):
    namespace_end = text.find("\n\n")
    if namespace_end < 0:
        raise ValueError("namespace form not found")
    namespace_form = text[:namespace_end]
    if re.search(r"\(:use\s+code\.test\)", namespace_form):
        return text
    if not namespace_form.endswith(")"):
        raise ValueError("namespace form does not end at first paragraph")
    namespace_form = namespace_form[:-1] + "\n  (:use code.test))"
    return namespace_form + text[namespace_end:]


def validate_balanced(path):
    state = {"stack": [], "string": False, "escape": False}
    for line in path.read_text().splitlines(True):
        scan_state(line, state)
    if incomplete(state):
        raise ValueError(f"{path}: unbalanced form state {state}")


def main():
    changed = []
    converted = 0
    facts = 0
    prose = 0
    for path in sorted(DOC_ROOT.rglob("*.clj")):
        text = path.read_text()
        count = sum(
            len(CODE_RE.findall(match.group(3)))
            for match in BLOCK_RE.finditer(text)
        )
        if not count:
            continue
        new_text = BLOCK_RE.sub(convert_block, text)
        if any(
            CODE_RE.findall(match.group(3))
            for match in BLOCK_RE.finditer(new_text)
        ):
            raise ValueError(f"clojure code directive remained in {path}")
        new_text = add_code_test_use(new_text)
        new_text = "\n".join(line.rstrip() for line in new_text.splitlines())
        if text.endswith("\n"):
            new_text += "\n"
        path.write_text(new_text)
        validate_balanced(path)
        changed.append(path)
        converted += count
        summary = "\n".join(
            match.group(3) for match in BLOCK_RE.finditer(new_text)
        )
        facts += len(re.findall(r"^\(fact ", summary, re.M))
        prose += len(re.findall(r'^"No direct test example', summary, re.M))
    if converted != 296:
        raise ValueError(f"expected 296 directives, converted {converted}")
    if facts + prose != converted:
        raise ValueError(
            f"expected {converted} replacements, found {facts} facts and {prose} prose blocks"
        )
    print(
        f"converted {converted} directives into {facts} facts and "
        f"{prose} prose notes across {len(changed)} files"
    )
    for path in changed:
        print(path.relative_to(ROOT))


if __name__ == "__main__":
    main()
