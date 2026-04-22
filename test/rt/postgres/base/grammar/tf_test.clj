(ns rt.postgres.base.grammar.tf-test
  (:require [rt.postgres.base.grammar.tf :refer :all])
  (:use code.test))

^{:refer rt.postgres.base.grammar.tf/pg-js-idx :added "4.0"}
(fact "ignores single letter prefix"

  (pg-js-idx 'i-hello)
  => "hello")

^{:refer rt.postgres.base.grammar.tf/pg-tf-js :added "4.0"}
(fact "converts a map to js object"

  (pg-tf-js '(js {:a 1 :b 2}))
  => '(jsonb-build-object "a" 1 "b" 2)

  (pg-tf-js '(js {:a [1 2 3]}))
  => '(jsonb-build-object "a" (jsonb-build-array 1 2 3)))


^{:refer rt.postgres.base.grammar.tf/pg-tf-for :added "4.0"}
(fact "creates for loop"

  (pg-tf-for '(for [i < 0] (:++ i)))
  => '[:FOR i < 0
       :LOOP \\ (\| (do (:++ i))) \\ :END-LOOP \;])

^{:refer rt.postgres.base.grammar.tf/pg-tf-foreach :added "4.0"}
(fact "creates foreach loop"

  (pg-tf-foreach '(foreach [i :in (array 1 2 3 4 5)]
                           (:++ i)))
  => '[:FOREACH i :in (array 1 2 3 4 5)
       :LOOP \\ (\| (do (:++ i))) \\ :END-LOOP \;])

^{:refer rt.postgres.base.grammar.tf/pg-tf-loop :added "4.0"}
(fact "creates loop"

  (pg-tf-loop '(loop (:++ i)))
  => '[:LOOP \\ (\| (do (:++ i))) \\ :END-LOOP \;])

^{:refer rt.postgres.base.grammar.tf/pg-tf-throw :added "4.0"}
(fact "creates throw transform"

  (pg-tf-throw '(throw {}))
  => '[:raise-exception :using (quote [[:detail := (:text (% {}))] [:message := "nil"]])])

^{:refer rt.postgres.base.grammar.tf/pg-tf-error :added "4.0"}
(fact "creates error transform"

  (pg-tf-error '(error {}))
  => '[:raise-exception :using (quote [[:detail := (:text (% {:status "error"}))] [:message := "nil"]])])

^{:refer rt.postgres.base.grammar.tf/pg-tf-assert :added "4.0"}
(fact "creates assert transform"

  (pg-tf-assert '(assert (= 1 1)
                         [:tag {}]))
  => '(if [:NOT (quote ((= 1 1)))] [:raise-exception :using (quote [[:detail := (:text (% {:status "error", :tag :tag}))]
                                                                    [:message := "tag"]])]))

