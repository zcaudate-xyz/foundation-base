(ns std.lang.interface.type-notify-test
  (:use code.test)
  (:require [std.lang.interface.type-notify :as notify]
            [rt.basic.impl.process-lua :as lua]
            [net.http :as http]
            [std.lib :as h]
            [std.concurrent :as cc]
            [std.json :as json]
            [std.json :as json]
            [std.lang :as l]
            [std.string :as str]))

(defonce +server+ (notify/notify-server {}))

(fact:global
 {:setup [(h/start +server+)]
  :teardown [(h/stop +server+)]})

^{:refer std.lang.interface.type-notify/has-sink? :added "4.0"
  :setup [(notify/get-sink +server+ "abc")]}
(fact "checks that sink exists"
  (notify/has-sink? +server+ "abc")
  => true

  (notify/has-sink? +server+ "def")
  => false)

^{:refer std.lang.interface.type-notify/get-sink :added "4.0"
  :setup [(notify/clear-sink +server+ "abc")]}
(fact "gets a sink from the notification app server"
  ^:hidden
  
  (notify/get-sink +server+ "abc")
  => h/atom?)

^{:refer std.lang.interface.type-notify/clear-sink :added "4.0"
  :setup [(notify/get-sink +server+ "abc")]}
(fact "clears a sink"
  ^:hidden
  
  (notify/clear-sink +server+ "abc")
  => h/atom?)

^{:refer std.lang.interface.type-notify/add-listener :added "4.0"}
(fact "adds a listener to the sink"
  (let [out (atom nil)]
    (notify/add-listener +server+ "abc" :key (fn [v] (reset! out v)))
    (reset! (notify/get-sink +server+ "abc") {:a 1})
    @out)
  => {:a 1})

^{:refer std.lang.interface.type-notify/remove-listener :added "4.0"}
(fact "removes a listener from the sink"
  (let [out (atom nil)]
    (notify/add-listener +server+ "abc" :key (fn [v] (reset! out v)))
    (notify/remove-listener +server+ "abc" :key)
    (reset! (notify/get-sink +server+ "abc") {:a 2})
    @out)
  => nil)

^{:refer std.lang.interface.type-notify/get-oneshot-id :added "4.0"}
(fact "registers a oneshot id for the app server"
  ^:hidden
  
  (notify/get-oneshot-id +server+)
  => #"oneshot")

^{:refer std.lang.interface.type-notify/remove-oneshot-id :added "4.0"}
(fact "removes a oneshot id"
  ^:hidden
  
  (->> (notify/get-oneshot-id +server+)
       (notify/remove-oneshot-id +server+))
  => string?)

^{:refer std.lang.interface.type-notify/clear-oneshot-sinks :added "4.0"
  :setup [(notify/clear-oneshot-sinks +server+)]}
(fact "clear all registered oneshot sinks"
  ^:hidden
  
  (do (dotimes [i 10]
        (notify/get-oneshot-id +server+))
      (count (notify/clear-oneshot-sinks +server+)))
  => 10)


^{:refer std.lang.interface.type-notify/process-print :added "4.0"}
(fact "processes `print` id option"
  (h/with-out-str
    (notify/process-print {"value" "hello" "key" ["type" {"data" "world"}]}))
  => (fn [s] (str/includes? s "world")))


^{:refer std.lang.interface.type-notify/process-capture :added "4.0"}
(fact "processes `capture` id option"
  (notify/process-capture {:a 1})
  (last @notify/*notify-capture*)
  => {:a 1})

^{:refer std.lang.interface.type-notify/process-message :added "4.0"}
(fact "processes a message recieved by the notification server"
  (let [out (atom nil)]
    (notify/add-listener +server+ "msg" :key (fn [v] (reset! out v)))
    (notify/process-message +server+ (json/write {:id "msg" :data "hello"}))
    (notify/process-message +server+ (json/write {:id "msg" :data "hello"}))
    @out)
  => (contains {:data "hello"}))

^{:refer std.lang.interface.type-notify/handle-notify-http :added "4.0"}
(fact "handler for http request")

^{:refer std.lang.interface.type-notify/start-notify-http :added "4.0"}
(fact "starts http server"
  ^:hidden
  
  (do (def +value+ (h/sid))
      (http/post (str "http://127.0.0.1:" (:http-port +server+) "/")
                 {:body (json/write {:id "hello"
                                     :data +value+})})
      
      (get @(notify/get-sink +server+ "hello")
           "data"))
  => +value+)

^{:refer std.lang.interface.type-notify/stop-notify-http :added "4.0"}
(fact "stops http server"
  (notify/stop-notify-http +server+)
  @(:http-instance +server+) => nil)

^{:refer std.lang.interface.type-notify/handle-notify-socket :added "4.0"}
(fact "handler for socket request")

^{:refer std.lang.interface.type-notify/start-notify-socket :added "4.0"}
(fact "starts socket server"
  (notify/start-notify-socket +server+)
  @(:socket-instance +server+) => map?)

^{:refer std.lang.interface.type-notify/stop-notify-socket :added "4.0"}
(fact "stops socket server"
  (notify/stop-notify-socket +server+)
  @(:socket-instance +server+) => nil)

^{:refer std.lang.interface.type-notify/start-notify :added "4.0"
  :setup [(notify/stop-notify +server+)]}
(fact "starts both servers"

  (notify/start-notify +server+)
  => map?)

^{:refer std.lang.interface.type-notify/stop-notify :added "4.0"}
(fact "stops both servers"
  (notify/stop-notify +server+)
  @(:http-instance +server+) => nil
  @(:socket-instance +server+) => nil)

^{:refer std.lang.interface.type-notify/notify-server:create :added "4.0"}
(fact "creates notify serve "
  (notify/notify-server:create {})
  => map?)

^{:refer std.lang.interface.type-notify/notify-server :added "4.0"}
(fact "create and start notify server"
  (h/stop (notify/notify-server {}))
  => map?)

^{:refer std.lang.interface.type-notify/default-notify :added "4.0"}
(fact "gets the default notify server"

  (notify/default-notify)
  => map?)

^{:refer std.lang.interface.type-notify/default-notify:reset :added "4.0"}
(fact "resets the default notify server"
  (notify/default-notify:reset)
  => map?)

^{:refer std.lang.interface.type-notify/watch-oneshot :added "4.0"}
(fact "returns a completable future"

  (notify/watch-oneshot +server+
                        10)
  => (contains [string? h/future?]))

(comment
  (./import))
