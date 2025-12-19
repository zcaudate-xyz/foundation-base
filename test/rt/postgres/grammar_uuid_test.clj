(ns rt.postgres.grammar-uuid-test
  (:require [rt.postgres.grammar :refer :all]
            [std.lang :as l]
            [code.test :as t]))

(t/fact "check UUID emission"
  (let [uuid (java.util.UUID/fromString "59477209-661b-410a-85b5-14f762696660")]
    (l/emit-as :postgres [uuid]))
  => "'59477209-661b-410a-85b5-14f762696660'::uuid")
