^{:no-test true}
(ns component.task-native-index
  (:require
   [hara.lang :as l]
   [std.lib :as h]
   [std.string :as str]
   [std.make :as make]
   [std.lib.link :as link]
   [component.build-native-index :as build-native-index]))

(defn task-gh-push
  [& [message]]
  (make/build-all build-native-index/COMPONENT-NATIVE)
  (make/gh:dwim-push build-native-index/COMPONENT-NATIVE message))

(defn task-gh-init
  [& [message]]
  (make/build-all build-native-index/COMPONENT-NATIVE)
  (make/gh:dwim-init build-native-index/COMPONENT-NATIVE message))

(defn -main
  []
  (try
    (h/p "")
    (h/p "********************************************************************")
    (h/p "********************************************************************")
    (h/p "")
    (h/p "GITHUB PUSH")
    (h/p "")
    (h/p "********************************************************************")
    (h/p "********************************************************************")
    (task-gh-push)
    (catch Throwable t
      (prn t)
      (System/exit 1)))
  
  (System/exit 0))

(comment)
