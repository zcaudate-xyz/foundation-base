import React from 'react';
import { translateToHeal } from '../../../api';
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

export function HealCodeTask() {
    const [source, setSource] = usePersistentState('task-heal-source', '');
    const [result, setResult] = React.useState('');
    const [loading, setLoading] = React.useState(false);

    const handleHeal = async () => {
        setLoading(true);
        try {
            const res = await translateToHeal(source);
            setResult(typeof res === 'string' ? res : JSON.stringify(res, null, 2));
        } catch (e) {
            console.error(e);
            toast.error(e.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex flex-col h-full w-full">
            <div className="flex justify-end items-center p-2 border-b">
                <button
                    className="px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50"
                    onClick={handleHeal}
                    disabled={loading}
                >
                    {loading ? 'Healing...' : 'Heal'}
                </button>
            </div>
            <div className="flex-1 overflow-hidden">
                <SplitPane
                    left={
                        <MonacoEditor
                            value={source}
                            onChange={setSource}
                            language="clojure"
                            className="h-full w-full"
                        />
                    }
                    right={
                        <MonacoEditor
                            value={result}
                            onChange={() => { }}
                            language="clojure"
                            className="h-full w-full"
                            readOnly
                        />
                    }
                />
            </div>
        </div>
    );
}
