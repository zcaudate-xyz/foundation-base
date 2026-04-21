(ns std.lang.base.emit-common-test
  (:require [std.lang.base.emit-common :as common :refer :all]
            [std.lang.base.emit-helper :as helper]
            [std.string.prose :as prose])
  (:use code.test))

^{:refer std.lang.base.emit-common/with:explode :added "4.0"}
(fact "form to control `explode` option"
  (with:explode *explode*) => true)

^{:refer std.lang.base.emit-common/with-trace :added "4.0"}
(fact "form to control `trace` option"
  (with-trace *trace*) => true)

^{:refer std.lang.base.emit-common/with-compressed :added "3.0"}
(fact "formats without newlines and indents"
  (with-compressed *compressed*) => true)

^{:refer std.lang.base.emit-common/with-indent :added "3.0"}
(fact "adds indentation levels"

  (with-indent [10]
    *indent*)
  => 10)

^{:refer std.lang.base.emit-common/newline-indent :added "3.0"}
(fact "returns a newline with indent"

  (newline-indent)
  => "\n"

  (with-indent [2]
    (newline-indent))
  => "\n  ")

^{:refer std.lang.base.emit-common/emit-reserved-value :added "4.0"}
(fact "emits a reserved value"

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

  (emit-free-raw "." [1 2 3] helper/+default+ {})
  => "1.2.3")

^{:refer std.lang.base.emit-common/emit-free :added "4.0"}
(fact "emits string with multiline support"

  (emit-free " "
             '(:- 1 2 3 4)
             helper/+default+
             {})
  => "1 2 3 4")

^{:refer std.lang.base.emit-common/emit-comment :added "4.0"}
(fact "emits a comment"

  (emit-comment nil '(:# "This is a comment" A B 1) helper/+default+ {})
  => "// This is a comment A B 1"

  (emit-comment nil '(:# "This \nis \na comment" A B 1)
                helper/+default+ {})

  => (prose/|
      "// This "
      "// is "
      "// a comment A B 1"))

^{:refer std.lang.base.emit-common/emit-indent :added "4.0"}
(fact "emits an indented form"

  (emit-indent nil '(\| "This\nis\nan indented" A B 1)
               helper/+default+ {})
  => (prose/|
      "  This"
      "  is"
      "  an indented A B 1"))

^{:refer std.lang.base.emit-common/emit-macro :added "4.0"}
(fact "emits form"

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

  (emit-array [1 2 3] {} {})
  => '("1" "2" "3"))

^{:refer std.lang.base.emit-common/emit-wrappable? :added "4.0"}
(fact "checks if form if wrappable"

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

  (emit-squash nil '(:% 1 2 3 "hello")
               (merge helper/+default+
                      {})
               {})
  => "123\"hello\"")

^{:refer std.lang.base.emit-common/emit-wrapping :added "4.0"}
(fact "emits a potentially wrapped form"

  (emit-wrapping '(!= 1 x)
                 (merge helper/+default+
                        {:reserved {'!= {:emit :infix}}})
                 {})
  => "((!= 1 x))")

^{:refer std.lang.base.emit-common/wrapped-str :added "3.0"}
(fact "wrapped string using `:start` and `:end` keys of grammar"

  (wrapped-str "hello" [:data :map] helper/+default+)
  => "{hello}")

^{:refer std.lang.base.emit-common/emit-unit :added "4.0"}
(fact "emits a unit"

  (emit-unit {:default 'hello}
             '(:unit)
             helper/+default+
             {})
  => "hello")

^{:refer std.lang.base.emit-common/emit-internal :added "4.0"}
(fact "emits string within the form"

  (emit-internal '(% "hello")
                 helper/+default+
                 {})
  => "\"hello\"")

^{:refer std.lang.base.emit-common/emit-internal-str :added "4.0"}
(fact "emits internal string"

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

  (emit-pre "!" '[x] helper/+default+ {})
  => "!x")

^{:refer std.lang.base.emit-common/emit-post :added "3.0"}
(fact "emits string after the arg"

  (emit-post "--" '[x] helper/+default+ {})
  => "x--")

^{:refer std.lang.base.emit-common/emit-prefix :added "4.0"}
(fact "emits operator before the arg"

  (emit-prefix "hello" '[x] helper/+default+ {})
  => "hello x")

^{:refer std.lang.base.emit-common/emit-postfix :added "4.0"}
(fact  "emits operator before the arg"

  (emit-postfix "hello" '[x] helper/+default+ {})
  => "x hello")

^{:refer std.lang.base.emit-common/emit-infix :added "3.0"}
(fact "emits infix ops"

  (emit-infix "|" '[x y z] helper/+default+ {})
  => "x | y | z")

^{:refer std.lang.base.emit-common/emit-infix-default :added "3.0"}
(fact "emits infix with a default value"

  (emit-infix-default "/" '[x] 1 helper/+default+ {})
  => "1 / x")

^{:refer std.lang.base.emit-common/emit-infix-pre :added "3.0"}
(fact "emits infix with a default value"

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

  (emit-between ":" [1 2] helper/+default+ {})
  => "1:2")

^{:refer std.lang.base.emit-common/emit-bi :added "3.0"}
(fact "emits infix with two args"

  (emit-bi "==" '[x y] helper/+default+ {})
  => "x == y"

  (emit-bi "==" '[x y z] helper/+default+ {})
  => (throws))

^{:refer std.lang.base.emit-common/emit-assign :added "3.0"}
(fact "emits a setter expression"

  (emit-assign ":eq" '[x 1] helper/+default+ {})
  => "x :eq 1"

  (emit-assign "=" '[x 1] helper/+default+ {})
  => "x = 1"

  (emit-assign "=" '[x 1] helper/+default+ {})
  => "x = 1")

^{:refer std.lang.base.emit-common/emit-return-do :added "4.0"}
(fact "creates a return statement on `do` block"

  (emit-return-do
   [1 2 3] helper/+default+ {})
  => "(1 2 (return 3))")

^{:refer std.lang.base.emit-common/emit-return-base :added "4.0"}
(fact "return base type"

  (emit-return-base "break" [1] helper/+default+ {})
  => "break 1"

  (emit-return-base "return" [1] helper/+default+ {})
  => "return 1")

^{:refer std.lang.base.emit-common/emit-return :added "3.0"}
(fact "creates a return type statement"

  (emit-return "break" [1] helper/+default+ {})
  => "break 1"

  (emit-return "return" [1 2 3] helper/+default+ {})
  => (throws)

  (emit-return "return" [1 2 3] (assoc-in helper/+default+
                                          [:default :return :multi] true) {})
  => "return 1, 2, 3")

^{:refer std.lang.base.emit-common/emit-with-global :added "4.0"}
(fact "customisable emit function for global vars"

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

  (emit-symbol 'a
               {:token {:symbol {:emit-fn  (fn [sym _ _]
                                             (str sym 123))}}}
               {})
  => "a123")

^{:refer std.lang.base.emit-common/emit-token :added "3.0"}
(fact "customisable emit function for tokens"

  (emit-token :number 1 helper/+default+ {})
  => "1"

  (emit-token :string "1" {:token {:string {:quote :single}}} {})
  => "'1'"

  (emit-token :string "1" {:token {:string {:emit (fn [s _ _] (keyword s))}}} {})
  => :1)

^{:refer std.lang.base.emit-common/emit-with-decorate :added "4.0"}
(fact "customisable emit function for global vars"

  (emit-with-decorate nil '(!:decorate {:id 1} HELLO) {} {})
  => "HELLO")

^{:refer std.lang.base.emit-common/emit-with-uuid :added "4.0"}
(fact "injects uuid for testing"

  (emit-with-uuid nil '(!:uuid :hello :world) {} {})
  => "00000000-05e9-18d2-0000-000006c11b92"

  (emit-with-uuid nil '(!:uuid) {} {})
  => string?)

^{:refer std.lang.base.emit-common/emit-with-rand :added "4.0"}
(fact "injects uuid for testing"

  (read-string (emit-with-rand nil '(!:rand :int) {} {}))
  => integer?

  (read-string (emit-with-rand nil '(!:rand) {} {}))
  => float?)

^{:refer std.lang.base.emit-common/invoke-kw-parse :added "3.0"}
(fact "seperates standard and keyword arguments"

  (invoke-kw-parse [1 2 3 4 :name "hello" :foo "bar"])
  => '[(1 2 3 4)
       ((:name "hello") (:foo "bar"))])

^{:refer std.lang.base.emit-common/emit-invoke-kw-pair :added "3.0"}
(fact  "emits a kw argument pair"

  (emit-invoke-kw-pair [:name "hello"] helper/+default+
                       {})
  => "name=\"hello\"")

^{:refer std.lang.base.emit-common/emit-invoke-args :added "3.0"}
(fact "produces the string for invoke call"

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

  (emit-invoke-raw "-" '[abc] helper/+default+ {})
  => "-(abc)")

^{:refer std.lang.base.emit-common/emit-invoke-static :added "3.0"}
(fact "generates a static call, alternat"

  (emit-invoke-static '(:table/new "hello")
                      helper/+default+
                      {})
  => "table.new(\"hello\")")

^{:refer std.lang.base.emit-common/emit-invoke-typecast :added "3.0"}
(fact "generates typecast expression"

  (emit-invoke-typecast '(:int (:char 2))
                        helper/+default+
                        {})
  => "((int)(:char 2))")

^{:refer std.lang.base.emit-common/emit-invoke :added "3.0"}
(fact "general invoke call, incorporating keywords"

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

  (emit-new "new"
            '(String 1 2 3 4)
            helper/+default+
            {})
  => "new String(1,2,3,4)")

^{:refer std.lang.base.emit-common/emit-class-static-invoke :added "4.0"}
(fact "creates "

  (emit-class-static-invoke
   nil
   '(String "new" 1 2 3 4)
   helper/+default+
   {})
  => "String.new(1,2,3,4)")

^{:refer std.lang.base.emit-common/emit-index-entry :added "3.0"}
(fact "classifies the index entry"

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

  (emit-index nil '[x [hello] (world foo bar) baz] helper/+default+ {})
  => "x[hello].world(foo,bar).baz")

^{:refer std.lang.base.emit-common/emit-op :added "3.0"}
(fact "helper for the emit op"

  (emit-op :- '(:- "~~") (merge helper/+default+
                                {:reserved {:-  {:emit :free}}})
           {})
  => "~~")

^{:refer std.lang.base.emit-common/form-key :added "3.0"}
(fact "returns the key associated with the form"

  (form-key (first {:a 1}) {}) => [:map-entry :data nil]

  (form-key [] {}) => [:vector :data nil]

  (form-key () {}) => [:invoke :invoke nil])

^{:refer std.lang.base.emit-common/emit-common-loop :added "4.0"}
(fact "emits the raw string"

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
