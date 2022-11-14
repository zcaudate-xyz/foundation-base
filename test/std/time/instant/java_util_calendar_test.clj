(ns std.time.instant.java-util-calendar-test
  (:use code.test)
  (:require [std.time.instant.java-util-calendar :refer :all :as cal]
            [std.time :as t]))

^{:refer std.time.instant.java-util-calendar/from-map :added "3.0"}
(fact "creates a Calendar object from a map"

  (cal/from-map (t/epoch {:timezone "GMT"}))
  => java.util.Calendar)

^{:refer std.time.instant.java-util-calendar/with-timezone :added "3.0"}
(fact "adds the timezone to a Calendar object"

  (-> (cal/from-map (t/epoch {:timezone "EST"}))
      (cal/with-timezone "GMT"))
  => java.util.Calendar ;;#inst "1970-01-01T00:00:00.000+00:00"
  )

(comment
  (code.manage/import))