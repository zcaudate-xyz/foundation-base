(ns hara.lang.model-annex.spec-r-test
  (:require [clojure.walk :as walk]
            [hara.lang :as l]
             [hara.lang.base.book :as book]
             [hara.lang.model-annex.spec-r.rewrite :as rewrite]
             [hara.lang.model-annex.spec-r :refer :all])
  (:use code.test))

(fact "Preliminary Checks"
  (l/emit-as :R '[[1 2 3 4]])
  => "list(1,2,3,4)"

  (l/emit-as :R '[(+ 1 2 3)])
  => "1 + 2 + 3"

  (l/emit-as :R '[(paste "hello" "world" :sep " ")])
  => "paste('hello','world',sep=' ')"

  (l/emit-as :R '[{:a {:b 3}}])
  => "list(a=list(b=3))"

  (l/emit-as :R '[(. ["a" "b" "c"] [2])])
  => "list('a','b','c')[[2]]"
)

^{:refer hara.lang.model-annex.spec-r/tf-defn :added "3.0"}
(fact "function declaration for R"
  (tf-defn '(defn hello [x y] (return (+ x y))))
  => '(def hello (fn [x y] (return (+ x y)))))

^{:refer hara.lang.model-annex.spec-r/tf-infix-if :added "4.0"}
(fact "transform for infix if"
  (tf-infix-if '(:? 1 2 3 4))
  => '((:- "`if`") 1 2 ((:- "`if`") 3 4)))

^{:refer hara.lang.model-annex.spec-r/tf-for-object :added "4.0"}
(fact "transform for `for:object`"
  (tf-for-object '(for:object [[k v] obj]))
  => '(for [k :in (names obj)] (:= v (. obj [k]))))

^{:refer hara.lang.model-annex.spec-r/tf-for-array :added "4.0"}
(fact "transform for `for:array`"
  (tf-for-array '(for:array [[i e] arr]))
  => '(do (var i := 0) (for [e :in (% arr)] (:= i (+ i 1))))

  (tf-for-array '(for:array [e arr]))
  => '(for [e :in (% arr)]))

^{:refer hara.lang.model-annex.spec-r/tf-for-iter :added "4.0"}
(fact "transform for `for:iter`"
  (tf-for-iter '(for:iter [e it]))
  => '(for [e :in (% it)]))

^{:refer hara.lang.model-annex.spec-r/tf-for-index :added "4.0"}
(fact "transform for `for:index`"
  (tf-for-index '(for:index [i [0 10 3]]))
  => '(for [i :in (seq 0 10 3)]))


^{:refer hara.lang.model-annex.spec-r/r-map :added "4.1"}
(fact "TODO")
