(ns std.lib.system.type-test
  (:use code.test)
  (:require [std.lib.system.type :refer :all]
            [std.lib.system.array :as array]
            [std.lib.system.topology :as topology]
            [std.lib.component.track :as track]
            [std.protocol.component :as protocol.component]
            [std.lib.component-test :refer [map->Database]]
            [std.lib :as h]))

(defrecord Filesystem []
  protocol.component/IComponent
  (-start [sys]
    (assoc sys :status "started"))
  (-stop [sys]
    (dissoc sys :status))
  protocol.component/IComponentQuery
  (-info [sys level]
    {:hello "world"})
  (-health  [db]
    {:status :ok}))

(defmethod print-method Filesystem
  [v ^java.io.Writer w]
  (.write w (str "#fs" (into {} v))))

(defrecord Catalog []
  protocol.component/IComponent
  (-start [store]
    (assoc store :status "started"))
  (-stop [store]
    (dissoc store :status))
  protocol.component/IComponentQuery
  (-health  [db]
    {:status :ok}))

(defmethod print-method Catalog
  [v ^java.io.Writer w]
  (.write w (str "#cat" (into {} v))))

(defrecord Camera []
  Object
  (toString [cam]
    (str "#cam" (into {} cam)))

  protocol.component/IComponent
  (-start [cam]
    (assoc cam :status "started"))
  (-stop [cam]
    (dissoc cam :status))
  protocol.component/IComponentQuery
  (-health  [db]
    {:status :ok}))

(defmethod print-method Camera
  [v ^java.io.Writer w]
  (.write w (str v)))

^{:refer std.lib.system.type/system.additional-features :added "3.0" :adopt true
  :teardown [(track/tracked:last [:system] :stop)]}
(fact "creates a system of components" ^:hidden

  (def topology {:database   [{:constructor map->Database}]
                 :cameras    [[{:constructor map->Camera
                                :setup #(array/->ComponentArray (map (fn [x] (assoc x :a 1)) %))}]
                              :database]})

  (-> (system topology
              {:database {}
               :watchmen [{:id 1} {:id 2}]
               :cameras  ^{:hello "world"} [{:id 1} {:id 2 :hello "again"}]})
      (h/start)
      (update :cameras seq))
  => (contains-in {:database {:status "started"}
                   :cameras
                   [{:hello "world", :id 1,  :a 1 :status "started"}
                    {:hello "again", :id 2,  :a 1 :status "started"}]}))

^{:refer std.lib.system.type/system.expose-feature :added "3.0" :adopt true
  :teardown [(track/tracked:last [:system] :stop)]}
(fact "exposes sub-components within a system"

  (def topology {:database [map->Database]
                 :status   [{:expose :status} :database]})

  (-> (system topology
              {:database {:status "stopped"}})
      (h/start))
  => (contains {:database {:status "started"}
                :status   "started"}))

^{:refer std.lib.system.type/system.import-feature :added "3.0" :adopt true
  :teardown [(track/tracked:last [:system] :stop)]}
(fact "exposes sub-components within a system"

  (topology/long-form {:database [map->Database [:schema {:nocheck true}]]})
  => (contains-in {:database {:compile :single,
                              :type :build,
                              :constructor fn?
                              :import {:schema {:type :single, :nocheck true}},
                              :dependencies ()}})

  (def topology {:database [map->Database [:schema {:nocheck true}]]})

  (-> (system topology
              {:database {:status "stopped"}})
      (assoc :schema "Hello")
      (h/start)
      :database)
  => (contains {:status "started"
                :schema "Hello"}))

^{:refer std.lib.system.type/info-system :added "3.0"
  :teardown [(track/tracked:last [:system] :stop)]}
(fact "gets the info for the system" ^:hidden

  (def topology {:database   [{:constructor map->Database}]

                 :cameras    [[{:constructor map->Camera
                                :setup #(array/->ComponentArray (map (fn [x] (assoc x :a 1)) %))}]
                              :database]})

  (-> (system topology
              {:database {}
               :watchmen [{:id 1} {:id 2}]
               :cameras  ^{:hello "world"} [{:id 1} {:id 2 :hello "again"}]})
      (h/start)
      (info-system))
  => any?)

^{:refer std.lib.system.type/health-system :added "3.0"
  :teardown [(track/tracked:last [:system] :stop)]}
(fact "gets the health for the system" ^:hidden

  (def topology {:database   [{:constructor map->Database}]

                 :cameras    [[{:constructor map->Camera
                                :setup #(array/->ComponentArray (map (fn [x] (assoc x :a 1)) %))}]
                              :database]})
  (-> (system topology
              {:database {}
               :watchmen [{:id 1} {:id 2}]
               :cameras  ^{:hello "world"} [{:id 1} {:id 2 :hello "again"}]})
      (h/start)
      (health-system))
  => {:status :ok})

^{:refer std.lib.system.type/remote?-system :added "3.0"
  :teardown [(track/tracked:last [:system] :stop)]}
(fact "gets the remote status for the system" ^:hidden

  (def topology {:database   [{:constructor map->Database}]

                 :cameras    [[{:constructor map->Camera
                                :setup #(array/->ComponentArray (map (fn [x] (assoc x :a 1)) %))}]
                              :database]})
  (-> (system topology
              {:database {}
               :watchmen [{:id 1} {:id 2}]
               :cameras  ^{:hello "world"} [{:id 1} {:id 2 :hello "again"}]})
      (h/start)
      (remote?-system))
  => false)

^{:refer std.lib.system.type/system-string :added "3.0"}
(fact "get the string for the system"
  (resolve 'system-string) => var?)

^{:refer std.lib.system.type/system :added "3.0"
  :teardown [(track/tracked:last [:system] :stop)]}
(fact "creates a system of components"

  ;; The topology specifies how the system is linked
  (def topo {:db        [map->Database]
             :files     [[map->Filesystem]]
             :catalogs  [[map->Catalog] [:files {:type :element :as :fs}] :db]}) ^:hidden

  ;; The configuration customises the system
  (def cfg  {:db     {:type :basic
                      :host "localhost"
                      :port 8080}
             :files [{:path "/app/local/1"}
                     {:path "/app/local/2"}]
             :catalogs [{:id 1}
                        {:id 2}]})

  ;; `system` will build it and calling `start` initiates it
  (def sys (-> (system topo cfg) h/start))

  ;; Check that the `:db` entry has started
  (:db sys)
  => (just {:status "started",
            :type :basic,
            :port 8080,
            :host "localhost"})

  ;; Check the first `:files` entry has started
  (-> sys :files first)
  => (just {:status "started",
            :path "/app/local/1"})

  ;; Check that the second `:store` entry has started
  (->> sys :catalogs second)
  => (contains-in {:id 2
                   :status "started"
                   :db {:status "started",
                        :type :basic,
                        :port 8080,
                        :host "localhost"}
                   :fs {:path "/app/local/2", :status "started"}}))

^{:refer std.lib.system.type/system-import :added "3.0"}
(fact "imports a component into the system"
  (resolve 'system-import) => var?)

^{:refer std.lib.system.type/system-expose :added "3.0"}
(fact "exposes a component into the system")

^{:refer std.lib.system.type/start-system :added "3.0"}
(fact "starts a system" ^:hidden
  (->> (system {:models [[identity] [:files {:type :element :as :fs}]]
                :files  [[identity]]}
               {:models [{:m 1} {:m 2}]
                :files  [{:id 1} {:id 2}]})
       start-system
       (into {})
       (h/map-vals seq))
  => (contains-in {:models [{:m 1,
                             :fs {:id 1}}
                            {:m 2,
                             :fs {:id 2}}],
                   :files [{:id 1} {:id 2}]}))

^{:refer std.lib.system.type/system-deport :added "3.0"}
(fact "deports a component from the system")

^{:refer std.lib.system.type/stop-system :added "3.0"}
(fact "stops a system" ^:hidden

  (->> (system {:model   [identity]
                :ids     [[identity]]
                :traps   [[identity] [:model {:as :raw}] [:ids {:type :element :as :id}]]
                :entry   [identity :model :ids]
                :nums    [[{:expose :id}] :traps]
                :model-tag  [{:expose :tag
                              :setup identity}  :model]}
               {:model {:tag :barbie}
                :ids   [1 2 3 4 5]
                :traps [{} {} {} {} {}]
                :entry {}})
       (start-system)
       (stop-system)
       (h/map-vals (fn [v] (if (array/array? v)
                             (seq v)
                             v))))
  =>  {:model {:tag :barbie}, :ids [1 2 3 4 5], :traps [{} {} {} {} {}], :entry {}})

^{:refer std.lib.system.type/kill-system :added "3.0"}
(fact "kills the system (stopping immediately)")

(comment
  (./arrange)
  (./import))
