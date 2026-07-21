(ns xt.substrate.view-demos
  "Kitchen-sink demo for the xt.substrate.view grammar.

   `kitchen-sink-render` exercises every portable ui/ component in the
   catalog - all text, layout, input, action and display kinds, every
   button/badge/alert variant, hidden nodes and event action descriptors -
   as one serializable view tree that renders through both platform
   frameworks (js.react.view and dart.ui.view). `web-escape-render` holds
   the `fg/` platform band: figma-kit components with no Wind counterpart,
   which portable validation rejects by design."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.substrate.view :as view]]})

(defn.xt sink-section
  [title children]
  (return
   (view/node "ui/column" {"class" "flex flex-col gap-3"}
              [(view/node "ui/label"
                          {"value" title
                           "class" "text-xs font-semibold uppercase tracking-wide text-slate-400"}
                          [])
               children])))

;;
;; test-support walkers - usable from consumer tests on any target
;;

(defn.xt collect-ids
  "collects every component id in a view tree into an array"
  [value out]
  (cond (xt/x:is-array? value)
        (xt/for:array [item value]
          (-/collect-ids item out))

        (xt/x:is-object? value)
        (do (xt/x:arr-push out (xt/x:get-key value "component"))
            (-/collect-ids (or (xt/x:get-key value "children") []) out))

        :else nil)
  (return out))

(defn.xt has-val?
  "checks whether an array contains a value"
  [arr value]
  (var found false)
  (xt/for:array [item arr]
    (when (== item value)
      (:= found true)))
  (return found))

(defn.xt kitchen-sink-render
  "renders every portable catalog component on one screen"
  [snapshot]
  (var pending (== true (xt/x:get-key snapshot "pending")))
  (var fills (or (xt/x:get-key snapshot "fills")
                 [["10:02" "BUY" "12.50" "4"]
                  ["10:05" "SELL" "12.55" "2"]
                  ["10:11" "BUY" "12.45" "6"]]))
  (return
   (view/node
    "ui/column" {"class" "flex flex-col gap-8 p-8 max-w-3xl"}
    [;; header
     (view/node "ui/row" {"class" "flex flex-row items-center justify-between"}
                [(view/node "ui/column" {"class" "flex flex-col gap-1"}
                            [(view/node "ui/title" {"value" "Kitchen sink"} [])
                             (view/node "ui/description"
                                        {"value" "Every portable ui/ component in one serializable tree."}
                                        [])])
                 (view/node "ui/badge" {"value" "v1" "variant" "secondary"} [])])
     (view/node "ui/separator" {"class" "bg-slate-200"} [])

     ;; text band
     (-/sink-section
      "Text"
      [(view/node "ui/text" {"value" "Body text - span on web, WText in Wind."} [])
       (view/node "ui/label" {"value" "Inline label" "for" "sink-name"} [])
       (view/node "ui/row" {"class" "flex flex-row items-center gap-2"}
                  [(view/node "ui/icon" {"value" "info" "aria_label" "Info"} [])
                   (view/node "ui/spinner" {"value" "Loading…"} [])])
       (view/node "ui/text" {"value" "you should not see this" "hidden" true} [])])

     ;; badges
     (-/sink-section
      "Badges"
      [(view/node "ui/row" {"class" "flex flex-row gap-2"}
                  [(view/node "ui/badge" {"value" "default"} [])
                   (view/node "ui/badge" {"value" "secondary" "variant" "secondary"} [])
                   (view/node "ui/badge" {"value" "outline" "variant" "outline"} [])
                   (view/node "ui/badge" {"value" "destructive" "variant" "destructive"} [])])])

     ;; buttons
     (-/sink-section
      "Buttons"
      [(view/node "ui/row" {"class" "flex flex-row flex-wrap gap-2"}
                  [(view/node "ui/button" {} ["Default"])
                   (view/node "ui/button" {"variant" "secondary"} ["Secondary"])
                   (view/node "ui/button" {"variant" "outline"} ["Outline"])
                   (view/node "ui/button" {"variant" "ghost"} ["Ghost"])
                   (view/node "ui/button" {"variant" "destructive"} ["Delete"])
                   (view/node "ui/button" {"variant" "link"} ["Link"])
                   (view/node "ui/button" {"size" "sm"} ["Small"])
                   (view/node "ui/button" {"disabled" true} ["Disabled"])
                   (view/node "ui/button" {"pending" pending} ["Saving"])
                   (view/node "ui/button" {"variant" "default"
                                           "on_press" (view/action "demo/sink-press" nil)}
                              ["Fire action"])])])

     ;; alerts
     (-/sink-section
      "Alerts"
      [(view/node "ui/alert" {"variant" "default"}
                  [(view/node "ui/text" {"value" "Default alert body"} [])])
       (view/node "ui/alert" {"variant" "destructive"}
                  [(view/node "ui/text" {"value" "Destructive alert body"} [])])])

     ;; form card
     (-/sink-section
      "Form"
      [(view/node
        "ui/card" {"class" "flex flex-col gap-4 p-6"}
        [(view/node "ui/card-header" {"class" "flex flex-col gap-1 p-0"}
                    [(view/node "ui/card-title" {"value" "Create organisation"} [])
                     (view/node "ui/card-description"
                                {"value" "Names are unique within your workspace."}
                                [])])
         (view/node "ui/card-content" {"class" "flex flex-col gap-4 p-0"}
                    [(view/node "ui/input" {"id" "sink-name"
                                            "value" (or (xt/x:get-key snapshot "name") "")
                                            "placeholder" "Acme Trading"
                                            "on_change" (view/action "demo/set-name"
                                                                     (view/event-value ["value"]))}
                                [])
                     (view/node "ui/textarea" {"placeholder" "What does this organisation trade?"
                                               "rows" 3
                                               "on_change" (view/action "demo/set-about"
                                                                        (view/event-value ["value"]))}
                                [])])
         (view/node "ui/card-footer" {"class" "flex flex-row justify-end gap-2 p-0"}
                    [(view/node "ui/button" {"variant" "ghost"} ["Cancel"])
                     (view/node "ui/button" {"pending" pending
                                             "on_press" (view/action "demo/create-org" nil)}
                                ["Create"])])])])

     ;; image
     (-/sink-section
      "Image"
      [(view/node "ui/image" {"src" "/demo.png" "alt" "Demo" "class" "h-16 w-16 rounded"} [])])

     ;; table
     (-/sink-section
      "Table"
      [(view/node
        "ui/table" {"class" "w-full text-sm"}
        [(view/node "ui/table-header" {}
                    [(view/node "ui/table-row" {}
                                [(view/node "ui/table-head" {"value" "Time"} [])
                                 (view/node "ui/table-head" {"value" "Side"} [])
                                 (view/node "ui/table-head" {"value" "Price"} [])
                                 (view/node "ui/table-head" {"value" "Size"} [])])])
         (view/node "ui/table-body" {}
                    (xtd/arr-map
                     fills
                     (fn [fill]
                       (return
                        (view/node "ui/table-row" {}
                                   [(view/node "ui/table-cell" {"value" (xt/x:get-idx fill 0)} [])
                                    (view/node "ui/table-cell" {"value" (xt/x:get-idx fill 1)} [])
                                    (view/node "ui/table-cell" {"value" (xt/x:get-idx fill 2)} [])
                                    (view/node "ui/table-cell" {"value" (xt/x:get-idx fill 3)} [])])))))])])

     ;; scroll region
     (-/sink-section
      "Scroll"
      [(view/node "ui/scroll" {"class" "h-24 overflow-auto rounded border border-slate-200 p-2"}
                  [(view/node "ui/column" {"class" "flex flex-col gap-1"}
                              [(view/node "ui/text" {"value" "row 1"} [])
                               (view/node "ui/text" {"value" "row 2"} [])
                               (view/node "ui/text" {"value" "row 3"} [])
                               (view/node "ui/text" {"value" "row 4"} [])
                               (view/node "ui/text" {"value" "row 5"} [])
                               (view/node "ui/text" {"value" "row 6"} [])])])])

     (view/node "ui/fragment" {}
                [(view/node "ui/separator" {"class" "bg-slate-200"} [])
                 (view/node "ui/description"
                            {"value" "Platform-only figma components live in demo/web-escape."}
                            [])])])))

(defn.xt kitchen-sink-spec
  []
  (return (view/view-spec "demo/kitchen-sink" {} (-/kitchen-sink-render {}))))

;;
;; web-escape - fg/ platform band (web-only; rejected by validate-portable)
;;

(defn.xt web-escape-render
  "uses figma-kit components that have no portable counterpart yet"
  [_snapshot]
  (return
   (view/node "ui/column" {"class" "flex flex-col gap-4 p-6"}
              [(view/node "ui/title" {"value" "Web-only components"} [])
               (view/node "ui/description"
                          {"value" "These lower straight to @xtalk/figma-ui; the Wind backend rejects them."}
                          [])
               (view/node "fg/hover-card" {}
                          [(view/node "fg/hover-card-trigger" {}
                                      [(view/node "ui/button" {"variant" "link"} ["Hover me"])])
                           (view/node "fg/hover-card-content" {}
                                      [(view/node "ui/text" {"value" "Rendered by Radix on web"} [])])])
               (view/node "fg/progress" {"class" "w-1/2"} [])])))

(defn.xt web-escape-spec
  []
  (return (view/view-spec "demo/web-escape" {} (-/web-escape-render {}))))
