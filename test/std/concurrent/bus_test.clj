(ns std.concurrent.bus-test
  (:use [code.test :exclude [global]])
  (:require [std.concurrent.bus :refer :all]
            [std.concurrent :as cc]
            [std.concurrent.queue :as q]
            [std.lib.future :as f]))

^{:refer std.concurrent.bus/bus:get-thread :added "3.0"}
(fact "gets thread given an id"

  (bus:with-temp bus
                 (->> (bus:get-id bus)
                      (bus:get-thread bus)))
  => (cc/thread:current))

^{:refer std.concurrent.bus/bus:get-id :added "3.0"}
(fact "gets registered id given thread"

  (bus:with-temp bus
                 (bus:get-id bus))
  => string?)

^{:refer std.concurrent.bus/bus:has-id? :added "3.0"}
(fact "checks that the bus has a given id"

  (bus:with-temp bus
                 (bus:has-id? bus (bus:get-id bus)))
  => true)

^{:refer std.concurrent.bus/bus:get-queue :added "3.0"}
(fact "gets the message queue associated with the thread"

  (bus:with-temp bus
                 (vec (bus:get-queue bus)))
  => [])

^{:refer std.concurrent.bus/bus:all-ids :added "3.0"}
(fact "returns all registered ids"

  (bus:with-temp bus
                 (bus:all-ids bus))
  => (contains #{string?}))

^{:refer std.concurrent.bus/bus:all-threads :added "3.0"}
(fact "returns all registered threads"

  (bus:with-temp bus
                 (= (first (vals (bus:all-threads bus)))
                    (Thread/currentThread)))
  => true)

^{:refer std.concurrent.bus/bus:get-count :added "3.0"}
(fact "returns the number of threads registered"

  (bus:with-temp bus
                 (bus:get-count bus))
  => 1)

^{:refer std.concurrent.bus/bus:register :added "3.0"}
(fact "registers a thread to the bus"

  (bus:with-temp bus
                 (bus:register bus "foo" (Thread.))
                 (bus:has-id? bus "foo"))
  => true)

^{:refer std.concurrent.bus/bus:deregister :added "3.0"}
(fact "deregisters from the bus"

  (bus:with-temp bus
                 (bus:register bus "foo" (Thread.))
                 (bus:deregister bus "foo")
                 (bus:has-id? bus "foo"))
  => false)

^{:refer std.concurrent.bus/bus:send :added "3.0"}
(fact "sends a message to the given thread" ^:hidden

  (bus:with-temp bus
                 (bus:send bus (bus:get-id bus)
                           {:op :hello :message "world"})
                 (cc/take (bus:get-queue bus) 1000 :ms))
  => (contains {:op :hello, :message "world", :id string?}))

^{:refer std.concurrent.bus/bus:wait :added "3.0"}
(fact "bus:waits on the message queue for message"

  (bus:with-temp bus
                 (bus:send bus (bus:get-id bus)
                           {:op :hello :message "world"})
                 (bus:wait bus {:timeout 1000}))
  => (contains {:op :hello, :message "world", :id string?})

  (bus:with-temp bus
                 (bus:wait bus {:timeout 100})))

^{:refer std.concurrent.bus/handler-thunk :added "3.0"}
(fact "creates a thread loop for given message handler"

  (bus:with-temp bus
                 (let [output (q/queue)
                       bus (assoc bus :output output)
                       handler (fn [msg] (assoc msg :handled true))
                       thunk (handler-thunk bus handler {:stopped (f/incomplete)})
                       id (bus:get-id bus)]
                   (f/future
                     (bus:register bus id (Thread/currentThread))
                     (thunk))
                   (Thread/sleep 10)
                   (bus:send bus id {:op :test})
                   (q/take output 1000 :ms)))
  => (contains {:status :success
                :data (contains {:handled true})}))

^{:refer std.concurrent.bus/run-handler :added "3.0"}
(fact "runs the handler in a thread loop"

  (bus:with-temp bus
                 (let [started (f/incomplete)
                       stopped (f/incomplete)
                       _ (f/future (run-handler bus (fn [m] (assoc m :done true))
                                                 {:started started :stopped stopped}))
                       {id :id} (deref started 1000 :timeout)]
                   (bus:send bus id {:op :test})
                   (bus:send bus id {:op :exit})
                   (deref stopped 1000 :timeout)))
  => (contains {:exit :normal}))

^{:refer std.concurrent.bus/bus:send-all :added "3.0"}
(fact "bus:sends message to all thread queues"

  (bus:with-temp bus
                 (bus:send-all bus {:op :all})
                 (bus:wait bus {:timeout 1000}))
  => (contains {:op :all}))

^{:refer std.concurrent.bus/bus:open :added "3.0"}
(fact "bus:opens a new handler loop given function"

  (bus:with-temp bus
                 (let [{:keys [id]} (deref (bus:open bus (fn [m]
                                                           (update m :value inc)))
                                           1000 :timeout)]
                   (Thread/sleep 100)
                   (deref (bus:send bus id {:value 1}) 1000 :timeout)))
  => (contains {:value 2, :id string?}) ^:hidden

  (bus:with-temp bus
                 (let [{:keys [id stopped]} (deref (bus:open bus (fn [m]
                                                                   (update m :value inc))
                                                             {:timeout 400})
                                                   1000 :timeout)]
                   [(deref stopped 1000 :timeout) (bus:get-count bus)]))
  => (contains-in [{:exit :timeout
                    :id string?
                    :start number?
                    :end number?}
                   1]))

^{:refer std.concurrent.bus/bus:close :added "3.0"}
(fact "bus:closes all bus:opened loops"

  (bus:with-temp bus
                 (let [{:keys [stopped id]} @(bus:open bus (fn [m]
                                                             (update m :value inc)))]
                   (Thread/sleep 10)
                   (bus:close bus id)
                   [@stopped (bus:get-count bus)]))
  => (contains-in [{:exit :normal,
                    :id string?
                    :unprocessed empty?
                    :start number?
                    :end number?}
                   1]))

^{:refer std.concurrent.bus/bus:close-all :added "3.0"}
(fact "stops all thread loops" ^:hidden

  (bus:with-temp bus
                 (let [_ @(bus:open bus (fn [m]
                                          (update m :value inc)))
                       _ @(bus:open bus (fn [m]
                                          (update m :value inc)))
                       _ @(bus:open bus (fn [m]
                                          (update m :value inc)))
                       _ @(bus:open bus (fn [m]
                                          (update m :value inc)))]
                   (Thread/sleep 10)
                   (bus:close-all bus)
                   (Thread/sleep 100)
                   (bus:get-count bus)))
  => 1)

^{:refer std.concurrent.bus/bus:kill :added "3.0"}
(fact "bus:closes all bus:opened loops" ^:hidden

  (bus:with-temp bus
                 (let [{:keys [id stopped]} @(bus:open bus (fn [m]
                                                             (update m :value inc)))]
                   (Thread/sleep 10)
                   (bus:kill bus id)
                   [(deref stopped 1000 :timeout) (bus:get-count bus)]))
  => (contains-in [{:exit :interrupted
                    :id string?
                    :unprocessed empty?
                    :start number?
                    :end number?}
                   1]))

^{:refer std.concurrent.bus/bus:kill-all :added "3.0"}
(fact "stops all thread loops" ^:hidden

  (bus:with-temp bus
                 (let [_ @(bus:open bus (fn [m]
                                          (update m :value inc)))
                       _ @(bus:open bus (fn [m]
                                          (update m :value inc)))
                       _ @(bus:open bus (fn [m]
                                          (update m :value inc)))
                       _ @(bus:open bus (fn [m]
                                          (update m :value inc)))]
                   (Thread/sleep 10)
                   (bus:kill-all bus)
                   (Thread/sleep 10)
                   (bus:get-count bus)))
  => 1)

^{:refer std.concurrent.bus/main-thunk :added "3.0"}
(fact "creates main message return handler"

  (bus:with-temp bus
                 (let [thunk (main-thunk bus)
                       output (:output bus)
                       results (:results bus)
                       id "test-id"
                       ret (f/incomplete)
                       t (Thread. thunk)]
                   (swap! results assoc id ret)
                   (q/put output {:id id :status :success :data "data"})
                   (.start t)
                   (try
                     (deref ret 1000 :timeout)
                     (finally
                       (.interrupt t)
                       (.join t 100)))))
  => "data")

^{:refer std.concurrent.bus/main-loop :added "3.0"}
(fact "creates a new message return loop"
  (bus:with-temp bus
                 (bus:send bus (bus:get-id bus)
                           {:op :hello :message "world"})))

^{:refer std.concurrent.bus/started?-bus :added "3.0"}
(fact "checks if bus is running"

  (bus:with-temp bus
                 (Thread/sleep 10)
                 (started?-bus bus))
  => true)

^{:refer std.concurrent.bus/start-bus :added "3.0"}
(fact "starts the bus"

  (let [bus (start-bus (bus:create))]
    (loop [i 0]
      (if (started?-bus bus)
        true
        (if (< i 10)
          (do (Thread/sleep 20) (recur (inc i)))
          false))))
  => true)

^{:refer std.concurrent.bus/stop-bus :added "3.0"}
(fact "stops the bus"

  (let [bus (doto (bus:create)
              (start-bus))]
    (loop [i 0]
      (if (started?-bus bus)
        (do (stop-bus bus)
            (not (started?-bus bus)))
        (if (< i 10)
          (do (Thread/sleep 20) (recur (inc i)))
          false))))
  => true)

^{:refer std.concurrent.bus/info-bus :added "3.0"}
(fact "returns info about the bus"

  (bus:with-temp bus
                 (loop [i 0]
                   (if (started?-bus bus)
                     (info-bus bus)
                     (if (< i 10)
                       (do (Thread/sleep 20) (recur (inc i)))
                       (info-bus bus)))))
  => (contains {:running true}))

^{:refer std.concurrent.bus/bus:create :added "3.0"}
(fact "creates a bus"

  (bus:create)
  => bus?)

^{:refer std.concurrent.bus/bus :added "3.0"}
(fact "creates and starts a bus"

  (bus)
  => bus?)

^{:refer std.concurrent.bus/bus? :added "3.0"}
(fact "checks if object is instance of Bus"

  (bus? (bus))
  => true)

^{:refer std.concurrent.bus/bus:with-temp :added "3.0"
  :style/indent 1}
(fact "checks if object is instance of Bus"

  (bus:with-temp bus
                 (bus? bus))
  => true)

^{:refer std.concurrent.bus/bus:reset-counters :added "4.0"}
(fact "resets the counters for a bus"

  (bus:with-temp bus
                 (bus:send bus (bus:get-id bus) {:op :test})
                 (bus:reset-counters bus)
                 @(:received (:counters bus)))
  => 0)

(comment

  (use 'jvm.tool)
  (./arrange)
  (./incomplete)
  (./import)

  (bus)
  (list-active)
  (bus:kill-active)

  (stop-bus *1)

  (stop-bus -bus1-)
  (def -bus1- (bus))
  (into {} -bus1-)

  (spawn -bus1- (fn [m]
                  (prn :PROCESSING m)
                  (thread/sleep 1000)
                  (prn :DONE m))
         {:on-stop (fn [] (prn :STOPPING))})

  (bus:send-all -bus1- {:hello 2}))
