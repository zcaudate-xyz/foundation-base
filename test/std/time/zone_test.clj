(ns std.time.zone-test
  (:require [std.time.common :as common]
            [std.time.zone :as zone])
  (:use code.test)
  (:import [java.util Date TimeZone Calendar]))

^{:refer std.time.zone/pad-zeros :added "3.0"}
(fact "if number less than 10, make double digit"

  (zone/pad-zeros "5") => "05")

^{:refer std.time.zone/generate-offsets :added "3.0"}
(comment "make offsets in milliseconds"
  (zone/generate-offsets)
  => (["00:00" 0] ["00:15" 900000] .... ["11:30" 41400000] ["11:45" 42300000]))

(comment
  (code.manage/import))

(comment
  ;; All the uncommon ones
  (->> (filter (fn [[_ d]]
                 (not (.startsWith d "Etc"))) (seq zone/by-offset))
       (map second)
       (map (juxt identity #(common/calendar (Date. 0) (TimeZone/getTimeZone %))))))