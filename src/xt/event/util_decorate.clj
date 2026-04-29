(ns xt.event.util-decorate
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as spec-promise]]})

(defspec.xt incr-fn
  [:fn [] [:fn [] :xt/str]])

(defspec.xt plugin-timing
  [:fn [:xt/any] :xt/any])

(defspec.xt plugin-counts
  [:fn [:xt/any] :xt/any])

(defspec.xt to-handle-callback
  [:fn [[:xt/maybe :xt/any]] :xt/any])

(defspec.xt promise-wrap
  [:fn [:xt/any] :xt/promise])

(defspec.xt new-handle
  [:fn [[:fn [:xt/any] :xt/any] [:xt/array [:fn [:xt/any] :xt/any]] [:xt/maybe :xt/any]] :xt/any])

(defspec.xt run-handle
  [:fn [:xt/any [:xt/maybe [:xt/array :xt/any]] [:xt/maybe :xt/any]] :xt/promise])

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
  {:added "4.1"}
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
                            :on-setup on-setup
                            :on-teardown on-teardown
                            :on-reset on-reset})))

(defn.xt plugin-counts
  "plugin counts"
  {:added "4.1"}
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
                            :on-success on-success
                            :on-error on-error
                            :on-reset on-reset})))

(defn.xt to-handle-callback
  "adapts a cb map to the handle callback"
  {:added "4.1"}
  [cb]
  (:= cb (:? (xt/x:nil? cb) {} cb))
  (return {:on-success (xt/x:get-key cb "success" nil)
           :on-error (xt/x:get-key cb "error" nil)
           :on-teardown (xt/x:get-key cb "finally" nil)}))

(defn.xt promise-wrap
  "normalises a value into the host promise interface"
  {:added "4.1"}
  [value]
  (return
   (spec-promise/x:promise
    (fn []
      (return value)))))

(defn.xt new-handle
  "creates a new handle"
  {:added "4.1"}
  [handler plugin-fns opts]
  (var #{create-fn
         wrap-fn
         dump-fn
         id-fn
         delay
         name} opts)
  (:= id-fn (:? (xt/x:nil? id-fn) (-/incr-fn) id-fn))
  (:= create-fn (:? (xt/x:nil? create-fn) (fn [x] (return x)) create-fn))
  (var handle (create-fn {"::" "handle"
                          :name name
                          :id-fn id-fn
                          :wrap-fn wrap-fn
                          :handler handler
                          :delay (:? (xt/x:nil? delay) 0 delay)}))
  (var plugins (xt/x:arr-map plugin-fns
                             (fn [f]
                               (return (f handle)))))
  (xt/x:set-key handle "plugins" plugins)
  (return handle))

(defn.xt run-handle
  "runs a handle and resolves to a receipt"
  {:added "4.1"}
  [handle args tcb]
  (var #{handler wrap-fn delay id-fn plugins} handle)
  (:= args (:? (xt/x:nil? args) [] args))
  (var tcbs (xt/x:arr-assign (xt/x:arr-clone plugins)
                             (:? (xt/x:nil? tcb) []
                                 (xt/x:is-array? tcb) tcb
                                 :else [tcb])))
  (when (xt/x:is-function? delay)
    (:= delay (delay)))
  (var receipt {:id (id-fn)})
  (var teardown-fn
       (fn []
         (xt/for:array [cb tcbs]
           (var on-teardown (xt/x:get-key cb "on_teardown"))
           (var name (xt/x:get-key cb "name"))
           (var output (xt/x:get-key cb "output"))
           (when (xt/x:not-nil? on-teardown)
             (on-teardown))
           (when (and (xt/x:not-nil? name)
                      (xt/x:not-nil? output))
             (xt/x:set-key receipt name output)))
         (return receipt)))
  (var run-fn
       (fn []
         (xt/for:array [cb tcbs]
           (var on-setup (xt/x:get-key cb "on_setup"))
           (when (xt/x:not-nil? on-setup)
             (on-setup args)))
         (var base-thunk
              (fn []
                (return (xt/x:apply handler args))))
         (var base-promise
              (:? (< 0 (:? (xt/x:nil? delay) 0 delay))
                  (spec-promise/x:with-delay delay base-thunk)
                  (spec-promise/x:promise base-thunk)))
         (return
          (spec-promise/x:promise-finally
           (spec-promise/x:promise-catch
            (spec-promise/x:promise-then
             base-promise
             (fn [ret]
               (xt/for:array [cb tcbs]
                 (var on-success (xt/x:get-key cb "on_success"))
                 (when (xt/x:not-nil? on-success)
                   (on-success ret)))
               (xt/x:set-key receipt "status" "success")
               (xt/x:set-key receipt "value" ret)
               (return receipt)))
            (fn [err]
              (xt/for:array [cb tcbs]
                (var on-error (xt/x:get-key cb "on_error"))
                (when (xt/x:not-nil? on-error)
                  (on-error err)))
              (xt/x:set-key receipt "status" "error")
              (xt/x:set-key receipt "error" err)
              (return
               (spec-promise/x:promise
                (fn []
                  (xt/x:throw receipt))))))
           teardown-fn))))
  (var proc (:? (xt/x:not-nil? wrap-fn)
                (wrap-fn run-fn args receipt handle)
                (run-fn)))
  (return (-/promise-wrap proc)))
