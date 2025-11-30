(ns std.lang.base.library-snapshot
  (:require [std.protocol.deps :as protocol.deps]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.impl-entry :as entry]
            [std.lib.atom :as atom]
            [std.lib :as h :refer [defimpl]]))

(def ^:dynamic *parent* nil)

(defn get-deps
  "gets a dependency chain"
  {:added "4.0"}
  ([snapshot id]
   (if-let [parent (get-in snapshot [id :book :parent])]
     #{parent}
     #{})))

(defn snapshot-string
  "gets the snapshot string"
  {:added "4.0"}
  ([snapshot]
   (str "#lib.snapshot " (vec (sort (keys snapshot))))))

(defimpl Snapshot []
  :final true
  :string snapshot-string
  :protocols [protocol.deps/IDeps
              :method {-get-entry  get
                       -list-entries keys}])

(defn snapshot?
  "checks if object is a snapshot"
  {:added "4.0"}
  ([obj]
   (instance? Snapshot obj)))

(defn snapshot
  "creates a snapshot"
  {:added "4.0"}
  ([m]
   (map->Snapshot m)))



;;
;;
;;

(defn snapshot-reset
  "resets a snapshot to it's blanked modules"
  {:added "4.0"}
  ([snapshot]
   (snapshot-reset snapshot nil))
  ([snapshot ks]
   (map->Snapshot
    (h/map-vals (fn [m]
                  (assoc-in m [:book :modules] {}))
                (if ks
                  (select-keys snapshot ks)
                  snapshot)))))

(defn snapshot-merge
  "a rough merge of only the modules from the child to the parent"
  {:added "4.0"}
  ([parent child]
   (cond (not parent)
         child

         :else
         (reduce-kv (fn [parent lang {:keys [book]}]
                      (if (get parent lang)
                        (update-in parent [lang :book :modules] merge (:modules book))
                        (h/error "Parent has no book" {:lang lang})))
                    parent
                    child))))

;;
;; merge access
;;

(defn get-book-raw
  "gets the raw book"
  {:added "4.0"}
  [snapshot lang]
  (get-in snapshot [lang :book]))

(defn get-book
  "gets the merged book for a given language"
  {:added "4.0"}
  [snapshot lang]
  (or lang (h/error "Lang cannot be null." {:input lang}))
  (book/book-from snapshot lang))

;;
;; snapshot methods
;;

(defn add-book
  "adds a book to a snapshot"
  {:added "4.0"}
  [snapshot {:keys [lang] :as book}]
  (or lang (h/error "Lang cannot be null." {:input book}))
  (assoc snapshot lang {:id lang
                        :book book}))

(defn set-module
  "sets a module in the snapshot"
  {:added "4.0"}
  [snapshot module]
  (let [{:keys [lang]} module]
    (atom/update-diff snapshot [lang :book]
                      (fn [book]
                        (book/set-module book module)))))

(defn delete-module
  "deletes a module in the snapshot"
  {:added "4.0"}
  [snapshot lang module-id]
  (atom/update-diff snapshot [lang :book]
                    (fn [book]
                      (book/delete-module book module-id))))

(defn delete-modules
  "deletes a bunch of modules in the snapshot"
  {:added "4.0"}
  [snapshot lang module-ids]
  (atom/update-diff snapshot [lang :book]
                    (fn [book]
                      (book/delete-modules book module-ids))))

(defn list-modules
  "list modules for a snapshot"
  {:added "4.0"}
  [snapshot lang]
  (book/list-entries (get-book snapshot lang) :module))

(defn list-entries
  "lists entries for a snapshot"
  {:added "4.0"}
  ([snapshot lang]
   (book/list-entries (get-book snapshot lang) :code))
  ([snapshot lang module-id]
   (let [book (get-book snapshot lang)]
     (h/map-juxt [identity
                  (fn [section]
                    (keys (get-in book [:modules module-id section])))]
                 [:code :fragment])))
  ([snapshot lang module-id section]
   (-> (get-book snapshot lang)
       (get-in [:modules module-id section])
       keys)))

(defn set-entry
  "sets an entry in the snapshot"
  {:added "4.0"}
  [snapshot entry & [mopts]]
  (let [{:keys [lang module section]} entry
        shadow-snapshot (snapshot-merge *parent* snapshot)
        {:keys [grammar modules]
         :as book} (get-book shadow-snapshot lang)
        mopts  (merge {:lang lang
                       :snapshot shadow-snapshot
                       :module (book/get-module book module)}
                      mopts)
        entry  (if (= section :fragment)
                 (entry/create-fragment-hydrate entry
                                                grammar
                                                modules
                                                mopts)
                 
                 (entry/create-code-hydrate entry
                                            (get-in grammar [:reserved (:op entry)])
                                            grammar
                                            modules
                                            mopts))]
    (atom/update-diff snapshot [lang :book]
                      book/set-entry entry)))

(defn set-entries
  "sets an entry in the snapshot"
  {:added "4.0"}
  [snapshot entries & [opts]]
  (reduce (fn [[diffs snapshot] {:keys [namespace] :as entry}]
            (binding [*ns* (the-ns namespace)]
              (let [[diff new-snapshot] (set-entry snapshot entry opts)]
                [(concat diffs diff) new-snapshot])))
          [[] snapshot]
          entries))

(defn delete-entry
  "deletes an entry from the snapshot"
  {:added "4.0"}
  [snapshot {:keys [lang module section id]}]
  (atom/update-diff snapshot [lang :book]
                    (fn [book]
                      (book/delete-entry book module section id))))

(defn delete-entries
  "delete entries from the snapshot"
  {:added "4.0"}
  [snapshot entries]
  (reduce (fn [[diffs snapshot] entry]
            (let [[diff new-snapshot] (delete-entry snapshot entry)]
              [(concat diffs diff) new-snapshot]))
          [[] snapshot]
          entries))


;;
;;
;;

(defn install-check-merged
  "checks that the book is not merged (used to check mutate)"
  {:added "4.0"}
  [book & [module-id]]
  (when (< 1 (count (:merged book)))
    (h/prn  [module-id (:merged book)
             (sort (keys (:modules book)))])
    (h/error "MERGED BOOK DISALLOWED")))

(defn install-module-update
  "updates the book module"
  {:added "4.0"}
  [book {module-id :id
         :as module}]
  (let [soft-keys    [:code :fragment #_#_:bundle :export #_:macro-only]
        soft-maps    (select-keys module soft-keys)
        hard-maps    (apply dissoc module soft-keys)
        
        [soft-diffs new-book] (atom/atom-put-fn book [:modules module-id] soft-maps)
        [hard-diffs new-book] (atom/atom-set-keys-fn new-book [:modules module-id] hard-maps)
        
        soft-changes (atom/atom:put-changed soft-diffs)
        hard-changes (atom/atom:set-changed hard-diffs)
        all-changes  (merge  (second soft-changes)
                             (second hard-changes))]
    (if (empty? all-changes)
      [new-book :no-change]
      [new-book :changed all-changes]))) 


(defn install-module
  "adds an new module or update fields if exists"
  {:added "4.0"}
  ([snapshot lang module-id options]
   (let [new-module (-> (get-book snapshot lang)
                        (book/module-create module-id (assoc options
                                                             :lang lang
                                                             :id module-id)))
         book       (get-book-raw snapshot lang)
         [new-book status changes]  (if (book/has-module? book module-id)
                                      (install-module-update book new-module)
                                      [(second (book/set-module book new-module)) :new new-module])
         new-snapshot (if (= status :no-change)
                        snapshot
                        (assoc snapshot lang {:id lang
                                              :book new-book}))]
     [new-snapshot status changes])))

;;
;;
;;

(defn install-book-update
  "updates the book grammar, meta and parent"
  {:added "4.0"}
  [snapshot {:keys [lang grammar parent meta]}]
  (let [new-book  (-> (get-book-raw snapshot lang)
                      (assoc :meta meta))
        [diffs new-book] (atom/atom-set-keys-fn new-book []
                                                {:parent parent
                                                 :grammar grammar})]
    (if (empty? diffs)
      [(add-book snapshot new-book) :no-change]
      (vec (cons (add-book snapshot new-book) (atom/atom:set-changed diffs))))))

(defn install-book
  "adds a new book or updates grammar if exists"
  {:added "4.0"}
  [snapshot {:keys [lang parent] :as new-book}]
  (let [_ (if parent
            (or (get-book-raw snapshot parent)
                (h/error "Parent not installed"
                         {:options (keys snapshot)})))
        _ (install-check-merged new-book nil)]
    (if (not (get-book-raw snapshot lang))
      [(add-book snapshot new-book) :new new-book]
      (install-book-update snapshot new-book))))
