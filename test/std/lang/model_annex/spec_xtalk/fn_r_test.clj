(ns std.lang.model-annex.spec-xtalk.fn-r-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lang.model-annex.spec-xtalk.fn-r :refer :all]
            [xt.lang.common-lib :as k]
            [xt.lang.common-string :as xts]
            [xt.lang.spec-base :as xt]))

(l/script- :r
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-string :as xts]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer std.lang.model-annex.spec-xtalk.fn-r/r-tf-x-lu-create :added "4.1"}
(fact "creates an environment-backed lookup"
  (l/emit-as :r [(r-tf-x-lu-create '(_))])
  => "new.env(hash=TRUE,parent=emptyenv())")

^{:refer std.lang.model-annex.spec-xtalk.fn-r/r-tf-x-lu-get :added "4.1"}
(fact "emits lookup access through get0"
  (l/emit-as :r [(r-tf-x-lu-get '(_ lu key nil))])
  => #"get0\("

  (l/emit-as :r [(r-tf-x-lu-set '(_ lu key value))])
  => #"assign\("

  (l/emit-as :r [(r-tf-x-lu-del '(_ lu key))])
  => #"exists\(")

(fact "supports lookup mutation and identity in the R runtime"
  (!.R
   (var lu (xt/x:lu-create))
   (xt/x:lu-set lu "a" 1)
   (xt/x:lu-get lu "a"))
  => 1

  (!.R
   (var lu (xt/x:lu-create))
   (xt/x:lu-set lu lu 2)
   (xt/x:lu-get lu lu))
  => 2

  (!.R
   (var lu (xt/x:lu-create))
   (xt/x:lu-set lu "a" 1)
   (xt/x:lu-del lu "a")
   (xt/x:lu-get lu "a"))
  => nil

  (!.R
   (var lu0 (xt/x:lu-create))
   (var lu1 (xt/x:lu-create))
   [(xt/x:lu-eq lu0 lu0)
    (xt/x:lu-eq lu0 lu1)
    (xt/x:lu-eq "a" "a")
    (xt/x:lu-eq "a" "b")])
  => [true false true false])

^{:refer std.lang.model-annex.spec-xtalk.fn-r/r-tf-x-str-char :added "4.1"}
(fact "emits R string and predicate helpers"
  (l/emit-as :r [(r-tf-x-str-char '(_ s i))])
  => #"utf8ToInt"

  (l/emit-as :r [(r-tf-x-str-to-fixed '(_ n digits))])
  => #"sprintf"

  (l/emit-as :r [(r-tf-x-str-trim '(_ s))])
  => #"trimws"

  (l/emit-as :r [(r-tf-x-str-comp '(_ a b))])
  => "a < b"

  (l/emit-as :r [(r-tf-x-is-integer? '(_ x))])
  => #"floor")

(fact "supports key R backend runtime helpers"
  (!.R
   [(xt/x:is-array? [1 2 3])
    (xt/x:is-array? {:a 1})
    (xt/x:is-integer? 4)
    (xt/x:is-integer? 4.5)])
  => [true false true false]

  (!.R
   (xts/get-char "abc" (xt/x:offset 0)))
  => 97

  (!.R
   [(xts/to-fixed 1.2 3)
    (xts/trim " \n  hello \n  ")
    (xts/trim-left "  hello  ")
    (xts/trim-right "  hello  ")])
  => ["1.200" "hello" "hello  " "  hello"]

  (!.R
   (xts/pad-lines (xts/join "\n" ["hello" "world"]) 2 " "))
  => "  hello\n  world"

  (!.R
   (xt/x:json-decode (k/return-encode 1 "id-A" "key-A")))
  => {"return" "number", "key" "key-A", "id" "id-A", "value" 1, "type" "data"}

  (!.R
   (xt/x:json-decode (k/return-wrap (fn [] (return 3)))))
  => (contains {"return" "number", "value" 3, "type" "data"})

  (!.R
   (xt/x:json-decode (k/return-eval "1+1")))
  => {"return" "number", "value" 2, "type" "data"})
