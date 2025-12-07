import React from 'react'
import Editor from '@monaco-editor/react'
import { saveNamespaceSource, fetchCompletions, scaffoldTest } from '../../../api'
import { slurpForward, barfForward, getSexpBeforeCursor } from '../../utils/paredit'
import { send, addMessageListener } from '../../../repl-client'
import * as FigmaUi from '@xtalk/figma-ui'
import * as Lucide from 'lucide-react'
import { useAppState } from '../../state'
import { toast } from 'sonner'
import { MenuContainer, MenuToolbar, MenuButton } from '../common/common-menu.jsx'

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
                let regex;

                if (fileViewMode === "source") {
                    regex = new RegExp(`\\(def(n|macro)?\\s+${selectedVar}[\\s\\n]`);
                } else if (fileViewMode === "test") {
                    regex = new RegExp(`\\(deftest\\s+${selectedVar}(-test)?[\\s\\n]`);
                }

                if (regex) {
                    const match = text.match(regex);

                    if (match) {
                        const position = model.getPositionAt(match.index);
                        fileEditorRef.current.revealPositionInCenter(position);
                        fileEditorRef.current.setPosition(position);
                        fileEditorRef.current.focus();

                        // Flash decoration
                        const range = new monaco.Range(position.lineNumber, 1, position.lineNumber + 1, 1);
                        const flashDecoration = {
                            range: range,
                            options: {
                                className: 'eval-flash-decoration',
                                isWholeLine: true
                            }
                        };
                        const collection = fileEditorRef.current.createDecorationsCollection([flashDecoration]);
                        setTimeout(() => {
                            collection.clear();
                        }, 500);
                    }
                }
            }
        }
    }, [selectedVar, viewType, fileViewMode]);

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

    const handleManageTask = (task, args) => {
        if (!namespace) return;
        // args is a string representing a Clojure vector, e.g. "['ns', {:write true}]"
        // We use apply to pass the elements of this vector as arguments to the function.
        const code = `(do (require 'code.manage) (apply code.manage/${task} ${args}))`;
        console.log("Running manage task:", code);
        const id = "manage-" + Date.now();

        toast.promise(
            new Promise((resolve, reject) => {
                const removeListener = addMessageListener((msg) => {
                    if (msg.id === id) {
                        removeListener();
                        if (msg.error) {
                            reject(new Error(msg.error));
                        } else {
                            resolve(msg.result);
                        }
                    }
                });
                send({ op: "eval", id: id, code: code, ns: namespace });
            }).then((res) => {
                // Refresh the code after a successful manage task
                refreshNamespaceCode();
                return res;
            }),
            {
                loading: `Running ${task}...`,
                success: (data) => `${task} completed: ${data}`,
                error: (err) => `${task} failed: ${err.message}`
            }
        );
    };

    const getActiveEditor = () => {
        if (viewType === "file") return fileEditorRef.current;
        if (viewType === "entry") return sourceEditorRef.current;
        return null;
    };

    const handleEval = () => {
        const editor = getActiveEditor();
        if (!editor) return;

        const model = editor.getModel();
        const selection = editor.getSelection();
        let code = "";

        if (selection && !selection.isEmpty()) {
            code = model.getValueInRange(selection);
        } else {
            const position = editor.getPosition();
            const offset = model.getOffsetAt(position);
            const text = model.getValue();
            code = getSexpBeforeCursor(text, offset);
        }

        if (code) {
            const id = "eval-" + Date.now();
            // Reuse the decoration logic from the editor action if possible, 
            // but for now just sending to REPL and showing toast/console.
            // Actually, let's try to trigger the editor action if it exists?
            // Triggering action is cleaner for UI feedback (decorations).
            // But if we want a button, we can just run the logic.

            // Let's replicate the send logic with toast feedback for the button
            toast.promise(
                new Promise((resolve, reject) => {
                    const removeListener = addMessageListener((msg) => {
                        if (msg.id === id) {
                            removeListener();
                            if (msg.error) {
                                reject(new Error(msg.error));
                            } else {
                                resolve(msg.result);
                            }
                        }
                    });
                    send({ op: "eval", id: id, code: code, ns: namespace });
                }),
                {
                    loading: 'Evaluating...',
                    success: (data) => `Result: ${data}`,
                    error: (err) => `Eval failed: ${err.message}`
                }
            );
        }
    };

    const handleEvalFile = () => {
        const editor = getActiveEditor();
        if (!editor) return;

        const code = editor.getValue();
        if (code) {
            const id = "eval-file-" + Date.now();
            toast.promise(
                new Promise((resolve, reject) => {
                    const removeListener = addMessageListener((msg) => {
                        if (msg.id === id) {
                            removeListener();
                            if (msg.error) {
                                reject(new Error(msg.error));
                            } else {
                                resolve(msg.result);
                            }
                        }
                    });
                    send({ op: "eval", id: id, code: code, ns: namespace });
                }),
                {
                    loading: 'Evaluating file...',
                    success: 'File evaluated',
                    error: (err) => `Eval file failed: ${err.message}`
                }
            );
        }
    };

    const handleEvalLastSexp = () => {
        const editor = getActiveEditor();
        if (!editor) return;

        const model = editor.getModel();
        const position = editor.getPosition();
        const offset = model.getOffsetAt(position);
        const text = model.getValue();
        const code = getSexpBeforeCursor(text, offset);

        if (code) {
            const id = "eval-" + Date.now();
            toast.promise(
                new Promise((resolve, reject) => {
                    const removeListener = addMessageListener((msg) => {
                        if (msg.id === id) {
                            removeListener();
                            if (msg.error) {
                                reject(new Error(msg.error));
                            } else {
                                resolve(msg.result);
                            }
                        }
                    });
                    send({ op: "eval", id: id, code: code, ns: namespace });
                }),
                {
                    loading: 'Evaluating last sexp...',
                    success: (data) => `Result: ${data}`,
                    error: (err) => `Eval failed: ${err.message}`
                }
            );
        }
    };

    const handleScaffold = async () => {
        // Legacy handler, now replaced by handleManageTask("scaffold")
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

    // Ref to track current namespace for editor actions
    const namespaceRef = React.useRef(namespace);
    React.useEffect(() => {
        namespaceRef.current = namespace;
    }, [namespace]);

    const setupEditor = (editor, monaco, type) => {
        console.log(`Setting up editor: ${type}`);
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
                let targetNs = namespaceRef.current;
                if (type === "file" && fileViewMode === "test") targetNs = targetNs + "-test";
                // For entry view, we might want to save the whole file? 
                // But we are viewing a snippet. Saving snippet is hard.
                if (type === "file") {
                    handleSave(editor, targetNs);
                } else {
                    toast.info("Saving individual entries is not yet supported.");
                }
            }
        });

        // Add Eval Last Sexp Action (Ctrl+E)
        editor.addAction({
            id: 'eval-last-sexp',
            label: 'Eval Last Sexp',
            keybindings: [
                monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_E,
                monaco.KeyMod.WinCtrl | monaco.KeyCode.KEY_E
            ],
            run: (ed) => {
                console.log("Eval action triggered (Ctrl+E)");
                const model = ed.getModel();
                const selection = ed.getSelection();
                let code = "";
                let isSelection = false;
                let range;

                if (selection && !selection.isEmpty()) {
                    code = model.getValueInRange(selection);
                    isSelection = true;
                    range = selection;
                } else {
                    const position = ed.getPosition();
                    const offset = model.getOffsetAt(position);
                    const text = model.getValue();
                    code = getSexpBeforeCursor(text, offset);
                    // We need to calculate the range for the s-expression to highlight it
                    // This is a bit tricky without a proper parser, but we can approximate or skip for now
                    // For now, let's just highlight the current line or cursor position if it's not a selection
                    range = new monaco.Range(position.lineNumber, position.column, position.lineNumber, position.column);
                }

                console.log("Code to eval:", code);
                if (code) {
                    // Visual Indicator: Flash the code
                    if (isSelection) {
                        const flashDecoration = {
                            range: range,
                            options: {
                                className: 'eval-flash-decoration', // We need to define this CSS class
                                isWholeLine: false
                            }
                        };
                        const collection = ed.createDecorationsCollection([flashDecoration]);
                        setTimeout(() => {
                            collection.clear();
                        }, 300);
                    }

                    console.log("Evaluating:", code);
                    const id = "eval-" + Date.now() + "-" + Math.random();
                    const removeListener = addMessageListener((msg) => {
                        if (msg.id === id) {
                            removeListener();
                            const resultText = msg.error ? ("Error: " + msg.error) : msg.result;
                            const color = msg.error ? "red" : "#888888";

                            decorationsCollection.set([{
                                range: range,
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
                    send({ op: "eval", id: id, code: code, ns: namespaceRef.current });
                }
            }
        });

        // Paredit Actions
        editor.addAction({
            id: 'paredit-slurp-forward',
            label: 'Paredit Slurp Forward',
            keybindings: [
                monaco.KeyMod.CtrlCmd | monaco.KeyCode.RightArrow,
                monaco.KeyMod.Alt | monaco.KeyCode.RightArrow,
                monaco.KeyMod.WinCtrl | monaco.KeyCode.RightArrow
            ],
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
            keybindings: [
                monaco.KeyMod.CtrlCmd | monaco.KeyCode.LeftArrow,
                monaco.KeyMod.Alt | monaco.KeyCode.LeftArrow,
                monaco.KeyMod.WinCtrl | monaco.KeyCode.LeftArrow
            ],
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

        // Manual handling for Dvorak/Layout compatibility
        // We check the produced character key, not the physical code
        editor.onKeyDown((e) => {
            const key = e.browserEvent.key.toLowerCase();
            const isCtrlOrCmd = e.ctrlKey || e.metaKey;
            const isAlt = e.altKey;

            // Ctrl+E (Eval)
            if (isCtrlOrCmd && !isAlt && key === 'e') {
                e.preventDefault();
                e.stopPropagation();
                handleEval();
            }

            // Ctrl+Alt+T (Test Shortcut)
            if (isCtrlOrCmd && isAlt && key === 't') {
                e.preventDefault();
                e.stopPropagation();
                console.log("Test shortcut triggered (manual handler)");
                toast.info("Test Shortcut Triggered (manual handler)");
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

    const handleEditorWillMount = (monaco) => {
        monaco.editor.defineTheme('indigo-dark', {
            base: 'vs-dark',
            inherit: true,
            rules: [],
            colors: {
                'editor.background': '#000000',
            }
        });
    };

    if (!namespace) {
        return (
            <div className="flex flex-col h-full bg-[#1a1a1a] items-center justify-center text-gray-500 text-xs">
                No namespace selected
            </div>
        );
    }

    return (
        <MenuContainer>
            {/* Toolbar */}
            <MenuToolbar className="justify-between px-3 h-8">
                <div className="flex items-center gap-3">
                    {/* View Toggle */}
                    <div className="flex bg-[#1e1e1e] rounded p-0.5 border border-[#323232]">
                        <button
                            onClick={() => setViewType("file")}
                            className={`px - 2 py - 0.5 text - [10px] rounded ${viewType === "file" ? "bg-[#323232] text-gray-200" : "text-gray-500 hover:text-gray-300"} `}
                        >
                            File
                        </button>
                        <button
                            onClick={() => setViewType("entry")}
                            className={`px - 2 py - 0.5 text - [10px] rounded ${viewType === "entry" ? "bg-[#323232] text-gray-200" : "text-gray-500 hover:text-gray-300"} `}
                        >
                            Entry
                        </button>
                    </div>

                    {/* Code Manage Buttons (Icons) */}
                    <div className="flex items-center gap-1">
                        <MenuButton
                            title="Eval (Ctrl+E)"
                            onClick={handleEval}
                            icon={Lucide.Play}
                        />
                        <MenuButton
                            title="Eval Last Sexp"
                            onClick={handleEvalLastSexp}
                            icon={Lucide.Code}
                        />
                        <MenuButton
                            title="Eval File"
                            onClick={handleEvalFile}
                            icon={Lucide.FileCode}
                        />
                    </div>
                    <div className="w-px h-4 bg-gray-700 mx-1" />
                    <div className="flex items-center gap-1">
                        <MenuButton
                            title="Scaffold Test"
                            onClick={() => handleManageTask("scaffold", `['${namespace}, {:write true}]`)}
                            disabled={scaffoldLoading}
                            icon={Lucide.Hammer}
                        />
                        <MenuButton
                            title="Import"
                            onClick={() => handleManageTask("import", `['${namespace}, {:write true}]`)}
                            icon={Lucide.Import}
                        />
                        <MenuButton
                            title="Purge"
                            onClick={() => handleManageTask("purge", `['${namespace}, {:write true}]`)}
                            icon={Lucide.Trash2}
                        />
                        <MenuButton
                            title="Find Incomplete"
                            onClick={() => handleManageTask("incomplete", `['${namespace}]`)}
                            icon={Lucide.AlertCircle}
                        />
                    </div >
                </div >

                {/* Right: Namespace */}
                < span className="text-xs text-gray-400 font-mono" > {namespace}</span >
            </MenuToolbar >

            {/* Content Area */}
            < div className="flex-1 overflow-hidden relative flex" >
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
                                beforeMount={handleEditorWillMount}
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
                                    beforeMount={handleEditorWillMount}
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
        </MenuContainer >
    );
}
