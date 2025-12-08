import React from 'react'
import * as Lucide from 'lucide-react'
import { toast } from 'sonner'

import { useAppState } from '../../state'
import { useEvents } from '../../events-context'
import { MenuButton } from '../common/common-menu'
import { runTestNs } from '../../../api'

export function ToolsSidebar() {
    const {
        activeModal,
        setActiveModal,
        theme,
        setTheme,
        selectedNamespace
    } = useAppState();

    const { emit, evalRequest } = useEvents();
    const [manageLoading, setManageLoading] = React.useState(false);

    const handleRunAllTests = async () => {
        if (!selectedNamespace) return;
        try {
            await runTestNs(selectedNamespace);
            console.log("Running all tests for", selectedNamespace);
        } catch (err) {
            console.error("Failed to run all tests", err);
        }
    };

    const handleManageTask = (task, args) => {
        // args is a string representing a Clojure vector, e.g. "['ns', {:write true}]"
        const code = `(do (require 'code.manage) (apply code.manage/${task} ${args}))`;
        console.log("Running manage task:", code);

        setManageLoading(true);
        toast.promise(
            evalRequest(code, selectedNamespace)
                .finally(() => setManageLoading(false)),
            {
                loading: `Running ${task}...`,
                success: (data) => `${task} completed: ${data}`,
                error: (err) => `${task} failed: ${err.message}`
            }
        );
    };

    return (
        <div className="w-[40px] border-l border-border bg-muted/10 flex flex-col items-center gap-2 py-2 shrink-0">
            {/* New Tasks */}
            <MenuButton title="Heal Code"
                onClick={() => setActiveModal('heal-code')}
                active={activeModal === 'heal-code'}
                icon={Lucide.Bandage}
            />
            <MenuButton title="Translate HTML"
                onClick={() => setActiveModal('translate-html')}
                active={activeModal === 'translate-html'}
                icon={Lucide.Code}
            />
            <div className="h-px bg-border w-6 my-1" />

            <MenuButton title="Eval (Ctrl+E)" onClick={() => emit('editor:eval')} icon={Lucide.Play} />
            <MenuButton title="Eval Last Sexp" onClick={() => emit('editor:eval-last-sexp')} icon={Lucide.Code} />
            <MenuButton title="Eval File" onClick={() => emit('editor:eval-file')} icon={Lucide.FileCode} />

            <div className="h-px bg-border w-6 my-1" />

            <MenuButton
                title="Run All Tests"
                onClick={handleRunAllTests}
                icon={Lucide.FlaskConical}
            />

            <MenuButton
                title="Scaffold Test"
                onClick={() => handleManageTask("scaffold", `['${selectedNamespace}, {:write true}]`)}
                disabled={manageLoading}
                icon={Lucide.Hammer}
            />
            <MenuButton
                title="Import"
                onClick={() => handleManageTask("import", `['${selectedNamespace}, {:write true}]`)}
                icon={Lucide.Import}
            />
            <MenuButton
                title="Purge"
                onClick={() => handleManageTask("purge", `['${selectedNamespace}, {:write true}]`)}
                icon={Lucide.Trash2}
            />
            <MenuButton
                title="Find Incomplete"
                onClick={() => handleManageTask("incomplete", `['${selectedNamespace}]`)}
                icon={Lucide.AlertCircle}
            />

            <div className="flex-1" />

            {/* Theme Toggle */}
            <button
                onClick={() => setTheme(prev => prev === 'dark' ? 'light' : 'dark')}
                className="p-2 rounded hover:bg-muted text-muted-foreground hover:text-foreground transition-colors"
                title={`Switch to ${theme === 'dark' ? 'light' : 'dark'} mode`}
            >
                {theme === 'dark' ? <Lucide.Sun className="w-4 h-4" /> : <Lucide.Moon className="w-4 h-4" />}
            </button>
        </div>
    );
}
