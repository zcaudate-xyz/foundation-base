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
  {:xtalk-common  [xtalk/+xt-common-basic+
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
   :xtalk-fn      [xtalk/+xt-functional-base+
                   xtalk/+xt-functional-invoke+
                   xtalk/+xt-functional-return+
                   xtalk/+xt-functional-array+
                   xtalk/+xt-functional-iter+]
   :xtalk-impl    [xtalk/+xt-lang-global+
                   xtalk/+xt-lang-proto+
                   xtalk/+xt-lang-bit+
                   xtalk/+xt-lang-throw+
                   xtalk/+xt-lang-unpack+
                   xtalk/+xt-lang-random+
                   xtalk/+xt-lang-time+]
   :xtalk-link    [xtalk/+xt-notify-socket+
                   xtalk/+xt-notify-http+
                   xtalk/+xt-network-socket+
                   xtalk/+xt-network-ws+
                   xtalk/+xt-network-client-basic+
                   xtalk/+xt-network-client-ws+]
   :xtalk-runtime [xtalk/+xt-runtime-cache+
                   xtalk/+xt-runtime-thread+
                   xtalk/+xt-runtime-shell+
                   xtalk/+xt-runtime-file+
                   xtalk/+xt-runtime-b64+
                   xtalk/+xt-runtime-uri+
                   xtalk/+xt-runtime-json+]})

(def +xtalk-area->surface+
  {:xtalk-common  {:area :common
                   :profile :xtalk-common}
   :xtalk-fn      {:area :functional
                   :profile :xtalk-functional}
   :xtalk-impl    {:area :language-specific
                   :profile :xtalk-language-specific}
   :xtalk-link    {:area :std-lang-link-specific
                   :profile :xtalk-std-lang-link-specific}
   :xtalk-runtime {:area :runtime-specific
                   :profile :xtalk-runtime-specific}})

(def +xtalk-profile-order+
  (mapv (comp :profile +xtalk-area->surface+) +xtalk-area-order+))

(def +xtalk-areas+
  (mapv (comp :area +xtalk-area->surface+) +xtalk-area-order+))

(def +xtalk-profiles+
  (mapv (fn [area]
          [(:profile (get +xtalk-area->surface+ area))
           (->> (get +xtalk-area->entries+ area)
                (mapcat identity)
                vec)])
        +xtalk-area-order+))

(def +xtalk-area->profiles+
  (into {}
        (map (fn [area]
               (let [{surface-area :area
                      profile      :profile} (get +xtalk-area->surface+ area)]
                 [surface-area [profile]])))
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

(def +xtalk-op->entry+
  (->> +xtalk-profiles+
       (mapcat second)
       (filter xtalk-entry?)
       (map (juxt :op identity))
       (into {})))

(def +xtalk-op-entry+
  +xtalk-op->entry+)

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

(def +xtalk-op-profiles+
  +xtalk-op->profiles+)

(def +xtalk-ops+
  (->> +xtalk-profile->ops+
       vals
       (apply set/union)
       sort
       vec))

(def +xtalk-library->profiles+
  {'xtalk.lib.db.check #{:xtalk-common}
   'xtalk.lib.db.sql #{:xtalk-common
                       :xtalk-runtime-specific}
   'xtalk.lib.db.call #{:xtalk-common
                        :xtalk-functional
                        :xtalk-runtime-specific}})

(defn xtalk-profiles
  "returns xtalk profiles in grammar order"
  {:added "4.1"}
  []
  +xtalk-profile-order+)

(defn xtalk-areas
  "returns xtalk implementation areas in declaration order"
  {:added "4.1"}
  []
  +xtalk-areas+)

(defn xtalk-area-profiles
  "returns xtalk profiles grouped by implementation area"
  {:added "4.1"}
  [area]
  (get +xtalk-area->profiles+ area []))

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
  "scans a form for xtalk op usage, linked hard-link modules, and template restaging"
  {:added "4.1"}
  ([input]
   (scan-xtalk input nil))
  ([input grammar]
   (let [ops       (volatile! #{})
         symbols   (volatile! #{})
         template? (volatile! false)
         reserved  (or (:reserved grammar) grammar)]
     (walk/prewalk
      (fn [form]
        (when (and (collection/form? form)
                   (symbol? (first form)))
          (when-let [op (xtalk-symbol-op (first form))]
            (vswap! symbols conj (first form))
            (vswap! ops set/union (xtalk-op-closure op)))
          (when (and reserved
                     (= :hard-link (get-in reserved [(first form) :emit])))
            (vreset! template? true)))
        form)
      input)
     (let [xtalk-ops @ops]
       (cond-> {:ops xtalk-ops
                :symbols @symbols
                :profiles (xtalk-ops-profiles xtalk-ops)
                :polyfill-modules (->> xtalk-ops
                                       (keep (fn [op]
                                               (let [{:keys [emit raw]} (xtalk-op-entry op)]
                                                 (when (and (= :hard-link emit)
                                                            (symbol? raw)
                                                            (namespace raw))
                                                   (symbol (namespace raw))))))
                                       set)}
         @template? (assoc :template? true))))))

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
