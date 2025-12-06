export function fuzzyMatch(text, query) {
    if (!query) return true;
    text = text.toLowerCase();
    query = query.toLowerCase().replace(/\s+/g, ''); // Ignore spaces in query
    let i = 0, j = 0;
    while (i < text.length && j < query.length) {
        if (text[i] === query[j]) {
            j++;
        }
        i++;
    }
    return j === query.length;
}
