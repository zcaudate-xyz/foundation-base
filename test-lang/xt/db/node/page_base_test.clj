(ns xt.db.node.page-base-test
  (:use code.test)
  (:require [lang.core :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.db.node.page-base :as page-base]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})



^{:refer xt.db.node.page-base/page-model-path :added "4.1.6"}
(fact "returns the group/model path for a page handle"
  (!.js
   (page-base/page-model-path {"group_id" "campaigns"} "overview"))
  => ["campaigns" "overview"])

^{:refer xt.db.node.page-base/page-detach :added "4.1.6"}
(fact "safely tears down despite close and detach handler failures, then stays idempotent"
  (notify/wait-on :js
    (var node (substrate/node-create {"id" "page-base-detach"}))
    (var calls [])
    (substrate/register-handler
     node "@page/group-close"
     (fn [_ _ _ _]
       (xt/x:arr-push calls ["close"])
       (return (promise/x:promise-new
                (fn [_ reject] (reject "close failed")))))
     nil)
    (substrate/register-handler
     node "@xt.db/detach-model"
     (fn [_ args _ _]
       (var page-args (xtd/nth args 1))
       (xt/x:arr-push calls ["detach" (. page-args ["model_id"])])
       (return (promise/x:promise-new
                (fn [_ reject] (reject "detach failed")))))
     nil)
    (var page {"client" node
               "primary_id" "db/primary"
               "space_id" "room/a"
               "group_id" "campaigns"
               "proxy_open" true
               "closed" false
               "attached" [{"model_id" "overview"}
                           {"model_id" "audience"}]})
    (var first-promise (page-base/page-detach page {}))
    (var result
         (promise/x:promise-then
          first-promise
          (fn [first]
            (return
             (promise/x:promise-then
              (page-base/page-detach page {})
              (fn [second]
                (return
                 (repl/notify
                  {"results" [first second]
                   "closed" (. page ["closed"])
                   "calls" calls}))))))))
    (promise/x:promise-catch
     result
     (fn [err]
       (repl/notify {"error" err "calls" calls}))))
  => {"results" [true true]
      "closed" true
      "calls" [["close"]
               ["detach" "overview"]
               ["detach" "audience"]]})

^{:refer xt.db.node.page-base/page-attach :added "4.1.6"}
(fact "attaches declared RPC and dataview models, opens the proxy, then tears down"
  (notify/wait-on :js
    (var node (substrate/node-create {"id" "page-base-attach"}))
    (var calls [])
    (substrate/register-handler
     node "@xt.db/rpc-attach-model"
     (fn [_ args _ _]
       (var page-args (xtd/nth args 1))
       (var rpc (xtd/nth args 2))
       (xt/x:arr-push calls ["rpc" (. page-args ["model_id"]) (. rpc ["name"])])
       (return {"status" "attached"}))
     nil)
    (substrate/register-handler
     node "@xt.db/dataview-attach-model"
     (fn [_ args _ _]
       (var page-args (xtd/nth args 1))
       (var dataview (xtd/nth args 2))
       (xt/x:arr-push calls ["dataview" (. page-args ["model_id"])
                             (. dataview ["name"])])
       (return {"status" "attached"}))
     nil)
    (substrate/register-handler
     node "@page/group-open"
     (fn [_ args _ _]
       (var payload (xt/x:first args))
       (xt/x:arr-push calls ["open" (. payload ["space"]) (. payload ["group"])])
       (return {"models" {}}))
     nil)
    (substrate/register-handler
     node "@page/group-close"
     (fn [_ args _ _]
       (var payload (xt/x:first args))
       (xt/x:arr-push calls ["close" (. payload ["space"]) (. payload ["group"])])
       (return {"status" "closed"}))
     nil)
    (substrate/register-handler
     node "@xt.db/detach-model"
     (fn [_ args _ _]
       (var page-args (xtd/nth args 1))
       (xt/x:arr-push calls ["detach" (. page-args ["model_id"])])
       (return {"status" "detached"}))
     nil)
    (var spec {"group_id" "campaigns"
               "models" {"overview" {"rpc" {"name" "campaign.list"}
                                     "model" {"kind" "table"}}
                         "audience" {"dataview" {"name" "campaign.audience"}
                                     "model" {"kind" "table"}}}})
    (-> (page-base/page-attach node "db/primary" "room/a" spec {})
        (promise/x:promise-then
         (fn [page]
           (var attached (. page ["attached"]))
           (return
            (-> (page-base/page-detach page {})
                (promise/x:promise-then
                 (fn [detached]
                   (repl/notify
                    {"proxy-opened" (. page ["proxy_open"])
                     "closed" (. page ["closed"])
                     "detached" detached
                     "attached-ids" [(. (xtd/nth attached 0) ["model_id"])
                                     (. (xtd/nth attached 1) ["model_id"])]
                     "proxy-removed" (xt/x:nil?
                                      (page-core/group-get node "room/a" "campaigns"))
                     "calls" calls})))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err "calls" calls})))))
  => {"proxy-opened" true
      "closed" true
      "detached" true
      "attached-ids" ["overview" "audience"]
      "proxy-removed" true
      "calls" [["rpc" "overview" "campaign.list"]
               ["dataview" "audience" "campaign.audience"]
               ["open" "room/a" "campaigns"]
               ["close" "room/a" "campaigns"]
               ["detach" "overview"]
               ["detach" "audience"]]}

  (notify/wait-on :js
    (var node (substrate/node-create {"id" "page-base-rollback"}))
    (var calls [])
    (substrate/register-handler
     node "@xt.db/rpc-attach-model"
     (fn [_ args _ _]
       (var page-args (xtd/nth args 1))
       (xt/x:arr-push calls ["rpc" (. page-args ["model_id"])])
       (return (promise/x:promise-new
                (fn [_ reject] (reject "rpc failed")))))
     nil)
    (substrate/register-handler
     node "@xt.db/dataview-attach-model"
     (fn [_ args _ _]
       (var page-args (xtd/nth args 1))
       (xt/x:arr-push calls ["dataview" (. page-args ["model_id"])])
       (return (promise/x:promise-run true)))
     nil)
    (substrate/register-handler
     node "@xt.db/detach-model"
     (fn [_ args _ _]
       (var page-args (xtd/nth args 1))
       (xt/x:arr-push calls ["detach" (. page-args ["model_id"])])
       (return {"status" "detached"}))
     nil)
    (substrate/register-handler
     node "@page/group-open"
     (fn [_ _ _ _]
       (xt/x:arr-push calls ["open"])
       (return {"models" {}}))
     nil)
    (var spec {"group_id" "campaigns"
               "models" {"broken" {"rpc" {"name" "campaign.broken"}}
                         "good" {"dataview" {"name" "campaign.good"}}}})
    (-> (page-base/page-attach node "db/primary" "room/a" spec {})
        (promise/x:promise-then
         (fn [_]
           (repl/notify {"unexpected" "resolved" "calls" calls})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err "calls" calls})))))
  => {"error" "rpc failed"
      "calls" [["rpc" "broken"]
               ["dataview" "good"]
               ["detach" "broken"]
               ["detach" "good"]]}
)

^{:refer xt.db.node.page-base/page-model-call :added "4.1.6"}
(fact "dispatches a model call through the page proxy with the selected scope"
  (notify/wait-on :js
    (var node (substrate/node-create {"id" "page-base-call"}))
    (var runtime (substrate/page-space-ensure node "room/a"))
    (var calls [])
    (xt/x:set-key (. runtime ["groups"])
                  "campaigns"
                  {"models" {}
                   "proxy_dispatch"
                   (fn [op _ space-id group-id args]
                     (xt/x:arr-push calls [op space-id group-id args])
                     (return (promise/x:promise-run
                              {"received" true "output" nil})))})
    (-> (page-base/page-model-call
         {"client" node "space_id" "room/a" "group_id" "campaigns"}
         "overview"
         [{"select_args" []}]
         true
         nil)
        (promise/x:promise-then
         (fn [out]
           (repl/notify {"result" out "calls" calls})))))
  => {"result" {"received" true "output" nil}
      "calls" [["proxy-call" "room/a" "campaigns"
                ["overview" [{"select_args" []}] true]] ]})
