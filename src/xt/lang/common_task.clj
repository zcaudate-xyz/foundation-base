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

(defn.xt task-run
  "runs a thunk as a task handle"
  {:added "4.0"}
  [thunk]
  (try
    (var out (thunk))
    (return
     (:? (and (xt/x:is-object? out)
              (xt/x:is-function? (. out ["then"])))
         out
         (new Promise
              (fn [resolve reject]
                (resolve out)))))
    (catch err
      (return
       (new Promise
            (fn [resolve reject]
              (reject err)))))))

(defn.xt task-then
  "chains success continuation on a task"
  {:added "4.0"}
  [task on-ok]
  (return
   (:? (and (xt/x:is-object? task)
            (xt/x:is-function? (. task ["then"])))
       (. task (then on-ok))
       (new Promise
            (fn [resolve reject]
              (try
                (resolve (on-ok task))
                (catch err
                  (reject err))))))))

(defn.xt task-catch
  "chains error continuation on a task"
  {:added "4.0"}
  [task on-err]
  (return
   (:? (and (xt/x:is-object? task)
            (xt/x:is-function? (. task ["catch"])))
       (. task (catch on-err))
       (new Promise
            (fn [resolve reject]
              (resolve task))))))

(defn.xt task-finally
  "chains completion continuation on a task"
  {:added "4.0"}
  [task on-done]
  (return
   (:? (and (xt/x:is-object? task)
            (xt/x:is-function? (. task ["finally"])))
       (. task (finally on-done))
       (new Promise
            (fn [resolve reject]
              (try
                (on-done)
                (resolve task)
                (catch err
                  (reject err))))))))

(defn.xt task-cancel
  "cancels task cooperatively"
  {:added "4.0"}
  [task]
  (return task))

(defn.xt task-status
  "gets task status"
  {:added "4.0"}
  [task]
  (return
   (:? (and (xt/x:is-object? task)
            (xt/x:is-function? (. task ["then"])))
       "pending"
       "done")))

(defn.xt task-await
  "awaits task result where supported"
  {:added "4.0"}
  ([task]
   (return task))
  ([task timeout-ms default]
   (return task)))

(defn.xt task-from-async
  "bridges callback-style async into a task"
  {:added "4.0"}
  [executor]
  (return
   (new Promise
        (fn [resolve reject]
          (executor resolve reject)))))
