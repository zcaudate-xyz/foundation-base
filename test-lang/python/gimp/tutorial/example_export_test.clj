(ns python.gimp.tutorial.example-export-test
  (:require [std.lib.env :as env]
            [python.gimp.tutorial.example-export])
  (:import [std.lib.foundation Wrapped])
  (:use code.test))

(fact:global {:skip (not (or (env/program-exists? "gimp")
                              (env/program-exists? "docker")))})

(defn- unwrap [x]
  (if (instance? Wrapped x)
    (clojure.core/deref x)
    x))

(defn- in-export
  "Evaluates form inside the example-export namespace."
  [form]
  (binding [*ns* (find-ns 'python.gimp.tutorial.example-export)]
    (unwrap (eval form))))

(fact "saves an image as both xcf and png"
  (in-export
   '(!.py
     (do (var id (python.gimp.tutorial.example-core/create-image 32 32))
         (var layer (python.gimp.tutorial.example-core/add-layer id "Bg" 32 32))
         (var Gimp (python.gimp.tutorial.example-core/ensure-gimp))
         (python.gimp.tutorial.example-core/fill-layer layer (. Gimp FillType WHITE))
         (python.gimp.tutorial.example-export/save-both-formats
          id "/tmp/tutorial-export.xcf" "/tmp/tutorial-export.png"))))
  => ["/tmp/tutorial-export.xcf"
      "/tmp/tutorial-export.png"])

^{:refer python.gimp.tutorial.example-export/convert-png-to-xcf :added "4.1"}
(fact "converts a png to xcf"
  (in-export
   '(!.py
     (do
       (python.gimp.tutorial.example-image/create-white-png
        "/tmp/tutorial-export-source.png" 32 32)
       (python.gimp.tutorial.example-export/convert-png-to-xcf
        "/tmp/tutorial-export-source.png"
        "/tmp/tutorial-export-converted.xcf"))))
  => "/tmp/tutorial-export-converted.xcf")
