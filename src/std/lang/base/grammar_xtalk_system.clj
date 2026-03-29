(ns std.lang.base.grammar-xtalk-system
  (:require [clojure.set :as set]
            [std.lang.base.grammar-xtalk :as xtalk]
            [std.lib.collection :as collection]
            [std.lib.walk :as walk]))

(def +xtalk-area-order+
  [:xtalk-common
   :xtalk-fn
   :xtalk-impl
   :xtalk-link
   :xtalk-runtime])

(def +xtalk-area->entries+
  {:xtalk-common   [xtalk/+xt-common-basic+
                    xtalk/+xt-common-index+
                    xtalk/+xt-common-number+
                    xtalk/+xt-common-nil+
                    xtalk/+xt-common-primitives+
                    xtalk/+xt-common-lu+
                    xtalk/+xt-common-object+
                    xtalk/+xt-common-array+
                    xtalk/+xt-common-print+
                    xtalk/+xt-common-string+
                    xtalk/+xt-common-math+]
   :xtalk-fn       [xtalk/+xt-functional-base+
                    xtalk/+xt-functional-invoke+
                    xtalk/+xt-functional-return+
                    xtalk/+xt-functional-array+
                    xtalk/+xt-functional-future+
                    xtalk/+xt-functional-iter+]
   :xtalk-impl     [xtalk/+xt-lang-global+
                    xtalk/+xt-lang-proto+
                    xtalk/+xt-lang-bit+
                    xtalk/+xt-lang-throw+
                    xtalk/+xt-lang-unpack+
                    xtalk/+xt-lang-random+
                    xtalk/+xt-lang-time+]
   :xtalk-link     [xtalk/+xt-notify-socket+
                    xtalk/+xt-notify-http+
                    xtalk/+xt-network-socket+
                    xtalk/+xt-network-ws+
                    xtalk/+xt-network-client-basic+
                    xtalk/+xt-network-client-ws+
                    xtalk/+xt-network-server-basic+
                    xtalk/+xt-network-server-ws+]
   :xtalk-runtime  [xtalk/+xt-runtime-cache+
                    xtalk/+xt-runtime-thread+
                    xtalk/+xt-runtime-shell+
                    xtalk/+xt-runtime-file+
                    xtalk/+xt-runtime-b64+
                    xtalk/+xt-runtime-uri+
                    xtalk/+xt-runtime-js+]})

(def +xtalk-profiles+
  (mapv (fn [area]
          [area
           (->> (get +xtalk-area->entries+ area)
                (mapcat identity)
                vec)])
        +xtalk-area-order+))

(defn xtalk-entry?
  [entry]
  (and (map? entry)
       (keyword? (:op entry))))

(def +xtalk-profile->ops+
  (into {}
        (map (fn [[profile entries]]
               [profile (->> entries
                             (filter xtalk-entry?)
                             (map :op)
                             set)]))
        +xtalk-profiles+))

(def +xtalk-op-entry+
  (->> +xtalk-profiles+
       (mapcat second)
       (filter xtalk-entry?)
       (map (juxt :op identity))
       (into {})))

(def +xtalk-symbol->op+
  (->> +xtalk-op-entry+
       vals
       (mapcat (fn [{:keys [op symbol]}]
                 (map (fn [sym] [sym op]) symbol)))
       (into {})))

(def +xtalk-op-profiles+
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


(defn xtalk-ops-profiles
  "returns the combined profile set for a collection of ops"
  {:added "4.1"}
  [ops]
  (->> ops
       (map +xtalk-op-profiles+)
       (apply set/union #{})))

(defn scan-xtalk
  "scans a form for xtalk op usage, linked hard-link modules, and template restaging"
  {:added "4.1"}
  ([input]
   (scan-xtalk input nil))
  ([input grammar]
   (let [ops       (volatile! #{})
         template? (volatile! false)
         reserved  (:reserved grammar)]
     (walk/prewalk
      (fn [form]
        (when (and (collection/form? form)
                   (symbol? (first form)))
          (when-let [op ( (first form))]
            (vswap! ops set/union (xtalk-op-closure op)))
          (when (and reserved
                     (= :hard-link (get-in reserved [(first form) :emit])))
            (vreset! template? true)))
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
                               set)
        :template? @template?}))))

(defn xtalk-grammar-supported-ops
  "returns the xtalk ops supported by a grammar reserved map"
  {:added "4.1"}
  [grammar]
  (let [reserved (or (:reserved grammar) grammar)]
    (->> reserved
         vals
         (keep :op)
         (filter +xtalk-op-profiles+)
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

(defn xtalk-unclassified-ops
  "returns xtalk ops not covered by the declared profiles"
  {:added "4.1"}
  []
  (let [grammar-ops (->> +xtalk-profile->ops+
                         vals
                         (apply set/union))]
    (->> grammar-ops
         (remove +xtalk-op-profiles+)
         sort
         vec)))
