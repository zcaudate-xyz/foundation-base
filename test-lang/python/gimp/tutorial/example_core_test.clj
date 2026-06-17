(ns python.gimp.tutorial.example-core-test
  (:require [std.lib.env :as env]
            [python.gimp.tutorial.example-core])
  (:import [std.lib.foundation Wrapped])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "gimp"))})

(defn- unwrap [x]
  (if (instance? Wrapped x)
    (clojure.core/deref x)
    x))

(defn- in-core
  "Evaluates form inside the example-core namespace so that the !.py macro
   and the GIMP runtime set up by that namespace are used."
  [form]
  (binding [*ns* (find-ns 'python.gimp.tutorial.example-core)]
    (unwrap (eval form))))

^{:refer python.gimp.tutorial.example-core/create-image :added "4.1"}
(fact "creates a new image and returns an id"
  (in-core '(!.py (python.gimp.tutorial.example-core/create-image 32 32)))
  => integer?)

^{:refer python.gimp.tutorial.example-core/add-layer :added "4.1"}
(fact "adds a layer to an image"
  (in-core
   '(!.py
     (do (var id (python.gimp.tutorial.example-core/create-image 32 32))
         (python.gimp.tutorial.example-core/add-layer id "Layer" 32 32))))
  => integer?)

^{:refer python.gimp.tutorial.example-core/fill-layer :added "4.1"}
(fact "fills a layer with white"
  (in-core
   '(!.py
     (do (var id (python.gimp.tutorial.example-core/create-image 16 16))
         (var layer (python.gimp.tutorial.example-core/add-layer id "Bg" 16 16))
         (var Gimp (python.gimp.tutorial.example-core/ensure-gimp))
         (python.gimp.tutorial.example-core/fill-layer layer (. Gimp FillType WHITE))
         layer)))
  => integer?)

^{:refer python.gimp.tutorial.example-core/image-size :added "4.1"}
(fact "reports the image size"
  (in-core
   '(!.py
     (do (var id (python.gimp.tutorial.example-core/create-image 64 32))
         (python.gimp.tutorial.example-core/image-size id))))
  => [64 32])

^{:refer python.gimp.tutorial.example-core/save-to :added "4.1"}
(fact "saves an image to disk"
  (in-core
   '(!.py
     (do (var id (python.gimp.tutorial.example-core/create-image 32 32))
         (var layer (python.gimp.tutorial.example-core/add-layer id "Bg" 32 32))
         (var Gimp (python.gimp.tutorial.example-core/ensure-gimp))
         (python.gimp.tutorial.example-core/fill-layer layer (. Gimp FillType WHITE))
         (python.gimp.tutorial.example-core/save-to id "/tmp/tutorial-core.png"))))
  => "/tmp/tutorial-core.png")

^{:refer python.gimp.tutorial.example-core/load-image :added "4.1"}
(fact "loads a previously saved image"
  (in-core
   '(!.py
     (do (var id (python.gimp.tutorial.example-core/create-image 32 32))
         (var layer (python.gimp.tutorial.example-core/add-layer id "Bg" 32 32))
         (var Gimp (python.gimp.tutorial.example-core/ensure-gimp))
         (python.gimp.tutorial.example-core/fill-layer layer (. Gimp FillType WHITE))
         (python.gimp.tutorial.example-core/save-to id "/tmp/tutorial-core-load.png")
         (python.gimp.tutorial.example-core/image-size
          (python.gimp.tutorial.example-core/load-image "/tmp/tutorial-core-load.png")))))
  => [32 32])

^{:refer python.gimp.tutorial.example-core/delete-image :added "4.1"}
(fact "deletes an image"
  (in-core
   '(!.py
     (do (var id (python.gimp.tutorial.example-core/create-image 8 8))
         (python.gimp.tutorial.example-core/delete-image id))))
  => integer?)
