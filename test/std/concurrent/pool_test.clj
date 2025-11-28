(ns std.concurrent.pool-test
  (:use code.test)
  (:require [std.concurrent.pool :refer :all]
            [std.lib.component.track :as track ]
            [std.lib :as h]))

(defn create-pool
  []
  (pool:create {:size 3
                :max 8
                :keep-alive 10000
                :poll 20000
                :resource {:create (fn [] '<RESOURCE>)
                           :initial 0.8
                           :thread-local false}}))

^{:refer std.concurrent.pool/resource-info :added "3.0"}
(fact "returns info about the pool resource"
  ^:hidden
  
  (h/with:component [|pool| (create-pool)]
    (->  (pool-resource "hello" |pool|)
         (resource-info :full)))
  => (contains {:total number?,
                :busy 0.0,
                :count 0,
                :utilization 0.0,
                :duration 0}))

^{:refer std.concurrent.pool/resource-string :added "3.0"}
(fact "returns a string describing the resource")

^{:refer std.concurrent.pool/pool-resource :added "3.0"}
(fact "creates a pool resource"
  ^:hidden
  
  (pool-resource "hello"
                 {:resource {:create (fn [] '<RESOURCE>)}})
  => (contains {:id "hello",
                :object '<RESOURCE>
                :status :idle,
                :create-time number?
                :update-time number?
                :busy 0.0, :used 0}))

^{:refer std.concurrent.pool/pool:acquire :added "3.0"}
(fact "acquires a resource from the pool"
  ^:hidden
  
  (h/with:component [|pool| (create-pool)]
    (pool:acquire |pool|))
  => (contains [string? '<RESOURCE>]))

^{:refer std.concurrent.pool/dispose-fn :added "3.0"}
(fact "helper function for `dispose` and `cleanup`")

^{:refer std.concurrent.pool/pool:dispose :added "3.0"}
(fact "disposes an idle object"
  ^:hidden

  (h/with:component [|pool| (create-pool)]
    (pool:dispose |pool| (first (keys (pool:resources:idle |pool|))))))

^{:refer std.concurrent.pool/pool:dispose-over :added "3.0"}
(fact "disposes if idle and busy are over size limit"
  ^:hidden
  
  (h/with:component [|pool| (create-pool)]
    (let [[id _] (pool:acquire |pool|)]
      (pool:release |pool| id)
      (pool:dispose-over |pool| id)))
  => string?)

^{:refer std.concurrent.pool/pool:release :added "3.0"}
(fact "releases a resource back to the pool"
  ^:hidden

  (h/with:component [|pool| (create-pool)]
    (let [[id _] (pool:acquire |pool|)]
      (pool:release |pool| id)))
  => string?)

^{:refer std.concurrent.pool/pool:cleanup :added "3.0"}
(fact "runs cleanup on the pool" ^:hidden
  ^:hidden

  (h/with:component [|pool| (create-pool)]
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
    => 3))

^{:refer std.concurrent.pool/pool-handler :added "3.0"}
(fact "creates a handler loop for cleanup"
  ^:hidden
  
  (pool-handler {:state (atom {:running true})
                 :cleanup (fn [])
                 :poll 100}))

^{:refer std.concurrent.pool/pool:started? :added "3.0"}
(fact "checks if pool has started"
  ^:hidden

  (h/with:component [|pool| (create-pool)]
    (pool:started? |pool|))
  => true)

^{:refer std.concurrent.pool/pool:stopped? :added "3.0"}
(fact "checks if pool has stopped"
  ^:hidden
  
  (h/with:component [|pool| (create-pool)]
    (pool:stopped? |pool|))
  => false)

^{:refer std.concurrent.pool/pool:start :added "3.0"}
(fact "starts the pool"
  ^:hidden
  
  (h/with:component [|pool| (create-pool)]
    (pool:start |pool|)
    => pool:started?))

^{:refer std.concurrent.pool/pool:stop :added "3.0"}
(fact "stops the pool"
  ^:hidden
  
  (h/with:component [|pool| (create-pool)]
    (pool:stop |pool|)
    => pool:stopped?))

^{:refer std.concurrent.pool/pool:kill :added "3.0"}
(fact "kills the pool"
  ^:hidden
  
  (h/with:component [|pool| (create-pool)]
    (pool:kill |pool|)
    => pool:stopped?))

^{:refer std.concurrent.pool/pool:info :added "3.0"}
(fact "returns information about the pool"
  ^:hidden
  
  (h/with:component [|pool| (create-pool)]
    (pool:info |pool|))
  => (contains-in {:running true,
                   :idle 2, :busy 0,
                   :resource {:count 0, :total number?
                              :busy 0.0, :utilization 0.0,
                              :duration 0}}))

^{:refer std.concurrent.pool/pool:props :added "3.0"}
(fact "gets props for the pool"
  ^:hidden
  
  (h/with:component [|pool| (create-pool)]
    (keys (pool:props |pool|)))
  => (contains [:size :max :keep-alive :poll]))

^{:refer std.concurrent.pool/pool:health :added "3.0"}
(fact "returns health of the pool"
  ^:hidden
  
  (h/with:component [|pool| (create-pool)]
    (pool:health |pool|))
  => {:status :ok})

^{:refer std.concurrent.pool/pool:track-path :added "3.0"}
(fact "gets props for the pool"
  ^:hidden
  
  (h/with:component [|pool| (create-pool)]
    (pool:track-path |pool|))
  => [:raw :pool])

^{:refer std.concurrent.pool/pool? :added "3.0"}
(fact "checks that object is a pool"
  ^:hidden
  
  (h/with:component [|pool| (create-pool)]
    (pool? |pool|))
  => true)

^{:refer std.concurrent.pool/pool:create :added "3.0"}
(fact "creates an initial pool"
  ^:hidden
  
  (pool:create {:size 5
                :max 8
                :keep-alive 10000
                :poll 20000
                :resource {:create (fn [] '<RESOURCE>)
                           :initial 0.3
                           :thread-local true}}))

^{:refer std.concurrent.pool/pool :added "3.0"}
(fact "creates and starts the pool"
  ^:hidden
  
  (h/with:component [p (pool {:size 2
                              :max 10
                              :keep-alive 10000
                              :poll 20000
                              :resource {:create (fn [] (rand))
                                         :initial 0.3
                                         :thread-local true}})]
    p => pool?))

^{:refer std.concurrent.pool/pool:resources:thread :added "3.0"}
(fact "returns acquired resources for a given thread"
  ^:hidden

  (h/with:component [|pool| (create-pool)]
    (-> (doto |pool|
          (pool:acquire)
          (pool:acquire))
        (pool:resources:thread)
        count))
  => 2)

^{:refer std.concurrent.pool/pool:resources:busy :added "3.0"}
(fact "returns all the busy resources" ^:hidden

  (h/with:component [|pool| (create-pool)]
    (pool:resources:busy |pool|))
  => {}

  (h/with:component [|pool| (create-pool)]
    (-> (doto |pool|
          (pool:acquire)
          (pool:acquire))
        (pool:resources:busy)
        count))
  => 2)

^{:refer std.concurrent.pool/pool:resources:idle :added "3.0"}
(fact "returns all the idle resources" ^:hidden

  (h/with:component [|pool| (create-pool)]
    (count (pool:resources:idle |pool|)))
  => 2

  (h/with:component [|pool| (create-pool)]
    (-> (doto |pool|
          (pool:acquire)
          (pool:acquire))
        (pool:resources:idle)))
  => {})

^{:refer std.concurrent.pool/pool:dispose:mark :added "3.0"}
(fact "marks the current resource for dispose" ^:hidden
  
  (h/with:component [|pool| (create-pool)]
    (dotimes [i 2]
      ((wrap-pool-resource
        (fn [pool]
          (pool:dispose:mark))
        |pool|)))

    (count (pool:resources:idle |pool|))
    => 0))

^{:refer std.concurrent.pool/pool:dispose:unmark :added "3.0"}
(fact "unmarks the current resource for dispose" ^:hidden

  (h/with:component [|pool| (create-pool)]
    (dotimes [i 2]
      ((wrap-pool-resource
        (fn [pool]
          (pool:dispose:mark)
          (pool:dispose:unmark))
        |pool|)))

    (count (pool:resources:idle |pool|))
    => 2))

^{:refer std.concurrent.pool/wrap-pool-resource :added "3.0"}
(fact "wraps a function to operate on a pool resource" ^:hidden

  (h/with:component [|pool| (create-pool)]
    ((wrap-pool-resource (fn [obj]
                           (str obj))
                         |pool|)))
  => "<RESOURCE>")

^{:refer std.concurrent.pool/pool:with-resource :added "3.0"
  :style/indent 1
  :teardown [(track/tracked:list [] {:namespace (.getName *ns*)} :stop)]}
(fact "takes an object from the pool, performs operation then returns it" ^:hidden

  (h/with:component [|pool| (create-pool)]
    (pool:with-resource [obj |pool|]
      (str obj))
    => "<RESOURCE>"))

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
