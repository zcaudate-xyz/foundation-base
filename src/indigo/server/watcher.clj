(ns indigo.server.watcher
  (:require [std.fs.watch :as watch]
            [std.string :as str]
            [std.fs :as fs]
            [std.json :as json]
            [std.lib :as h]
            [indigo.server.dispatch :as dispatch]))

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
