(ns std.lib.result-test
  (:require [std.lib.result :refer :all])
  (:use code.test))

^{:refer std.lib.result/result :added "3.0"}
(fact "creates a result used for printing"

  (result {:status :warn :data [1 2 3 4]})
  ;; #result{:status :warn, :data [1 2 3 4]}
  => std.lib.result.Result)

^{:refer std.lib.result/result? :added "3.0"}
(fact "checks if an object is a result"

  (-> (result {:status :warn :data [1 2 3 4]})
      result?)
  => true)

^{:refer std.lib.result/->result :added "3.0"}
(fact "converts data into a result"

  (->result :hello [1 2 3])
  ;;#result.return{:data [1 2 3], :key :hello}
  => std.lib.result.Result)

^{:refer std.lib.result/result-data :added "3.0"}
(fact "accesses the data held by the result"

  (result-data 1) => 1

  (result-data (result {:status :warn :data [1 2 3 4]}))
  => [1 2 3 4])
