(ns python.gimp.tutorial.example-core-test
  (:require [std.lib.env :as env]
            [hara.lang :as l]
            [python.gimp.tutorial.example-core])
  (:import [std.lib.foundation Wrapped])
  (:use code.test))

(l/script :python
  {:runtime :gimp
   :require [[python.gimp.tutorial.example-core :as core]]
   :import [["gi" :as gi]]})

(fact:global
 {:skip (not (or (env/program-exists? "gimp")
                 (env/program-exists? "docker")))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer python.gimp.tutorial.example-core/create-image :added "4.1"}
(fact "creates a new image and returns an id"

  (!.py
    (core/create-image 32 32))
  => integer?)

^{:refer python.gimp.tutorial.example-core/add-layer :added "4.1"}
(fact "adds a layer to an image"
  
  (!.py
    (var id (core/create-image 32 32))
    (core/add-layer id "Layer" 32 32))
  => integer?)

^{:refer python.gimp.tutorial.example-core/fill-layer :added "4.1"}
(fact "fills a layer with white"
  
  (!.py
    (var id    (core/create-image 16 16))
    (var layer (core/add-layer id "Bg" 16 16))
    (var Gimp   (core/ensure-gimp))
    (core/fill-layer layer (. Gimp FillType WHITE))
    layer)
  => integer?)

^{:refer python.gimp.tutorial.example-core/image-size :added "4.1"}
(fact "reports the image size"

  (!.py
    (core/image-size
     (core/create-image 64 32)))
  => [64 32])

^{:refer python.gimp.tutorial.example-core/save-to :added "4.1"}
(fact "saves an image to disk"
  
  @(!.py
     (var id (core/create-image 32 32))
     (var layer (core/add-layer id "Bg" 32 32))
     (var Gimp (core/ensure-gimp))
     (core/fill-layer layer (. Gimp FillType WHITE))
     (core/save-to id "/tmp/tutorial-core.png"))
  => "/tmp/tutorial-core.png")

^{:refer python.gimp.tutorial.example-core/load-image :added "4.1"}
(fact "loads a previously saved image"
  
  (!.py
    (var id (core/create-image 32 32))
    (var layer (core/add-layer id "Bg" 32 32))
     (var Gimp (core/ensure-gimp))
     (core/fill-layer layer (. Gimp FillType WHITE))
     (core/save-to id "/tmp/tutorial-core-load.png")
     (core/image-size
      (core/load-image "/tmp/tutorial-core-load.png")))
  => [32 32])

^{:refer python.gimp.tutorial.example-core/delete-image :added "4.1"}
(fact "deletes an image"
  
  (!.py
    (var id (core/create-image 8 8))
    (core/delete-image id))
  => integer?)
