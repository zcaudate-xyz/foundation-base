(ns xt.db.node.view-model-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.view-model :as model]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.view-model/model-put :added "4.1"}
(fact "stores a clean view model using primary and caching sources"

  (!.js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node {"sources" {"primary" {"kind" "postgres"}
                                    "caching" {"kind" "sqlite"}}})
    (model/model-put node
                     "screen/admin"
                     "entries-screen"
                     {"sources" {"primary" {"kind" "postgres"}
                                 "caching" {"kind" "sqlite"}}
                      "views" {"list" {"query" {"table" "Task"}}
                               "detail" {"query" {"table" "Task"}
                                         "default_input" ["alpha"]}}})
    [(xt/x:obj-keys (. (model/model-get node "screen/admin" "entries-screen") ["sources"]))
     (xt/x:obj-keys (. (. node ["spaces"]) ["screen/admin"] ["state"] ["models"]))])
  => [["primary" "caching"]
      ["entries-screen"]])

^{:refer xt.db.node.view-model/view-set-input :added "4.1"}
(fact "updates input and returns a ready view snapshot"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-b"}))
    (model/install node nil)
    (model/model-put node
                     "screen/admin"
                     "entries-screen"
                     {"views" {"detail" {"query" {"table" "Task"}
                                         "default_input" ["alpha"]}}})
    (-> (model/view-set-input node "screen/admin" "entries-screen" "detail" ["beta"])
        (promise/x:promise-then
         (fn [result]
           (repl/notify {"input" (model/view-input node "screen/admin" "entries-screen" "detail")
                         "status" (. result ["status"])})))))
  => {"input" ["beta"]
      "status" "ready"})
