(ns std.lib.component.track
  (:require [std.lib.atom :as at]
            [std.lib.collection :as c]
            [std.lib.foundation :as h]
            [std.lib.impl :refer [defimpl]]
            [std.lib.time :as t]
            [std.protocol.track :as protocol.track]))

(declare untrack track)

(defonce ^:dynamic *metadata* nil)

(defonce ^:dynamic *registry* (atom {}))

(defonce ^:dynamic *actions* (atom {:identity identity
                                    :peek #(doto % prn)}))

;;
;;
;;

(extend-type Object
  protocol.track/ITrack
  (-track-path [_] nil))

(defn track-path
  "retrieves the track path
 
   (track-path (Store.))
   => [:test :store]"
  {:added "3.0"}
  ([obj]
   (try
     (protocol.track/-track-path obj)
     (catch java.lang.IllegalArgumentException e)
     (catch java.lang.AbstractMethodError e))))

(defn trackable?
  "checks if record implements `-track-path`
 
   (trackable? 1)
   => false
 
   (trackable? (Store.))
   => true"
  {:added "3.0"}
  ([obj]
   (not (nil? (track-path obj)))))

(defn- entry-string
  ([{:keys [time namespace component]}]
   (str "#track " {:t (t/format-ms (t/elapsed-ms time))
                   :ns namespace
                   :component (type component)})))

(defimpl TrackEntry [time component] :string entry-string)

(defn track-entry?
  "checks if object is a tracking entry"
  {:added "3.0"}
  ([obj]
   (instance? TrackEntry obj)))

(defn track-id
  "retrieves the track id and updates object if map
 
   (track-id (Store.))
   => (contains [string? map?])"
  {:added "3.0"}
  ([obj]
   (if (h/iobj? obj)
     (if-let [id (:track/id (meta obj))]
       [id obj]
       (let [id (h/sid)]
         [id (vary-meta obj merge {:track/id id})]))
     [(h/hash-id obj) obj])))

(defn track
  "tracks an object
 
   (track (Store.))
   => anything"
  {:added "3.0"}
  ([obj]
   (if-let [path (track-path obj)]
     (track obj path)
     obj))
  ([obj path]
   (track obj path *metadata*))
  ([obj path meta]
   (let [[id obj] (track-id obj)]
     (at/swap-return! *registry*
                      (fn [m]
                        (let [entry (-> (merge meta
                                               {:component obj
                                                :namespace (if (symbol? *ns*)
                                                             *ns*
                                                             (.getName ^clojure.lang.Namespace *ns*))
                                                :time (t/time-ms)})
                                        (map->TrackEntry))]
                          [entry (assoc-in m (conj path id) entry)])))
     obj)))

(defn tracked?
  "checks if object has been tracked"
  {:added "3.0"}
  ([obj]
   (boolean (if-let [path (track-path obj)]
              (let [[id _] (track-id obj)]
                (get-in @*registry* (conj path id)))))))

(defn untrack
  "untracks a given object"
  {:added "3.0"}
  ([obj]
   (if-let [path (track-path obj)]
     (untrack obj path)
     obj))
  ([obj path]
   (let [[id obj] (track-id obj)]
     (at/swap-return! *registry*
                      (fn [m]
                        (let [path (conj path id)
                              ret (get-in m path)]
                          [ret (c/dissoc-nested m path)])))
     (if (h/iobj? obj)
       (vary-meta obj dissoc :track/id)
       obj))))

(defn untrack-all
  "clears all tracked objects
 
   (untrack-all [:test :store])"
  {:added "3.0"}
  ([]
   (at/swap-return! *registry* (fn [m] [m {}])))
  ([path]
   (at/swap-return! *registry*
                    (fn [m]
                      [(get-in m path) (c/dissoc-nested m path)]))))

(defn tracked:action:add
  "adds an action
 
   (tracked:action:add :println println)"
  {:added "3.0"}
  ([key action & more]
   (let [actions (apply hash-map key action more)]
     (at/swap-return! *actions*
                      (fn [m]
                        [(keys actions) (merge m actions)])))))

(defn tracked:action:remove
  "removes an action
 
   (tracked:action:remove :println)"
  {:added "3.0"}
  ([key & more]
   (let [actions (apply list key more)]
     (at/swap-return! *actions*
                      (fn [m]
                        [actions (apply dissoc m actions)])))))

(defn tracked:action:get
  "gets an action
 
   (tracked:action:get :identity)
   => identity"
  {:added "3.0"}
  ([key]
   (or (get @*actions* key)
       (throw (ex-info "Cannot find key" {:input key})))))

(defn tracked:action:list
  "lists all actions
 
   (tracked:action:list)"
  {:added "3.0"}
  ([]
   (sort (keys @*actions*))))

(defn tracked:all
  "returns all tracked objects
 
   (tracked:all)
 
   (tracked:all [:test :store])"
  {:added "3.0"}
  ([]
   @*registry*)
  ([path]
   (get-in @*registry* (c/seqify path))))

(defn- tracked-action
  [f]
  (fn action [m]
    (if (keyword? (ffirst m))
      (let [m (c/map-vals action m)]
        (c/filter-vals (fn [v]
                         (if (map? v)
                           (not (empty? v))
                           v))
                       m))
      (f m))))

(defn tracked
  "returns and performs actions on tracked objects
 
   (tracked [:test] :identity)"
  {:added "3.0"}
  ([]
   (tracked []))
  ([path]
   (tracked path :identity))
  ([path action]
   (let [action (if (fn? action)
                  action
                  (tracked:action:get action))
         f (comp action :component)]
     ((tracked-action
       (fn [m] (c/map-vals f m)))
      (get-in @*registry* (c/seqify path))))))

(defn tracked:count
  "returns the count on the categories
 
   (tracked:count)"
  {:added "3.0"}
  ([]
   (tracked:count []))
  ([path]
   ((tracked-action count)
    (get-in @*registry* (c/seqify path)))))

(defn tracked:locate
  "locates given entries with tag"
  {:added "3.0"}
  ([meta]
   (tracked:locate [] meta))
  ([path meta]
   ((tracked-action
     (fn [m] (c/filter-vals (fn [entry]
                              (every? (fn [[k v]]
                                        (let [f (if (fn? v)
                                                  v
                                                  #{v})]
                                          (h/suppress (f (get entry k)))))
                                      meta))
                            m)))
    (get-in @*registry* (c/seqify path)))))

(defn tracked:list
  "outputs entries with tag as list
 
   (tracked:list {:tag :executor})
   => coll?"
  {:added "3.0"}
  ([meta]
   (tracked:list [] meta :identity))
  ([path meta action]
   (let [action (if (fn? action)
                  action
                  (tracked:action:get action))
         entries (tracked:locate path meta)
         output  (volatile! nil)]
     ((tracked-action (fn [m]
                        (vswap! output concat (vals m))))
      entries)
     (mapv (comp action :component) @output))))

(defn tracked:last
  "operates on the last tracked objects
 
   (tracked:last [] :identity 3)"
  {:added "3.0"}
  ([]
   (tracked:last []))
  ([path-or-action]
   (let [[path action] (if (or (keyword? path-or-action)
                               (fn? path-or-action))
                         [[] path-or-action]
                         [path-or-action :identity])]
     (tracked:last path action)))
  ([path action]
   (tracked:last path action 1))
  ([path action n]
   (let [action (if (fn? action)
                  action
                  (tracked:action:get action))
         f (comp action :component)
         entries (volatile! [])
         _ ((tracked-action (fn [m] (vswap! entries concat (vals m))))
            (get-in @*registry* (c/seqify path)))]
     (->> @entries
          (sort-by :time)
          (reverse)
          (take n)
          (mapv f)))))

(defmacro track:with-metadata
  "applies additional metadata to the tracking object"
  {:added "3.0" :style/indent 1}
  [[metadata] & body]
  `(binding [*metadata* (merge *metadata* ~metadata)]
     ~@body))

(tracked:action:add :untrack untrack
                    :track track)
