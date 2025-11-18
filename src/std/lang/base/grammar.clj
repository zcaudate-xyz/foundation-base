(ns std.lang.base.grammar
  (:require [std.lib :as h :refer [defimpl]]
            [std.string :as str]
            [std.lang.base.grammar-spec :as spec]
            [std.lang.base.grammar-macro :as macro]
            [std.lang.base.grammar-xtalk :as xtalk]))

(defn gen-ops
  "generates ops
 
   (gen-ops 'std.lang.base.grammar-spec \"spec\")
   => vector?"
  {:added "4.0"}
  [ns shortcut]
  (->> (ns-publics ns)
       (keep (fn [[k var]]
               (let [kname  (name k)]
                 (if (str/starts-with? kname "+")
                   [(keyword (subs kname 4 (dec (count kname))))
                    var]))))
       (sort-by (fn [[k var]]
                  (:line (meta var))))
       (mapv    (fn [[k var]]
                  [k (symbol shortcut (name (h/var-sym var)))]))))

(defn collect-ops
  "collects alll ops together
 
   (collect-ops +op-all+)
   => map?"
  {:added "4.0"}
  [arr]
  (->> (map-indexed (fn [i [k v]]
                      [k (with-meta
                           (h/map-juxt [:op identity]
                                       v)
                           {:order i})])
                    arr)
       (into {})))

(def ^{:generator (fn []
                    (vec (concat (gen-ops 'std.lang.base.grammar-spec "spec")
                                 (gen-ops 'std.lang.base.grammar-macro "macro")
                                 (gen-ops 'std.lang.base.grammar-xtalk "xtalk"))))}
  +op-all+
  (->> [[:builtin spec/+op-builtin+]
        [:builtin-global spec/+op-builtin-global+]
        [:builtin-module spec/+op-builtin-module+]
        [:builtin-helper spec/+op-builtin-helper+]
        [:free-control spec/+op-free-control+]
        [:free-literal spec/+op-free-literal+]
        [:math       spec/+op-math+]
        [:compare    spec/+op-compare+]
        [:logic      spec/+op-logic+]
        [:counter    spec/+op-counter+]
        [:return     spec/+op-return+]
        [:throw      spec/+op-throw+]
        [:await             spec/+op-await+]
        [:async             spec/+op-async+]
        [:data-table        spec/+op-data-table+]
        [:data-shortcuts    spec/+op-data-shortcuts+]
        [:data-range        spec/+op-data-range+]
        [:vars              spec/+op-vars+]
        [:bit               spec/+op-bit+]
        [:pointer           spec/+op-pointer+]
        [:fn                spec/+op-fn+]
        [:block             spec/+op-block+]
        [:control-base      spec/+op-control-base+]
        [:control-general   spec/+op-control-general+]
        [:control-try-catch spec/+op-control-try-catch+]
        [:top-base     spec/+op-top-base+]
        [:top-global   spec/+op-top-global+]
        [:class        spec/+op-class+]
        [:for          spec/+op-for+]
        [:coroutine    spec/+op-coroutine+]
        [:macro         macro/+op-macro+]
        [:macro-arrow   macro/+op-macro-arrow+]
        [:macro-let     macro/+op-macro-let+]
        [:macro-xor     macro/+op-macro-xor+]
        [:macro-case    macro/+op-macro-case+]
        [:macro-forange macro/+op-macro-forange+]
        [:xtalk-core    xtalk/+op-xtalk-core+]
        [:xtalk-proto   xtalk/+op-xtalk-proto+]
        [:xtalk-global  xtalk/+op-xtalk-global+]
        [:xtalk-custom  xtalk/+op-xtalk-custom+]
        [:xtalk-math xtalk/+op-xtalk-math+]
        [:xtalk-type xtalk/+op-xtalk-type+]
        [:xtalk-bit  xtalk/+op-xtalk-bit+]
        [:xtalk-lu   xtalk/+op-xtalk-lu+]
        [:xtalk-obj  xtalk/+op-xtalk-obj+]
        [:xtalk-arr  xtalk/+op-xtalk-arr+]
        [:xtalk-str  xtalk/+op-xtalk-str+]
        [:xtalk-js   xtalk/+op-xtalk-js+]
        [:xtalk-return  xtalk/+op-xtalk-return+]
        [:xtalk-socket  xtalk/+op-xtalk-socket+]
        [:xtalk-iter    xtalk/+op-xtalk-iter+]
        [:xtalk-cache   xtalk/+op-xtalk-cache+]
        [:xtalk-thread  xtalk/+op-xtalk-thread+]
        [:xtalk-file    xtalk/+op-xtalk-file+]
        [:xtalk-b64     xtalk/+op-xtalk-b64+]
        [:xtalk-uri     xtalk/+op-xtalk-uri+]
        [:xtalk-special xtalk/+op-xtalk-special+]]
       (collect-ops)))

(defn ops-list
  "lists all ops in the grammar"
  {:added "4.0"}
  ([]
   (map first (sort-by (comp :order meta second) +op-all+))))

(defn ops-symbols
  "gets a list of symbols"
  {:added "4.0"}
  []
  (map (fn [k]
         [k (mapcat :symbol (vals (get +op-all+ k)))])
       (ops-list)))

(defn ops-summary
  "gets the symbol and op name for a given category"
  {:added "4.0"}
  ([& [ks]]
   (mapv (fn [k]
           [k (h/map-vals :symbol
                          (get +op-all+ k))])
         (or ks (ops-list)))))

(defn ops-detail
  "get sthe detail of the ops"
  {:added "4.0"}
  ([k]
   (get +op-all+ k)))

;;
;;
;;  Build Grammer Keywords
;;
;;

(defn build
  "selector for picking required ops in grammar"
  {:added "3.0"}
  ([]
   (apply merge (vals +op-all+)))
  ([& {:keys [lookup
              include
              exclude]
       :or {lookup +op-all+}}]
   (let [sel-fn (fn [[k tag entries]]
                  (let [all (get lookup k)]
                    (case tag
                      :include (select-keys all entries)
                      :exclude (apply dissoc all entries))))
         selected (cond include
                        (reduce (fn [out sel]
                                  (cond (vector? sel)
                                        (assoc out (first sel) (sel-fn sel))

                                        :else
                                        (assoc out sel (get lookup sel))))
                                {}
                                include)

                        exclude
                        (reduce (fn [out sel]
                                  (cond (vector? sel)
                                        (assoc out (first sel) (sel-fn sel))

                                        :else
                                        (dissoc out sel)))
                                lookup
                                exclude))]
     (apply merge (vals selected)))))

(defn build-min
  "minimum ops example for a language"
  {:added "4.0"}
  [& [arr]]
  (build :include (concat [:builtin
                           :builtin-module
                           :builtin-helper
                           :free-control
                           :free-literal
                           :math
                           :compare
                           :logic
                           :return
                           :vars
                           :fn
                           :data-table
                           :control-base
                           :control-general
                           :top-base
                           :top-global
                           :macro]
                          arr)))

(defn build-xtalk
  "xtalk ops
 
   (build-xtalk)
   => map?"
  {:added "4.0"}
  []
  (build :include [:xtalk-core
                   :xtalk-proto
                   :xtalk-global
                   :xtalk-custom
                   :xtalk-math
                   :xtalk-type
                   :xtalk-bit
                   :xtalk-lu
                   :xtalk-arr
                   :xtalk-str
                   :xtalk-js
                   :xtalk-return
                   :xtalk-socket
                   :xtalk-iter
                   :xtalk-cache
                   :xtalk-thread
                   :xtalk-file
                   :xtalk-b64
                   :xtalk-uri
                   :xtalk-special]))

(defn build:override
  "overrides existing ops in the map"
  {:added "4.0"}
  [build m]
  (let [ks (h/difference (set (keys m))
                         (set (keys build)))
        _  (if (not-empty ks)
             (h/error "Keys not in original map: " {:keys ks}))]
    (h/merge-nested build m)))

(defn build:extend
  "adds new  ops in the map"
  {:added "4.0"}
  [build m]
  (let [ks (h/intersection (set (keys m))
                           (set (keys build)))
        _  (if (not-empty ks)
             (h/error "Keys in original map: " {:keys ks}))]
    (merge build m)))

(defn to-reserved
  "convert op map to symbol map"
  {:added "3.0"}
  ([build]
   (->> build
        (mapcat (fn [[k m]]
                  (map (fn [sym]
                         [sym m])
                       (:symbol m))))
        (into {}))))

(defn grammar-structure
  "returns all the `:block` and `:fn` forms"
  {:added "3.0"}
  ([reserved]
   (h/map-juxt [identity
                (fn [k] (set (keys (h/filter-vals (comp #{k} :type) reserved))))]
               [:block :def :fn])))

(defn grammar-sections
  "process sections witihin the grammar"
  {:added "3.0"}
  ([reserved]
   (set (vals (h/keep-vals :section reserved)))))

(defn grammar-macros
  "process macros within the grammar"
  {:added "3.0"}
  ([reserved]
   (set (keys (h/filter-vals (comp #{:def} :type) reserved)))))

(defn- grammar-string
  ([{:keys [tag structure reserved banned highlight macros sections] :as grammar}]
   (str "#grammar " [tag] " "
        (assoc structure
               :sections sections
               :ops (count reserved)
               :banned banned
               :highlight highlight
               :macros macros))))

(defimpl Grammer [tag emit structure reserved banned highlight]
  :string grammar-string)

(defn grammar?
  "checks that an object is instance of grammar"
  {:added "3.0"}
  ([obj]
   (instance? Grammer obj)))

(defn grammar
  "constructs a grammar"
  {:added "3.0" :style/indent 1}
  ([tag reserved template]
   (-> template
       (assoc :tag tag
              :reserved reserved
              :sections  (grammar-sections reserved)
              :macros    (grammar-macros reserved)
              :structure (grammar-structure reserved))
       (map->Grammer))))

