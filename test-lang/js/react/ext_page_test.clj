(ns js.react.ext-page-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-notify :as notify]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [js.react.ext-page :as ext-page]]})

(defn.js create-node
  []
  (return
   {"id" "node-a"
    "spaces" {"space/a" {"state" {}}}}))

(fact:global
 {:setup [(l/rt:restart :js)]
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
(fact "is a function"

  (!.js
   (typeof ext-page/initModelBase))
  => "function")

^{:refer js.react.ext-page/listenModel :added "4.1"}
(fact "is a function"

  (!.js
   (typeof ext-page/listenModel))
  => "function")

^{:refer js.react.ext-page/listenModelOutput :added "4.1"}
(fact "is a function"

  (!.js
   (typeof ext-page/listenModelOutput))
  => "function")

^{:refer js.react.ext-page/listenModelThrottled :added "4.1"}
(fact "is a function"

  (!.js
   (typeof ext-page/listenModelThrottled))
  => "function")

^{:refer js.react.ext-page/useRefreshArgs :added "4.1"}
(fact "is a function"

  (!.js
   (typeof ext-page/useRefreshArgs))
  => "function")

^{:refer js.react.ext-page/listenSuccess :added "4.1"}
(fact "is a function"

  (!.js
   (typeof ext-page/listenSuccess))
  => "function")