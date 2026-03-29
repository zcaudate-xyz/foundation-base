(ns std.lang.base.grammar-xtalk-system
  (:require [clojure.set :as set]
            [std.lang.base.grammar-xtalk :as xtalk]
            [std.lib.collection :as collection]
            [std.lib.walk :as walk]))

(def +xtalk-area-order+
  [:common
   :functional
   :language-specific
   :std-lang-link-specific
   :runtime-specific])

(def +xtalk-area->entries+
  {:common [xtalk/+xt-common-basic+
            xtalk/+xt-common-index+
            xtalk/+xt-common-number+
            xtalk/+xt-common-nil+
            xtalk/+xt-common-primitives+
            xtalk/+xt-common-object+
            xtalk/+xt-common-array+
            xtalk/+xt-common-print+
            xtalk/+xt-common-string+
            xtalk/+xt-common-math+]
   :functional [xtalk/+xt-functional-base+
                xtalk/+xt-functional-invoke+
                xtalk/+xt-functional-return+
                xtalk/+xt-functional-array+
                xtalk/+xt-functional-future+
                xtalk/+xt-functional-iter+]
   :language-specific [xtalk/+xt-lang-lu+
                       xtalk/+xt-lang-global+
                       xtalk/+xt-lang-proto+
                       xtalk/+xt-lang-bit+
                       xtalk/+xt-lang-throw+
                       xtalk/+xt-lang-unpack+
                       xtalk/+xt-lang-random+
                       xtalk/+xt-lang-time+]
   :std-lang-link-specific [xtalk/+xt-notify-socket+
                            xtalk/+xt-notify-http+
                            xtalk/+xt-network-socket+
                            xtalk/+xt-network-ws+
                            xtalk/+xt-network-client-basic+
                            xtalk/+xt-network-client-ws+
                            xtalk/+xt-network-server-basic+
                            xtalk/+xt-network-server-ws+]
   :runtime-specific [xtalk/+xt-runtime-cache+
                      xtalk/+xt-runtime-thread+
                      xtalk/+xt-runtime-shell+
                      xtalk/+xt-runtime-file+
                      xtalk/+xt-runtime-b64+
                      xtalk/+xt-runtime-uri+
                      xtalk/+xt-runtime-js+]})

(def +xtalk-area->profile+
  {:common :xtalk-common
   :functional :xtalk-functional
   :language-specific :xtalk-language-specific
   :std-lang-link-specific :xtalk-std-lang-link-specific
   :runtime-specific :xtalk-runtime-specific})

(def +xtalk-profiles+
  (mapv (fn [area]
          [(get +xtalk-area->profile+ area)
           (->> (get +xtalk-area->entries+ area)
                (mapcat identity)
                vec)])
        +xtalk-area-order+))

(def +xtalk-profile-order+
  (mapv first +xtalk-profiles+))

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

(def +xtalk-op->entry+
  (->> +xtalk-profiles+
       (mapcat second)
       (filter xtalk-entry?)
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

(defn xtalk-profiles
  "returns xtalk profiles in grammar order"
  {:added "4.1"}
  []
  +xtalk-profile-order+)

(defn xtalk-areas
  "returns xtalk implementation areas in declaration order"
  {:added "4.1"}
  []
  +xtalk-area-order+)

(defn xtalk-area-profiles
  "returns xtalk profiles grouped by implementation area"
  {:added "4.1"}
  [area]
  (if-let [profile (get +xtalk-area->profile+ area)]
    [profile]
    []))

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
   (let [ops     (volatile! #{})
         symbols (volatile! #{})]
     (walk/prewalk
      (fn [form]
         (when (and (collection/form? form)
                    (symbol? (first form)))
           (when-let [op (xtalk-symbol-op (first form))]
             (vswap! symbols conj (first form))
             (vswap! ops set/union (xtalk-op-closure op))))
         form)
      input)
    (let [xtalk-ops @ops]
      {:ops xtalk-ops
       :symbols @symbols
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
