(ns code.layout-test
  (:use code.test)
  (:require [code.layout :as layout]
            [code.edit :as edit]
            [std.lib.zip :as zip]))

^{:refer code.layout/get-max-width-children :added "4.0"}
(fact "gets the max with of the children"
  ^:hidden

  (layout/get-max-width-children [:a :b :c])
  => 8

  (layout/get-max-width-children [:a :b :c :d])
  => 11)

^{:refer code.layout/get-max-width :added "4.0"}
(fact "gets the max width of whole form"
  ^:hidden
  
  (layout/get-max-width [:a :b :c])
  => 10

  (layout/get-max-width [:a :b :c :d])
  => 13)

^{:refer code.layout/estimate-multiline-special :added "4.0"}
(fact "estimates if special forms are multilined"
  ^:hidden
  
  (layout/estimate-multiline-special '(let [] a)
                                     {:readable-len 30
                                      })
  => false
  
  (layout/estimate-multiline-special '(let [a 1] a)
                                     {:readable-len 30})
  => true)

^{:refer code.layout/estimate-multiline :added "4.0"}
(fact "creates multiline function"
  ^:hidden

  (layout/estimate-multiline '(a-function that does this)
                             {:readable-len 30})
  => false
  
  (layout/estimate-multiline '(a-really-long funtion with lots of parameters that need more input)
                             {:readable-len 30})
  => true

  (layout/estimate-multiline '(let [a 1] a)
                             {:readable-len 30})
  => true)

^{:refer code.layout/layout-form-insert :added "4.0"}
(fact "TODO"
  ^:hidden
  
  (-> (layout/layout-form-insert
       (layout/layout-form-initial {})
       (zip/form-zip '1))
      first
      :code
      (edit/root-string))
  => "1"
  )

^{:refer code.layout/layout-form-initial :added "4.0"}
(fact "TODO")

^{:refer code.layout/layout-form :added "4.0"}
(fact "TODO")
