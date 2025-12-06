import * as ReactDndHtml5Backend from 'react-dnd-html5-backend'

import * as ReactDnd from 'react-dnd'

import * as FigmaUi from '@xtalk/figma-ui'

import React from 'react'


import * as te from '@/client/app/components/editor/theme-editor'

import * as pp from '@/client/app/components/editor/properties-panel'

import * as vc from '@/client/app/components/canvas/viewport-canvas'

import * as cb from '@/client/app/components/browser/component-browser'

import * as lb from '@/client/app/components/browser/library-browser'

import * as rp from '@/client/app/components/repl/repl-panel'

import * as nv from '@/client/app/components/browser/namespace-viewer'

import { useAppState } from '@/client/app/state'

// code.dev.client.app/App [140] 
export function App() {
  const {
    components,
    selectedComponent,
    selectedNamespace,
    selectedVar,
    viewMode,
    theme,
    activeTab,
    selectedComponentData,
    setSelectedComponent,
    setSelectedNamespace,
    setSelectedVar,
    setActiveTab,
    addComponent,
    moveComponent,
    updateComponentProperty,
    updateComponentInputs,
    updateComponentInputValues,
    updateComponentStates,
    updateComponentTriggers,
    updateComponentActions,
    deleteComponent,
    importComponent,
    importAndEditComponent
  } = useAppState();

  return (
    <ReactDnd.DndProvider backend={ReactDndHtml5Backend.HTML5Backend}>
      <div className="flex flex-col h-screen bg-[#1e1e1e]">
        <FigmaUi.ResizablePanelGroup direction="horizontal" className="flex-1">
          <FigmaUi.ResizablePanel defaultSize={20} minSize={15} maxSize={30}>
            <div className="flex-1 flex flex-col h-full">
              <div className="bg-[#252525] border-b border-[#323232]">
                <div className="flex w-full h-10">
                  <button
                    onClick={() => setActiveTab("env")}
                    className={`flex-1 text-xs ${activeTab === "env" ? "text-gray-200 bg-[#323232]" : "text-gray-400 hover:text-gray-300"}`}
                  >
                    Env
                  </button>
                  <button
                    onClick={() => setActiveTab("library")}
                    className={`flex-1 text-xs ${activeTab === "library" ? "text-gray-200 bg-[#323232]" : "text-gray-400 hover:text-gray-300"}`}
                  >
                    Library
                  </button>
                </div>
              </div>
              {activeTab === "env" && (
                <div className="flex-1 m-0 overflow-hidden">
                  <cb.ComponentBrowser
                    onAddComponent={addComponent}
                    selectedNamespace={selectedNamespace}
                    onSelectNamespace={setSelectedNamespace}>
                  </cb.ComponentBrowser>
                </div>
              )}
              {activeTab === "library" && (
                <div className="flex-1 m-0 overflow-hidden">
                  <lb.LibraryBrowser />
                </div>
              )}
            </div>
          </FigmaUi.ResizablePanel>
          <FigmaUi.ResizableHandle className="w-[1px] bg-[#323232]"></FigmaUi.ResizableHandle>
          <FigmaUi.ResizablePanel defaultSize={50} minSize={30}>
            <FigmaUi.ResizablePanelGroup direction="vertical">
              <FigmaUi.ResizablePanel defaultSize={70} minSize={40}>
                {selectedNamespace ? (
                  <nv.NamespaceViewer />
                ) : (
                  <vc.ViewportCanvas
                    components={components}
                    selectedComponent={selectedComponent}
                    onSelectComponent={setSelectedComponent}
                    onAddComponent={addComponent}
                    onMoveComponent={moveComponent}
                    viewMode={viewMode}
                    theme={theme}>
                  </vc.ViewportCanvas>
                )}
              </FigmaUi.ResizablePanel>
              <FigmaUi.ResizableHandle className="h-[1px] bg-[#323232]"></FigmaUi.ResizableHandle>
              <FigmaUi.ResizablePanel defaultSize={30} minSize={15}>
                <rp.ReplPanel />
              </FigmaUi.ResizablePanel>
            </FigmaUi.ResizablePanelGroup>
          </FigmaUi.ResizablePanel>
          <FigmaUi.ResizableHandle className="w-[1px] bg-[#323232]"></FigmaUi.ResizableHandle>
          <FigmaUi.ResizablePanel defaultSize={30} minSize={20} maxSize={40}>
            <pp.PropertiesPanel />
          </FigmaUi.ResizablePanel>
        </FigmaUi.ResizablePanelGroup>
      </div>
    </ReactDnd.DndProvider>);
}