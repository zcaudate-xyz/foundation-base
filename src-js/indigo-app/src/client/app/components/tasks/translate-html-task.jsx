import React from 'react';
import { translateFromHtml, translateToHtml } from '../../../api';
import { SplitPane } from '../../../ui-common';
import { MonacoEditor } from '../common/monaco-editor';
import { toast } from 'sonner';

function usePersistentState(key, initialValue) {
    const [state, setState] = React.useState(() => {
        try {
            const item = window.localStorage.getItem(key);
            return item ? JSON.parse(item) : initialValue;
        } catch { return initialValue; }
    });
    React.useEffect(() => {
        window.localStorage.setItem(key, JSON.stringify(state));
    }, [key, state]);
    return [state, setState];
}

export function TranslateHtmlTask() {
    const [html, setHtml] = usePersistentState('task-translate-html-source', '');
    const [dsl, setDsl] = usePersistentState('task-translate-dsl-source', '');
    const [loading, setLoading] = React.useState(false);

    const handleFromHtml = async () => {
        setLoading(true);
        try {
            const res = await translateFromHtml(html);
            setDsl(typeof res === 'string' ? res : JSON.stringify(res, null, 2));
        } catch (e) {
            toast.error(e.message);
        } finally {
            setLoading(false);
        }
    };

    const handleToHtml = async () => {
        setLoading(true);
        try {
            const res = await translateToHtml(dsl);
            setHtml(typeof res === 'string' ? res : JSON.stringify(res, null, 2));
        } catch (e) {
            toast.error(e.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex flex-col h-full w-full">
            <div className="flex justify-end items-center p-2 border-b gap-2">
                <div className="flex gap-2">
                    <button className="px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50" onClick={handleFromHtml} disabled={loading}>From HTML</button>
                    <button className="px-3 py-1 bg-green-500 text-white rounded hover:bg-green-600 disabled:opacity-50" onClick={handleToHtml} disabled={loading}>To HTML</button>
                </div>
            </div>
            <SplitPane
                left={
                    <div className="flex flex-col h-full w-full p-1">
                        <div className="p-1 text-xs bg-muted/20 border-b mb-1">HTML</div>
                        <div className="flex-1 overflow-hidden">
                            <MonacoEditor value={html} onChange={setHtml} language="html" className="h-full" />
                        </div>
                    </div>
                }
                right={
                    <div className="flex flex-col h-full w-full p-1">
                        <div className="p-1 text-xs bg-muted/20 border-b mb-1">DSL</div>
                        <div className="flex-1 overflow-hidden">
                            <MonacoEditor value={dsl} onChange={setDsl} language="clojure" className="h-full" />
                        </div>
                    </div>
                }
            />
        </div>
    );
}
