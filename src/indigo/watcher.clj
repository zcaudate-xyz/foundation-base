(ns indigo.watcher
  (:require [indigo.event :as event]
            [clojure.java.io :as io])
  (:import [java.nio.file StandardWatchEventKinds Path FileSystems WatchService WatchKey WatchEvent]))

(defn- to-path ^Path [s]
  (.toPath (io/file s)))

(defn- watch-service-loop [^WatchService watcher event-bus]
  (loop []
    (when-let [^WatchKey key (.take watcher)]
      (let [^Path dir (.watchable key)]
        (doseq [^WatchEvent event (.pollEvents key)]
          (let [kind (.kind event)
                ^Path path (.resolve dir ^Path (.context event))]
            (when (not= kind StandardWatchEventKinds/OVERFLOW)
              (event/publish event-bus :watcher {:type "file-change"
                                                 :path (str path)
                                                 :event-kind (str kind)})))))
      (.reset key)
      (recur))))

(defn start-watcher! [paths event-bus]
  (let [^WatchService watcher (.newWatchService (FileSystems/getDefault))
        thread (Thread. #(watch-service-loop watcher event-bus))]
    (doseq [path paths]
      (.register (to-path path) watcher (into-array [StandardWatchEventKinds/ENTRY_CREATE
                                                     StandardWatchEventKinds/ENTRY_DELETE
                                                     StandardWatchEventKinds/ENTRY_MODIFY])))
    (.start thread)
    {:watcher watcher :thread thread}))

(defn stop-watcher! [{:keys [^WatchService watcher ^Thread thread]}]
  (.close watcher)
  (.interrupt thread))
