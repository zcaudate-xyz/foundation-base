(ns std.concurrent.relay-test
  (:use code.test)
  (:require [std.concurrent.relay :refer :all]
            [std.concurrent :as cc]
            [std.string :as str]
            [std.lib :as h])
  (:import (java.net ServerSocket
                     Socket))
  (:refer-clojure :exclude [send]))

^{:refer std.concurrent.relay/get-bus :added "3.0"}
(fact "gets the common stream bus"
  ^:hidden

  (get-bus)
  => map?)

^{:refer std.concurrent.relay/with:bus :added "4.0"}
(fact "sets the default relay bus"
  ;; (with:bus [nil]
  ;;   std.concurrent.relay/*bus*)
  ;; => nil?
  )

^{:refer std.concurrent.relay/attach-read-passive :added "4.0"}
(fact "attaches a passive process to an input stream")

^{:refer std.concurrent.relay/attach-interactive :added "4.0"}
(fact "attaches a bus process to an input stream")

^{:refer std.concurrent.relay/relay-stream :added "4.0"}
(fact "creates a relay stream"
  ^:hidden
  
  (relay-stream "hello"
                :input
                nil
                {})
  => relay-stream?)

^{:refer std.concurrent.relay/relay-stream? :added "4.0"}
(fact "checks if object is a relay stream"
  (relay-stream? (relay-stream "hello" :input nil {}))
  => true)

^{:refer std.concurrent.relay/make-socket-instance :added "4.0"
  :setup [(def +server+ (ServerSocket. (h/port:check-available 0)))
          (def +socket+ (h/socket (.getLocalPort ^ServerSocket +server+)))]}
(fact "creates a socket instance"
  ^:hidden
  
  (def +instance+ (make-socket-instance +socket+
                                        "hello"
                                        {}))
  (set (keys +instance+))
  => #{:started :socket :type :out :id :in}

  (do (.close ^ServerSocket +server+)
      (.close ^Socket +socket+)))

^{:refer std.concurrent.relay/make-process-instance :added "4.0"
  :setup [(def +process+ (h/sh "ls" {:wait false}))]}
(fact "creates a process instance"
  ^:hidden
  
  (def +instance+ (make-process-instance +process+ "hello"  {}))

  +instance+
  => map?
  
  @(:thread (:err +instance+))
  => map?)

^{:refer std.concurrent.relay/make-instance :added "4.0"}
(fact "creates an instance"
  ^:hidden
  
  (make-instance (h/sh "ls" {:wait false}))
  => map?)

^{:refer std.concurrent.relay/relay-start :added "4.0"}
(fact "starts the relay"
  ^:hidden
  
  (def +relay+ (doto (relay:create {:type :process
                                    :args ["bash"]})
                 (relay-start)))
  
  (cc/bus:has-id? (get-bus) (:id +relay+))
  => true
  
  (do (def +process+ (:process @(:instance +relay+)))
      (.isAlive ^Process +process+))
  => true
  
  (:output @(send +relay+ "echo hello"))
  => "hello\n"

  (do (send +relay+ {:op :partial
                      :line "echo hello"})
      (send +relay+ {:op :partial
                     :line "echo hello"})
      
      (:count   @(send +relay+ {:op :count})))
  => 12
  
  (:output  @(send +relay+ {:op :read-limit
                            :limit 8}))
  => "hello\nhe"
  
  (:dropped @(send +relay+ {:op :clean}))
  => 4

  (h/stop +relay+)
  => map?)

^{:refer std.concurrent.relay/relay-stop :added "4.0"}
(fact "stops the relay"
  (let [relay (doto (relay:create {:type :process
                                   :args ["bash"]})
                (relay-start))]
    (relay-stop relay)
    (h/stopped? relay))
  => true)

^{:refer std.concurrent.relay/relay:create :added "4.0"}
(fact "creates a relay"
  (relay:create {:type :process :args ["bash"]})
  => map?)

^{:refer std.concurrent.relay/relay :added "3.0"}
(fact "creates and starts a relay"
  (relay {:type :process :args ["bash"]})
  => map?)

^{:refer std.concurrent.relay/send :added "3.0"}
(fact "sends command to relay"
  (let [r (relay {:type :process :args ["bash"]})]
    (try
      (-> @(send r "echo hello") :output)
      => "hello\n"
      (finally
        (relay-stop r)))))

(comment
  (./import)
  (./create-tests)
  
  (def -r- (relay {:type :socket
                   :host "localhost"
                   :port 51311}))
  
  (def -r- (relay {:type :process
                   :args ["lua" "-i"]
                   }))
  

  (:bus @(:instance -r-))
  @(send:instance @(:instance -r-) "print(1 + 1)")

  @(send -r- "print(1 + 1)\n1")
  
  (:id @(:instance -r-))
  )
