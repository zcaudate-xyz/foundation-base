(ns std.lang.model.spec-r-test
  (:use code.test)
  (:require [std.lang.model.spec-r :refer :all]
            [std.lang :as l]))

(l/script- :r
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(comment
  ^{:refer std.lang.spec.r/CANARY :guard true :adopt true :added "3.0"}
  (fact "Preliminary Checks"

    (!.R [1 2 3 4])
    => [1 2 3 4]
    
    (!.R (+ 1 2 3))
    => 6
    
    (!.R (paste "hello" "world" :sep " "))
    => "hello world"
    
    (!.R (k/add 1 2))
    => 3
    
    (!.R {:a {:b 3}})
    => {"a" {"b" 3}}
    
    (!.R (. ["a" "b" "c"]
            [2]))
    => "b"
    
    (!.R
     (block
      [1 2 3 4]
      [1 2 3 4]))
    => [1 2 3 4])

  ^{:refer std.lang.model.spec-r/tf-defn :added "3.0"}
  (fact "function declaration for python"

    (tf-defn '(defn hello [x y] (return (+ x y))))
    => '(def hello (fn [x y] (return (+ x y))))
    
    (!.R
     (defn ^{:inner true}
       hello [x y] (+ x y))
     (hello 1 2))
    => 3)

  ^{:refer std.lang.model.spec-r/tf-infix-if :added "4.0"}
  (fact "transform for infix if"
    ^:hidden
    
    (tf-infix-if '(:? 1 2 3 4))
    => '((:- "`if`") 1 2 ((:- "`if`") 3 4)))

  ^{:refer std.lang.model.spec-r/tf-for-object :added "4.0"}
  (fact "transform for `for:object`"
    ^:hidden
    
    (tf-for-object '(for:object [[k v] obj]))
    => '(for [k :in (names obj)] (:= v (. obj [k]))))

  ^{:refer std.lang.model.spec-r/tf-for-array :added "4.0"}
  (fact "transform for `for:array`"
    ^:hidden
    
    (tf-for-array '(for:array [[i e] arr]))
    => '(do (var i := 0) (for [e :in (% arr)] (:= i (+ i 1))))

    (tf-for-array '(for:array [e arr]))
    => '(for [e :in (% arr)]))

  ^{:refer std.lang.model.spec-r/tf-for-iter :added "4.0"}
  (fact "transform for `for:iter`"
    ^:hidden
    
    (tf-for-iter '(for:iter [e it]))
    => '(for [e :in (% it)]))

  ^{:refer std.lang.model.spec-r/tf-for-index :added "4.0"}
  (fact "transform for `for:index`"
    ^:hidden
    
    (tf-for-index '(for:index [i [0 10 3]]))
    => '(for [i :in (seq 0 10 3)]))

  ^{:refer std.lang.model.spec-r/tf-for-return :added "4.0"}
  (fact  "transform for `for:return`"
    ^:hidden
    
    (tf-for-return '(for:return [[ok err] (call)]
                                {:success ok
                                 :error err}))
    => '(tryCatch (block (var ok (call)) ok) :error (fn [err] err))))


^{:refer std.lang.model.spec-r/tf-defn :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-r/tf-infix-if :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-r/tf-for-object :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-r/tf-for-array :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-r/tf-for-iter :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-r/tf-for-index :added "4.1"}
(fact "TODO")

^{:refer std.lang.model.spec-r/tf-for-return :added "4.1"}
(fact "TODO")