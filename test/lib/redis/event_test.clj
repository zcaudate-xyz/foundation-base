(ns lib.redis.event-test
  (:use code.test)
  (:require [lib.redis.event :refer :all]
            [lib.redis :as r]
            [lib.redis.bench :as bench]
            [net.resp.pool :as pool]
            [net.resp.connection :as conn]
            [net.resp.wire :as wire]
            [std.concurrent :as cc]
            [std.lib :as h]))

(def |client|
  {:pool :pool
   :runtime (atom {:listeners {}})})

^{:refer lib.redis.event/action:add :added "3.0"}
(fact "adds an action from registry"
  (action:add :test {:a 1})
  => (contains {:test {:a 1}}))

^{:refer lib.redis.event/action:remove :added "3.0"}
(fact "removes an action from registry"
  (action:add :test {:a 1})
  (action:remove :test)
  => map?)

^{:refer lib.redis.event/action:list :added "3.0"}
(fact "lists action types"
  (action:list)
  => seq?)

^{:refer lib.redis.event/action:get :added "3.0"}
(fact "gets action type"
  (action:add :test {:a 1})
  (action:get :test)
  => {:a 1})

^{:refer lib.redis.event/listener-string :added "3.0"}
(fact "string description of a listener")

^{:refer lib.redis.event/listener? :added "3.0"}
(fact "checks that object is a listener")

^{:refer lib.redis.event/listener-loop :added "3.0"}
(fact "creates a listener loop")

^{:refer lib.redis.event/listener:create :added "3.0"}
(fact "creates a listener"
  (with-redefs [cc/pool:acquire (constantly [:raw-id :raw])
                action:get (constantly {:args (constantly []) :subscribe "SUB" :wrap (constantly identity)})
                wire/call (constantly nil)
                h/future (constantly :future)]
    (listener:create |client| :test :id "in" identity))
  => listener?)

^{:refer lib.redis.event/listener:teardown :added "3.0"}
(fact "tears down the listener"
  (with-redefs [action:get (constantly {:unsubscribe "UNSUB"})
                wire/write (constantly nil)
                h/future:cancel (constantly nil)
                cc/pool:release (constantly nil)]
    (listener:teardown {:type :test :connection {:id :id :args [] :pool :pool :thread :thread :raw :raw}}))
  => :id)

^{:refer lib.redis.event/listener:add :added "3.0"}
(fact "adds a listener to the redis client"
  (with-redefs [listener:create (constantly {:id :id :type :test})
                h/sid (constantly "sid")]
    (listener:add :test |client| :id "in" identity))
  => map?)

^{:refer lib.redis.event/listener:remove :added "3.0"}
(fact "removes a listener from the client"
  (with-redefs [listener:teardown (constantly nil)]
    (swap! (:runtime |client|) assoc-in [:listeners :test :id] {})
    (listener:remove :test |client| :id))
  => {})

^{:refer lib.redis.event/listener:all :added "3.0"}
(fact "lists all listeners"
  (swap! (:runtime |client|) assoc :listeners {:test {:id {:a 1}}})
  (listener:all |client|)
  => [{:a 1}])

^{:refer lib.redis.event/listener:count :added "3.0"}
(fact "counts all listeners"
  (listener:count |client|)
  => {:test 1})

^{:refer lib.redis.event/listener:list :added "3.0"}
(fact "lists all listeners"
  (listener:list |client|)
  => {:test [:id]})

^{:refer lib.redis.event/listener:get :added "3.0"}
(fact "gets a client listener"
  (listener:get |client| :test :id)
  => {:a 1})

^{:refer lib.redis.event/subscribe:wrap :added "3.0"}
(fact "wrapper for the subscribe delivery function"
  ((subscribe:wrap (fn [ch redis _ msg] msg) {:format :edn})
   [(.getBytes "message") (.getBytes "channel") (.getBytes "{:a 1}")])
  => {:a 1})

^{:refer lib.redis.event/subscribe :added "3.0"}
(fact "subscribes to a channel on the cache"
  (with-redefs [listener:add (constantly :added)]
    (subscribe |client| :id ["ch"] identity))
  => :added)

^{:refer lib.redis.event/unsubscribe :added "3.0"}
(fact "unsubscribes from a channel"
  (with-redefs [listener:remove (constantly :removed)]
    (unsubscribe |client| :id))
  => :removed)

^{:refer lib.redis.event/psubscribe:wrap :added "3.0"}
(fact "wrapper for the psubscribe delivery function"
  ((psubscribe:wrap (fn [ch redis _ msg] msg) {:format :edn})
   [(.getBytes "pmessage") (.getBytes "pattern") (.getBytes "channel") (.getBytes "{:a 1}")])
  => {:a 1})

^{:refer lib.redis.event/psubscribe :added "3.0"}
(fact "subscribes to a pattern on the cache"
  (with-redefs [listener:add (constantly :added)]
    (psubscribe |client| :id ["*"] identity))
  => :added)

^{:refer lib.redis.event/punsubscribe :added "3.0"}
(fact "unsubscribes from the pattern"
  (with-redefs [listener:remove (constantly :removed)]
    (punsubscribe |client| :id))
  => :removed)

^{:refer lib.redis.event/events-string :added "3.0"}
(fact "creates a string from a set of enums"
  (events-string #{:string :hash :generic})
  => "h$g")

^{:refer lib.redis.event/events-parse :added "3.0"}
(fact "creates a set of enums from a string"
  (events-parse "h$g")
  => #{:hash :string :generic})

^{:refer lib.redis.event/config:get :added "3.0"}
(fact "gets the config for notifications"
  (with-redefs [pool/pool:request-single (constantly ["key" "K$g"])]
    (config:get |client|))
  => #{:string :generic})

^{:refer lib.redis.event/config:set :added "3.0"}
(fact "sets the config for notifications"
  (with-redefs [pool/pool:request-single (constantly "OK")]
    (config:set |client| #{:string}))
  => "OK")

^{:refer lib.redis.event/config:add :added "3.0"}
(fact "adds config notifications"
  (with-redefs [config:get (constantly #{:string})
                config:set (constantly "OK")]
    (config:add |client| #{:generic}))
  => #{:string})

^{:refer lib.redis.event/config:remove :added "3.0"}
(fact "removes config notifications"
  (with-redefs [config:get (constantly #{:string :generic})
                config:set (constantly "OK")]
    (config:remove |client| #{:generic}))
  => #{:string :generic})

^{:refer lib.redis.event/notify:args :added "3.0"}
(fact "produces the notify args"
  (notify:args "test" ["input"])
  => ["__keyspace@*:test:input"])

^{:refer lib.redis.event/notify:wrap :added "3.0"}
(fact "wrapper for the notify delivery function"
  ((notify:wrap (fn [redis key] key) {:namespace "test"})
   [(.getBytes "pmessage")
    (.getBytes "pattern")
    (.getBytes "__keyspace@*:test:input")
    (.getBytes "set")])
  => "input")

^{:refer lib.redis.event/notify :added "3.0"}
(fact "notifications for a given client"
  (with-redefs [listener:add (constantly :added)]
    (notify |client| :id "*" identity))
  => :added)

^{:refer lib.redis.event/unnotify :added "3.0"}
(fact "removes notifications for a given client"
  (with-redefs [listener:remove (constantly :removed)]
    (unnotify |client| :id))
  => :removed)

^{:refer lib.redis.event/has-notify :added "3.0"}
(fact "checks that a given notify listener is installed"
  (swap! (:runtime |client|) assoc-in [:listeners :notify :id] {})
  (has-notify |client| :id)
  => true)

^{:refer lib.redis.event/list-notify :added "3.0"}
(fact "lists all notify listeners for a client"
  (list-notify |client|)
  => {:id {}})

^{:refer lib.redis.event/start:events-redis :added "3.0"}
(fact "creates action for `:events`"
  (with-redefs [config:set (constantly nil)]
    (start:events-redis |client| #{}))
  => map?)

^{:refer lib.redis.event/start:notify-redis :added "3.0"}
(fact "creates action for `:notify`"
  (with-redefs [notify (constantly nil)]
    (start:notify-redis |client| {:id {:pattern "*" :handler identity}}))
  => map?)

^{:refer lib.redis.event/stop:notify-redis :added "3.0"}
(fact "stop action for `:notify` field"
  (with-redefs [unnotify (constantly nil)]
    (stop:notify-redis |client| {:id {}}))
  => map?)
