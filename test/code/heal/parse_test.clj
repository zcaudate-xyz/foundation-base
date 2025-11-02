(ns code.heal.parse-test
  (:use code.test)
  (:require [code.heal.parse :as parse]
            [std.lib :as h]))

^{:refer code.heal.parse/parse-delimiters :added "4.0"}
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

^{:refer code.heal.parse/print-delimiters :added "4.0"}
(fact "prints all the parsed carets"
  ^:hidden

  (std.concurrent.print/with-out-str
    (parse/print-delimiters
     (h/sys:resource-content "code/heal/cases/001_basic.block")
     (parse/parse-delimiters
      (h/sys:resource-content "code/heal/cases/001_basic.block"))
     {:line-numbers true}))
  => string?)

^{:refer code.heal.parse/pair-delimiters :added "4.0"}
(fact "pairs the delimiters and annotates whether it's erroring"
  ^:hidden
  
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

^{:refer code.heal.parse/make-delimiter-lu :added "4.0"}
(fact "creates a delimiter lookup "
  ^:hidden
  
  (parse/make-delimiter-lu
   (parse/pair-delimiters
    (parse/parse-delimiters "
(defn
 (do
   (it)
 (this))")))
  => map?)

(comment

  (let [])
  
  
    
  )
