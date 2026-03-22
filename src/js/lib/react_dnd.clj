(ns js.lib.react-dnd
  (:require [std.lang :as l]
            [std.lib.foundation :as f]))

(l/script :js
  {:import  [["react-dnd" :as [* ReactDnd]]
             ["react-dnd-html5-backend" :as [* ReactDndHtml5Backend]]
             ["react-dnd-touch-backend" :as [* ReactDndTouchBackend]]]})

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactDnd"
                                   :tag "js"}]
  [useDrag
   useDrop
   useDragLayer
   useDragDropManager
   DragSourceMonitor
   DropTargetMonitor
   DragLayerMonitor
   DndProvider
   DragPreviewImage])

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactDndHtml5Backend"
                                   :tag "js"}]
  [HTML5Backend])

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactDndTouchBackend"
                                   :tag "js"}]
  [TouchBackend])
