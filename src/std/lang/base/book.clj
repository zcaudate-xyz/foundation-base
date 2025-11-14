(ns std.lang.base.book
  (:require [std.protocol.deps :as protocol.deps]
            [std.lang.base.book-module :as module]
            [std.lang.base.book-entry :as entry]
            [std.lang.base.book-meta :as meta]
            [std.lang.base.util :as ut]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.string :as str]
            [std.lib.atom :as atom]
            [std.lib :as h :refer [defimpl]]))

(h/intern-in module/book-module
             module/book-module?
             entry/book-entry
             entry/book-entry?
             meta/book-meta
             meta/book-meta?)

(def ^:dynamic *skip-check* false)

(def ^:dynamic *dep-types* :code)

(defn get-base-entry
  "gets an entry in the book"
  {:added "4.0"}
  ([{:keys [modules] :as book} module-id id section]
   (get-in modules [module-id section id])))

(defn get-code-entry
  "gets a code entry in the book"
  {:added "4.0"}
  ([{:keys [modules] :as book} id]
   (let [[mid sid] (ut/sym-pair id)]
     (or (get-base-entry book mid sid :code)
         (get-base-entry book mid sid :header)
         (h/error "ENTRY NOT FOUND"
                  {:module mid
                   :symbol sid})))))

(defn get-entry
  "gets either the module or code entry"
  {:added "4.0"}
  ([{:keys [modules] :as book} id]
   (if (namespace id)
     (get-code-entry book id)
     (get modules id))))

(defn get-module
  "gets the module"
  {:added "4.0"}
  ([{:keys [modules] :as book} id]
   (get modules id)))

(defn get-code-deps
  "gets `:deps` or if a `:static/template` calculate dependencies"
  {:added "4.0"}
  ([{:keys [modules grammar lang] :as book} id]
   (let [entry (get-code-entry book id)]
     (if-not (:static/template entry)
       (:deps entry)
       (get-in (swap! (:static/template.cache entry)
                      (fn [m]
                        (if (get-in m [lang :deps])
                          m
                          (let [input (:form entry)
                                mopts {:modules modules
                                       :grammar grammar
                                       :entry entry}
                                
                                [form deps] (preprocess/to-staging input grammar modules mopts)]
                            (assoc-in m [lang :deps]  deps)))))
               [lang :deps])))))

(defn get-deps
  "get dependencies for a given id"
  {:added "4.0"}
  ([{:keys [modules] :as book} id]
   (if (namespace id)
     (get-code-deps book id)
     (module/module-deps (get modules id) id))))

(defn get-deps-native
  "get dependencies for a given id"
  {:added "4.0"}
  ([{:keys [modules] :as book} id]
   (if (namespace id)
     (get-code-deps book id)
     (module/module-deps (get modules id) id))))

(defn list-entries
  "lists entries for a given symbol"
  {:added "4.0"}
  ([book]
   (list-entries book *dep-types*))
  ([{:keys [modules]} dep-types]
   (case dep-types
     :module  (keys modules)
     :code    (mapcat (fn [[ns {:keys [code]}]]
                        (map (partial ut/sym-full ns) (keys code)))
                      modules))))

(defn book-string
  "shows the book string"
  {:added "4.0"}
  ([{:keys [lang
            meta
            grammar
            modules
            parent
            scope]}]
   (str "#book " [lang] " "
        (h/map-vals (fn [module]
                      (->> (select-keys module [:code :fragment])
                           (h/map-vals count)))
                    modules))))

(defimpl Book [lang
               meta
               grammar
               modules
               parent
               referenced]
  :final true
  :string book-string
  :protocols [protocol.deps/IDeps])

(defn book?
  "checks that object is a book"
  {:added "4.0"}
  ([obj]
   (instance? Book obj)))

(defn book
  "creates a book"
  {:added "4.0"}
  ([{:keys [lang
            meta
            grammar
            modules
            parent
            merged] :as m}]
   (assert (not-empty meta) "Meta required")
   (assert (not-empty grammar) "Grammer required")
   (map->Book (merge {:parent nil
                      :modules {}}
                     m
                     {:merged (conj (set merged) lang)}))))

(defn book-merge
  "merges a book with it's parent"
  {:added "4.0"}
  [book parent]
  (let [_  (or (= (:parent book)
                  (:lang parent))
               (h/error "Not mergable" {:book   (select-keys book   [:lang :parent :merged])
                                        :parent (select-keys parent [:lang :parent :merged])}))]
    (-> book
        (update :merged  conj (:lang parent))
        (update :modules (fn [m]
                           (reduce-kv
                            (fn [m k module]
                              (let [native (get m k)
                                    module (if native
                                             (assoc native
                                                    :code     (merge (:code module)
                                                                     (:code native))
                                                    :fragment (merge (:fragment module)
                                                                     (:fragment native)))
                                             module)]
                                (assoc m k module)))
                            m
                            (:modules parent))))
        (merge {:parent (:parent parent)}))))

(defn book-from
  "returns the merged book given snapshot"
  {:added "4.0"}
  [snapshot lang]
  (loop [{:keys [parent] :as book} (get-in snapshot [lang :book])]
    (if parent
      (recur (book-merge book (get-in snapshot [parent :book])))
      book)))

(defn check-compatible-lang
  "checks if the lang is compatible with the book"
  {:added "4.0"}
  [book lang]
  (boolean (or (=  (:lang book) lang)
               (get (:merged book) lang))))

(defn assert-compatible-lang
  "asserts that the lang is compatible"
  {:added "4.0"}
  [book lang]
  (or (check-compatible-lang book lang)
      (h/error "Not compatible" {:book (select-keys book   [:lang :parent :merged])
                                 :lang lang})))

(defn set-module
  "adds an addional module to the book"
  {:added "4.0"}
  [book module]
  (let [_ (assert-compatible-lang book (:lang module))
        {:keys [id]} module]
    (atom/atom-set-fn book [[[:modules id] module]])))

(defn put-module
  "adds or updates a module"
  {:added "4.0"}
  [book module]
  (let [_ (assert-compatible-lang book (:lang module))
        {:keys [id]} module]
    (atom/atom-put-fn book [:modules id] (h/filter-vals identity module))))

(defn delete-module
  "deletes a module given a book"
  {:added "4.0"}
  [book module-id]
  (atom/atom-delete-fn book [[:modules module-id]]))

(defn delete-modules
  "deletes all modules"
  {:added "4.0"}
  [book module-ids]
  (atom/atom-batch-fn book (mapv (fn [module-id]
                                   [:delete [:modules module-id]])
                                 module-ids)))

(defn has-module?
  "checks that a books has a given module"
  {:added "4.0"}
  [book module-id]
  (boolean (get-in book [:modules module-id])))

(defn assert-module
  "asserts that module exists"
  {:added "4.0"}
  [book module-id]
  (or (has-module? book module-id)
      (h/error "No module found." {:available (set (list-entries book :module))
                                   :module module-id})))

(defn set-entry
  "sets entry in the book"
  {:added "4.0"}
  [book entry]
  (let [_ (assert-compatible-lang book (:lang entry))
        _ (assert-module book (:module entry))
        {:keys [id module section]} entry]
    (atom/atom-set-fn book [[[:modules module section id] entry]])))

(defn put-entry
  "updates entry value in the book"
  {:added "4.0"}
  [book entry]
  (let [_ (assert-compatible-lang book (:lang entry))
        _ (assert-module book (:module entry))
        {:keys [id module section]} entry]
    (atom/atom-put-fn book [:modules module section id] (h/filter-vals identity entry))))

(defn has-entry?
  "checks that book has an entry"
  {:added "4.0"}
  [book module-id section symbol-id]
  (boolean (get-in book [:modules module-id  section symbol-id])))

(defn assert-entry
  "asserts that module exists"
  {:added "4.0"}
  [book module-id section symbol-id]
  (or (has-entry? book module-id section symbol-id)
      (and (assert-module book module-id)
           (h/error "No entry found." {:available (set (keys (get (get-entry book module-id)
                                                                  section)))
                                       :section section
                                       :module module-id}))))

(defn delete-entry
  "deletes an entry"
  {:added "4.0"}
  [book module-id section symbol-id]
  (atom/atom-delete-fn book [[:modules module-id section symbol-id]]))


;;
;;
;;

(defn module-create-bundled
  "creates bundled packages given input modules"
  {:added "4.0"}
  [book requires]
  (h/map-vals  (fn [{:keys [id suppress no-default include]}]
                 (let [include  (cond-> (set (flatten include))
                                  (not no-default) (conj :default))
                       {:keys [macro-only bundle]} (get-module book id)
                       suppress (or suppress
                                    (and macro-only (not (:fn include))))
                       native  (mapcat (fn bundle-fn [[k e]]
                                         (cond (not (include k)) nil
                                               (map? e) (mapcat bundle-fn e)
                                               (vector? e) e))
                                       bundle)]
                   {:suppress suppress
                    :native   native}))
               requires))

(defn module-create-filename
  "creates a filename for module"
  {:added "4.0"}
  [book module-id]
  (let [suffix   (or (get-in book [:file :suffix])
                     (name (get-in book [:grammar :tag])))]
    (str (last (str/split (name module-id) #"\."))
         "." suffix)))

(defn module-create-check
  "checks that bundles are available"
  {:added "4.0"}
  [{:keys [modules] :as book} module-id link]
  (let [missing (h/difference (set (vals link))
                              (set (conj (keys modules)
                                         module-id)))]
    (if (not-empty missing)
      (h/error "Missing namespaces" {:ns missing
                                     :link link
                                     :all (keys modules)})
      true)))

(defn module-create-requires
  "creates a map for the requires"
  {:added "4.0"}
  [require]
  (h/map-juxt [first #(apply hash-map (cons :id %))] require))

(defn module-create
  "creates a module given book and options"
  {:added "4.0"}
  ([{:keys [lang] :as book} module-id options]
   (let [_ (or book      (h/error "Book is required." {:lang lang
                                                       :module module-id}))
         _ (or module-id (h/error "Module id is required." {:lang lang
                                                            :book book
                                                            :module module-id}))
         {:keys [require
                 require-impl
                 import alias
                 export file
                 static]} options
         requires  (module-create-requires require)
         export    (if export
                     (merge {:as (first export)}
                            (apply hash-map (rest export))))
         internal (-> (h/map-entries (fn [[id {:keys [as]}]] [id (or as id)])
                                     requires)
                      (assoc module-id '-))
         link     (-> (h/map-entries (fn [[id {:keys [as]}]]
                                       [(if (vector? as)
                                          (last as)
                                          (or as id))
                                        id])
                                     requires)
                      (assoc '- module-id))
         internal (h/transpose link)
         native     (h/map-juxt [first #(apply hash-map (rest %))]
                                import)
         native-lu  (->> native
                         (mapcat (fn [[import {:keys [as]}]]
                                   (->> (disj (set (flatten (seq (h/seqify as)))) '*)
                                        (filter symbol?)
                                        (map (fn [sym] [sym import])))))
                         (into {}))
         alias    (merge (h/map-juxt [#(nth % 2) first] alias)
                         (->> (vals native)
                              (map :as)
                              (keep (fn [x] (if (vector? x) (last x) x)))
                              (h/map-juxt [identity identity])))
         _          (if (not *skip-check*) (module-create-check book module-id link))]
     (module/book-module {:lang lang
                          :id module-id
                          
                          ;; link 
                          :alias      alias
                          :link       link
                          :internal   internal
                          :native     native
                          :native-lu  native-lu
                          :require-impl require-impl
                          
                          :export     export
                          :file file

                          :static static}))))

(comment
  
  (dissoc (std.lang/get-module
           (std.lang/default-library)
           :js
           'js.lib.puck)
          :fragment)
  
  (dissoc (std.lang/get-module
           (std.lang/default-library)
           :js
           'js.core)
          #_:fragment
          :code
          )
  )
