(ns std.lib.system.array-test
  (:use code.test)
  (:require [std.lib.system.array :refer :all]
            [std.lib.component.track :as track]
            [std.lib.component :as common]
            [std.lib.component-test :refer [map->Database]]
            [std.lib :as h]))

^{:refer std.lib.system.array/info-array :added "3.0"}
(fact "returns the info of elements within the array"

  (str (info-array [(map->Database nil) (map->Database nil)]))
  => "[#db{} #db{}]")

^{:refer std.lib.system.array/health-array :added "3.0"}
(fact "returns the health of the array"

  (health-array [(map->Database nil) (map->Database nil)])
  => {:status :ok})

^{:refer std.lib.system.array/start-array :added "3.0"
  :teardown [(track/tracked:last [:test :db] :stop 2)]}
(fact "starts an array of components"

  (start-array [(map->Database nil) (map->Database nil)])
  => any?)

^{:refer std.lib.system.array/stop-array :added "3.0"}
(fact "stops an array of components" ^:hidden

  (-> (start-array [(map->Database nil) (map->Database nil)])
      (stop-array))
  => any?)

^{:refer std.lib.system.array/array :added "3.0"}
(fact "constructs a system array"

  (array {:constructor map->Database} [{:id 1} {:id 2}])
  => any?)

^{:refer std.lib.system.array/array? :added "3.0"}
(fact "checks if object is a system array"

  (array? (array {:constructor map->Database} [{}]))
  => true)
