(ns std.scheduler-test
  (:use code.test)
  (:require [std.scheduler :refer :all]
            [std.scheduler.spawn :as spawn]
            [std.concurrent :as cc]
            [std.lib :as h]))

(fact:global
 {:component
  {|run| {:create   (runner:create {})
          :setup    runner:start
          :teardown runner:stop}}})

(defn test-scaffold
  ([f]
   (test-scaffold f 100))
  ([f interval]
   (h/with:component [runner (runner:create)]
                     (let [q (cc/queue)]
                       (install runner {:id :world
                                        :type :basic
                                        :interval interval
                                        :main-fn (fn [args t]
                                                   (cc/put q t))})
                       (f runner q)))))

^{:refer std.scheduler/runner:start :added "3.0"
  :let [|runner| (runner:create {})]
  :teardown [(runner:stop |runner|)]}
(fact "starts up the runner"

  (-> (runner:start |runner|)
      (runner:info))
  =>  {:executors {:core {:threads 0, :active 0, :queued 0, :terminated false},
                   :scheduler {:threads 0, :active 0, :queued 0, :terminated false}},
       :programs {}})

^{:refer std.scheduler/runner:stop :added "3.0"}
(fact "stops the runner")

^{:refer std.scheduler/runner:kill :added "3.0"}
(fact "kills the runner")

^{:refer std.scheduler/runner:started? :added "3.0"
  :use [|run|]}
(fact "checks if runner is started"

  (runner:started? |run|)
  => true)

^{:refer std.scheduler/runner:stopped? :added "3.0"
  :use [|run|]}
(fact "checks if runner has stopped"

  (-> (doto |run| (runner:kill))
      (runner:stopped?))
  => true)

^{:refer std.scheduler/runner:health :added "3.0"
  :use [|run|]}
(fact "returns health of runner"

  (runner:health |run|)
  => {:status :ok})

^{:refer std.scheduler/runner:info :added "3.0"}
(fact "returns runner info"

  (h/with:component [runner (runner:create {})]
                    (runner:info runner))
  => {:executors {:core {:threads 0, :active 0, :queued 0, :terminated false},
                  :scheduler {:threads 0, :active 0, :queued 0, :terminated false}},
      :programs {}} ^:hidden

  (test-scaffold (fn [runner q]
                   (spawn runner :world {} "abc")
                   (doall (for [i (range 10)]
                            (cc/take q)))
                   (runner:info runner)))
  => (contains-in
      {:executors {:core {:threads 0, :active 0, :queued 0, :terminated false},
                   :scheduler {:threads 2}},
       :programs {:world {:type :basic,
                          :current {"abc" {:status :running, :duration string?
                                           :jobs {:submitted number?, :succeeded number?}}},
                          :counts {:submitted number?, :succeeded number?}}}}))

^{:refer std.scheduler/runner? :added "3.0"
  :use [|run|]}
(fact "checks if object is a runner"

  (runner? |run|)
  => true)

^{:refer std.scheduler/runner:create :added "3.0"}
(fact "creates a runner"

  (runner:create {:id "runner"})
  => runner?)

^{:refer std.scheduler/runner :added "3.0"}
(fact "creates and starts a runner"

  (-> (runner {:id "runner"})
      (h/comp:kill)))

^{:refer std.scheduler/installed? :added "3.0"}
(fact "checks if program is installed"

  (test-scaffold
   (fn [runner _]
     (installed? runner :world)))
  => true)

^{:refer std.scheduler/create-program :added "3.0"}
(fact "creates a runner program"

  (create-program {:type :constant
                   :id :hello
                   :interval 10}))

^{:refer std.scheduler/uninstall :added "3.0"}
(fact "uninstalls a program")

^{:refer std.scheduler/install :added "3.0"}
(fact "installs a program" ^:hidden

  (test-scaffold
   (fn [runner _]
     (install runner {:id :hello
                      :type :constant
                      :interval 1000
                      :create-fn (fn [] {:runner runner
                                         :spawn-id spawn/*spawn-id*})
                      :main-fn (fn [args t]
                                 (let [{:keys [state]} spawn/*spawn*
                                       {:keys [spawn-id runner] :as m} @state]
                                   (print
                                    (assoc (dissoc m :runner)
                                           :job-id (:id spawn/*job*)
                                           :time t
                                           :jobs   (->> (get-in @(:runtime runner)
                                                                [:running :hello])
                                                        vals
                                                        (map  (comp count deref :board))
                                                        (apply +))
                                           :spawns (count (get-in @(:runtime runner)
                                                                  [:running :hello]))))
                                   (Thread/sleep (rand-int 10000))))})
     (installed? runner :hello)))
  => true)

^{:refer std.scheduler/spawn :added "3.0"}
(fact "spawns a runner that contains the program")

^{:refer std.scheduler/unspawn :added "3.0"}
(fact "unspawns the running program")

^{:refer std.scheduler/trigger :added "3.0"}
(fact "triggers the program manually, without spawning"

  (test-scaffold
   (fn [runner q]
     (trigger runner :world)
     (trigger runner :world)
     (count q)))
  => 2)

^{:refer std.scheduler/set-interval :added "3.0"}
(fact "manually overrides the interval for a spawn/program"
  ^:hidden
  
  (h/bench-ms
   (test-scaffold
    (fn [runner q]
      (set-interval runner :world 10)
      (spawn runner :world)
      (doall (for [i (range 2)]
               (cc/take q))))))
  => number?
  

  (h/bench-ms
   (test-scaffold
    (fn [runner q]
      (set-interval runner :world 100)
      (spawn runner :world)
      (doall (for [i (range 2)]
               (cc/take q))))))
  => #(<= 150 % 300))

^{:refer std.scheduler/get-props :added "3.0"}
(fact "gets the current props map for the runner" ^:hidden

  (test-scaffold
   (fn [runner q]
     (spawn runner :world {} "abc")
     (doall (for [i (range 2)]
              (cc/take q)))
     (Thread/sleep 10)
     (get-props runner :world)))
  => (contains-in {"abc" {:started integer?
                          :updated integer?
                          :terminate false,
                          :completed 2,
                          :submitted 3,
                          :errored 0,
                          :error nil,
                          :interval 100,
                          :succeeded 2}}))

(comment

  (runner {})
  (h/tracked [] :stop)
  (count (test-scaffold
          (fn [runner q]
            (spawn runner :world)
            (doall (for [i (range 10)]
                     (cc/take q))))
          40)))

(comment
  (use 'code.manage)
  (code.manage/import)
  (./create-tests)

  (interval-fn [runner m t])
  (stop-fn [runner m t])
  (stop-fn [runner m t]))
