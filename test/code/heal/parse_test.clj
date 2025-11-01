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
  => '({:char "[", :line 1, :col 1, :type :open, :style :square, :level 0, :correct? true, :pair-id 0}
       {:char "]", :line 1, :col 2, :type :close, :style :square, :level 0, :correct? true, :pair-id 0})

  (parse/pair-delimiters
   (parse/parse-delimiters "[[[]"))
  => '({:char "[", :line 1, :col 1, :type :open, :style :square, :level 0, :correct? false}
       {:char "[", :line 1, :col 2, :type :open, :style :square, :level 1, :correct? false}
       {:char "[", :line 1, :col 3, :type :open, :style :square, :level 2, :correct? true, :pair-id 0}
       {:char "]", :line 1, :col 4, :type :close, :style :square, :level 2, :correct? true, :pair-id 0})
  
  (parse/pair-delimiters
   (parse/parse-delimiters "{)"))
  => '({:char "{", :line 1, :col 1, :type :open, :style :curly, :level 0, :correct? false, :pair-id 0}
       {:char ")", :line 1, :col 2, :type :close, :style :paren, :level 0, :correct? false, :pair-id 0}))

^{:refer code.heal.parse/group-delimiter-indentation :added "4.0"}
(fact "groups the open delimiters by their indentation"
  ^:hidden
  
  (parse/group-delimiter-indentation
   (parse/pair-delimiters
    (parse/parse-delimiters "{)")))
  => {1 [{:char "{", :line 1, :col 1, :type :open, :style :curly, :level 0, :correct? false, :pair-id 0}]})

^{:refer code.heal.parse/parse-lines :added "4.0"}
(fact "parses the code to get line information"
  ^:hidden

  (parse/parse-lines
   (h/sys:resource-content "code/heal/cases/001_basic.block"))
  (parse/parse-lines
   (h/sys:resource-content "code/heal/cases/002_complex.block"))
  
  )
