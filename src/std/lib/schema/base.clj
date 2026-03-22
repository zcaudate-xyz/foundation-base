(ns std.lib.schema.base
  (:require [std.lib.collection :as coll]
            [std.lib.foundation :as h]))

(defonce +scope-brief+
  {:*/min        #{:-/id       :-/key}
   :*/info       #{:*/min      :-/info}
   :*/data       #{:*/info     :-/data}
   :*/default    #{:*/data     :-/ref}
   :*/detail     #{:*/data     :-/detail}
   :*/standard   #{:*/detail   :-/ref}
   :*/all        #{:*/standard :-/system}
   :*/everything #{:*/all      :-/hidden}})

(defn expand-scopes
  "expand scopes for all globbed keywords"
  {:added "4.0"}
  ([m]
   (expand-scopes m (coll/filter-vals (fn [v] (every? (comp #(= % "-") namespace)
                                                      v))
                                      m)))
  ([m expanded]
   (let [prev (apply dissoc m (keys expanded))
         curr (coll/map-vals (fn [ks]
                               (let [scoped (set (remove expanded ks))
                                     es (select-keys expanded ks)]
                                 (apply clojure.set/union scoped (vals es))))
                             prev)]
     (if (not-empty curr)
       (expand-scopes curr (merge expanded
                                  (coll/filter-vals (fn [v] (every? (comp #(= % "-") namespace)
                                                                    v))
                                                    curr)))
       expanded))))

(defonce +scope+
  (expand-scopes +scope-brief+))

(defn check-scope
  "check if a scope is valid"
  {:added "4.0"}
  [scope]
  (or (get (:*/everything +scope+) scope)
      (h/error "Scope not Valid" {:value scope
                                  :allowed (:*/everything +scope+)})))

(def base-meta
  {:ident        {:required true
                  :check keyword?}
   :type         {:required true
                  :default :string
                  :auto true}
   :cardinality  {:check #{:one :many}
                  :auto true
                  :default :one}
   :scope        {:check (:*/everything +scope+)}
   :doc          {:check string?}
   :unique       {:check #{:value :identity}}
   :index        {:check boolean?}
   :required     {:check boolean?}
   :restrict     {:check ifn?}
   :default      {:check identity}})


(defn attr-add-ident
  "adds the key of a pair as :ident to a schema property pair"
  {:added "3.0"}
  ([[k [attr :as v]]]
   [k (assoc-in v [0 :ident] k)]))

(defn attr-add-defaults
  "adds defaults to a given schema property pair"
  {:added "3.0"}
  ([[k [attr :as v]] dfts]
   (let [mks   (map :id dfts)
         mdfts (map :default dfts)
         v-defaults (->> (merge (zipmap mks mdfts) attr)
                         (assoc v 0))]
     [k v-defaults])))

(defn defaults
  "constructs a map according to specifics
 
   (base/defaults [:person/name {:default \"Unknown\"
                                 :auto true}])
   => {:default \"Unknown\", :auto true, :id :person/name}"
  {:added "3.0"}
  ([[k prop]]
   (-> (select-keys prop [:default :auto])
       (assoc :id k))))

(defn all-auto-defaults
  "all automatic defaults for the schema
 
   (base/all-auto-defaults)
   => [{:default :string, :auto true, :id :type}
       {:default :one, :auto true, :id :cardinality}]"
  {:added "3.0"}
  ([] (all-auto-defaults base-meta))
  ([meta]
   (filter (fn [m] (-> m :auto))
           (map defaults meta))))

(defn all-defaults
  "all defaults for the schema
 
   (base/all-defaults)
   => [{:default :string, :auto true, :id :type}
       {:default :one, :auto true, :id :cardinality}]"
  {:added "3.0"}
  ([] (all-defaults base-meta))
  ([meta]
   (filter (fn [m] (-> m :default nil? not))
           (map defaults meta))))

(defmulti type-checks
  "gets type-checks according to category
 
   ((base/type-checks :default :string) \"Hello\")
   => true"
  {:added "3.0"}
  (fn [t k] t))

(defmethod type-checks :default
  ([_ k]
   (get {:string string?
         :boolean boolean?
         :long integer?
         :float float?
         :double double?}
        k)))
