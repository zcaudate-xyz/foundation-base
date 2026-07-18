(ns demo.wind-task-list-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]
             [xt.ui.core :as ui]
             [xt.ui.state.core :as state]
             [xt.ui.widgets.core :as widgets]
             [demo.wind-task-list.app :as app]]})

^{:refer demo.wind-task-list.app/make-controller :added "4.1"}
(fact "adds and removes trimmed tasks through portable controller actions"
  (notify/wait-on :js
    (var controller (app/make-controller))
    (var actions (state/actions-create
                  controller
                  ["set_draft" "add_item" "remove_item"]))
    (promise/x:promise-then
     ((xt/x:get-key actions "set_draft") "  Ship the demo  ")
     (fn [_]
       (return
        (promise/x:promise-then
         ((xt/x:get-key actions "add_item") nil)
         (fn [_]
           (var added (state/snapshot controller))
           (var added-items (xt/x:get-key added "items"))
           (var added-id (xt/x:get-key (xt/x:get-idx added-items 2) "id"))
           (return
            (promise/x:promise-then
             ((xt/x:get-key actions "remove_item") added-id)
             (fn [_]
               (var removed (state/snapshot controller))
               (return
                (repl/notify
                 [(xt/x:get-key added "draft")
                  (xt/x:len added-items)
                  (xt/x:get-key (xt/x:get-idx added-items 2) "value")
                  (xt/x:len (xt/x:get-key removed "items"))])))))))))))
  => ["" 3 "Ship the demo" 2])

^{:refer demo.wind-task-list.app/view :added "4.1"}
(fact "produces a valid portable tree and an explicit empty state"
  (!.js
   (var noop (fn [_] nil))
   (var actions {"set_draft" noop "add_item" noop "remove_item" noop})
   (var tree (app/view {"items" [{"id" "task-1" "value" "One"}]
                             "draft" ""}
                            actions))
   (var empty-tree (app/view {"items" [] "draft" ""} actions))
   [(xt/x:get-key tree "component")
    (ui/validate-node (widgets/registry) tree)
    (xt/x:get-path tree ["children" 0 "children" 0 "children" 0 "props" "value"])
    (xt/x:get-path empty-tree
                   ["children" 0 "children" 1 "children" 1
                    "children" 0 "props" "value"])])
  => ["ui/column" true "xt.ui Wind Task List" "No tasks yet. Add one above."])
