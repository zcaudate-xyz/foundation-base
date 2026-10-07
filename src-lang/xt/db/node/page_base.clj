(ns xt.db.node.page-base
  "Transport-neutral declarative page model wiring for xt.db nodes."
  (:require [lang.core :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.client-base :as client-base]
             [xt.substrate.page-proxy :as page-proxy]]})

(defn.xt ^{:public true}
  page-model-path
  "Returns the group/model path represented by a page handle."
  {:added "4.1.6"}
  [page model-id]
  (return [(. page ["group_id"]) model-id]))

(defn.xt ^{:public true}
  page-detach
  "Closes a page proxy and detaches its declared models idempotently."
  {:added "4.1.6"}
  [page opts]
  (when (or (xt/x:nil? page)
            (. page ["closed"]))
    (return (promise/x:promise-run true)))
  (:= (. page ["closed"]) true)
  (var node (. page ["client"]))
  (var close-promise (promise/x:promise-run nil))
  (when (. page ["proxy_open"])
    (:= close-promise
        (promise/x:promise-catch
         (page-proxy/group-close-proxy
          node
          (. page ["space_id"])
          (. page ["group_id"])
          (or opts {}))
         (fn [_]
           (return nil)))))
  (var tasks [])
  (xt/for:array [page-args (. page ["attached"])]
                (xt/x:arr-push
                 tasks
                 (promise/x:promise-catch
                  (client-base/detach-model
                   node
                   (. page ["primary_id"])
                   page-args
                   (or opts {}))
                  (fn [_]
                    (return nil)))))
  (return
   (-> close-promise
       (promise/x:promise-then
        (fn [_]
          (return (promise/x:promise-all tasks))))
       (promise/x:promise-then
        (fn [_]
          (return true))))))

(defn.xt ^{:public true}
  page-attach
  "Attaches the RPC/dataview models declared by a page and opens its proxy group."
  {:added "4.1.6"}
  [node primary-id space-id page-spec opts]
  (var group-id (. page-spec ["group_id"]))
  (var models (or (. page-spec ["models"]) {}))
  (var page {"client" node
             "primary_id" primary-id
             "space_id" space-id
             "group_id" group-id
             "models" models
             "attached" []
             "proxy_open" false
             "closed" false})
  (var invalid nil)
  (xt/for:object [[model-id entry] models]
                (when (and (xt/x:nil? (. entry ["rpc"]))
                           (xt/x:nil? (. entry ["dataview"])))
                  (:= invalid
                      (xt/x:cat "Unknown page model type: " model-id))))
  (when (xt/x:not-nil? invalid)
    (xt/x:err invalid))
  (var tasks [])
  (xt/for:object [[model-id entry] models]
                (var page-args {"space_id" space-id
                                "group_id" group-id
                                "model_id" model-id})
                (var task nil)
                (xt/x:arr-push (. page ["attached"]) page-args)
                (cond (xt/x:not-nil? (. entry ["rpc"]))
                      (:= task
                          (client-base/rpc-attach-model
                           node
                           primary-id
                           page-args
                           (. entry ["rpc"])
                           (. entry ["model"])
                           (or opts {})))
                      (xt/x:not-nil? (. entry ["dataview"]))
                      (:= task
                          (client-base/dataview-attach-model
                           node
                           primary-id
                           page-args
                           (. entry ["dataview"])
                           (. entry ["model"])
                           (or opts {})))
                      :else
                      (:= task (promise/x:promise-run nil)))
                (xt/x:arr-push
                 tasks
                 (promise/x:promise-catch
                  task
                  (fn [err]
                    (return {"error" err})))))
  (var open-promise
       (promise/x:promise-then
        (promise/x:promise-all tasks)
        (fn [results]
          (var error nil)
          (xt/for:array [result results]
                        (when (and (xt/x:nil? error)
                                   (xt/x:not-nil? (. result ["error"])))
                          (:= error (. result ["error"]))))
          (when (xt/x:not-nil? error)
            (xt/x:err error))
          (return
           (promise/x:promise-then
            (page-proxy/group-open-proxy
             node
             space-id
             group-id
             (or opts {}))
            (fn [_]
              (:= (. page ["proxy_open"]) true)
              (return page)))))))
  (return
   (promise/x:promise-catch
    open-promise
    (fn [err]
      (var cleanup (-/page-detach page opts))
      (return
       (promise/x:promise-then
        cleanup
        (fn [_]
          (return (xt/x:err err)))))))))

(defn.xt ^{:public true}
  page-model-call
  "Invokes a model through the page proxy."
  {:added "4.1.6"}
  [page model-id args save-output opts]
  (return
   (page-proxy/model-proxy-call
    (. page ["client"])
    (. page ["space_id"])
    (. page ["group_id"])
    model-id
    args
    save-output
    (or opts {}))))
