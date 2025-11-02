(ns code.heal.indent-test
  (:use code.test)
  (:require [code.heal.parse :as parse]
            [code.heal.indent :as indent]
            [std.lib :as h]))

^{:refer code.heal.indent/flag-indent-flag-function :added "4.0"
  :setup [(def ^:dynamic *dlm4*
            (parse/pair-delimiters
             (parse/parse-delimiters (h/sys:resource-content "code/heal/cases/004_shorten.block"))))]}
(fact "flags the delimiter if there are any discrepancies"
  ^:hidden

  (indent/flag-indent-discrepancies-single
   *dlm4*
   (parse/make-delimiter-line-lu *dlm4*)
   (get *dlm4* 177))
  => [{:char "[", :line 59, :col 13, :type :open, :style :square, :depth 10, :correct? false, :index 140}
      {:correct? true, :index 136, :pair-id 64, :type :open, :style :square, :line 57, :col 13, :depth 10, :char "["}
      {:char "[", :line 56, :col 12, :type :open, :style :square, :depth 9, :correct? false, :index 133}])

^{:refer code.heal.indent/flag-indent-discrepancies-single :added "4.0"
  :setup [(def ^:dynamic *dlm1*
            (parse/pair-delimiters
             (parse/parse-delimiters "
(defn
 (do
   (it
 (this))")))]}
(fact "finds previous index discrepancies given indent layout"
  ^:hidden
  
  (indent/flag-indent-discrepancies-single
   *dlm1*
   (parse/make-delimiter-line-lu *dlm1*)
   (get *dlm1* 3))
  => [{:correct? true, :index 2, :pair-id 1, :type :open, :style :paren, :line 4, :col 4, :depth 2, :char "("}
      {:char "(", :line 3, :col 2, :type :open, :style :paren, :depth 1, :correct? false, :index 1}]
  
  
  (def ^:dynamic *dlm4*
    (parse/pair-delimiters
     (parse/parse-delimiters (h/sys:resource-content "code/heal/cases/002_complex.block")))))

^{:refer code.heal.indent/flag-indent-discrepancies-raw :added "4.0"}
(fact "finds all discrepancies given some code"
  ^:hidden

  (def ^:dynamic *dlm0*
    (parse/pair-delimiters
    (parse/parse-delimiters "
(defn
 (do
   (it)
 (this))")))
  
  (indent/flag-indent-discrepancies-raw
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

  (indent/flag-indent-discrepancies-raw
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
  
  (indent/flag-indent-discrepancies-raw
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
  
  (indent/flag-indent-discrepancies-raw
   *dlm2*
   (parse/make-delimiter-line-lu *dlm2*))
  => [[9 [{:char "[", :line 4, :col 3, :type :open, :style :square, :depth 2, :correct? false, :index 2}
          {:char "[", :line 3, :col 2, :type :open, :style :square, :depth 1, :correct? false, :index 1}]]]
  
  (def ^:dynamic *dlm3*
    (parse/pair-delimiters
     (parse/parse-delimiters (h/sys:resource-content "code/heal/cases/001_basic.block"))))
  
  (indent/flag-indent-discrepancies-raw
   *dlm3*
   (parse/make-delimiter-line-lu *dlm3*))
  => []

  (def ^:dynamic *dlm4*
    (parse/pair-delimiters
     (parse/parse-delimiters (h/sys:resource-content "code/heal/cases/002_complex.block"))))
  
  (indent/flag-indent-discrepancies-raw
   *dlm4*
   (parse/make-delimiter-line-lu *dlm4*))
  => vector?)

^{:refer code.heal.indent/flag-indent-discrepancies :added "4.0"}
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
  
  (indent/flag-indent-discrepancies *dlm0a*)
  => [[4 [{:correct? true, :index 1, :pair-id 3, :type :open, :style :paren, :line 3, :col 2, :depth 1, :char "("}]]]

  (def ^:dynamic *dlm4*
    (parse/pair-delimiters
     (parse/parse-delimiters (h/sys:resource-content "code/heal/cases/002_complex.block"))))
  
  (count
   (indent/flag-indent-discrepancies-raw
    *dlm4*
    (parse/make-delimiter-line-lu *dlm4*)))
  => 21
  
  (first
   (indent/flag-indent-discrepancies
    *dlm4*))
  => [177 [{:char "[", :line 59, :col 13, :type :open, :style :square, :depth 10, :correct? false, :index 140}
           {:correct? true, :index 136, :pair-id 64, :type :open, :style :square, :line 57, :col 13, :depth 10, :char "["}
           {:char "[", :line 56, :col 12, :type :open, :style :square, :depth 9, :correct? false, :index 133}]]
  
  (count
   (second
    (last
     (indent/flag-indent-discrepancies
      *dlm4*))))
  => 200
  
  
  (first (last
          (indent/flag-indent-discrepancies
           *dlm4*)))
  => 987

  (count
   (indent/flag-indent-discrepancies
    *dlm4*))
  => 12)

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
   {:correct? true, :index 1, :pair-id 2, :type :open, :style :paren, :line 3, :col 2, :depth 1, :char "("}
   4)
  => {:correct? true, :index 3, :pair-id 0, :type :close, :style :paren, :line 4, :col 7, :depth 2, :char ")"}


  (indent/find-indent-last-close
   (parse/pair-delimiters
    (parse/parse-delimiters "
(defn
 (do
   (it
 (this))"))
   {:correct? true, :index 2, :pair-id 1, :type :open, :style :paren, :line 4, :col 4, :depth 2, :char "("}
   3) => nil
  
  (indent/find-indent-last-close
   (parse/pair-delimiters
    (parse/parse-delimiters "
[:h 
 [[[[[]
    ]]
 []")
    )
   {:char "[", :line 3, :col 2, :type :open, :style :square, :depth 1, :correct? false, :index 1}
   9)
  => {:correct? true, :index 8, :pair-id 2, :type :close, :style :square, :line 4, :col 6, :depth 3, :char "]"}


  (def ^:dynamic *dlm4*
    (parse/pair-delimiters
     (parse/parse-delimiters (h/sys:resource-content "code/heal/cases/002_complex.block"))))
  
  (first
   (indent/flag-indent-discrepancies
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
  => {:correct? true, :index 986, :pair-id 480, :type :close, :style :square, :line 410, :col 51, :depth 25, :char "]"})






(comment

  (def ^:dynamic *dlm4*
    (parse/pair-delimiters
     (parse/parse-delimiters (h/sys:resource-content "code/heal/cases/004_shorten.block"))))
  
  (first
   (second
    (last
     (indent/flag-indent-discrepancies
      *dlm4*))))
  
  ^{:refer code.heal.indent/find-depth-indent-discrepancies :added "4.0"}
  (fact "Finds opening delimiters where the column is less than a previous opening delimiter at the same depth."
    ^:hidden
    
    ;; findging the open paren at (this ...) meaning that (do ...) is not correct
    
    (parse/pair-delimiters
     (parse/parse-delimiters "
(defn
 (do
   (it)
 (this))"))
    [{:char "(", :line 2, :col 1, :type :open, :style :paren, :depth 0, :correct? false, :index 0}
     {:correct? true, :index 1, :pair-id 2, :type :open, :style :paren, :depth 1, :line 3, :col 2, :char "("}
     {:correct? true, :index 2, :pair-id 0, :type :open, :style :paren, :depth 2, :line 4, :col 4, :char "("}
     {:correct? true, :index 3, :pair-id 0, :type :close, :style :paren, :depth 2, :line 4, :col 7, :char ")"}
     {:correct? true, :index 4, :pair-id 1, :type :open, :style :paren, :depth 2, :line 5, :col 2, :char "("}
     {:correct? true, :index 5, :pair-id 1, :type :close, :style :paren, :depth 2, :line 5, :col 7, :char ")"}
     {:correct? true, :index 6, :pair-id 2, :type :close, :style :paren, :depth 1, :line 5, :col 8, :char ")"}]
    

    ;; findging the open paren at (this ...) meaning that (do ...) is not correct
    
    (parse/pair-delimiters
     (parse/parse-delimiters "
(defn
 (do
   (it
 (this))"))
    [{:char "(", :line 2, :col 1, :type :open, :style :paren, :depth 0, :correct? false, :index 0}
     {:char "(", :line 3, :col 2, :type :open, :style :paren, :depth 1, :correct? false, :index 1}
     {:correct? true, :index 2, :pair-id 1, :type :open, :style :paren, :depth 2, :line 4, :col 4, :char "("}
     {:correct? true, :index 3, :pair-id 0, :type :open, :style :paren, :depth 3, :line 5, :col 2, :char "("}
     {:correct? true, :index 4, :pair-id 0, :type :close, :style :paren, :depth 3, :line 5, :col 7, :char ")"}
     {:correct? true, :index 5, :pair-id 1, :type :close, :style :paren, :depth 2, :line 5, :col 8, :char ")"}]))
