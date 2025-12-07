import React from 'react'
import Editor from '@monaco-editor/react'
import { saveNamespaceSource, fetchCompletions, scaffoldTest } from '../../../api'
import { slurpForward, barfForward, getSexpBeforeCursor } from '../../utils/paredit'
import { send, addMessageListener } from '../../../repl-client'
import * as FigmaUi from '@xtalk/figma-ui'
import * as Lucide from 'lucide-react'
import { useAppState } from '../../state'
import { toast } from 'sonner'

export function NamespaceViewer() {
    const {
        selectedNamespace: namespace,
        selectedVar,
        namespaceViewType: viewType,
        namespaceFileViewMode: fileViewMode,
        namespaceEntries: entries,
        namespaceCode: code,
        namespaceLoading: loading,
        namespaceError: error,
        setNamespaceViewType: setViewType,
        setNamespaceFileViewMode: setFileViewMode,
        setNamespaceCode: setCode,
        setNamespaceLoading: setLoading,
        setNamespaceError: setError,
        refreshNamespaceCode,
        refreshNamespaceEntries
    } = useAppState();

    const [scaffoldLoading, setScaffoldLoading] = React.useState(false);
    const [runningTest, setRunningTest] = React.useState(null);

    // Refs for editors
    const fileEditorRef = React.useRef(null);
    const sourceEditorRef = React.useRef(null);
    const testEditorRef = React.useRef(null);

    // Global Monaco refs
    const monacoRef = React.useRef(null);
    const completionProviderRef = React.useRef(null);

    // Effect to load content based on view type
    React.useEffect(() => {
        if (!namespace) return;
        if (viewType === "file") {
            refreshNamespaceCode();
        } else {
            refreshNamespaceEntries();
        }
    }, [viewType, namespace, refreshNamespaceCode, refreshNamespaceEntries]);

    // Effect to handle external selection (scroll to var in file view)
    React.useEffect(() => {
        if (selectedVar && viewType === "file" && fileEditorRef.current) {
            const model = fileEditorRef.current.getModel();
            if (model) {
                const text = model.getValue();
                const regex = new RegExp(`\\(def(n|macro)?\\s+${selectedVar}[\\s\\n]`);
                const match = text.match(regex);

                if (match) {
                    const position = model.getPositionAt(match.index);
                    fileEditorRef.current.revealPositionInCenter(position);
                    fileEditorRef.current.setPosition(position);
                    fileEditorRef.current.focus();
                }
            }
        }
    }, [selectedVar, viewType]);

    // Register Completion Provider (Global)
    React.useEffect(() => {
        // We need monaco instance to register provider. 
        // We can capture it from the first editor mount.
        // Or we can use the `beforeMount` prop of Editor to get monaco instance?
        // But `onMount` gives us `monaco`.

        return () => {
            if (completionProviderRef.current) {
                completionProviderRef.current.dispose();
            }
        };
    }, []);

    const registerCompletion = (monaco) => {
        if (!monaco || completionProviderRef.current) return;

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
                    // Use current namespace from state (might be stale in callback?)
                    // Better to use the model's content or context if possible.
                    // For now, using `namespace` from closure.
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
    };

    const handleSave = async (editorInstance, targetNs) => {
        if (!namespace || !editorInstance) return;

        const currentSource = editorInstance.getValue();

        try {
            await saveNamespaceSource(targetNs, currentSource);
            console.log("Saved namespace:", targetNs);
            if (viewType === "file") {
                setCode(currentSource);
            }
            toast.success(`Saved ${targetNs}`);
        } catch (err) {
            console.error("Failed to save namespace", err);
            toast.error("Failed to save: " + err.message);
        }
    };

    const handleScaffold = async () => {
        setScaffoldLoading(true);
        try {
            await scaffoldTest(namespace);
            await new Promise(r => setTimeout(r, 1000));
            if (viewType === "file" && fileViewMode === "test") {
                refreshNamespaceCode();
            }
        } catch (err) {
            console.error("Failed to scaffold test", err);
            toast.error("Scaffold failed: " + err.message);
        } finally {
            setScaffoldLoading(false);
        }
    };

    const setupEditor = (editor, monaco, type) => {
        monacoRef.current = monaco;
        registerCompletion(monaco);

        if (type === "file") fileEditorRef.current = editor;
        if (type === "source") sourceEditorRef.current = editor;
        if (type === "test") testEditorRef.current = editor;

        const decorationsCollection = editor.createDecorationsCollection();

        // Add Save Action
        editor.addAction({
            id: 'save-namespace',
            label: 'Save Namespace',
            keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_S],
            run: () => {
                let targetNs = namespace;
                if (type === "file" && fileViewMode === "test") targetNs = namespace + "-test";
                // For entry view, we might want to save the whole file? 
                // But we are viewing a snippet. Saving snippet is hard.
                if (type === "file") {
                    handleSave(editor, targetNs);
                } else {
                    toast.info("Saving individual entries is not yet supported.");
                }
            }
        });

        // Add Eval Last Sexp Action
        editor.addAction({
            id: 'eval-last-sexp',
            label: 'Eval Last Sexp',
            keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_E],
            run: (ed) => {
                const model = ed.getModel();
                const position = ed.getPosition();
                const offset = model.getOffsetAt(position);
                const text = model.getValue();
                const sexp = getSexpBeforeCursor(text, offset);
                if (sexp) {
                    console.log("Evaluating:", sexp);
                    const id = "eval-" + Date.now() + "-" + Math.random();
                    const removeListener = addMessageListener((msg) => {
                        if (msg.id === id) {
                            removeListener();
                            const resultText = msg.error ? ("Error: " + msg.error) : msg.result;
                            const color = msg.error ? "red" : "#888888";
                            decorationsCollection.set([{
                                range: new monaco.Range(position.lineNumber, position.column, position.lineNumber, position.column),
                                options: {
                                    after: {
                                        content: " => " + resultText,
                                        inlineClassName: msg.error ? "text-red-500" : "text-gray-500",
                                        color: color
                                    }
                                }
                            }]);
                            setTimeout(() => { decorationsCollection.clear(); }, 5000);
                        }
                    });
                    send({ op: "eval", id: id, code: sexp });
                }
            }
        });

        // Paredit Actions
        editor.addAction({
            id: 'paredit-slurp-forward',
            label: 'Paredit Slurp Forward',
            keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.RightArrow, monaco.KeyMod.Alt | monaco.KeyCode.RightArrow],
            run: (ed) => {
                const model = ed.getModel();
                const position = ed.getPosition();
                const offset = model.getOffsetAt(position);
                const text = model.getValue();
                const result = slurpForward(text, offset);
                if (result) {
                    ed.executeEdits('paredit', [{ range: model.getFullModelRange(), text: result.text }]);
                    ed.setPosition(model.getPositionAt(result.offset));
                }
            }
        });

        editor.addAction({
            id: 'paredit-barf-forward',
            label: 'Paredit Barf Forward',
            keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.LeftArrow, monaco.KeyMod.Alt | monaco.KeyCode.LeftArrow],
            run: (ed) => {
                const model = ed.getModel();
                const position = ed.getPosition();
                const offset = model.getOffsetAt(position);
                const text = model.getValue();
                const result = barfForward(text, offset);
                if (result) {
                    ed.executeEdits('paredit', [{ range: model.getFullModelRange(), text: result.text }]);
                    ed.setPosition(model.getPositionAt(result.offset));
                }
            }
        });
    };

    const selectedEntry = React.useMemo(() => {
        return entries.find(e => e.var === selectedVar);
    }, [entries, selectedVar]);

    const entryCode = React.useMemo(() => {
        if (!selectedEntry) return "";
        return selectedEntry.source?.source?.code || ";; No source code found";
    }, [selectedEntry]);

    if (!namespace) {
        return (
            <div className="flex flex-col h-full bg-[#1a1a1a] items-center justify-center text-gray-500 text-xs">
                No namespace selected
            </div>
        );
    }

    return (
        <div className="flex flex-col h-full bg-[#1e1e1e]">
            {/* Toolbar */}
            <div className="h-8 bg-[#252526] border-b border-[#323232] flex items-center px-3 justify-between shrink-0">
                <div className="flex items-center gap-3">
                    {/* View Toggle */}
                    <div className="flex bg-[#1e1e1e] rounded p-0.5 border border-[#323232]">
                        <button
                            onClick={() => setViewType("file")}
                            className={`px-2 py-0.5 text-[10px] rounded ${viewType === "file" ? "bg-[#323232] text-gray-200" : "text-gray-500 hover:text-gray-300"}`}
                        >
                            File
                        </button>
                        <button
                            onClick={() => setViewType("entry")}
                            className={`px-2 py-0.5 text-[10px] rounded ${viewType === "entry" ? "bg-[#323232] text-gray-200" : "text-gray-500 hover:text-gray-300"}`}
                        >
                            Entry
                        </button>
                    </div>

                    {/* Code Manage Buttons (Icons) */}
                    <div className="flex items-center gap-1">
                        <button
                            title="Scaffold Test"
                            onClick={handleScaffold}
                            disabled={scaffoldLoading}
                            className={`text-gray-400 hover:text-gray-200 p-1 rounded hover:bg-[#323232] ${scaffoldLoading ? "opacity-50 cursor-not-allowed" : ""}`}
                        >
                            <Lucide.Hammer size={14} />
                        </button>
                        <button title="Import" className="text-gray-400 hover:text-gray-200 p-1 rounded hover:bg-[#323232]">
                            <Lucide.Import size={14} />
                        </button>
                        <button title="Find Incomplete" className="text-gray-400 hover:text-gray-200 p-1 rounded hover:bg-[#323232]">
                            <Lucide.AlertCircle size={14} />
                        </button>
                    </div>
                </div>

                {/* Right: Namespace */}
                <span className="text-xs text-gray-400 font-mono">{namespace}</span>
            </div>

            {/* Content Area */}
            <div className="flex-1 overflow-hidden relative flex">
                {viewType === "file" ? (
                    // File View
                    <div className="flex-1 relative">
                        {/* Floating File View Mode Toggles */}
                        <div className="absolute bottom-4 right-4 flex bg-[#252526] rounded-md p-1 shadow-lg border border-[#323232] z-10">
                            <button
                                onClick={() => setFileViewMode("source")}
                                className={`px-3 py-1 text-xs rounded ${fileViewMode === "source" ? "bg-[#37373d] text-white shadow-sm" : "text-gray-400 hover:text-gray-200 hover:bg-[#2a2d2e]"}`}
                            >
                                Source
                            </button>
                            <button
                                onClick={() => setFileViewMode("test")}
                                className={`px-3 py-1 text-xs rounded ${fileViewMode === "test" ? "bg-[#37373d] text-white shadow-sm" : "text-gray-400 hover:text-gray-200 hover:bg-[#2a2d2e]"}`}
                            >
                                Test
                            </button>
                            <button
                                onClick={() => setFileViewMode("doc")}
                                className={`px-3 py-1 text-xs rounded ${fileViewMode === "doc" ? "bg-[#37373d] text-white shadow-sm" : "text-gray-400 hover:text-gray-200 hover:bg-[#2a2d2e]"}`}
                            >
                                Doc
                            </button>
                        </div>

                        {loading ? (
                            <div className="absolute inset-0 flex items-center justify-center text-xs text-gray-500">Loading...</div>
                        ) : error ? (
                            <div className="absolute inset-0 flex flex-col items-center justify-center gap-4">
                                <div className="text-xs text-red-500">Error: {error}</div>
                                {fileViewMode === "test" && error === "Test file not found" && (
                                    <button
                                        onClick={handleScaffold}
                                        disabled={scaffoldLoading}
                                        className="px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white text-xs rounded shadow-sm transition-colors disabled:opacity-50"
                                    >
                                        {scaffoldLoading ? "Scaffolding..." : "Scaffold Test"}
                                    </button>
                                )}
                            </div>
                        ) : (
                            <Editor
                                height="100%"
                                language="clojure"
                                theme="vs-dark"
                                value={code || ""}
                                options={{
                                    minimap: { enabled: false },
                                    fontSize: 11,
                                    lineNumbers: 'on',
                                    scrollBeyondLastLine: false,
                                    automaticLayout: true,
                                    autoClosingBrackets: 'always',
                                    matchBrackets: 'always',
                                    readOnly: fileViewMode === "doc"
                                }}
                                onMount={(editor, monaco) => setupEditor(editor, monaco, "file")}
                                onChange={(value) => setCode(value)}
                            />
                        )}
                    </div>
                ) : (
                    // Entry View
                    <div className="flex flex-1 h-full relative">
                        <div className="absolute top-0 left-0 right-0 h-6 bg-[#252526] border-b border-[#323232] flex items-center px-2 text-xs text-gray-400 select-none z-10">
                            Source
                        </div>
                        <div className="absolute top-6 left-0 right-0 bottom-0">
                            {selectedEntry ? (
                                <Editor
                                    key={`source-${selectedEntry.var}`}
                                    path={`file:///source/${namespace.replace(/[^a-zA-Z0-9]/g, '_')}/${selectedEntry.var.replace(/[^a-zA-Z0-9]/g, '_')}.clj`}
                                    height="100%"
                                    language="clojure"
                                    theme="vs-dark"
                                    value={entryCode || ""}
                                    options={{
                                        minimap: { enabled: false },
                                        fontSize: 11,
                                        lineNumbers: 'on',
                                        scrollBeyondLastLine: false,
                                        automaticLayout: true,
                                        readOnly: true
                                    }}
                                    onMount={(editor, monaco) => setupEditor(editor, monaco, "source")}
                                />
                            ) : (
                                <div className="absolute inset-0 flex items-center justify-center text-xs text-gray-500">
                                    Select an entry
                                </div>
                            )}
                        </div>
                    </div>
                )
                }
            </div >
        </div >
    );
}
