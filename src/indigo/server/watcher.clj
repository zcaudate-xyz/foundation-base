(ns indigo.server.watcher
  (:require [indigo.server.dispatch :as dispatch]
            [std.fs :as fs]
            [std.fs.watch :as watch]
            [std.json :as json]))

(defonce ^:dynamic *watcher* (atom nil))

(defn broadcast-change [kind file]
  (let [msg {:type "file-change"
             :path (str file)
             :kind (name kind)}]
    (dispatch/broadcast! msg)))

(defn start-watcher []
  (when-not @*watcher*
    (let [w (watch/watcher ["src"]
                           #'broadcast-change
                           {:recursive true
                            :types :all
                            :filter  ["*.clj$"]
                            :exclude [".#*.clj$"]})]
      (reset! *watcher* (watch/start-watcher w)))))

(defn stop-watcher []
  (when @*watcher*
    (watch/stop-watcher @*watcher*)
    (reset! *watcher* nil)))
