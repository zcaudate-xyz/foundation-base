(ns std.log.match-test
  (:require [std.log.match :refer :all])
  (:use code.test))

^{:refer std.log.match/filter-base :added "3.0"}
(fact "matches based on input and filter"

  (filter-base  "hello"  #"h.*")
  => true

  (filter-base  #{"hello"}  #"h.*")
  => true

  (filter-base  #{"world"}  #"h.*")
  => false

  (filter-base  #{:hello}  #"h.*")
  => true

  (filter-base  #{:hello}  keyword?)
  => true

  (filter-base  :hello  "hello")
  => true

  (filter-base  :hello  "world")
  => false)

^{:refer std.log.match/filter-include :added "3.0"}
(fact "positive if filter is empty or one of the matches hit"

  (filter-include :hello nil)
  => true

  (filter-include :hello ["hello" "world"])
  => true

  (filter-include :hello [#"world"])
  => false

  (filter-include #{:hello} [#"world"])
  => false

  (filter-include #{:world} [#"world"])
  => true

  (filter-include #{:hello :world} [#"world"])
  => true)

^{:refer std.log.match/filter-exclude :added "3.0"}
(fact "negative if one of the matches hit"

  (filter-exclude :hello nil)
  => true

  (filter-exclude :hello ["hello" "world"])
  => false

  (filter-exclude :hello [#"world"])
  => true

  (filter-exclude #{:hello} [#"world"])
  => true

  (filter-exclude #{:world} [#"world"])
  => false

  (filter-exclude #{:hello :world} [#"world"])
  => false)

^{:refer std.log.match/filter-value :added "3.0"}
(fact "filters based on exclude and include filters"

  (filter-value  "hello"  nil)
  => true

  (filter-value  "hello"  {:include [#"h"]})
  => true

  (filter-value  "hello"  {:exclude [#"h"]})
  => false

  (filter-value  "hello"  {:include ["hello"]
                           :exclude [#"h"]})
  => true)

^{:refer std.log.match/match-filter :added "3.0"}
(fact "helper for `match`"

  (match-filter {:log/tags   {:include ["hello"]}}
                {:log/tags #{:hello :world}}))

^{:refer std.log.match/match :added "3.0"}
(fact "matches the logger with event information"

  (match {:level     :debug}
    {:log/level :debug})
  => true

  (match {:level     :info}
    {:log/level :debug})
  => false

  (match {:level     :debug}
    {:log/level :info})
  => true

  (match {:level     :debug
          :function  {:include [#"h.*"]}}
    {:log/level :debug
     :log/function  "hello"})
  => true

  (match {:level     :debug
          :function  {:include [#"h.*"]}
          :filter {:log/tags   {:include ["hello"]}}}
    {:log/level :debug
     :log/function  "hello"
     :log/tags #{:hello :world}})
  => true

  (match {:level     :debug
          :filter    {:log/tags {:include []}}}
    {:log/level :debug
     :log/tags #{:hello :world}})
  => false)

(comment
  (./import)

  (re-find #"h"  "hoeueo")

  (filter-base  "hoeueo"  #"h.*")

  (filter-include  "hoeueo"  [#"h"])

  (filter-value  "hoeueo"  {:include [#"h"]})

  (match {:level     :debug
          :name      {:include []}
          :tags      {:include []
                      :exclude []}
          :namespace {:include []}
          :function  {:include []}}

    {:log/level :debug}))
