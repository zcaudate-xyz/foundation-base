(ns rt.postgres.grammar.common-tracker
  (:require [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.impl :as impl]
            [std.string.case :as case]))

(defn tracker-string
  ([{:keys [name disable in out] :as tracker}]
   (str "#pg.tracker [" name "] "
        (cond-> {}
          disable (assoc :disable disable)
          in   (assoc :in true)
          out  (assoc :out true)))))

(impl/defimpl Tracker []
  :string tracker-string)

(defn add-tracker
  "call to adjust data to that of the tracker"
  {:added "4.0"}
  ([params tracker spec op]
   (let [_ (if (get (:disable tracker) op)
             (f/error (str op " disabled for spec" {:op op
                                                    :spec spec})))]
     (cond (= :ignore (:track params))
           params
                 
           (and (:in tracker)
                (not (:track params))
                (not (get (:ignore tracker) op)))
           (f/error (str "`:track` key required for spec") {:spec spec})

           :else
           (assoc params :static/tracker tracker)))))

(defn tracker-map-in
  "creates the insert map"
  {:added "4.0"}
  ([{:static/keys [tracker]
     :keys [track]:as params}]
   (if-let [kmp (:create (:in tracker))]
     (collection/map-vals (fn [k]
                   (list :->> track (case/snake-case (name k))))
                 kmp))))

(defn tracker-map-modify
  "creates the modify map"
  {:added "4.0"}
  ([{:static/keys [tracker]
     :keys [track]:as params}]
   (if-let [kmp (:modify (:in tracker))]
     (collection/map-vals (fn [k]
                   (list :->> track (case/snake-case (name k))))
                 kmp))))
