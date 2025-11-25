(ns std.lib.template-test
  (:use code.test)
  (:require [std.lib.template :refer :all]))

^{:refer std.lib.template/replace-template :added "3.0"}
(fact "replace arguments with values"

  (replace-template '(+ $0 $1 $2) '[x y z])
  => '(+ x y z))

^{:refer std.lib.template/template:locals :added "3.0"}
(fact "gets local variable symbol names and values"

  (let [x 1 y 2]
    (template:locals))
  => '{x 1, y 2})

^{:refer std.lib.template/local-eval :added "3.0"}
(fact "evals local variables"

  (let [x 1]
    (binding [*locals* (template:locals)]
      (local-eval '(+ x x))))
  => 2)

^{:refer std.lib.template/replace-unquotes :added "3.0"}
(fact "replace unquote and unquote splicing"

  (let [x 1]
    (binding [*locals* (template:locals)]
      (replace-unquotes '(+ ~x ~@(list x 1 2 3)))))
  => '(+ 1 1 1 2 3) ^:hidden

  (let [f (fn [& args] (apply + args))
        y (f 1)]
    (binding [*locals* (template:locals)]
      (replace-unquotes '(+ ~(f 1 2 3) ~(str 'hello)))))
  => '(+ 6 "hello")

  (let [x 1
        arr [1 2 3 4]]
    (binding [*locals* (template:locals)]
      (replace-unquotes '(+ ~x ~@arr))))
  => '(+ 1 1 2 3 4)

  (let [x 1]
    (binding [*locals* (template:locals)]
      (replace-unquotes '(+ x x))))
  => '(+ x x))

^{:refer std.lib.template/template-fn :added "3.0"}
(fact "replace body with template"

  (let [-x- 'a]
    (binding [*locals* (template:locals)]
      (template-fn '(+ ~-x- 2 nil))))
  => '(+ a 2 nil))

^{:refer std.lib.template/$ :added "3.0"}
(fact "template macro"

  (def -x- 'a)

  ($ (+ ~-x- 2 3))
  => '(+ a 2 3)


  (let [a []]
    ($ (~@a)))
  => ()

  (let [a nil]
    ($ (~@a)))
  => ())

^{:refer std.lib.template/deftemplate :added "3.0"}
(fact "marker for a template form"
  (resolve 'deftemplate) => var?)

(comment
  (./import))
