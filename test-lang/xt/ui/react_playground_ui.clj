(ns xt.ui.react-playground-ui
  "Portable UI + React renderer POC for the js-playground runtime.

   Demonstrates an xt.ui headless controller driving a React view through a
   minimal renderer registry. No substrate backend is required."
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.ui.core :as ui]
             [xt.ui.state.core :as state]
             [js.react :as r]]})

(defn.js render-children
  "renders a UiNode children array to React children"
  [registry children]
  (when (xt/x:nil? children)
    (return nil))
  (return (. children (map (fn [child]
                             (return (-/render-ui-node registry child)))))))

(defn.js render-ui-node
  "recursively turns a UiNode descriptor into a React element"
  [registry node]
  (when (== null node)
    (return null))
  (when (xt/x:is-string? node)
    (return node))
  (when (xt/x:is-number? node)
    (return (String node)))
  (when (xt/x:is-array? node)
    (return (. node (map (fn [child]
                           (return (-/render-ui-node registry child)))))))
  (var component-id (xt/x:get-key node "component"))
  (var props (or (xt/x:get-key node "props") {}))
  (var children (xt/x:get-key node "children"))
  (var renderer (xt/x:get-key registry component-id))
  (when (xt/x:nil? renderer)
    (return null))
  (return (renderer props (-/render-children registry children))))

(defn.js react-registry
  "minimal registry mapping portable xt.ui components to React elements"
  []
  (return
   {"ui/column"
    (fn [props children]
      (return (React.createElement
               "div"
               (xt/x:obj-assign {"style" {"display" "flex"
                                           "flexDirection" "column"
                                           "gap" (or (xt/x:get-key props "gap") "0px")
                                           "padding" (or (xt/x:get-key props "padding") "0px")}}
                                props)
               children)))
    "ui/row"
    (fn [props children]
      (return (React.createElement
               "div"
               (xt/x:obj-assign {"style" {"display" "flex"
                                           "flexDirection" "row"
                                           "gap" (or (xt/x:get-key props "gap") "0px")
                                           "alignItems" (or (xt/x:get-key props "alignItems") "stretch")}}
                                props)
               children)))
    "ui/text"
    (fn [props _children]
      (return (React.createElement
               "span"
               props
               (or (xt/x:get-key props "value") ""))))
    "ui/title"
    (fn [props _children]
      (return (React.createElement
               "h2"
               props
               (or (xt/x:get-key props "value") ""))))
    "ui/input"
    (fn [props _children]
      (return (React.createElement
               "input"
               {"style" {"border" "1px solid #ccc"
                         "borderRadius" "4px"
                         "padding" "8px"
                         "fontSize" "14px"}
                "type" "text"
                "value" (or (xt/x:get-key props "value") "")
                "placeholder" (or (xt/x:get-key props "placeholder") "")
                "disabled" (== true (xt/x:get-key props "disabled"))
                "onChange" (fn [event]
                             (var on-change (xt/x:get-key props "on_change"))
                             (when (xt/x:is-function? on-change)
                               (return (on-change (. event target value))))
                             (return undefined))}
               null)))
    "ui/button"
    (fn [props _children]
      (return (React.createElement
               "button"
               {"style" {"border" "1px solid #111"
                         "background" "#111"
                         "color" "#fff"
                         "borderRadius" "4px"
                         "padding" "8px 12px"
                         "fontSize" "14px"
                         "cursor" "pointer"}
                "disabled" (== true (xt/x:get-key props "disabled"))
                "onClick" (fn []
                            (var on-press (xt/x:get-key props "on_press"))
                            (when (xt/x:is-function? on-press)
                              (return (on-press)))
                            (return undefined))}
               (or (xt/x:get-key props "label") "Button"))))}))

(defn.js make-controller
  "creates a headless controller with mock list state and actions"
  []
  (return
   (state/controller-create
    {"items" ["alpha" "beta"]
     "draft" ""}
    {"set_draft"
     (fn [controller value _deps]
       (return (state/update-state!
                controller
                (fn [s]
                  (return (xt/x:obj-assign s {"draft" value}))))))
     "add_item"
     (fn [controller _payload _deps]
       (return (state/update-state!
                controller
                (fn [s]
                  (var draft (or (xt/x:get-key s "draft") ""))
                  (var text (. draft (trim)))
                  (when (== "" text)
                    (return s))
                  (var items (or (xt/x:get-key s "items") []))
                  (return (xt/x:obj-assign
                           (xt/x:obj-assign s {"draft" ""})
                           {"items" (. items (concat [text]))}))))))
     "remove_item"
     (fn [controller idx _deps]
       (return (state/update-state!
                controller
                (fn [s]
                  (var items (or (xt/x:get-key s "items") []))
                  (return (xt/x:obj-assign
                           s
                           {"items" (. items (filter (fn [item i]
                                                       (return (not= i idx)))))})))))))}
    {}
    {})))

(defn.js view
  "portable view: state + actions -> UiNode tree"
  [state actions]
  (var draft (or (xt/x:get-key state "draft") ""))
  (var items (or (xt/x:get-key state "items") []))
  (return
   (ui/node "ui/column" {"gap" "12px" "padding" "24px"}
            [(ui/node "ui/title" {"value" "xt.ui React Playground"} [])
             (ui/node "ui/input"
                      {"value" draft
                       "placeholder" "New item..."
                       "on_change" (xt/x:get-key actions "set_draft")}
                      [])
             (ui/node "ui/button"
                      {"label" "Add"
                       "on_press" (xt/x:get-key actions "add_item")}
                      [])
             (ui/node "ui/column" {"gap" "8px"}
                      (. items (map (fn [item i]
                                      (return
                                       (ui/node "ui/row"
                                                {"key" i
                                                 "gap" "8px"
                                                 "alignItems" "center"}
                                                [(ui/node "ui/text" {"value" item} [])
                                                 (ui/node "ui/button"
                                                          {"label" "Remove"
                                                           "on_press" (fn []
                                                                        (return ((xt/x:get-key actions "remove_item") i)))}
                                                          [])]))))))])))

(defn.js App
  "React component that owns the controller subscription"
  []
  (var refresh (r/useRefresh))
  (var controllerRef (r/ref (-/make-controller)))
  (r/init []
    (var controller (r/curr controllerRef))
    (state/open! controller)
    (state/subscribe! controller "react"
                      (fn [_state _rev]
                        (return (refresh))))
    (return (fn []
              (state/unsubscribe! controller "react")
              (state/close! controller))))
  (var controller (r/curr controllerRef))
  (var state (state/snapshot controller))
  (var actions (state/actions-create controller ["set_draft" "add_item" "remove_item"]))
  (return (React.createElement
           "div"
           {"style" {"fontFamily" "system-ui, sans-serif"}}
           (-/render-ui-node (-/react-registry) (-/view state actions)))))

(defn.js mount!
  "mounts the App into the playground stage"
  []
  (var el (React.createElement -/App nil))
  (window.PLAYGROUND.setStage el)
  (return true))
