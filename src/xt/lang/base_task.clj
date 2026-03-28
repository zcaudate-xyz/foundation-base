(ns xt.lang.base-task
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {})

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
  (return (x:task-run thunk)))

(defn.xt task-then
  "chains success continuation on a task"
  {:added "4.0"}
  [task on-ok]
  (return (x:task-then task on-ok)))

(defn.xt task-catch
  "chains error continuation on a task"
  {:added "4.0"}
  [task on-err]
  (return (x:task-catch task on-err)))

(defn.xt task-finally
  "chains completion continuation on a task"
  {:added "4.0"}
  [task on-done]
  (return (x:task-finally task on-done)))

(defn.xt task-cancel
  "cancels task cooperatively"
  {:added "4.0"}
  [task]
  (return (x:task-cancel task)))

(defn.xt task-status
  "gets task status"
  {:added "4.0"}
  [task]
  (return (x:task-status task)))

(defn.xt task-await
  "awaits task result where supported"
  {:added "4.0"}
  ([task]
   (return (x:task-await task nil nil)))
  ([task timeout-ms default]
   (return (x:task-await task timeout-ms default))))

(defn.xt task-from-async
  "bridges callback-style async into a task"
  {:added "4.0"}
  [executor]
  (return (x:task-from-async executor)))
