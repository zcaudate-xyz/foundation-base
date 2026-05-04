(ns hara.lang.base.book-module
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [hara.common.emit-template :as impl-template]
            [std.lib.foundation :as f]
            [std.lib.collection :as collection]
            [std.lib.impl :as impl]))

(defn- book-module-string
  ([{:keys [lang id display] :as module}]
   (let [meta (->> (dissoc module :id :lang :code :fragment :display)
                   (collection/filter-vals (fn [e]
                                    (if (coll? e)
                                      (not-empty e)
                                      e))))
         entries (->> (select-keys module [:code :fragment])
                      (collection/map-vals (comp sort keys)))]
     (str "#book.module " [lang id] 
          (if (= :brief display)
            ""
            (str " "
                 {:meta meta
                  :entries entries}))))))
  
;;
;; 
;;

(impl/defimpl BookModule [;; required
                      lang
                      id
                      
                      ;; dependency management
                      alias         ;; map of shortcuts to packages (both external and internal)
                      link          ;; map of internal dependencies
                      native        ;; packages that are native to the workspace runtime
                      native-lu     ;; lookup for native imports
                      internal      ;; set of required packages
                      implements    ;; abstract contracts implemented by the module
                      specialize    ;; declarative backend bindings for linked modules
                      
                      ;; actual code
                      
                      fragment   ;; macros
                      code       ;; main code
                     includes   ;; included modules

                     ;; misc (for adding additional data not related to runtime)
                     static]
  :final true
  :string book-module-string)

(defn book-module?
  "checks of object is a book module"
  {:added "4.0"}
  ([x]
   (instance? BookModule x)))

(defn book-module
  "creates a book module"
  {:added "4.0"}
  ([{:keys [lang
            id

            alias
            link
            internal
             native
             native-lu
             require-impl
             implements
             specialize
             
             fragment
             code
             includes

            static
            display] :as m}]
   (assert (not= nil lang) "Module :lang required")
   (assert (not= nil id)   "Module :id Required")
   (map->BookModule (merge { ;; Link
                             :alias {}
                             :link {}
                             :native {}
                             :require-impl []
                             :implements []
                             :specialize {}
 
                             ;; Code
                             :fragment {}
                             :code {}
                            :includes #{}

                            ;; Misc
                            :static {}
                             :display :default}
                           m))))

(defn polyfill-default-alias
  "returns the default alias for a derived polyfill module"
  {:added "4.1"}
  [module-id]
  (let [suffix (-> (name module-id)
                   (string/replace #"^common-" "")
                   (string/replace #"^polyfill-" ""))]
    (symbol (str "polyfill-" suffix))))

(defn- module-materialized-code
  [book module]
  (if book
    (let [module-view (assoc module :display :brief)
          lang        (:lang module)]
      (collection/map-vals
       (fn [entry]
         (impl-template/materialize-code-entry book
                                               entry
                                               {:lang lang
                                                :module module-view}))
       (:code module)))
    (:code module)))

(defn- module-add-polyfill-link
  [module dep]
  (let [existing-alias (get (:internal module) dep)
        alias          (or existing-alias
                           (polyfill-default-alias dep))
        link-target     (get (:link module) alias)
        alias-target    (get (:alias module) alias)]
    (when (and link-target
               (not= link-target dep))
      (f/error "Derived polyfill alias conflicts with existing link"
               {:module (:id module)
                :alias alias
                :current link-target
                :polyfill dep}))
    (when (and alias-target
               (not= alias-target dep))
      (f/error "Derived polyfill alias conflicts with existing alias"
               {:module (:id module)
                :alias alias
                :current alias-target
                :polyfill dep}))
    (-> module
        (update :link assoc alias dep)
        (update :internal assoc dep alias)
        (update :alias assoc alias dep))))

(defn module-derived-view
  "returns a compilation view of the module with materialized code and derived polyfill links"
  {:added "4.1"}
  ([book module]
   (let [code      (module-materialized-code book module)
         polyfills (->> (vals code)
                        (mapcat (fn [entry]
                                  (or (:polyfill-modules entry) #{})))
                        set
                        sort)
         module    (assoc module :code code)]
     (reduce (fn [module dep]
               (cond
                 (= dep (:id module))
                 module

                 (contains? (:internal module) dep)
                 module

                 (not (get-in book [:modules dep]))
                 (f/error "Derived polyfill module not found"
                          {:module (:id module)
                           :polyfill dep
                           :available (keys (:modules book))})

                 :else
                  (module-add-polyfill-link module dep)))
              module
              polyfills))))

(defn resolve-module-view
  "resolves a module id or module map to the derived per-language compilation view"
  {:added "4.1"}
  [book module]
  (when module
    (let [module (if (symbol? module)
                   (get-in book [:modules module])
                   module)]
      (when module
        (module-derived-view book module)))))

(defn module-deps-code
  "gets dependencies for a given module"
  {:added "4.0"}
  ([module]
    (module-deps-code nil module))
   ([book module]
    (let [module   (if book
                     (module-derived-view book module)
                     module)
          reserved (or (-> book :grammar :reserved) {})
          polyfill-modules
          (fn [entry]
            (set/union
             (or (:polyfill-modules entry) #{})
             (->> (:xtalk-ops entry)
                  (mapcat (fn [op-id]
                            (keep (fn [{reserved-op :op
                                        :keys [emit raw]}]
                                    (when (and (= op-id reserved-op)
                                               (= :hard-link emit)
                                               (symbol? raw)
                                               (namespace raw))
                                      (symbol (namespace raw))))
                                  (vals reserved))))
                  set)))
          entries (vals (:code module))]
      (disj (apply set/union
                   #{}
                   (map (fn [e]
                          (set/union
                           (->> (:deps e)
                                (keep (fn [sym]
                                        (when-let [ns (namespace sym)]
                                          (symbol ns))))
                                set)
                           (polyfill-modules e)))
                        entries))
            (:id module)))))

(defn module-deps-all
  "gets dependencies for a given module (including explicit links)"
  {:added "4.0"}
  ([module]
   (module-deps-all nil module))
  ([book module]
   (let [module (if book
                  (module-derived-view book module)
                  module)]
     (disj (set/union (module-deps-code book module)
                      (set (vals (:link module))))
           (:id module)))))

(defn module-deps-native
  "gets dependencies for a given module"
  {:added "4.0"}
  ([module]
   (let [entries (vals (:code module))]
     (apply merge-with
            set/union
            (keep (fn [e]
                    (not-empty (:deps-native e)))
                  entries))))
  ([book module]
   (module-deps-native (if book
                         (module-derived-view book module)
                         module))))

(defn module-deps-fragment
  "gets dependencies for a given module"
  {:added "4.0"}
  ([module]
   (let [entries (vals (:code module))]
     (apply set/union
            (map :deps-fragment entries))))
  ([book module]
   (module-deps-fragment (if book
                            (module-derived-view book module)
                            module))))

(defn module-entries
  [module filter-keys]
  (->> (:code module) 
       (vals)
       (sort-by (juxt :priority :time :line))
       (keep (fn [{:keys [id op-key]
                   :as entry}]
               (if (filter-keys op-key)
                 [(list :% \" id \")
                  (symbol (str (:id module))
                          (name id))])))))



(comment
  h/atom:get
  #_
  (defn library-install-module
    "installs a module into the library"
    {:added "3.0"}
    ([{:keys [lang store] :as lib} module-id conf]
     (let [{:keys [link native alias code fragment]} conf
           missing (h/difference (set (vals link))
                                 (set (conj (keys @store)
                                            module-id)))
           _  (if (not-empty missing)
                (h/error "Missing namespaces" {:ns missing}))
           module     (library-module (assoc conf
                                             :id   module-id
                                             :lang lang
                                             :alias     (or alias {})
                                             :link      (or link {})
                                             :native    (or native {})
                                             :code      (or code {})
                                             :fragment  (or fragment {})))]))))
