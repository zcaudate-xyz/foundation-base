(ns std.config.global-test
  (:use code.test)
  (:require [std.config.global :refer :all]))

^{:refer std.config.global/global? :added "3.0"}
(fact "checks if object is of type global"

  (global? (map->Global {}))
  => true)

^{:refer std.config.global/global-raw :added "3.0"}
(fact "constructs a global object"

  (global-raw {[:a] 1
               [:b] 2}
              identity)
  => global?)

^{:refer std.config.global/global-env-raw :added "3.0"}
(fact "returns the global object for system env"

  (:home (global-env-raw))
  => string?)

^{:refer std.config.global/global-properties-raw :added "3.0"}
(fact "returns the global object for system properties"

  (:java (global-properties-raw))
  ;; {:compile {:path "./target/classes"}, :debug "false"}
  => map?)

^{:refer std.config.global/global-project-raw :added "3.0"}
(fact "returns the global object for thecurrent project"

  (:group (global-project-raw))
  => "zcaudate")

^{:refer std.config.global/global-home-raw :added "3.0"}
(fact "returns the global object for all global types"

  (global-home-raw)
  => anything)

^{:refer std.config.global/global-session-raw :added "3.0"}
(fact "returns the global object within the current session"

  (global-session-raw)
  => {})

^{:refer std.config.global/global-all-raw :added "3.0"}
(fact "returns the global object for all global types"

  (:group (global-all-raw))
  => "zcaudate")

^{:refer std.config.global/global :added "3.0"}
(fact "returns the entire global map"

  (global :all))

(comment
  (global :all))
