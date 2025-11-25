(ns std.lang.base.emit-fn-test
  (:use code.test)
  (:require [std.lang.base.emit-fn :refer :all]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar]
            [std.lib :as h]))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer std.lang.base.emit-fn/emit-input-default :added "3.0"}
(fact "create input arg strings"
  ^:hidden

  (-> (helper/emit-typed-args '(:int i := 9, :const :int j := 10)
                              +grammar+)
      first
      (emit-input-default "=" {} {}))
  => "int i = 9"
  
  (emit-input-default
   '{:modifiers [:int], :symbol i, :assign true, :force true, :value 9}
   "=" {} {})
  => "int i = 9"
  
  (emit-input-default
   '{:modifiers [:const :int], :symbol j, :assign true, :force true, :value 10}
   "=" {} {})
  => "const int j = 10"
  
  (emit-input-default
   '{:modifiers [:const :int]}
   "=" {} {})
  => "const int"

  (emit-input-default
   '{:symbol #{key url}, :assign true, :force true, :value 10}
   "=" {} {}))

^{:refer std.lang.base.emit-fn/emit-hint-type :added "4.0"}
(fact "emits the return type"
  "placeholder for tests")

^{:refer std.lang.base.emit-fn/emit-def-type :added "4.0"}
(fact "emits the def type"
  "placeholder for tests")

^{:refer std.lang.base.emit-fn/emit-fn-type :added "3.0"}
(fact "returns the function type"
  ^:hidden
  
  (emit-fn-type (with-meta 'hello {:- [:char]}) nil
                +grammar+
                {})
  => "char"

  (emit-fn-type nil "function"
                +grammar+
                {})
  => "function")

^{:refer std.lang.base.emit-fn/emit-fn-block :added "4.0"}
(fact "gets the block options for a given grammar"
  ^:hidden
  
  (emit-fn-block :default +grammar+)
  => {:raw "function",
      :args {:start "(", :end ")", :space ""},
      :body {:start "{", :end "}"}})

^{:refer std.lang.base.emit-fn/emit-fn-preamble-args :added "4.0"}
(fact "constructs the function preamble args"
  ^:hidden
  
  (emit-fn-preamble-args :defn '[:int i 9, :const :int j 10]
                         #_(emit-fn-block :default +grammar+)
                         +grammar+
                         {})
  => '("int i = 9" "const int j = 10"))

^{:refer std.lang.base.emit-fn/emit-fn-preamble :added "4.0"}
(fact "constructs the function preamble"
  ^:hidden
  
  (emit-fn-preamble [:defn 'sym '[:int i 9, :const :int j 10]]
                    #_(emit-fn-block :default +grammar+)
                    +grammar+
                    {})
  => "sym(int i = 9,const int j = 10)")

^{:refer std.lang.base.emit-fn/emit-fn :added "3.0"}
(fact "emits a function template"
  ^:hidden

  (binding [common/*emit-fn* test-fn-emit]
    (emit-fn :function
             '[function sym [:int i 9, :const :int j 10]
               (for [(:= i 0) (< i j) (:++ i)]
                 (return i))]
             +grammar+
             {}))
  => (std.string/|
      "function sym(int i = 9,const int j = 10){"
      "  for(i = 0, i < j, ++i){"
      "    return i;"
      "  }"
      "}"))

^{:refer std.lang.base.emit-fn/test-fn-loop :added "4.0"}
(fact "add blocks, fn, var and const to emit"
  "placeholder for tests")

^{:refer std.lang.base.emit-fn/test-fn-emit :added "4.0"}
(fact  "add blocks, fn, var and const to emit"
  "placeholder for tests")
