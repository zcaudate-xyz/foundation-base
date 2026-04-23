(ns xt.lang.common-task
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defspec.xt Task
  :xt/any)

(defspec.xt task-run
  [:fn [[:fn [] :xt/any]] Task])

(defspec.xt task-then
  [:fn [Task [:fn [:xt/any] :xt/any]] Task])

(defspec.xt task-catch
  [:fn [Task [:fn [:xt/any] :xt/any]] Task])

(defspec.xt task-finally
  [:fn [Task [:fn [] :xt/any]] Task])

(defspec.xt task-cancel
  [:fn [Task] Task])

(defspec.xt task-status
  [:fn [Task] :xt/str])

(defspec.xt task-await
  [:fn [Task [:xt/maybe :xt/num] [:xt/maybe :xt/any]] :xt/any])

(defspec.xt task-from-async
  [:fn [[:fn [[:fn [:xt/any] :xt/any]
               [:fn [:xt/any] :xt/any]] :xt/any]]
   Task])

(defn.xt task-pending?
  "returns the task executor when the value is a task"
  {:added "4.0"}
  [value]
  (when (and (xt/x:is-object? value)
             (== "task"
                 (xt/x:get-key value "::")))
    (return (xt/x:get-key value "executor")))
  (return nil))

(defn.xt task-follow
  "resolves a value, flattening nested tasks"
  {:added "4.0"}
  [value resolve reject]
  (var executor (-/task-pending? value))
  (if (xt/x:not-nil? executor)
    (executor resolve reject)
    (resolve value)))

(defn.xt task-complete
  "completes a task and drains listeners"
  {:added "4.0"}
  [task status payload]
  (when (== "pending" (xt/x:get-key task "status"))
    (xt/x:set-key task "status" status)
    (if (== "ok" status)
      (xt/x:set-key task "value" payload)
      (xt/x:set-key task "error" payload))
    (var listeners (xt/x:get-key task "listeners"))
    (xt/x:set-key task "listeners" [])
    (xt/for:array [entry listeners]
      (var [on-ok on-err] entry)
      (if (== "ok" status)
        (on-ok payload)
        (on-err payload))))
  (return task))

(defn.xt task-listen
  "attaches listeners to a task"
  {:added "4.0"}
  [task on-ok on-err]
  (var status (xt/x:get-key task "status"))
  (cond (== "ok" status)
        (on-ok (xt/x:get-key task "value"))

        (== "error" status)
        (on-err (xt/x:get-key task "error"))

        :else
        (xt/x:arr-push (xt/x:get-key task "listeners")
                       [on-ok on-err]))
  (return task))

(defn.xt task-from-async
  "bridges callback-style async into a task"
  {:added "4.0"}
  [executor]
  (var task {"::" "task"
             :status "pending"
             :value nil
             :error nil
             :listeners []
             :cancel-fn nil
             :executor executor})
  (fn resolve [value]
    (-/task-complete task "ok" value))
  (fn reject [error]
    (-/task-complete task "error" error))
  (xt/for:try [[cancel-fn err] (executor resolve reject)]
    {:success (when (xt/x:is-function? cancel-fn)
                (xt/x:set-key task "cancel-fn" cancel-fn))
     :error   (reject err)})
  (return task))

(defn.xt task-run
  "runs a thunk as a task handle"
  {:added "4.0"}
  [thunk]
  (return
   (-/task-from-async
    (fn [resolve reject]
      (xt/for:try [[out err] (thunk)]
        {:success (-/task-follow out resolve reject)
         :error   (reject err)})))))

(defn.xt task-then
  "chains success continuation on a task"
  {:added "4.0"}
  [task on-ok]
  (return
   (-/task-from-async
    (fn [resolve reject]
      (-/task-listen
       task
       (fn [value]
         (xt/for:try [[out err] (on-ok value)]
           {:success (-/task-follow out resolve reject)
            :error   (reject err)}))
       reject)))))

(defn.xt task-catch
  "chains error continuation on a task"
  {:added "4.0"}
  [task on-err]
  (return
   (-/task-from-async
    (fn [resolve reject]
      (-/task-listen
       task
       resolve
       (fn [error]
         (xt/for:try [[out next-error] (on-err error)]
           {:success (-/task-follow out resolve reject)
            :error   (reject next-error)})))))))

(defn.xt task-finally
  "chains completion continuation on a task"
  {:added "4.0"}
  [task on-done]
  (return
   (-/task-from-async
    (fn [resolve reject]
      (-/task-listen
       task
       (fn [value]
         (xt/for:try [[_ err] (on-done)]
           {:success (resolve value)
            :error   (reject err)}))
       (fn [error]
         (xt/for:try [[_ done-error] (on-done)]
           {:success (reject error)
            :error   (reject done-error)})))))))

(defn.xt task-cancel
  "cancels task cooperatively"
  {:added "4.0"}
  [task]
  (var cancel-fn (xt/x:get-key task "cancel-fn"))
  (when (xt/x:is-function? cancel-fn)
    (cancel-fn))
  (return task))

(defn.xt task-status
  "gets task status"
  {:added "4.0"}
  [task]
  (return (xt/x:get-key task "status")))

(defn.xt task-await
  "awaits task result where supported"
  {:added "4.0"}
  ([task timeout-ms default]
   (var status (-/task-status task))
   (cond (== "ok" status)
         (return (xt/x:get-key task "value"))

         (== "error" status)
         (xt/x:err (xt/x:get-key task "error"))

         :else
         (return default))))
