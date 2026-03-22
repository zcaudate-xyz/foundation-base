(ns std.image.base
  (:require [std.image.awt.common :as awt]
            [std.image.awt.io :as io]
            [std.image.base.common :as common]
            [std.image.base.display :as display]
            [std.image.base.model :as model]
            [std.image.base.size :as size]
            [std.image.protocol :as protocol.image])
  (:import (clojure.lang IPersistentMap PersistentArrayMap)))

(defrecord Image [model size data]
  Object
  (toString [_]
    (str "#image.base" [(protocol.image/-width size)
                        (protocol.image/-height size)]
         {:model (:label model)}))

  protocol.image/IRepresentation
  (-channels [_] ((-> model :channel :fn) data))
  (-size     [_] size)
  (-model    [_] model)
  (-data     [_] data)
  (-subimage [img x y w h]
    (common/subimage img x y w h))

  protocol.image/ITransfer
  (-to-byte-gray [image]
    (-> (common/convert-base image (model/model :byte-gray))
        :data))
  (-to-int-argb  [image]
    (-> (common/convert-base image (model/model :int-argb))
        :data))
  (-write [image sink opts]
    (io/write (awt/image image) sink opts)))

(defmethod print-method Image
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defn image
  "creates a new image record for holding data
 
   (image {:size [100 100]
           :model :ushort-555-rgb
           :data nil})
   ;; #img[100 100]{:format :ushort-555-rgb}
   => std.image.base.Image"
  {:added "3.0"}
  ([{:keys [model size data] :as m}]
   (-> (map->Image m)
       (update-in [:size] size/size->map)
       (update-in [:model] model/model)
       (assoc :type IPersistentMap))))

(defmethod protocol.image/-read Image
  ([source model _]
   (-> (io/read source model)
       (common/convert model)
       image)))

(defmethod protocol.image/-read :base
  ([source model _]
   (protocol.image/-read source model Image)))

(defmethod protocol.image/-image Image
  ([size model data _]
   (image {:size size
           :model model
           :data data})))

(defmethod protocol.image/-image :base
  ([size model data _]
   (protocol.image/-image size model data Image)))

(defmethod protocol.image/-image IPersistentMap
  ([size model data _]
   (protocol.image/-image size model data Image)))

(defmethod protocol.image/-blank Image
  ([size model _]
   (let [model (model/model model)
         channels (common/create-channels model (size/length size))
         data ((-> model :channel :inv) channels)]
     (image {:size size
             :model model
             :data data}))))

(defmethod protocol.image/-blank :base
  ([size model _]
   (protocol.image/-blank size model Image)))

(defmethod protocol.image/-blank IPersistentMap
  ([size model _]
   (protocol.image/-blank size model Image)))

(defmethod protocol.image/-display :base
  ([img opts _]
   (display/display img opts)))

(defmethod protocol.image/-display Image
  ([img opts _]
   (display/display img opts)))

(defmethod protocol.image/-display-class :base
  ([_]
   #{Image}))

(defmethod protocol.image/-display-class Image
  ([_]
   #{Image}))

(defmethod protocol.image/-display-class :base
  ([_]
   #{Image}))
