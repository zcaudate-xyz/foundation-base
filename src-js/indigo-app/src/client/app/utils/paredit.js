export function slurpForward(text, offset) {
    // 1. Find the parent list enclosing the offset
    const parent = findEnclosingList(text, offset);
    if (!parent) return null;

    // 2. Find the next sibling form after the parent's closing delimiter
    const nextSibling = findNextForm(text, parent.end);
    if (!nextSibling) return null;

    // 3. Move the closing delimiter to after the next sibling
    // Remove the old closing delimiter
    const before = text.substring(0, parent.end - 1);
    const middle = text.substring(parent.end, nextSibling.end);
    const after = text.substring(nextSibling.end);

    // Insert the closing delimiter after the next sibling
    // We need to handle whitespace carefully?
    // Actually, just moving the char is enough for basic slurp.

    const newText = before + middle + text[parent.end - 1] + after;

    return {
        text: newText,
        offset: offset // Cursor stays relative to content
    };
}

export function barfForward(text, offset) {
    // 1. Find the parent list enclosing the offset
    const parent = findEnclosingList(text, offset);
    if (!parent) return null;

    // 2. Find the last form inside the parent
    const lastChild = findLastChild(text, parent.start, parent.end);
    if (!lastChild) return null;

    // 3. Move the closing delimiter to before the last child
    const before = text.substring(0, lastChild.start);
    const childText = text.substring(lastChild.start, lastChild.end);
    const after = text.substring(parent.end); // Includes everything after old close

    // We need to insert the closing delimiter before the child
    // And remove the old closing delimiter (at parent.end - 1)

    // text: (... (child) )
    // parent.end is index after ')'
    // parent.end - 1 is ')'

    // New text: (... ) (child)

    const newText = before + text[parent.end - 1] + " " + childText + text.substring(parent.end);
    // Note: added a space to separate the ejected form

    // But wait, we need to remove the old closing delimiter from 'after'?
    // 'after' starts at parent.end, so it doesn't include the old close.
    // So we just construct:
    // before + close + child + after

    // Wait, 'before' includes everything up to start of last child.
    // So it includes the open paren and previous children.

    return {
        text: before + text[parent.end - 1] + childText + after,
        offset: offset
    };
}

// Helpers

function findEnclosingList(text, offset) {
    // Scan backwards for open paren, keeping track of balance
    let balance = 0;
    let start = -1;
    for (let i = offset - 1; i >= 0; i--) {
        const char = text[i];
        if (char === ')') balance++;
        else if (char === '}') balance++;
        else if (char === ']') balance++;
        else if (char === '(') {
            if (balance === 0) {
                start = i;
                break;
            }
            balance--;
        }
        else if (char === '{') {
            if (balance === 0) {
                start = i;
                break;
            }
            balance--;
        }
        else if (char === '[') {
            if (balance === 0) {
                start = i;
                break;
            }
            balance--;
        }
    }

    if (start === -1) return null;

    // Find matching close
    const openChar = text[start];
    const closeChar = openChar === '(' ? ')' : openChar === '{' ? '}' : ']';

    balance = 0;
    let end = -1;
    for (let i = start + 1; i < text.length; i++) {
        const char = text[i];
        if (char === openChar) balance++;
        else if (char === closeChar) {
            if (balance === 0) {
                end = i + 1; // Index after close char
                break;
            }
            balance--;
        }
    }

    if (end === -1) return null;

    // Check if offset is actually inside (it should be if we found start backwards)
    if (offset > end) return null; // Should not happen if logic is correct

    return { start, end, type: openChar };
}

function findNextForm(text, startOffset) {
    // Skip whitespace
    let i = startOffset;
    while (i < text.length && /\s/.test(text[i])) i++;
    if (i >= text.length) return null;

    const start = i;
    const char = text[i];

    if (char === '(' || char === '{' || char === '[') {
        // List
        const list = findEnclosingList(text, start + 1); // +1 to be inside
        return list; // list has .start and .end
    } else if (char === '"') {
        // String
        i++;
        while (i < text.length) {
            if (text[i] === '"' && text[i - 1] !== '\\') {
                return { start, end: i + 1 };
            }
            i++;
        }
        return null;
    } else {
        // Atom (symbol, number, keyword)
        // Read until whitespace or delimiter
        while (i < text.length && !/[\s\(\)\[\]\{\}]/.test(text[i])) i++;
        return { start, end: i };
    }
}

function findLastChild(text, parentStart, parentEnd) {
    // Scan inside parent for children
    // We want the last one.
    // Strategy: scan all children, keep the last one.

    let current = parentStart + 1;
    let last = null;

    while (current < parentEnd - 1) {
        const form = findNextForm(text, current);
        if (!form) break;
        if (form.end >= parentEnd) break; // Should not happen
        last = form;
        current = form.end;
    }

    return last;
}

// Helper for token chars
const isTokenChar = (char) => !/[\s\(\)\[\]\{\}"]/.test(char);

export function getSexpBeforeCursor(text, offset) {
    // 0. If cursor is in the middle of a token, move offset to the end of it.
    if (offset > 0 && offset < text.length && isTokenChar(text[offset]) && isTokenChar(text[offset - 1])) {
        while (offset < text.length && isTokenChar(text[offset])) {
            offset++;
        }
    }

    // 1. Skip whitespace backwards
    let i = offset - 1;
    while (i >= 0 && /\s/.test(text[i])) i--;
    if (i < 0) return null;

    const end = i + 1;
    const char = text[i];

    if (char === ')' || char === '}' || char === ']') {
        // List: find matching open
        const list = findEnclosingList(text, end); // findEnclosingList looks backwards from offset
        // Wait, findEnclosingList looks for *enclosing* list of the offset.
        // If we are at '... )|', the offset is outside.
        // We need a function that finds the start of the list ending at 'i'.

        // Let's reuse findEnclosingList logic but adapted?
        // Actually, scan backwards for matching open.
        let balance = 0;
        let start = -1;
        for (let j = i; j >= 0; j--) {
            const c = text[j];
            if (c === ')' || c === '}' || c === ']') balance++;
            else if (c === '(' || c === '{' || c === '[') {
                balance--;
                if (balance === 0) {
                    start = j;
                    break;
                }
            }
        }
        if (start !== -1) {
            return text.substring(start, end);
        }
    } else if (char === '"') {
        // String: find matching open quote
        // This is tricky with escaped quotes.
        let start = -1;
        for (let j = i - 1; j >= 0; j--) {
            if (text[j] === '"' && text[j - 1] !== '\\') {
                start = j;
                break;
            }
        }
        if (start !== -1) {
            return text.substring(start, end);
        }
    } else {
        // Atom: read backwards until whitespace or delimiter
        let start = i;
        while (start >= 0 && !/[\s\(\)\[\]\{\}"]/.test(text[start])) start--;
        start++; // The first char of the atom
        return text.substring(start, end);
    }
    return null;
}

export function getSexpRangeBeforeCursor(text, offset) {
    // 0. If cursor is in the middle of a token, move offset to the end of it.
    if (offset > 0 && offset < text.length && isTokenChar(text[offset]) && isTokenChar(text[offset - 1])) {
        while (offset < text.length && isTokenChar(text[offset])) {
            offset++;
        }
    }

    // 1. Skip whitespace backwards
    let i = offset - 1;
    while (i >= 0 && /\s/.test(text[i])) i--;
    if (i < 0) return null;

    const end = i + 1;
    const char = text[i];

    let start = -1;

    if (char === ')' || char === '}' || char === ']') {
        // List: find matching open
        let balance = 0;
        for (let j = i; j >= 0; j--) {
            const c = text[j];
            if (c === ')' || c === '}' || c === ']') balance++;
            else if (c === '(' || c === '{' || c === '[') {
                balance--;
                if (balance === 0) {
                    start = j;
                    break;
                }
            }
        }
    } else if (char === '"') {
        // String: find matching open quote
        for (let j = i - 1; j >= 0; j--) {
            if (text[j] === '"' && text[j - 1] !== '\\') {
                start = j;
                break;
            }
        }
    } else {
        // Atom: read backwards until whitespace or delimiter
        start = i;
        while (start >= 0 && !/[\s\(\)\[\]\{\}"]/.test(text[start])) start--;
        start++; // The first char of the atom
    }

    if (start !== -1) {
        return {
            text: text.substring(start, end),
            start: start,
            end: end
        };
    }
    return null;
}
