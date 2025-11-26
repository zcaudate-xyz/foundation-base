(ns std.concurrent.atom-test
  (:use code.test)
  (:require [std.concurrent.atom :refer :all]
            [std.concurrent.executor :as executor]
            [std.lib.future :as f]
            [std.lib :as h]))

^{:refer std.concurrent.atom/aq:new :added "3.0"}
(fact "creates an atom with a vec as queue"

  @(aq:new)
  => [])

^{:refer std.concurrent.atom/aq:process :added "3.0"}
(fact "processes elements given a batch"
  
  (let [+state+ (atom [])]
  
    (aq:process (fn [elems] (swap! +state+ conj elems))
                (atom [1 2 3 4 5]) 3)

    @+state+)
  => [[1 2 3] [4 5]])

^{:refer std.concurrent.atom/aq:submit :added "3.0"}
(fact "submission function for one or multiple entries to aq"
  
  (let [-out- (atom [])
        -exe- (executor/executor:single)
        -q-   (atom [])
        -sub- (aq:submit -exe- -q- {:handler (fn [_ items]
                                               (swap! -out- concat items))
                                    :interval 10
                                    :max-batch 10})]
    (try
      (-sub- 1 2 3)
      (Thread/sleep 100)
      @-out-
      (finally
        (executor/exec:shutdown -exe-))))
  => [1 2 3])

^{:refer std.concurrent.atom/aq:executor :added "3.0"}
(fact "creates a executor that takes in an atom queue"

  (let [-res- (atom [])
        -aq- (aq:executor {:handler (fn [_ items]
                                      (swap! -res- concat items))
                           :interval 10
                           :max-batch 10})
        -exe- (:executor -aq-)]
    (try
      ((:submit -aq-) 1 2 3)
      (Thread/sleep 100)
      @-res-
      (finally
        (executor/exec:shutdown -exe-))))
  => [1 2 3])

^{:refer std.concurrent.atom/hub-state :added "4.0"}
(fact "creates a hub state"

  (hub-state [1 2 3])
  => (contains {:ticket f/future?
                :queue [1 2 3]}))

^{:refer std.concurrent.atom/hub:new :added "3.0"}
(fact "creates a trackable atom queue"

  @(hub:new [1 2 3])
  => (contains {:ticket f/future?
                :queue [1 2 3]}))

^{:refer std.concurrent.atom/hub:process :added "3.0"}
(fact "like aq:process but with a hub"

  (let [+state+ (atom [])]
  
    (hub:process (fn [elems] (swap! +state+ conj elems))
                 (hub:new [1 2 3 4 5]) 3)
  
    @+state+)
  => [[1 2 3] [4 5]])

^{:refer std.concurrent.atom/hub:add-entries :added "3.0"}
(fact "adds entries to the hub"

  (hub:add-entries (hub:new) [1 2 3 4 5])
  => (contains [f/future? 0 5]))

^{:refer std.concurrent.atom/hub:submit :added "3.0"}
(fact "submission function for the hub"

  (let [-out- (atom [])
        -exe- (executor/executor:single)
        -hub- (hub:new)
        -sub- (hub:submit nil -exe- -hub- {:handler (fn [_ items]
                                                      (swap! -out- concat items)
                                                      items)
                                           :interval 10
                                           :max-batch 10})]
    (try
      (let [[ticket] (-sub- 1 2 3)]
        @ticket)
      @-out-
      (finally
        (executor/exec:shutdown -exe-))))
  => [1 2 3])

^{:refer std.concurrent.atom/hub:executor :added "3.0"}
(fact "creates a hub based executor"
  
  (let [-exe- (hub:executor nil {:handler (fn [& args]
                                            args)
                                 :interval 50
                                 :max-batch 1000})
        -exec- (:executor -exe-)]
    (try
      (do (def -res- ((:submit -exe-) 1 2 3 4 5 6))
          @(first -res-))
      => '[(nil (1 2 3 4 5 6))]

      (hub:wait (:queue -exe-))
      => nil
      (finally
        (executor/exec:shutdown -exec-)))))

^{:refer std.concurrent.atom/hub:wait :added "4.0"}
(fact "waits for the hub executor to be ready"

  (let [-hub- (hub:new [1 2 3])]
    (f/future (Thread/sleep 50)
              (swap! -hub- update :ticket f/future:force true))
    (hub:wait -hub-))
  => true)

(fact "hello"
  ^:hidden
  
  (let [a 1]
    (list 'a)
    => '(a)))
