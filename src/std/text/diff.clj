(ns std.text.diff
  (:require [std.string :as str]
            [std.print.ansi :as ansi]
            [std.lib.enum :as enum]
            [std.object :as object])
  (:import (difflib ChangeDelta Chunk DeleteDelta Delta
                    Delta$TYPE DiffUtils InsertDelta Patch)))

(defn ^Patch create-patch
  "creates a Patch object
 
   (create-patch [{:type \"CHANGE\"
                   :original {:position 1
                              :lines [\"hello\"]}
                   :revised {:position 1
                             :lines [\"world\"]}}
                  {:type \"DELETE\"
                   :original {:position 1
                              :lines [\"again\"]}
                  :revised {:position 1
                             :lines []}}
                  {:type \"INSERT\"
                   :original {:position 1
                              :lines []}
                   :revised {:position 1
                             :lines [\"again\"]}}])
   => Patch"
  {:added "3.0"}
  ([arr]
   (let [p (Patch.)]
     (doseq [delta arr]
       (.addDelta p (object/from-data delta Delta)))
     p)))

(object/vector-like
 Patch
 {:tag "patch"
  :read  (fn [^Patch patch]
           (mapv object/to-data (.getDeltas patch)))
  :write create-patch})

(object/string-like
 Delta$TYPE
 {:tag "delta.type"
  :read  (fn [e] (str e))
  :write (fn [s] (enum/create-enum s Delta$TYPE))})

(defn create-delta
  "creates a Delta object from map
 
   (create-delta {:type \"CHANGE\"
                  :original {:position 1
                             :lines [\"hello\"]}
                  :revised {:position 1
                            :lines []}})
   => Delta"
  {:added "3.0"}
  ([{:keys [type original revised]}]
   (let [original (object/from-data original Chunk)
         revised  (object/from-data revised Chunk)]
     (case type
       "INSERT" (InsertDelta. original revised)
       "CHANGE" (ChangeDelta. original revised)
       "DELETE" (DeleteDelta. original revised)))))

(object/map-like
 Delta
 {:tag "delta"
  :include [:type :original :revised]
  :write {:from-map create-delta}})

(defn create-chunk
  "creates a Chunk object from map
 
   (create-chunk {:position 1
                  :lines [\"hello\"]})
   => Chunk"
  {:added "3.0"}
  ([{:keys [^long position ^java.util.List lines]}]
   (Chunk. position lines)))

(object/map-like
 Chunk
 {:tag "chunk"
  :read  {:methods (merge (object/read-getters Chunk)
                          {:count {:type Long
                                   :fn (fn [^Chunk c] (count (.getLines c)))}})}
  :write {:from-map create-chunk}})

(defn diff
  "returns a set of diff results
 
   (diff \"10\\n11\\n\12\\n13\\n14\"
         \"10\\n18\\n\12\\n19\\n14\")
   => [{:type \"CHANGE\",
        :original {:count 1,
                   :position 1,
                   :lines [\"11\"]},
        :revised {:count 1,
                  :position 1,
                 :lines [\"18\"]}}
       {:type \"CHANGE\",
        :original {:count 1,
                   :position 4,
                   :lines [\"13\"]},
        :revised {:count 1,
                  :position 4,
                  :lines [\"19\"]}}]"
  {:added "3.0"}
  ([original revised]
   (-> (DiffUtils/diff (str/split-lines original)
                       (str/split-lines revised))
       (object/to-data))))

(defn patch
  "takes a series of deltas and returns the revised result
 
   (patch \"10\\n11\\n\12\\n13\\n14\"
          [{:type \"CHANGE\",
            :original {:count 1,
                       :position 1,
                       :lines [\"11\"]},
            :revised {:count 1,
                      :position 1,
                      :lines [\"18\"]}}
          {:type \"CHANGE\",
            :original {:count 1,
                       :position 4,
                       :lines [\"13\"]},
            :revised {:count 1,
                      :position 4,
                      :lines [\"19\"]}}])
   => \"10\\n18\\n\\n\\n19\\n14\""
  {:added "3.0"}
  ([original diff]
   (let [p  (create-patch diff)
         revised   (.applyTo p (str/split-lines original))]
     (str/joinl revised "\n"))))

(defn unpatch
  "takes a series of deltas and restores the original result
 
   (unpatch \"10\\n18\\n\\n\\n19\\n14\"
            [{:type \"CHANGE\",
              :original {:count 1,
                         :position 1,
                         :lines [\"11\"]},
              :revised {:count 1,
                        :position 1,
                        :lines [\"18\"]}}
            {:type \"CHANGE\",
              :original {:count 1,
                         :position 4,
                         :lines [\"13\"]},
              :revised {:count 1,
                        :position 4,
                        :lines [\"19\"]}}])
   => \"10\\n11\\n\12\\n13\\n14\""
  {:added "3.0"}
  ([revised diff]
   (let [p  (create-patch diff)
         revised   (.restore p (str/split-lines revised))]
     (str/joinl revised "\n"))))

(defn ->string
  "returns a human readable output of the diffs
 
   (println (-> (diff \"10\\n11\\n\12\\n13\\n14\"
                      \"10\\n18\\n\12\\n19\\n14\")
               (->string)))"
  {:added "3.0"}
  ([deltas]
   (-> (mapcat (fn [{:keys [label original revised]}]
                 (concat
                  [(format (ansi/bold "@@ -%s,%s +%s,%s %s")
                           (:position original)
                           (:count original)
                           (:position revised)
                           (:count revised)
                           (if label (ansi/white ":: " (ansi/underline label)) ""))]
                  (map #(ansi/red "-" (ansi/on-grey %))
                       (:lines original))
                  (map #(ansi/green "+" (ansi/on-grey %))
                       (:lines revised))
                  [""]))
               deltas)
       (str/joinl "\n"))))

(defn summary
  "creates a summary of the various diffs
 
   (summary (diff \"10\\n11\\n\12\\n13\\n14\"
                  \"10\\n18\\n\12\\n19\\n14\"))
   => {:deletes 2, :inserts 2}"
  {:added "3.0"}
  ([deltas]
   (reduce (fn [out {:keys [original revised]}]
             (-> out
                 (update-in [:deletes] (fnil #(+ % (:count original)) 0))
                 (update-in [:inserts] (fnil #(+ % (:count revised)) 0))))
           {}
           deltas)))

(defn get-lines
  "creates a summary of the various diffs
 
   (summary (diff \"10\\n11\\n\12\\n13\\n14\"
                  \"10\\n18\\n\12\\n19\\n14\"))
   => {:deletes 2, :inserts 2}"
  {:added "3.0"}
  ([deltas]
   (mapv (fn [{:keys [type original revised]}]
           (case type
             "CHANGE" [(:position original) (:count original)]
             "INSERT" ["+" (:position revised)  (:count revised)]
             "DELETE" ["-" (:position original) (:count original)] ))
        deltas)))
