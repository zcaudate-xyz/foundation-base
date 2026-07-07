import json
import re
import sys
from pathlib import Path

sys.path.insert(0, ".github/scripts")
from convert_merged_docs_to_dsl import render

PATTERN = re.compile(
    r"^;; BEGIN merged documentation: ([^\n]+)\n"
    r";; sha256: ([0-9a-f]+)\n"
    r"\[\[:chapter[^\n]*\]\]\n"
    r"([^\n]+)\n"
    r";; END merged documentation: [^\n]+$",
    re.MULTILINE,
)


def replace_block(match):
    source = match.group(1)
    checksum = match.group(2)
    markdown = json.loads(match.group(3))
    return "\n".join((
        f";; BEGIN merged documentation: {source}",
        f";; sha256: {checksum}",
        render(source, markdown),
        f";; END merged documentation: {source}",
    ))
