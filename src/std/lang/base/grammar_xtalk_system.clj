(ns std.lang.base.grammar-xtalk-system
  (:require [clojure.set :as set]
            [std.lang.base.grammar-xtalk :as xtalk]
            [std.lib.collection :as collection]
            [std.lib.walk :as walk]))

(def +xtalk-profiles+
  [[:xtalk-core-value (vec (concat xtalk/+xt-common-basic+
                                   xtalk/+xt-common-number+
                                   xtalk/+xt-fn-identity+))]
   [:xtalk-core-invoke (vec (concat xtalk/+xt-common-print+
                                    xtalk/+xt-lang-invoke+))]
   [:xtalk-runtime     xtalk/+xt-runtime-shell+]
   [:xtalk-system      (vec (concat xtalk/+xt-lang-unpack+
                                    xtalk/+xt-lang-random+
                                    xtalk/+xt-lang-time+))]
   [:xtalk-task        xtalk/+xt-core-future+]
   [:xtalk-proto       xtalk/+xt-lang-proto+]
   [:xtalk-global      xtalk/+xt-lang-global+]
   [:xtalk-predicate   (vec (concat xtalk/+xt-common-number+
                                    xtalk/+xt-common-nil+))]
   [:xtalk-access      xtalk/+xt-common-object+]
   [:xtalk-index       xtalk/+xt-common-index+]
   [:xtalk-callback    xtalk/+xt-common-function+]
   [:xtalk-math        xtalk/+xt-math+]
   [:xtalk-type        (vec (concat xtalk/+xt-common-primitives+
                                    xtalk/+xt-common-basic+))]
   [:xtalk-bit         xtalk/+xt-lang-bit+]
   [:xtalk-lu          xtalk/+xt-lu+]
   [:xtalk-obj         (vec (concat xtalk/+xt-common-object+
                                    xtalk/+xt-obj+))]
   [:xtalk-arr         (vec (concat xtalk/+xt-common-array+
                                    xtalk/+xt-arr+))]
   [:xtalk-str         xtalk/+xt-str+]
   [:xtalk-js          xtalk/+xt-js+]
   [:xtalk-return      xtalk/+xt-return+]
   [:xtalk-socket      xtalk/+xt-socket+]
   [:xtalk-ws          xtalk/+xt-ws+]
   [:xtalk-iter        xtalk/+xt-iter+]
   [:xtalk-cache       xtalk/+xt-cache+]
   [:xtalk-thread      xtalk/+xt-thread+]
   [:xtalk-file        xtalk/+xt-file+]
   [:xtalk-b64         xtalk/+xt-b64+]
   [:xtalk-uri         xtalk/+xt-uri+]
   [:xtalk-notify      xtalk/+xt-notify+]
   [:xtalk-service     xtalk/+xt-service+]
   [:xtalk-special     xtalk/+xt-special+]])

(def +xtalk-profile-order+
  (mapv first +xtalk-profiles+))

(def +xtalk-profile->ops+
  (into {}
        (map (fn [[profile entries]]
               [profile (->> entries
                             (map :op)
                             set)]))
        +xtalk-profiles+))

(def +xtalk-op->entry+
  (->> +xtalk-profiles+
       (mapcat second)
       (map (juxt :op identity))
       (into {})))

(def +xtalk-symbol->op+
  (->> +xtalk-op->entry+
       vals
       (mapcat (fn [{:keys [op symbol]}]
                 (map (fn [sym] [sym op]) symbol)))
       (into {})))

(def +xtalk-op->profiles+
  (reduce-kv (fn [out profile ops]
               (reduce (fn [inner op]
                         (update inner op (fnil conj #{}) profile))
                       out
                       ops))
             {}
             +xtalk-profile->ops+))

(def +xtalk-ops+
  (->> +xtalk-profile->ops+
       vals
       (apply set/union)
       sort
       vec))

(def +xtalk-library->profiles+
  {'xtalk.lib.db.check #{:xtalk-access
                         :xtalk-core-value
                         :xtalk-index
                         :xtalk-str
                         :xtalk-type}
   'xtalk.lib.db.sql #{:xtalk-core-value
                       :xtalk-js
                       :xtalk-predicate
                       :xtalk-str
                       :xtalk-type}
   'xtalk.lib.db.call #{:xtalk-access
                        :xtalk-arr
                        :xtalk-core-value
                        :xtalk-js
                        :xtalk-str
                        :xtalk-type}})

(defn xtalk-profiles
  "returns xtalk profiles in grammar order"
  {:added "4.1"}
  []
  +xtalk-profile-order+)

(defn xtalk-profile-ops
  "returns ops declared under an xtalk grammar profile"
  {:added "4.1"}
  [profile]
  (get +xtalk-profile->ops+ profile #{}))

(defn xtalk-op-profiles
  "returns the xtalk grammar profiles that declare an op"
  {:added "4.1"}
  [op]
  (get +xtalk-op->profiles+ op #{}))

(defn xtalk-op-entry
  "returns the xtalk grammar entry for an op"
  {:added "4.1"}
  [op]
  (get +xtalk-op->entry+ op))

(defn xtalk-symbol-op
  "returns the xtalk op for a symbol"
  {:added "4.1"}
  [sym]
  (get +xtalk-symbol->op+ sym))

(defn xtalk-symbol-entry
  "returns the xtalk grammar entry for a symbol"
  {:added "4.1"}
  [sym]
  (some-> sym
          xtalk-symbol-op
          xtalk-op-entry))

(defn xtalk-op-requires
  "returns the direct required xtalk ops for an op"
  {:added "4.1"}
  [op]
  (or (:requires (xtalk-op-entry op))
      #{}))

(defn xtalk-op-closure
  "returns the transitive required xtalk ops for an op, including itself"
  {:added "4.1"}
  [op]
  (loop [seen #{}
         pending (if op [op] [])]
    (if-let [current (first pending)]
      (if (seen current)
        (recur seen (rest pending))
        (recur (conj seen current)
               (concat (rest pending)
                       (remove seen (xtalk-op-requires current)))))
      seen)))

(defn xtalk-ops-profiles
  "returns the combined profile set for a collection of ops"
  {:added "4.1"}
  [ops]
  (->> ops
       (map xtalk-op-profiles)
       (apply set/union #{})))

(defn scan-xtalk
  "scans a form for xtalk op usage and linked hard-link modules"
  {:added "4.1"}
  [input]
  (let [ops (volatile! #{})]
    (walk/prewalk
     (fn [form]
       (when (and (collection/form? form)
                  (symbol? (first form)))
         (when-let [op (xtalk-symbol-op (first form))]
           (vswap! ops set/union (xtalk-op-closure op))))
       form)
     input)
    (let [xtalk-ops @ops]
      {:ops xtalk-ops
       :profiles (xtalk-ops-profiles xtalk-ops)
       :polyfill-modules (->> xtalk-ops
                              (keep (fn [op]
                                      (let [{:keys [emit raw]} (xtalk-op-entry op)]
                                        (when (and (= :hard-link emit)
                                                   (symbol? raw)
                                                   (namespace raw))
                                          (symbol (namespace raw))))))
                              set)})))

(defn xtalk-grammar-supported-ops
  "returns the xtalk ops supported by a grammar reserved map"
  {:added "4.1"}
  [grammar]
  (let [reserved (or (:reserved grammar) grammar)]
    (->> reserved
         vals
         (keep :op)
         (filter +xtalk-op->profiles+)
         set)))

(defn xtalk-grammar-supported-profiles
  "returns the xtalk profiles fully supported by a grammar"
  {:added "4.1"}
  [grammar]
  (let [supported-ops (xtalk-grammar-supported-ops grammar)]
    (->> +xtalk-profile-order+
         (filter (fn [profile]
                   (set/subset? (xtalk-profile-ops profile)
                                supported-ops)))
         vec)))

(defn xtalk-grammar-missing-profiles
  "returns required xtalk profiles not fully supported by a grammar"
  {:added "4.1"}
  [grammar required-profiles]
  (let [supported (set (xtalk-grammar-supported-profiles grammar))]
    (->> required-profiles
         (remove supported)
         set)))

(defn xtalk-library-profiles
  "returns the upfront xtalk grammar profiles required by a new xtalk library namespace"
  {:added "4.1"}
  [ns-sym]
  (get +xtalk-library->profiles+ ns-sym #{}))

(defn xtalk-unclassified-ops
  "returns xtalk ops not covered by the declared profiles"
  {:added "4.1"}
  []
  (let [grammar-ops (->> +xtalk-profile->ops+
                         vals
                         (apply set/union))]
    (->> grammar-ops
         (remove +xtalk-op->profiles+)
         sort
         vec)))
