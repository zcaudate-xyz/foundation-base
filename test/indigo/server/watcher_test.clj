(ns indigo.server.watcher-test
  (:require [indigo.server.dispatch :as dispatch]
            [indigo.server.watcher :refer :all])
  (:use code.test))

^{:refer indigo.server.watcher/broadcast-change :added "4.1"}
(fact "broadcasts a file-change message"
  (let [captured (atom nil)]
    (with-redefs [dispatch/broadcast! (fn [msg] (reset! captured msg))]
      (broadcast-change :create "/tmp/foo.clj"))
    [(:type @captured) (:kind @captured) (:path @captured)])
  => ["file-change" "create" "/tmp/foo.clj"])

^{:refer indigo.server.watcher/start-watcher :added "4.1"}
(fact "starts a watcher and sets the atom"
  (with-redefs [*watcher* (atom nil)]
    (start-watcher)
    (let [started (some? @*watcher*)]
      (stop-watcher)
      started))
  => true)

^{:refer indigo.server.watcher/stop-watcher :added "4.1"}
(fact "stops the watcher and clears the atom"
  (with-redefs [*watcher* (atom nil)]
    (start-watcher)
    (stop-watcher)
    @*watcher*)
  => nil)