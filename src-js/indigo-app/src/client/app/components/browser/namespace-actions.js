import { saveNamespaceSource, fetchCompletions, scaffoldTest } from '../../../api'
import { slurpForward, barfForward, getSexpRangeBeforeCursor } from '../../utils/paredit'
import { send, addMessageListener } from '../../../repl-client'
import { toast } from 'sonner'

export const registerCompletion = (monaco, completionProviderRef, namespace) => {
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

export const saveNamespace = async (editorInstance, targetNs, setCode) => {
    if (!editorInstance) return;

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

const flashDecoration = (editor, monaco, range, isWholeLine) => {
    if (!editor || !monaco || !range) return;

    const decoration = {
        range: range,
        options: {
            className: 'eval-flash-decoration',
            isWholeLine: isWholeLine
        }
    };
    const collection = editor.createDecorationsCollection([decoration]);
    setTimeout(() => {
        collection.clear();
    }, 500);
};

export const evalCode = (editor, monaco, namespace, fileViewMode) => {
    if (!editor) return;

    const model = editor.getModel();
    const selection = editor.getSelection();
    let code = "";
    let range = null;
    let isWholeLine = false;

    if (selection && !selection.isEmpty()) {
        code = model.getValueInRange(selection);
        range = selection;
        isWholeLine = false;
    } else {
        const position = editor.getPosition();
        const offset = model.getOffsetAt(position);
        const text = model.getValue();
        const result = getSexpRangeBeforeCursor(text, offset);
        if (result) {
            code = result.text;
            const startPos = model.getPositionAt(result.start);
            const endPos = model.getPositionAt(result.end);
            range = new monaco.Range(startPos.lineNumber, startPos.column, endPos.lineNumber, endPos.column);
            // Although original code set isWholeLine: true for some reason in one branch? 
            // Ah, for "eval file" or something else maybe? 
            // Original eval logic for getting sexp used isWholeLine: false.
            // Wait, I see lines 72-85 in original code had isWholeLine: true. That was for *external selection* highlight.
            // The eval handler (lines 175-183) has isWholeLine: false.
            isWholeLine = false;
        }
    }

    if (code) {
        flashDecoration(editor, monaco, range, isWholeLine);

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

export const evalLastSexp = (editor, monaco, namespace, fileViewMode) => {
    if (!editor) return;

    const model = editor.getModel();
    const position = editor.getPosition();
    const offset = model.getOffsetAt(position);
    const text = model.getValue();

    const result = getSexpRangeBeforeCursor(text, offset);

    if (result) {
        const code = result.text;
        const targetNs = fileViewMode === 'test' ? namespace + '-test' : namespace;

        if (monaco) {
            const startPos = model.getPositionAt(result.start);
            const endPos = model.getPositionAt(result.end);
            const range = new monaco.Range(startPos.lineNumber, startPos.column, endPos.lineNumber, endPos.column);

            flashDecoration(editor, monaco, range, false);
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

export const scaffoldNamespaceTest = async (namespace, fileViewMode, refreshNamespaceCode, setScaffoldLoading) => {
    setScaffoldLoading(true);
    try {
        await scaffoldTest(namespace);
        await new Promise(r => setTimeout(r, 1000));
        await scaffoldTest(namespace); // Run twice as per original code (maybe to ensure stability/format?)
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

export const setupPareditActions = (editor, monaco) => {
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
};
