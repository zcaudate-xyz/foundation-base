(ns std.lib.transform.base.alias
  (:require [std.lib.foundation :as h]
            [std.lib.transform.base.complex :as complex]
            [std.lib.walk :as walk]
            [std.string.common :as str]
            [std.string.path :as path]
            [std.string.wrap :as wrap]))

(defn find-aliases
  "finds necessary aliases
 
   (find-aliases (:flat (schema/schema family/family-links))
                 [:person/brother
                  :person/mother])
   => [[:person/brother {:ns :sibling,
                         :template {:sibling {:gender :m}}}]
       [:person/mother {:ns :parent,
                        :template {:parent {:gender :f}}}]]"
  {:added "3.0"}
  ([tsch ks]
   (reduce (fn [out k]
             (let [sub (get tsch k)]
               (cond (vector? sub)
                     (if-let [alias (-> sub first :alias)]
                       (conj out [k alias])
                       out)
                     :else out)))
           []  ks)))

(defn template-alias
  "templates an alias, replacing symbols with random
 
   (template-alias {:db/id 'hello})
   ;;{:db/id hello_141387}
   => (contains {:db/id symbol?})"
  {:added "3.0"}
  ([tmpl]
   (let [symbols (atom {})
         rep-fn (fn [e]
                  (if (symbol? e)
                    (if-let [sym (get @symbols e)]
                      sym
                      (let [sym (gensym (str e "_"))]
                        (swap! symbols assoc e sym)
                        sym))
                    e))]
     (walk/postwalk rep-fn tmpl))))

(defn resolve-alias
  "resolves the data for the alias
 
   (resolve-alias (:tree (schema/schema family/family-links))
                  {:male {:name \"Chris\"}}
                  [:male {:ns :person,
                          :template {:person {:gender :m}}}]
                  nil)
   => {:person {:gender :m, :name \"Chris\"}}"
  {:added "3.0"}
  ([tsch tdata alias no-gen-sym]
   (let [[k rec] alias
         ans    ((wrap/wrap path/path-split) (:ns rec))
         atmpl  (:template rec)
         atmpl  (if no-gen-sym
                  atmpl
                  (template-alias atmpl))
         sdata  (get tdata k)
         adata  (update-in atmpl ans merge sdata)]
     (complex/merges (dissoc tdata k) adata))))

(defn wrap-alias
  "wraps normalise to process aliases for a database schema
 
   (graph/normalise {:male/name \"Chris\"}
                    {:schema (schema/schema family/family-links)}
                    {:normalise [wrap-alias]})
   => {:person {:gender :m, :name \"Chris\"}}
 
   (graph/normalise {:female {:parent/name \"Sam\"
                              :brother {:brother/name \"Chris\"}}}
                    {:schema (schema/schema family/family-links)})
   => {:person {:gender :f, :parent {:name \"Sam\"},
                :sibling {:gender :m,
                          :sibling {:gender :m,
                                    :name \"Chris\"}}}}"
  {:added "3.0"}
  ([f]
   (fn [tdata tsch nsv interim fns datasource]
     (let [ks (keys tdata)
           aliases (find-aliases tsch ks)
           _       (if (and (= (:command datasource) :datoms)
                            (not (empty? aliases)))
                     (h/error (str "WRAP_ALIAS: Aliases cannot be specified on datoms")
                              {:id :no-alias
                               :data tdata :nsv nsv :key-path (:key-path interim)}))
           ntdata (reduce #(resolve-alias tsch %1 %2 (-> datasource :options :no-alias-gen)) tdata aliases)]
       (f ntdata tsch nsv interim fns datasource)))))
