import React from 'react'
import Editor from '@monaco-editor/react'
import { fetchNamespaceSource, saveNamespaceSource, fetchCompletions, scaffoldTest } from '../../../api'
import { slurpForward, barfForward, getSexpBeforeCursor } from '../../utils/paredit'
import { send, addMessageListener } from '../../../repl-client'

export function NamespaceViewer({ namespace, selectedVar }) {
    const [source, setSource] = React.useState("");
    const [loading, setLoading] = React.useState(false);
    const [error, setError] = React.useState(null);
    const [viewMode, setViewMode] = React.useState("source"); // "source" | "test"
    const editorRef = React.useRef(null);
    const monacoRef = React.useRef(null);
    const completionProviderRef = React.useRef(null);
    const decorationsRef = React.useRef(null);

    const loadSource = React.useCallback(() => {
        if (namespace) {
            setLoading(true);
            setError(null);
            setSource("");

            const targetNs = viewMode === "test" ? (namespace + "-test") : namespace;

            // Fetch source
            fetchNamespaceSource(targetNs)
                .then(data => {
                    if (data.startsWith(";; File not found")) {
                        if (viewMode === "test") {
                            setError("Test file not found");
                        } else {
                            setError(data);
                        }
                    } else {
                        setSource(data);
                    }
                    setLoading(false);
                })
                .catch(err => {
                    console.error("Failed to fetch namespace source", err);
                    setError(err.message);
                    setLoading(false);
                });
        } else {
            setSource("");
        }
    }, [namespace, viewMode]);

    React.useEffect(() => {
        loadSource();
    }, [loadSource]);

    React.useEffect(() => {
        if (selectedVar && editorRef.current) {
            const model = editorRef.current.getModel();
            const text = model.getValue();
            // Simple regex to find (defn varName ...) or (def varName ...)
            const regex = new RegExp(`\\(def(n|macro)?\\s+${selectedVar}[\\s\\n]`);
            const match = text.match(regex);

            if (match) {
                const position = model.getPositionAt(match.index);
                editorRef.current.revealPositionInCenter(position);
                editorRef.current.setPosition(position);
                editorRef.current.focus();
            }
        }
    }, [selectedVar]);

    // Cleanup completion provider on unmount
    React.useEffect(() => {
        return () => {
            if (completionProviderRef.current) {
                completionProviderRef.current.dispose();
            }
        };
    }, []);

    const handleSave = async () => {
        if (!namespace || !editorRef.current) return;
        const currentSource = editorRef.current.getValue();
        const targetNs = viewMode === "test" ? (namespace + "-test") : namespace;
        try {
            await saveNamespaceSource(targetNs, currentSource);
            console.log("Saved namespace:", targetNs);
            // Optional: Show success feedback
        } catch (err) {
            console.error("Failed to save namespace", err);
            // Optional: Show error feedback
        }
    };

    const handleScaffold = async () => {
        setLoading(true);
        try {
            await scaffoldTest(namespace);
            // Reload source to show the new test file
            loadSource();
        } catch (err) {
            console.error("Failed to scaffold test", err);
            setError(err.message);
            setLoading(false);
        }
    };

    if (!namespace) {
        return (
            <div className="flex flex-col h-full bg-[#1a1a1a] items-center justify-center text-gray-500 text-xs">
                No namespace selected
            </div>
        );
    }

    return (
        <div className="flex flex-col h-full bg-[#1e1e1e]">
            <div className="h-10 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-4 justify-between">
                <span className="text-xs text-gray-400 font-mono">{namespace}</span>
                <div className="flex bg-[#1e1e1e] rounded p-0.5">
                    <button
                        onClick={() => setViewMode("source")}
                        className={`px-3 py-1 text-xs rounded ${viewMode === "source" ? "bg-[#323232] text-gray-200" : "text-gray-500 hover:text-gray-300"}`}
                    >
                        Source
                    </button>
                    <button
                        onClick={() => setViewMode("test")}
                        className={`px-3 py-1 text-xs rounded ${viewMode === "test" ? "bg-[#323232] text-gray-200" : "text-gray-500 hover:text-gray-300"}`}
                    >
                        Test
                    </button>
                </div>
            </div>
            <div className="flex-1 overflow-hidden relative">
                {loading ? (
                    <div className="absolute inset-0 flex items-center justify-center text-xs text-gray-500">Loading source...</div>
                ) : error ? (
                    <div className="absolute inset-0 flex flex-col items-center justify-center gap-4">
                        <div className="text-xs text-red-500">Error: {error}</div>
                        {viewMode === "test" && error === "Test file not found" && (
                            <button
                                onClick={handleScaffold}
                                className="px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white text-xs rounded shadow-sm transition-colors"
                            >
                                Scaffold Test
                            </button>
                        )}
                    </div>
                ) : (
                    <Editor
                        height="100%"
                        defaultLanguage="clojure"
                        value={source}
                        theme="vs-dark"
                        options={{
                            minimap: { enabled: false },
                            fontSize: 12,
                            lineNumbers: 'on',
                            scrollBeyondLastLine: false,
                            automaticLayout: true,
                            autoClosingBrackets: 'always',
                            matchBrackets: 'always',
                            readOnly: false
                        }}
                        onMount={(editor, monaco) => {
                            editorRef.current = editor;
                            monacoRef.current = monaco;
                            decorationsRef.current = editor.createDecorationsCollection();

                            // Register Completion Provider
                            // Dispose previous if exists (though useEffect handles unmount, this handles re-mounts if any)
                            if (completionProviderRef.current) {
                                completionProviderRef.current.dispose();
                            }

                            completionProviderRef.current = monaco.languages.registerCompletionItemProvider('clojure', {
                                provideCompletionItems: async (model, position) => {
                                    const word = model.getWordUntilPosition(position);
                                    const range = {
                                        startLineNumber: position.lineNumber,
                                        endLineNumber: position.lineNumber,
                                        startColumn: word.startColumn,
                                        endColumn: word.endColumn
                                    };

                                    try {
                                        const suggestions = await fetchCompletions(namespace, word.word);
                                        return {
                                            suggestions: suggestions.map(s => ({
                                                label: s,
                                                kind: monaco.languages.CompletionItemKind.Function,
                                                insertText: s,
                                                range: range
                                            }))
                                        };
                                    } catch (err) {
                                        console.error("Completion error", err);
                                        return { suggestions: [] };
                                    }
                                }
                            });

                            // Add Save Action
                            editor.addAction({
                                id: 'save-namespace',
                                label: 'Save Namespace',
                                keybindings: [
                                    monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_S
                                ],
                                run: () => handleSave()
                            });

                            // Add Eval Last Sexp Action
                            editor.addAction({
                                id: 'eval-last-sexp',
                                label: 'Eval Last Sexp',
                                keybindings: [
                                    monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_E
                                ],
                                run: (ed) => {
                                    const model = ed.getModel();
                                    const position = ed.getPosition();
                                    const offset = model.getOffsetAt(position);
                                    const text = model.getValue();

                                    const sexp = getSexpBeforeCursor(text, offset);
                                    if (sexp) {
                                        console.log("Evaluating:", sexp);
                                        const id = "eval-" + Date.now() + "-" + Math.random();

                                        // Add listener for result
                                        const removeListener = addMessageListener((msg) => {
                                            if (msg.id === id) {
                                                console.log("Eval Result:", msg);
                                                removeListener();

                                                const resultText = msg.error ? ("Error: " + msg.error) : msg.result;
                                                const color = msg.error ? "red" : "#888888"; // Gray for result

                                                // Show result inline
                                                decorationsRef.current.set([
                                                    {
                                                        range: new monaco.Range(position.lineNumber, position.column, position.lineNumber, position.column),
                                                        options: {
                                                            after: {
                                                                content: " => " + resultText,
                                                                inlineClassName: msg.error ? "text-red-500" : "text-gray-500",
                                                                color: color
                                                            }
                                                        }
                                                    }
                                                ]);

                                                // Clear after 5 seconds
                                                setTimeout(() => {
                                                    decorationsRef.current.clear();
                                                }, 5000);
                                            }
                                        });

                                        send({
                                            op: "eval",
                                            id: id,
                                            code: sexp
                                        });
                                    } else {
                                        console.log("No sexp found before cursor");
                                    }
                                }
                            });

                            editor.addAction({
                                id: 'paredit-slurp-forward',
                                label: 'Paredit Slurp Forward',
                                keybindings: [
                                    monaco.KeyMod.CtrlCmd | monaco.KeyCode.RightArrow,
                                    monaco.KeyMod.Alt | monaco.KeyCode.RightArrow
                                ],
                                run: (ed) => {
                                    const model = ed.getModel();
                                    const position = ed.getPosition();
                                    const offset = model.getOffsetAt(position);
                                    const text = model.getValue();

                                    const result = slurpForward(text, offset);
                                    if (result) {
                                        ed.executeEdits('paredit', [{
                                            range: model.getFullModelRange(),
                                            text: result.text
                                        }]);
                                        ed.setPosition(model.getPositionAt(result.offset));
                                    }
                                }
                            });

                            editor.addAction({
                                id: 'paredit-barf-forward',
                                label: 'Paredit Barf Forward',
                                keybindings: [
                                    monaco.KeyMod.CtrlCmd | monaco.KeyCode.LeftArrow,
                                    monaco.KeyMod.Alt | monaco.KeyCode.LeftArrow
                                ],
                                run: (ed) => {
                                    const model = ed.getModel();
                                    const position = ed.getPosition();
                                    const offset = model.getOffsetAt(position);
                                    const text = model.getValue();

                                    const result = barfForward(text, offset);
                                    if (result) {
                                        ed.executeEdits('paredit', [{
                                            range: model.getFullModelRange(),
                                            text: result.text
                                        }]);
                                        ed.setPosition(model.getPositionAt(result.offset));
                                    }
                                }
                            });
                        }}
                        onChange={(value) => setSource(value)}
                    />
                )}
            </div>
        </div>
    );
}
