(ns lib.supabase
  (:require [clojure.java.io :as io]))

(defonce +loaded-namespaces+
  (atom #{}))

(def +resource-map+
  {'lib.supabase.route "supabase/route.clj"
   'lib.supabase.common "supabase/common.clj"
   'lib.supabase.auth "supabase/auth.clj"
   'lib.supabase.admin "supabase/admin.clj"
   'lib.supabase.query "supabase/query.clj"
   'lib.supabase.rpc "supabase/rpc.clj"
   'lib.supabase.realtime "supabase/realtime.clj"
   'net.openapi.params "openapi/params.clj"})

(defn ensure-resource-loaded
  [ns-sym]
  (when-not (contains? @+loaded-namespaces+ ns-sym)
    (when-let [resource (get +resource-map+ ns-sym)]
      (load-file (.getPath (io/resource resource)))
      (swap! +loaded-namespaces+ conj ns-sym))))

(defn resolve-impl
  [sym]
  (let [ns-sym (symbol (namespace sym))]
    (ensure-resource-loaded ns-sym)
    (or (ns-resolve ns-sym (symbol (name sym)))
        (requiring-resolve sym))))

(defn client?
  [obj]
  ((resolve-impl 'lib.supabase.common/client?) obj))

(defn create-client
  [base_url api_key & [opts]]
  ((resolve-impl 'lib.supabase.common/create-client) base_url api_key opts))

(defn state-atom
  [client]
  ((resolve-impl 'lib.supabase.common/state-atom) client))

(defn raw-state
  [client]
  ((resolve-impl 'lib.supabase.common/raw-state) client))

(defn swap-state!
  [client f & args]
  (apply (resolve-impl 'lib.supabase.common/swap-state!) client f args))

(defn api-call
  ([opts]
   ((resolve-impl 'lib.supabase.common/api-call) opts))
  ([opts body]
   ((resolve-impl 'lib.supabase.common/api-call) opts body)))

(defn auth-call
  ([opts]
   ((resolve-impl 'lib.supabase.common/auth-call) opts))
  ([opts body]
   ((resolve-impl 'lib.supabase.common/auth-call) opts body)))

(defn admin-call
  ([opts]
   ((resolve-impl 'lib.supabase.common/admin-call) opts))
  ([opts body]
   ((resolve-impl 'lib.supabase.common/admin-call) opts body)))

(defn sqlrest-call
  ([opts]
   ((resolve-impl 'lib.supabase.common/sqlrest-call) opts))
  ([opts body]
   ((resolve-impl 'lib.supabase.common/sqlrest-call) opts body)))

(defn rpc-call
  ([opts]
   ((resolve-impl 'lib.supabase.common/rpc-call) opts))
  ([opts body]
   ((resolve-impl 'lib.supabase.common/rpc-call) opts body)))

(defn realtime-call
  ([opts]
   ((resolve-impl 'lib.supabase.common/realtime-call) opts))
  ([opts body]
   ((resolve-impl 'lib.supabase.common/realtime-call) opts body)))

(defn api-rpc
  [opts]
  ((resolve-impl 'lib.supabase.rpc/api-rpc) opts))

(defn api-select-all
  ([table]
   ((resolve-impl 'lib.supabase.query/api-select-all) table))
  ([table opts]
   ((resolve-impl 'lib.supabase.query/api-select-all) table opts)))

(defn api-signup
  [body opts]
  ((resolve-impl 'lib.supabase.auth/api-signup) body opts))

(defn api-signin
  [body opts]
  ((resolve-impl 'lib.supabase.auth/api-signin) body opts))

(defn api-signup-create
  [body opts]
  ((resolve-impl 'lib.supabase.admin/api-signup-create) body opts))

(defn api-signup-delete
  [user_id opts]
  ((resolve-impl 'lib.supabase.admin/api-signup-delete) user_id opts))

(defn api-impersonate
  [body opts]
  ((resolve-impl 'lib.supabase.auth/api-impersonate) body opts))

(defn connected?
  [client]
  ((resolve-impl 'lib.supabase.realtime/connected?) client))

(defn connect
  ([client]
   ((resolve-impl 'lib.supabase.realtime/connect) client))
  ([client opts]
   ((resolve-impl 'lib.supabase.realtime/connect) client opts)))

(defn disconnect
  [client]
  ((resolve-impl 'lib.supabase.realtime/disconnect) client))
