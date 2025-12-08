import * as ReactDndHtml5Backend from 'react-dnd-html5-backend'

import * as ReactDnd from 'react-dnd'

import * as FigmaUi from '@xtalk/figma-ui'
import * as Lucide from 'lucide-react'

import React from 'react'


import * as te from '@/client/app/components/editor/theme-editor'

import * as rp from '@/client/app/components/repl/repl-panel'
import * as pp from '@/client/app/components/editor/properties-panel'

import { ViewportCanvas } from '@/client/app/components/canvas/viewport-canvas'
import { GettingStarted } from '@/client/app/components/common/getting-started'
import { ReplPanel } from '@/client/app/components/repl/repl-panel'

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
    importAndEditComponent,
    setTheme // Destructure setTheme
  } = useAppState();

  // Theme Effect
  React.useEffect(() => {
    if (typeof theme === 'string') {
      const root = window.document.documentElement;
      root.classList.remove("light", "dark");
      root.classList.add(theme);
    }
  }, [theme]);

  return (
    <ReactDnd.DndProvider backend={ReactDndHtml5Backend.HTML5Backend}>
      <div className="flex flex-col h-screen bg-background text-foreground">
        <FigmaUi.ResizablePanelGroup direction="horizontal" className="flex-1">
          <FigmaUi.ResizablePanel defaultSize={20} minSize={10} className="bg-muted/30">
            <FigmaUi.ResizablePanelGroup direction="vertical">
              <FigmaUi.ResizablePanel defaultSize={70} minSize={5}>
                <div className="flex-1 flex flex-col h-full">
                  <div className="flex-1 m-0 overflow-hidden">
                    <cb.ComponentBrowser />
                  </div>
                </div>
              </FigmaUi.ResizablePanel>
              <FigmaUi.ResizableHandle className="h-[1px] bg-border hover:bg-primary/50 transition-colors" />
              <FigmaUi.ResizablePanel defaultSize={30} minSize={5}>
                <div className="flex-1 flex flex-col h-full bg-background">
                  <ReplPanel />
                </div>
              </FigmaUi.ResizablePanel>
            </FigmaUi.ResizablePanelGroup>
          </FigmaUi.ResizablePanel>
          <FigmaUi.ResizableHandle className="w-[1px] bg-border hover:bg-primary/50 transition-colors" />
          {/* Work Area Wrapper */}
          <FigmaUi.ResizablePanel defaultSize={80} minSize={30}>
            <FigmaUi.ResizablePanelGroup direction="horizontal">
              {/* Main Content Area */}
              <FigmaUi.ResizablePanel minSize={10} defaultSize={70} className="flex flex-col relative bg-background">
                {selectedNamespace ? (
                  <nv.NamespaceViewer />
                ) : (
                  <GettingStarted />
                )}
              </FigmaUi.ResizablePanel>
              {selectedNamespace && (
                <>
                  <FigmaUi.ResizableHandle className="w-[1px] bg-border hover:bg-primary/50 transition-colors" />
                  <FigmaUi.ResizablePanel defaultSize={30} minSize={20} maxSize={40}>
                    <pp.PropertiesPanel />
                  </FigmaUi.ResizablePanel>
                </>
              )}
            </FigmaUi.ResizablePanelGroup>
          </FigmaUi.ResizablePanel>
        </FigmaUi.ResizablePanelGroup>
      </div>
      <Toaster position="top-right" theme={theme} />
    </ReactDnd.DndProvider >);
}