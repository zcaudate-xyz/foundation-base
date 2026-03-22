(ns std.fs.watch-test
  (:require [clojure.java.io :as io]
            [std.fs.watch :refer :all]
            [std.lib :as h])
  (:use code.test)
  (:import (java.nio.file WatchService Paths FileSystems) (java.io File)))

^{:refer std.fs.watch/pattern :added "3.0"}
(fact "creates a regex pattern from the string representation"

  (pattern ".*") => #"\Q.\E.+"

  (pattern "*.jar") => #".+\Q.\Ejar")

^{:refer std.fs.watch/register-entry :added "3.0"}
(comment "adds a path to the watch service"

  (-> (.newWatchService (FileSystems/getDefault))
      (register-entry "src")))

^{:refer std.fs.watch/register-sub-directory :added "3.0"}
(comment "registers a directory to an existing watcher"

  (-> (watcher ["src"] {} {})
      (assoc :service (.newWatchService (FileSystems/getDefault)))
      (register-sub-directory "test")))

^{:refer std.fs.watch/register-path :added "3.0"}
(comment "registers either a file or a path to the watcher"
  (-> (watcher [] {} {})
      (assoc :service (.newWatchService (FileSystems/getDefault)))
      (register-path "test")))

^{:refer std.fs.watch/process-event :added "3.0"}
(comment "helper function to process event")

^{:refer std.fs.watch/run-watcher :added "3.0"}
(comment "initiates the watcher with the given callbacks")

^{:refer std.fs.watch/start-watcher :added "3.0"}
(comment "starts the watcher")

^{:refer std.fs.watch/stop-watcher :added "3.0"}
(comment "stops the watcher")

^{:refer std.fs.watch/watcher :added "3.0"}
(comment "the watch interface provided for java.io.File"

  (def ^:dynamic *happy* (promise))

  (std.fs.watch/add-io-watch (io/file ".") :save
               (fn [f k _ [cmd file]]
                 (std.fs.watch/remove-io-watch f k nil)
                 (.delete ^File file)
                 (deliver *happy* [cmd (.getName ^File file)]))
               {:types #{:create :modify}
                :recursive false
                :filter  [".hara"]
                :exclude [".git" "target"]})

  (std.fs.watch/list-io-watch (io/file ".") nil)
  => (contains {:save fn?})

  (spit "happy.hara" "hello")

  (deref *happy*)
  => [:create "happy.hara"]

  (std.fs.watch/list-io-watch (io/file ".") nil)
  => {})

^{:refer std.fs.watch/watch-callback :added "3.0"}
(comment "helper function to create watch callback")

^{:refer std.fs.watch/add-io-watch :added "3.0"}
(comment "registers the watch to a global list of *filewatchers*")

^{:refer std.fs.watch/list-io-watch :added "3.0"}
(comment "list all *filewatchers")

^{:refer std.fs.watch/remove-io-watch :added "3.0"}
(comment "removes the watcher with the given key")

(comment
  (code.manage/import))
