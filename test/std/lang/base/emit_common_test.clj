(ns std.lang.base.emit-common-test
  (:use code.test)
  (:require [std.lang.base.emit-common :refer :all :as common]
            [std.lang.base.emit-helper :as helper]))

^{:refer std.lang.base.emit-common/with:explode :added "4.0"}
(fact "form to control `explode` option"
  "placeholder for tests")

^{:refer std.lang.base.emit-common/with-trace :added "4.0"}
(fact "form to control `trace` option"
  "placeholder for tests")

^{:refer std.lang.base.emit-common/with-compressed :added "3.0"}
(fact "formats without newlines and indents"
  "placeholder for tests")

^{:refer std.lang.base.emit-common/with-indent :added "3.0"}
(fact "adds indentation levels"
  ^:hidden
  
  (with-indent [10]
    *indent*)
  => 10)

^{:refer std.lang.base.emit-common/newline-indent :added "3.0"}
(fact "returns a newline with indent"
  ^:hidden

  (newline-indent)
  => "\n"

  (with-indent [2]
    (newline-indent))
  => "\n  ")

^{:refer std.lang.base.emit-common/emit-reserved-value :added "4.0"}
(fact "emits a reserved value"
  ^:hidden
  
  (emit-reserved-value 'hello
                       {}
                       {})
  => nil

  (emit-reserved-value 'hello
                       {:reserved '{hello {:value true
                                           :raw "world"}}}
                       {})
  => "world"
  
  
  (emit-reserved-value 'hello
                       {:reserved '{hello {}}}
                       {})
  => (throws))

^{:refer std.lang.base.emit-common/emit-free-raw :added "4.0"}
(fact "emits free value"
  ^:hidden
  
  (emit-free-raw "." [1 2 3] helper/+default+ {})
  => "1.2.3")

^{:refer std.lang.base.emit-common/emit-free :added "4.0"}
(fact "emits string with multiline support"
  ^:hidden
  
  (emit-free " "
             '(:- 1 2 3 4)
             helper/+default+
             {})
  => "1 2 3 4")

^{:refer std.lang.base.emit-common/emit-comment :added "4.0"}
(fact "emits a comment"
  ^:hidden
  
  (emit-comment nil '(:# "This is a comment" A B 1) helper/+default+ {})
  => "// This is a comment A B 1"

  (emit-comment nil '(:# "This \nis \na comment" A B 1)
                helper/+default+ {})
  
  => (std.string/|
      "// This "
      "// is "
      "// a comment A B 1"))

^{:refer std.lang.base.emit-common/emit-indent :added "4.0"}
(fact "emits an indented form"
  ^:hidden

  (emit-indent nil '(\| "This\nis\nan indented" A B 1)
               helper/+default+ {})
  => (std.string/|
      "  This"
      "  is"
      "  an indented A B 1"))

^{:refer std.lang.base.emit-common/emit-macro :added "4.0"}
(fact "emits form"
  ^:hidden
  
  (emit-macro :double-array
            '(double-array x y c)
            {:reserved
             {'double-array {:macro (fn [[_ & args]]
                                      (vec (concat args
                                                   args)))}}}
            {})
  => "[x y c x y c]")

^{:refer std.lang.base.emit-common/emit-array :added "4.0"}
(fact  "returns an array of emitted strings"
  ^:hidden

  (emit-array [1 2 3] {} {})
  => '("1" "2" "3"))

^{:refer std.lang.base.emit-common/emit-wrappable? :added "4.0"}
(fact "checks if form if wrappable"
  ^:hidden
  
  (emit-wrappable? '(!= 1 x)
                   {:reserved {'!= {:emit :infix}}})
  => true

  (emit-wrappable? '(!= 1 x)
                   {:reserved {'!= {:emit :none}}})
  => false

  (emit-wrappable? '(fn:> 1)
                   {:reserved {'fn    {:wrappable true}
                               'fn:>  {:emit :macro
                                       :macro (fn [_ & args]
                                                (apply list 'fn args))}}})
  => true)

^{:refer std.lang.base.emit-common/emit-squash :added "4.0"}
(fact "emits a squashed representation"
  ^:hidden
  
  (emit-squash nil '(:% 1 2 3 "hello")
               (merge helper/+default+
                      {})
               {})
  => "123\"hello\"")

^{:refer std.lang.base.emit-common/emit-wrapping :added "4.0"}
(fact "emits a potentially wrapped form"
  ^:hidden
  
  (emit-wrapping '(!= 1 x)
                 (merge helper/+default+
                        {:reserved {'!= {:emit :infix}}})
                 {})
  => "((!= 1 x))")

^{:refer std.lang.base.emit-common/wrapped-str :added "3.0"}
(fact "wrapped string using `:start` and `:end` keys of grammar"
  ^:hidden

  (wrapped-str "hello" [:data :map] helper/+default+)
  => "{hello}")

^{:refer std.lang.base.emit-common/emit-unit :added "4.0"}
(fact "emits a unit"
  ^:hidden
  
  (emit-unit {:default 'hello}
             '(:unit)
             helper/+default+
             {})
  => "hello")

^{:refer std.lang.base.emit-common/emit-internal :added "4.0"}
(fact "emits string within the form"
  ^:hidden

  (emit-internal '(% "hello")
                 helper/+default+
                 {})
  => "\"hello\"")

^{:refer std.lang.base.emit-common/emit-internal-str :added "4.0"}
(fact "emits internal string"
  ^:hidden
  
  (emit-internal-str
   '(-%%- ["hello"
           "hello"
           "hello"])
   helper/+default+
   {})
  => "\"hello\"\n\"hello\"\n\"hello\""

  (binding [common/*emit-fn* common/emit-common-loop]
    (emit-internal-str
     '(%%% ["hello"
            "hello"
            "hello"])
     helper/+default+
     {}))
  => "hello\nhello\nhello")

^{:refer std.lang.base.emit-common/emit-pre :added "3.0"}
(fact "emits string before the arg"
  ^:hidden

  (emit-pre "!" '[x] helper/+default+ {})
  => "!x")

^{:refer std.lang.base.emit-common/emit-post :added "3.0"}
(fact "emits string after the arg"
  ^:hidden

  (emit-post "--" '[x] helper/+default+ {})
  => "x--")

^{:refer std.lang.base.emit-common/emit-prefix :added "4.0"}
(fact "emits operator before the arg"
  ^:hidden

  (emit-prefix "hello" '[x] helper/+default+ {})
  => "hello x")

^{:refer std.lang.base.emit-common/emit-postfix :added "4.0"}
(fact  "emits operator before the arg"
  ^:hidden

  (emit-postfix "hello" '[x] helper/+default+ {})
  => "x hello")

^{:refer std.lang.base.emit-common/emit-infix :added "3.0"}
(fact "emits infix ops"
  ^:hidden

  (emit-infix "|" '[x y z] helper/+default+ {})
  => "x | y | z")

^{:refer std.lang.base.emit-common/emit-infix-default :added "3.0"}
(fact "emits infix with a default value"
  ^:hidden

  (emit-infix-default "/" '[x] 1 helper/+default+ {})
  => "1 / x")

^{:refer std.lang.base.emit-common/emit-infix-pre :added "3.0"}
(fact "emits infix with a default value"
  ^:hidden

  (emit-infix-pre "-" '[x] helper/+default+ {})
  => "-x")

^{:refer std.lang.base.emit-common/emit-infix-if-single :added "4.0"}
(fact "checks for infix in single"

  (emit-infix-if '(:? true x y)
                 helper/+default+
                 {})
  => "true ? x : y")

^{:refer std.lang.base.emit-common/emit-infix-if :added "3.0"}
(fact "emits an infix if string"
  ^:hidden

  (emit-infix-if '(:? true x y)
                 helper/+default+
                 {})
  => "true ? x : y"

  (emit-infix-if '(:? true x y)
                 helper/+default+
                 {})
  => "true ? x : y"

  (emit-infix-if '(:? true x
                      :else y)
                 helper/+default+
                 {})
  => "true ? x : y"

  (emit-infix-if '(:? true x
                      true y
                      true z
                      :else t)
                 helper/+default+
                 {:reserved  {:?  {:emit :infix-if}}})
  => "true ? x : (:? true y (:? true z t))")

^{:refer std.lang.base.emit-common/emit-between :added "3.0"}
(fact "emits the raw symbol between two elems"
  ^:hidden

  (emit-between ":" [1 2] helper/+default+ {})
  => "1:2")

^{:refer std.lang.base.emit-common/emit-bi :added "3.0"}
(fact "emits infix with two args"
  ^:hidden

  (emit-bi "==" '[x y] helper/+default+ {})
  => "x == y"

  (emit-bi "==" '[x y z] helper/+default+ {})
  => (throws))

^{:refer std.lang.base.emit-common/emit-assign :added "3.0"}
(fact "emits a setter expression"
  ^:hidden
  
  (emit-assign ":eq" '[x 1] helper/+default+ {})
  => "x :eq 1"

  (emit-assign "=" '[x 1] helper/+default+ {})
  => "x = 1"

  (emit-assign "=" '[x 1] helper/+default+ {})
  => "x = 1")

^{:refer std.lang.base.emit-common/emit-return-do :added "4.0"}
(fact "creates a return statement on `do` block"
  ^:hidden
  
  (emit-return-do
   [1 2 3] helper/+default+ {})
  => "(1 2 (return 3))")

^{:refer std.lang.base.emit-common/emit-return-base :added "4.0"}
(fact "return base type"
  ^:hidden
  
  (emit-return-base "break" [1] helper/+default+ {})
  => "break 1"

  (emit-return-base "return" [1] helper/+default+ {})
  => "return 1")

^{:refer std.lang.base.emit-common/emit-return :added "3.0"}
(fact "creates a return type statement"
  ^:hidden

  (emit-return "break" [1] helper/+default+ {})
  => "break 1"

  (emit-return "return" [1 2 3] helper/+default+ {})
  => (throws)
  
  (emit-return "return" [1 2 3] (assoc-in helper/+default+
                                          [:default :return :multi] true) {})
  => "return 1, 2, 3")

^{:refer std.lang.base.emit-common/emit-with-global :added "4.0"}
(fact "customisable emit function for global vars"
  ^:hidden

  (emit-with-global nil '(!:G HELLO) {} {})
  => "HELLO")

^{:refer std.lang.base.emit-common/emit-symbol-classify :added "3.0"}
(fact "classify symbol given options"

  (emit-symbol-classify 't/hello {:module {:alias '{t table}}})
  => '[:alias table]

  (emit-symbol-classify 't.n/hello {:module {:alias '{t table}}})
  => '[:unknown t.n])

^{:refer std.lang.base.emit-common/emit-symbol-standard :added "3.0"}
(fact "emits a standard symbol"

  (emit-symbol-standard 'print! helper/+default+ {:layout :full})
  => "printf"

  (emit-symbol-standard 'print!
                        {:token {:symbol    {:replace {}}
                                 :string    {:quote :single}}}
                        {:layout :full})
  => "print!")

^{:refer std.lang.base.emit-common/emit-symbol :added "4.0"}
(fact "emits symbol allowing for custom functions"
  ^:hidden
  
  (emit-symbol 'a
               {:token {:symbol {:emit-fn  (fn [sym _ _]
                                             (str sym 123))}}}
               {})
  => "a123")

^{:refer std.lang.base.emit-common/emit-token :added "3.0"}
(fact "customisable emit function for tokens"
  ^:hidden

  (emit-token :number 1 helper/+default+ {})
  => "1"

  (emit-token :string "1" {:token {:string {:quote :single}}} {})
  => "'1'"

  (emit-token :string "1" {:token {:string {:emit (fn [s _ _] (keyword s))}}} {})
  => :1)

^{:refer std.lang.base.emit-common/emit-with-decorate :added "4.0"}
(fact "customisable emit function for global vars"
  ^:hidden

  (emit-with-decorate nil '(!:decorate {:id 1} HELLO) {} {})
  => "HELLO")

^{:refer std.lang.base.emit-common/emit-with-uuid :added "4.0"}
(fact "injects uuid for testing"
  ^:hidden

  (emit-with-uuid nil '(!:uuid :hello :world) {} {})
  => "00000000-05e9-18d2-0000-000006c11b92"

  (emit-with-uuid nil '(!:uuid) {} {})
  => string?)

^{:refer std.lang.base.emit-common/emit-with-rand :added "4.0"}
(fact "injects uuid for testing"
  ^:hidden

  (read-string (emit-with-rand nil '(!:rand :int) {} {}))
  => integer?

  (read-string (emit-with-rand nil '(!:rand) {} {}))
  => float?)

^{:refer std.lang.base.emit-common/invoke-kw-parse :added "3.0"}
(fact "seperates standard and keyword arguments"
  ^:hidden

  (invoke-kw-parse [1 2 3 4 :name "hello" :foo "bar"])
  => '[(1 2 3 4)
       ((:name "hello") (:foo "bar"))])

^{:refer std.lang.base.emit-common/emit-invoke-kw-pair :added "3.0"}
(fact  "emits a kw argument pair"
  ^:hidden

  (emit-invoke-kw-pair [:name "hello"] helper/+default+
                       {})
  => "name=\"hello\"")

^{:refer std.lang.base.emit-common/emit-invoke-args :added "3.0"}
(fact "produces the string for invoke call"
  ^:hidden

  (emit-invoke-args [1 2 3 4 :name "hello" :foo "bar"]
                    helper/+default+
                    {})
  => '("1" "2" "3" "4" "name=\"hello\"" "foo=\"bar\""))

^{:refer std.lang.base.emit-common/emit-invoke-layout :added "4.0"}
(fact "layout for invoke blocks"

  (emit-invoke-layout ["ab\nc"
                       "de\nf"]
                      helper/+default+ {})
  => "(ab\nc,de\nf)")

^{:refer std.lang.base.emit-common/emit-invoke-raw :added "3.0"}
(fact "invoke call for reserved ops"
  ^:hidden

  (emit-invoke-raw "-" '[abc] helper/+default+ {})
  => "-(abc)")

^{:refer std.lang.base.emit-common/emit-invoke-static :added "3.0"}
(fact "generates a static call, alternat"
  ^:hidden

  (emit-invoke-static '(:table/new "hello")
                      helper/+default+
                      {})
  => "table.new(\"hello\")")

^{:refer std.lang.base.emit-common/emit-invoke-typecast :added "3.0"}
(fact "generates typecast expression"
  ^:hidden

  (emit-invoke-typecast '(:int (:char 2))
                        helper/+default+
                        {})
  => "((int)(:char 2))")

^{:refer std.lang.base.emit-common/emit-invoke :added "3.0"}
(fact "general invoke call, incorporating keywords"
  ^:hidden

  (emit-invoke :invoke
               '(call "hello" (+ 1 2))
               helper/+default+
               {})
  => "call(\"hello\",(+ 1 2))"

  (emit-invoke :invoke
               '(:help/call "hello" (+ 1 2))
               helper/+default+
               {}))

^{:refer std.lang.base.emit-common/emit-new :added "3.0"}
(fact "invokes a constructor"
  ^:hidden

  (emit-new "new"
            '(String 1 2 3 4)
            helper/+default+
            {})
  => "new String(1,2,3,4)")

^{:refer std.lang.base.emit-common/emit-class-static-invoke :added "4.0"}
(fact "creates "
  ^:hidden
  
  (emit-class-static-invoke
   nil
   '(String "new" 1 2 3 4)
   helper/+default+
   {})
  => "String.new(1,2,3,4)")

^{:refer std.lang.base.emit-common/emit-index-entry :added "3.0"}
(fact "classifies the index entry"
  ^:hidden

  (emit-index-entry 'hello helper/+default+ {})
  => ".hello"

  (emit-index-entry [9] helper/+default+ {})
  => "[9]"

  (emit-index-entry [9 10] helper/+default+ {})
  => (throws)
  
  (emit-index-entry '(call 1 2 3) helper/+default+ {})
  => ".call(1,2,3)")

^{:refer std.lang.base.emit-common/emit-index :added "3.0"}
(fact "creates an indexed expression"
  ^:hidden

  (emit-index nil '[x [hello] (world foo bar) baz] helper/+default+ {})
  => "x[hello].world(foo,bar).baz")

^{:refer std.lang.base.emit-common/emit-op :added "3.0"}
(fact "helper for the emit op"
  ^:hidden

  (emit-op :- '(:- "~~") (merge helper/+default+
                                {:reserved {:-  {:emit :free}}})
           {})
  => "~~")

^{:refer std.lang.base.emit-common/form-key :added "3.0"}
(fact "returns the key associated with the form"
  ^:hidden

  (form-key (first {:a 1}) {}) => [:map-entry :data nil]
  
  (form-key [] {}) => [:vector :data nil]

  (form-key () {}) => [:invoke :invoke nil])

^{:refer std.lang.base.emit-common/emit-common-loop :added "4.0"}
(fact "emits the raw string"
  ^:hidden
  
  (emit-common-loop '(add 1 (:int 1))
                   helper/+default+
                   {})
  => "add(1,(:int 1))"

  (binding [common/*emit-fn* emit-common-loop]
    (emit-common-loop '(add 1 (:int 1))
                     helper/+default+
                     {}))
  => "add(1,((int)1))")

^{:refer std.lang.base.emit-common/emit-common :added "4.0"}
(fact "emits a string based on grammar"
  ^:hidden
  
  (emit-common '(add 1
                     (:int 1)
                     (add (new Class 1 2 3)))
               helper/+default+
               {})
  => "add(1,((int)1),add(new(Class,1,2,3)))")

(comment
  (emit-token :symbol 'for (merge helper/+default+
                                  {:reserved '{for {}}})
              {})
  => (throws))
