(ns js.lib.react-dnd
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:import  [["react-dnd" :as [* ReactDnd]]
             ["react-dnd-html5-backend" :as [* ReactDndHtml5Backend]]
             ["react-dnd-touch-backend" :as [* ReactDndTouchBackend]]]})

(h/template-entries [l/tmpl-entry {:type :fragment
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

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactDndHtml5Backend"
                                   :tag "js"}]
  [HTML5Backend])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactDndTouchBackend"
                                   :tag "js"}]
  [TouchBackend])
