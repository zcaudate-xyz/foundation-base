import json
import re

HEADING = re.compile(r"^(#{1,6})\s+(.+?)\s*$")
FENCE = re.compile(r"^\s*(`{3,}|~{3,})\s*([^\s`]*)?.*$")


def quote(value):
    return json.dumps(value, ensure_ascii=False)


def clean_title(value):
    value = re.sub(r"\[([^]]+)\]\([^)]*\)", r"\1", value)
    return re.sub(r"[`*_]+", "", value).strip()
