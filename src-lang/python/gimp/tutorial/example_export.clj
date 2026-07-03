^{:no-test true}
(ns python.gimp.tutorial.example-export
  (:require [hara.lang :as l]))

(l/script :python
  {:require [[python.gimp.tutorial.example-core :as core]
             [python.gimp.tutorial.example-image]]})

(defn.py save-both-formats
  "Saves image-id as both an XCF and a PNG file. Returns [xcf-path png-path]."
  {:added "4.1"}
  [image-id xcf-path png-path]
  (core/save-to image-id xcf-path)
  (core/save-to image-id png-path)
  (return [xcf-path png-path]))

(defn.py convert-png-to-xcf
  "Loads png-path and saves it as an XCF file at xcf-path. Returns xcf-path."
  {:added "4.1"}
  [png-path xcf-path]
  (:= id (core/load-image png-path))
  (core/save-to id xcf-path)
  (core/delete-image id)
  (return xcf-path))

(comment
  (clojure.core/deref
   (!.py (python.gimp.tutorial.example-export/save-both-formats
          1 "/tmp/tutorial-both.xcf" "/tmp/tutorial-both.png")))

  (clojure.core/deref
   (!.py (python.gimp.tutorial.example-export/convert-png-to-xcf
          "/tmp/tutorial-both.png" "/tmp/tutorial-converted.xcf"))))
