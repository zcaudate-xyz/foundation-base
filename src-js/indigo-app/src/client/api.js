
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
    const response = await fetch(`/api/browse/${type}/components`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ ns })
    });
    if (!response.ok) {
        throw new Error(`Failed to fetch components: ${response.statusText}`);
    }
    return response.json();
}

export async function fetchComponent(type, ns, component) {
    const response = await fetch(`/api/browse/${type}/component`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ ns, component })
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
