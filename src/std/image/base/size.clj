(ns std.image.base.size
  (:require [std.image.protocol :as protocol.image])
  (:import (clojure.lang IPersistentMap IPersistentVector)))

(extend-protocol protocol.image/ISize

  IPersistentVector
  (-width  [v] (first v))
  (-height [v] (second v))

  IPersistentMap
  (-width  [m] (:width m))
  (-height [m] (:height m)))

(defn size->map
  "converts a size to a map
 
   (size->map [100 200])
   => {:width 100, :height 200}
 
   (size->map {:width 2 :height 3})
   => {:width 2, :height 3}"
  {:added "3.0"}
  ([size]
   {:width (protocol.image/-width size)
    :height (protocol.image/-height size)}))

(defn length
  "calculates the length of the array
 
   (length {:width 2 :height 3})
   => 6
 
   (length [100 200])
   => 20000"
  {:added "3.0"}
  ([size]
   (* (protocol.image/-width size) (protocol.image/-height size))))
