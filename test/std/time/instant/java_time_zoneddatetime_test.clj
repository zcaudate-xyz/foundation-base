(ns std.time.instant.java-time-zoneddatetime-test
  (:require [std.lib.version :as version]
            [std.time :as t])
  (:use code.test))

^{:refer std.time.instant.java-time-zoneddatetime/from-map :added "3.0"}
(comment "creates a ZonedDateTime object from a map"

  (tz/from-map  (t/epoch {:timezone "GMT"}))
  => java.time.ZonedDateTime)

(version/init [[:java :not-older {:major 1 :minor 8}]
               [:clojure :not-older {:major 1 :minor 6}]]
              (:require [std.time.instant.java-time-zoneddatetime :as tz]))

(comment
  (code.manage/import))
