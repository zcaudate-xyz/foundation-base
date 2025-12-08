(ns std.block.layout.common-test
  (:use code.test)
  (:require [std.block.layout.common :as common]
            [std.block.layout :as bind]
            [std.block.construct :as construct]
            [std.block.base :as base]
            [std.string :as str]))

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

^{:refer std.block.layout.common/layout-single-row :added "4.0"}
(fact "layouts a row"
  ^:hidden
  
  (construct/rep
   (common/layout-single-row
    '(1 2 3 4 5)
    {:indent 10}))
  => '[1 ␣ 2 ␣ 3 ␣ 4 ␣ 5])

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
                               {:col-pad 4}
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
  => '[[100 ␣ ␣ ␣ ␣ ␣ ␣ 2 ␣ ␣ ␣ ␣ ␣ ␣ ␣ 3]
       [3 ␣ ␣ ␣ ␣ ␣ ␣ ␣ ␣ 400]
       [6 ␣ ␣ ␣ ␣ ␣ ␣ ␣ ␣ 7 ␣ ␣ ␣ ␣ ␣ ␣ ␣ 600 ␣ ␣ ␣ ␣ 7 ␣ ␣ ␣ ␣ ␣ 8]])

^{:refer std.block.layout.common/layout-n-column-align :added "4.0"}
(fact "layout code based on n columns"
  ^:hidden
  
  (construct/rep
   (common/layout-n-column-align
    [[100 2 3]
     [3 400]
     [6 7 600 7 8]]
    {}
    {}))
  => '[[100 ␣ 2 ␣ ␣ ␣ 3]
       [3 ␣ ␣ ␣ 400]
       [6 ␣ ␣ ␣ 7 ␣ ␣ ␣ 600 ␣ 7 ␣ 8]]

  (construct/rep
   (common/layout-n-column-align
    [[100 2 3]
     [3 400]
     [6 7 600 7 8]]
    {:col-pad 2}
    {}))
  => '[[100 ␣ 2 ␣ ␣ ␣ 3]
       [3 ␣ ␣ ␣ 400]
       [6 ␣ ␣ ␣ 7 ␣ ␣ ␣ 600 ␣ 7 ␣ ␣ 8]]
  
  (construct/rep
   (common/layout-n-column-align
    [[100 2 3]
     [3 400]
     [6 7 600 7 8]]
    {:col-pad 4}
    {}))
  => '[[100 ␣ ␣ 2 ␣ ␣ ␣ ␣ 3]
       [3 ␣ ␣ ␣ ␣ 400]
       [6 ␣ ␣ ␣ ␣ 7 ␣ ␣ ␣ ␣ 600 ␣ ␣ 7 ␣ ␣ ␣ ␣ 8]])

^{:refer std.block.layout.common/layout-n-column :added "4.0"}
(fact "layout for arbitrary column formatting"
  ^:hidden
  
  (construct/rep
   (common/layout-n-column
    [[100 2 3]
     [3 400]
     [6 7 600 7 8]]
    {}
    {}))
  => '[[100 ␣ 2 ␣ 3] [3 ␣ 400] [6 ␣ 7 ␣ 600 ␣ 7 ␣ 8]])

^{:refer std.block.layout.common/layout-pair-blocks :added "4.0"}
(fact "layout-pair-blocks"
  ^:hidden
  
  (construct/rep
   (common/layout-pair-blocks '(:a 1 :b 2) 
                              {:spec {:col-align true}
                               :indents 2}))
  => '[[(:a ␣ 1) (:b ␣ 2)] (\n ␣ ␣)]

  
  (construct/rep
   (common/layout-pair-blocks '(:a 1 :b 2) 
                              {:spec {:col-align true}
                               :indents 5}))
  => '[[(:a ␣ 1) (:b ␣ 2)] (\n ␣ ␣ ␣ ␣ ␣)]

  (construct/rep
   (common/layout-pair-blocks '(:a-long 1 :b 2) 
                              {:spec {:col-align true}
                               :indents 5}))
  => '[[(:a-long ␣ 1) (:b ␣ ␣ ␣ ␣ ␣ ␣ 2)] (\n ␣ ␣ ␣ ␣ ␣)])

^{:refer std.block.layout.common/layout-multiline-call-setup :added "4.0"}
(fact "helper function to prep multiline form"
  ^:hidden

  (construct/rep
   (common/layout-multiline-call-setup '(apply 1 2 3 4)
                                       {:indents 0}))
  => '[apply 6])

^{:refer std.block.layout.common/layout-multiline-call :added "4.0"}
(fact "custom function for most list based functions"
  ^:hidden

  (construct/rep
   (common/layout-multiline-call :list
                                 '(apply 1 2 3 4)
                                 {:indents 0}))
  => '(apply 1 2 3 4))

^{:refer std.block.layout.common/layout-multiline-list :added "4.0"}
(fact "layout standard paired inputs"
  ^:hidden

  (-> (common/layout-multiline-list '(defn hello
                                       "oeuoeu"
                                       {:add 1}
                                       [x]
                                       (+ x 1))
                                    {:spec {:col-from 1
                                            :col-start 2}})
      (base/block-string)
      (str/split-lines))
  => ["(defn hello"
      "  \"oeuoeu\""
      "  {:add 1}"
      "  [x]"
      "  (+ x 1))"]
  
  (-> (common/layout-multiline-list '(assoc hello :key1 val :key2-oeueu-oeue val2)
                                      {:spec {:columns 2
                                              :col-from 1
                                              :col-align true}})
      (base/block-string)
      (str/split-lines))
  => ["(assoc hello"
      "       :key1            val"
      "       :key2-oeueu-oeue val2)"]
  

  (-> (common/layout-multiline-list '(assoc hello :key1 val :key2-oeueu-oeue val2)
                                      {:spec {:columns 2
                                              :col-from 1
                                              :col-align false}})
      (base/block-string)
      (str/split-lines))
  => ["(assoc hello"
      "       :key1 val"
      "       :key2-oeueu-oeue val2)"]


  (-> (common/layout-multiline-list '(hash-map :key1 val :key2-oeueu-oeue val2)
                                      {:spec {:columns 2
                                              :col-from 0
                                              :col-align true}})
      (base/block-string)
      (str/split-lines))
  => ["(hash-map :key1            val"
      "          :key2-oeueu-oeue val2)"]

  (-> (common/layout-multiline-list '(hash-map :key1 val :key2-oeueu-oeue val2)
                                      {:spec {:columns 2
                                              :col-from 0
                                              :col-align false}})
      (base/block-string)
      (str/split-lines))
  => ["(hash-map :key1 val"
      "          :key2-oeueu-oeue val2)"]
  

  (-> (common/layout-multiline-list '(case (type) :a 1 :b 2)
                                      {:spec {:columns 2
                                              :col-from 1
                                              :col-start 2}})
      (base/block-string)
      (str/split-lines))
  => ["(case (type)"
      "  :a 1"
      "  :b 2)"]
  
  (-> (common/layout-multiline-list '(something to do (type) :a 1 :b 2)
                                      {:spec {:columns 2
                                              :col-from 3
                                              :col-start 2}})
      (base/block-string)
      (str/split-lines))
  => ["(something to do (type)"
      "  :a 1"
      "  :b 2)"]

  (-> (common/layout-multiline-list '(something to do (type) :a 1 :b 2)
                                      {:spec {:columns 2
                                              :col-from 3
                                              :col-start 2}})
      (base/block-string)
      (str/split-lines))
  => ["(something to do (type)"
      "  :a 1"
      "  :b 2)"])

^{:refer std.block.layout.common/layout-multiline-hiccup :added "4.0"}
(fact "generates hiccup code"
  ^:hidden
  
  (-> (common/layout-multiline-hiccup [:hello {:a 1
                                               :b 2}
                                       [:world {}]]
                                      {:spec {:col-from 1
                                              :col-start 1}})
      (base/block-string)
      (str/split-lines))
  ["[:hello {:a 1 :b 2}"
   " [:world {}]]"]

  (-> (common/layout-multiline-hiccup [:hello {:a 1
                                               :b 2}
                                       [:world {}]]
                                      {:spec {:col-from 0
                                              :col-start 1}})
      (base/block-string)
      (str/split-lines))
  => ["[:hello"
      " {:a 1 :b 2}"
      " [:world {}]]"])

^{:refer std.block.layout.common/layout-multiline-paired :added "4.0"}
(fact "layout standard paired inputs"
  ^:hidden
  
  (-> (common/layout-multiline-paired '(assoc hello :key1 val :key2-oeueu-oeue val2)
                                      {:spec {:col-from 1
                                              :col-align true}})
      (base/block-string)
      (str/split-lines))
  => ["(assoc hello"
      "       :key1            val"
      "       :key2-oeueu-oeue val2)"]
  

  (-> (common/layout-multiline-paired '(assoc hello :key1 val :key2-oeueu-oeue val2)
                                      {:spec {:col-from 1
                                              :col-align false}})
      (base/block-string)
      (str/split-lines))
  => ["(assoc hello"
      "       :key1 val"
      "       :key2-oeueu-oeue val2)"]


  (-> (common/layout-multiline-paired '(hash-map :key1 val :key2-oeueu-oeue val2)
                                      {:spec {:col-from 0
                                              :col-align true}})
      (base/block-string)
      (str/split-lines))
  => ["(hash-map :key1            val"
      "          :key2-oeueu-oeue val2)"]

  (-> (common/layout-multiline-paired '(hash-map :key1 val :key2-oeueu-oeue val2)
                                      {:spec {:col-from 0
                                              :col-align false}})
      (base/block-string)
      (str/split-lines))
  => ["(hash-map :key1 val"
      "          :key2-oeueu-oeue val2)"]
  

  (-> (common/layout-multiline-paired '(case (type) :a 1 :b 2)
                                      {:spec {:col-from 1
                                              :col-start 2}})
      (base/block-string)
      (str/split-lines))
  => ["(case (type)"
      "  :a 1"
      "  :b 2)"]
  
  (-> (common/layout-multiline-paired '(something to do (type) :a 1 :b 2)
                                      {:spec {:col-from 3
                                              :col-start 2}})
      (base/block-string)
      (str/split-lines))
  => ["(something to do (type)"
      "  :a 1"
      "  :b 2)"]

  (-> (common/layout-multiline-paired '(something to do (type) :a 1 :b 2)
                                      {:spec {:col-from 3
                                              :col-start 2}})
      (base/block-string)
      (str/split-lines)))

^{:refer std.block.layout.common/layout-multiline-hashmap :added "4.0"}
(fact "layouts the hashmap"
  ^:hidden
  
  (-> (common/layout-multiline-hashmap {:a 1 :b 2}
                                       {:spec {:col-align true}})
      (base/block-string)
      (str/split-lines))
  => ["{:a 1"
      " :b 2}"]

  (-> (common/layout-multiline-hashmap {:a 1 :b 2}
                                       {:spec {:col-align true}
                                        :indents 5})
      (base/block-string)
      (str/split-lines))
  => ["{:a 1"
      "      :b 2}"])

^{:refer std.block.layout.common/layout-by-columns :added "4.0"}
(fact "layout data using columns"
  ^:hidden

  (construct/rep
   (common/layout-by-columns [:a-long 1 :b 2]
                             1
                             {:spec {:col-align false}}))
  => '[:a-long ␣ 1 \n ␣ :b ␣ 2]

  (construct/rep
   (common/layout-by-columns [:a-long 1 :b 2]
                             2
                             {:spec {:col-align true}}))
  => '[:a-long ␣ 1 \n ␣ ␣ :b ␣ ␣ ␣ ␣ ␣ ␣ 2])

^{:refer std.block.layout.common/layout-by-rows :added "4.0"}
(fact "layout data using rows"
  ^:hidden
  
  (construct/rep
   (common/layout-by-rows [:a-long 1 :b-long 2]
                          1
                          {:spec {:row-len 10}}))
  => '[:a-long ␣ 1 \n ␣ :b-long ␣ 2]

  (construct/rep
   (common/layout-by-rows [:a-long 1 :b-long 2 :c-long 3 :d-long 4]
                          10
                          {:spec {:row-len 20}}))
  => '[:a-long ␣ 1 ␣ :b-long ␣ 2
       \n ␣ ␣ ␣ ␣ ␣ ␣ ␣ ␣ ␣ ␣ :c-long ␣ 3 ␣ :d-long ␣ 4]

  (construct/rep
   (common/layout-by-rows [:a-long 1 :b-long 2 :c-long 3 :d-long 4]
                          10
                          {:spec {:row-len 50}}))
  => '[:a-long ␣ 1 ␣ :b-long ␣ 2 ␣ :c-long ␣ 3 ␣ :d-long ␣ 4])

^{:refer std.block.layout.common/layout-by :added "4.0"}
(fact "general layout function"

  (construct/rep
   (common/layout-by [:a 1 :b 2]
                     0
                     {:spec {:row-wrap true :row-len 5}}))
  => '[:a ␣ 1 \n :b ␣ 2]

  (construct/rep
   (common/layout-by [:a 1 :b 2]
                     0
                     {:spec {:columns 2}}))
  => '[:a ␣ 1 \n :b ␣ 2])

^{:refer std.block.layout.common/layout-multiline-hashset :added "4.0"}
(fact "layouts the hashset"
  ^:hidden
  
  (-> (common/layout-multiline-hashset [:a 1 :b 2]
                                       {:spec {:columns 3
                                               :col-align true}})
      (base/block-string)
      (str/split-lines))
  => ["#{:a 1 :b"
      "  2}"])

^{:refer std.block.layout.common/layout-multiline-vector :added "4.0"}
(fact "layouts the vector"
  ^:hidden
  
  (-> (common/layout-multiline-vector [:a 1 :b 2]
                                      {:spec {:col-align true}})
      (base/block-string)
      (str/split-lines))
  => ["[:a" " 1" " :b" " 2]"]


  (bind/layout-default-fn
   '^{:spec {:columns 1}}
   [col-align
    columns]
   {})
  
  (binding [common/*layout-fn* bind/layout-default-fn]
    (-> (common/layout-multiline-vector
         '[{:keys ^{:readable-len 10 :spec {:columns 1}}
            [col-align
             columns]
            :as spec}       (merge {:columns 2
                                    :col-align false}
                                   spec)
           hello world]
         {:spec {:col-align true :columns 2}})
        (base/block-string)
        (str/split-lines)))
  => ["[{:keys [col-align"
      "         columns]"
      "  :as spec}        (merge {:col-align false :columns 2}"
      "                          spec)"
      " hello             world]"]
  
  (binding [common/*layout-fn* bind/layout-default-fn]
    (-> (bind/layout-default-fn
         '(let [{:keys [col-align
                        columns]
                 :as spec}       (merge {:columns 2
                                         :col-align false}
                                        spec)
                hello world])
         {})
        (base/block-string)
        (str/split-lines)))
  => ["(let [{:keys [col-align"
      "              columns]"
      "       :as spec}        (merge {:col-align false :columns 2}"
      "                               spec)"
      "      hello             world])"])

^{:refer std.block.layout.common/layout-with-bindings :added "4.0"}
(fact "layout with bindings"
  ^:hidden
  
  (-> (common/layout-with-bindings '(let [a 1 b 2]
                                      (+ a b)
                                      (+ a b)
                                      (+ a b))
                                   {})
      (base/block-string)
      (str/split-lines))
  => ["(let [a 1 b 2]"
      " (+ a b)"
      " (+ a b)"
      " (+ a b))"]

  (-> (common/layout-with-bindings '(binding [*ns* 1]
                                      (do (make-something-with *ns* 1 2 3)))
                                   {})
      (base/block-string)
      (str/split-lines))
  => ["(binding [*ns* 1]"
      " (do (make-something-with *ns* 1 2 3)))"])

^{:refer std.block.layout.common/layout-multiline-basic :added "4.0"}
(fact "layout standard multiline forms"
  ^:hidden
  
  (-> (common/layout-multiline-basic '(apply 1 2 3 4)
                                    {:indents 0})
      (base/block-string)
      (str/split-lines))
  => ["(apply 1"
      "       2"
      "       3"
      "       4)"])

(comment
  (std.lib/p
   (binding [common/*layout-fn* bind/layout-default-fn]
     (-> (common/layout-multiline-vector
          '[{:a 1}       (merge {:columns 2
                                 :col-align false}
                                spec)
            hello world]
          {:spec {:col-align true}})
         (base/block-string)
         ))))

(defn layout-default [form]
  (binding [common/*layout-fn* bind/layout-default-fn]
    (bind/layout-default-fn form {})))

(defn split-block [form]
  (str/split-lines (base/block-string (layout-default form))))

^{:refer std.block.layout/layout-default-fn-fix :added "4.0"}
(fact "layout standard vectors as 1 column, but bindings as 2"

  (split-block [:a 1 :b 2 :c 3])
  => ["[:a 1 :b 2 :c 3]"]

  (split-block ^{:readable-len 10}
               [:this-is-long-1
                :this-is-long-2
                :this-is-long-3])
  => ["[:this-is-long-1"
      " :this-is-long-2"
      " :this-is-long-3]"]

  (split-block ^{:readable-len 10}
               '(let [variable-a 100
                      variable-b 200
                      variable-c (+ variable-a variable-b)]
                  (println variable-c)))
  => ["(let [variable-a 100"
      "      variable-b 200"
      "      variable-c (+ variable-a variable-b)]"
      "  (println variable-c))"]

  (split-block ^{:readable-len 10}
               '(cond (= x 1) :one
                      (= x 2) :two
                      :else   :other))
  => ["(cond (= x 1) :one"
      "      (= x 2) :two"
      "      :else   :other)"])
