tahto/model/annex/spec_r_test.clj:1:(ns tahto.model.annex.spec-r-test
  (:require [clojure.walk :as walk]
            [tahto.core :as l]
             [tahto.base.book :as book]
tahto/model/annex/spec_r_test.clj:5:             [tahto.model.annex.spec-r.rewrite :as rewrite]
tahto/model/annex/spec_r_test.clj:6:             [tahto.model.annex.spec-r :refer :all])
  (:use code.test))

(fact "Preliminary Checks"
  (l/emit-as :R '[[1 2 3 4]])
  => "c(1,2,3,4)"

  (l/emit-as :R '[(+ 1 2 3)])
  => "1 + 2 + 3"

  (l/emit-as :R '[(paste "hello" "world" :sep " ")])
  => "paste('hello','world',sep=' ')"

  (l/emit-as :R '[{:a {:b 3}}])
  => "list(a=list(b=3))"

  (l/emit-as :R '[(. ["a" "b" "c"] [2])])
  => "c('a','b','c')[[2]]"
)

tahto/model/annex/spec_r_test.clj:26:^{:refer tahto.model.annex.spec-r/tf-defn :added "3.0"}
(fact "function declaration for R"
  (tf-defn '(defn hello [x y] (return (+ x y))))
  => '(def hello (fn [x y] (return (+ x y)))))

tahto/model/annex/spec_r_test.clj:31:^{:refer tahto.model.annex.spec-r/tf-infix-if :added "4.0"}
(fact "transform for infix if"
  (tf-infix-if '(:? 1 2 3 4))
  => '((:- "`if`") 1 2 ((:- "`if`") 3 4)))

tahto/model/annex/spec_r_test.clj:36:^{:refer tahto.model.annex.spec-r/tf-for-object :added "4.0"}
(fact "transform for `for:object`"
  (tf-for-object '(for:object [[k v] obj]))
  => '(for [k :in (names obj)] (:= v (. obj [k]))))

tahto/model/annex/spec_r_test.clj:41:^{:refer tahto.model.annex.spec-r/tf-for-array :added "4.0"}
(fact "transform for `for:array`"
  (tf-for-array '(for:array [[i e] arr]))
  => '(do (var i := 0) (for [e :in (% arr)] (:= i (+ i 1))))

  (tf-for-array '(for:array [e arr]))
  => '(for [e :in (% arr)]))

tahto/model/annex/spec_r_test.clj:49:^{:refer tahto.model.annex.spec-r/tf-for-iter :added "4.0"}
(fact "transform for `for:iter`"
  (tf-for-iter '(for:iter [e it]))
  => '(for [e :in (% it)]))

tahto/model/annex/spec_r_test.clj:54:^{:refer tahto.model.annex.spec-r/tf-for-index :added "4.0"}
(fact "transform for `for:index`"
  (tf-for-index '(for:index [i [0 10 3]]))
  => '(for [i :in (seq 0 10 3)]))


tahto/model/annex/spec_r_test.clj:60:^{:refer tahto.model.annex.spec-r/r-map :added "4.1"}
(fact "R empty map literal"
  (r-map {} nil nil)
tahto/model/annex/spec_r_test.clj:63:  => "structure(list(), names=ctahtocter())")

tahto/model/annex/spec_r_test.clj:65:^{:refer tahto.model.annex.spec-r/tf-formula :added "4.1"}
(fact "transform for formula"
  (tf-formula '(formula y x))
  => '(:- "y ~ x"))

tahto/model/annex/spec_r_test.clj:70:^{:refer tahto.model.annex.spec-r/tf-library :added "4.1"}
(fact "transform for library"
  (tf-library '(library jsonlite))
  => '(:- "library(\"jsonlite\")"))

tahto/model/annex/spec_r_test.clj:75:^{:refer tahto.model.annex.spec-r/tf-df :added "4.1"}
(fact "transform for data frame"
  (tf-df '(df {:a [1 2] :b [3 4]}))
  => '(data.frame :a (c 1 2) :b (c 3 4)))

(fact "New grammar additions"
  (l/emit-as :R '[NA])
  => "NA"

  (l/emit-as :R '[NaN])
  => "NaN"

  (l/emit-as :R '[Inf])
  => "Inf"

  (l/emit-as :R '[(throw "boom")])
  => "stop('boom')")
