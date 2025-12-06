import React from 'react'
import * as FigmaUi from '@xtalk/figma-ui'
import * as Lucide from 'lucide-react'
import { fetchVarTests } from '../../api'

export function VarsPanel({ component }) {
    const [tests, setTests] = React.useState([]);
    const [loading, setLoading] = React.useState(false);
    const [error, setError] = React.useState(null);

    React.useEffect(() => {
        if (component && component.libraryRef) {
            setLoading(true);
            setError(null);
            fetchVarTests(component.libraryRef)
                .then(data => {
                    setTests(data);
                    setLoading(false);
                })
                .catch(err => {
                    console.error("Failed to fetch var tests", err);
                    setError(err.message);
                    setLoading(false);
                });
        }
    }, [component]);

    if (!component) {
        return <div className="p-4 text-xs text-gray-500">No component selected</div>;
    }

    if (!component.libraryRef) {
        return <div className="p-4 text-xs text-gray-500">Selected component is not linked to a library var.</div>;
    }

    if (loading) {
        return <div className="p-4 text-xs text-gray-500">Loading tests...</div>;
    }

    if (error) {
        return <div className="p-4 text-xs text-red-500">Error: {error}</div>;
    }

    return (
        <FigmaUi.ScrollArea className="h-full">
            <div className="p-4 space-y-4">
                <div>
                    <FigmaUi.Label className="text-xs text-gray-500 uppercase tracking-wider">Linked Var</FigmaUi.Label>
                    <div className="mt-1 text-sm text-blue-400 font-mono">{component.libraryRef}</div>
                </div>

                <div className="h-[1px] bg-[#323232]" />

                <div>
                    <FigmaUi.Label className="text-xs text-gray-500 uppercase tracking-wider mb-3 block">Tests</FigmaUi.Label>
                    {tests.length === 0 ? (
                        <div className="text-xs text-gray-500 italic">No tests found for this var.</div>
                    ) : (
                        <div className="space-y-2">
                            {tests.map((test, i) => (
                                <div key={i} className="bg-[#1e1e1e] border border-[#3a3a3a] rounded p-2">
                                    <div className="flex items-center justify-between mb-1">
                                        <span className="text-xs font-medium text-gray-300">{test.name || "Test"}</span>
                                        <Lucide.CheckCircle2 className="w-3 h-3 text-green-500" />
                                    </div>
                                    <pre className="text-[10px] text-gray-400 font-mono overflow-x-auto">
                                        {test.code || ""}
                                    </pre>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </FigmaUi.ScrollArea>
    );
}
