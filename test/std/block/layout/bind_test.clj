(ns std.block.layout.bind-test
  (:use code.test)
  (:require [std.block.layout.bind :as bind]
            [std.block.construct :as construct]
            [std.lib :as h]))

^{:refer std.block.layout.bind/layout-spec-fn :added "4.0"}
(fact "TODO")

^{:refer std.block.layout.bind/layout-default-fn :added "4.0"}
(fact "TODO")

^{:refer std.block.layout.bind/layout-main-loop :added "4.0"}
(fact "TODO")

^{:refer std.block.layout.bind/layout-main :added "4.0"}
(fact "performs the main layout"

  
  (bind/layout-main '(+ 1 2 3)
                    {})
  

  (h/p
   (bind/layout-main '(let [a   {:a 1 :b 2}
                            b   {:a 1 :b 2}]
                        (+ a 2))
                     {}))
  

  (h/p
   (bind/layout-main '(let [allowable   {:allowable 1 :b 2}
                            b   {:a 1 :botherable 2}]
                        (+ a 2))
                     {}))
  
  (h/p
   (bind/layout-main '(assoc m :key1 val1 :key2 val2
                             :key3 (+ a 2))
                     {}))

  )

(comment
  (h/p
   (bind/layout-main '{:a1 {:b1-data-long0 1
                            :b1-data-long1 2}
                       :a2 {:b2-data-long0 3
                            :b2-data-long1 4}}))

  (h/p
   (bind/layout-main '[[{:a1 {:b1-data-long0 1
                              :b1-data-long1 2}
                         :a2-long {:b2-data-long0 {:c2-data-long0 6
                                                   :c2-data 5}
                                   :b2-data-long1 4}}]]))
  

  (h/p 
   (bind/layout-main '[{:keys [a ab abc]
                        :as data}   (merge {:a1 {:b1-data-long0 1
                                                 :b1-data-long1 2}
                                            :a2 {:b2-data-long0 3
                                                 :b2-data-long1 4}}
                                           spec)]
                     {}))
  
  
  (h/p 
   (bind/layout-main '[[{:keys [a ab abc]
                         :as data}   (merge {:a1 {:b1-data-long0 1
                                                  :b1-data-long1 2}
                                             :a2 {:b2-data-long0 3
                                                  :b2-data-long1 4}}
                                            spec)
                        foo-bind     [1 2 3]]]
                     {}))
  
  )
