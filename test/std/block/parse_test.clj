(ns std.block.parse-test
  (:use code.test)
  (:require [std.block.parse :refer :all]
            [std.block.base :as base]
            [std.block.reader :as reader]))

^{:refer std.block.parse/read-dispatch :added "3.0"}
(fact "dispatches on first symbol"

  (read-dispatch \tab)
  => :void

  (read-dispatch (first "#"))
  => :hash)

^{:refer std.block.parse/-parse :added "3.0"}
(fact "extendable parsing function"

  (base/block-info (-parse (reader/create ":a")))
  => {:type :token, :tag :keyword, :string ":a", :height 0, :width 2}
  
  (base/block-info (-parse (reader/create "\"\\\\n\"")))
  => {:type :token, :tag :string, :string "\"\\\\n\"", :height 0, :width 5}
  
  (base/block-info (-parse (reader/create "\"\\n\"")))
  => {:type :token, :tag :string, :string "\"\\n\"", :height 0, :width 4}

  (base/block-info (-parse (reader/create "\"\n\"")))
  => {:type :token, :tag :string, :string "\"\n\"", :height 1, :width 1}


  (base/block-info (-parse (reader/create (slurp "test-data/std.block/cases/001-newlines.example"))))
  => {:type :void, :tag :linebreak, :string "\n", :height 1, :width 0}

  (base/block-info (-parse (reader/create (slurp "test-data/std.block/cases/002-string-rep.example"))))
  => {:type :token, :tag :string, :string "\"\n\n\"", :height 2, :width 1}

  (base/block-info (-parse (reader/create (slurp "test-data/std.block/cases/003-newlines-printed.example"))))
  => {:type :token, :tag :string, :string "\"\\n\"", :height 0, :width 4}
  
  (base/block-info (-parse (reader/create (slurp "test-data/std.block/cases/004-newline-with.example"))))
  => {:type :token, :tag :string, :string "\"\\n\n\"", :height 1, :width 1})

^{:refer std.block.parse/parse-void :added "3.0"}
(fact "reads a void block from reader"

  (->> (reader/read-repeatedly (reader/create " \t\n\f")
                               parse-void
                               eof-block?)
       (take 5)
       (map str))
  => ["␣" "\\t" "\\n" "\\f"])

^{:refer std.block.parse/parse-comment :added "3.0"}
(fact "creates a comment"

  (-> (reader/create ";this is a comment")
      parse-comment
      (base/block-info))
  => {:type :comment, :tag :comment, :string ";this is a comment", :height 0, :width 18})

^{:refer std.block.parse/parse-token :added "3.0"}
(fact "reads token block from the reader"

  (-> (reader/create "abc")
      (parse-token)
      (base/block-value))
  => 'abc

  (-> (reader/create "3/5")
      (parse-token)
      (base/block-value))
  => 3/5)

^{:refer std.block.parse/parse-keyword :added "3.0"}
(fact "reads a keyword block from the reader"

  (-> (reader/create ":a/b")
      (parse-keyword)
      (base/block-value))
  => :a/b

  (-> (reader/create "::hello")
      (parse-keyword)
      (base/block-value))
  => (keyword ":hello"))

^{:refer std.block.parse/parse-reader :added "3.0"}
(fact "reads a :char block from the reader" ^:hidden

  (-> (reader/create "\\c")
      (parse-reader)
      (base/block-info))
  => (contains {:type :token, :tag :char, :string "\\c"}))

^{:refer std.block.parse/read-string-data :added "3.0"}
(fact "reads string data from the reader"
  
  (read-string-data (reader/create "\"hello\""))
  => "hello"
  
  (read-string-data (reader/create "\"\\\"hello\\\"\""))
  => "\\\"hello\\\""
  
  (read-string-data (reader/create (slurp "test-data/std.block/cases/001-newlines.example")))
  => (throws)

  (read-string-data (reader/create (slurp "test-data/std.block/cases/002-string-rep.example")))
  => "\n\n"

  (read-string-data (reader/create (slurp "test-data/std.block/cases/003-newlines-printed.example")))
  => "\\n"
  
  (read-string-data (reader/create (slurp "test-data/std.block/cases/004-newline-with.example")))
  => "\\n\n")

^{:refer std.block.parse/eof-block? :added "3.0"}
(fact "checks if block is of tag :eof"

  (eof-block? (-parse (reader/create "")))
  => true)

^{:refer std.block.parse/delimiter-block? :added "3.0"}
(fact "checks if block is of tag :delimiter"

  (delimiter-block?
   (binding [*end-delimiter* (first "}")]
     (-parse (reader/create "}"))))
  => true)

^{:refer std.block.parse/read-whitespace :added "3.0"}
(fact "reads whitespace blocks as vector"

  (count (read-whitespace (reader/create "   ")))
  => 3)

^{:refer std.block.parse/parse-non-expressions :added "3.0"}
(fact "parses whitespace until next token"

  (str (parse-non-expressions (reader/create " \na")))
  => "[(␣ \\n) a]")

^{:refer std.block.parse/read-start :added "3.0"}
(fact "helper to verify that the beginning has been read"

  (read-start (reader/create "~@") "~#")
  => (throws))

^{:refer std.block.parse/read-collection :added "3.0"}
(fact "reads all children, taking a delimiter as option"

  (->> (read-collection (reader/create "(1 2 3 4 5)") "(" (first ")"))
       (apply str))
  => "1␣2␣3␣4␣5")

^{:refer std.block.parse/read-cons :added "3.0"}
(fact "helper method for reading "
  (->> (read-cons (reader/create "@hello") "@")
       (map base/block-string))
  => '("hello")

  (->> (read-cons (reader/create "^hello {}") "^" 2)
       (map base/block-string))
  => '("hello" " " "{}"))

^{:refer std.block.parse/parse-collection :added "3.0"}
(fact "parses a collection"

  (-> (parse-collection (reader/create "#(+ 1 2 3 4)") :fn)
      (base/block-value))
  => '(fn* [] (+ 1 2 3 4))

  (-> (parse-collection (reader/create "(1 2 3 4)") :list)
      (base/block-value))
  => '(1 2 3 4)

  (-> (parse-collection (reader/create "[1 2 3 4]") :vector)
      (base/block-value))
  => [1 2 3 4]

  (-> (parse-collection (reader/create "{1 2 3 4}") :map)
      (base/block-value))
  => {1 2, 3 4}

  (-> (parse-collection (reader/create "#{1 2 3 4}") :set)
      (base/block-value))
  => #{1 4 3 2})

^{:refer std.block.parse/parse-cons :added "3.0"}
(fact "parses a cons"

  (-> (parse-cons (reader/create "~hello") :unquote)
      (base/block-value))
  => '(unquote hello)

  (-> (parse-cons (reader/create "~@hello") :unquote-splice)
      (base/block-value))
  => '(unquote-splicing hello)

  (-> (parse-cons (reader/create "^tag {:a 1}") :meta)
      (base/block-value)
      ((juxt meta identity)))
  => [{:tag 'tag} {:a 1}]

  (-> (parse-cons (reader/create "@hello") :deref)
      (base/block-value))
  => '(deref hello)

  (-> (parse-cons (reader/create "`hello") :syntax)
      (base/block-value))
  => '(quote std.block.parse-test/hello))

^{:refer std.block.parse/parse-unquote :added "3.0"}
(fact "parses a block starting with `~` from the reader"

  (-> (parse-unquote (reader/create "~hello"))
      (base/block-value))
  => '(unquote hello)

  (-> (parse-unquote (reader/create "~@hello"))
      (base/block-value))
  => '(unquote-splicing hello))

^{:refer std.block.parse/parse-select :added "3.0"}
(fact "parses a block starting with `#?` from the reader"

  (-> (parse-select (reader/create "#?(:cljs a)"))
      (base/block-value))
  => '(? {:cljs a})

  (-> (parse-select (reader/create "#?@(:cljs a)"))
      (base/block-value))
  => '(?-splicing {:cljs a}))

^{:refer std.block.parse/parse-hash-uneval :added "3.0"}
(fact "parses the hash-uneval string"

  (str (parse-hash-uneval (reader/create "#_")))
  => "#_")

^{:refer std.block.parse/parse-hash-cursor :added "3.0"}
(fact "parses the hash-cursor string"

  (str (parse-hash-cursor (reader/create "#|")))
  => "|")

^{:refer std.block.parse/parse-hash :added "3.0"}
(fact "parses a block starting with `#` from the reader"

  (-> (parse-hash (reader/create "#{1 2 3}"))
      (base/block-value))
  => #{1 2 3}

  (-> (parse-hash (reader/create  "#(+ 1 2)"))
      (base/block-value))
  => '(fn* [] (+ 1 2)) ^:hidden

  (-> (parse-hash (reader/create  "#\"hello\""))
      (base/block-value))
  => #"hello"

  (-> (parse-hash (reader/create  "#^hello {}"))
      (base/block-value))
  => (with-meta {} {:tag 'hello})

  (-> (parse-hash (reader/create  "#'hello"))
      (base/block-value))
  => '(var hello)

  (-> (parse-hash (reader/create  "#=(list 1 2 3)"))
      (base/block-value))
  => '(1 2 3)

  (-> (parse-hash (reader/create  "#?(:clj true)"))
      (base/block-value))
  => '(? {:clj true})

  (-> (parse-hash (reader/create  "#?@(:clj [1 2 3])"))
      (base/block-value))
  => '(?-splicing {:clj [1 2 3]})

  (-> (parse-hash (reader/create  "#:hello {:a 1 :b 2}"))
      (base/block-value))
  => #:hello{:b 2, :a 1}

  (-> (parse-hash (reader/create  "#inst \"2018-08-06T06:01:40.682-00:00\""))
      (base/block-value))
  => #inst "2018-08-06T06:01:40.682-00:00")

^{:refer std.block.parse/parse-string :added "3.0"}
(fact "parses a block from a string input"

  (-> (parse-string "#(:b {:b 1})")
      (base/block-value))
  => '(fn* [] ((keyword "b") {(keyword "b") 1})))

^{:refer std.block.parse/parse-root :added "3.0"}
(fact "parses a root"

  (str (parse-root "a b c"))
  => "a b c")

^{:refer std.block.parse/parse-first :added "4.0"}
(fact "gets the first element in root"
  
  (str (parse-first "a b c"))
  => "a")

(comment

  (./run '[std.block]))


