(ns code.test.checker.common
  (:require [std.lib :as h]
            [std.string :as str]
            [std.lib.result :as res])
  (:import (java.util.regex Pattern)))

(defn function-string
  "returns the string representation of a function
 
   (function-string every?) => \"every?\"
 
   (function-string reset!) => \"reset!\""
  {:added "3.0"}
  ([func]
   (-> (type func)
       str
       (str/split #"\$")
       last
       h/demunge)))

(defrecord Checker [fn]
  Object
  (toString [{:keys [expect tag]}]
    (str "#" (name tag) (cond (coll? expect)
                              expect

                              (fn? expect)
                              (str "<" (function-string expect) ">")

                              :else
                              (str "<" expect ">"))))

  clojure.lang.IFn
  (invoke [ck data] (let [func (:fn ck)] (func data))))

(defmethod print-method Checker
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defn checker
  "creates a 'code.test.checker.common.Checker' object
 
   (checker {:tag :anything :fn (fn [x] true)})
   => code.test.checker.common.Checker"
  {:added "3.0"}
  ([m]
   (map->Checker m)))

(defn checker?
  "checks to see if a datastructure is a 'code.test.checker.common.Checker'
 
   (checker? (checker {:tag :anything :fn (fn [x] true)}))
   => true"
  {:added "3.0"}
  ([x]
   (instance? Checker x)))

(defn verify
  "verifies a value with it's associated check
 
   (verify (satisfies 2) 1)
   => (contains-in {:status :success
                    :data false
                    :checker {:tag :satisfies
                              :doc string?
                              :expect 2}
                    :actual 1
                    :from :verify})
 
   (verify (->checker #(/ % 0)) 1)
   => (contains {:status :exception
                 :data java.lang.ArithmeticException
                 :from :verify})"
  {:added "3.0"}
  ([ck result]
   (let [out (try
               {:type :code/test :status :success :data (ck result)}
               (catch Throwable t
                 {:type :code/test :status :exception :data t}))]
     (res/result (assoc out :checker ck :actual result :from :verify)))))

(defn succeeded?
  "determines if the results of a check have succeeded
 
   (-> (satisfies Long)
       (verify 1)
       succeeded?)
   => true
 
   (-> (satisfies even?)
       (verify 1)
       succeeded?)
   => false"
  {:added "3.0"}
  ([{:keys [status data]}]
   (and (= :success status)
        (= true data))))

(defn throws
  "checker that determines if an exception has been thrown
 
   ((throws Exception \"Hello There\")
    (res/result
     {:status :exception
      :data (Exception. \"Hello There\")}))
   => true"
  {:added "3.0"}
  ([]  (throws Throwable))
  ([e] (throws e nil))
  ([e msg]
   (checker
    {:tag :throws
     :doc "Checks if an exception has been thrown"
     :fn (fn [{:keys [data status]}]
           (and (= :exception status)
                (instance? e data)
                (if msg
                  (= msg (.getMessage ^Throwable data))
                  true)))
     :expect {:exception e :message msg}})))

(defn exactly
  "checker that allows exact verifications
 
   ((exactly 1) 1) => true
 
   ((exactly Long) 1) => false
 
   ((exactly number?) 1) => false"
  {:added "3.0"}
  ([v]
   (exactly v identity))
  ([v function]
   (checker
    {:tag :exactly
     :doc "Checks if the result exactly satisfies the condition"
     :fn (fn [res] (= (function (res/result-data res)) v))
     :expect v})))

(defn approx
  "checker that allows approximate verifications
 
   ((approx 1) 1.000001) => true
 
   ((approx 1) 1.1) => false
 
   ((approx 1 0.0000001) 1.001) => false"
  {:added "3.0"}
  ([v]
   (approx v 0.001))
  ([v threshold]
   (checker
    {:tag :approx
     :doc "Checks if the result is approximately the given value"
     :fn (fn [res] (< (- v threshold) (res/result-data res) (+ v threshold)))
     :expect v})))

(defn satisfies
  "checker that allows loose verifications
 
   ((satisfies 1) 1) => true
 
   ((satisfies Long) 1) => true
 
   ((satisfies number?) 1) => true
 
   ((satisfies #{1 2 3}) 1) => true
 
   ((satisfies [1 2 3]) 1) => false
 
   ((satisfies number?) \"e\") => false
 
   ((satisfies #\"hello\") #\"hello\") => true"
  {:added "3.0"}
  ([v]
   (satisfies v identity))
  ([v function]
   (checker
    {:tag :satisfies
     :doc "Checks if the result can satisfy the condition:"
     :fn (fn [res]
           (let [data (function (res/result-data res))]
             (cond (= data v) true

                   (class? v) (instance? v data)

                   (and (h/comparable? v data)
                        (zero? (compare v data)))
                   true

                   (map? v) (= (into {} data) v)

                   (vector? v) (= data v)

                   (ifn? v) (boolean (v data))

                   (h/regexp? v)
                   (cond (h/regexp? data)
                         (= (.pattern ^Pattern v)
                            (.pattern ^Pattern data))

                         (string? data)
                         (boolean (re-find v data))

                         :else false)

                   :else false)))
     :expect v})))

(defn stores
  "a checker that looks into a ref object
 
   ((stores 1) (volatile! 1)) => true
 
   ((stores 1) 1) => false"
  {:added "3.0"}
  ([v]
   (checker
    {:tag :stores
     :doc "Checks if the result is a ref with "
     :fn (fn [res]
           (if (h/ideref? res)
             ((satisfies v) (deref res))
             false))
     :expect v})))

(defn anything
  "a checker that returns true for any value
 
   (anything nil) => true
 
   (anything [:hello :world]) => true"
  {:added "3.0"}
  ([x]
   ((satisfies h/T) x)))

(defn ->checker
  "creates a 'satisfies' checker if not already a checker
 
   ((->checker 1) 1) => true
 
   ((->checker (exactly 1)) 1) => true"
  {:added "3.0"}
  ([x]
   (cond (checker? x)
         x

         (= x 'any)
         (satisfies anything)

         :else
         (satisfies x))))

(defmacro capture
  "adds a form to capture test input"
  {:added "3.0"}
  ([]
   `(capture nil ~'$0))
  ([checker]
   `(capture ~checker ~'$0))
  ([checker sym]
   `(fn [~'x]
      (intern *ns* (quote ~(with-meta sym {:dynamic true})) ~'x)
      (or ~(if checker `(~checker ~'x) true)))))
