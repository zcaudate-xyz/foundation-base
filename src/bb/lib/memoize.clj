(ns bb.lib.memoize
  (:require [bb.protocol-invoke :as protocol.invoke]
            [bb.lib.impl :refer [defimpl]]
            [bb.lib.invoke :as invoke :refer [definvoke]]
            [bb.lib.function :as fn])
  (:refer-clojure :exclude [memoize]))

(defonce ^:dynamic *registry* (atom {}))

(declare memoize:info
         memoize:invoke
         memoize:enabled?
         memoize:disabled?)

(defn- memoize-string
  ([mem]
   (str "#memoize" (memoize:info mem))))

(defprotocol IMemoize
  (call [_])
  (call [_ a])
  (call [_ a b])
  (call [_ a b c])
  (call [_ a b c d])
  (call [_ a b c d e])
  (call [_ a b c d e f])
  (call [_ a b c d e f g])
  (call [_ a b c d e f g h])
  (call [_ a b c d e f g h i])
  (call [_ a b c d e f g h i j])
  (call [_ a b c d e f g h i j k])
  (call [_ a b c d e f g h i j k l])
  (call [_ a b c d e f g h i j k l m])
  (call [_ a b c d e f g h i j k l m n])
  (call [_ a b c d e f g h i j k l m n o])
  (call [_ a b c d e f g h i j k l m n o p])
  (call [_ a b c d e f g h i j k l m n o p q])
  (call [_ a b c d e f g h i j k l m n o p q r])
  (call [_ a b c d e f g h i j k l m n o p q r s])
  (call [_ a b c d e f g h i j k l m n o p q r s t])
  (applyTo [_ args]))

(defrecord Memoize [function memfunction cache var registry status]
  IMemoize
  (call [_] (memfunction))
  (call [_ a] (memfunction a))
  (call [_ a b] (memfunction a b))
  (call [_ a b c] (memfunction a b c))
  (call [_ a b c d] (memfunction a b c d))
  (call [_ a b c d e] (memfunction a b c d e))
  (call [_ a b c d e f] (memfunction a b c d e f))
  (call [_ a b c d e f g] (memfunction a b c d e f g))
  (call [_ a b c d e f g h] (memfunction a b c d e f g h))
  (call [_ a b c d e f g h i] (memfunction a b c d e f g h i))
  (call [_ a b c d e f g h i j] (memfunction a b c d e f g h i j))
  (call [_ a b c d e f g h i j k] (memfunction a b c d e f g h i j k))
  (call [_ a b c d e f g h i j k l] (memfunction a b c d e f g h i j k l))
  (call [_ a b c d e f g h i j k l m] (memfunction a b c d e f g h i j k l m))
  (call [_ a b c d e f g h i j k l m n] (memfunction a b c d e f g h i j k l m n))
  (call [_ a b c d e f g h i j k l m n o] (memfunction a b c d e f g h i j k l m n o))
  (call [_ a b c d e f g h i j k l m n o p] (memfunction a b c d e f g h i j k l m n o p))
  (call [_ a b c d e f g h i j k l m n o p q] (memfunction a b c d e f g h i j k l m n o p q))
  (call [_ a b c d e f g h i j k l m n o p q r] (memfunction a b c d e f g h i j k l m n o p q r))
  (call [_ a b c d e f g h i j k l m n o p q r s] (memfunction a b c d e f g h i j k l m n o p q r s))
  (call [_ a b c d e f g h i j k l m n o p q r s t] (memfunction a b c d e f g h i j k l m n o p q r s t))
  (applyTo [_ args] (apply memfunction args)))

(defmethod print-method Memoize
  [v ^java.io.Writer w]
  (.write w (memoize-string v)))

(defn memoize
  "caches the result of a function
   (ns-unmap *ns* '+-inc-)
   (ns-unmap *ns* '-inc-)
   (def +-inc- (atom {}))
   (declare -inc-)
   (def -inc-  (memoize inc +-inc- #'-inc-))

   (-inc- 1) => 2
   (-inc- 2) => 3"
  {:added "3.0"}
  ([function cache var]
   (memoize function cache var *registry* (volatile! :enabled)))
  ([function cache var registry status]
   (let [memfunction (fn [& args]
                       (if-let [e (find @cache args)]
                         (val e)
                         (when-let [ret (apply function args)]
                           (swap! cache assoc args ret)
                           ret)))]
     (Memoize. function memfunction cache var registry status))))

(defn register-memoize
  "registers the memoize function

   (register-memoize -inc-)"
  {:added "3.0"}
  ([^Memoize mem]
   (let [var (.var mem)
         registry (.registry mem)]
     (register-memoize mem var registry)))
  ([^Memoize mem var registry]
   (swap! registry assoc var mem)))

(defn deregister-memoize
  "deregisters the memoize function

   (deregister-memoize -inc-)"
  {:added "3.0"}
  ([^Memoize mem]
   (let [var (.var mem)
         registry (.registry mem)]
     (deregister-memoize mem var registry)))
  ([^Memoize mem var registry]
   (swap! registry dissoc var)))

(defn registered-memoizes
  "lists all registered memoizes

   (registered-memoizes)"
  {:added "3.0"}
  ([] (registered-memoizes nil))
  ([status] (registered-memoizes status *registry*))
  ([status registry]
   (let [pred (case status
                :enabled  memoize:enabled?
                :disabled memoize:disabled?
                identity)]
     (keys (cond->> @registry
             status (keep (fn [[var mem]]
                            (if (memoize:disabled? mem)
                              [var mem])))
             :then (into {}))))))

(defn registered-memoize?
  "checks if a memoize function is registered

   (registered-memoize? -mem-)
   => false"
  {:added "3.0"}
  ([^Memoize mem]
   (let [var      (.var mem)
         registry (.registry mem)]
     (= mem (get @registry var)))))

(defn memoize:status
  "returns the status of the object

   (memoize:status -inc-)
   => :enabled"
  {:added "3.0"}
  ([^Memoize mem]
   @(.status mem)))

(defn memoize:info
  "formats the memoize object

   (def +-plus- (atom {}))
   (declare -plus-)
   (def -plus- (memoize + +-plus- #'-plus-))
   (memoize:info -plus-)
   => (contains {:status :enabled, :registered false, :items number?})
   ;; {:fn +, :cache #atom {(1 1) 2}}"
  {:added "3.0"}
  ([^Memoize mem]
   {:status (memoize:status mem)
    :registered (registered-memoize? mem)
    :items (count @(.cache mem))}))

(defn memoize:disable
  "disables the usage of the cache

   (memoize:disable -inc-)
   => :disabled"
  {:added "3.0"}
  ([^Memoize mem]
   (vreset! (.status mem) :disabled)))

(defn memoize:disabled?
  "checks if the memoized function is disabled

   (memoize:disabled? -inc-)
   => true"
  {:added "3.0"}
  ([^Memoize mem]
   (= @(.status mem) :disabled)))

(defn memoize:enable
  "enables the usage of the cache

   (memoize:enable -inc-)
   => :enabled"
  {:added "3.0"}
  ([^Memoize mem]
   (vreset! (.status mem) :enabled)))

(defn memoize:enabled?
  "checks if the memoized function is disabled

   (memoize:enabled? -inc-)
   => true"
  {:added "3.0"}
  ([^Memoize mem]
   (= @(.status mem) :enabled)))

(defn memoize:invoke
  "invokes the function with arguments

   (memoize:invoke -plus- 1 2 3)
   => 6"
  {:added "3.0"}
  ([^Memoize mem & args]
   (if (memoize:enabled? mem)
     (apply (.memfunction mem) args)
     (apply (.function mem) args))))

(defn memoize:remove
  "removes a cached result

   (memoize:remove -inc- 1)
   => 2"
  {:added "3.0"}
  ([^Memoize mem & args]
   (let [cache (.cache mem)
         v (get @cache args)]
     (swap! cache dissoc args)
     v)))

(defn memoize:clear
  "clears all results

   (memoize:clear -inc-)
   => '{(2) 3}"
  {:added "3.0"}
  ([^Memoize mem]
   (let [cache (.cache mem)
         v @cache]
     (reset! cache {})
     v)))

(defmethod bb.protocol-invoke/-invoke-intern :memoize
  [_ name {:keys [function cache] :as config} body]
   (let [arglists (if (seq body)
                    (invoke/invoke:arglists body)
                    (or (:arglists config) ()))

         [cache-name cache-form]
         (if (or (map? cache)
                 (nil? cache))
           (let [prefix (or (:prefix cache) "+")
                 cache-name (symbol (str prefix name))]
             [cache-name [`(def ~cache-name (atom {}))]])
           [cache []])

         [function-name function-form]
         (if (or (map? function)
                 (nil? function))
           (let [suffix (or (:suffix function) "-raw")
                 function-name (symbol (str name suffix))]
             [function-name [`(defn ~function-name
                                ~(str "helper function for " *ns* "/" name)
                                ~@body)]])
           [function []])

         name (with-meta name (assoc (dissoc config :function :cache)
                                     :arglists arglists))

         membody `(memoize ~function-name ~cache-name (var ~name))]
     `(do (declare ~name)
          ~@cache-form
          ~@function-form
          (def ~name ~membody)
          (doto ~name
            (register-memoize)
            (memoize:clear))
          (var ~name))))
