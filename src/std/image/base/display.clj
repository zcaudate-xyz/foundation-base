(ns std.image.base.display
  (:require [std.image.base.common :as common]
            [std.image.base.display.gradient :as gradient]
            [std.image.base.model :as model]
            [std.image.base.util :as util]
            [std.image.protocol :as protocol.image]
            [std.string.common]))

(defn render-string
  "render string based on rows containing values
 
   (render-string [[10 20 30 40]
                   [10 20 30 40]])
   ;;\"ÂNHœŠm\\nWNÀXŠm\"
   => string?"
  {:added "3.0"}
  ([rows]
   (render-string rows
                  gradient/*default-table*
                  gradient/*default-len*))
  ([rows table len]
   (->> rows
        (map (fn [row]
               (let [row (map #(bit-and % 255) row)
                     avg (->> (map + (butlast row) (next row))
                              (map #(int (/ % 2))))]
                 (->> (interleave row avg)
                      (map (fn [n] (gradient/lookup-char table len n)))
                      (apply str)))))
        (std.string.common/join "\n"))))

(defn byte-gray->rows
  "creates rows from byte-gray array
 
   (-> (byte-array [10 20 30 40])
       (byte-gray->rows [2 2])
       vec)
   => [[10 20] [30 40]]"
  {:added "3.0"}
  ([^bytes array size]
   (let [w (protocol.image/-width size)
         h (protocol.image/-height size)]
     (doall (for [i (range h)]
              (mapv (fn [j]
                      (let [t (+ (* w i) j)]
                        (aget array t)))
                    (range w)))))))

(defn render-byte-gray
  "creates an ascii string for the byte-gray array
 
   (render-byte-gray {:data (byte-array [10 20 30 40])
                      :size [2 2]})
   ;;\"Ã#\\nŠp\"
   => string?"
  {:added "3.0"}
  ([image]
   (render-byte-gray image {}))
  ([{:keys [data size] :as image} opts]
   (render-string (byte-gray->rows data size))))

(defn int-argb->rows
  "creates rows from int-argb array
 
   (-> (map util/bytes->int [[255 10 20 30] [255 40 50 60]])
       (int-array)
       (int-argb->rows [1 2]))
   => [[20] [50]]"
  {:added "3.0"}
  ([^ints array size]
   (let [w (protocol.image/-width size)
         h (protocol.image/-height size)]
     (doall (for [i (range h)]
              (mapv (fn [j]
                      (let [t (+ (* w i) j)
                            [a r g b] (util/int->bytes (aget  array t))]
                        (unchecked-byte (/ (* a (+ r g b)) 255 3))))
                    (range w)))))))

(defn render-int-argb
  "creates an ascii string for the int-argb array
 
   (render-int-argb {:data (->> [[255 10 20 30] [255 40 50 60]]
                                (map util/bytes->int)
                                (int-array))
                     :size [2 1]})
   ;; \"Äp\"
   => string?"
  {:added "3.0"}
  ([image]
   (render-int-argb image {}))
  ([{:keys [data size] :as image} opts]
   (render-string (int-argb->rows data size))))

(defn render
  "renders an image for output"
  {:added "3.0"}
  ([image]
   (render image {}))
  ([image {:keys [channel] :as opts}]
   (let [image (if channel
                 (common/slice (common/base-map image) channel)
                 image)]
     (cond (-> image :model :label (= :byte-gray))
           (render-byte-gray image)

           (-> image :model :label (= :int-argb))
           (render-int-argb image)

           :else
           (-> (common/convert-base image (model/model :byte-gray))
               (render-byte-gray))))))

(def UP "\033[1A")
(def CLEARLINE "\033[2K")

(defn display
  "outputs an ascii string based on image input
 
   (with-out-str
     (display {:data (byte-array [10 20 30 40 50])
               :size [5 1]
               :model (model/model :byte-gray)}))
   ;;\"Ã#HXÕß¶$\\n\""
  {:added "3.0"}
  ([image]
   (display image {}))
  ([image {:keys [channel] :as opts}]
   (println (render image opts))))

(defn animate
  "allows for animation of images to occur"
  {:added "3.0"}
  ([images {:keys [channel pause loop]
            :or   {loop 1}
            :as   opts}]
   (let [frames (mapv (juxt (comp :height protocol.image/-size) #(render % opts)) images)]
     (dotimes [i loop]
       (doseq [[h lines] frames]
         (println lines)
         (Thread/sleep pause)
         (dotimes [i h]
           (print UP)
           (print CLEARLINE)))))))
