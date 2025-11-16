(ns js.lib.three-extra
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:bundle  {:gltf  [["three/examples/jsm/loaders/GLTFLoader.js" :as [* ThreeGLTF]]]
             :fbx   [["three/examples/jsm/loaders/FBXLoader.js"  :as [* ThreeFBX]]]
             :orbit [["three/addons/controls/OrbitControls.js"   :as [* ThreeOrbitControl]]]}})

(def$.js GLTFLoader
  (. ThreeGLTF GLTFLoader))

(def$.js FBXLoader
  (. ThreeFBX FBXLoader))

(def$.js OrbitControl
  (. ThreeOrbitControl OrbitControl))
