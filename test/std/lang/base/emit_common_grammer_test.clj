(ns std.lang.base.emit-common-grammer-test
  (:use code.test)
  (:require [std.lang.base.emit-common :as common :refer :all]
            [std.lang.base.emit-data :as data :refer :all]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammer :as grammer]
            [std.lib :as h]))

(def +reserved+
  (-> (grammer/build)
      (grammer/to-reserved)))

(def +grammer+
  (grammer/grammer :test +reserved+ helper/+default+))

(fact:global
 {:setup    [(alter-var-root #'common/*emit-fn*
                             (fn [_]
                               data/test-data-loop))]
  :teardown [(alter-var-root #'common/*emit-fn*
                             (fn [_]
                               (fn [val _ _] (pr-str val))))]})

(comment
  (fact:global :setup)
  (fact:global :teardown))

^{:refer std.lang.base.emit-common/emit-comment :adopt true :added "4.0"}
(fact "emits a comment"
  ^:hidden
  
  (emit-comment nil '(:# "This is a comment" A B 1) +grammer+ {})
  => "// This is a comment A B 1")

^{:refer std.lang.base.emit-common/emit-macro :adopt true :added "4.0"}
(fact "emits form"
  ^:hidden
  
  (emit-macro :double-array
            '(double-array x y c)
            {:reserved
             {'double-array {:macro (fn [[_ & args]]
                                      (vec (concat args
                                                   args)))}}}
            {})
  => "xycxyc")

^{:refer std.lang.base.emit-common/emit-array :adopt true :added "4.0"}
(fact  "returns an array of emitted strings"
  ^:hidden

  (emit-array [1 2 3] {} {})
  => '("1" "2" "3"))

^{:refer std.lang.base.emit-common/emit-wrappable? :adopt true :added "4.0"}
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

^{:refer std.lang.base.emit-common/emit-wrapping :adopt true :added "4.0"}
(fact "emits a potentially wrapped form"

  (emit-wrapping '(not= 1 x)
                 +grammer+
                 {})
  => "(1 != x)")

^{:refer std.lang.base.emit-common/wrapped-str :adopt true :added "3.0"}
(fact "wrapped string using `:start` and `:end` keys of grammer"
  ^:hidden

  (wrapped-str "hello" [:data :map] +grammer+)
  => "{hello}")

^{:refer std.lang.base.emit-common/emit-pre :adopt true :added "3.0"}
(fact "emits string before the arg"
  ^:hidden

  (emit-pre "!" '[x] +grammer+ {})
  => "!x")

^{:refer std.lang.base.emit-common/emit-post :adopt true :added "3.0"}
(fact "emits string after the arg"
  ^:hidden

  (emit-post "--" '[x] +grammer+ {})
  => "x--")

^{:refer std.lang.base.emit-common/emit-infix :adopt true :added "3.0"}
(fact "emits infix ops"
  ^:hidden

  (emit-infix "|" '[x y z] +grammer+ {})
  => "x | y | z")

^{:refer std.lang.base.emit-common/emit-infix-default :adopt true :added "3.0"}
(fact "emits infix with a default value"
  ^:hidden

  (emit-infix-default "/" '[x] 1 +grammer+ {})
  => "1 / x")

^{:refer std.lang.base.emit-common/emit-infix-pre :adopt true :added "3.0"}
(fact "emits infix with a default value"
  ^:hidden

  (emit-infix-pre "-" '[x] +grammer+ {})
  => "-x")

^{:refer std.lang.base.emit-common/emit-infix-if-single :adopt true :added "4.0"}
(fact "checks for infix in single"

  (emit-infix-if '(:? true
                      x
                      y)
                 +grammer+
                 {})
  => "true ? x : y")

^{:refer std.lang.base.emit-common/emit-infix-if :adopt true :added "3.0"}
(fact "emits an infix if string"
  ^:hidden

  (emit-infix-if '(:? true x y)
                 +grammer+
                 {})
  => "true ? x : y"

  (emit-infix-if '(:? true x y)
                 +grammer+
                 {})
  => "true ? x : y"

  (emit-infix-if '(:? true x
                      :else y)
                 +grammer+
                 {})
  => "true ? x : y"

  (emit-infix-if '(:? true x
                      true y
                      true z
                      :else t)
                 +grammer+
                 {:reserved  {:?  {:emit :infix-if}}})
  => "true ? x : (true ? y : (true ? z : t))")

^{:refer std.lang.base.emit-common/emit-between :adopt true :added "3.0"}
(fact "emits the raw symbol between two elems"
  ^:hidden

  (emit-between ":" [1 2] +grammer+ {})
  => "1:2")

^{:refer std.lang.base.emit-common/emit-bi :adopt true :added "3.0"}
(fact "emits infix with two args"
  ^:hidden

  (emit-bi "==" '[x y] +grammer+ {})
  => "x == y"

  (emit-bi "==" '[x y z] +grammer+ {})
  => (throws))

^{:refer std.lang.base.emit-common/emit-assign :adopt true :added "3.0"}
(fact "emits a setter expression"
  ^:hidden
  
  (emit-assign ":eq" '[x 1] +grammer+ {})
  => "x :eq 1")

^{:refer std.lang.base.emit-common/emit-return :adopt true :added "3.0"}
(fact "creates a return type statement"
  ^:hidden

  (emit-return "break" [1] +grammer+ {})
  => "break 1")

^{:refer std.lang.base.emit-common/emit-map-key :adopt true :added "4.0"}
(fact "emits the map key"
  ^:hidden
  
  (emit-map-key 'hello
                +grammer+
                {})
  => "hello"

  (emit-map-key "hello"
                +grammer+
                {})
  => "\"hello\""

  (emit-map-key :hello
                (merge +grammer+
                       {:data {:map-entry  {:start ""  :end ""  :assign ":" :space "" :keyword :keyword}}})
                {})
  => (throws))

^{:refer std.lang.base.emit-common/emit-map-entry :adopt true :added "3.0"}
(fact "emits the map entry"
  ^:hidden

  (emit-map-entry [:hello "world"] +grammer+ {})
  => "\"hello\":\"world\""

  (emit-map-entry [:hello "world"]
                  (assoc-in +grammer+
                            [:data :map-entry :assign] " = ") {})
  => "\"hello\" = \"world\"")

^{:refer std.lang.base.emit-common/emit-coll-layout :adopt true :added "4.0"}
(fact "constructs the collection"
  ^:hidden
  
  (emit-coll-layout :vector 2 ["1" "2" "3"] +grammer+ {})
  => "[1,2,3]"
  
  (emit-coll-layout :vector 2 ["1" "\n2" "3"] +grammer+ {})
  => "[\n    1,\n    \n  2,\n    3\n  ]")

^{:refer std.lang.base.emit-common/emit-coll :adopt true :added "3.0"}
(fact "emits a collection"
  ^:hidden

  (emit-coll :vector [1 2 3] +grammer+ {})
  => "[1,2,3]"

  (emit-coll :set [1 2 3] +grammer+ {})
  => "(1,2,3)"
  
  (emit-coll :custom [1 2 3] (assoc-in +grammer+
                                       [:data :custom]
                                       {:start "<" :end ">" :space ""}) {})
  => "<1,2,3>")

^{:refer std.lang.base.emit-common/emit-data-standard :adopt true :added "4.0"}
(fact "emits either a custom string or default coll")

^{:refer std.lang.base.emit-common/emit-data :adopt true :added "3.0"}
(fact "main function for data forms"
  ^:hidden

  (emit-data :map {:a 1 :b 2} +grammer+ {})
  => "{\"a\":1,\"b\":2}"
  
  (emit-data :vector {:a 1 :b 2} +grammer+ {})
  => "[\"a\":1,\"b\":2]")

^{:refer std.lang.base.emit-common/emit-symbol-classify :adopt true :added "3.0"}
(fact "classify symbol given options"

  (emit-symbol-classify 't/hello {:module {:alias '{t table}}})
  => '[:alias table]

  (emit-symbol-classify 't.n/hello {:module {:alias '{t table}}})
  => '[:unknown t.n])

^{:refer std.lang.base.emit-common/emit-symbol-standard :adopt true :added "3.0"}
(fact "emits a standard symbol"

  (emit-symbol-standard 'print! +grammer+ {:layout :full})
  => "printf")

^{:refer std.lang.base.emit-common/emit-symbol :adopt true :added "4.0"}
(fact "emits symbol allowing for custom functions"
  ^:hidden
  
  (emit-symbol 'a
               {:token {:symbol {:emit-fn  (fn [sym _ _]
                                             (str sym 123))}}}
               {})
  => "a123")

^{:refer std.lang.base.emit-common/emit-token :adopt true :added "3.0"}
(fact "customisable emit function for tokens"
  ^:hidden

  (emit-token :number 1 +grammer+ {})
  => "1"

  (emit-token :string "1" {:token {:string {:quote :single}}} {})
  => "'1'"

  (emit-token :string "1" {:token {:string {:emit (fn [s _ _] (keyword s))}}} {})
  => :1)

^{:refer std.lang.base.emit-common/invoke-kw-parse :adopt true :added "3.0"}
(fact "seperates standard and keyword arguments"
  ^:hidden

  (invoke-kw-parse [1 2 3 4 :name "hello" :foo "bar"])
  => '[(1 2 3 4)
       ((:name "hello") (:foo "bar"))])

^{:refer std.lang.base.emit-common/emit-invoke-kw-pair :adopt true :added "3.0"}
(fact  "emits a kw argument pair"
  ^:hidden

  (emit-invoke-kw-pair [:name "hello"] +grammer+
                       {})
  => "name=\"hello\"")

^{:refer std.lang.base.emit-common/emit-invoke-args :adopt true :added "3.0"}
(fact "produces the string for invoke call"
  ^:hidden

  (emit-invoke-args [1 2 3 4 :name "hello" :foo "bar"]
                    +grammer+
                    {})
  => '("1" "2" "3" "4" "name=\"hello\"" "foo=\"bar\""))

^{:refer std.lang.base.emit-common/emit-invoke-layout :adopt true :added "4.0"}
(fact "layout for invoke blocks"

  (emit-invoke-layout ["ab\nc"
                       "de\nf"]
                      +grammer+ {})
  => "(ab\nc,de\nf)")

^{:refer std.lang.base.emit-common/emit-invoke-raw :adopt true :added "3.0"}
(fact "invoke call for reserved ops"
  ^:hidden

  (emit-invoke-raw "-" '[abc] +grammer+ {})
  => "-(abc)")

^{:refer std.lang.base.emit-common/emit-invoke-static :adopt true :added "3.0"}
(fact "generates a static call, alternat"
  ^:hidden

  (emit-invoke-static '(:table/new "hello")
                      +grammer+
                      {})
  => "table.new(\"hello\")")

^{:refer std.lang.base.emit-common/emit-invoke-typecast :adopt true :added "3.0"}
(fact "generates typecast expression"
  ^:hidden

  (emit-invoke-typecast '(:int (:char 2))
                        +grammer+
                        {})
  => "((int)((char)2))"

  (emit-invoke-typecast '(:char :int 2)
                        +grammer+
                        {})
  => "((char int)2)")

^{:refer std.lang.base.emit-common/emit-invoke :adopt true :added "3.0"}
(fact "general invoke call, incorporating keywords"
  ^:hidden

  (emit-invoke :invoke
               '(call "hello" (+ 1 2))
               +grammer+
               {})
  => "call(\"hello\",1 + 2)")

^{:refer std.lang.base.emit-common/emit-new :adopt true :added "3.0"}
(fact "invokes a constructor"
  ^:hidden

  (emit-new "new"
             '(String 1 2 3 4)
             +grammer+
            {})
  => "new String(1,2,3,4)")

^{:refer std.lang.base.emit-common/emit-raw :adopt true :added "4.0"}
(fact  "emits a raw value"
  ^:hidden

  (emit-free " "
             '(:- 1 2 3 4)
             +grammer+
             {})
  => "1 2 3 4")

^{:refer std.lang.base.emit-common/emit-index-entry :adopt true :added "3.0"}
(fact "classifies the index entry"
  ^:hidden

  (emit-index-entry 'hello +grammer+ {})
  => ".hello"

  (emit-index-entry [9] +grammer+ {})
  => "[9]"
  
  (emit-index-entry '(call 1 2 3) +grammer+ {})
  => ".call(1,2,3)")

^{:refer std.lang.base.emit-common/emit-index :adopt true :added "3.0"}
(fact "creates an indexed expression"
  ^:hidden

  (emit-index nil '[x [hello] (world foo bar) baz] +grammer+ {})
  => "x[hello].world(foo,bar).baz")

^{:refer std.lang.base.emit-common/emit-quote :adopt true :added "4.0"}
(fact "emits quoted"
  ^:hidden
  
  (emit-quote nil nil ''[1 2 3 4] +grammer+ {})
  => "1,2,3,4"

  (emit-quote nil nil ''(1 2 3 4) +grammer+ {})
  => "(1,2,3,4)"
  
  (emit-quote nil nil ''(1) +grammer+ {})
  => "(1)")

^{:refer std.lang.base.emit-common/emit-table-group :adopt true :added "4.0"}
(fact "gets all the table groups"

  (emit-table-group '[:a 1 :b 3 (:.. x) (:.. y)])
  => '[[:a 1] [:b 3] (:.. x) (:.. y)])

^{:refer std.lang.base.emit-common/emit-table :adopt true :added "4.0"}
(fact "emits the table"
  
  (emit-table nil nil '(tab :a 1 :b 3 (:.. x) (:.. y))
              +grammer+
              {})
  => "{\"a\":1,\"b\":3,...x,...y}")

^{:refer std.lang.base.emit-common/emit-op :adopt true :added "3.0"}
(fact "helper for the emit op"
  ^:hidden

  (emit-op :- '(:- "~~") (merge +grammer+
                                {:reserved {:-  {:emit :free}}})
           {})
  => "~~")

^{:refer std.lang.base.emit/form-key :adopt true :added "3.0"}
(fact "returns the key associated with the form"
  ^:hidden

  (form-key [] +grammer+) => [:vector :data nil]
  
  (form-key ''() +grammer+) => [:quote :statement nil]

  (form-key '(try) +grammer+) => [:try :block nil]

  (form-key \0 +grammer+) => [:char :token true])

^{:refer std.lang.base.emit-data/test-data-loop :adopt true :added "4.0"}
(fact "emits the raw string"
  ^:hidden
  
  (test-data-loop '(add [1 (:int 1)])
                        +grammer+
                        {})
  => "add([1,((int)1)])"
  
  (test-data-loop '(add [1 (:int 1)])
                        +grammer+
                        {})
  => "add([1,((int)1)])"

  
  (test-data-loop '['(1 \0)]
                        +grammer+
                        {})
  => "[(1,)]"

  (test-data-loop '['[1 \0]]
                        +grammer+
                        {})
  => "[1,]"

  (test-data-loop '(:- 1 2 3)
                        +grammer+
                        {})
  => "1 2 3"

  (test-data-loop '(:% 1 2 3)
                        +grammer+
                        {})
  => "123"

  
  (test-data-loop '(:% \0 1 . 2 \, \( \) \;)
                        +grammer+
                        {})
  => "1 2,();"

  (test-data-loop '(\\ 1 2 3 4)
                        +grammer+
                        {})
  => "1 2 3 4"

  (test-data-loop '(\\ 1 2 \\ 3 4\\ 5 6)
                        +grammer+
                        {})
  => "1 2\n3 4\n5 6"

  (test-data-loop '(:# 1 2 3)
                        +grammer+
                        {})
  => "// 1 2 3"

  (test-data-loop '(:# (\\ 1 2
                              \\ 3 4))
                        +grammer+
                        {})
  
  (test-data-loop '(:# (\\ 1 2
                              \\ 3 4
                              \\ 5 6))
                        +grammer+
                        {})
  => "// 1 2\n// 3 4\n// 5 6"

  (test-data-loop '(:# (:# (\\ 1 2
                                  \\ 3 4
                                  \\ 5 6)))
                        +grammer+
                        {})
  => "// // 1 2\n// // 3 4\n// // 5 6"
  
  (test-data-loop '(:- \\ \^ \, \: \; \( \) \[ \] \{ \} \" \' \@ \~ \`)
                        +grammer+
                        {})
  => "\n^,:;()[]{}\"'@~`"
  

  (test-data-loop '(\* hello world \;
                          \* hello world \;)
                        +grammer+
                        {})
  => "hello world;\nhello world;")

^{:refer std.lang.base.emit-data/test-data-emit :adopt true :added "4.0"}
(fact "emits a string based on grammer"
  ^:hidden
  
  (test-data-emit '(add [1 (:int 1)]
                          (add [(hello 1 \0)
                                {:a 1 :b [1 2 3]}]))
                    +grammer+
                    {})
  => "add([1,((int)1)],add([hello(1,),{\"a\":1,\"b\":[1,2,3]}]))")

(comment
  (./import)
  (emit-token :symbol 'try +grammer+
              {})
  => (throws))
  
