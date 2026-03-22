(ns std.image.base.model
  (:require [std.lib.collection]))

;; Generated Using
;;
;; (->> (.? java.awt.image.BufferedImage #"TYPE" :name)
;;      (map (comp keyword
;;                 #(subs % 5)
;;                 std.string.str/spear-case)))

(def model-lookup
  {:standard-gray    {:type :gray
                      :meta [{:type Byte/TYPE :span 1}]
                      :channel {:count 1
                                :fn  (fn [{:keys [raw]}] [raw])
                                :inv (fn [[raw]] {:raw raw})}
                      :data {:raw   {:channel 0}}}

   :standard-argb    {:type :color
                      :meta [{:type Byte/TYPE :span 1}
                             {:type Byte/TYPE :span 1}
                             {:type Byte/TYPE :span 1}
                             {:type Byte/TYPE :span 1}]
                      :channel {:count 4
                                :fn  (fn [{:keys [alpha red green blue]}]
                                       [alpha red green blue])
                                :inv (fn [[alpha red green blue]]
                                       {:alpha alpha :red red :green green :blue blue})}
                      :data {:alpha {:channel 0}
                             :red   {:channel 1}
                             :green {:channel 2}
                             :blue  {:channel 3}}}

   :3-byte-rgb       {:type :color
                      :meta [{:type Byte/TYPE :span 3}]
                      :data {:red   {:index 0}
                             :green {:index 1}
                             :blue  {:index 2}}}

   :3-byte-bgr       {:type :color
                      :meta [{:type Byte/TYPE :span 3}]
                      :data {:red   {:index 2}
                             :green {:index 1}
                             :blue  {:index 0}}}

   :4-byte-argb      {:type :color
                      :meta [{:type Byte/TYPE :span 4}]
                      :data {:alpha {:index 0}
                             :red   {:index 1}
                             :green {:index 2}
                             :blue  {:index 3}}}

   :4-byte-abgr      {:type :color
                      :meta [{:type Byte/TYPE :span 4}]
                      :data {:alpha {:index 3}
                             :red   {:index 0}
                             :green {:index 1}
                             :blue  {:index 2}}}

   :int-argb         {:type :color
                      :meta [{:type Integer/TYPE :span 1}]
                      :data {:alpha {:access [24 8]}
                             :red   {:access [16 8]}
                             :green {:access [8  8]}
                             :blue  {:access [0  8]}}}

   :int-bgr          {:type :color
                      :meta [{:type Integer/TYPE :span 1}]
                      :data {:red   {:access [8  8]}
                             :green {:access [16 8]}
                             :blue  {:access [24 8]}}}

   :int-rgb          {:type :color
                      :meta [{:type Short/TYPE :span 1}]
                      :data {:red   {:access [24 8]}
                             :green {:access [16 8]}
                             :blue  {:access [8  8]}}}

   :ushort-555-rgb   {:type :color
                      :meta [{:type Short/TYPE :span 1}]
                      :data {:red   {:access [10 5]}
                             :green {:access [5  5]}
                             :blue  {:access [0  5]}}}

   :ushort-565-rgb   {:type :color
                      :meta [{:type Short/TYPE :span 1}]
                      :data {:red   {:access [11 5]}
                             :green {:access [5  6]}
                             :blue  {:access [0  5]}}}

   :byte-binary      {:type :gray
                      :meta [{:type Byte/TYPE :span 1}]
                      :data {:raw  {}}}

   :byte-gray        {:type :gray
                      :meta [{:type Byte/TYPE :span 1}]
                      :data {:raw  {}}}

   :ushort-gray      {:type :gray
                      :meta [{:type Short/TYPE :span 1}]
                      :data {:raw  {}}}

   :3ch-byte-rgb     {:type :color
                      :meta [{:type Byte/TYPE :span 1}
                             {:type Byte/TYPE :span 1}
                             {:type Byte/TYPE :span 1}]
                      :channel {:count 3 :fn identity :inv identity}
                      :data {:red   {:channel 0}
                             :green {:channel 1}
                             :blue  {:channel 2}}}

   :4ch-byte-argb    {:type :color
                      :meta [{:type Byte/TYPE :span 1}
                             {:type Byte/TYPE :span 1}
                             {:type Byte/TYPE :span 1}
                             {:type Byte/TYPE :span 1}]
                      :channel {:count 4 :fn identity :inv identity}
                      :data {:alpha {:channel 0}
                             :red   {:channel 1}
                             :green {:channel 2}
                             :blue  {:channel 3}}}})

(def ^:dynamic *default-channel-options*
  {:count 1 :fn vector :inv first})

(def ^:dynamic *default-data-options*
  {:type Byte/TYPE :channel 0 :index 0})

(def ^:dynamic *default-indices*
  {:color {:alpha 0
           :red   1
           :green 2
           :blue  3}
   :gray  {:raw 0}})

(defn create-model
  "creates a predefined image model given a label
 
   (create-model :ushort-555-rgb)
   => {:type :color
       :label :ushort-555-rgb
       :channel {:count 1 :fn vector :inv first}
       :meta [{:type Short/TYPE :span 1}]
       :data  {:red {:type Byte/TYPE
                     :channel 0
                     :index 0
                    :access [10 5]}
               :green {:type Byte/TYPE
                       :channel 0
                       :index 0
                       :access [5 5]}
               :blue {:type Byte/TYPE
                      :channel 0
                      :index 0
                      :access [0 5]}}}"
  {:added "3.0"}
  ([label]
   (if-let [model (model-lookup label)]
     (-> model
         (assoc :label label)
         (update-in [:channel] #(merge *default-channel-options* %))
         (update-in [:data] (fn [data]
                              (std.lib.collection/map-vals (fn [opts]
                                            (merge *default-data-options* opts))
                                          data)))))))

(def ^:dynamic *defaults*
  {:color     (create-model :standard-argb)
   :gray      (create-model :standard-gray)})

(defn model-inv-table
  "creates a inverse access table for setting data within an image
 
   ;; channel 0, 0th index = (i1>>3)<<11 + (i2>>2)<<6 + i3>>3 
   (model-inv-table (model :ushort-565-rgb))
   => {0 {0 {1 [11 5],
             2 [5 6],
             3 [0 5]}}}"
  {:added "3.0"}
  ([{:keys [meta data type]}]
   (let [paths (reduce-kv
                (fn [out k {:keys [channel index access]}]
                  (let [path [channel index (get-in *default-indices* [type k])]]
                    (assoc-in out path access)))
                {}
                data)]
     paths)))

(defn model
  "creates a model with overwrites
 
   (model)
   => [:3-byte-bgr :3-byte-rgb :3ch-byte-rgb
       :4-byte-abgr :4-byte-argb :4ch-byte-argb
       :byte-binary :byte-gray
       :int-argb :int-bgr :int-rgb
       :standard-argb :standard-gray
       :ushort-555-rgb :ushort-565-rgb :ushort-gray]
 
   (model :int-rgb {:type :stuff
                    :data :none
                    :channel {:hello :world}})
   => (contains-in {:type :stuff,
                    :data :none,
                    :channel {:count 1,
                              :hello :world}})"
  {:added "3.0"}
  ([] (vec (sort (keys model-lookup))))
  ([x]
   (model x {}))
  ([x overwrites]
   (let [model (cond (keyword? x) (create-model x)
                     (map? x) x
                     :else (throw (Exception. (str "Use keyword or map, not " x))))]
     (std.lib.collection/merge-nested model overwrites))))
