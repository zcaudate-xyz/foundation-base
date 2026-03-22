(ns code.test.compile.types
  (:require [code.test.base.runtime :as rt]
            [std.lib.collection :as collection]
            [std.lib.env :as env]
            [std.lib.foundation]
            [std.lib.future :as f]
            [std.lib.impl :as impl]
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
             (collection/filter-vals identity)))))

(defn- fact-string
  [m]
  (str "#fact " (fact-display m)))

(impl/defimpl Fact [type id ns path refer desc column line source global]
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
                               (env/prn "RUN TEST: " (Thread/currentThread))
                               (execution-fn)))
              (f/future:timeout timeout))

  (-> (f/future (Thread/sleep 1000))
      (env/do:prn)
      (f/future:timeout 10)
      (env/do:prn)
      (f/future:cancel)
      (env/do:prn)))
