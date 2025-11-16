(ns std.lang.base.book-module
  (:require [std.lib :as h :refer [defimpl]]
            [clojure.set :as set]))

(defn- book-module-string
  ([{:keys [lang id display] :as module}]
   (let [meta (->> (dissoc module :id :lang :code :fragment :display)
                   (h/filter-vals (fn [e]
                                    (if (coll? e)
                                      (not-empty e)
                                      e))))
         entries (->> (select-keys module [:code :fragment])
                      (h/map-vals (comp sort keys)))]
     (str "#book.module " [lang id] 
          (if (= :brief display)
            ""
            (str " "
                 {:meta meta
                  :entries entries}))))))
  
;;
;; 
;;

(defimpl BookModule [;; required
                     lang
                     id
                     
                     ;; dependency management
                     alias         ;; map of shortcuts to packages (both external and internal)
                     link          ;; map of internal dependencies
                     native        ;; packages that are native to the workspace runtime
                     native-lu     ;; lookup for native imports
                     internal      ;; set of required packages
                     
                     ;; actual code
                     
                     fragment   ;; macros
                     code       ;; main code

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
            
            fragment
            code

            static
            display] :as m}]
   (assert (not= nil lang) "Module :lang required")
   (assert (not= nil id)   "Module :id Required")
   (map->BookModule (merge { ;; Link
                            :alias {}
                            :link {}
                            :native {}
                            :require-impl []

                            ;; Code
                            :fragment {}
                            :code {}

                            ;; Misc
                            :static {}
                            :display :default}
                           m))))

(defn module-deps-code
  "gets dependencies for a given module"
  {:added "4.0"}
  ([module]
   (let [entries (vals (:code module))]
     (disj (apply set/union
                  (map (fn [e]
                         (set (map (comp symbol namespace) (:deps e))))
                       entries))
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
                  entries)))))

(defn module-deps-fragment
  "gets dependencies for a given module"
  {:added "4.0"}
  ([module]
   (let [entries (vals (:code module))]
     (apply set/union
            (map :deps-fragment entries)))))

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
