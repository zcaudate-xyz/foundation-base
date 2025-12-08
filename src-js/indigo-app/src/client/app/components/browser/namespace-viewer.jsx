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
        closeEditorTab,
        theme // Added theme
    } = useAppState();

    const [scaffoldLoading, setScaffoldLoading] = React.useState(false);

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

    const handlersRef = React.useRef({ handleSave: () => { }, handleEval: () => { } });

    const handleEval = () => {
        const editor = fileEditorRef.current;
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
            if (monacoRef.current && range) {
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

    const handleEvalLastSexp = () => {
        const editor = fileEditorRef.current;
        if (!editor) return;

        const model = editor.getModel();
        const position = editor.getPosition();
        const offset = model.getOffsetAt(position);
        const text = model.getValue();

        const result = getSexpRangeBeforeCursor(text, offset);

        if (result) {
            const code = result.text;
            const targetNs = fileViewMode === 'test' ? namespace + '-test' : namespace;

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
    }, [namespace, fileViewMode]); // Cleaned up deps

    const setupEditor = (editor, monaco, type) => {
        console.log(`Setting up editor: ${type}`);
        monacoRef.current = monaco;
        registerCompletion(monaco);

        if (type === "file") fileEditorRef.current = editor;
        if (type === "test") testEditorRef.current = editor;

        const decorationsCollection = editor.createDecorationsCollection();

        editor.addAction({
            id: 'save-namespace',
            label: 'Save Namespace',
            keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_S],
            run: () => handlersRef.current.handleSave(editor)
        });

        editor.addAction({
            id: 'eval-last-sexp',
            label: 'Eval Last Sexp',
            keybindings: [
                monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_E,
                monaco.KeyMod.WinCtrl | monaco.KeyCode.KEY_E
            ],
            run: () => handlersRef.current.handleEval()
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

        editor.onKeyDown((e) => {
            const key = e.browserEvent.key.toLowerCase();
            const isCtrlOrCmd = e.ctrlKey || e.metaKey;
            const isAlt = e.altKey;

            if (isCtrlOrCmd && !isAlt && key === 's') {
                e.preventDefault();
                e.stopPropagation();
                handlersRef.current.handleSave(editor);
            }

            if (isCtrlOrCmd && !isAlt && key === 'e') {
                e.preventDefault();
                e.stopPropagation();
                handlersRef.current.handleEval();
            }
        });
    };

    const handleEditorWillMount = (monaco) => {
        // Define both themes if needed, or rely on built-ins
        monaco.editor.defineTheme('indigo-dark', {
            base: 'vs-dark',
            inherit: true,
            rules: [],
            colors: {
                'editor.background': '#00000000', // Transparent to let background show through? Or specific color
            }
        });
    };

    React.useEffect(() => {
        const unsubEval = subscribe('editor:eval', () => handlersRef.current.handleEval());
        const unsubEvalLast = subscribe('editor:eval-last-sexp', () => handlersRef.current.handleEvalLastSexp());

        return () => {
            unsubEval();
            unsubEvalLast();
        };
    }, [subscribe]);

    if (!namespace) {
        return (
            <div className="flex flex-col h-full bg-background items-center justify-center text-muted-foreground text-xs">
                No namespace selected
            </div>
        );
    }

    return (
        <MenuContainer>
            {/* Tab Bar */}
            <div className="flex items-center bg-muted/30 border-b border-border overflow-x-auto no-scrollbar h-8 shrink-0">
                {editorTabs.map(tab => (
                    <div
                        key={tab}
                        className={`group flex items-center gap-2 px-3 text-xs cursor-pointer border-r border-border min-w-[100px] max-w-[200px] h-full ${tab === namespace ? 'bg-background text-foreground border-t-[1px] border-t-primary' : 'text-muted-foreground hover:bg-muted/50 border-t-[1px] border-t-transparent'}`}
                        onClick={() => openEditorTab(tab)}
                    >
                        <Lucide.FileCode size={12} className={tab === namespace ? 'text-primary' : 'text-muted-foreground'} />
                        <span className="truncate flex-1">{tab}</span>
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                closeEditorTab(tab);
                            }}
                            className={`p-0.5 rounded hover:bg-muted ${tab === namespace ? 'opacity-100' : 'opacity-0 group-hover:opacity-100'}`}
                        >
                            <Lucide.X size={10} />
                        </button>
                    </div>
                ))}

                {/* View Mode Toggles (In Tab Bar) */}
                <div className="flex-1" /> {/* Spacer */}
                <div className="flex items-center gap-1 px-2 border-l border-border h-full">
                    {['source', 'test', 'doc'].map(mode => (
                        <button
                            key={mode}
                            onClick={() => setFileViewMode(mode)}
                            className={`px-2 py-0.5 text-[10px] rounded capitalize transition-colors ${fileViewMode === mode ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:bg-muted hover:text-foreground"}`}
                        >
                            {mode}
                        </button>
                    ))}
                </div>
            </div>

            {/* Content Area */}
            <div className="flex-1 overflow-hidden relative flex bg-background">
                <div className="flex-1 relative">
                    {loading ? (
                        <div className="absolute inset-0 flex items-center justify-center text-xs text-muted-foreground">Loading...</div>
                    ) : error ? (
                        <div className="absolute inset-0 flex flex-col items-center justify-center gap-4">
                            <div className="text-xs text-red-500">Error: {error}</div>
                            {fileViewMode === "test" && error === "Test file not found" && (
                                <button
                                    onClick={handleScaffold}
                                    disabled={scaffoldLoading}
                                    className="px-4 py-2 bg-primary hover:bg-primary/90 text-primary-foreground text-xs rounded shadow-sm transition-colors disabled:opacity-50"
                                >
                                    {scaffoldLoading ? "Scaffolding..." : "Scaffold Test"}
                                </button>
                            )}
                        </div>
                    ) : (
                        <Editor
                            height="100%"
                            language="clojure"
                            theme={theme === 'dark' ? 'indigo-dark' : 'light'}
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
                                readOnly: fileViewMode === "doc",
                                'semanticHighlighting.enabled': true
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
