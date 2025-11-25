(ns std.concurrent.pool-test
  (:use code.test)
  (:require [std.concurrent.pool :refer :all]
            [std.lib.component.track :as track ]
            [std.lib :as h]))

(declare -p-)

(fact:global
 {:component
  {|pool| {:create (pool:create {:size 3
                                 :max 8
                                 :keep-alive 10000
                                 :poll 20000
                                 :resource {:create (fn [] '<RESOURCE>)
                                            :initial 0.8
                                            :thread-local false}})
           :setup    h/start
           :teardown h/stop}}})

^{:refer std.concurrent.pool/resource-info :added "3.0"
  :use [|pool|]}
(fact "returns info about the pool resource"

  (->  (pool-resource "hello" |pool|)
       (resource-info :full))
  => (contains {:total number?,
                :busy 0.0,
                :count 0,
                :utilization 0.0,
                :duration 0}))

^{:refer std.concurrent.pool/resource-string :added "3.0"}
(fact "returns a string describing the resource")

^{:refer std.concurrent.pool/pool-resource :added "3.0"
  :use [|pool|]}
(fact "creates a pool resource"

  (pool-resource "hello"
                 {:resource {:create (fn [] '<RESOURCE>)}})
  => (contains {:id "hello",
                :object '<RESOURCE>
                :status :idle,
                :create-time number?
                :update-time number?
                :busy 0.0, :used 0}))

^{:refer std.concurrent.pool/pool:acquire :added "3.0"
  :use [|pool|]}
(fact "acquires a resource from the pool"

  (pool:acquire |pool|)
  => (contains [string? '<RESOURCE>]))

^{:refer std.concurrent.pool/dispose-fn :added "3.0"}
(fact "helper function for `dispose` and `cleanup`")

^{:refer std.concurrent.pool/pool:dispose :added "3.0"
  :use [|pool|]}
(fact "disposes an idle object"

  (pool:dispose |pool| (first (keys (pool:resources:idle |pool|)))))

^{:refer std.concurrent.pool/pool:dispose-over :added "3.0"
  :use [|pool|]}
(fact "disposes if idle and busy are over size limit"

  (let [[id _] (pool:acquire |pool|)]
    (pool:release |pool| id)
    (pool:dispose-over |pool| id))
  => string?)

^{:refer std.concurrent.pool/pool:release :added "3.0"
  :use [|pool|]}
(fact "releases a resource back to the pool"

  (let [[id _] (pool:acquire |pool|)]
    (pool:release |pool| id))
  => string?)

^{:refer std.concurrent.pool/pool:cleanup :added "3.0"
  :use [|pool|]}
(fact "runs cleanup on the pool" ^:hidden

  (def -ids- (->> (for [i (range 8)]
                    (pool:acquire |pool|))
                  (mapv first)))

  (count (pool:resources:busy |pool|))
  => 8

  (doseq [id -ids-]
    (pool:release |pool| id))

  (count (pool:resources:idle |pool|))
  => 8

  (pool:cleanup |pool|)

  (count (pool:resources:idle |pool|))
  => 3)

^{:refer std.concurrent.pool/pool-handler :added "3.0"}
(fact "creates a handler loop for cleanup"
  (pool-handler {:state (atom {:running true})
                 :cleanup (fn [])
                 :poll 100}))

^{:refer std.concurrent.pool/pool:started? :added "3.0"
  :use [|pool|]}
(fact "checks if pool has started"

  (pool:started? |pool|)
  => true)

^{:refer std.concurrent.pool/pool:stopped? :added "3.0"
  :use [|pool|]}
(fact "checks if pool has stopped"

  (pool:stopped? |pool|)
  => false)

^{:refer std.concurrent.pool/pool:start :added "3.0"
  :use [|pool|]}
(fact "starts the pool"

  (pool:start |pool|)
  => pool:started?)

^{:refer std.concurrent.pool/pool:stop :added "3.0"
  :use [|pool|]}
(fact "stops the pool"

  (pool:stop |pool|)
  => pool:stopped?)

^{:refer std.concurrent.pool/pool:kill :added "3.0"
  :use [|pool|]}
(fact "kills the pool"

  (pool:kill |pool|)
  => pool:stopped?)

^{:refer std.concurrent.pool/pool:info :added "3.0"
  :use [|pool|]}
(fact "returns information about the pool"

  (pool:info |pool|)
  => (contains-in {:running true,
                   :idle 2, :busy 0,
                   :resource {:count 0, :total number?
                              :busy 0.0, :utilization 0.0,
                              :duration 0}}))

^{:refer std.concurrent.pool/pool:props :added "3.0"
  :use [|pool|]}
(fact "gets props for the pool"

  (keys (pool:props |pool|))
  => (contains [:size :max :keep-alive :poll]))

^{:refer std.concurrent.pool/pool:health :added "3.0"
  :use [|pool|]}
(fact "returns health of the pool"

  (pool:health |pool|)
  => {:status :ok})

^{:refer std.concurrent.pool/pool:track-path :added "3.0"
  :use [|pool|]}
(fact "gets props for the pool"

  (pool:track-path |pool|)
  => [:raw :pool])

^{:refer std.concurrent.pool/pool? :added "3.0"
  :use [|pool|]}
(fact "checks that object is a pool"

  (pool? |pool|)
  => true)

^{:refer std.concurrent.pool/pool:create :added "3.0"}
(fact "creates an initial pool"

  (pool:create {:size 5
                :max 8
                :keep-alive 10000
                :poll 20000
                :resource {:create (fn [] '<RESOURCE>)
                           :initial 0.3
                           :thread-local true}}))

^{:refer std.concurrent.pool/pool :added "3.0"
  :teardown [(pool:stop -p-)]}
(fact "creates and starts the pool"

  (def -p- (pool {:size 2
                  :max 10
                  :keep-alive 10000
                  :poll 20000
                  :resource {:create (fn [] (rand))
                             :initial 0.3
                             :thread-local true}})))

^{:refer std.concurrent.pool/pool:resources:thread :added "3.0"
  :use [|pool|]}
(fact "returns acquired resources for a given thread" ^:hidden

  (-> (doto |pool|
        (pool:acquire)
        (pool:acquire))
      (pool:resources:thread)
      count)
  => 2)

^{:refer std.concurrent.pool/pool:resources:busy :added "3.0"
  :use [|pool|]}
(fact "returns all the busy resources" ^:hidden

  (pool:resources:busy |pool|)
  => {}

  (-> (doto |pool|
        (pool:acquire)
        (pool:acquire))
      (pool:resources:busy)
      count)
  => 2)

^{:refer std.concurrent.pool/pool:resources:idle :added "3.0"
  :use [|pool|]}
(fact "returns all the idle resources" ^:hidden

  (count (pool:resources:idle |pool|))
  => 2

  (-> (doto |pool|
        (pool:acquire)
        (pool:acquire))
      (pool:resources:idle))
  => {})

^{:refer std.concurrent.pool/pool:dispose:mark :added "3.0"
  :use [|pool|]}
(fact "marks the current resource for dispose" ^:hidden

  (dotimes [i 2]
    ((wrap-pool-resource
      (fn [pool]
        (pool:dispose:mark))
      |pool|)))

  (count (pool:resources:idle |pool|))
  => 0)

^{:refer std.concurrent.pool/pool:dispose:unmark :added "3.0"
  :use [|pool|]}
(fact "unmarks the current resource for dispose" ^:hidden

  (dotimes [i 2]
    ((wrap-pool-resource
      (fn [pool]
        (pool:dispose:mark)
        (pool:dispose:unmark))
      |pool|)))

  (count (pool:resources:idle |pool|))
  => 2)

^{:refer std.concurrent.pool/wrap-pool-resource :added "3.0"
  :use [|pool|]}
(fact "wraps a function to operate on a pool resource" ^:hidden

  ((wrap-pool-resource (fn [obj]
                         (str obj))
                       |pool|))
  => "<RESOURCE>")

^{:refer std.concurrent.pool/pool:with-resource :added "3.0"
  :style/indent 1
  :use [|pool|]
  :teardown [(track/tracked:list [] {:namespace (.getName *ns*)} :stop)]}
(fact "takes an object from the pool, performs operation then returns it" ^:hidden

  (pool:with-resource [obj |pool|]
                      (str obj))
  => "<RESOURCE>")

(comment
  (./import)
  (track/tracked:all)
  (track/tracked:count)
  (./create-tests)

  (./create-tests)
  (-p-)
  (h/info -p-)

  (h/hash-id -p-)
  (def -p- (pool {:size 2
                  :max 10
                  :keep-alive 10000
                  :poll 20000
                  :resource {:create (fn [] (rand))
                             ;;:initial 0.3
                             :thread-local true}}))
  (thread-objects -p-)
  (dotimes [i 20]
    (future (acquire -p-)))
  (:lookup -p-)
  ["qxwmuvy8gsbq" 0.799788725357809]
  [0.3006456020229241]
  (release -p- (first (acquire -p-)))
  (:stats @(:state -p-))
  (doseq [id (take 2 (keys (pool:resources:busy -p-)))]
    (release -p- id))
  (./arrange))
