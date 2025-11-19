(ns js.lib.puck
  (:require [std.string :as str]
            [std.block :as block]
            [std.lib :as h]
            [std.lang :as l]
            [js.react.compile :as compile]))

(l/script :js
  {:import  [["@measured/puck" :as [* Puck]
              :bundle [["@measured/puck/puck.css"]]]]})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Puck"
                                   :tag "js"}]
  [Action
   ActionBar
   AutoField
   Button
   Drawer
   DropZone
   FieldLabel
   Group
   IconButton
   Label
   Puck
   Render
   createUsePuck
   migrate
   overrideKeys
   registerOverlayPortal
   renderContext
   resolveAllData
   setDeep
   transformProps
   useGetPuck
   usePuck
   walkTree])

(def +components+
  (apply hash-map
         `[:puck/action                  {:tag -/Action}
           :puck/action-bar              {:tag -/ActionBar}
           :puck/auto-field              {:tag -/AutoField}
           :puck/button                  {:tag -/Button}
           :puck/drawer                  {:tag -/Drawer}
           :puck/drop-zone               {:tag -/DropZone}
           :puck/field-label             {:tag -/FieldLabel}
           :puck/group                   {:tag -/Group}
           :puck/icon-button             {:tag -/IconButton}
           :puck/label                   {:tag -/Label}
           :puck/puck                    {:tag -/Puck
                                          :children []}
           :puck/render                  {:tag -/Render}]))

(defn init-components
  []
  (compile/put-registry :puck +components+)
  true)

(def +init+
  (init-components))




;;
;; helpers

(comment

  (defmacro.js Help
  []
  (list `-/Action))
  (l/rt:module-meta :js)
  (!.js
    (return
     @-/Action))

  @-/Help

  (defn.js hello
    []
    (-/Action)
    (return
     -/Action))
  
  (defn.js hello1
    []
    (-/hello)
    Puck
    (return
     -/Action))
  
  (comment
    @hello
    @hello1
    (into {} @-/Action)
    )
  (:form @hello))

(defn generate-blocks
  []
  (block/layout
   (vec (mapcat (fn [[k]]
                  [(keyword "puck" (str/spear-case (str k)))
                   {:tag (symbol "-" (str k))}])
                (sort (ns-publics *ns*))))))
