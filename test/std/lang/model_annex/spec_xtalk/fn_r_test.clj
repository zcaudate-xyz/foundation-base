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

(fact "emits lookup mutation and identity helpers for the R runtime"
  (let [create-out (pr-str (r-tf-x-lu-create '(_)))
        set-out    (pr-str (r-tf-x-lu-set '(_ lu "a" 1)))
        get-out    (pr-str (r-tf-x-lu-get '(_ lu "a" nil)))
        eq-out     (pr-str (r-tf-x-lu-eq '(_ lu0 lu1)))]
    [(boolean (re-find #"new\.env" create-out))
     (boolean (re-find #"assign" set-out))
     (boolean (re-find #"get0" get-out))
     (boolean (re-find #"paste" eq-out))
     (boolean (re-find #"==" eq-out))])
  => [true true true true true])

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

(fact "uses base R collection primitives for array, object, and iterator helpers"
  (l/emit-as :r [(r-tf-x-arr-map '(_ arr f))])
  => #"lapply"

  (l/emit-as :r [(r-tf-x-arr-filter '(_ arr pred))])
  => #"Filter"

  (l/emit-as :r [(r-tf-x-arr-foldl '(_ arr f init))])
  => #"Reduce"

  (l/emit-as :r [(r-tf-x-obj-pairs '(_ obj))])
  => #"Map"

  (l/emit-as :r [(r-tf-x-iter-from-obj '(_ obj))])
  => #"Map")

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
    (xt/x:nil? (xt/x:print "hello")))
  => true

  (!.R
    (xt/x:str-index-of "hello/world" "/" (xt/x:offset 0)))
  => 5

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
  => {"return" "number", "value" 2, "type" "data"}

  (!.R
    [(xt/x:bit-and 6 3)
     (xt/x:bit-or 6 3)
     (xt/x:bit-lshift 3 2)
     (xt/x:bit-rshift 12 2)
     (xt/x:bit-xor 6 3)])
  => [2 7 12 3 5]

  (let [out (pr-str (r-tf-x-arr-remove '(_ out 1)))]
    [(boolean (re-find #"append" out))
     (boolean (re-find #"head" out))
     (boolean (re-find #"tail" out))])
  => [true true true]

  (let [out (pr-str (r-tf-x-arr-sort '(_ out key-fn comp-fn)))]
    [(boolean (re-find #"seq_len" out))
     (boolean (re-find #"\(seq " out))
     (boolean (re-find #"comp-fn" out))])
  => [true true true]

  (!.R
    [(xt/x:iter-has? [1 2 3])
     (xt/x:iter-has? {:a 1})])
  => [true false]

  (!.R
    [(xt/x:obj-keys {:a 1})
     (xt/x:obj-vals {:a 1})
     (xt/x:obj-pairs {:a 1})])
  => [["a"] [1] [["a" 1]]])
