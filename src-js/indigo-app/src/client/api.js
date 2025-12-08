
export async function fetchNamespaces(type = 'clj') {
    const response = await fetch(`/api/browse/${type}/namespaces`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({})
    });
    if (!response.ok) {
        throw new Error(`Failed to fetch namespaces: ${response.statusText}`);
    }
    return response.json();
}

export async function fetchLibraries() {
    const response = await fetch(`/api/browse/libraries`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({})
    });
    if (!response.ok) {
        throw new Error(`Failed to fetch libraries: ${response.statusText}`);
    }
    return response.json();
}

export async function scanNamespaces() {
    const response = await fetch(`/api/browse/scan`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({})
    });
    if (!response.ok) {
        throw new Error(`Failed to scan namespaces: ${response.statusText}`);
    }
    return response.json();
}

export async function fetchComponents(type, ns) {
    const response = await fetch(`/api/browse/lang/components`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ lang: type, ns })
    });
    if (!response.ok) {
        throw new Error(`Failed to fetch components: ${response.statusText}`);
    }
    return response.json();
}

export async function fetchComponent(type, ns, component) {
    let url = `/api/browse/${type}/component`;
    let body = { ns, component };

    if (type !== 'clj' && type !== 'test') {
        url = `/api/browse/lang/component`;
        body = { lang: type, ns, component };
    }

    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    });
    if (!response.ok) {
        throw new Error(`Failed to fetch component: ${response.statusText}`);
    }
    return response.json();
}

export async function fetchNamespaceSource(ns) {
    const response = await fetch(`/api/browse/clj/namespace-source`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ ns })
    });
    if (!response.ok) {
        throw new Error(`Failed to fetch namespace source: ${response.statusText}`);
    }
    return response.json();
}

export async function fetchCljVars(ns) {
    const response = await fetch(`/api/browse/clj/components`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ ns })
    });
    if (!response.ok) {
        throw new Error(`Failed to fetch vars: ${response.statusText}`);
    }
    return response.json();
}

export async function fetchVarTests(ns, varName) {
    const response = await fetch(`/api/browse/clj/var-tests`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ ns, var: varName })
    });
    if (!response.ok) {
        throw new Error(`Failed to fetch var tests: ${response.statusText}`);
    }
    return response.json();
}

export async function fetchTestFacts(ns) {
    const response = await fetch(`/api/browse/test/components`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ ns })
    });
    if (!response.ok) {
        throw new Error(`Failed to fetch test facts: ${response.statusText}`);
    }
    return response.json();
}

export async function saveNamespaceSource(ns, source) {
    const response = await fetch(`/api/browse/clj/save-namespace-source`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ ns, source })
    });
    if (!response.ok) {
        throw new Error(`Failed to save namespace source: ${response.statusText}`);
    }
    return response.json();
}
export async function fetchCompletions(ns, prefix) {
    const response = await fetch(`/api/browse/clj/completions`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ ns, prefix })
    });
    if (!response.ok) {
        throw new Error(`Failed to fetch completions: ${response.statusText}`);
    }
    return response.json();
}

export async function emitComponent(lang, ns, component) {
    const response = await fetch(`/api/browse/lang/emit-component`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ lang, ns, component })
    });
    if (!response.ok) {
        throw new Error(`Failed to emit component: ${response.statusText}`);
    }
    return response.text();
}

export function runTestVar(ns, varName) {
    return fetch('/api/browse/test/run-var', {
        method: 'POST',
        body: JSON.stringify({ ns, var: varName })
    }).then(res => res.json())
}

export function runTestNs(ns) {
    return fetch('/api/browse/test/run-ns', {
        method: 'POST',
        body: JSON.stringify({ ns })
    }).then(res => res.json())
}

export async function scaffoldTest(ns) {
    const res = await fetch(`/api/browse/clj/scaffold-test`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ ns }),
    });
    if (!res.ok) {
        throw new Error(`Failed to scaffold test: ${res.statusText}`);
    }
    return res.json();
}

export async function fetchDocPath(ns) {
    const res = await fetch(`/api/browse/clj/doc-path`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ ns }),
    });
    if (!res.ok) {
        throw new Error(`Failed to fetch doc path: ${res.statusText}`);
    }
    return res.json();
}

export async function fetchFileContent(path) {
    const res = await fetch(`/api/browse/clj/file-content`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ path }),
    });
    if (!res.ok) {
        throw new Error(`Failed to fetch file content: ${res.statusText}`);
    }
    return res.json();
}

export async function deletePath(path) {
    const res = await fetch(`/api/browse/clj/delete-path`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ path }),
    });
    if (!res.ok) {
        throw new Error(`Failed to delete path: ${res.statusText}`);
    }
    return res.json();
}

export async function fetchNamespaceEntries(ns) {
    const res = await fetch(`/api/browse/clj/namespace-entries`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ ns }),
    });
    if (!res.ok) {
        throw new Error(`Failed to fetch namespace entries: ${res.statusText}`);
    }
    return res.json();
}

export async function runTest(ns, varName) {
    const res = await fetch(`/api/browse/test/run-var`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ ns, var: varName }),
    });
    if (!res.ok) {
        throw new Error(`Failed to run test: ${res.statusText}`);
    }
    return res.json();
}

export async function translateToHeal(source) {
    const res = await fetch('/api/translate/to-heal', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ source })
    });
    if (!res.ok) {
        throw new Error(`Failed to heal code: ${res.statusText}`);
    }
    return res.json();
}

export async function translateFromHtml(html) {
    const res = await fetch('/api/translate/from-html', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ html })
    });
    if (!res.ok) {
        throw new Error(`Failed to translate from html: ${res.statusText}`);
    }
    return res.json();
}

export async function translateToHtml(dsl) {
    const res = await fetch('/api/translate/to-html', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ dsl })
    });
    if (!res.ok) {
        throw new Error(`Failed to translate to html: ${res.statusText}`);
    }
    return res.json();
}
