(ns xt.sys.conn-redis
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(defn.xt connect
  "connects to a datasource"
  {:added "4.0"}
  [m cb]
  (var #{constructor} m)
  (var success-fn (k/wrap-callback cb "success"))
  (var error-fn   (k/wrap-callback cb "error"))
  (k/for:return [[conn err] (constructor m (x:callback))]
    {:success (return (success-fn conn))
     :error   (return (error-fn err))
     :final   true}))

(defn.xt disconnect
  "disconnect redis"
  {:added "4.0"}
  [conn cb]
  (var disconnect-fn (k/get-key conn "::disconnect"))
  (var success-fn (k/wrap-callback cb "success"))
  (var error-fn   (k/wrap-callback cb "error"))
  (k/for:return [[res err] (disconnect-fn (x:callback))]
    {:success (return (success-fn res))
     :error   (return (error-fn err))
     :final   true}))

(defn.xt exec
  "executes a redis command"
  {:added "4.0"}
  [conn command args cb]
  (var exec-fn (k/get-key conn "::exec"))
  (var success-fn (k/wrap-callback cb "success"))
  (var error-fn   (k/wrap-callback cb "error"))
  (k/for:return [[conn err] (exec-fn command args (x:callback))]
    {:success (return (success-fn conn))
     :error   (return (error-fn err))
     :final   true}))

(defn.xt create-subscription
  "creates a subscription given channel"
  {:added "4.0"}
  [conn channels cb]
  (return (-/exec conn "subscribe" channels cb)))

(defn.xt create-psubscription
  "creates a pattern subscription given channel"
  {:added "4.0"}
  [conn channels cb]
  (return (-/exec conn "psubscribe" channels cb)))

(defn.xt eval-body
  "evaluates a the body"
  {:added "4.0"}
  [conn script args cb]
  (var #{sha body} script)
  (return (-/exec conn "eval" [body "0" (k/unpack args)]
                  cb)))

(defn.xt eval-script
  "evaluates sha, then body if errored"
  {:added "4.0"}
  [conn script args cb]
  (var #{sha body} script)
  (var err-fn
       (fn [err]
         (:= body (:? (k/fn? body) (body) body))
         (return (-/exec conn "eval" [body "0" (k/unpack args)]
                         cb))))
  (return (-/exec conn "evalsha" [sha "0" (k/unpack args)]
                  {:success (k/wrap-callback cb "success")
                   :error   err-fn})))

(comment
  (./create-tests)
  (./create-tests)
  )


