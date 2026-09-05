(ns lang.core.type-notify-test
  (:require [clojure.string]
            [net.http :as http]
            [lang.runtime.basic.impl.process-lua :as lua]
            [std.concurrent :as cc]
            [std.json :as json]
            [lang.core :as l]
            [lang.core.type-notify :as notify]
            [std.lib.component :as component]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.lib.future :as future])
  (:use code.test))

(defonce +server+ (notify/notify-server {}))

(fact:global
 {:setup [(component/start +server+)]
  :teardown [(component/stop +server+)]})

^{:refer lang.core.type-notify/has-sink? :added "4.0"
  :setup [(notify/get-sink +server+ "abc")]}
(fact "checks that sink exists"
  (notify/has-sink? +server+ "abc")
  => true

  (notify/has-sink? +server+ "def")
  => false)

^{:refer lang.core.type-notify/get-sink :added "4.0"
  :setup [(notify/clear-sink +server+ "abc")]}
(fact "gets a sink from the notification app server"

  (notify/get-sink +server+ "abc")
  => f/atom?)

^{:refer lang.core.type-notify/clear-sink :added "4.0"
  :setup [(notify/get-sink +server+ "abc")]}
(fact "clears a sink"

  (notify/clear-sink +server+ "abc")
  => f/atom?)

^{:refer lang.core.type-notify/add-listener :added "4.0"}
(fact "adds a listener to the sink"
  (let [out (atom nil)]
    (notify/add-listener +server+ "abc" :key (fn [v] (reset! out v)))
    (reset! (notify/get-sink +server+ "abc") {:a 1})
    @out)
  => {:a 1})

^{:refer lang.core.type-notify/remove-listener :added "4.0"}
(fact "removes a listener from the sink"
  (let [out (atom nil)]
    (notify/add-listener +server+ "abc" :key (fn [v] (reset! out v)))
    (notify/remove-listener +server+ "abc" :key)
    (reset! (notify/get-sink +server+ "abc") {:a 2})
    @out)
  => nil)

^{:refer lang.core.type-notify/get-oneshot-id :added "4.0"}
(fact "registers a oneshot id for the app server"

  (notify/get-oneshot-id +server+)
  => #"oneshot")

^{:refer lang.core.type-notify/remove-oneshot-id :added "4.0"}
(fact "removes a oneshot id"

  (->> (notify/get-oneshot-id +server+)
       (notify/remove-oneshot-id +server+))
  => string?)

^{:refer lang.core.type-notify/clear-oneshot-sinks :added "4.0"
  :setup [(notify/clear-oneshot-sinks +server+)]}
(fact "clear all registered oneshot sinks"

  (do (dotimes [i 10]
        (notify/get-oneshot-id +server+))
      (count (notify/clear-oneshot-sinks +server+)))
  => 10)


^{:refer lang.core.type-notify/process-print :added "4.0"}
(fact "processes `print` id option"
  (env/with-out-str
    (notify/process-print {"value" "hello" "key" ["type" {"data" "world"}]}))
  => (fn [s] (clojure.string/includes? s "world")))


^{:refer lang.core.type-notify/process-capture :added "4.0"}
(fact "processes `capture` id option"
  (notify/process-capture {:a 1})
  (last @notify/*notify-capture*)
  => {:a 1})

^{:refer lang.core.type-notify/process-message :added "4.0"}
(fact "processes a message recieved by the notification server"
  (let [out (atom nil)]
    (notify/add-listener +server+ "msg" :key (fn [v] (reset! out v)))
    (notify/process-message +server+ (json/write {:id "msg" :data "hello"}))
    (notify/process-message +server+ (json/write {:id "msg" :data "hello"}))
    @out)
  => (contains {:data "hello"}))

^{:refer lang.core.type-notify/handle-notify-http :added "4.0"}
(fact "handler for http request")

^{:refer lang.core.type-notify/start-notify-http :added "4.0"}
(fact "starts http server"

  (do (def +value+ (f/sid))
      (http/post (str "http://127.0.0.1:" (:http-port +server+) "/")
                 {:body (json/write {:id "hello"
                                     :data +value+})})

      (get @(notify/get-sink +server+ "hello")
           "data"))
  => +value+)

^{:refer lang.core.type-notify/stop-notify-http :added "4.0"}
(fact "stops http server"
  (notify/stop-notify-http +server+)
  @(:http-instance +server+) => nil)

^{:refer lang.core.type-notify/handle-notify-socket :added "4.0"}
(fact "handler for socket request")

^{:refer lang.core.type-notify/start-notify-socket :added "4.0"}
(fact "starts socket server"
  (notify/start-notify-socket +server+)
  @(:socket-instance +server+) => map?)

^{:refer lang.core.type-notify/stop-notify-socket :added "4.0"}
(fact "stops socket server"
  (notify/stop-notify-socket +server+)
  @(:socket-instance +server+) => nil)

^{:refer lang.core.type-notify/start-notify :added "4.0"
  :setup [(notify/stop-notify +server+)]}
(fact "starts both servers"

  (notify/start-notify +server+)
  => map?)

^{:refer lang.core.type-notify/stop-notify :added "4.0"}
(fact "stops both servers"
  (notify/stop-notify +server+)
  @(:http-instance +server+) => nil
  @(:socket-instance +server+) => nil)

^{:refer lang.core.type-notify/notify-server:create :added "4.0"}
(fact "creates notify serve "
  (notify/notify-server:create {})
  => map?)

^{:refer lang.core.type-notify/notify-server :added "4.0"}
(fact "create and start notify server"
  (component/stop (notify/notify-server {}))
  => map?)

^{:refer lang.core.type-notify/default-notify :added "4.0"}
(fact "gets the default notify server"

  (notify/default-notify)
  => map?)

^{:refer lang.core.type-notify/default-notify:reset :added "4.0"}
(fact "resets the default notify server"
  (notify/default-notify:reset)
  => map?)

^{:refer lang.core.type-notify/watch-oneshot :added "4.0"}
(fact "returns a completable future"

  (notify/watch-oneshot +server+
                        10)
  => (contains [string? future/future?]))

(comment
  (./import))
