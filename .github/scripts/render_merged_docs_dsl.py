from convert_merged_docs_to_dsl import quote, slug, source_title, tokenize


def heading_form(kind, title, link):
    return f'[[:{kind} {{:title {quote(title)} :link {quote(link)}}}]]'


def render(source, markdown):
    return []
