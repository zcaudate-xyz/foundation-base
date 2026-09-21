^{:no-test true}
(ns component.task-web-index
  (:require [std.make :as make]
            [component.build-web-index :as build-web-index]))

(defn task-build
  []
  (make/build-all build-web-index/WEB-INDEX))

(defn task-build-web
  []
  (make/run build-web-index/WEB-INDEX :build-web))

(defn -main
  [& [task]]
  (case task
    "build-web" (task-build-web)
    (task-build)))
