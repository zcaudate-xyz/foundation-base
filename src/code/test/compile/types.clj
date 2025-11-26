(ns code.test.compile.types
  (:require [std.math :as math]
            [code.test.base.runtime :as rt]
            [std.lib :as h :refer [defimpl]]
            [std.lib.future :as f]
            [std.lib.time :as time]))


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
   (dissoc m :path :eval :full :code :wrap
                     :function :setup :let :teardown :use)))

(defn fact-display
  "displays a fact"
  {:added "3.0"}
  ([m]
   (str (->> (fact-display-info m)
             (h/filter-vals identity)))))

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
       (h/error "Guard failed" (fact-display-info m)))
     result)))


(comment
  f       (-> (f/future:call (fn []
                             (h/prn "RUN TEST: " (Thread/currentThread))
                                      (execution-fn)))
                     (f/future:timeout timeout
                                       ))
  
  (-> (h/future (Thread/sleep 1000))
      (h/do:prn)
      (h/future:timeout 10)
      (h/do:prn)
      (h/future:cancel)
      (h/do:prn)))
