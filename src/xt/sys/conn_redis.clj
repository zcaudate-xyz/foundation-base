(ns xt.sys.conn-redis
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as xtl]]})

(defn.xt connect
  "connects to a datasource"
  {:added "4.0"}
  [m cb]
  (var #{constructor} m)
  (var success-fn (xtl/wrap-callback cb "success"))
  (var error-fn   (xtl/wrap-callback cb "error"))
  (xt/for:return [[conn err] (constructor m (xt/x:callback))]
    {:success (return (success-fn conn))
     :error   (return (error-fn err))
     :final   true}))

(defn.xt disconnect
  "disconnect redis"
  {:added "4.0"}
  [conn cb]
  (var disconnect-fn (xt/x:get-key conn "::disconnect"))
  (var success-fn (xtl/wrap-callback cb "success"))
  (var error-fn   (xtl/wrap-callback cb "error"))
  (xt/for:return [[res err] (disconnect-fn (xt/x:callback))]
    {:success (return (success-fn res))
     :error   (return (error-fn err))
     :final   true}))

(defn.xt exec
  "executes a redis command"
  {:added "4.0"}
  [conn command args cb]
  (var exec-fn (xt/x:get-key conn "::exec"))
  (var success-fn (xtl/wrap-callback cb "success"))
  (var error-fn   (xtl/wrap-callback cb "error"))
  (xt/for:return [[conn err] (exec-fn command args (xt/x:callback))]
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
  (return (-/exec conn "eval" [body "0" (xt/x:unpack args)]
                  cb)))

(defn.xt eval-script
  "evaluates sha, then body if errored"
  {:added "4.0"}
  [conn script args cb]
  (var #{sha body} script)
  (var err-fn
       (fn [err]
          (:= body (:? (xt/x:is-function? body) (body) body))
          (return (-/exec conn "eval" [body "0" (xt/x:unpack args)]
                          cb))))
  (return (-/exec conn "evalsha" [sha "0" (xt/x:unpack args)]
                  {:success (xtl/wrap-callback cb "success")
                   :error   err-fn})))

(comment
  (./create-tests)
  (./create-tests)
  )
