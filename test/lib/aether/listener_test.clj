(ns lib.aether.listener-test
  (:use code.test)
  (:require [lib.aether.listener :refer :all]
            [jvm.artifact :as artifact]))

^{:refer lib.aether.listener/event->rep :added "3.0"}
(fact "converts the event to a map representation"
  (event->rep {:event {:artifact "hara:hara:2.4.0"}})
  => (contains {:group "hara", :artifact "hara", :version "2.4.0"}))

^{:refer lib.aether.listener/record :added "3.0"}
(fact "adds an event to the recorder"
  (record :deploying {:id "event"})
  => (contains {:type :deploying
                :event {:id "event"}
                :time number?}))

^{:refer lib.aether.listener/aggregate :added "3.0"}
(fact "summarises all events that have been processed"
  (aggregate [{:type :deploying :event {:artifact "a"} :time 100}
              {:type :deployed :event {:artifact "a"} :time 200}])
  => {"a" [{:type :deploy :start 100 :total 100 :artifact "a"}]})

^{:refer lib.aether.listener/process-event :added "3.0"}
(fact "processes a recorded event"
  (process-event {:type :deploying :event {:artifact "a"} :time 100})
  => nil)
