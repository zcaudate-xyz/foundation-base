(ns std.lang.interface.type-shared-test
  (:use code.test)
  (:require [std.lang.interface.type-shared :as shared]
            [std.lang :as l]))

^{:refer std.lang.interface.type-shared/get-groups :added "4.0"}
(fact "gets all shared groups"

  (shared/get-groups)
  ;; (:hara/rt.postgres :hara/rt.redis :hara/rt.nginx :hara/rt.cpython.shared :hara/rt.luajit.shared)
  => vector?)

^{:refer std.lang.interface.type-shared/get-group-count :added "4.0"}
(fact "gets the group count for a type and id"

  (shared/get-group-count :hara/rt.redis)
  ;; {:default 21, :test 2}
  => map?)

^{:refer std.lang.interface.type-shared/update-group-count :added "4.0"
  :setup [(reset! shared/*groups* {})]}
(fact "updates the group counte"
  (shared/update-group-count :test :id (fnil inc 0))
  (shared/get-group-count :test :id)
  => 1)

^{:refer std.lang.interface.type-shared/get-group-instance :added "4.0"}
(fact "gets the group instance"
  (shared/set-group-instance :test :id {:a 1})
  (shared/get-group-instance :test :id)
  => {:a 1})

^{:refer std.lang.interface.type-shared/set-group-instance :added "4.0"}
(fact "sets the group instance"
  (shared/set-group-instance :test :id {:a 2})
  (shared/get-group-instance :test :id)
  => {:a 2})

^{:refer std.lang.interface.type-shared/update-group-instance :added "4.0"}
(fact "updates the group instance"
  (shared/update-group-instance :test :id (fn [m] (assoc m :b 2)))
  (shared/get-group-instance :test :id)
  => {:a 2 :b 2})

^{:refer std.lang.interface.type-shared/restart-group-instance :added "4.0"}
(fact "restarts the group instance"
  (shared/set-group-instance :test :id {:a 1} 1 {} {:constructor (fn [cfg] {:restarted true})})
  (shared/restart-group-instance :test :id)
  => {:restarted true})

^{:refer std.lang.interface.type-shared/remove-group-instance :added "4.0"}
(fact "removes the group instance"
  (shared/remove-group-instance :test :id)
  (shared/get-group-instance :test :id)
  => nil)

^{:refer std.lang.interface.type-shared/start-shared :added "4.0"}
(fact "starts a shared runtime client"
  (shared/start-shared {:id :id :client {:type :test :constructor (fn [cfg] {:started true})} :config {}})
  (shared/get-group-instance :test :id)
  => {:started true})

^{:refer std.lang.interface.type-shared/stop-shared :added "4.0"
  :setup [(reset! shared/*groups* {})]}
(fact "stops a shared runtime client"
  (shared/start-shared {:id :id :temp true :client {:type :test :constructor (fn [cfg] {:started true})} :config {}})
  (shared/stop-shared {:id :id :temp true :client {:type :test :constructor (fn [cfg] {:started true})} :config {}})
  (shared/get-group-instance :test :id)
  => nil)

^{:refer std.lang.interface.type-shared/rt-shared:create :added "4.0"}
(fact "creates a shared runtime client"
  (shared/rt-shared:create {:rt/id :id :rt/client {:type :test} :a 1})
  => (contains {:id :id :config {:a 1}}))

^{:refer std.lang.interface.type-shared/rt-shared :added "4.0"}
(fact "creates and starts and shared runtime client"
  (shared/rt-shared {:rt/id :id :rt/client {:type :test :constructor (fn [cfg] {:started true})}})
  (shared/get-group-instance :test :id)
  => {:started true})

^{:refer std.lang.interface.type-shared/rt-is-shared? :added "4.0"}
(fact "checks if a runtime is shared"
  (shared/rt-is-shared? (shared/rt-shared:create {}))
  => true)

^{:refer std.lang.interface.type-shared/rt-get-inner :added "4.0"}
(fact "gets the inner runtime"
  (shared/set-group-instance :test :id {:inner true})
  (let [rt (shared/rt-shared:create {:rt/id :id :rt/client {:type :test}})]
    (shared/rt-get-inner rt))
  => {:inner true})

(comment
  
  (rt-is-shared? (rt-shared:create {}))
  
  (require '[rt.basic.impl.script :as script]))
