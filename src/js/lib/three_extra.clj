(ns js.lib.three-extra
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:import [["three/addons/controls/OrbitControls.js" :as [* ThreeOrbitControl]]
            ["three/examples/jsm/loaders/GLTFLoader.js" :as [* ThreeGLTF]]
            ["three/examples/jsm/loaders/FBXLoader.js" :as [* ThreeFBX]]]})

(def$.js GLTFLoader
  (. ThreeGLTF GLTFLoader))

(def$.js FBXLoader
  (. ThreeFBX FBXLoader))

(def$.js OrbitControl
  (. ThreeOrbitControl OrbitControl))
