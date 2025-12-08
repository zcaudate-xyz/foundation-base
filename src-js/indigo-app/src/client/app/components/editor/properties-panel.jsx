import React from 'react'
import * as FigmaUi from '@xtalk/figma-ui'
import * as Lucide from 'lucide-react'

import { fetchCljVars, fetchTestFacts, runTestVar, runTestNs, fetchNamespaceEntries, runTest } from '../../../api'
import { useAppState } from '../../state'
import { useEvents } from '../../events-context'
import { MenuButton } from '../common/common-menu'
import { toast } from 'sonner'
import { send, addMessageListener } from '../../../repl-client'

export function PropertyInput({ componentId, propertyKey, value, onUpdateProperty }) {
  if (typeof value === "boolean") {
    return (
      <input
        type="checkbox"
        checked={value}
        onChange={(e) => onUpdateProperty(componentId, propertyKey, e.target.checked)}
        className="h-4 w-4"
      />
    );
  }

  if (typeof value === "string" && value.length > 50) {
    return (
      <FigmaUi.Textarea
        value={value}
        onChange={(e) => onUpdateProperty(componentId, propertyKey, e.target.value)}
        className="min-h-[80px] bg-[#1e1e1e] border-[#3a3a3a] text-gray-300 text-xs"
      />
    );
  }

  return (
    <FigmaUi.Input
      type="text"
      value={value || ""}
      onChange={(e) => onUpdateProperty(componentId, propertyKey, e.target.value)}
      className="h-8 bg-[#1e1e1e] border-[#3a3a3a] text-gray-300 text-xs"
    />
  );
}

export function PropertiesPanel() {
  const {
    selectedComponent,
    selectedComponentData,
    updateComponentProperty,
    selectedNamespace,
    selectedVar,
    setSelectedVar,
    namespaceEntries: entries,
    namespaceEntriesLoading: loading,
    runningTest,
    setRunningTest,
    updateComponentInputValues,
    theme,
    setTheme
  } = useAppState();

  const handleRunTest = async (e, entryVar) => {
    e.stopPropagation();
    setRunningTest(entryVar);
    try {
      await runTest(selectedNamespace, entryVar);
      console.log(`Test run initiated for ${entryVar}`);
      // We could add a listener here for the result if we want to show pass/fail immediately
    } catch (err) {
      console.error("Failed to run test", err);
      alert("Failed to run test: " + err.message);
    } finally {
      setRunningTest(null);
    }
  };

  const handleRunAllTests = async () => {
    if (!selectedNamespace) return;
    // Maybe add a global "running all tests" state?
    // For now just log it.
    try {
      await runTestNs(selectedNamespace);
      console.log("Running all tests for", selectedNamespace);
    } catch (err) {
      console.error("Failed to run all tests", err);
    }
  };

  const { emit } = useEvents();
  const [manageLoading, setManageLoading] = React.useState(false);

  const handleManageTask = (task, args) => {
    // args is a string representing a Clojure vector, e.g. "['ns', {:write true}]"
    const code = `(do (require 'code.manage) (apply code.manage/${task} ${args}))`;
    console.log("Running manage task:", code);
    const id = "manage-" + Date.now();

    // We just fire and forget here? Or promise?
    // Use toast promise similar to original implementation
    setManageLoading(true);
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
        send({ op: "eval", id: id, code: code, ns: selectedNamespace });
      }).finally(() => setManageLoading(false)),
      {
        loading: `Running ${task}...`,
        success: (data) => `${task} completed: ${data}`,
        error: (err) => `${task} failed: ${err.message}`
      }
    );
  };

  if (selectedNamespace) {
    return (
      <div className="flex h-full bg-background text-muted-foreground text-xs">
        {/* Main Content (Entries List) */}
        <div className="flex-1 flex flex-col min-w-0">
          <div className="h-8 bg-muted/30 flex items-center px-4 font-bold border-b border-border justify-between shrink-0">
            <span>Entries</span>
          </div>
          <div className="flex-1 overflow-y-auto">
            {loading ? (
              <div className="p-4 text-muted-foreground text-center">Loading entries...</div>
            ) : (
              <div className="flex flex-col">
                {entries.map((entry) => (
                  <div
                    key={entry.var}
                    className={`flex items-center justify-between px-4 py-2 cursor-pointer hover:bg-muted/50 transition-colors ${selectedVar === entry.var ? "bg-muted text-foreground" : ""}`}
                    onClick={() => setSelectedVar(entry.var)}
                  >
                    <div className="flex items-center gap-2 overflow-hidden">
                      <span className={`w-2 h-2 rounded-full shrink-0 ${entry.type === ':fragment' ? 'bg-green-400' :
                        entry.op === 'defn' || entry.type === 'function' ? 'bg-blue-400' :
                          entry.op === 'defmacro' || entry.type === 'macro' ? 'bg-purple-400' :
                            'bg-yellow-400'
                        }`} />
                      <span className="truncate" title={entry.var}>{entry.var}</span>
                    </div>

                    <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setSelectedVar(entry.var);
                          setNamespaceViewType("file");
                          setNamespaceFileViewMode("source");
                        }}
                        className="px-1.5 py-0.5 text-[10px] bg-muted hover:bg-primary/20 text-muted-foreground hover:text-foreground rounded border border-border transition-colors"
                        title="View Source"
                      >
                        src
                      </button>
                      {entry.test && (
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedVar(entry.var);
                            setNamespaceViewType("file");
                            setNamespaceFileViewMode("test");
                          }}
                          className="px-1.5 py-0.5 text-[10px] bg-muted hover:bg-primary/20 text-muted-foreground hover:text-foreground rounded border border-border transition-colors"
                          title="View Test"
                        >
                          test
                        </button>
                      )}
                      {entry.test && (
                        <button
                          onClick={(e) => handleRunTest(e, entry.var)}
                          className="p-1 hover:bg-muted rounded relative transition-colors"
                          title="Run Test"
                        >
                          {runningTest === entry.var ? (
                            <div className="w-2 h-2 rounded-full bg-yellow-500 animate-pulse" />
                          ) : (
                            <Lucide.Play size={10} className="text-muted-foreground hover:text-green-500 transition-colors" />
                          )}
                        </button>
                      )}
                    </div>
                  </div>
                ))}
                {entries.length === 0 && (
                  <div className="p-4 text-muted-foreground text-center">No entries found.</div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Tools Sidebar */}
        <div className="w-[40px] border-l border-border bg-muted/10 flex flex-col items-center gap-2 py-2 shrink-0">
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
      </div>
    );
  }

  // ... (Keep existing component properties logic)
  if (!selectedComponentData) {
    return (
      <div className="p-4 text-muted-foreground text-xs">
        Select a component to view properties.
      </div>
    );
  }

  const properties = selectedComponentData.properties || {};
  const inputs = selectedComponentData.inputs || {};
  const inputValues = selectedComponentData.inputValues || {};

  return (
    <div className="flex flex-col h-full bg-background text-muted-foreground text-xs overflow-y-auto">
      <div className="h-8 bg-muted/30 flex items-center px-4 font-bold border-b border-border shrink-0">
        Properties
      </div>

      <div className="p-4 space-y-6">
        {/* ID (Read-only) */}
        <div className="space-y-2">
          <label className="block text-muted-foreground font-medium">ID</label>
          <div className="px-2 py-1 bg-muted rounded text-foreground font-mono select-all">
            {selectedComponentData.id}
          </div>
        </div>

        {/* Label */}
        <div className="space-y-2">
          <label className="block text-muted-foreground font-medium">Label</label>
          <input
            type="text"
            value={selectedComponentData.label}
            onChange={(e) => updateComponentProperty(selectedComponentData.id, "label", e.target.value)}
            className="w-full bg-muted border border-border rounded px-2 py-1 text-foreground focus:border-primary outline-none"
          />
        </div>

        {/* Component Properties */}
        {Object.keys(properties).length > 0 && (
          <div className="space-y-4">
            <div className="text-muted-foreground font-bold border-b border-border pb-1">Component Props</div>
            {Object.entries(properties).map(([key, value]) => (
              <div key={key} className="space-y-1">
                <label className="block text-muted-foreground">{key}</label>
                <input
                  type="text"
                  value={value}
                  onChange={(e) => updateComponentProperty(selectedComponentData.id, key, e.target.value)}
                  className="w-full bg-muted border border-border rounded px-2 py-1 text-foreground focus:border-primary outline-none font-mono"
                />
              </div>
            ))}
          </div>
        )}

        {/* Inputs Definition (For Components) */}
        {/* Simplified for now, just showing existence */}
        {Object.keys(inputs).length > 0 && (
          <div className="space-y-2">
            <div className="text-muted-foreground font-bold border-b border-border pb-1">Inputs Defined</div>
            <div className="text-muted-foreground italic">
              {Object.keys(inputs).join(", ")}
            </div>
          </div>
        )}

        {/* Input Values (For Instances) */}
        {Object.keys(inputValues).length > 0 && (
          <div className="space-y-4">
            <div className="text-muted-foreground font-bold border-b border-border pb-1">Input Values</div>
            {Object.entries(inputValues).map(([key, value]) => (
              <div key={key} className="space-y-1">
                <label className="block text-muted-foreground">{key}</label>
                <input
                  type="text"
                  value={value}
                  onChange={(e) => {
                    updateComponentInputValues(selectedComponentData.id, { ...inputValues, [key]: e.target.value });
                  }}
                  className="w-full bg-muted border border-border rounded px-2 py-1 text-foreground focus:border-primary outline-none"
                />
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}