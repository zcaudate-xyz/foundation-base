(ns code.layout-test
  (:use code.test)
  (:require [code.layout :as layout]
            [code.edit :as edit]
            [std.lib.zip :as zip]))

^{:refer code.layout/layout-form-insert :added "4.0"}
(fact "inserts a single element"
  ^:hidden

  ;; Simple Insert
  (-> (layout/layout-form-insert
       (layout/layout-form-initial {})
       (zip/form-zip '1))
      first
      :code
      (str))
  => "<0,0> |1:eof"

  (-> (layout/layout-form-initial {})
      (layout/layout-form-insert
       (zip/form-zip '1))
      first
      (layout/layout-form-insert
       (zip/form-zip '2))
      first
      :code
      str)
  => "<0,2> 1␣|2:eof"

  (-> (layout/layout-form-insert
       (layout/layout-form-initial {})
       (zip/form-zip '(+ 1 2 3)))
      first
      :code
      (str))
  => "<0,0> |(+ 1 2 3):eof"

  (-> (layout/layout-form-initial {})
      (layout/layout-form-insert
       (zip/form-zip '(+ 1 2 3)))
      first
      (layout/layout-form-insert
       (zip/form-zip '(+ 4 5 6)))
      first
      :code
      str)
  => "<0,10> (+ 1 2 3)␣|(+ 4 5 6):eof")

^{:refer code.layout/layout-form-initial :added "4.0"}
(fact "generates the initial state"
  ^:hidden

  (layout/layout-form-initial {})
  => map?)

^{:refer code.layout/layout-form :added "4.0"}
(fact "layout a form"
  ^:hidden
  
  (str (layout/layout-form
        '(+ 1 2 3)))
  => "<0,0> |(+ 1 2 3):eof"
  
  (layout/layout-form
   '(+ 1 2 3)))
