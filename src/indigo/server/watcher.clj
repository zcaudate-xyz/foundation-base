(ns indigo.server.watcher
  (:require [std.fs.watch :as watch]
            [std.lib :as h]
            [std.string :as str]
            [std.fs :as fs]
            [indigo.server.dispatch :as dispatch]
            [org.httpkit.server :as http]
            [code.project :as project]
            [cheshire.core :as cheshire]))

(defonce ^:dynamic *watcher* (atom nil))

(defn broadcast-change [kind file]
  (let [msg {:type "file-change"
             :path (str file)
             :kind (name kind)}]
    (h/prn "File Change:" msg)
    (dispatch/broadcast! msg)))

(defn start-watcher []
  (when-not @*watcher*
    (h/prn "Starting File Watcher on .")
    (let [w (watch/watcher ["src"]
                           #'broadcast-change
                           {:recursive true
                            :types :all
                            :filter  ["*.clj$"]
                            :exclude [".#*.clj$"]})]
      (reset! *watcher* (watch/start-watcher w)))))

(defn stop-watcher []
  (when @*watcher*
    (h/prn "Stopping File Watcher")
    (watch/stop-watcher @*watcher*)
    (reset! *watcher* nil)))
