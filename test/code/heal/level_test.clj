(ns code.heal.level-test
  (:use code.test)
  (:require [code.heal.level :as level]
            [std.string :as str]))

^{:refer code.heal.level/group-min-col :added "4.0"}
(fact "gets the minimum column"
  ^:hidden
  
  (level/group-min-col
   [{:col 6}
    {}
    {}
    {:col 21}
    {}
    {:col 6}
    {:col 6}
    {:col 8}]
   0)
  => 6)

^{:refer code.heal.level/group-entries :added "4.0"}
(fact "groups entries by colation"
  ^:hidden
  
  (level/group-entries
   [{:type :code :line 504 :last-idx 15 :col 6}
    {:type :code :line 505 :last-idx 38}
    {:type :code :line 506 :last-idx 92}
    {:type :code :line 507 :last-idx 68 :col 21}
    {:type :code :line 508 :last-idx 52}
    {:type :code :line 509 :last-idx 81 :col 6}
    {:type :code :line 510 :last-idx 40 :col 6}
    {:type :code :line 511 :last-idx 56 :col 8}
    {:type :code :line 512 :last-idx 63 :col 10}
    {:type :code :line 513 :last-idx 79 :col 10}
    {:type :code :line 514 :last-idx 63 :col 12}
    {:type :code :line 515 :last-idx 23}
    {:type :code :line 516 :last-idx 74 :col 8}
    {:type :code :line 517 :last-idx 20 :col 8}
    {:type :code :line 518 :last-idx 20 :col 10}
    {:type :code :line 519 :last-idx 26}
    {:type :code :line 520 :last-idx 44 :col 22}
    {:type :code :line 521 :last-idx 56 :col 22}
    {:type :code :line 522 :last-idx 143}
    {:type :code :line 523 :last-idx 57 :col 10}
    {:type :code :line 524 :last-idx 18}
    {:type :code :line 525 :last-idx 20 :col 8}
    {:type :code :line 526 :last-idx 20 :col 10}
    {:type :code :line 527 :last-idx 26}
    {:type :code :line 528 :last-idx 44 :col 22}
    {:type :code :line 529 :last-idx 54 :col 22}
    {:type :code :line 530 :last-idx 143}
    {:type :code :line 531 :last-idx 57 :col 10}
    {:type :code :line 532 :last-idx 30}]
   6)
  => [[{:type :code :line 504 :last-idx 15 :col 6}
       {:type :code :line 505 :last-idx 38}
       {:type :code :line 506 :last-idx 92}
       {:type :code :line 507 :last-idx 68 :col 21}
       {:type :code :line 508 :last-idx 52}]
      [{:type :code :line 509 :last-idx 81 :col 6}]
      [{:type :code :line 510 :last-idx 40 :col 6}
       {:type :code :line 511 :last-idx 56 :col 8}
       {:type :code :line 512 :last-idx 63 :col 10}
       {:type :code :line 513 :last-idx 79 :col 10}
       {:type :code :line 514 :last-idx 63 :col 12}
       {:type :code :line 515 :last-idx 23}
       {:type :code :line 516 :last-idx 74 :col 8}
       {:type :code :line 517 :last-idx 20 :col 8}
       {:type :code :line 518 :last-idx 20 :col 10}
       {:type :code :line 519 :last-idx 26}
       {:type :code :line 520 :last-idx 44 :col 22}
       {:type :code :line 521 :last-idx 56 :col 22}
       {:type :code :line 522 :last-idx 143}
       {:type :code :line 523 :last-idx 57 :col 10}
       {:type :code :line 524 :last-idx 18}
       {:type :code :line 525 :last-idx 20 :col 8}
       {:type :code :line 526 :last-idx 20 :col 10}
       {:type :code :line 527 :last-idx 26}
       {:type :code :line 528 :last-idx 44 :col 22}
       {:type :code :line 529 :last-idx 54 :col 22}
       {:type :code :line 530 :last-idx 143}
       {:type :code :line 531 :last-idx 57 :col 10}
       {:type :code :line 532 :last-idx 30}]])

^{:refer code.heal.level/group-blocks-single :added "4.0"}
(fact "creates a single block"
  ^:hidden
  
  (level/group-blocks-single
   (:entries (level/group-blocks-prep
              (->> (slurp "test-data/code.heal/cases/005_example.block")
                   (str/split-lines)
                   (drop 10)
                   (take 3)
                   (str/join-lines))))
   10)
  => {:lead {:char "(", :line 1, :col 1, :type :open, :style :paren},
      :line [1 3], :col 10})

^{:refer code.heal.level/group-blocks-multi :added "4.0"}
(fact "categorises the blocks"
  ^:hidden
  
  (level/group-blocks-multi
   (:entries (level/group-blocks-prep
              (->> (slurp "test-data/code.heal/cases/005_example.block")
                   (str/split-lines)
                   (drop 10)
                   (take 10)
                   (str/join-lines)))))
  => [{:lead {:char "(" :line 1 :col 1 :type :open :style :paren}
       :line [1 10]
       :col 1
       :children
       [{:lead
         {:char "[" :line 2 :col 3 :type :open :style :square}
         :line [2 10]
         :col 3
         :children
         [{:lead
           {:char "(" :line 2 :col 4 :type :open :style :paren}
           :line [2 10]
           :col 4
           :children
           [{:lead
             {:char "{" :line 2 :col 8 :type :open :style :curly}
             :line [2 10]
             :col 8
             :children
             [{:lead
               {:char "(" :line 8 :col 9 :type :open :style :paren}
               :line [8 10]
               :col 9
               :children
               [{:lead
                 {:char "{"
                  :line 8
                  :col 13
                  :type :open
                  :style :curly}
                 :line [8 10]
                 :col 13
                 :last true}]
               :last true}]
             :last true}]
           :last true}]
         :last true}]
       :last true}])
  
^{:refer code.heal.level/group-blocks-prep :added "4.0"}
(fact "prepares the block entries for a file"
  ^:hidden 

  (level/group-blocks-prep
   (slurp "test-data/code.heal/cases/005_example.block"))
  => (contains
      {:lines vector?
       :entries vector?}))

^{:refer code.heal.level/group-blocks :added "4.0"}
(fact "groups the lines by colation sections"
  ^:hidden
  
  (level/group-blocks
   (str/join-lines
    (take 10
          (str/split-lines
           (slurp "test-data/code.heal/cases/005_example.block")))))
  => [{:lead {:char "(" :line 1 :col 1 :type :open :style :paren}
       :line [1 2]
       :col 1
       :children
       [{:lead {:char "(" :line 2 :col 3 :type :open :style :paren}
         :line [2 2]
         :col 3
         :children
         [{:lead
           {:char "[" :line 2 :col 13 :type :open :style :square}
           :line [2 2]
           :col 13
           :last true}]
         :last true}]}
      {:lead {:char "(" :line 3 :col 1 :type :open :style :paren}
       :line [3 8]
       :col 1
       :children
       [{:lead {:char "{" :line 4 :col 3 :type :open :style :curly}
         :line [4 8]
         :col 3
         :children
         [{:lead
           {:char "[" :line 4 :col 13 :type :open :style :square}
           :line [4 8]
           :col 13
           :children
           [{:lead
             {:char "[" :line 4 :col 14 :type :open :style :square}
             :line [4 4]
             :col 14}
            {:lead
             {:char "[" :line 5 :col 14 :type :open :style :square}
             :line [5 5]
             :col 14}
            {:lead
             {:char "[" :line 6 :col 14 :type :open :style :square}
             :line [6 6]
             :col 14}
            {:lead
             {:char "[" :line 7 :col 14 :type :open :style :square}
             :line [7 7]
             :col 14}
            {:lead
             {:char "[" :line 8 :col 14 :type :open :style :square}
             :line [8 8]
             :col 14
             :last true}]
           :last true}]
         :last true}]
       :last true}])

^{:refer code.heal.level/get-block-lines :added "4.0"}
(fact "gets the block lines"
  ^:hidden

  (level/get-block-lines
   (str/split-lines
    (slurp "test-data/code.heal/cases/005_example.block"))
   [2 5] 3)
  => ["  (:require [std.lang :as l]))"
      ""
      "(l/script :js"
      "  {:require [[js.react :as r]"]
  
  (level/get-block-lines
   (str/split-lines
    (slurp "test-data/code.heal/cases/005_example.block"))
   [2 5] 4)
  => ["   :require [std.lang :as l]))"
      ""
      "(l/script :js"
      "  {:require [[js.react :as r]"])

^{:refer code.heal.level/get-errored-loop :added "4.0"}
(fact "runs the check block loop"
  ^:hidden

  (level/get-errored-loop
   (nth (level/group-blocks (slurp "test-data/code.heal/cases/005_example.block")) 2)
   (str/split-lines (slurp "test-data/code.heal/cases/005_example.block"))
   {})
  {:errors
   [{:char "{",
     :line 12,
     :col 8,
     :type :open,
     :style :curly,
     :depth 0,
     :index 0,
     :correct? false}],
   :lines
   ["       {:id \"ui.sections/hero-gradient\""
    "        :namespace \"ui.sections\""
    "        :name \"HeroGradient\""
    "        :description \"Hero section with gradient background\""
    "        :stars 245"
    "        :component"
    "        (do {:id \"hero-section\""
    "             :type \"Container\""
    "             :label \"Hero Section\""
    "             :libraryRef \"ui.sections/HeroGradient\""
    "             :properties {:className \"bg-gradient-to-r from-purple-600 to-blue-600 text-white py-20 px-6\"}"
    "             :children"
    "             [(do {:id \"hero-content\""
    "                   :type \"Container\""
    "                   :label \"Hero Content\""
    "                   :properties {:className \"max-w-4xl mx-auto text-center\"}"
    "                   :children"
    "                   [(do {:id \"hero-title\""
    "                         :type \"Heading\""
    "                         :label \"Title\""
    "                         :properties {:children \"Build Amazing UIs\""
    "                                      :className \"text-5xl font-bold mb-4\"}"
    "                         :children []})"
    "                    (do {:id \"hero-subtitle\""
    "                         :type \"Text\""
    "                         :label \"Subtitle\""
    "                         :properties {:children \"Create beautiful interfaces with our component builder\""
    "                                      :className \"text-xl mb-8 opacity-90\"}"
    "                         :children []})"
    "                    (do {:id \"hero-cta\""
    "                         :type \"Button\""
    "                         :label \"CTA Button\""
    "                         :properties {:children \"Get Started\""
    "                                      :className \"bg-white text-purple-600 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100\"}"
    "                         :children []})]})]})        "],
   :at
   {:lead {:char "{", :line 12, :col 8, :type :open, :style :curly},
    :line [12 46],
    :col 8,
    :last true}})

^{:refer code.heal.level/get-errored :added "4.0"}
(fact "checks content for irregular blocks"
  ^:hidden
  
  (level/get-errored
   (slurp "test-data/code.heal/cases/005_example.block"))
  => [{:errors
       [{:char "{",
         :line 12,
         :col 8,
         :type :open,
         :style :curly,
         :depth 0,
         :index 0,
         :correct? false}],
       :lines
       ["       {:id \"ui.sections/hero-gradient\""
        "        :namespace \"ui.sections\""
        "        :name \"HeroGradient\""
        "        :description \"Hero section with gradient background\""
        "        :stars 245"
        "        :component"
        "        (do {:id \"hero-section\""
        "             :type \"Container\""
        "             :label \"Hero Section\""
        "             :libraryRef \"ui.sections/HeroGradient\""
        "             :properties {:className \"bg-gradient-to-r from-purple-600 to-blue-600 text-white py-20 px-6\"}"
        "             :children"
        "             [(do {:id \"hero-content\""
        "                   :type \"Container\""
        "                   :label \"Hero Content\""
        "                   :properties {:className \"max-w-4xl mx-auto text-center\"}"
        "                   :children"
        "                   [(do {:id \"hero-title\""
        "                         :type \"Heading\""
        "                         :label \"Title\""
        "                         :properties {:children \"Build Amazing UIs\""
        "                                      :className \"text-5xl font-bold mb-4\"}"
        "                         :children []})"
        "                    (do {:id \"hero-subtitle\""
        "                         :type \"Text\""
        "                         :label \"Subtitle\""
        "                         :properties {:children \"Create beautiful interfaces with our component builder\""
        "                                      :className \"text-xl mb-8 opacity-90\"}"
        "                         :children []})"
        "                    (do {:id \"hero-cta\""
        "                         :type \"Button\""
        "                         :label \"CTA Button\""
        "                         :properties {:children \"Get Started\""
        "                                      :className \"bg-white text-purple-600 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100\"}"
        "                         :children []})]})]})        "],
       :at
       {:lead {:char "{", :line 12, :col 8, :type :open, :style :curly},
        :line [12 46],
        :col 8,
        :last true}}
      {:errors
       [{:char ")",
         :line 402,
         :col 89,
         :type :close,
         :style :paren,
         :index 66,
         :depth -1,
         :correct? false}],
       :lines
       ["                          (. components (forEach (fn [comp]"
        "                                                   (var parts (. comp.namespace (split \".\")))"
        "                                                   (var current root)"
        "                                                   (. parts (forEach (fn [part index]"
        "                                                                       (when (not (. current.children (has part)))"
        "                                                                         (. current.children (set part"
        "                                                                                                (do {:name part"
        "                                                                                                     :fullPath (. (. parts (slice 0 (+ index 1))) (join \".\"))"
        "                                                                                                     :components []"
        "                                                                                                     :children (new Map())}))))"
        "                                                                       (:= current (. current.children (get part))))))"
        "                                                   (. current.components (push comp))))))                           "],
       :at
       {:lead
        {:char "(", :line 391, :col 27, :type :open, :style :paren},
        :line [391 403],
        :col 27}}])
