(ns python.gimp.tutorial.example-core
  (:require [hara.lang :as l]))

(l/script :python
  {:import [["gi" :as gi]]})

(defn.py ensure-gimp
  "Makes sure the gi Gimp version is required and returns the Gimp module."
  {:added "4.1"}
  []
  (. gi (require_version "Gimp" "3.0"))
  (return (__import__ "gi.repository.Gimp" :fromlist ["Gimp"])))

(defn.py ensure-gegl
  "Makes sure the gi Gegl version is required and returns the Gegl module."
  {:added "4.1"}
  []
  (. gi (require_version "Gegl" "0.4"))
  (return (__import__ "gi.repository.Gegl" :fromlist ["Gegl"])))

(defn.py ensure-gio
  "Returns the Gio module."
  {:added "4.1"}
  []
  (return (__import__ "gi.repository.Gio" :fromlist ["Gio"])))

(defn.py create-image
  "Creates a new RGB image of the given size and returns its ID."
  {:added "4.1"}
  [width height]
  (:= Gimp (-/ensure-gimp))
  (:= img (. Gimp Image (new width height (. Gimp ImageBaseType RGB))))
  (return (. img (get_id))))

(defn.py add-layer
  "Adds an RGBA layer named name to image-id and returns the layer ID."
  {:added "4.1"}
  [image-id name width height]
  (:= Gimp (-/ensure-gimp))
  (:= gimage (. Gimp Image (get_by_id image-id)))
  (:= layer (. Gimp Layer (new gimage name width height
                               (. Gimp ImageType RGBA_IMAGE)
                               100.0
                               (. Gimp LayerMode NORMAL))))
  (. gimage (insert_layer layer None 0))
  (return (. layer (get_id))))

(defn.py fill-layer
  "Fills layer-id with a Gimp.FillType constant and returns the layer ID."
  {:added "4.1"}
  [layer-id fill-type]
  (:= Gimp (-/ensure-gimp))
  (:= drawable (. Gimp Drawable (get_by_id layer-id)))
  (. Gimp Drawable (fill drawable fill-type))
  (return layer-id))

(defn.py fill-layer-color
  "Fills layer-id with a named Gegl color and returns the layer ID."
  {:added "4.1"}
  [layer-id color-name]
  (:= Gimp (-/ensure-gimp))
  (:= Gegl (-/ensure-gegl))
  (:= color (. Gegl Color (new color-name)))
  (. Gimp (context_set_foreground color))
  (:= drawable (. Gimp Drawable (get_by_id layer-id)))
  (. Gimp Drawable (edit_fill drawable (. Gimp FillType FOREGROUND)))
  (return layer-id))

(defn.py flatten-image
  "Flattens image-id and returns the resulting layer ID."
  {:added "4.1"}
  [image-id]
  (:= Gimp (-/ensure-gimp))
  (:= img (. Gimp Image (get_by_id image-id)))
  (:= layer (. img (flatten)))
  (return (. layer (get_id))))

(defn.py image-size
  "Returns [width height] for image-id."
  {:added "4.1"}
  [image-id]
  (:= Gimp (-/ensure-gimp))
  (:= img (. Gimp Image (get_by_id image-id)))
  (return [(. img (get_width)) (. img (get_height))]))

(defn.py save-to
  "Saves image-id to filepath, deriving the format from the extension.
   Returns filepath."
  {:added "4.1"}
  [image-id filepath]
  (:= Gimp (-/ensure-gimp))
  (:= Gio (-/ensure-gio))
  (:= img (. Gimp Image (get_by_id image-id)))
  (:= file (. Gio File (new_for_path filepath)))
  (. Gimp (file_save (. Gimp RunMode NONINTERACTIVE) img file None))
  (return filepath))

(defn.py load-image
  "Loads an image from filepath and returns its ID."
  {:added "4.1"}
  [filepath]
  (:= Gimp (-/ensure-gimp))
  (:= Gio (-/ensure-gio))
  (:= file (. Gio File (new_for_path filepath)))
  (:= img (. Gimp (file_load (. Gimp RunMode NONINTERACTIVE) file)))
  (return (. img (get_id))))

(defn.py delete-image
  "Deletes the image with id image-id and returns the id."
  {:added "4.1"}
  [image-id]
  (:= Gimp (-/ensure-gimp))
  (:= img (. Gimp Image (get_by_id image-id)))
  (. img (delete))
  (return image-id))

(comment
  ;; Run from Clojure with !.py. Results are wrapped; deref to get values.
  (clojure.core/deref
   (!.py (-/create-image 64 64)))
  (clojure.core/deref
   (!.py (-/image-size 1))))
