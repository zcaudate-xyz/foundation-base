import React from 'react'
import Editor from '@monaco-editor/react'
import * as FigmaUi from '@xtalk/figma-ui'
import * as Lucide from 'lucide-react'
import { useAppState } from '../../state'
import { MenuContainer, MenuToolbar, MenuButton } from '../common/common-menu.jsx'
import { useEvents } from '../../events-context.jsx'
import * as Actions from './namespace-actions'

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
        namespaceEntries,
        theme // Added theme
    } = useAppState();

    const [scaffoldLoading, setScaffoldLoading] = React.useState(false);

    // Get events context
    const { subscribe, evalRequest } = useEvents();

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
            const entry = namespaceEntries.find(e => e.var === selectedVar);

            if (entry) {
                let lineInfo;
                if (fileViewMode === "source" && entry.source?.source?.line) {
                    lineInfo = entry.source.source.line;
                } else if (fileViewMode === "test" && entry.test?.test?.line) {
                    lineInfo = entry.test.test.line;
                }

                if (lineInfo) {
                    const position = { lineNumber: lineInfo.row, column: lineInfo.col };
                    fileEditorRef.current.revealPositionInCenter(position);
                    fileEditorRef.current.setPosition(position);
                    fileEditorRef.current.focus();

                    // Flash decoration
                    if (monacoRef.current) {
                        const range = new monacoRef.current.Range(
                            lineInfo.row,
                            lineInfo.col,
                            lineInfo['end-row'],
                            lineInfo['end-col']
                        );
                        const flashDecoration = {
                            range: range,
                            options: {
                                className: 'eval-flash-decoration',
                                isWholeLine: false
                            }
                        };
                        const collection = fileEditorRef.current.createDecorationsCollection([flashDecoration]);
                        setTimeout(() => {
                            collection.clear();
                        }, 500);
                    }
                    return; // Found and scrolled, exit
                }
            }

            // Fallback to Regex if no metadata
            const model = fileEditorRef.current.getModel();
            if (model) {
                const text = model.getValue();
                let regex;

                if (fileViewMode === "source") {
                    // Match (defn...), (defn.pg...), (defmacro...), (def...), etc.
                    regex = new RegExp(`\\(def[\\w\\.-]*\\s+${selectedVar}[\\s\\n]`);
                } else if (fileViewMode === "test") {
                    // escape selectedVar for regex
                    const escapedVar = selectedVar.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
                    const patterns = [
                        `\\(deftest\\s+${escapedVar}(-test)?[\\s\\n]`,         // (deftest var ...)
                        `\\(def\\s+${escapedVar}(-test)?[\\s\\n]`,             // (def var ...)
                        `\\(fact\\s+"${escapedVar}"`,                          // (fact "var" ...)
                        `:refer\\s+([^\\s\\/]+\\/)?${escapedVar}[\\s\\}]`      // :refer ns/var or :refer var
                    ];
                    regex = new RegExp(patterns.join("|"));
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
    }, [selectedVar, fileViewMode, namespaceEntries]);

    // Register Completion Provider (Global)
    React.useEffect(() => {
        return () => {
            if (completionProviderRef.current) {
                completionProviderRef.current.dispose();
            }
        };
    }, []);

    const handlersRef = React.useRef({ handleSave: () => { }, handleEval: () => { } });

    // Actions Wrappers
    const handleEval = () => {
        Actions.evalCode(fileEditorRef.current, monacoRef.current, namespace, fileViewMode, evalRequest);
    };

    const handleEvalLastSexp = () => {
        Actions.evalLastSexp(fileEditorRef.current, monacoRef.current, namespace, fileViewMode, evalRequest);
    };

    const handleEvalFile = () => {
        Actions.evalFile(fileEditorRef.current, monacoRef.current, namespace, fileViewMode, evalRequest);
    };

    const handleScaffold = () => {
        Actions.scaffoldNamespaceTest(namespace, fileViewMode, refreshNamespaceCode, setScaffoldLoading);
    };

    React.useEffect(() => {
        handlersRef.current = {
            handleSave: (editor) => {
                let targetNs = namespace;
                if (fileViewMode === "test") targetNs = targetNs + "-test";
                Actions.saveNamespace(editor, targetNs, setCode);
            },
            handleEval: handleEval,
            handleEvalLastSexp: handleEvalLastSexp,
            handleEvalFile: handleEvalFile
        };
    }, [namespace, fileViewMode, setCode]);

    const setupEditor = (editor, monaco, type) => {
        console.log(`Setting up editor: ${type}`);
        monacoRef.current = monaco;
        Actions.registerCompletion(monaco, completionProviderRef, namespace);

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

        Actions.setupPareditActions(editor, monaco);

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
        const unsubEvalFile = subscribe('editor:eval-file', () => handlersRef.current.handleEvalFile());

        return () => {
            unsubEval();
            unsubEvalLast();
            unsubEvalFile();
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
