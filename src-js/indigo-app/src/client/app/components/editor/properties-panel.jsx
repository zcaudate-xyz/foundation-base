import React from 'react'
import * as FigmaUi from '@xtalk/figma-ui'
import * as Lucide from 'lucide-react'

import { fetchCljVars, fetchTestFacts, runTestVar, runTestNs } from '../../../api'

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

export function PropertiesPanel({
  component,
  selectedNamespace,
  onSelectVar,
  onUpdateProperty,
  onDeleteComponent,
  onUpdateInputs,
  onUpdateInputValues,
  onUpdateStates,
  onUpdateTriggers,
  onUpdateActions
}) {
  const [vars, setVars] = React.useState([]);
  const [tests, setTests] = React.useState([]);
  const [loadingVars, setLoadingVars] = React.useState(false);
  const [loadingTests, setLoadingTests] = React.useState(false);

  React.useEffect(() => {
    if (selectedNamespace) {
      setLoadingVars(true);
      setLoadingTests(true);

      fetchCljVars(selectedNamespace)
        .then(data => {
          setVars(data);
          setLoadingVars(false);
        })
        .catch(err => {
          console.error("Failed to fetch vars", err);
          setLoadingVars(false);
        });

      fetchTestFacts(selectedNamespace + "-test")
        .then(data => {
          setTests(data);
          setLoadingTests(false);
        })
        .catch(err => {
          console.log("No tests found or failed to fetch tests", err);
          setTests([]);
          setLoadingTests(false);
        });
    } else {
      setVars([]);
      setTests([]);
    }
  }, [selectedNamespace]);

  if (selectedNamespace) {
    return (
      <div className="flex flex-col h-full bg-[#252525]">
        {/* Vars Section */}
        <div className="flex-1 flex flex-col min-h-0 border-b border-[#323232]">
          <div className="h-10 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-3">
            <span className="text-xs text-gray-400 font-medium uppercase">Vars</span>
          </div>
          <div className="flex-1 overflow-y-auto p-2">
            {loadingVars ? (
              <div className="text-xs text-gray-500">Loading vars...</div>
            ) : (
              <div className="space-y-1">
                {vars.map(v => (
                  <div
                    key={v}
                    className="text-xs text-gray-400 hover:text-blue-400 cursor-pointer truncate px-2 py-1 hover:bg-[#323232] rounded"
                    onClick={() => onSelectVar(v)}
                    title={v}
                  >
                    {v}
                  </div>
                ))}
                {vars.length === 0 && !loadingVars && (
                  <div className="text-xs text-gray-600 italic">No public vars</div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Tests Section */}
        <div className="flex-1 flex flex-col min-h-0">
          <div className="h-10 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-3 justify-between">
            <span className="text-xs text-gray-400 font-medium uppercase">Tests</span>
            <button
              className="p-1 hover:bg-[#3a3a3a] rounded text-gray-400 hover:text-white"
              onClick={() => runTestNs(selectedNamespace)}
              title="Run All Tests"
            >
              <Lucide.Play size={12} />
            </button>
          </div>
          <div className="flex-1 overflow-y-auto p-2">
            {loadingTests ? (
              <div className="text-xs text-gray-500">Loading tests...</div>
            ) : (
              <div className="space-y-1">
                {tests.map(t => (
                  <div
                    key={t}
                    className="flex items-center justify-between group px-2 py-1 hover:bg-[#323232] rounded"
                  >
                    <div
                      className="text-xs text-gray-400 hover:text-green-400 cursor-pointer truncate flex-1"
                      title={t}
                    >
                      {t}
                    </div>
                    <button
                      className="opacity-0 group-hover:opacity-100 p-1 hover:bg-[#3a3a3a] rounded text-gray-400 hover:text-white"
                      onClick={(e) => {
                        e.stopPropagation();
                        runTestVar(selectedNamespace, t);
                      }}
                      title="Run Test"
                    >
                      <Lucide.Play size={10} />
                    </button>
                  </div>
                ))}
                {tests.length === 0 && !loadingTests && (
                  <div className="text-xs text-gray-600 italic">No tests found</div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    );
  }

  if (!component) {
    return (
      <div className="flex flex-col h-full bg-[#252525]">
        <div className="h-10 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-3">
          <h2 className="text-xs text-gray-400 uppercase tracking-wide">Properties</h2>
        </div>
        <div className="flex-1 flex items-center justify-center">
          <p className="text-xs text-gray-600">No component selected</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full bg-[#252525]">
      <div className="h-10 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-3 justify-between">
        <span className="text-xs text-gray-400">Properties</span>
      </div>

      <div className="flex-1 m-0 overflow-hidden flex items-center justify-center">
        <p className="text-xs text-gray-600">Select a component to view properties</p>
      </div>
    </div>
  );
}