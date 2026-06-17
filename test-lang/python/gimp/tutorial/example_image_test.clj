(ns python.gimp.tutorial.example-image-test
  (:require [std.lib.env :as env]
            [python.gimp.tutorial.example-image])
  (:import [std.lib.foundation Wrapped])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "gimp"))})

(defn- unwrap [x]
  (if (instance? Wrapped x)
    (clojure.core/deref x)
    x))

(defn- in-image
  "Evaluates form inside the example-image namespace."
  [form]
  (binding [*ns* (find-ns 'python.gimp.tutorial.example-image)]
    (unwrap (eval form))))

^{:refer python.gimp.tutorial.example-image/create-white-png :added "4.1"}
(fact "renders a white image to a png file"
  (in-image
   '(!.py (python.gimp.tutorial.example-image/create-white-png
           "/tmp/tutorial-image-white.png" 64 64)))
  => "/tmp/tutorial-image-white.png")
