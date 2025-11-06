(ns std.block.layout.common-test
  (:use code.test)
  (:require [std.block.layout.common :as common]
            [std.block.construct :as construct]
            [std.block.base :as base]
            [std.string :as str]))

^{:refer std.block.layout.common/layout-n-column :added "4.0"}
(fact "layout for arbitrary column formatting")

^{:refer std.block.layout.common/join-blocks :added "4.0"}
(fact "joins blocks together with a spacing array"
  ^:hidden
  
  (common/join-blocks [nil nil]
                      [1 2 3 4])
  => '(1 nil nil 2 nil nil 3 nil nil 4))

^{:refer std.block.layout.common/join-block-arrays :added "4.0"}
(fact "joins block-arrays together with a spacing array"
  ^:hidden
  
  (common/join-block-arrays
   [nil nil]
   [[1 2] [3 4 5] [6 7]])
  => '(1 2 nil nil 3 4 5 nil nil 6 7))

^{:refer std.block.layout.common/format-multiline-string :added "4.0"}
(fact "makes a multiline string into a form"
  ^:hidden

  (common/format-multiline-string
   "hoeuoeu\noeuoeuoeu\noeuoeuoe\noeoeuoe")
  => '(String/join "\\n"
                   ["hoeuoeu"
                    "oeuoeuoeu"
                    "oeuoeuoe"
                    "oeoeuoe"]))

^{:refer std.block.layout.common/layout-one-column :added "4.0"}
(fact "layout for one column"
  ^:hidden

  (construct/rep
   (common/layout-one-column
    '(1 2 3 4 5)
    {:indent 10}))
  => [1 2 3 4 5])

^{:refer std.block.layout.common/layout-two-column :added "4.0"}
(fact "layout for 2 column bindings and :key val combinations"
  ^:hidden
  
  (construct/rep
   (common/layout-two-column
    '((:key1 val) (:key2-oeueu-oeue val2))
    {:col-align false}
    {:indent 10}))
  => '[[:key1 ␣ val] [:key2-oeueu-oeue ␣ val2]]
  
  (construct/rep
   (common/layout-two-column
    '((:key1 val) (:key2-oeueu-oeue val2))
    {:col-align true}
    {:indent 10}))
  => '[(:key1 ␣ ␣ ␣ ␣ ␣ ␣ ␣ ␣ ␣ ␣ ␣ ␣ val)
       (:key2-oeueu-oeue ␣ val2)]

  (construct/rep
   (common/layout-two-column
    '{:a 1 :b 2}
    {:col-align true}
    {:indent 10}))
  => '[(:a ␣ 1) (:b ␣ 2)])

^{:refer std.block.layout.common/layout-n-column-space :added "4.0"}
(fact "layouts a set of columns with single space"
  ^:hidden

  (construct/rep
   (common/layout-n-column-space [[1 2 3]
                                  [3 4]
                                  [6 7 6 7 8]]
                                 {}))
  => '[[1 ␣ 2 ␣ 3] [3 ␣ 4] [6 ␣ 7 ␣ 6 ␣ 7 ␣ 8]])

^{:refer std.block.layout.common/layout-n-column-pad :added "4.0"}
(fact "layouts a set of columns with single space"
  ^:hidden

  (construct/rep
   (common/layout-n-column-pad [[100 2 3]
                                [3 400]
                                [6 7 600 7 8]]
                               {:col-pad 5}
                               {}))
  => '[[100 ␣ ␣ 2 ␣ ␣ ␣ ␣ 3]
       [3 ␣ ␣ ␣ ␣ 400]
       [6 ␣ ␣ ␣ ␣ 7 ␣ ␣ ␣ ␣ 600 ␣ ␣ 7 ␣ ␣ ␣ ␣ 8]]

  (construct/rep
   (common/layout-n-column-pad [[100 2 3]
                                [3 400]
                                [6 7 600 7 8]]
                               {:col-pad [8 7 6 5 4]}
                               {}))
  => '[[100 ␣ ␣ ␣ ␣ ␣ 2 ␣ ␣ ␣ ␣ ␣ ␣ 3]
       [3 ␣ ␣ ␣ ␣ ␣ ␣ ␣ 400]
       [6 ␣ ␣ ␣ ␣ ␣ ␣ ␣ 7 ␣ ␣ ␣ ␣ ␣ ␣ 600 ␣ ␣ ␣ 7 ␣ ␣ ␣ ␣ 8]])

^{:refer std.block.layout.common/layout-n-column-align :added "4.0"}
(fact "TODO")

^{:refer std.block.layout.common/layout-multiline-form-setup :added "4.0"}
(fact "helper function to prep multiline form"
  ^:hidden

  (construct/rep
   (common/layout-multiline-form-setup '(apply 1 2 3 4)
                                       {:indents 0}))
  => '[apply 7])

^{:refer std.block.layout.common/layout-multiline-form :added "4.0"}
(fact "layout standard multiline forms"
  ^:hidden
  
  (-> (common/layout-multiline-form '(apply 1 2 3 4)
                                    {:indents 0})
      (base/block-string)
      (str/split-lines))
  => ["(apply 1"
      "       2"
      "       3"
      "       4)"])

^{:refer std.block.layout.common/layout-multiline-paired :added "4.0"}
(fact "layout standard paired inputs"
  ^:hidden
  
  (-> (common/layout-multiline-paired '(assoc hello :key1 val :key2-oeueu-oeue val2)
                                      {:spec {:pair-from 2
                                              :col-align true}})
      (base/block-string)
      (str/split-lines))
  => ["(assoc hello"
      "       :key1            val"
      "       :key2-oeueu-oeue val2)"]
  

  (-> (common/layout-multiline-paired '(assoc hello :key1 val :key2-oeueu-oeue val2)
                                      {:spec {:pair-from 2
                                              :col-align false}})
      (base/block-string)
      (str/split-lines))
  => ["(assoc hello"
      "       :key1 val"
      "       :key2-oeueu-oeue val2)"]


  (-> (common/layout-multiline-paired '(hash-map :key1 val :key2-oeueu-oeue val2)
                                      {:spec {:pair-from 1
                                              :col-align true}})
      (base/block-string)
      (str/split-lines))
  => ["(hash-map :key1            val"
      "          :key2-oeueu-oeue val2)"]

  (-> (common/layout-multiline-paired '(hash-map :key1 val :key2-oeueu-oeue val2)
                                      {:spec {:pair-from 1
                                              :col-align false}})
      (base/block-string)
      (str/split-lines))
  => ["(hash-map :key1 val"
      "          :key2-oeueu-oeue val2)"])

^{:refer std.block.layout.common/layout-multiline-hashmap :added "4.0"}
(fact "layouts the hashmap"
  ^:hidden
  
  (-> (common/layout-multiline-hashmap {:a 1 :b 2}
                                       {:spec {:col-align true}})
      (base/block-string)
      (str/split-lines))
  => ["{:a 1"
      " :b 2}"])

^{:refer std.block.layout.common/layout-multiline-hashset :added "4.0"}
(fact "TODO")
