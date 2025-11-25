(ns code.test.compile.types-test
  (:use code.test)
  (:require [code.test.compile.types :refer :all]
            [code.test.base.runtime :as rt]))

(fact "fact creation and properties"
  (let [fact-obj (map->Fact {:id 'my-fact, :desc "my description"})]
    (fact? fact-obj) => true
    (str fact-obj) => #"#fact \{:desc \"my description\", :id my-fact\}"))

(fact "fact-display-info function"
  (fact-display-info {:a 1, :b 2, :path "/path"})
  => {:a 1, :b 2})

(fact "fact-display function"
  (fact-display {:a 1, :b 2, :path "/path"})
  => "{:a 1, :b 2}")

(fact "bench-single function"
  (let [result (bench-single {:times 10} (fn [] (Thread/sleep 1)))]
    (> (:mean result) 1000000) => true
    (:times result) => 10))

(fact "fact-invoke for core facts"
  (let [fact-obj (map->Fact
                  {:type :core
                   :wrap {:bindings (fn [t] (fn [] (t)))
                          :ceremony (fn [t] (fn [] (t)))
                          :check (fn [t] (fn [] (t)))
                          :replace (fn [t] (fn [] (t)))}
                   :function {:thunk (fn [] "hello")}})]
    (fact-invoke fact-obj) => "hello"))

^{:refer code.test.compile.types/map->Fact :added "3.0" :adopt true}
(fact "creates a fact object")

^{:refer code.test.compile.types/fact-invoke :added "3.0"
  :guard true}
(fact "invokes a fact object")
