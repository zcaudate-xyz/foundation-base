import * as ReactDndHtml5Backend from 'react-dnd-html5-backend'

import * as ReactDnd from 'react-dnd'

import * as FigmaUi from '@xtalk/figma-ui'

import React from 'react'


import * as te from '@/client/app/components/editor/theme-editor'

import * as rp from '@/client/app/components/repl/repl-panel'
import * as pp from '@/client/app/components/editor/properties-panel'

import * as vc from '@/client/app/components/canvas/viewport-canvas'

import * as cb from '@/client/app/components/browser/component-browser'

import * as lb from '@/client/app/components/browser/library-browser'
import * as llv from '@/client/app/components/browser/library-live-view'

import * as bv from '@/client/app/components/browser/book-view'

// ... existing imports ...



import * as nv from '@/client/app/components/browser/namespace-viewer'

import { Toaster } from 'sonner'
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
          <FigmaUi.ResizablePanel defaultSize={20} minSize={15}>
            <FigmaUi.ResizablePanelGroup direction="vertical">
              <FigmaUi.ResizablePanel defaultSize={70} minSize={30}>
                <div className="flex-1 flex flex-col h-full">
                  <div className="flex-1 m-0 overflow-hidden">
                    <cb.ComponentBrowser
                      onAddComponent={addComponent}
                      selectedNamespace={selectedNamespace}
                      onSelectNamespace={setSelectedNamespace}>
                    </cb.ComponentBrowser>
                  </div>
                </div>
              </FigmaUi.ResizablePanel>
              <FigmaUi.ResizableHandle className="h-1 bg-[#252526] hover:bg-blue-500 transition-colors"></FigmaUi.ResizableHandle>
              <FigmaUi.ResizablePanel defaultSize={30} minSize={20}>
                <rp.ReplPanel />
              </FigmaUi.ResizablePanel>
            </FigmaUi.ResizablePanelGroup>
          </FigmaUi.ResizablePanel>
          <FigmaUi.ResizableHandle className="w-1 bg-[#252526] hover:bg-blue-500 transition-colors"></FigmaUi.ResizableHandle>
          <FigmaUi.ResizablePanel defaultSize={50} minSize={30}>
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
          <FigmaUi.ResizableHandle className="w-1 bg-[#252526] hover:bg-blue-500 transition-colors"></FigmaUi.ResizableHandle>
          <FigmaUi.ResizablePanel defaultSize={30} minSize={20} maxSize={40}>
            <pp.PropertiesPanel />
          </FigmaUi.ResizablePanel>
        </FigmaUi.ResizablePanelGroup>
      </div>
      <Toaster position="top-right" theme="dark" />
    </ReactDnd.DndProvider >);
}