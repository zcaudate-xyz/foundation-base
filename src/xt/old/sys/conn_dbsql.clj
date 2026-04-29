(ns xt.old.sys.conn-dbsql
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [lua.nginx.common-promise]
             [xt.runtime.type-sql-connection :as sqlrt]]})

(defn.xt wrap-callback
  [callbacks key]
  (when (xt/x:nil? callbacks)
    (:= callbacks {}))
  (var result-fn
       (fn [result]
          (var f (xt/x:get-key callbacks key))
         (if (xt/x:not-nil? f)
           (return (f result))
           (return result))))
  (return result-fn))

(defn.xt connect
  "connects to a database"
  {:added "4.0"}
  [m cb]
  (when (xt/x:nil? m)
    (:= m {}))
  (if (xt/x:nil? cb)
    (if (sqlrt/legacy-driver? m)
      (do (var constructor (xt/x:get-key m "constructor"))
          (var out (constructor m nil))
          (if (promise/x:promise-native? out)
            (return
             (promise/x:promise-then out
                                     (fn [conn]
                                       (return (sqlrt/connection-coerce conn)))))
            (return (sqlrt/connection-coerce out))))
      (return
       (sqlrt/driver-connect
        (sqlrt/driver-coerce m)
        m)))
    (do (var success-fn (-/wrap-callback cb "success"))
        (var error-fn   (-/wrap-callback cb "error"))
        (var promise
             (sqlrt/driver-connect
              (sqlrt/driver-coerce m)
              m))
        (return
         (promise/x:promise-catch
          (promise/x:promise-then promise
                                  (fn [conn]
                                    (return (success-fn conn))))
          (fn [err]
            (return (error-fn err))))))))

(defn.xt disconnect
  "disconnects form database"
  {:added "4.0"}
  [conn cb]
  (var out (sqlrt/connection-disconnect
            (sqlrt/connection-coerce conn)))
  (if (xt/x:nil? cb)
    (return out)
    (do (var success-fn (-/wrap-callback cb "success"))
        (var error-fn   (-/wrap-callback cb "error"))
        (if (promise/x:promise-native? out)
          (return
           (promise/x:promise-catch
            (promise/x:promise-then out
                                    (fn [res]
                                      (return (success-fn res))))
            (fn [err]
              (return (error-fn err)))))
          (return (success-fn out))))))

(defn.xt query-base
  "calls query without the wrapper"
  {:added "4.0"}
  [conn raw]
  (return (sqlrt/connection-query
           (sqlrt/connection-coerce conn)
           raw)))

(defn.xt query
  "sends a query"
  {:added "4.0"}
  [conn raw cb]
  (var out (sqlrt/connection-query
            (sqlrt/connection-coerce conn)
            raw))
  (if (xt/x:nil? cb)
    (return out)
    (do (var success-fn (-/wrap-callback cb "success"))
        (var error-fn   (-/wrap-callback cb "error"))
        (if (promise/x:promise-native? out)
          (return
           (promise/x:promise-catch
            (promise/x:promise-then out
                                    (fn [res]
                                      (return (success-fn res))))
            (fn [err]
              (return (error-fn err)))))
          (return (success-fn out))))))

(defn.xt query-sync
  "sends a synchronous query"
  {:added "4.0"}
  [conn raw]
  (return (sqlrt/connection-query-sync
           (sqlrt/connection-coerce conn)
           raw)))
