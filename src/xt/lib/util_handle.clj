(ns xt.lang.util-handle
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt incr-fn
  []
  (var i -1)
  (var next-id-fn
       (fn []
         (:= i (+ i 1))
         (return (xt/x:cat "id-" (xt/x:to-string i)))))
  (return next-id-fn))

(defn.xt plugin-timing
  "plugin timing"
  {:added "4.0"}
  [handle]
  (var result {:output {}})
  (var on-setup
       (fn [args]
         (xt/x:set-key result "output" {:start (xt/x:now-ms)})))
  (var on-teardown
       (fn []
         (var output (xt/x:get-key result "output"))
         (var t-end (xt/x:now-ms))
         (var t-elapsed (- t-end (xt/x:get-key output "start")))
         (xt/x:set-key output "end" t-end)
         (xt/x:set-key output "elapsed" t-elapsed)))
  (var on-reset
       (fn []
         (xt/x:set-key result "output" {})))
  (return (xt/x:obj-assign result
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
         (xt/x:set-key output "success"
                    (+ 1 (xt/x:get-key output "success")))))
  (var on-error
       (fn [ret]
         (var #{output} result)
         (xt/x:set-key output "error"
                    (+ 1 (xt/x:get-key output "error")))))
  (var on-reset
       (fn []
         (xt/x:set-key result "output" {:success 0
                                     :error 0})))
  (return (xt/x:obj-assign result
                        {:name "counts"
                         :on-success  on-success
                         :on-error    on-error
                         :on-reset    on-reset})))

(defn.xt to-handle-callback
  "adapts a cb map to the handle callback"
  {:added "4.0"}
  [cb]
  (:= cb (:? (xt/x:nil? cb) {} cb))
  (return {:on-success  (xt/x:get-key cb "success")
           :on-error    (xt/x:get-key cb "error")
           :on-teardown (xt/x:get-key cb "finally")}))

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
  (:= id-fn     (:? (xt/x:nil? id-fn) (-/incr-fn) id-fn))
  (:= create-fn (:? (xt/x:nil? create-fn) (fn [x] (return x)) create-fn))
  (var handle (create-fn {"::" "handle"
                         :name    name
                         :id-fn   id-fn
                         :wrap-fn wrap-fn
                         :handler handler
                         :delay   (:? (xt/x:nil? delay) 0 delay)}))
  (var plugins  (xt/x:arr-map plugin-fns
                           (fn [f] (return (f handle)))))
  (xt/x:set-key handle "plugins" plugins)
  (return handle))

(defn.xt run-handle
  "runs a handle"
  {:added "4.0"}
  [handle args tcb]
  (var #{handler wrap-fn delay id-fn plugins} handle)
  (var tcbs (xt/x:arr-assign (xt/x:arr-clone plugins)
                             (:? (xt/x:nil? tcb) []
                                 (xt/x:is-array? tcb) tcb
                                 :else [tcb])))
  (when (xt/x:is-function? delay) (:= delay (delay)))
  (var receipt  {:id (id-fn)})
  (var teardown-fn
       (fn []
           (xt/for:array [cb tcbs]
              (var on_teardown (xt/x:get-key cb "on_teardown"))
              (var name (xt/x:get-key cb "name"))
              (var output (xt/x:get-key cb "output"))
              (when (xt/x:not-nil? on_teardown)
                (on_teardown))
              (when (and (xt/x:not-nil? name)
                         (xt/x:not-nil? output))
                (xt/x:set-key receipt name output)))))
  (var call-fn
        (fn []
           (xt/for:async [[ret err] (xt/x:apply handler args)]
             {:success (do
                          (xt/for:array [cb tcbs]
                            (var on_success (xt/x:get-key cb "on_success"))
                            (when (xt/x:not-nil? on_success)
                              (on_success ret)))
                          (teardown-fn)
                          (return ret))
              :error   (do
                          (xt/for:array [cb tcbs]
                            (var on_error (xt/x:get-key cb "on_error"))
                            (when (xt/x:not-nil? on_error)
                              (on_error err)))
                          (teardown-fn)
                          (xt/x:throw err))})))
  (var run-fn
        (fn []
             (xt/for:array [cb tcbs]
               (var on_setup (xt/x:get-key cb "on_setup"))
                (when (xt/x:not-nil? on_setup)
                  (on_setup args)))
             (if (< 0 (:? (xt/x:nil? delay) 0 delay))
               (do (var delayed-run-fn
                        (fn []
                          (return (call-fn))))
                   (xt/x:with-delay delayed-run-fn delay))
                (return (call-fn)))))
  (var proc (:? (xt/x:not-nil? wrap-fn)
                (wrap-fn run-fn args receipt handle)
                (run-fn)))
  (return [receipt proc]))
