(ns xt.lang.util-handle
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(defn-.xt incr-fn
  []
  (var i -1)
  (return (fn []
            (:= i (+ i 1))
            (return (k/cat "id-" i)))))

(defn.xt plugin-timing
  "plugin timing"
  {:added "4.0"}
  [handle]
  (var result {:output {}})
  (var on-setup
       (fn [args]
         (k/set-key result "output" {:start (k/now-ms)})))
  (var on-teardown
       (fn []
         (var output (k/get-key result "output"))
         (var t-end (k/now-ms))
         (var t-elapsed (- t-end (k/get-key output "start")))
         (k/set-key output "end" t-end)
         (k/set-key output "elapsed" t-elapsed)))
  (var on-reset
       (fn []
         (k/set-key result "output" {})))
  (return (k/obj-assign result
                        {:name "timing"
                         :on-setup    on-setup
                         :on-teardown on-teardown
                         :on-reset    on-reset})))

(defn.xt plugin-counts
  "plugin counts"
  {:added "4.0"}
  [handle]
  (var result {:output {:success 0
                        :error 0}})
  (var on-success
       (fn [ret]
         (var #{output} result)
         (k/set-key output "success"
                    (+ 1 (k/get-key output "success")))))
  (var on-error
       (fn [ret]
         (var #{output} result)
         (k/set-key output "error"
                    (+ 1 (k/get-key output "error")))))
  (var on-reset
       (fn []
         (k/set-key result "output" {:success 0
                                     :error 0})))
  (return (k/obj-assign result
                        {:name "counts"
                         :on-success  on-success
                         :on-error    on-error
                         :on-reset    on-reset})))

(defn.xt to-handle-callback
  "adapts a cb map to the handle callback"
  {:added "4.0"}
  [cb]
  (:= cb (or cb {}))
  (return {:on-success  (k/get-key cb "success")
           :on-error    (k/get-key cb "error")
           :on-teardown (k/get-key cb "finally")}))

(defn.xt new-handle
  "creates a new handle"
  {:added "4.0"}
  [handler plugin-fns opts]
  (var #{create-fn
         wrap-fn
         dump-fn
         id-fn
         delay
         name} opts)
  (:= id-fn     (or id-fn (-/incr-fn)))
  (:= create-fn (or create-fn k/identity))
  (var handle (create-fn {"::" "handle"
                        :name    name
                        :id-fn   id-fn
                        :wrap-fn wrap-fn
                        :handler handler
                        :delay   (or delay 0)}))
  (var plugins  (k/arr-map plugin-fns
                           (fn [f] (return (f handle)))))
  (k/set-key handle "plugins" plugins)
  (return handle))

(defn.xt run-handle
  "runs a handle"
  {:added "4.0"}
  [handle args tcb]
  (var #{handler wrap-fn delay id-fn plugins} handle)
  (var tcbs (k/arr-append (k/arr-clone plugins)
                         (:? (k/nil? tcb) []
                             (k/arr? tcb) tcb
                             :else [tcb])))
  (when (k/fn? delay) (:= delay (delay)))
  (var receipt  {:id (id-fn)})
  (var teardown-fn
       (fn []
         (k/for:array [cb tcbs]
           (var #{on-teardown
                  name
                  output} cb)
           (when on-teardown
             (on-teardown))
           (when (and name output)
             (k/set-key receipt name output)))))
  (var call-fn
       (fn []
         (return (k/for:async [[ret err] (handler args)]
                   {:success (do
                               (k/for:array [cb tcbs]
                                 (var #{on-success} cb)
                                 (when on-success
                                   (var output (on-success ret))))
                               (teardown-fn)
                               (return ret))
                    :error   (do
                               (k/for:array [cb tcbs]
                                 
                                 (var #{on-error} cb)
                                 (when on-error
                                   (on-error err)))
                               (teardown-fn)
                               (k/throw err))
                    :finally (do )}))))
  (var run-fn
       (fn []
         (k/for:array [cb tcbs]
           (var #{on-setup} cb)
           (when on-setup
             (on-setup args)))
         (if (< 0 (or delay 0))
           (return (k/with-delay call-fn delay))
           (return (call-fn)))))
  (var proc (:? wrap-fn
                (wrap-fn run-fn args receipt handle)
                (run-fn)))
  (return [receipt proc]))

