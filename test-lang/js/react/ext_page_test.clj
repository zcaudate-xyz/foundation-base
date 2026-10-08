(ns js.react.ext-page-test
    (:require [lang.core :as l]
              [js.react.helper-jsdom :as helper-source])
    (:use code.test))

(l/script- :js
           {:runtime :basic
            :require [[xt.lang.common-notify :as notify]
                      [xt.lang.common-repl :as repl]
                      [xt.lang.spec-base :as xt]
                      [xt.lang.spec-promise :as promise]
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
(defn.js await-dom
         [f]
         (return
          (new Promise
               (fn [resolve]
                   (setTimeout (fn [] (resolve (f))) 0)))))

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
(fact "updates the keyed listener result"
  (helper-source/wait-on
   {}
   (var node (substrate/node-create (-/create-node)))
   (page-core/group-add-attach node "space/a" "page"
                               {"ping" {"handler" (fn [ctx]
                                                      (return {"ok" true}))}
                                "defaults" {"args" [1 2]}})
   (var controls {})
   (var Component
        (fn []
            (var model (ext-page/get-model node "space/a" ["page" "ping"]))
            (var [result setResult] (r/local (. (. model ["input"]) ["current"])))
            (var ref (r/useFollowRef result))
            (var cleanup (ext-page/initModelBase
                          node "space/a" ["page" "ping"]
                          {"setResult" setResult
                           "getResult" (fn [] (return (. (. model ["input"]) ["current"])))
                           "resultRef" ref}))
            (xt/x:set-key controls "cleanup" cleanup)
            (xt/x:set-key controls "result" result)
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (. (ext-page/refreshArgsFn node "space/a" ["page" "ping"] [9 8] {})
                    (then (fn [_]
                              (return (-/await-dom
                                       (fn []
                                           (return {"data" (. (. controls ["result"]) ["data"])
                                                    "cleanup" (xt/x:is-function? (. controls ["cleanup"]))}))))))))))))
  => {"data" [9 8] "cleanup" true})

^{:refer js.react.ext-page/initModelBase
  :added "4.1"}
(fact "allocates the listener id before the effect callback"
  (let [source (str (var-get (find-var 'js.react.ext-page/initModelBase)))]
       (< (.indexOf source "r.id")
          (.indexOf source "useEffect")))
  => true)


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

^{:refer js.react.ext-page/listenModelOutput :added "4.1"}
(fact "updates the full output record"
  (helper-source/wait-on
   {}
   (var node (substrate/node-create (-/create-node)))
   (page-core/group-add-attach node "space/a" "page"
                               {"ping" {"handler" (fn [ctx]
                                                      (return {"ok" true}))}
                                "defaults" {"args" []}})
   (var controls {})
   (var Component
        (fn []
            (var output (ext-page/listenModelOutput node "space/a" ["page" "ping"] ["output"] nil))
            (xt/x:set-key controls "output" output)
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (. (ext-page/refreshModel node "space/a" ["page" "ping"] {})
                    (then (fn [_]
                              (return (-/await-dom
                                       (fn []
                                           (return {"current" (. (. controls ["output"]) ["current"])
                                                    "type" (. (. controls ["output"]) ["type"])}))))))))))))
  => {"current" {"ok" true} "type" "output"})

^{:refer js.react.ext-page/listenModelThrottled :added "4.1"}
(fact "throttles successful page output"
  (helper-source/wait-on
   {}
   (var node (substrate/node-create (-/create-node)))
   (page-core/group-add-attach node "space/a" "page"
                               {"ping" {"handler" (fn [ctx]
                                                      (return {"ok" true}))}
                                "defaults" {"args" []}})
   (var controls {})
   (var Component
        (fn []
            (var output (ext-page/listenModelThrottled node "space/a" ["page" "ping"] 10 nil))
            (xt/x:set-key controls "output" output)
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (. (ext-page/refreshModel node "space/a" ["page" "ping"] {})
                    (then (fn [_]
                              (return
                               (new Promise
                                    (fn [resolve]
                                        (setTimeout
                                         (fn [] (resolve (. controls ["output"])))
                                         30))))))))))))
  => {"ok" true})

^{:refer js.react.ext-page/useRefreshArgs :added "4.1"}
(fact "refreshes page model input from React args"
  (helper-source/wait-on
   {}
   (var node (substrate/node-create (-/create-node)))
   (page-core/group-add-attach node "space/a" "page"
                               {"ping" {"handler" (fn [ctx]
                                                      (return {"ok" true}))}
                                "defaults" {"args" []}})
   (var Component
        (fn []
            (ext-page/useRefreshArgs node "space/a" ["page" "ping"] [4 5] {})
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (return (-/await-dom
                          (fn []
                              (var model (ext-page/get-model node "space/a" ["page" "ping"]))
                              (return (. (. model ["input"]) ["current"]))))))))))
  => {"data" [4 5]})

^{:refer js.react.ext-page/listenSuccess :added "4.1"}
(fact "returns the successful page output"
  (helper-source/wait-on
   {}
   (var node (substrate/node-create (-/create-node)))
   (page-core/group-add-attach node "space/a" "page"
                               {"ping" {"handler" (fn [ctx]
                                                      (return {"ok" true}))}
                                "defaults" {"args" []}})
   (var controls {})
   (var Component
        (fn []
            (var result (ext-page/listenSuccess node "space/a" ["page" "ping"] []
                                                {"default" {"empty" true}}
                                                nil))
            (xt/x:set-key controls "result" result)
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (. (ext-page/refreshModel node "space/a" ["page" "ping"] {})
                    (then (fn [_]
                              (return (-/await-dom (fn [] (return (. controls ["result"])))))))))))))
  => {"ok" true})
