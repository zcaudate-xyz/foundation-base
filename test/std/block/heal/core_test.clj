(ns std.block.heal.core-test
  (:use code.test)
  (:require [std.block.heal.core :as level]
            [std.string :as str]
            [std.block :as b]
            [std.lib :as h]))

^{:refer std.block.heal.core/group-min-col :added "4.0"}
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

^{:refer std.block.heal.core/group-entries :added "4.0"}
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

^{:refer std.block.heal.core/group-blocks-single :added "4.0"}
(fact "creates a single block"
  ^:hidden
    
  (level/group-blocks-single
   (:entries (level/group-blocks-prep
              (str/join-lines
               ["(:? ()"
                "    ())"
                "    nil {})"])))
   1)
  => {:lead {:char "(", :line 1, :col 1, :type :open, :style :paren},
      :line [1 3],
      :level 1,
      :col 1,
      :children
      [{:lead
        {:char "(", :line 1, :col 5, :type :open, :style :paren},
        :line [1 1],
        :level 0,
        :col 5}
       {:lead
        {:char "(", :line 2, :col 5, :type :open, :style :paren},
        :line [2 2],
        :level 0,
        :col 5}
       {:lead
        {:char "{", :line 3, :col 9, :type :open, :style :curly},
        :line [3 3],
        :level 0,
        :col 9,
        :last true}]}
  
  (level/group-blocks-single
   (:entries (level/group-blocks-prep
              (str/join-lines
               ["(:? ()"
                "    ())"
                "    nil)"])))
   1)
  => {:lead {:char "(", :line 1, :col 1, :type :open, :style :paren},
      :line [1 3],
      :level 1,
      :col 1,
      :children
      [{:lead {:char "(", :line 1, :col 5, :type :open, :style :paren},
        :line [1 1],
        :level 0,
        :col 5}
       {:lead {:char "(", :line 2, :col 5, :type :open, :style :paren},
        :line [2 2],
        :level 0,
        :col 5}]})
  
^{:refer std.block.heal.core/group-blocks-multi :added "4.0"}
(fact "categorises the blocks"
  ^:hidden
  
  (level/group-blocks-multi
   (:entries (level/group-blocks-prep
              (str/join-lines
               ["(:? ()"
                "    ())"
                "    nil {})"]))))
  => [{:lead {:char "(", :line 1, :col 1, :type :open, :style :paren},
       :line [1 3],
       :level 1,
       :last true
       :col 1,
       :children
       [{:lead {:char "(", :line 1, :col 5, :type :open, :style :paren},
         :line [1 1],
         :level 0,
         :col 5}
        {:lead {:char "(", :line 2, :col 5, :type :open, :style :paren},
         :line [2 2],
         :level 0,
         :col 5}
        {:lead {:char "{", :line 3, :col 9, :type :open, :style :curly},
         :line [3 3],
         :level 0,
         :col 9,
         :last true}]}])


^{:refer std.block.heal.core/group-blocks-prep-entries :added "4.0"}
(fact "prepares the block entries for a file"
  ^:hidden 

  (level/group-blocks-prep-entries
   (std.block.heal.parse/parse-delimiters
    (str/join-lines
     ["(:? ()"
      "    ())"
      "    nil {})"]))
   (std.block.heal.parse/parse-lines
    (str/join-lines
     ["(:? ()"
      "    ())"
      "    nil {})"])))
  => [{:char "(", :line 1, :col 1, :type :open, :style :paren}
      {:char "(", :line 1, :col 5, :type :open, :style :paren}
      {:char "(", :line 2, :col 5, :type :open, :style :paren}
      {:type :code, :line 3, :last-idx 10, :col 5, :char "n"}
      {:char "{", :line 3, :col 9, :type :open, :style :curly}])

^{:refer std.block.heal.core/group-blocks-prep :added "4.0"}
(fact "prepares the block entries for a file"
  ^:hidden 
  
  
  (level/group-blocks-prep
   (str/join-lines
    ["(:? ()"
     "    ())"
     "    nil {})"]))
  => map?
  
  (level/group-blocks-prep
   (slurp "test-data/std.block.heal/cases/005_example.block"))
  => (contains
      {:lines vector?
       :entries vector?}))

^{:refer std.block.heal.core/group-blocks :added "4.0"}
(fact "groups the lines by colation sections"
  ^:hidden

  (level/group-blocks
   (str/join-lines
    ["(  (+ a b"
     "  {} b))"]))
  => [{:lead {:char "(", :line 1, :col 1, :type :open, :style :paren},
       :line [1 2],
       :level 1,
       :col 1,
       :children
       [{:lead {:char "(", :line 1, :col 4, :type :open, :style :paren},
         :line [1 1],
         :level 0,
         :col 4}
        {:lead {:char "{", :line 2, :col 3, :type :open, :style :curly},
         :line [2 2],
         :level 0,
         :col 3,
         :last true}],
       :last true}]
  
  (level/group-blocks
   (str/join-lines
    ["(:? ()"
     "    ())"
     "    nil)"]))
  => [{:lead
       {:char "(", :line 1, :col 1, :type :open, :style :paren},
       :line [1 3],
       :level 1,
       :col 1,
       :children
       [{:lead
         {:char "(", :line 1, :col 5, :type :open, :style :paren},
         :line [1 1],
         :level 0,
         :col 5}
        {:lead
         {:char "(", :line 2, :col 5, :type :open, :style :paren},
         :line [2 2],
         :level 0,
         :col 5}],
       :last true}]

  
  (level/group-blocks
   "
(:? (or )
    [:div {:className \"mb-2\"}
     [:a]]
    nil)")
  => [{:lead
       {:char "(", :line 2, :col 1, :type :open, :style :paren},
       :line [2 5],
       :level 2,
       :col 1,
       :children
       [{:lead
         {:char "(", :line 2, :col 5, :type :open, :style :paren},
         :line [2 2],
         :level 0,
         :col 5}
        {:lead
         {:char "[", :line 3, :col 5, :type :open, :style :square},
         :line [3 4],
         :level 1,
         :col 5,
         :children
         [{:lead
           {:char "{", :line 3, :col 11, :type :open, :style :curly},
           :line [3 3],
           :level 0,
           :col 11}
          {:lead
           {:char "[", :line 4, :col 6, :type :open, :style :square},
           :line [4 4],
           :level 0,
           :col 6,
           :last true}]}],
       :last true}]
  
  (level/group-blocks
   (str/join-lines
    (take 10
          (str/split-lines
           (slurp "test-data/std.block.heal/cases/005_example.block")))))
  => vector?

  (level/group-blocks
   (str/join-lines
    [""
     "    {(stateName) (Object.assign {} (. component.states [stateName])"
     "       {:description description})}"]))
  => [{:lead {:char "{", :line 2, :col 5, :type :open, :style :curly},
    :line [2 3],
    :level 5,
    :col 5,
    :children
    [{:lead {:char "(", :line 2, :col 6, :type :open, :style :paren},
      :line [2 3],
      :level 4,
      :col 6,
      :children
      [{:lead
        {:char "(", :line 2, :col 18, :type :open, :style :paren},
        :line [2 2],
        :level 3,
        :col 18,
        :children
        [{:lead
          {:char "{", :line 2, :col 33, :type :open, :style :curly},
          :line [2 2],
          :level 2,
          :col 33,
          :children
          [{:lead
            {:char "(",
             :line 2,
             :col 36,
             :type :open,
             :style :paren},
            :line [2 2],
            :level 1,
            :col 36,
            :children
            [{:lead
              {:char "[",
               :line 2,
               :col 56,
               :type :open,
               :style :square},
              :line [2 2],
              :level 0,
              :col 56,
              :last true}],
            :last true}],
          :last true}]}
       {:lead
        {:char "{", :line 3, :col 8, :type :open, :style :curly},
        :line [3 3],
        :level 0,
        :col 8,
        :last true}],
      :last true}],
    :last true}])

^{:refer std.block.heal.core/get-block-lines :added "4.0"}
(fact "gets the block lines"
  ^:hidden

  (level/get-block-lines
   (str/split-lines
    (slurp "test-data/std.block.heal/cases/005_example.block"))
   [2 5] 3)
  => ["  (:require [std.lang :as l]))"
      ""
      "(l/script :js"
      "  {:require [[js.react :as r]"]
  
  (level/get-block-lines
   (str/split-lines
    (slurp "test-data/std.block.heal/cases/005_example.block"))
   [2 5] 4)
  => ["   :require [std.lang :as l]))"
      ""
      "(l/script :js"
      "  {:require [[js.react :as r]"])

^{:refer std.block.heal.core/get-errored-loop :added "4.0"}
(fact "runs the check block loop"
  ^:hidden

  (level/get-errored-loop
   (nth (level/group-blocks (slurp "test-data/std.block.heal/cases/005_example.block")) 2)
   (str/split-lines (slurp "test-data/std.block.heal/cases/005_example.block")))
  => {:errors
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
       "                         :children []})]})]})"],
      :at
      {:lead {:char "{", :line 12, :col 8, :type :open, :style :curly},
       :line [12 46],
       :level 9,
       :col 8,
       :last true}})
  
^{:refer std.block.heal.core/get-errored-raw :added "4.0"}
(fact "helper function for get-errored"

  (level/get-errored
   (str/join-lines
    ["  (map"
     "    (fn "
     "      (return"
     "       [:% Card"
     "        [:a ]]"
     "        [:b]"
     "        [:c]])))"]))
  => [{:errors
       [{:char "]",
         :line 5,
         :col 14,
         :type :close,
         :style :square,
         :index 2,
         :depth -1,
         :correct? false}],
       :lines ["        [:a ]]"],
       :at
       {:lead {:char "[", :line 5, :col 9, :type :open, :style :square},
        :line [5 5],
        :level 0,
        :col 9}}]
  
  
  (level/get-errored
   (str/join-lines
    ["          [:% fg/Input"
     "           {:onChange (fn [e])]"
     "            :maxLength 10}]"]))
  => [{:errors
       [{:char "]",
         :line 2,
         :col 31,
         :type :close,
         :style :square,
         :index 4,
         :depth -1,
         :correct? false}],
       :lines ["                      (fn [e])]"],
       :at
       {:lead {:char "(", :line 2, :col 23, :type :open, :style :paren},
        :line [2 2],
        :level 1,
        :col 23}}]
  
  (level/get-errored
   (str/join-lines
    [""
     "          [:% fg/Input"
     "           {:onChange (fn [e])}"
     "            :maxLength 10}]"]))
  => [{:errors
       [{:char "}",
         :line 3,
         :col 31,
         :type :close,
         :style :curly,
         :index 4,
         :depth -1,
         :correct? false}],
       :lines ["                      (fn [e])}"],
       :at
       {:lead {:char "(", :line 3, :col 23, :type :open, :style :paren},
        :line [3 3],
        :level 1,
        :col 23}}]
  
  
  (level/get-errored
   (str/join-lines
    [""
     "          [:% fg/Input"
     "           {:placeholder \"e.g., TBP\""
     "            :value tokenData.symbol"
     "            :onChange (fn [e] (return (handleChange \"symbol\" (. e.target.value (toUpperCase)))))}"
     "            :className \"bg-[#0a0a0a] border-[#2d2d2d] text-white\""
     "            :maxLength 10}]"]))
  => [{:errors
       [{:char "}",
         :line 5,
         :col 97,
         :type :close,
         :style :curly,
         :index 12,
         :depth -1,
         :correct? false}],
       :lines
       ["                      (fn [e] (return (handleChange \"symbol\" (. e.target.value (toUpperCase)))))}"],
       :at
       {:lead {:char "(", :line 5, :col 23, :type :open, :style :paren},
        :line [5 5],
        :level 5,
        :col 23,
        :last true}}]

  (level/get-errored
   (str/join-lines
    [""
     "          [{:placeholder \"e.g., TBP\""
     "            :value tokenData.symbol"
     "            :onChange (fn [e] (return (handleChange \"symbol\" (. e.target.value (toUpperCase)))))}"
     "            :className \"bg-[#0a0a0a] border-[#2d2d2d] text-white\""
     "            :maxLength 10}]"]))
  => [{:errors
       [{:char "}",
         :line 4,
         :col 97,
         :type :close,
         :style :curly,
         :index 12,
         :depth -1,
         :correct? false}],
       :lines
       ["                      (fn [e] (return (handleChange \"symbol\" (. e.target.value (toUpperCase)))))}"],
       :at
       {:lead
        {:char "(", :line 4, :col 23, :type :open, :style :paren},
        :line [4 4],
        :level 5,
        :col 23,
        :last true}}])

^{:refer std.block.heal.core/get-errored :added "4.0"}
(fact "checks content for irregular blocks"
  ^:hidden

  (level/get-errored
   (str/join-lines
    ["         [:div"
     "          [:% fg/Label {:className \"text-[#b4b4b4] mb-2\"} \"Token Symbol *\"]"
     "          [:% fg/Input"
     "           {:placeholder \"e.g., TBP\""
     "            :value tokenData.symbol"
     "            :onChange (fn [e] (return (handleChange \"symbol\" (. e.target.value (toUpperCase)))))}"
     "            :className \"bg-[#0a0a0a] border-[#2d2d2d] text-white\""
     "            :maxLength 10}]"
     "          [:p {:className \"text-xs text-[#6d6d6d] mt-1\"} \"Short symbol (3-10 characters)\"]]"]))
  => [{:errors
       [{:char "}",
         :line 6,
         :col 97,
         :type :close,
         :style :curly,
         :index 12,
         :depth -1,
         :correct? false}],
       :lines
       ["                      (fn [e] (return (handleChange \"symbol\" (. e.target.value (toUpperCase)))))}"],
       :at
       {:lead {:char "(", :line 6, :col 23, :type :open, :style :paren},
        :line [6 6],
        :level 5,
        :col 23,
     :last true}}]
  

  (level/get-errored
   (str/join-lines
    ["{:hello (fn [] (+ ou))}"
     " :part 1}"]))
  => [{:errors
       [{:char "}",
         :line 1,
         :col 23,
         :type :close,
         :style :curly,
         :index 6,
         :depth -1,
         :correct? false}],
       :lines ["        (fn [] (+ ou))}"],
       :at
       {:lead {:char "(", :line 1, :col 9, :type :open, :style :paren},
        :line [1 1],
        :level 2,
        :col 9}}]
  
  (level/get-errored
   (str/join-lines
    ["(  (+ a b"
     "  {} b))"]))
  => []
  
  (level/get-errored
   (str/join-lines
    ["(:? ()"
     "    (+ 1) (+ 2))"
     "    nil {})"]))
  => [{:errors
       [{:char ")",
         :line 2,
         :col 16,
         :type :close,
         :style :paren,
         :index 2,
         :depth -1,
         :correct? false}],
       :lines ["          (+ 2))"],
       :at
       {:lead {:char "(", :line 2, :col 11, :type :open, :style :paren},
        :line [2 2],
        :level 0,
        :col 11,
        :last true}}]
  
  
  (level/get-errored
   (str/join-lines
    ["(:? ()"
     "    ())"
     "    nil {})"]))
  => [{:errors
       [{:char ")",
         :line 2,
         :col 7,
         :type :close,
         :style :paren,
         :index 2,
         :depth -1,
      :correct? false}],
       :lines ["    ())"],
       :at
       {:lead {:char "(", :line 2, :col 5, :type :open, :style :paren},
        :line [2 2],
        :level 0,
        :col 5}}]
  
  
  (level/get-errored
   (str/join-lines
    ["(:? ()"
     "    (+ 1 2) (+ 23 4))"
     "    nil {})"]))
  => [{:errors
       [{:char ")",
         :line 2,
         :col 21,
         :type :close,
         :style :paren,
         :index 2,
         :depth -1,
         :correct? false}],
       :lines ["            (+ 23 4))"],
       :at
       {:lead {:char "(", :line 2, :col 13, :type :open, :style :paren},
        :line [2 2],
        :level 0,
        :col 13,
        :last true}}]
  
  (level/get-errored
   (str/join-lines
    [""
     "    {(stateName) (Object.assign {} (. component.states [stateName])"
     "       {:description description})}"]))
  => []

  (level/get-errored
   (str/join-lines
    ["(:? ()"
     "    ())"
     "    nil {})"]))
  => [{:errors
    [{:char ")",
      :line 2,
      :col 7,
      :type :close,
      :style :paren,
      :index 2,
      :depth -1,
      :correct? false}],
    :lines ["    ())"],
    :at
    {:lead {:char "(", :line 2, :col 5, :type :open, :style :paren},
     :line [2 2],
     :level 0,
     :col 5}}])

^{:refer std.block.heal.core/get-errored.more :added "4.0"}
(fact "get errored more cases"

  (level/get-errored
   (slurp "test-data/std.block.heal/cases/005_example.block"))
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
        "                         :children []})]})]})"],
       :at
       {:lead {:char "{", :line 12, :col 8, :type :open, :style :curly},
        :line [12 46],
        :level 9,
        :col 8,
        :last true}}
      {:errors
       [{:char ")",
         :line 403,
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
        ""
        "                                                   (. parts (forEach (fn [part index]"
        "                                                                       (when (not (. current.children (has part)))"
        "                                                                         (. current.children (set part"
        "                                                                                                (do {:name part"
        "                                                                                                     :fullPath (. (. parts (slice 0 (+ index 1))) (join \".\"))"
        "                                                                                                     :components []"
        "                                                                                                     :children (new Map())}))))"
        "                                                                       (:= current (. current.children (get part))))))"
        "                                                   (. current.components (push comp))))))"],
       :at
       {:lead
        {:char "(", :line 391, :col 27, :type :open, :style :paren},
        :line [391 403],
        :level 15,
        :col 27}}
      {:errors
       [{:char ")",
         :line 532,
         :col 31,
         :type :close,
         :style :paren,
         :index 124,
         :depth -1,
         :correct? false}],
       :lines
       ["(defn.js LibraryComponentItem [{:# [comp depth onImportComponent onImportAndEdit]}]"
        "  (var [isDragging drag]"
        "    (dnd/useDrag (fn []"
        "                   (return {:type \"LIBRARY_COMPONENT\""
        "                            :item {:libraryComponent comp.component}"
        "                            :collect (fn [monitor]"
        "                                       (return {:isDragging (. monitor (isDragging))}))}))))"
        ""
        "  (var handleDoubleClick (fn []"
        "                           (onImportAndEdit comp.component)))"
        ""
        "  (return"
        "    [:div"
        "      {:ref drag"
        "       :onDoubleClick handleDoubleClick"
        "       :className (+ \"flex items-start gap-2 py-2 px-2 hover:bg-[#323232] group cursor-grab \""
        "                     (:? isDragging \"opacity-50 cursor-grabbing\" \"\"))"
        "       :style {:paddingLeft (+ (* depth 12) 8 \"px\")}}"
        "      [:% lc/FileCode {:className \"w-3 h-3 text-purple-400 mt-0.5 flex-shrink-0\"}]"
        "      [:div {:className \"flex-1 min-w-0\"}"
        "        [:div {:className \"flex items-center gap-2 mb-1\"}"
        "          [:span {:className \"text-xs text-gray-300\"} comp.name]"
        "          [:div {:className \"flex items-center gap-1 text-[10px] text-gray-500\"}"
        "            [:% lc/Star {:className \"w-2.5 h-2.5 fill-current\"}]"
        "            comp.stars]]"
        "        [:p {:className \"text-[10px] text-gray-600 mb-1\"} comp.description]"
        "        [:% fg/Button"
        "          {:size \"sm\""
        "           :onClick (fn [e]"
        "                      (. e (stopPropagation))"
        "                      (onImportComponent comp.component))"
        "           :className \"h-5 text-[10px] px-2 bg-[#404040] hover:bg-[#4a4a4a] text-gray-300 opacity-0 group-hover:opacity-100 transition-opacity\"}"
        "          [:% lc/Download {:className \"w-2.5 h-2.5 mr-1\"}]"
        "          \"Import\"]"
        "        [:% fg/Button"
        "          {:size \"sm\""
        "           :onClick (fn [e]"
        "                      (. e (stopPropagation))"
        "                      (onImportAndEdit comp.component))"
        "           :className \"h-5 text-[10px] px-2 bg-[#404040] hover:bg-[#4a4a4a] text-gray-300 opacity-0 group-hover:opacity-100 transition-opacity\"}"
        "          [:% lc/Download {:className \"w-2.5 h-2.5 mr-1\"}]"
        "          \"Import & Edit\"]]])))"],
       :at
       {:lead
        {:char "(", :line 491, :col 1, :type :open, :style :paren},
        :line [491 532],
        :level 10,
        :col 1,
        :last true}}])

^{:refer std.block.heal.core/heal-content-single-pass :added "4.0"}
(fact "heals the content in a single pass"
  ^:hidden
  
  (str/split-lines
   (level/heal-content-single-pass
    (str/join-lines
     ["(:? ()"
      "    ())"
      "    nil {})"])))
  => ["(:? ()"
      "    ()"
      "    nil {})"]

  (str/split-lines
   (level/heal-content-single-pass
    (str/join-lines
     ["(:? ()"
      "    ())"
      "    nil)"])))
  => ["(:? ()"
      "    ()"
      "    nil)"]

  (str/split-lines
   (level/heal-content-single-pass
    (str/join-lines
     ["(:? ()"
      "    (+ 1) (+ 2))"
      "    nil {})"])))
  => ["(:? ()"
      "    (+ 1) (+ 2)"
      "    nil {})"]

  (str/split-lines
   (level/heal-content-single-pass
    (str/join-lines
     ["(:? ()"
      "    (+ 1))) (+ 2)"
      "    nil {})"])))
  => ["(:? ()"
      "    (+ 1) (+ 2)"
      "    nil {})"]

  ((level/wrap-diff level/heal-content-single-pass)
   (str/join-lines
    [""
     "(:? (or (== actionDef.type \"setState\") (== actionDef.type \"toggleState\") (== actionDef.type \"incrementState\"))"
     "    [:div {:className \"mb-2\"}"
     "      [:% fg/Select"
     "        [:% fg/SelectContent {}"
     "          (:? (> availableStates.length 0)"
     "              [:% fg/SelectItem {:value \"_none\" :disabled true} \"No states defined\"])]]]]"
     "    ())"
     ""
     "(:? (== actionDef.type \"setState\")"
     "    [:div"
     "      [:% fg/Label {:className \"text-[10px] text-gray-500 mb-1 block\"} \"Value\"]"
     "    nil)"]))
  => [{:type "CHANGE",
       :original
       {:lines
        ["              [:% fg/SelectItem {:value \"_none\" :disabled true} \"No states defined\"])]]]]"],
        :position 6,
        :count 1},
       :revised
       {:lines
        ["              [:% fg/SelectItem {:value \"_none\" :disabled true} \"No states defined\"])]]]"],
        :position 6,
        :count 1}}
      {:type "CHANGE",
       :original
       {:lines
        ["      [:% fg/Label {:className \"text-[10px] text-gray-500 mb-1 block\"} \"Value\"]"],
        :position 11,
        :count 1},
       :revised
       {:lines
        ["      [:% fg/Label {:className \"text-[10px] text-gray-500 mb-1 block\"} \"Value\"]]"],
        :position 11,
        :count 1}}])
  
^{:refer std.block.heal.core/heal-content :added "4.0"}
(fact "allow multiple passes to heal the delimiter"
  ^:hidden
  
  (b/parse-root
   (level/heal-content
    (slurp "test-data/std.block.heal/cases/005_example.block")))
  => b/block?
  
  (b/parse-root
   (level/heal-content
    (slurp "test-data/std.block.heal/cases/004_shorten.block")))
  => b/block?
  
  (b/parse-root
   (level/heal-content
    (slurp "test-data/std.block.heal/cases/003_examples.block")))
  => b/block?
  
  (b/parse-root
   (level/heal-content
    (slurp "test-data/std.block.heal/cases/002_complex.block")))
  => b/block?)

^{:refer std.block.heal.core/wrap-print-diff :added "4.0"}
(fact "print wrapper for the heal function"
  ^:hidden
  
  (h/with-out-str
    ((level/wrap-print-diff level/heal-content-single-pass)
     (str/join-lines
      ["(:? ()"
       "    (+ 1))) (+ 2)"
       "    nil {})"])))
  => "[1m@@ -1,1 +1,1 [0m\n[31m-[31m[40m    (+ 1))) (+ 2)[0m[0m\n[32m+[32m[40m    (+ 1) (+ 2)[0m[0m\n\n")

^{:refer std.block.heal.core/wrap-diff :added "4.0"}
(fact "wraps the heal function to output the diff"
  ^:hidden
  
  ((level/wrap-diff level/heal-content-single-pass)
   (str/join-lines
    ["(:? ()"
     "    (+ 1))) (+ 2)"
     "    nil {})"]))
  => [{:type "CHANGE", :original {:lines ["    (+ 1))) (+ 2)"],
                                  :position 1, :count 1},
       :revised {:lines ["    (+ 1) (+ 2)"], :position 1, :count 1}}])


(comment
  
  ((level/wrap-print-diff level/heal-content)
   (str/join-lines
    ["(:? ()"
     "    ())"
     "    nil)"]))
  
  ((level/wrap-print-diff level/heal-content)
   (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/states_triggers_panel2.clj"))
  
  (std.block/parse-root
   ((level/wrap-print-diff level/heal-content)
    (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser3.clj")))
  
  ((level/wrap-print-diff level/heal-content)
   (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser2.clj"))
  
  (s/layout
   (read-string
    (str "["
         ((level/wrap-print-diff level/heal-content)
          (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj"))
         "]")))
  
  (read-string
   (str "["
        ((wrap-print-diff heal-content)
         (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj"))
        "]"))
  
  (read-string
   (str "["
        ((wrap-print-diff std.block.heal.tokens/heal-tokens)
         ((wrap-print-diff core/heal)
          ((wrap-print-diff heal-content)
           (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj"))))
        "]"))
  
  (read-string
   (str "["
        (h/suppress
         ((wrap-print-diff core/heal)
          ((wrap-print-diff heal-content)
           (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/states_triggers_panel.clj"))))
        "]"))
  
  (read-string
   (str "["
        (level/heal-content
         (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/outliner_panel.clj"))
        "]"))
  (read-string
   (str "["
        (level/heal-content
         (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/states_triggers_panel.clj"))
        "]"))
  
  
  (level/heal-content
   )
  
  (level/heal-content
   (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/states_triggers_panel2.clj")
   
   )
  
  (h/suppress
   ((wrap-print-diff core/heal)
    ((wrap-print-diff heal-content)
     (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/states_triggers_panel2.clj"))))
  
  
  
  (doseq [f (keys
             (std.fs/list
              "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/"
              {:include [".clj$"]}))]
    
    (h/p f)
    (try (read-string
          (str "["
               ((level/wrap-print-diff level/heal-content)
                (slurp f))
               "]"))
         (catch Throwable t
           (h/p :FAILED))))
  
  (doseq [f (keys
             (std.fs/list
              "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/"
              {:include [".clj$"]}))]
    (try (read-string
          (str "["
               ((level/wrap-print-diff level/heal-content)
                (slurp f))
               "]"))
         (h/p  f :SUCCESS)
         (catch Throwable t
           (h/p  f :FAILED))))
  
  (level/heal-content
   (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/chat_input.clj"))
  
  (doseq [f (keys
             (std.fs/list
              "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/"
              {:include [".clj$"]}))]
    
    
    (try (read-string
          (str "["
               (slurp f)
               "]"))
         (h/p  f :SUCCESS)
         (catch Throwable t
           (h/p  f :FAILED)))))


(comment
  (count
   (str/split-lines
    (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj")))
  
  (level/heal-content-single-pass
   (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj"))

  (std.block/parse-root
   ((level/wrap-print-diff level/heal-content)
    (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/app.clj")))
  
  (h/p (diff/->string
        (diff/diff
         (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj")
         *new-content*
         )))
  
  
  (group-blocks
   (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser2.clj"))
  
  (h/p
   (std.text.diff/->string
    (std.text.diff/diff
     (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj")
     (level/heal-content-single-pass
      (level/heal-content-single-pass
       (level/heal-content-single-pass
        (level/heal-content-single-pass
         (level/heal-content-single-pass
          (level/heal-content-single-pass
           (level/heal-content-single-pass
            (level/heal-content-single-pass
             (level/heal-content-single-pass
              (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj")))))))))))))
  
   

   ((level/wrap-print-diff level/heal-content)
    ((level/wrap-print-diff level/heal-content)
     ((level/wrap-print-diff level/heal-content)
      ((level/wrap-print-diff level/heal-content)
       ((level/wrap-print-diff level/heal-content)
        ((level/wrap-print-diff level/heal-content)
         ((level/wrap-print-diff level/heal-content)
          ((level/wrap-print-diff level/heal-content)
           ))))))))
  
  (level/heal-content
   (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj"))
  
  (std.block.heal/print-rainbow
   (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj"))
  (get-errored
   (slurp "../../buffer/Smalltalkinterfacedesign/translate/src-translated/components/library_browser.clj")))


^{:refer std.block.heal.core/check-errored-suspect :added "4.0"}
(fact "TODO")

^{:refer std.block.heal.core/heal-content-complex-edits :added "4.0"}
(fact "TODO")
