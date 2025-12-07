import React from 'react'
import * as FigmaUi from '@xtalk/figma-ui'
import * as Lucide from 'lucide-react'

import { fetchCljVars, fetchTestFacts, runTestVar, runTestNs, fetchNamespaceEntries, runTest } from '../../../api'
import { useAppState } from '../../state'

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
    updateComponentInputValues
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

  if (selectedNamespace) {
    return (
      <div className="flex flex-col h-full bg-[#1e1e1e] text-gray-300 text-xs">
        <div className="h-8 bg-[#252526] flex items-center px-4 font-bold border-b border-[#323232] justify-between shrink-0">
          <span>Entries</span>
          <button
            onClick={handleRunAllTests}
            className="text-[10px] bg-[#323232] hover:bg-[#3e3e3e] px-2 py-0.5 rounded text-gray-300"
          >
            Run All Tests
          </button>
        </div>
        <div className="flex-1 overflow-y-auto">
          {loading ? (
            <div className="p-4 text-gray-500 text-center">Loading entries...</div>
          ) : (
            <div className="flex flex-col">
              {entries.map((entry) => (
                <div
                  key={entry.var}
                  className={`flex items-center justify-between px-4 py-2 cursor-pointer hover:bg-[#2a2d2e] ${selectedVar === entry.var ? "bg-[#37373d] text-white" : ""}`}
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
                      className="px-1.5 py-0.5 text-[10px] bg-[#323232] hover:bg-[#444] text-gray-400 hover:text-gray-200 rounded border border-[#444]"
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
                        className="px-1.5 py-0.5 text-[10px] bg-[#323232] hover:bg-[#444] text-gray-400 hover:text-gray-200 rounded border border-[#444]"
                        title="View Test"
                      >
                        test
                      </button>
                    )}
                    {entry.test && (
                      <button
                        onClick={(e) => handleRunTest(e, entry.var)}
                        className="p-1 hover:bg-[#444] rounded relative"
                        title="Run Test"
                      >
                        {runningTest === entry.var ? (
                          <div className="w-2 h-2 rounded-full bg-yellow-500 animate-pulse" />
                        ) : (
                          <Lucide.Play size={10} className="text-gray-500 hover:text-green-500" />
                        )}
                      </button>
                    )}
                  </div>
                </div>
              ))}
              {entries.length === 0 && (
                <div className="p-4 text-gray-500 text-center">No entries found.</div>
              )}
            </div>
          )}
        </div>
      </div>
    );
  }

  // ... (Keep existing component properties logic)
  if (!selectedComponentData) {
    return (
      <div className="p-4 text-gray-500 text-xs">
        Select a component to view properties.
      </div>
    );
  }

  const properties = selectedComponentData.properties || {};
  const inputs = selectedComponentData.inputs || {};
  const inputValues = selectedComponentData.inputValues || {};

  return (
    <div className="flex flex-col h-full bg-[#1e1e1e] text-gray-300 text-xs overflow-y-auto">
      <div className="h-8 bg-[#252526] flex items-center px-4 font-bold border-b border-[#323232] shrink-0">
        Properties
      </div>

      <div className="p-4 space-y-6">
        {/* ID (Read-only) */}
        <div className="space-y-2">
          <label className="block text-gray-500 font-medium">ID</label>
          <div className="px-2 py-1 bg-[#252526] rounded text-gray-400 font-mono select-all">
            {selectedComponentData.id}
          </div>
        </div>

        {/* Label */}
        <div className="space-y-2">
          <label className="block text-gray-500 font-medium">Label</label>
          <input
            type="text"
            value={selectedComponentData.label}
            onChange={(e) => updateComponentProperty(selectedComponentData.id, "label", e.target.value)}
            className="w-full bg-[#252526] border border-[#323232] rounded px-2 py-1 text-gray-200 focus:border-blue-500 outline-none"
          />
        </div>

        {/* Component Properties */}
        {Object.keys(properties).length > 0 && (
          <div className="space-y-4">
            <div className="text-gray-500 font-bold border-b border-[#323232] pb-1">Component Props</div>
            {Object.entries(properties).map(([key, value]) => (
              <div key={key} className="space-y-1">
                <label className="block text-gray-500">{key}</label>
                <input
                  type="text"
                  value={value}
                  onChange={(e) => updateComponentProperty(selectedComponentData.id, key, e.target.value)}
                  className="w-full bg-[#252526] border border-[#323232] rounded px-2 py-1 text-gray-200 focus:border-blue-500 outline-none font-mono"
                />
              </div>
            ))}
          </div>
        )}

        {/* Inputs Definition (For Components) */}
        {/* Simplified for now, just showing existence */}
        {Object.keys(inputs).length > 0 && (
          <div className="space-y-2">
            <div className="text-gray-500 font-bold border-b border-[#323232] pb-1">Inputs Defined</div>
            <div className="text-gray-500 italic">
              {Object.keys(inputs).join(", ")}
            </div>
          </div>
        )}

        {/* Input Values (For Instances) */}
        {Object.keys(inputValues).length > 0 && (
          <div className="space-y-4">
            <div className="text-gray-500 font-bold border-b border-[#323232] pb-1">Input Values</div>
            {Object.entries(inputValues).map(([key, value]) => (
              <div key={key} className="space-y-1">
                <label className="block text-gray-500">{key}</label>
                <input
                  type="text"
                  value={value}
                  onChange={(e) => {
                    updateComponentInputValues(selectedComponentData.id, { ...inputValues, [key]: e.target.value });
                  }}
                  className="w-full bg-[#252526] border border-[#323232] rounded px-2 py-1 text-gray-200 focus:border-blue-500 outline-none"
                />
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}