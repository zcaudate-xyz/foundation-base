tahto/model/spec_postgres/tf_test.clj:1:(ns tahto.model.spec-postgres.tf-test
tahto/model/spec_postgres/tf_test.clj:2:  (:require [tahto.model.spec-postgres.tf :refer :all])
  (:use code.test))

tahto/model/spec_postgres/tf_test.clj:5:^{:refer tahto.model.spec-postgres.tf/pg-js-idx :added "4.0"}
(fact "ignores single letter prefix"

  (pg-js-idx 'i-hello)
  => "hello")

tahto/model/spec_postgres/tf_test.clj:11:^{:refer tahto.model.spec-postgres.tf/pg-tf-js :added "4.0"}
(fact "converts a map to js object"

  (pg-tf-js '(js {:a 1 :b 2}))
  => '(jsonb-build-object "a" 1 "b" 2)

  (pg-tf-js '(js {:a [1 2 3]}))
  => '(jsonb-build-object "a" (jsonb-build-array 1 2 3)))


tahto/model/spec_postgres/tf_test.clj:21:^{:refer tahto.model.spec-postgres.tf/pg-tf-for :added "4.0"}
(fact "creates for loop"

  (pg-tf-for '(for [i < 0] (:++ i)))
  => '[:FOR i < 0
       :LOOP \\ (\| (do (:++ i))) \\ :END-LOOP \;])

tahto/model/spec_postgres/tf_test.clj:28:^{:refer tahto.model.spec-postgres.tf/pg-tf-foreach :added "4.0"}
(fact "creates foreach loop"

  (pg-tf-foreach '(foreach [i :in (array 1 2 3 4 5)]
                           (:++ i)))
  => '[:FOREACH i :in (array 1 2 3 4 5)
       :LOOP \\ (\| (do (:++ i))) \\ :END-LOOP \;])

tahto/model/spec_postgres/tf_test.clj:36:^{:refer tahto.model.spec-postgres.tf/pg-tf-loop :added "4.0"}
(fact "creates loop"

  (pg-tf-loop '(loop (:++ i)))
  => '[:LOOP \\ (\| (do (:++ i))) \\ :END-LOOP \;])

tahto/model/spec_postgres/tf_test.clj:42:^{:refer tahto.model.spec-postgres.tf/pg-tf-throw :added "4.0"}
(fact "creates throw transform"

  (pg-tf-throw '(throw {}))
  => '[:raise-exception :using (quote [[:detail := (:text (% {}))] [:message := "nil"]])])

tahto/model/spec_postgres/tf_test.clj:48:^{:refer tahto.model.spec-postgres.tf/pg-tf-error :added "4.0"}
(fact "creates error transform"

  (pg-tf-error '(error {}))
  => '[:raise-exception :using (quote [[:detail := (:text (% {:status "error"}))] [:message := "nil"]])])

tahto/model/spec_postgres/tf_test.clj:54:^{:refer tahto.model.spec-postgres.tf/pg-tf-assert :added "4.0"}
(fact "creates assert transform"

  (pg-tf-assert '(assert (= 1 1)
                         [:tag {}]))
  => '(if [:NOT (quote ((= 1 1)))] [:raise-exception :using (quote [[:detail := (:text (% {:status "error", :tag :tag}))]
                                                                    [:message := "tag"]])]))

