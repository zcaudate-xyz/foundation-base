import React from 'react'
import { useAppState } from '../../state'
import { fetchComponent, emitComponent } from '../../../api'
import Editor from '@monaco-editor/react'
import { MenuContainer, MenuHeader } from '../common/common-menu.jsx'

export function LibraryLiveView() {
    const { selectedNamespace, selectedVar, libraryData } = useAppState();
    const [source, setSource] = React.useState("");
    const [emitted, setEmitted] = React.useState("");
    const [loading, setLoading] = React.useState(false);
    const [error, setError] = React.useState(null);

    const emittedEditorRef = React.useRef(null);

    const [detectedLang, setDetectedLang] = React.useState('clj');

    React.useEffect(() => {
        if (!selectedNamespace || !selectedVar) {
            setSource("");
            setEmitted("");
            return;
        }

        async function loadPreview() {
            setLoading(true);
            setError(null);
            try {
                // Find language
                let lang = 'clj';
                for (const group of libraryData) {
                    if (group.namespaces.some(n => n.fullName === selectedNamespace || n === selectedNamespace)) {
                        lang = group.language;
                        break;
                    }
                }
                setDetectedLang(lang);

                const [src, emit] = await Promise.all([
                    fetchComponent(lang, selectedNamespace, selectedVar),
                    emitComponent(lang, selectedNamespace, selectedVar)
                ]);

                setSource(src.form || src); // Handle object or string response

                // Handle unwrapping for postgres/sql/js if it's a string literal
                let finalEmit = emit;
                const unwrapLangs = ['postgres', 'sql', 'js', 'javascript', 'python', 'solidity', 'lua'];
                if (unwrapLangs.includes(lang) && typeof emit === 'string') {
                    const trimmed = emit.trim();
                    if (trimmed.startsWith('"') && trimmed.endsWith('"')) {
                        try {
                            finalEmit = JSON.parse(trimmed);
                        } catch (e) {
                            // If parse fails, keep original
                            console.warn("Failed to unwrap string", e);
                        }
                    }
                }
                setEmitted(finalEmit);

            } catch (err) {
                console.error("Failed to load preview", err);
                setError(err.message);
            } finally {
                setLoading(false);
            }
        }

        loadPreview();
    }, [selectedNamespace, selectedVar, libraryData]);

    React.useEffect(() => {
        if (emittedEditorRef.current && emitted) {
            // Give Monaco a moment to load the content and workers
            setTimeout(() => {
                emittedEditorRef.current.getAction('editor.action.formatDocument').run();
            }, 200);
        }
    }, [emitted]);

    const getEditorLanguage = (lang) => {
        if (lang === 'postgres' || lang === 'sql') return 'pgsql';
        if (lang === 'js' || lang === 'javascript') return 'javascript';
        if (lang === 'lua') return 'lua';
        if (lang === 'python') return 'python';
        return 'javascript'; // Default
    };

    const getEmittedLabel = (lang) => {
        if (lang === 'postgres' || lang === 'sql') return 'Emitted (SQL)';
        if (lang === 'lua') return 'Emitted (Lua)';
        if (lang === 'python') return 'Emitted (Python)';
        return 'Emitted (JS)';
    };

    const handleEditorWillMount = (monaco) => {
        monaco.editor.defineTheme('emitted-dark', {
            base: 'vs-dark',
            inherit: true,
            rules: [],
            colors: {
                'editor.background': '#000000',
            }
        });
    };

    console.log(emitted);
    return (
        <MenuContainer>
            <div className="h-8 bg-[#252525] border-b border-[#323232] flex items-center px-3 justify-between shrink-0">
                <span className="text-xs font-medium text-gray-300 uppercase tracking-wide">Live View</span>
                <div className="flex items-center gap-2">
                    <span className="text-xs text-gray-500">{selectedNamespace}</span>
                    {selectedVar && <span className="text-xs text-gray-400">/ {selectedVar}</span>}
                </div>
            </div>
            <div className="flex-1 flex flex-col overflow-hidden">
                {!selectedNamespace ? (
                    <div className="flex-1 flex items-center justify-center text-gray-500 text-xs">
                        Select a namespace to preview
                    </div>
                ) : !selectedVar ? (
                    <div className="flex-1 flex items-center justify-center text-gray-500 text-xs">
                        Select a component to preview
                    </div>
                ) : loading ? (
                    <div className="flex-1 flex items-center justify-center text-gray-500 text-xs">
                        Loading preview...
                    </div>
                ) : error ? (
                    <div className="flex-1 flex items-center justify-center text-red-400 text-xs p-4">
                        Error: {error}
                    </div>
                ) : (
                    <div className="flex flex-col h-full">
                        {/* Split View: Source (Top) / Emitted (Bottom) */}
                        <div className="flex-1 relative border-b border-[#323232]">
                            <div className="absolute top-0 left-0 right-0 h-6 bg-[#252526] border-b border-[#323232] flex items-center px-2 text-xs text-gray-400 select-none z-10">
                                Source
                            </div>
                            <div className="absolute top-6 left-0 right-0 bottom-0">
                                <Editor
                                    key={`preview-source-${selectedVar}`}
                                    path={`file:///preview/source/${selectedNamespace.replace(/[^a-zA-Z0-9]/g, '_')}/${selectedVar.replace(/[^a-zA-Z0-9]/g, '_')}.clj`}
                                    height="100%"
                                    language="clojure"
                                    theme="vs-dark"
                                    value={source}
                                    options={{
                                        minimap: { enabled: false },
                                        fontSize: 11,
                                        lineNumbers: 'on',
                                        readOnly: true,
                                        automaticLayout: true
                                    }}
                                />
                            </div>
                        </div>
                        <div className="flex-1 relative">
                            <div className="absolute top-0 left-0 right-0 h-6 bg-[#252526] border-b border-[#323232] flex items-center px-2 text-xs text-gray-400 select-none z-10">
                                {getEmittedLabel(detectedLang)}
                            </div>
                            <div className="absolute top-6 left-0 right-0 bottom-0">
                                <Editor
                                    key={`preview-emit-${selectedVar}`}
                                    path={`file:///preview/emit/${selectedNamespace.replace(/[^a-zA-Z0-9]/g, '_')}/${selectedVar.replace(/[^a-zA-Z0-9]/g, '_')}.${getEditorLanguage(detectedLang) === 'pgsql' ? 'sql' : 'js'}`}
                                    height="100%"
                                    language={getEditorLanguage(detectedLang)}
                                    theme="emitted-dark"
                                    beforeMount={handleEditorWillMount}
                                    value={emitted}
                                    onMount={(editor) => {
                                        emittedEditorRef.current = editor;
                                    }}
                                    options={{
                                        minimap: { enabled: false },
                                        fontSize: 11,
                                        lineNumbers: 'on',
                                        readOnly: true,
                                        automaticLayout: true
                                    }}
                                />
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </MenuContainer>
    );
}
