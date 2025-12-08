import React from 'react'
import Editor from '@monaco-editor/react'
import { saveNamespaceSource, fetchCompletions, scaffoldTest } from '../../../api'
import { slurpForward, barfForward, getSexpBeforeCursor, getSexpRangeBeforeCursor } from '../../utils/paredit'
import { send, addMessageListener } from '../../../repl-client'
import * as FigmaUi from '@xtalk/figma-ui'
import * as Lucide from 'lucide-react'
import { useAppState } from '../../state'
import { toast } from 'sonner'
import { MenuContainer, MenuToolbar, MenuButton } from '../common/common-menu.jsx'
import { useEvents } from '../../events-context.jsx'

export function NamespaceViewer() {
    const {
        selectedNamespace: namespace,
        selectedVar,
        namespaceFileViewMode: fileViewMode,
        namespaceCode: code,
        namespaceLoading: loading,
        namespaceError: error,
        setNamespaceFileViewMode: setFileViewMode,
        setNamespaceCode: setCode,
        setNamespaceLoading: setLoading,
        setNamespaceError: setError,
        refreshNamespaceCode,
        editorTabs,
        openEditorTab,
        closeEditorTab
    } = useAppState();

    const [scaffoldLoading, setScaffoldLoading] = React.useState(false);
    const [runningTest, setRunningTest] = React.useState(null);

    // Refs for editors
    const fileEditorRef = React.useRef(null);
    const testEditorRef = React.useRef(null);

    // Global Monaco refs
    const monacoRef = React.useRef(null);
    const completionProviderRef = React.useRef(null);

    // Effect to load content
    React.useEffect(() => {
        if (!namespace) return;
        refreshNamespaceCode();
    }, [namespace, refreshNamespaceCode]);

    // Effect to handle external selection (scroll to var in file view)
    React.useEffect(() => {
        if (selectedVar && fileEditorRef.current) {
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
                        if (monacoRef.current) {
                            const range = new monacoRef.current.Range(position.lineNumber, 1, position.lineNumber + 1, 1);
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
        }
    }, [selectedVar, fileViewMode]);

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

            setCode(currentSource);
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
        return fileEditorRef.current;
    };

    const handleEval = () => {
        const editor = getActiveEditor();
        if (!editor) return;

        const model = editor.getModel();
        const selection = editor.getSelection();
        let code = "";
        let range = null;

        if (selection && !selection.isEmpty()) {
            code = model.getValueInRange(selection);
            range = selection;
        } else {
            const position = editor.getPosition();
            const offset = model.getOffsetAt(position);
            const text = model.getValue();
            const result = getSexpRangeBeforeCursor(text, offset);
            if (result) {
                code = result.text;
                const startPos = model.getPositionAt(result.start);
                const endPos = model.getPositionAt(result.end);
                range = new monacoRef.current.Range(startPos.lineNumber, startPos.column, endPos.lineNumber, endPos.column);
            }
        }

        if (code) {
            // Flash Decoration
            if (monacoRef.current && range) {
                const flashDecoration = {
                    range: range,
                    options: {
                        className: 'eval-flash-decoration',
                        isWholeLine: false // Flash only the specific code
                    }
                };
                const collection = editor.createDecorationsCollection([flashDecoration]);
                setTimeout(() => {
                    collection.clear();
                }, 500);
            }

            const id = "eval-" + Date.now();
            const targetNs = fileViewMode === 'test' ? namespace + '-test' : namespace;

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
                    send({ op: "eval", id: id, code: code, ns: targetNs });
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

        const model = editor.getModel();
        const code = editor.getValue();
        if (code) {
            // Flash whole file
            if (monacoRef.current) {
                const range = model.getFullModelRange();
                const flashDecoration = {
                    range: range,
                    options: {
                        className: 'eval-flash-decoration',
                        isWholeLine: true
                    }
                };
                const collection = editor.createDecorationsCollection([flashDecoration]);
                setTimeout(() => {
                    collection.clear();
                }, 500);
            }

            const id = "eval-file-" + Date.now();
            const targetNs = fileViewMode === 'test' ? namespace + '-test' : namespace;

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
                    send({ op: "eval", id: id, code: code, ns: targetNs });
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

        const result = getSexpRangeBeforeCursor(text, offset);

        if (result) {
            const code = result.text;
            const targetNs = fileViewMode === 'test' ? namespace + '-test' : namespace;

            // Highlight
            if (monacoRef.current) {
                const startPos = model.getPositionAt(result.start);
                const endPos = model.getPositionAt(result.end);
                const range = new monacoRef.current.Range(startPos.lineNumber, startPos.column, endPos.lineNumber, endPos.column);

                const flashDecoration = {
                    range: range,
                    options: {
                        className: 'eval-flash-decoration',
                        isWholeLine: false
                    }
                };
                const collection = editor.createDecorationsCollection([flashDecoration]);
                setTimeout(() => {
                    collection.clear();
                }, 500);
            }

            const id = "eval-last-" + Date.now();
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
                    send({ op: "eval", id: id, code: code, ns: targetNs });
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
            await scaffoldTest(namespace);
            await new Promise(r => setTimeout(r, 1000));
            if (fileViewMode === "test") {
                refreshNamespaceCode();
            }
        } catch (err) {
            console.error("Failed to scaffold test", err);
            toast.error("Scaffold failed: " + err.message);
        } finally {
            setScaffoldLoading(false);
        }
    };

    const { subscribe } = useEvents();

    // Latest handlers ref to avoid stale closures in editor callbacks
    const handlersRef = React.useRef({ handleSave: () => { }, handleEval: () => { } });
    React.useEffect(() => {
        handlersRef.current = {
            handleSave: (editor) => {
                let targetNs = namespace;
                if (fileViewMode === "test") targetNs = targetNs + "-test";
                handleSave(editor, targetNs);
            },
            handleEval: handleEval,
            handleEvalLastSexp: handleEvalLastSexp
        };
    }, [namespace, fileViewMode, handleSave, handleEval, handleEvalLastSexp]);


    const setupEditor = (editor, monaco, type) => {
        console.log(`Setting up editor: ${type}`);
        monacoRef.current = monaco;
        registerCompletion(monaco);

        if (type === "file") fileEditorRef.current = editor;
        if (type === "test") testEditorRef.current = editor;

        const decorationsCollection = editor.createDecorationsCollection();

        // Add Save Action
        editor.addAction({
            id: 'save-namespace',
            label: 'Save Namespace',
            keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_S],
            run: () => handlersRef.current.handleSave(editor)
        });

        // Add Eval Last Sexp Action (Ctrl+E)
        editor.addAction({
            id: 'eval-last-sexp',
            label: 'Eval Last Sexp',
            keybindings: [
                monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_E,
                monaco.KeyMod.WinCtrl | monaco.KeyCode.KEY_E
            ],
            run: () => handlersRef.current.handleEval()
        });

        // Paredit Actions (No changes needed for stateless actions, but keeping consistency)
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

        // Manual handling for Dvorak/Layout compatibility and browser override
        editor.onKeyDown((e) => {
            const key = e.browserEvent.key.toLowerCase();
            const isCtrlOrCmd = e.ctrlKey || e.metaKey;
            const isAlt = e.altKey;

            // Ctrl+S (Save)
            if (isCtrlOrCmd && !isAlt && key === 's') {
                e.preventDefault();
                e.stopPropagation();
                handlersRef.current.handleSave(editor);
            }

            // Ctrl+E (Eval)
            if (isCtrlOrCmd && !isAlt && key === 'e') {
                e.preventDefault();
                e.stopPropagation();
                handlersRef.current.handleEval();
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

    // Global Event Subscriptions
    React.useEffect(() => {
        const unsubEval = subscribe('editor:eval', () => handlersRef.current.handleEval());
        const unsubEvalLast = subscribe('editor:eval-last-sexp', () => handlersRef.current.handleEvalLastSexp());
        // For eval file, we can probably just use the fresh handler directly or via ref if needed
        // but let's be consistent
        // const unsubEvalFile = subscribe('editor:eval-file', handleEvalFile); 
        // handleEvalFile is not packed in handlersRef yet, adding it.
        // Actually for simplicity, just letting handleEvalFile be called directly if not used in shortcuts.
        // But wait, subscribe callback is also a closure?
        // Yes, useEffect runs on mount (or dep change).
        // If dependencies are [subscribe, handleEval], it re-runs when handleEval changes.
        // handleEval changes every render.
        // So global subscriptions are re-subscribing every render?
        // That's acceptable but maybe inefficient.
        // Using handlersRef allows stable subscription.

    }, [subscribe]); // Empty deps if using handlersRef

    // Extended useEffect for subscriptions with stable ref
    React.useEffect(() => {
        const unsubEval = subscribe('editor:eval', () => handlersRef.current.handleEval());
        const unsubEvalLast = subscribe('editor:eval-last-sexp', () => handlersRef.current.handleEvalLastSexp());
        const unsubEvalFile = subscribe('editor:eval-file', handleEvalFile);

        return () => {
            unsubEval();
            unsubEvalLast();
            unsubEvalFile();
        };
    }, [subscribe, handleEvalFile]); // dependent on handleEvalFile only if not in ref

    if (!namespace) {
        return (
            <div className="flex flex-col h-full bg-[#1a1a1a] items-center justify-center text-gray-500 text-xs">
                No namespace selected
            </div>
        );
    }

    return (
        <MenuContainer>
            {/* Tab Bar */}
            <div className="flex items-center bg-[#252526] border-b border-[#323232] overflow-x-auto no-scrollbar">
                {editorTabs.map(tab => (
                    <div
                        key={tab}
                        className={`group flex items-center gap-2 px-3 py-1.5 text-xs cursor-pointer border-r border-[#323232] min-w-[100px] max-w-[200px] ${tab === namespace ? 'bg-[#1e1e1e] text-white border-t-2 border-t-blue-500' : 'text-gray-400 hover:bg-[#2a2d2e]'}`}
                        onClick={() => openEditorTab(tab)}
                    >
                        <Lucide.FileCode size={12} className={tab === namespace ? 'text-blue-400' : 'text-gray-500'} />
                        <span className="truncate flex-1">{tab}</span>
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                closeEditorTab(tab);
                            }}
                            className={`p-0.5 rounded hover:bg-[#323232] ${tab === namespace ? 'opacity-100' : 'opacity-0 group-hover:opacity-100'}`}
                        >
                            <Lucide.X size={10} />
                        </button>
                    </div>
                ))}

                {/* View Mode Toggles (In Tab Bar) */}
                <div className="flex-1" /> {/* Spacer */}
                <div className="flex items-center gap-1 px-2 border-l border-[#323232]">
                    <button
                        onClick={() => setFileViewMode("source")}
                        className={`px-2 py-0.5 text-[10px] rounded ${fileViewMode === "source" ? "bg-[#37373d] text-white" : "text-gray-400 hover:text-gray-200 hover:bg-[#323232]"}`}
                    >
                        Source
                    </button>
                    <button
                        onClick={() => setFileViewMode("test")}
                        className={`px-2 py-0.5 text-[10px] rounded ${fileViewMode === "test" ? "bg-[#37373d] text-white" : "text-gray-400 hover:text-gray-200 hover:bg-[#323232]"}`}
                    >
                        Test
                    </button>
                    <button
                        onClick={() => setFileViewMode("doc")}
                        className={`px-2 py-0.5 text-[10px] rounded ${fileViewMode === "doc" ? "bg-[#37373d] text-white" : "text-gray-400 hover:text-gray-200 hover:bg-[#323232]"}`}
                    >
                        Doc
                    </button>
                </div>
            </div>

            {/* Content Area */}
            <div className="flex-1 overflow-hidden relative flex">
                <div className="flex-1 relative">
                    {/* Floating File View Mode Toggles - REMOVED */}

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
            </div>
        </MenuContainer>
    );
}
