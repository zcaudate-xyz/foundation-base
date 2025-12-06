import React from 'react'
import Editor from '@monaco-editor/react'
import { fetchNamespaceSource, saveNamespaceSource, fetchCompletions } from '../../../api'
import { slurpForward, barfForward, getSexpBeforeCursor } from '../../utils/paredit'
import { send } from '../../../repl-client'

export function NamespaceViewer({ namespace, selectedVar }) {
    const [source, setSource] = React.useState("");
    const [loading, setLoading] = React.useState(false);
    const [error, setError] = React.useState(null);
    const editorRef = React.useRef(null);
    const monacoRef = React.useRef(null);
    const completionProviderRef = React.useRef(null);

    React.useEffect(() => {
        if (namespace) {
            setLoading(true);

            // Fetch source
            fetchNamespaceSource(namespace)
                .then(data => {
                    setSource(data);
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
    }, [namespace]);

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
        try {
            await saveNamespaceSource(namespace, currentSource);
            console.log("Saved namespace:", namespace);
            // Optional: Show success feedback
        } catch (err) {
            console.error("Failed to save namespace", err);
            // Optional: Show error feedback
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
            <div className="h-10 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-4">
                <span className="text-xs text-gray-400 font-mono">{namespace}</span>
            </div>
            <div className="flex-1 overflow-hidden">
                {loading ? (
                    <div className="p-4 text-xs text-gray-500">Loading source...</div>
                ) : error ? (
                    <div className="p-4 text-xs text-red-500">Error: {error}</div>
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
                                        send(sexp);
                                        // Optional: Flash selection?
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
