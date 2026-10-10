(ns js.react.ext-page-test
  (:use code.test)
  (:require [lang.core :as l]
            [js.react.helper-jsdom :as helper-source]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-notify :as notify]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [js.react.ext-page :as ext-page]
             [js.react :as r]
             [js.react.helper-jsdom :as helper]]})

(defn.js create-node
  []
  (return
   {"id" "node-a"
    "spaces" {"space/a" {"state" {}}}}))

(declare test-page-listen-model-output-sync)

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.react.ext-page/model-key :added "4.1"}
(fact "creates a stable listener key"

  (!.js
    (ext-page/model-key "space/a" ["page" "ping"]))
  => "[\"space/a\",[\"page\",\"ping\"]]")

^{:refer js.react.ext-page/get-model :added "4.1"}
(fact "retrieves a registered page model"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (page-core/group-add-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx] (return {"ok" true}))
              "defaults" {"args" [1 2]}}})
    (var model (ext-page/get-model node "space/a" ["page" "ping"]))
    {"type" (. model ["::"])
     "input" (. (. model ["input"]) ["current"])})
  => {"type" "event.model"
      "input" {"data" [1 2]}})

^{:refer js.react.ext-page/refreshModel :added "4.1"}
(fact "refreshes a page model through the wrapper"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/group-add-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx] (return {"refreshed" true}))
              "defaults" {"args" []}}})
    (-> (ext-page/refreshModel node "space/a" ["page" "ping"] {})
        (promise/x:promise-then
         (fn [acc]
           (repl/notify {"main" (. acc ["main"])
                         "path" (. acc ["path"])})))))
  => {"main" [true {"refreshed" true}]
      "path" ["page" "ping"]})

^{:refer js.react.ext-page/remoteCall :added "4.1"}
(fact "invokes the remote stage through the wrapper"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/group-add-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx] (return {"main" true}))
              "pipeline" {"remote" {"handler" (fn [ctx] (return {"remote" true}))}}
              "defaults" {"args" []}}})
    (-> (ext-page/remoteCall node "space/a" ["page" "ping"] [] true)
        (promise/x:promise-then
         (fn [acc]
           (repl/notify {"remote" (. acc ["remote"])
                         "path" (. acc ["path"])})))))
  => {"remote" [true {"remote" true}]
      "path" ["page" "ping"]})

^{:refer js.react.ext-page/refreshArgsFn :added "4.1"}
(fact "sets model input from args"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/group-add-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx]
                          (return {"data" (. (. ctx ["input"]) ["data"])}))
              "defaults" {"args" []}}})
    (-> (ext-page/refreshArgsFn node "space/a" ["page" "ping"] [1 2 3] {})
        (promise/x:promise-then
         (fn [acc]
           (repl/notify {"main" (. acc ["main"])})))))
  => {"main" [true {"data" [1 2 3]}]})

^{:refer js.react.ext-page/throttled-setter :added "4.1"}
(fact "creates a throttled setter"

  (notify/wait-on :js
    (var results [])
    (var [setThrottled throttle] (ext-page/throttled-setter
                                  (fn [x] (. results (push x)))
                                  20))
    (setThrottled 1)
    (setThrottled 2)
    (setThrottled 3)
    (do (setTimeout (fn [] (repl/notify results)) 60)
        nil))
  => #(>= (count %) 1))

^{:refer js.react.ext-page/initModelBase :added "4.1"}
(fact "syncs the echoed output when the model input changes"

  (helper-source/test
   (fn [{:# [node state]}]
     (var model (ext-page/get-model node "space/a" ["page" "echo"]))
     (var ref (r/ref nil))
     (var cleanup
          (ext-page/initModelBase
           node
           "space/a"
           ["page" "echo"]
           {"setResult" (fn [value]
                          (xt/x:set-key state "result" value))
            "getResult" (fn []
                          (return (event-model/get-current model nil)))
            "resultRef" ref}))
     (xt/x:set-key state "cleanup" cleanup)
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (var node (substrate/node-create (-/create-node)))
     (page-core/group-add-attach
      node
      "space/a"
      "page"
      {"echo" {"handler" (fn [ctx]
                           (return (+ "ECHO-"
                                      (. ctx ["input"] ["data"]))))
               "defaults" {"args" ["initial"]}}})
     (return {"node" node
              "state" {}}))
   (fn [{:# [node state]} document _]
     (return
      (-> (ext-page/refreshArgsFn
           node
           "space/a"
           ["page" "echo"]
           ["CHANGED"]
           {})
          (promise/x:promise-then
           (fn [_]
             (return
              (new Promise
                   (fn [resolve]
                     (setTimeout
                      (fn []
                        (resolve
                         {"result" (. state ["result"])
                          "cleanup" (xt/x:is-function?
                                     (. state ["cleanup"]))}))
                      0))))))))))
  => {"result" "ECHO-CHANGED"
      "cleanup" true})

^{:refer js.react.ext-page/listenModel
  :added "4.1"}
(fact "updates a mounted component when the page model input changes"

  (notify/wait-on :js
    (var node (substrate/node-create (-/create-node)))
    (page-core/group-add-attach
     node
     "space/a"
     "page"
     {"ping" {"handler" (fn [ctx]
                          (return {"ok" true}))
              "defaults" {"args" [1 2]}}})
    (var env (helper/setup {}))
    (var React (require "react"))
    (var Probe
         (fn [props]
           (var value (ext-page/listenModel
                       (. props ["node"])
                       "space/a"
                       ["page" "ping"]
                       "input"
                       nil))
           (return
            (r/createElement "span" nil
                             (JSON.stringify (. value ["data"]))))))
    (. (helper/render env Probe {"node" node})
       (then (fn [_]
               (var before document.body.innerHTML)
               (. (React.act
                   (fn []
                     (return
                      (ext-page/refreshArgsFn
                       node
                       "space/a"
                       ["page" "ping"]
                       [9 8]
                       {}))))
                  (then (fn [_]
                          (setTimeout
                           (fn []
                             (var after document.body.innerHTML)
                             (var closed (helper/teardown env))
                             (repl/notify {"before" before
                                           "after" after
                                           "closed" closed}))
                           0))))))))
  => {"before" "<div id=\"root\"><span>[1,2]</span></div>"
      "after" "<div id=\"root\"><span>[9,8]</span></div>"
      "closed" true})

^{:refer js.react.ext-page/listenModel
  :id test-page-listen-model-output-sync
  :added "4.1"}
(fact "syncs selected page output after input changes"

  (helper-source/test
   (fn [props]
     (var value
          (ext-page/listenModel
           (. props ["node"])
           "space/a"
           ["page" "echo"]
           "output"
           nil))
     (return
      (r/createElement
       "span"
       nil
       (JSON.stringify (or value "INITIAL")))))
   
   (fn [_]
     (var node (substrate/node-create (-/create-node)))
     (page-core/group-add-attach
      node
      "space/a"
      "page"
      {"echo" {"handler" (fn [ctx]
                           (return (. (. ctx ["input"]) ["data"])))
               "defaults" {"args" ["initial"]}}})
     (return {"node" node}))
   
   (fn [props document _]
     (var node (. props ["node"]))
     (var before document.body.innerHTML)
     (var task
          (r/act
           (fn []
             (return
              (ext-page/refreshArgsFn
               node
               "space/a"
               ["page" "echo"]
               ["changed"]
               {})))))
     (return
      (. (Promise.resolve task)
         (then
          (fn [_]
            (return
             (helper/await-dom
              (fn []
                (return
                 {"before" before
                  "after" document.body.innerHTML}))))))))))
  
  => {"after" "<div id=\"root\"><span>[\"changed\"]</span></div>", "before" "<div id=\"root\"><span>\"INITIAL\"</span></div>"})


^{:refer js.react.ext-page/listenModelOutput :added "4.1"}
(fact "updates the full output record"
  (helper-source/test
   (fn [props]
     (var node (. props ["node"]))
     (var output (ext-page/listenModelOutput node "space/a" ["page" "ping"] ["output"] nil))
     (return (r/createElement "span" nil
                              (JSON.stringify (or output {})))))
   (fn [_]
     (var node (substrate/node-create (-/create-node)))
     (page-core/group-add-attach node "space/a" "page"
                                 {"ping" {"handler" (fn [ctx]
                                                      (return {"ok" true}))
                                          "defaults" {"args" []}}})
     (return {"node" node}))
   (fn [props document _]
     (var node (. props ["node"]))
     (var task
          (r/act
           (fn []
             (return (ext-page/refreshModel node "space/a" ["page" "ping"] {})))))
     (return
      (. (Promise.resolve task)
         (then (fn [_]
                 (return
                  (helper/await-dom
                   (fn []
                     (var output (JSON.parse document.body.textContent))
                     (var result {"current" (. output ["current"])
                                  "type" (. output ["type"])})
                     (return result))))))))))
  => {"current" {"ok" true} "type" "output"})

^{:refer js.react.ext-page/listenModelThrottled :added "4.1"}
(fact "throttles successful page output"
  (helper-source/test
   (fn [props]
     (var node (. props ["node"]))
     (var output (ext-page/listenModelThrottled node "space/a" ["page" "ping"] 10 nil))
     (return (r/createElement "span" nil
                              (JSON.stringify (or output {})))))
   (fn [_]
     (var node (substrate/node-create (-/create-node)))
     (page-core/group-add-attach node "space/a" "page"
                                 {"ping" {"handler" (fn [ctx]
                                                      (return {"ok" true}))
                                          "defaults" {"args" []}}})
     (return {"node" node}))
   (fn [props document _]
     (var node (. props ["node"]))
     (var task
          (r/act
           (fn []
             (return (ext-page/refreshModel node "space/a" ["page" "ping"] {})))))
     (return
      (. (Promise.resolve task)
         (then (fn [_]
                 (return
                  (new Promise
                       (fn [resolve]
                         (setTimeout
                          (fn []
                            (var result document.body.textContent)
                            (resolve result))
                          30))))))))))
  => "{\"ok\":true}")

^{:refer js.react.ext-page/useRefreshArgs :added "4.1"}
(fact "refreshes page model input from React args"
  (helper-source/test
   (fn [props]
     (var node (. props ["node"]))
     (ext-page/useRefreshArgs node "space/a" ["page" "ping"] [4 5] {})
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (var node (substrate/node-create (-/create-node)))
     (page-core/group-add-attach node "space/a" "page"
                                 {"ping" {"handler" (fn [ctx]
                                                      (return {"ok" true}))
                                          "defaults" {"args" []}}})
     (return {"node" node}))
   (fn [props document _]
     (var node (. props ["node"]))
     (return
      (new Promise
           (fn [resolve]
             (setTimeout
              (fn []
                (var model (ext-page/get-model node "space/a" ["page" "ping"]))
                (var result (. (. model ["input"]) ["current"]))
                (resolve result))
              30))))))
  => {"data" [4 5]})

^{:refer js.react.ext-page/listenSuccess :added "4.1"}
(fact "returns the successful page output"
  (helper-source/test
   (fn [props]
     (var node (. props ["node"]))
     (var result (ext-page/listenSuccess node "space/a" ["page" "ping"] []
                                         {"default" {"empty" true}}
                                         nil))
     (return (r/createElement "span" nil
                              (JSON.stringify (or result {})))))
   (fn [_]
     (var node (substrate/node-create (-/create-node)))
     (page-core/group-add-attach node "space/a" "page"
                                 {"ping" {"handler" (fn [ctx]
                                                      (return {"ok" true}))
                                          "defaults" {"args" []}}})
     (return {"node" node}))
   (fn [props document _]
     (var node (. props ["node"]))
     (var task
          (r/act
           (fn []
             (return (ext-page/refreshModel node "space/a" ["page" "ping"] {})))))
     (return
      (. (Promise.resolve task)
         (then (fn [_]
                 (return
                  (helper/await-dom
                   (fn []
                     (var result (JSON.parse document.body.textContent))
                     (return result))))))))))
  => {"ok" true})
