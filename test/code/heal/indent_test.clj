(ns code.heal.indent-test
  (:use code.test)
  (:require [code.heal.parse :as parse]
            [code.heal.indent :as indent]
            [std.string :as str]
            [std.lib :as h]))

^{:refer code.heal.indent/flag-close-heavy-function :added "4.0"}
(fact "flags a function if at a different index"
  ^:hidden
  
  (indent/flag-close-heavy-function
   {:correct? true, :index 2, :pair-id 1, :type :open, :style :paren, :line 4, :col 3, :depth 0, :char "("}
   {:depth 0 :col 1 :index 0})
  => [{:correct? true, :index 2, :pair-id 1, :type :open, :style :paren, :line 4, :col 3, :depth 0, :char "("}
      true])

^{:refer code.heal.indent/flag-close-heavy-single :added "4.0"
  :setup [(def ^:dynamic *dlma*
            (parse/parse
             (str/join-lines
              [""
               "(fact )"
               " "
               "  (this))"])))]}
(fact "flags when there is open delimiter of the same depth with extra indentation"
  ^:hidden
  
  (indent/flag-close-heavy-single
   *dlma*
   (get *dlma* 0))
  => [{:correct? true, :index 2, :pair-id 1, :type :open, :style :paren, :line 4, :col 3, :depth 0, :char "("}])

^{:refer code.heal.indent/flag-close-heavy :added "4.0"}
(fact "finds open delimiters that have closed too early"
  ^:hidden
  
  (indent/flag-close-heavy
   (parse/parse "
(fact )
 
  (this))"))
  => [[0 [{:correct? true, :index 2, :pair-id 1, :type :open, :style :paren, :line 4, :col 3, :depth 0, :char "("}]]]
  
  (indent/flag-close-heavy
   (parse/parse "
(fact ))
 
  (this))"))
  => [[0 [{:correct? true, :index 2, :pair-id 1, :type :open, :style :paren, :line 4, :col 3, :depth -1, :char "("}]]])

^{:refer code.heal.indent/flag-open-heavy-function :added "4.0"
  :setup [(def ^:dynamic *dlm4*
            (parse/pair-delimiters
             (parse/parse-delimiters (slurp "test-data/code.heal/cases/004_shorten.block"))))]}
(fact "flags the delimiter if there are any discrepancies"
  ^:hidden

  (indent/flag-open-heavy-single
   *dlm4*
   (parse/make-delimiter-line-lu *dlm4*)
   (get *dlm4* 177))
  => [{:char "[", :line 59, :col 13, :type :open, :style :square, :depth 10, :correct? false, :index 140}
      {:correct? true, :index 136, :pair-id 64, :type :open, :style :square, :line 57, :col 13, :depth 10, :char "["}
      {:char "[", :line 56, :col 12, :type :open, :style :square, :depth 9, :correct? false, :index 133}])

^{:refer code.heal.indent/flag-open-heavy-single :added "4.0"
  :setup [(def ^:dynamic *dlm1*
            (parse/pair-delimiters
             (parse/parse-delimiters "
(defn
 (do
   (it
 (this))")))]}
(fact "finds previous index discrepancies given indent layout"
  ^:hidden
  
  (indent/flag-open-heavy-single
   *dlm1*
   (parse/make-delimiter-line-lu *dlm1*)
   (get *dlm1* 3))
  => [{:correct? true, :index 2, :pair-id 1, :type :open, :style :paren, :line 4, :col 4, :depth 2, :char "("}
      {:char "(", :line 3, :col 2, :type :open, :style :paren, :depth 1, :correct? false, :index 1}]
  
  
  (def ^:dynamic *dlm4*
    (parse/pair-delimiters
     (parse/parse-delimiters (slurp "test-data/code.heal/cases/002_complex.block")))))

^{:refer code.heal.indent/flag-open-heavy-raw :added "4.0"}
(fact "finds all discrepancies given some code"
  ^:hidden

  (def ^:dynamic *dlmx*
    (parse/pair-delimiters
    (parse/parse-delimiters "
[
 [
  [")))
  (indent/flag-open-heavy-raw
   *dlmx*
   (parse/make-delimiter-line-lu *dlmx*))
  => []

  (def ^:dynamic *dlmy*
    (parse/pair-delimiters
    (parse/parse-delimiters "
[
 [
  [
[]
")))
  (indent/flag-open-heavy-raw
   *dlmy*
   (parse/make-delimiter-line-lu *dlmy*))
  => [[3 [{:char "[", :line 4, :col 3, :type :open, :style :square, :depth 2, :correct? false, :index 2}
          {:char "[", :line 3, :col 2, :type :open, :style :square, :depth 1, :correct? false, :index 1}
          {:char "[", :line 2, :col 1, :type :open, :style :square, :depth 0, :correct? false, :index 0}]]]
  
  (def ^:dynamic *dlm0*
    (parse/pair-delimiters
     (parse/parse-delimiters "
(defn
 (do
   (it)
 (this))")))
  
  (indent/flag-open-heavy-raw
   *dlm0*
   (parse/make-delimiter-line-lu *dlm0*))
  => [[4 [{:correct? true, :index 1, :pair-id 2, :type :open, :style :paren, :line 3, :col 2, :depth 1, :char "("}]]]


  (def ^:dynamic *dlm0a*
    (parse/pair-delimiters
    (parse/parse-delimiters "
(defn
 (do
   (it)
 (this)
 (this))")))

  (indent/flag-open-heavy-raw
      *dlm0a*
      (parse/make-delimiter-line-lu *dlm0a*))
  => [[6 [{:correct? true, :index 1, :pair-id 3, :type :open, :style :paren, :line 3, :col 2, :depth 1, :char "("}]]
      [4 [{:correct? true, :index 1, :pair-id 3, :type :open, :style :paren, :line 3, :col 2, :depth 1, :char "("}]]]
  
  (def ^:dynamic *dlm1*
    (parse/pair-delimiters
     (parse/parse-delimiters "
(defn
 (do
   (it
 (this))")))
  
  (indent/flag-open-heavy-raw
   *dlm1*
   (parse/make-delimiter-line-lu *dlm1*))
  => [[3 [{:correct? true, :index 2, :pair-id 1, :type :open, :style :paren, :line 4, :col 4, :depth 2, :char "("}
          {:char "(", :line 3, :col 2, :type :open, :style :paren, :depth 1, :correct? false, :index 1}]]]
  
  (def ^:dynamic *dlm2*
    (parse/pair-delimiters
    (parse/parse-delimiters "
[:h 
 [
  [
   [
    [
     []]]
 []")))
  
  (indent/flag-open-heavy-raw
   *dlm2*
   (parse/make-delimiter-line-lu *dlm2*))
  => [[9 [{:char "[", :line 4, :col 3, :type :open, :style :square, :depth 2, :correct? false, :index 2}
          {:char "[", :line 3, :col 2, :type :open, :style :square, :depth 1, :correct? false, :index 1}]]]
  
  (def ^:dynamic *dlm3*
    (parse/pair-delimiters
     (parse/parse-delimiters (slurp "test-data/code.heal/cases/001_basic.block"))))
  
  (indent/flag-open-heavy-raw
   *dlm3*
   (parse/make-delimiter-line-lu *dlm3*))
  => []

  (def ^:dynamic *dlm4*
    (parse/pair-delimiters
     (parse/parse-delimiters (slurp "test-data/code.heal/cases/002_complex.block"))))
  
  (indent/flag-open-heavy-raw
   *dlm4*
   (parse/make-delimiter-line-lu *dlm4*))
  => vector?)

^{:refer code.heal.indent/flagged-candidates-merge-common :added "4.0"}
(fact "merges all common"
  ^:hidden
  
  (indent/flagged-candidates-merge-common
   (indent/flag-open-heavy-raw
    *dlm4*
    (parse/make-delimiter-line-lu *dlm4*)))
  => vector?)

^{:refer code.heal.indent/flagged-candidates-filter-run :added "4.0"}
(fact "cuts off all potentially difficult locations"
  ^:hidden
  
  (indent/flagged-candidates-filter-run
   (indent/flag-open-heavy-raw
    *dlm4*
    (parse/make-delimiter-line-lu *dlm4*))
   {:limit 2})
  => vector?)

^{:refer code.heal.indent/flagged-candidates-invert-lookup :added "4.0"}
(fact "inverts a lookup"
  ^:hidden

  (indent/flagged-candidates-invert-lookup
   (indent/flag-open-heavy-raw
    *dlm4*
    (parse/make-delimiter-line-lu *dlm4*)))
  => map?)

^{:refer code.heal.indent/flag-open-heavy :added "4.0"}
(fact "combines discrepancies that are the same"
  ^:hidden
  
  (def ^:dynamic *dlm0a*
    (parse/pair-delimiters
     (parse/parse-delimiters "
(defn
 (do
   (it)
 (this)
 (this))")))
  
  (indent/flag-open-heavy *dlm0a*)
  => [[4 [{:correct? true, :index 1, :pair-id 3, :type :open, :style :paren, :line 3, :col 2, :depth 1, :char "("}]]]

  (def ^:dynamic *dlm4*
    (parse/pair-delimiters
     (parse/parse-delimiters (slurp "test-data/code.heal/cases/002_complex.block"))))
  
  (count
   (indent/flag-open-heavy-raw
    *dlm4*
    (parse/make-delimiter-line-lu *dlm4*)))
  => 22
  
  (first
   (indent/flag-open-heavy
    *dlm4*))
  => '[177 ({:char "[", :line 59, :col 13, :type :open, :style :square, :depth 10, :correct? false, :index 140}
            {:correct? true, :index 136, :pair-id 64, :type :open, :style :square, :line 57, :col 13, :depth 10, :char "["}
            {:char "[", :line 56, :col 12, :type :open, :style :square, :depth 9, :correct? false, :index 133})]
  
  (count
   (second
    (last
     (indent/flag-open-heavy
      *dlm4*))))
  => 4


  (map (comp count second)
       (indent/flag-open-heavy
        *dlm4*))
  => '(3 1 5 19 3 2 1 6 1 6 1 153 4)
  
  (first (last
          (indent/flag-open-heavy
           *dlm4*)))
  => 993

  (count
   (indent/flag-open-heavy
    *dlm4*))
  => 13)

^{:refer code.heal.indent/find-indent-last-close :added "4.0"}
(fact "finds the last close delimiter"
  ^:hidden
  
  (indent/find-indent-last-close
   (parse/pair-delimiters
    (parse/parse-delimiters "
(defn
 (do
   (it)
 (this))"))
   4 1)
  => {:correct? true, :index 3, :pair-id 0, :type :close, :style :paren, :line 4, :col 7, :depth 2, :char ")"}


  (indent/find-indent-last-close
   (parse/pair-delimiters
    (parse/parse-delimiters "
(defn
 (do
   (it
 (this))"))
   3 2) => nil
  
  (indent/find-indent-last-close
   (parse/pair-delimiters
    (parse/parse-delimiters "
[:h 
 [[[[[]
    ]]
 []"))
   9 1)
  => {:correct? true, :index 8, :pair-id 2, :type :close, :style :square, :line 4, :col 6, :depth 3, :char "]"})

^{:refer code.heal.indent/build-insert-edit :added "4.0"
  :setup [(def ^:dynamic *dlm4*
            (parse/parse (slurp "test-data/code.heal/cases/002_complex.block")))]}
(fact "constructs a single edit"

  )

^{:refer code.heal.indent/build-insert-edits :added "4.0"
  :setup [(def ^:dynamic *dlm4*
            (parse/parse (slurp "test-data/code.heal/cases/002_complex.block")))]}
(fact "builds a list of edits to be made to a "
  ^:hidden
  
  (indent/build-insert-edits
   *dlm4*
   (indent/flagged-candidates-filter-run
    (indent/flag-open-heavy
     *dlm4*))
   (slurp "test-data/code.heal/cases/002_complex.block"))
  => '({:action :insert, :line 110, :col 68, :new-char "]"}
       {:action :insert, :line 276, :col 96, :new-char "]"}
   {:action :insert, :line 338, :col 90, :new-char "]"}
       {:action :insert, :line 396, :col 234, :new-char "]"})
  


  (def sample-do "
(defn
  (do
    (it

  (this)
  (this))")
  
  (indent/build-insert-edits
   (parse/parse sample-do)
   (indent/flagged-candidates-filter-run
    (indent/flag-open-heavy
     (parse/parse sample-do)))
   sample-do)
  => '({:action :insert, :line 4, :col 7, :new-char "))"}))

^{:refer code.heal.indent/build-remove-edits :added "4.0"}
(fact "builds a list of remove edits"
  ^:hidden
  
  (def +sample+ "(((
(fact)
  (hello))")
  
  (indent/build-remove-edits
   (parse/parse +sample+)
   (indent/flag-close-heavy
    (parse/parse +sample+)))
  => '({:action :remove, :line 2, :col 6}))















;;;
;;
;;
;;

(comment
  (map (comp count second)
       (indent/flag-open-heavy
        *dlm4*))
  *dlm4*
  
  (def ^:dynamic *dlm4*
    (parse/pair-delimiters
     (parse/parse-delimiters (slurp "test-data/code.heal/cases/004_shorten.block"))))
  
  (first
   (second
    (last
     (indent/flag-open-heavy
      *dlm4*))))
  (def ^:dynamic *dlm4*
    (parse/pair-delimiters
     (parse/parse-delimiters )))
  


  
  
  (first
   (indent/flag-open-heavy
    *dlm4*))
  => [177 [{:char "[", :line 59, :col 13, :type :open, :style :square, :depth 10, :correct? false, :index 140} {:correct? true, :index 136, :pair-id 64, :type :open, :style :square, :line 57, :col 13, :depth 10, :char "["} {:char "[", :line 56, :col 12, :type :open, :style :square, :depth 9, :correct? false, :index 133}]]

  (get *dlm4* 177)
  => {:correct? true, :index 177, :pair-id 83, :type :open, :style :square, :line 72, :col 12, :depth 11, :char "["}
  
  (= (indent/find-indent-last-close
      *dlm4*
      (get *dlm4* 140)
      177)

     (indent/find-indent-last-close
      *dlm4*
      (get *dlm4* 136)
      177)
     (indent/find-indent-last-close
      *dlm4*
      (get *dlm4* 133)
      177))
  => true

  (indent/find-indent-last-close
   *dlm4*
   (get *dlm4* 896)
   987)
  => {:correct? true, :index 986, :pair-id 480, :type :close, :style :square, :line 410, :col 51, :depth 25, :char "]"}

  )

