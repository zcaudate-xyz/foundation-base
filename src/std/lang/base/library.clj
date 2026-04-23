(ns std.lang.base.library
  (:require [std.concurrent :as cc]
            [std.lang.base.book :as b]
            [std.lang.base.book-entry :as e]
            [std.lang.base.book-module :as m]
            [std.lang.base.impl-entry :as entry]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.util :as ut]
            [std.lib.atom :as atom]
            [std.lib.collection :as collection]
            [std.lib.component :as component]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.lib.impl :as impl]
            [std.lib.os :as os]
            [std.protocol.component :as protocol.component]))

(def ^:dynamic *strict* false)

(def +ops+
  {:add-book    (fn [snapshot book]
                  [(:lang book) (snap/add-book snapshot book)])
   :delete-book (fn [snapshot lang]
                  [lang (dissoc snapshot lang)])
   :set-module     snap/set-module
   :delete-module  snap/delete-module
   :delete-entry   snap/delete-entry})

(defn wait-snapshot
  "gets the current waiting snapshot
 
   (lib/wait-snapshot +library+)
   => snap/snapshot?
 
   (meta (lib/wait-snapshot +library+))
   => {:parent nil}"
  {:added "4.0"}
  [lib]
  (let [parent (if-let [p (:parent lib)]
                 (wait-snapshot (if (or (var? p)(fn? p))
                                  (p)
                                  p)))
        #_#__    (cc/hub:wait (get-in lib [:dispatch :runtime :queue]))]
    (with-meta (snap/snapshot-merge parent @(:instance lib))
      {:parent parent})))

(defn wait-apply
  "get the library state when task queue is empty"
  {:added "4.0"}
  [lib f & args]
  (let [snapshot (wait-snapshot lib)]
    (apply f snapshot args)))

(defn wait-mutate!
  "mutates library once task queue is empty"
  {:added "4.0"}
  [lib f & args]
  (let [{:keys [parent]} (meta (wait-snapshot lib))]
    (atom/swap-return! (:instance lib)
      (fn [snapshot]
        (binding [snap/*parent* parent]
          (let [out (apply f snapshot args)
                _   (when *strict*
                      (collection/map-vals (comp snap/install-check-merged :book) (second out)))]
            out))))))

(defn get-snapshot
  "gets the current snapshot for the library"
  {:added "4.0"}
  [lib]
  (wait-apply lib identity))

(defn get-book
  "gets a book from library"
  {:added "4.0"}
  [lib lang]
  (wait-apply lib snap/get-book lang))

(defn get-book-raw
  "gets the raw book, without merge
  
   (b/list-entries (lib/get-book-raw +library+ :lua.redis))
   => empty?
   
   (b/list-entries (lib/get-book +library+ :lua.redis))
   => coll?
   
   (b/list-entries (lib/get-book +library+ :lua.nginx))
   => coll?"
  {:added "4.0"}
  [lib lang]
  (wait-apply lib snap/get-book-raw lang))

(defn get-module
  "gets a module from library"
  {:added "4.0"}
  [lib lang module-id]
  (b/get-entry (wait-apply lib snap/get-book lang)
               module-id))

(defn get-entry
  "gets an entry from library"
  {:added "4.0"}
  [lib {:keys [lang id module section]
        :as entry}]
  (b/get-base-entry (wait-apply lib snap/get-book lang)
                    module id (or section
                                  :code)))

(defn add-book!
  "adds a book to the library"
  {:added "4.0"}
  [lib book]
  (wait-mutate! lib
               (fn [snapshot book]
                 [book (snap/add-book snapshot book)])
               book))

(defn delete-book!
  "deletes a book"
  {:added "4.0"}
  [lib lang]
  (wait-mutate! lib
                (fn [snapshot lang]
                  [(get snapshot lang) (dissoc snapshot lang)])
               lang))

(defn reset-all!
  "resets the library"
  {:added "4.0"}
  ([lib]
   (reset-all! lib nil))
  ([lib new-snapshot]
   (wait-mutate! lib (fn [snapshot]
                      [snapshot (or new-snapshot
                                    (snap/snapshot {}))]))))

(defn list-modules
  "lists all modules"
  {:added "4.0"}
  [lib lang]
  (wait-apply lib snap/list-modules lang))

(defn list-entries
  "lists entries"
  {:added "4.0"}
  ([lib lang]
   (wait-apply lib snap/list-entries lang))
  ([lib lang module-id]
   (wait-apply lib snap/list-entries lang module-id))
  ([lib lang module-id section]
   (wait-apply lib snap/list-entries lang module-id section)))

(defn add-module!
  "adds a module to the library
 
   (lib/add-module! +library+ (module/book-module '{:lang :lua.redis
                                                    :id L.redis.hello
                                                    :link {r L.redis
                                                           u L.core}}))
   => coll?
   
   (lib/delete-module! +library+ :lua.redis 'L.redis.hello )
   => coll?"
  {:added "4.0"}
  [lib module]
  (wait-mutate! lib snap/set-module module))

(defn delete-module!
  "deletes a module from the library"
  {:added "4.0"}
  [lib lang module-id]
  (wait-mutate! lib snap/delete-module lang module-id))

(defn delete-modules!
  "deletes a bunch of modules from the library"
  {:added "4.0"}
  [lib lang module-ids]
  (wait-mutate! lib snap/delete-modules lang module-ids))

(defn library-string
  "returns the library string"
  {:added "4.0"}
  ([{:keys [id parent instance]}]
   (str "#lib " (vec (sort (keys @instance))))))

(impl/defimpl Library [id instance]
  :final true
  :string library-string
  #_#_
  :protocols [protocol.component/IComponent
              :body {-start (do (component/start (:dispatch component))
                                component)
                     -stop  (do (component/stop (:dispatch component))
                                component)
                     -kill  (do (component/kill (:dispatch component))
                                component)}])

(defn library?
  "checks if object is a library"
  {:added "4.0"}
  ([obj]
   (instance? Library obj)))

(defn library:create
  "creates a new library"
  {:added "4.0"}
  ([{:keys [id parent snapshot] :as m}]
   (let [instance (atom (or snapshot
                            (snap/snapshot {})))
         settings (atom {})
         library  (map->Library {:parent parent
                                 #_#_:dispatch (create-dispatch instance settings)
                                 :settings settings
                                 :instance instance})
         _ (swap! settings assoc :library library)]
     library)))

(defn library
  "creates and start a new library"
  {:added "4.0"}
  [{:keys [id parent snapshot] :as m}]
  (-> (library:create m)
      (component/start)))

(defn add-entry!
  "adds the entry with the bulk dispatcher"
  {:added "4.0"}
  [{:keys [dispatch] :as lib} entry]
  (dispatch entry))

(defn add-entry-single!
  "adds an entry synchronously"
  {:added "4.0"}
  [lib entry & [opts]]
  (wait-mutate! lib snap/set-entry entry opts))

(defn delete-entry!
  "deletes an entry from the library"
  {:added "4.0"}
  [lib {:keys [lang module section id] :as m}]
  (wait-mutate! lib snap/delete-entry m))

;;
;;
;;

(defn install-module!
  "installs a module to library"
  {:added "4.0"}
  [lib lang module-id options]
  (wait-mutate! lib
                (fn [snapshot]
                  (let [out (snap/install-module snapshot lang module-id options)]
                    [out (first out)]))))

(defn install-book!
  "installs a book to library"
  {:added "4.0"}
  [lib book]
  (wait-mutate! lib
                (fn [snapshot]
                  (let [out (snap/install-book snapshot book)]
                    [out (first out)]))))

(defn purge-book!
  "clears all modules from book"
  {:added "4.0"}
  [lib lang]
  (wait-mutate! lib
                (fn [snapshot]
                  (let [book (assoc (or (snap/get-book-raw snapshot lang)
                                        (f/error "Book not found" {:lang lang}))
                                    :modules {})]
                    [book (snap/add-book snapshot book)]))))

(comment
  [std.dispatch :as dispatch]

  (defn create-dispatch-handler
    "the actual dispatch handler"
    {:added "4.0"}
    [instance settings dispatch entries]
    (let [{:keys [library] :as opts} @settings]
      (try
        (wait-mutate! library snap/set-entries entries (dissoc opts :library))
        (catch Throwable t
          (os/beep)
          (env/prn t)))))

  (defn create-dispatch
    "creates the dispatch for adding entries in bulk"
    {:added "4.0"}
    [instance settings]
    (dispatch/create
     {:type :queue
      :handler (fn [m entries]
                 (create-dispatch-handler instance settings m entries))
      :hooks   {:on-startup (fn [_])
                :on-process (fn [_ entries])}
      :options {:queue {:max-batch 500
                        :interval 100
                        :delay 10}}})))

(comment
  (./import))
