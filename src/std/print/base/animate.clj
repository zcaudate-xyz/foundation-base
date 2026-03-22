(ns std.print.base.animate
  (:require [clojure.string]
            [std.concurrent.print :as print]
            [std.print.format.common :as common])
  (:refer-clojure :exclude [format]))

(defn print-animation
  "outputs an animated ascii file
 
   (print-animation \"test-data/std.print/plane.ascii\")"
  {:added "3.0"}
  ([file]
   (let [lines (clojure.string/split-lines (slurp file))
         x (Integer/parseInt (subs (first lines) 2))
         y (Integer/parseInt (subs (second lines) 2))
         frames (->> (drop 2 lines)
                     (partition (inc y))
                     (map rest))]
     (print-animation x y frames)))
  ([x y frames]
   (print-animation x y frames {:pause 20}))
  ([x y frames {:keys [pause]}]
   (doseq [frame frames]
     (doseq [line frame]
       (print/println line))
     (Thread/sleep pause)
     (dotimes [i y]
       (print/print common/+up+)
       (print/print common/+clearline+)))))
