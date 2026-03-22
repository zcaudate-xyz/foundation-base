(ns code.test.compile.types
  (:require [code.test.base.runtime :as rt]
            [std.lib.collection]
            [std.lib.env]
            [std.lib.foundation]
            [std.lib.future :as f]
            [std.lib.impl :refer [defimpl]]
            [std.lib.time :as time]
            [std.math :as math]))


(def ^:dynamic *compile-meta* nil)

(def ^:dynamic *compile-desc* nil)

(def ^:dynamic *file-path* nil)

(def +type+
  #{:core :template})

(defmulti fact-invoke
  "invokes a fact object"
  {:added "3.0" :guard true}
  :type)

(defn fact-display-info
  "displays a fact"
  {:added "4.0"}
  ([m]
   (dissoc m :path :eval :full :code :wrap :function :setup :teardown)))

(defn fact-display
  "displays a fact"
  {:added "3.0"}
  ([m]
   (str (->> (fact-display-info m)
             (std.lib.collection/filter-vals identity)))))

(defn- fact-string
  [m]
  (str "#fact " (fact-display m)))

(defimpl Fact [type id ns path refer desc column line source global]
  :type defrecord
  :invoke [fact-invoke 1]
  :string fact-string
  :final true)

(defn fact?
  "checks if object is a fact"
  {:added "3.0"}
  ([obj]
   (instance? Fact obj)))

(defmethod fact-invoke :template
  [_])

(defmethod fact-invoke :core
  ([{:keys [wrap function guard] :as m}]
   (let [{:keys [ceremony check]} wrap
         result ((-> function :thunk ceremony check))]
     (if (and guard (not result))
       (std.lib.foundation/error "Guard failed" (fact-display-info m)))
     result)))


(comment
  f       (-> (f/future:call (fn []
                               (std.lib.env/prn "RUN TEST: " (Thread/currentThread))
                               (execution-fn)))
              (f/future:timeout timeout))

  (-> (std.lib.future/future (Thread/sleep 1000))
      (std.lib.env/do:prn)
      (std.lib.future/future:timeout 10)
      (std.lib.env/do:prn)
      (std.lib.future/future:cancel)
      (std.lib.env/do:prn)))
