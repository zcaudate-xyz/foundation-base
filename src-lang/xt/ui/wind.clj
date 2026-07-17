(ns xt.ui.wind
  "Transforms portable UI nodes into fluttersdk_wind WDynamic bundles."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.ui.core :as ui]
             [xt.ui.widgets.core :as catalog]]})

(defn.xt flutter-registry
  "platform renderer ids consumed by Wind's safe WDynamic whitelist"
  []
  (var platform (ui/registry-create "xt.ui/flutter-wind"))
  (xt/for:array [entry [["ui/fragment" "WDiv"]
                        ["ui/row" "WDiv"]
                        ["ui/column" "WDiv"]
                        ["ui/text" "WText"]
                        ["ui/title" "WText"]
                        ["ui/description" "WText"]
                        ["ui/label" "WText"]
                        ["ui/icon" "WIcon"]
                        ["ui/image" "WImage"]
                        ["ui/card" "WDiv"]
                        ["ui/card-header" "WDiv"]
                        ["ui/card-content" "WDiv"]
                        ["ui/input" "WInput"]
                        ["ui/textarea" "WInput"]
                        ["ui/button" "WButton"]
                        ["ui/alert" "WDiv"]
                        ["ui/spinner" "WDiv"]]
                        ["ui/table" "WDiv"]
                        ["ui/table-header" "WDiv"]
                        ["ui/table-body" "WDiv"]
                        ["ui/table-row" "WDiv"]
                        ["ui/table-cell" "WText"]]
    (ui/registry-register-renderer platform (xt/x:get-key entry 0) (xt/x:get-key entry 1)))
  (return (ui/registry-compose [(catalog/registry) platform])))

(defn.xt action-add!
  [state event-id callback value-event]
  (var action-id (xt/x:cat "xt_ui_" (xt/x:get-key state "next")))
  (xt/x:set-key state "next" (+ 1 (xt/x:get-key state "next")))
  (xt/x:set-key
   (xt/x:get-key state "actions") action-id
   (fn [args]
     (return (:? value-event
                 (callback (xt/x:get-key args "_value"))
                 (callback args)))))
  (return {event-id {"action" action-id}}))

(defn.xt normalize-props
  [component-id props state]
  (var out {})
  (xt/for:object [[key value] (or props {})]
    (cond (== key "class") (xt/x:set-key out "className" value)
          (== key "aria_label") (xt/x:set-key out "semanticLabel" value)
          (== key "read_only") (xt/x:set-key out "readOnly" value)
          (== key "pending") (xt/x:set-key out "isLoading" value)
          (== key "on_press")
          (xt/x:obj-assign out (-/action-add! state "onTap" value false))
          (== key "on_change")
          (xt/x:obj-assign out (-/action-add! state "onChange" value true))
          (or (== key "hidden")
              (== key "on_submit")
              (== key "variant")
              (== key "size")
              (== key "tone")) nil
          :else (xt/x:set-key out key value)))
  (when (== component-id "ui/textarea")
    (xt/x:set-key out "maxLines" (or (xt/x:get-key props "rows") 4))
    (xt/x:del-key out "rows"))
  (when (== component-id "ui/row")
    (xt/x:set-key out "className"
                  (xt/x:cat "flex flex-row " (or (xt/x:get-key out "className") ""))))
  (when (or (== component-id "ui/column")
            (== component-id "ui/fragment"))
    (xt/x:set-key out "className"
                  (xt/x:cat "flex flex-col " (or (xt/x:get-key out "className") ""))))
  (return out))

(defn.xt prepare-node
  [runtime value state]
  (when (xt/x:nil? value)
    (return nil))
  (when (or (xt/x:is-string? value) (xt/x:is-number? value))
    (return {"type" "WText" "props" {"text" (xt/x:to-string value)}}))
  (when (xt/x:is-array? value)
    (return (xtd/arr-map value
                         (fn [child]
                           (return (-/prepare-node runtime child state))))))
  (var component-id (xt/x:get-key value "component"))
  (when (== component-id "ui/slot")
    (return (-/prepare-node runtime (ui/resolve-slot runtime value) state)))
  (var props (xt/x:get-key value "props"))
  (when (== true (xt/x:get-key props "hidden"))
    (return nil))
  (var renderer (ui/registry-renderer (xt/x:get-key runtime "registry") component-id))
  (when (not renderer)
    (return {"type" "WDiv"
             "props" {"className" "flex flex-col"}
             "children" (-/prepare-node runtime (xt/x:get-key value "children") state)}))
  (var out-props (-/normalize-props component-id props state))
  (when (or (== renderer "WText") (== component-id "ui/label"))
    (xt/x:set-key out-props "text" (or (xt/x:get-key props "value") ""))
    (xt/x:del-key out-props "value")
    (xt/x:del-key out-props "for"))
  (return {"type" renderer
           "props" out-props
           "children" (-/prepare-node runtime (xt/x:get-key value "children") state)}))

(defn.xt prepare
  "returns the json/actions pair passed directly to Wind WDynamic"
  [runtime value]
  (var state {"next" 0 "actions" {}})
  (var json (-/prepare-node runtime value state))
  (return {"json" json "actions" (xt/x:get-key state "actions")}))
