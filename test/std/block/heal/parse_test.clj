(ns std.block.heal.parse-test
  (:use code.test)
  (:require [std.block.heal.parse :as parse]
            [std.string :as str]
            [std.lib :as h]))

^{:refer std.block.heal.parse/parse-delimiters :added "4.0"}
(fact "gets all the delimiters in the file"
  ^:hidden

  ;; Simple
  (parse/parse-delimiters
   "(+ 1 2 3)")
  => [{:char "(", :line 1, :col 1, :type :open, :style :paren}
      {:char ")", :line 1, :col 9, :type :close, :style :paren}]

  ;; In comment
  (parse/parse-delimiters
   "( ; )")
  => [{:char "(", :line 1, :col 1, :type :open, :style :paren}]

  ;; In comment with string
  (parse/parse-delimiters
   "; \"\n\"(")
  => []

  ;; In comment with 
  (parse/parse-delimiters
   "; \"\n\"\"(")
  => [{:char "(", :line 2, :col 3, :type :open, :style :paren}]
  

  ;; Escaped character
  (parse/parse-delimiters
   "\\(")
  => []

  ;; In string
  (parse/parse-delimiters
   "\"()\" ")
  => []

  ;; In multilined string
  (parse/parse-delimiters
   "\"(\n)\" ")
  => [])

^{:refer std.block.heal.parse/pair-delimiters :added "4.0"}
(fact "pairs the delimiters and annotates whether it's erroring"
  ^:hidden

  (parse/pair-delimiters
   (parse/parse-delimiters "[]]]][]]"))
  
  (parse/pair-delimiters
   (parse/parse-delimiters "[]"))
  => (contains-in
      [{:char "[", :line 1, :col 1, :type :open, :style :square, :depth 0, :correct? true, :pair-id 0}
       {:char "]", :line 1, :col 2, :type :close, :style :square, :depth 0, :correct? true, :pair-id 0}])
  
  
  (parse/pair-delimiters
   (parse/parse-delimiters "]"))
  => (contains-in
      [{:char "]", :line 1, :col 1, :type :close, :style :square, :depth -1, :correct? false}])

  
  (parse/pair-delimiters
   (parse/parse-delimiters "]]"))
  => (contains-in
      [{:char "]", :line 1, :col 1, :type :close, :style :square, :depth -1, :correct? false}
       {:char "]", :line 1, :col 2, :type :close, :style :square, :depth -2, :correct? false}])
  
  (parse/pair-delimiters
   (parse/parse-delimiters "[[[]"))
  => (contains-in
      [{:char "[", :line 1, :col 1, :type :open, :style :square, :depth 0, :correct? false}
       {:char "[", :line 1, :col 2, :type :open, :style :square, :depth 1, :correct? false}
       {:char "[", :line 1, :col 3, :type :open, :style :square, :depth 2, :correct? true, :pair-id 0}
       {:char "]", :line 1, :col 4, :type :close, :style :square, :depth 2, :correct? true, :pair-id 0}])
  
  (parse/pair-delimiters
   (parse/parse-delimiters "{)"))
  => (contains-in
      [{:char "{", :line 1, :col 1, :type :open, :style :curly, :depth 0, :correct? false, :pair-id 0}
       {:char ")", :line 1, :col 2, :type :close, :style :paren, :depth 0, :correct? false, :pair-id 0}])

  (parse/pair-delimiters
   (parse/parse-delimiters "
(defn
 (do
   (it)
 (this))"))
  => (contains-in
      [{:char "(", :line 2, :col 1, :type :open, :style :paren, :depth 0, :correct? false}
       {:char "(", :line 3, :col 2, :type :open, :style :paren, :depth 1, :correct? true, :pair-id 2}
       {:char "(", :line 4, :col 4, :type :open, :style :paren, :depth 2, :correct? true, :pair-id 0}
       {:char ")", :line 4, :col 7, :type :close, :style :paren, :depth 2, :correct? true, :pair-id 0}
       {:char "(", :line 5, :col 2, :type :open, :style :paren, :depth 2, :correct? true, :pair-id 1}
       {:char ")", :line 5, :col 7, :type :close, :style :paren, :depth 2, :correct? true, :pair-id 1}
       {:char ")", :line 5, :col 8, :type :close, :style :paren, :depth 1, :correct? true, :pair-id 2}]))

^{:refer std.block.heal.parse/parse :added "4.0"}
(fact "creates a parse function"
  ^:hidden
  
  (parse/parse "(()")
  => [{:char "(", :line 1, :col 1, :type :open, :style :paren, :depth 0, :index 0, :correct? false}
      {:correct? true, :index 1, :pair-id 0, :type :open, :style :paren, :line 1, :col 2, :depth 1, :char "("}
      {:correct? true, :index 2, :pair-id 0, :type :close, :style :paren, :line 1, :col 3, :depth 1, :char ")"}])

^{:refer std.block.heal.parse/print-delimiters :added "4.0"}
(fact "prints all the parsed carets"
  ^:hidden

  (h/with-out-str
    (parse/print-delimiters
     (slurp "test-data/std.block.heal/cases/001_basic.block")
     (parse/parse-delimiters
      (slurp "test-data/std.block.heal/cases/001_basic.block"))
     {:line-numbers true}))
  => string?)

^{:refer std.block.heal.parse/count-unescaped-quotes :added "4.0"}
(fact "counting unescaped quotes in a line"
  ^:hidden
  
  (parse/count-unescaped-quotes "")
  => 0
  
  (parse/count-unescaped-quotes "\"hello\"")
  => 2
  
  (parse/count-unescaped-quotes "\"hello \\\"world\\\"\"")
  => 2

  (parse/count-unescaped-quotes "\" \\\"  \"")  ;; escaped
  => 2

  (parse/count-unescaped-quotes "\" \\\"") ;; multiline
  => 1

  (parse/count-unescaped-quotes "\\\"\\\"") ;; for chars
  => 0
  
  (parse/count-unescaped-quotes "test\\")
  => 0

  (parse/count-unescaped-quotes "test\\\"")
  => 0)

^{:refer std.block.heal.parse/parse-lines :added "4.0"}
(fact "parse lines"
  ^:hidden

  (parse/parse-lines
   (str/join-lines
    ["(:? ()"
     "    ())"
     "    nil)"]))
  => [{:type :code,
    :line 1,
    :last-idx 5,
    :col 1,
    :char "(",
    :style :paren,
    :action :open}
   {:type :code,
    :line 2,
    :last-idx 6,
    :col 5,
    :char "(",
    :style :paren,
    :action :open}
   {:type :code, :line 3, :last-idx 7, :col 5, :char "n"}]

  (parse/parse-lines
   (str/join-lines
    ["(:? ()"
     "    ())"
     "    nil {})"]))
  => [{:type :code,
       :line 1,
       :last-idx 5,
       :col 1,
       :char "(",
       :style :paren,
       :action :open}
      {:type :code,
       :line 2,
       :last-idx 6,
       :col 5,
       :char "(",
       :style :paren,
       :action :open}
      {:type :code, :line 3, :last-idx 10, :col 5, :char "n"}]
  
  (def sample-code
    "
(defn my-function
  \"This is a docstring
   that spans multiple lines.\"
  [arg1 arg2] ; This is an inline comment
  (println \"Hello, world!\")
  ;; Another comment line
  (let [x 1
        y 2]
    (+ x y)))

;; Final comment
")

  (parse/parse-lines sample-code)
  => vector?
  

  
  (def another-sample
    "\"\"\"
This is a triple-quoted string
that acts as a multi-line comment or docstring in other languages but not clojure
\"\"\"
(def x 1) ; inline
;; a comment
\"single line string\"
")
  
  (parse/parse-lines another-sample)
  => vector?)

^{:refer std.block.heal.parse/is-open-heavy :added "4.0"}
(fact "checks if open delimiters dominate"
  ^:hidden
  
  (parse/is-open-heavy
   (parse/pair-delimiters
    (parse/parse-delimiters "(()")))
  => true

  (parse/is-open-heavy
   (parse/pair-delimiters
    (parse/parse-delimiters "(()))")))
  => false)

^{:refer std.block.heal.parse/is-balanced :added "4.0"}
(fact "checks if parens are balanced"
  ^:hidden
  
  (parse/is-balanced
   (parse/pair-delimiters
    (parse/parse-delimiters "{{])")))
  => true
  
  (parse/is-balanced
   (parse/pair-delimiters
    (parse/parse-delimiters "((])")))
  => true

  (parse/is-balanced
   (parse/pair-delimiters
    (parse/parse-delimiters "(])")))
  => false
  
  (parse/is-balanced
   (parse/pair-delimiters
    (parse/parse-delimiters "(()))")))
  => false)

^{:refer std.block.heal.parse/is-readable :added "4.0"}
(fact "checks if parens are readable"
  ^:hidden
  
  (parse/is-readable
   (parse/pair-delimiters
    (parse/parse-delimiters "{{])")))
  => false
  
  (parse/is-readable
   (parse/pair-delimiters
    (parse/parse-delimiters "((])")))
  => false

  (parse/is-readable
   (parse/pair-delimiters
    (parse/parse-delimiters "(])")))
  => false
  
  (parse/is-readable
   (parse/pair-delimiters
    (parse/parse-delimiters "(()))")))
  => false)

^{:refer std.block.heal.parse/is-close-heavy :added "4.0"}
(fact "checks if parens are close heavy"
  ^:hidden

  (parse/is-close-heavy
   (parse/parse "{{])"))
  => false
  
  (parse/is-close-heavy
   (parse/pair-delimiters
    (parse/parse-delimiters "((])")))
  => false

  (parse/is-close-heavy
   (parse/pair-delimiters
    (parse/parse-delimiters "(])")))
  => true
  
  (parse/is-close-heavy
   (parse/pair-delimiters
    (parse/parse-delimiters "(()))")))
  => true)

^{:refer std.block.heal.parse/make-delimiter-line-lu :added "4.0"}
(fact "creates a line lu"
  ^:hidden

  (parse/make-delimiter-line-lu
   (parse/pair-delimiters
    (parse/parse-delimiters "((\n)\n) \n)")))
  => {1 [{:correct? true, :index 0, :pair-id 1, :type :open, :style :paren, :line 1, :col 1, :depth 0, :char "("}
         {:correct? true, :index 1, :pair-id 0, :type :open, :style :paren, :line 1, :col 2, :depth 1, :char "("}],
      2 [{:correct? true, :index 2, :pair-id 0, :type :close, :style :paren, :line 2, :col 1, :depth 1, :char ")"}],
      3 [{:correct? true, :index 3, :pair-id 1, :type :close, :style :paren, :line 3, :col 1, :depth 0, :char ")"}],
      4 [{:char ")", :line 4, :col 1, :type :close, :style :paren, :depth -1, :correct? false, :index 4}]})

^{:refer std.block.heal.parse/make-delimiter-index-lu :added "4.0"}
(fact "creates the index lookup"
  ^:hidden

  (sort (keys (parse/make-delimiter-index-lu
               (parse/pair-delimiters
                (parse/parse-delimiters "((\n)\n) \n)")))))
  => '(0 1 2 3 4))

(comment

  (let [])
  
  
    
  )


^{:refer std.block.heal.parse/parse-lines-raw :added "4.0"}
(fact "helper for parse-lines"
  (parse/parse-lines-raw ["(+ 1 2)"])
  => [{:type :code :line 1 :last-idx 6 :col 1 :char "(" :style :paren :action :open}])
