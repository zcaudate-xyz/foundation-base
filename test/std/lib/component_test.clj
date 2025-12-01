(ns std.lib.component-test
  (:use code.test)
  (:require [std.lib.component :refer :all]
            [std.lib.component.track :as track]
            [std.protocol.component :as protocol.component]
            [std.protocol.track :as protocol.track]
            [std.lib :as h]))

(defrecord Database []
  protocol.track/ITrack
  (-track-path [db] [:test :db])

  protocol.component/IComponent
  (-start [db]
    (assoc db :status "started"))
  (-stop [db]
    (dissoc db :status))

  protocol.component/IComponentQuery
  (-info [db level]
    (into {:info true} db))
  (-health  [db]
    {:status :ok})

  protocol.component/IComponentProps
  (-props [db]
    {:interval {:get (fn [] 10)}}))

(defmethod print-method Database
  [v ^java.io.Writer w]
  (.write w (str "#db" (into {} v))))

^{:refer std.lib.component/impl:component :added "3.0"}
(fact "rewrite function compatible with std.lib.impl"
  (impl:component {:name ':-foo})
  => 'std.lib.component/foo)

^{:refer std.lib.component/component? :added "3.0"}
(fact "checks if an instance extends IComponent"

  (component? (Database.))
  => true)

^{:refer std.lib.component/primitive? :added "3.0"}
(fact "checks if a component is a primitive type"

  (primitive? 1) => true

  (primitive? {}) => false)

^{:refer std.lib.component/started? :added "3.0"
  :teardown [(track/tracked:last [:test :db] stop)]}
(fact "checks if a component has been started"

  (started? 1)
  => true

  (started? (start {}))
  => true

  (started? (Database.))
  => true)

^{:refer std.lib.component/stopped? :added "3.0"
  :teardown [(track/tracked:last [:test :db] stop)]}
(fact "checks if a component has been stopped"

  (stopped? 1)
  => false

  (stopped? {})
  => false

  (stopped? (start {}))
  => false

  (stopped? (Database.))
  => false)

^{:refer std.lib.component/perform-hooks :added "3.0"}
(fact "perform hooks before main function"

  (perform-hooks (Database.)
                 {:init (fn [x] 1)}
                 [:init])
  => 1)

^{:refer std.lib.component/get-options :added "3.0"}
(fact "helper function for start and stop"

  (get-options (Database.)
               {:init (fn [x] 1)})
  => (contains {:init fn?}))

^{:refer std.lib.component/stop-raw :added "3.0"}
(fact "switch between stop and kill methods"
  (stop-raw (start (Database.))) => {})

^{:refer std.lib.component/start :added "3.0"
  :teardown [(track/tracked:last [:test :db] stop)]}
(fact "starts a component/array/system"

  (start (Database.))
  => {:status "started"})

^{:refer std.lib.component/stop :added "3.0"}
(fact "stops a component/array/system"

  (stop (start (Database.))) => {})

^{:refer std.lib.component/kill :added "3.0"}
(fact "kills a systems, or if method is not defined stops it"

  (kill (start (Database.))) => {})

^{:refer std.lib.component/info :added "3.0"}
(fact "returns info regarding the component"

  (info (Database.))
  => {:info true})

^{:refer std.lib.component/health :added "3.0"}
(fact "returns the health of the component"

  (health (Database.))
  => {:status :ok})

^{:refer std.lib.component/remote? :added "3.0"}
(fact "returns whether the component connects remotely"

  (remote? (Database.))
  => false)

^{:refer std.lib.component/all-props :added "3.0"}
(fact "lists all props in the component"

  (all-props (Database.))
  => [:interval])

^{:refer std.lib.component/get-prop :added "3.0"}
(fact "gets a prop in the component"

  (get-prop (Database.) :interval)
  => 10)

^{:refer std.lib.component/set-prop :added "3.0"}
(fact "sets a prop in the component"

  (set-prop (Database.) :interval 3)
  => (throws))

^{:refer std.lib.component/with :added "3.0"
  :style/indent 1}
(fact "do tests with an active component"

  (with [db (Database.)]
        (started? db))
  => true)

^{:refer std.lib.component/wrap-start :added "4.0"}
(fact "wraps the start function with more steps"
  ((wrap-start (fn [rt] (assoc rt :started true))
               [])
   {})
  => {:started true})

^{:refer std.lib.component/wrap-stop :added "4.0"}
(fact "wraps the stop function with more steps"
  ((wrap-stop (fn [rt] (assoc rt :stopped true))
              [])
   {})
  => {:stopped true})


(comment
  (./create-tests)
  (./import)

  (track/tracked [:test :db] stop))


^{:refer std.lib.component/with-lifecycle :added "4.1"}
(fact "helper for lifecycle management"
  (def out (atom []))
  (with-lifecycle [a {:start (do (swap! out conj :start) :resource)
                      :stop  (fn [x] (swap! out conj [:stop x]))}]
    (swap! out conj [:body a])
    :done)
  => :done

  @out
  => [:start [:body :resource] [:stop :resource]])