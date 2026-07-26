tahto/common/emit_helper_test.clj:1:(ns tahto.common.emit-helper-test
tahto/common/emit_helper_test.clj:2:  (:require [tahto.common.emit-common :as common]
tahto/common/emit_helper_test.clj:3:            [tahto.common.emit-helper :as helper :refer :all]
tahto/common/emit_helper_test.clj:4:            [tahto.common.grammar :as grammar])
  (:use code.test))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

tahto/common/emit_helper_test.clj:14:^{:refer tahto.common.emit-helper/default-emit-fn :added "4.0"}
(fact "the default emit function"
  (default-emit-fn 'abc {} {}) => "abc")

tahto/common/emit_helper_test.clj:18:^{:refer tahto.common.emit-helper/pr-single :added "3.0"}
(fact "prints a single quoted string"

  (pr-single "hello")
  => "'hello'"

  (pr-single "he'\"llo")
  => "'he\\'\"llo'"

  (pr-single "''")
  => "'\\'\\''")

tahto/common/emit_helper_test.clj:30:^{:refer tahto.common.emit-helper/get-option :added "3.0"}
(fact "gets either the path option or the default one"

  (get-option helper/+default+ [:block :for] :sep)
  => ","

  (get-option helper/+default+ [:data :map] :start)
  => "{")

tahto/common/emit_helper_test.clj:39:^{:refer tahto.common.emit-helper/get-options :added "3.0"}
(fact "gets the path option merged with defaults"

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

tahto/common/emit_helper_test.clj:57:^{:refer tahto.common.emit-helper/form-key-base :added "4.0"}
(fact "gets the key for a form"

  (form-key-base :a)
  => [:keyword :token true]

  (form-key-base ())
  => :expression)

tahto/common/emit_helper_test.clj:66:^{:refer tahto.common.emit-helper/basic-typed-args :added "4.0"}
(fact "typed args without grammar checks"

  (mapv (juxt meta identity)
        (basic-typed-args '(:int i, :const :int j)))
  => '[[{:- [:int]} i]
       [{:- [:const :int]} j]])

tahto/common/emit_helper_test.clj:74:^{:refer tahto.common.emit-helper/emit-typed-allowed-args :added "4.0"}
(fact "allowed declared args other than symbols"
  (emit-typed-allowed-args [[{:modifiers [:int]}] {:modifiers [:int]}] {:allow {:assign #{:symbol}}})
  => throws)

tahto/common/emit_helper_test.clj:79:^{:refer tahto.common.emit-helper/emit-typed-args :added "3.0"}
(fact "create types args from declarationns"

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
  => '[{:modifiers [:mutable], :symbol a, :type (:int), :value 9}]

  (emit-typed-args '(a b)
                   +grammar+)
  => '[{:modifiers [], :symbol a}
       {:modifiers [], :symbol b}]

  (emit-typed-args '(a b)
                   +grammar+
                   {:shorthand true})
  => '[{:modifiers [], :symbol a, :value b}]

  (emit-typed-args '(out [])
                   +grammar+
                   {:shorthand true})
  => '[{:modifiers [], :symbol out, :value []}])

tahto/common/emit_helper_test.clj:118:^{:refer tahto.common.emit-helper/emit-symbol-full :added "4.0"}
(fact "emits a full symbol"

  (emit-symbol-full 'hello 'ns +grammar+)
  => "ns____hello")

tahto/common/emit_helper_test.clj:124:^{:refer tahto.common.emit-helper/emit-type-record :added "4.0"}
(fact "formats to standard"

  (emit-type-record {:modifiers [:int]
                     :symbol "a"})
  => {:symbol "a", :type "int"})


tahto/common/emit_helper_test.clj:132:^{:refer tahto.common.emit-helper/rest-arg-form? :added "4.1"}
(fact "recognizes only the canonical final rest argument shape"
  (rest-arg-form? '(:.. args)) => true
  (rest-arg-form? '(:.. "args")) => false
  (rest-arg-form? '(:.. args extra)) => false
  (rest-arg-form? '[:.. args]) => false)

tahto/common/emit_helper_test.clj:139:^{:refer tahto.common.emit-helper/rest-arg-symbol :added "4.1"}
(fact "returns the symbol from a canonical rest argument"
  (rest-arg-symbol '(:.. args)) => 'args
  (rest-arg-symbol '(:.. "args")) => nil)
