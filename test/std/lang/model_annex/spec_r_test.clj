(ns std.lang.model-annex.spec-r-test
  (:require [clojure.walk :as walk]
            [std.lang :as l]
             [std.lang.base.book :as book]
             [std.lang.model-annex.spec-r.rewrite :as rewrite]
             [std.lang.model-annex.spec-r :refer :all])
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

^{:refer std.lang.model-annex.spec-r/tf-defn :added "3.0"}
(fact "function declaration for R"
  (tf-defn '(defn hello [x y] (return (+ x y))))
  => '(def hello (fn [x y] (return (+ x y)))))

^{:refer std.lang.model-annex.spec-r/tf-infix-if :added "4.0"}
(fact "transform for infix if"
  (tf-infix-if '(:? 1 2 3 4))
  => '((:- "`if`") 1 2 ((:- "`if`") 3 4)))

^{:refer std.lang.model-annex.spec-r/tf-for-object :added "4.0"}
(fact "transform for `for:object`"
  (tf-for-object '(for:object [[k v] obj]))
  => '(for [k :in (names obj)] (:= v (. obj [k]))))

^{:refer std.lang.model-annex.spec-r/tf-for-array :added "4.0"}
(fact "transform for `for:array`"
  (tf-for-array '(for:array [[i e] arr]))
  => '(do (var i := 0) (for [e :in (% arr)] (:= i (+ i 1))))

  (tf-for-array '(for:array [e arr]))
  => '(for [e :in (% arr)]))

^{:refer std.lang.model-annex.spec-r/tf-for-iter :added "4.0"}
(fact "transform for `for:iter`"
  (tf-for-iter '(for:iter [e it]))
  => '(for [e :in (% it)]))

^{:refer std.lang.model-annex.spec-r/tf-for-index :added "4.0"}
(fact "transform for `for:index`"
  (tf-for-index '(for:index [i [0 10 3]]))
  => '(for [i :in (seq 0 10 3)]))

^{:refer std.lang.model-annex.spec-r/tf-for-return :added "4.0"}
(fact  "transform for `for:return`"
  (tf-for-return '(for:return [[ok err] (call)]
                              {:success ok
                               :error err}))
  => '(tryCatch (block (var ok (call)) ok) :error (fn [err] err))

  (tf-for-return '(for:return [[ok err] (x:return-run runner)]
                              {:success ok
                               :error err}))
  => '(block
        (var ok nil)
        (tryCatch
         (block
          (runner
           (fn [value]
             (:= ok value))
            (fn [value]
              (stop value)))
          ok)
         :error (fn [err] err))))

^{:refer std.lang.model-annex.spec-r/r-tf-var :added "4.1"}
(fact "transforms destructuring vars for R"
  (let [out (r-tf-var '(var #{path host} opts))
        [_ temp-bind & binds] out
        [_ temp-sym _ temp-val] temp-bind
        bind-set (set binds)]
    (and (= 'do (first out))
         (= 'var* (first temp-bind))
         (= 'opts temp-val)
         (contains? bind-set
                    (list 'var* 'host := (list 'x:get-key temp-sym "host" nil)))
         (contains? bind-set
                    (list 'var* 'path := (list 'x:get-key temp-sym "path" nil)))))
  => true

  (let [out (r-tf-var '(var [ns name] pair))
        [_ temp-bind ns-bind name-bind] out
        [_ temp-sym _ temp-val] temp-bind]
    (and (= 'do (first out))
         (= 'var* (first temp-bind))
         (= 'pair temp-val)
         (= '(var* ns := (x:get-idx TEMP (x:offset 0))) (walk/postwalk-replace {temp-sym 'TEMP} ns-bind))
         (= '(var* name := (x:get-idx TEMP (x:offset 1))) (walk/postwalk-replace {temp-sym 'TEMP} name-bind))))
  => true

  (let [out (rewrite/r-rewrite-stage '(let [#{path} opts
                                            x 1]
                                        path)
                                     nil)
        [_ bindings body] out
        [temp-sym temp-val path-sym path-val x-sym x-val] bindings]
    (and (= 'let (first out))
         (= 'opts temp-val)
         (= 'path path-sym)
         (= (list 'x:get-key temp-sym "path" nil) path-val)
         (= 'x x-sym)
         (= 1 x-val)
         (= 'path body)))
  => true)
