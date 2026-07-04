(ns python.gimp.tutorial.example-layers-test
  (:require [std.lib.env :as env]
            [python.gimp.tutorial.example-layers])
  (:import [std.lib.foundation Wrapped])
  (:use code.test))

(defn- ci?
  []
  (boolean (System/getenv "CI")))

(fact:global {:skip (or (ci?)
                         (not (or (env/program-exists? "gimp")
                                   (env/program-exists? "docker"))))})

(defn- unwrap [x]
  (if (instance? Wrapped x)
    (clojure.core/deref x)
    x))

(defn- in-layers
  "Evaluates form inside the example-layers namespace."
  [form]
  (binding [*ns* (find-ns 'python.gimp.tutorial.example-layers)]
    (unwrap (eval form))))

^{:refer python.gimp.tutorial.example-layers/create-striped-png :added "4.1"}
(fact "renders a multi-layer striped image to a png file"
  (in-layers
   '(!.py (python.gimp.tutorial.example-layers/create-striped-png
           "/tmp/tutorial-layers-striped.png" 120 60 ["red" "white" "blue"])))
  => "/tmp/tutorial-layers-striped.png")
