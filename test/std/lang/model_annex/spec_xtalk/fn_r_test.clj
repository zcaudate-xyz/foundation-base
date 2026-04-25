(ns std.lang.model-annex.spec-xtalk.fn-r-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lang.model-annex.spec-xtalk.fn-r :refer :all]
            [xt.lang.spec-base :as xt]))

(l/script- :r
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]]})

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
