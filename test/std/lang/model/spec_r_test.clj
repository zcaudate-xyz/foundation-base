(ns std.lang.model.spec-r-test
  (:use code.test)
  (:require [std.lang.model.spec-r :refer :all]
            [std.lang :as l]
            [std.lib :as h]
            [std.lang.base.book :as book]))

(fact "Preliminary Checks"
  (l/emit-as :R '[[1 2 3 4]])
  => "[1,2,3,4]"

  (l/emit-as :R '[(+ 1 2 3)])
  => "1 + 2 + 3"

  (l/emit-as :R '[(paste "hello" "world" :sep " ")])
  => "paste(\"hello\",\"world\",sep=\" \")"

  (l/emit-as :R '[{:a {:b 3}}])
  => "{\"a\":{\"b\":3}}"

  (l/emit-as :R '[(. ["a" "b" "c"] [2])])
  => "[\"a\",\"b\",\"c\"][2]"
)

^{:refer std.lang.model.spec-r/tf-defn :added "3.0"}
(fact "function declaration for R"
  (tf-defn '(defn hello [x y] (return (+ x y))))
  => '(def hello (fn [x y] (return (+ x y)))))

^{:refer std.lang.model.spec-r/tf-infix-if :added "4.0"}
(fact "transform for infix if"
  (tf-infix-if '(:? 1 2 3 4))
  => '((:- "`if`") 1 2 ((:- "`if`") 3 4)))

^{:refer std.lang.model.spec-r/tf-for-object :added "4.0"}
(fact "transform for `for:object`"
  (tf-for-object '(for:object [[k v] obj]))
  => '(for [k :in (names obj)] (:= v (. obj [k]))))

^{:refer std.lang.model.spec-r/tf-for-array :added "4.0"}
(fact "transform for `for:array`"
  (tf-for-array '(for:array [[i e] arr]))
  => '(do (var i := 0) (for [e :in (% arr)] (:= i (+ i 1))))

  (tf-for-array '(for:array [e arr]))
  => '(for [e :in (% arr)]))

^{:refer std.lang.model.spec-r/tf-for-iter :added "4.0"}
(fact "transform for `for:iter`"
  (tf-for-iter '(for:iter [e it]))
  => '(for [e :in (% it)]))

^{:refer std.lang.model.spec-r/tf-for-index :added "4.0"}
(fact "transform for `for:index`"
  (tf-for-index '(for:index [i [0 10 3]]))
  => '(for [i :in (seq 0 10 3)]))

^{:refer std.lang.model.spec-r/tf-for-return :added "4.0"}
(fact  "transform for `for:return`"
  (tf-for-return '(for:return [[ok err] (call)]
                              {:success ok
                               :error err}))
  => '(tryCatch (block (var ok (call)) ok) :error (fn [err] err)))
