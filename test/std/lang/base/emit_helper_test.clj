(ns std.lang.base.emit-helper-test
  (:use code.test)
  (:require [std.lang.base.emit-helper :refer :all :as helper]
            [std.lang.base.emit-common :as common]
            [std.lang.base.grammar :as grammar]))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer std.lang.base.emit-helper/default-emit-fn :added "4.0"}
(fact "the default emit function"
  "placeholder for tests")

^{:refer std.lang.base.emit-helper/pr-single :added "3.0"}
(fact "prints a single quoted string"
  ^:hidden
  
  (pr-single "hello")
  => "'hello'"

  (pr-single "he'\"llo")
  => "'he\\'\"llo'"

  (pr-single "''")
  => "'\\'\\''")

^{:refer std.lang.base.emit-helper/get-option :added "3.0"}
(fact "gets either the path option or the default one"
  ^:hidden

  (get-option helper/+default+ [:block :for] :sep)
  => ","

  (get-option helper/+default+ [:data :map] :start)
  => "{")

^{:refer std.lang.base.emit-helper/get-options :added "3.0"}
(fact "gets the path option merged with defaults"
  ^:hidden
  
  (get-options helper/+default+ [:data :map])
  => {:statement ";",
      :sep ",",
      :space "",
      :static ".",
      :start "{",
      :line-spacing 1,
      :assign "=",
      :namespace-full "____",
      :apply ".",
      :access ".",
      :end "}",
      :namespace ".",
      :range ":"})

^{:refer std.lang.base.emit-helper/form-key-base :added "4.0"}
(fact "gets the key for a form"

  (form-key-base :a)
  => [:keyword :token true]

  (form-key-base ())
  => :expression)

^{:refer std.lang.base.emit-helper/basic-typed-args :added "4.0"}
(fact "typed args without grammar checks"

  (mapv (juxt meta identity)
        (basic-typed-args '(:int i, :const :int j)))
  => '[[{:- [:int]} i]
       [{:- [:const :int]} j]])

^{:refer std.lang.base.emit-helper/emit-typed-allowed-args :added "4.0"}
(fact "allowed declared args other than symbols"
  "placeholder for tests")

^{:refer std.lang.base.emit-helper/emit-typed-args :added "3.0"}
(fact "create types args from declarationns"
  ^:hidden

  (emit-typed-args '(:int i := 9, :const :int j := 10)
                   +grammar+)
  => '[{:modifiers [:int],
        :symbol i,
        :assign true,
        :force true,
        :value 9}
       {:modifiers [:const :int],
        :symbol j,
        :assign true,
        :force true,
        :value 10}]

  (emit-typed-args '((:int a) 9)
                   +grammar+)
  => '[{:modifiers [], :symbol a, :type (:int), :value 9}]

  (emit-typed-args '(:mutable (:int a) 9)
                   +grammar+)
  => '[{:modifiers [:mutable], :symbol a, :type (:int), :value 9}])

^{:refer std.lang.base.emit-helper/emit-symbol-full :added "4.0"}
(fact "emits a full symbol"
  ^:hidden

  (emit-symbol-full 'hello 'ns +grammar+)
  => "ns____hello")

^{:refer std.lang.base.emit-helper/emit-type-record :added "4.0"}
(fact "formats to standard"
  ^:hidden
  
  (emit-type-record {:modifiers [:int]
                     :symbol "a"})
  => {:symbol "a", :type "int"})
