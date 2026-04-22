(ns std.fs.watch-test
  (:require [clojure.java.io :as io]
            [std.fs.watch :refer :all])
  (:use code.test)
  (:import (java.nio.file WatchService Paths FileSystems) (java.io File)))

^{:refer std.fs.watch/pattern :added "3.0"}
(fact "creates a regex pattern from the string representation"

  (pattern ".*") => #"\Q.\E.+"

  (pattern "*.jar") => #".+\Q.\Ejar")

^{:refer std.fs.watch/register-entry :added "3.0"}
(fact "adds a path to the watch service"
  (let [service (.newWatchService (FileSystems/getDefault))
        key     (register-entry service "src")]
    (try
      (instance? java.nio.file.WatchKey key)
      (finally
        (.close service))))
  => true)

^{:refer std.fs.watch/register-sub-directory :added "3.0"}
(fact "registers a directory to an existing watcher"
  (let [service (.newWatchService (FileSystems/getDefault))
        seen    (atom #{})
        root    (.getCanonicalPath (io/file "src"))
        subdir  (.getCanonicalPath (io/file "src/std"))
        watcher (assoc (watcher [root] (fn [_ _]) {:recursive false})
                       :root root
                       :service service
                       :seen seen
                       :excludes []
                       :includes [])]
    (try
      (register-sub-directory watcher subdir)
      @seen
      (finally
        (.close service))))
  => (contains #{(.getCanonicalPath (io/file "src/std"))}))

^{:refer std.fs.watch/register-path :added "3.0"}
(fact "registers either a file or a path to the watcher"
  (let [service (.newWatchService (FileSystems/getDefault))
        root    (.getCanonicalPath (io/file "src"))
        watcher (assoc (watcher [root] (fn [_ _]) {:recursive false})
                       :root root
                       :service service
                       :seen (atom #{})
                       :excludes []
                       :includes [])]
    (try
      (= watcher (register-path watcher root))
      (finally
        (.close service))))
  => true)

^{:refer std.fs.watch/process-event :added "3.0"}
(fact "helper function to process event"
  (let [out (atom nil)
        watcher {:options {:mode :sync}
                 :callback (fn [type file]
                             (reset! out [type (.getName ^File file)]))
                 :excludes []
                 :filters []
                 :kinds #{java.nio.file.StandardWatchEventKinds/ENTRY_CREATE}}]
    (process-event watcher java.nio.file.StandardWatchEventKinds/ENTRY_CREATE (io/file "project.clj"))
    @out)
  => [:create "project.clj"])

^{:refer std.fs.watch/run-watcher :added "3.0"}
(fact "initiates the watcher with the given callbacks"

  (let [dir     (str (io/file "test-scratch/watch-run"))
        out     (promise)
        event   (reify java.nio.file.WatchEvent
                  (kind [_] java.nio.file.StandardWatchEventKinds/ENTRY_CREATE)
                  (count [_] 1)
                  (context [_] (Paths/get "hello.watch" (make-array String 0))))
        key     (reify java.nio.file.WatchKey
                  (isValid [_] true)
                  (pollEvents [_] [event])
                  (reset [_] true)
                  (cancel [_] nil)
                  (watchable [_] (Paths/get dir (make-array String 0))))
        service (let [calls (atom 0)]
                  (reify java.nio.file.WatchService
                    (close [_] nil)
                    (poll [_] nil)
                    (poll [_ _ _] nil)
                    (take [_]
                      (if (= 1 (swap! calls inc))
                        key
                        (throw (java.nio.file.ClosedWatchServiceException.))))))]
    (try
      (run-watcher {:paths [dir]
                    :callback (fn [type file]
                                (deliver out [type (.getName ^File file)]))
                    :options {:recursive false :mode :sync}
                    :root dir
                    :service service
                    :seen (atom #{})
                    :filters []
                    :excludes []
                    :includes []
                    :kinds #{java.nio.file.StandardWatchEventKinds/ENTRY_CREATE}})
      (catch java.nio.file.ClosedWatchServiceException _))
    @out)
  => [:create "hello.watch"])

^{:refer std.fs.watch/start-watcher :added "3.0"}
(fact "starts the watcher"

  (let [dir (str (io/file "test-scratch/watch-start"))
        _   (.mkdirs (io/file dir))
        wt  (start-watcher (watcher [dir] (fn [_ _]) {:recursive false}))]
    (try
      (future? (:running wt))
      (finally
        (stop-watcher wt))))
  => true)

^{:refer std.fs.watch/stop-watcher :added "3.0"}
(fact "stops the watcher"

  (let [dir (str (io/file "test-scratch/watch-stop"))
        _   (.mkdirs (io/file dir))
        wt  (start-watcher (watcher [dir] (fn [_ _]) {:recursive false}))]
    (contains? (stop-watcher wt) :running))
  => false)

^{:refer std.fs.watch/watcher :added "3.0"}
(fact "the watch interface provided for java.io.File"
  (watcher ["src"] (fn [_ _]) {:recursive false})
  => (contains {:paths ["src"]
                :options (contains {:recursive false})}))

^{:refer std.fs.watch/watch-callback :added "3.0"}
(fact "helper function to create watch callback"
  (let [out (promise)]
    ((watch-callback (fn [root k _ [cmd file]]
                       (deliver out [(.getName ^File root)
                                     k
                                     cmd
                                     (.getName ^File file)]))
                     (io/file ".")
                     :save)
     :create
     (io/file "project.clj"))
    @out)
  => ["." :save :create "project.clj"])

^{:refer std.fs.watch/add-io-watch :added "3.0"}
(fact "registers the watch to a global list of *filewatchers*"

  (let [dir (io/file "test-scratch/io-watch")
        _   (.mkdirs dir)]
    (add-io-watch dir :save (fn [_ _ _ _]) {:recursive false}))
  => java.io.File)

^{:refer std.fs.watch/list-io-watch :added "3.0"}
(fact "list all *filewatchers"

  (let [dir (io/file "test-scratch/io-watch-list")
        _   (.mkdirs dir)
        _   (add-io-watch dir :save (fn [_ _ _ _]) {:recursive false})]
    (list-io-watch dir nil))
  => (contains {:save fn?}))

^{:refer std.fs.watch/remove-io-watch :added "3.0"}
(fact "removes the watcher with the given key"

  (let [dir (io/file "test-scratch/io-watch-remove")
        _   (.mkdirs dir)
        _   (add-io-watch dir :save (fn [_ _ _ _]) {:recursive false})]
    (remove-io-watch dir :save nil)
    (list-io-watch dir nil))
  => {})

(comment
  (code.manage/import))
