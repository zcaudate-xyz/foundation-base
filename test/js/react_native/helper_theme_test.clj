(ns js.react-native.helper-theme-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[js.core :as j]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.react-native.helper-color :as c]
             [js.react-native.helper-theme :as helper-theme]
             [js.react-native.helper-theme-default :as helper-theme-default]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.react-native.helper-theme/transformColor :added "4.0" :unchecked true}
(fact "transforms a color given a set of indicators, the type and pipeline"
  ^:hidden
  
  (!.js
   (helper-theme/transformColor
    {:hovering 0.5
     :pressing 0.1}
    {:bgNormal "white"
     :bgHovered "yellow"
     :bgPressed 0.2}
    "bg"
    "default"
    ["hovering"]
    {}))
  => [60 50 75]
  
  (!.js
   (helper-theme/transformColor
    {:hovering 0.5
     :pressing 0.1}
    {:bgNormal "white"
     :bgHovered "yellow"
     :bgPressed 0.2}
    "bg"
    "default"
    ["hovering"
     "pressing"]
    {}))
  => [60 50 69]

  (!.js
   (helper-theme/transformColor
    {:hovering 0.5
     :pressing 0.1}
    {:bgNormal "blue"
     :bgHovered "yellow"
     :bgPressed "darkred"}
    "bg"
    "default"
    ["hovering"
     "pressing"]
    {}))
  => [135 100 47.72549019607843])

^{:refer js.react-native.helper-theme/mergeProps :added "4.0" :unchecked true}
(fact "merges the transformed props"
  ^:hidden
  
  (!.js
   (helper-theme/mergeProps [{:style {:fontSize 10}}
                             {:style {:opacity 0.1}
                              :value 0.1}]))
  => {"style" [{"fontSize" 10}
               {"opacity" 0.1}],
      "value" 0.1})

^{:refer js.react-native.helper-theme/createCombinedTransformations :added "4.0" :unchecked true}
(fact "creates a combined transformations function"
  ^:hidden
  
  (!.js
   (var indicators {:pressing  0.1
                    :hovering  0.1
                    :active    0.1
                    :disabled  0.1
                    :highlighted 0.1})
   (var chord   {:pressing true
                 :hovering true
                 :active   true
                 :disabled true
                 :highlighted true})
   (var f (helper-theme/createCombinedTransformations
           {:theme {:bgNormal      "#f4f4f4"
                    :bgHovered       0.2
                    :bgPressed       0.6
                    :bgDisabled      "#ccc"
                    :bgHighlighted   "yellow"
                    :bgActive        "#555"
                    
                    :fgNormal        "#333"
                    :fgHovered       0.2
                    :fgPressed       "#ccc"
                    :fgDisabled      "#888"
                    :fgHighlighted   "black"
                    :fgActive        "white"}
            :themePipeline {:fg {:initial "default"
                                 :stages  ["highlighted"
                                           "hovering"
                                           "pressing"
                                           "disabled"]
                                 :values  {:hovering (fn:> [hovering #{focusing}]
                                                       (j/max hovering focusing))}}
                            :bg {:initial "default"
                                 :stages  ["highlighted"
                                           "hovering"
                                           "pressing"
                                           "disabled"]
                                 :values {:hovering (fn:> [hovering #{focusing}]
                                                      (j/max hovering focusing))}}}
            :transformations
            {:bg (fn [#{hovering
                        pressing}]
                   (return {:style {:opacity pressing}}))
             :fg (fn [#{hovering
                        pressing}]
                   (return {:style {:fontWeight (+ 10 (* 5 pressing))}}))}}))
   
   (f indicators chord))
  => {"style"
      [{"opacity" 0.1}
       {"fontWeight" 10.5}
       {"backgroundColor" "hsl(0,0.00%,8.00%)",
        "borderColor" "hsl(0,0.00%,8.00%)",
        "color" "hsl(0,0.00%,12.53%)"}]})

^{:refer js.react-native.helper-theme/createSingleTransformations :added "4.0" :unchecked true}
(fact "creates the transformation for split `fg`/`bg` controls"
  ^:hidden
  
  (!.js
   (var indicators {:pressing  0.1
                    :hovering  0.1
                    :active    0.1
                    :disabled  0.1
                    :highlighted 0.1})
   (var chord   {:pressing true
                 :hovering true
                 :active   true
                 :disabled true
                 :highlighted true})
   (var f (helper-theme/createSingleTransformations
           {:theme {:bgNormal      "#f4f4f4"
                    :bgHovered       0.2
                    :bgPressed       0.6
                    :bgDisabled      "#ccc"
                    :bgHighlighted   "yellow"
                    :bgActive        "#555"
                    
                    :fgNormal        "#333"
                    :fgHovered       0.2
                    :fgPressed       "#ccc"
                    :fgDisabled      "#888"
                    :fgHighlighted   "black"
                    :fgActive        "white"}
            :themePipeline {:fg {:initial "default"
                                 :stages  ["highlighted"
                                           "hovering"
                                           "pressing"
                                           "disabled"]
                                 :values  {:hovering (fn:> [hovering #{focusing}]
                                                       (j/max hovering focusing))}}
                            :bg {:initial "default"
                                 :stages  ["highlighted"
                                           "hovering"
                                           "pressing"
                                           "disabled"]
                                 :values {:hovering (fn:> [hovering #{focusing}]
                                                      (j/max hovering focusing))}}}
            :transformations
            {:bg (fn [#{hovering
                        pressing}]
                   (return {:style {:opacity pressing}}))
             :fg (fn [#{hovering
                        pressing}]
                   (return {:style {:fontWeight (+ 10 (* 5 pressing))}}))}}
           "fg"
           ["backgroundColor"]))
   (f indicators chord))
  => {"style" [{"fontWeight" 10.5}
               {"backgroundColor" "hsl(0,0.00%,12.53%)"}]})

^{:refer js.react-native.helper-theme/combinedStatic :added "4.0" :unchecked true}
(fact "creates a static style from transform function"
  ^:hidden
  
  (!.js
   (var f (helper-theme/createSingleTransformations
           {:theme {:bgNormal      "#f4f4f4"
                    :bgHovered       0.2
                    :bgPressed       0.6
                    :bgDisabled      "#ccc"
                    :bgHighlighted   "yellow"
                    :bgActive        "#555"
                    
                    :fgNormal        "#333"
                    :fgHovered       0.2
                    :fgPressed       "#ccc"
                    :fgDisabled      "#888"
                    :fgHighlighted   "black"
                    :fgActive        "white"}
            :themePipeline {:fg {:initial "default"
                                 :stages  ["highlighted"
                                           "hovering"
                                           "pressing"
                                           "disabled"]
                                 :values  {:hovering (fn:> [hovering #{focusing}]
                                                       (j/max hovering focusing))}}
                            :bg {:initial "default"
                                 :stages  ["highlighted"
                                           "hovering"
                                           "pressing"
                                           "disabled"]
                                 :values {:hovering (fn:> [hovering #{focusing}]
                                                      (j/max hovering focusing))}}}
            :transformations
            {:bg (fn [#{hovering
                        pressing}]
                   (return {:style {:opacity pressing}}))
             :fg (fn [#{hovering
                        pressing}]
                   (return {:style {:fontWeight (+ 10 (* 5 pressing))}}))}}
           "fg"
           ["backgroundColor"]))
   (helper-theme/combinedStatic {:disabled true
                                 :highlighted true}
                                nil
                                f))
  => [{"fontWeight" 10}
      {"backgroundColor" "hsl(0,0.00%,53.33%)"}])

^{:refer js.react-native.helper-theme/prepThemeCombined :added "4.0" :unchecked true}
(fact "prepares the combined theme"
  ^:hidden
  
  (!.js
   (var indicators {:pressing  0.1
                    :hovering  0.1
                    :active    0.1
                    :disabled  0.1
                    :highlighted 0.1})
   (var chord   {:pressing true
                 :hovering true
                 :active   true
                 :disabled true
                 :highlighted true})
   (var [styleStatic
         transformFn]
        (helper-theme/prepThemeCombined
            {:theme {:bgNormal      "#f4f4f4"
                     :bgHovered       0.2
                     :bgPressed       0.6
                     :bgDisabled      "#ccc"
                     :bgHighlighted   "yellow"
                     :bgActive        "#555"
                    
                     :fgNormal        "#333"
                     :fgHovered       0.2
                     :fgPressed       "#ccc"
                     :fgDisabled      "#888"
                     :fgHighlighted   "black"
                     :fgActive        "white"}
             :themePipeline {:fg {:initial "default"
                                  :stages  ["highlighted"
                                            "hovering"
                                            "pressing"
                                            "disabled"]
                                  :values  {:hovering (fn:> [hovering #{focusing}]
                                                        (j/max hovering focusing))}}
                             :bg {:initial "default"
                                  :stages  ["highlighted"
                                            "hovering"
                                            "pressing"
                                            "disabled"]
                                  :values {:hovering (fn:> [hovering #{focusing}]
                                                       (j/max hovering focusing))}}}
             :transformations
             {:bg (fn [#{hovering
                         pressing}]
                    (return {:style {:opacity pressing}}))
              :fg (fn [#{hovering
                         pressing}]
                    (return {:style {:fontWeight (+ 10 (* 5 pressing))}}))}}))
   {:static styleStatic
    :dynamic (transformFn indicators chord)})
  => {"dynamic"
      {"style"
       [{"opacity" 0.1}
        {"fontWeight" 10.5}
        {"backgroundColor" "hsl(0,0.00%,8.00%)",
         "borderColor" "hsl(0,0.00%,8.00%)",
         "color" "hsl(0,0.00%,12.53%)"}]},
      "static"
      [{"opacity" 0}
       {"fontWeight" 10}
       {"backgroundColor" "hsl(60,0.00%,95.69%)",
        "borderColor" "hsl(60,0.00%,95.69%)",
        "color" "hsl(0,0.00%,20.00%)"}]})

^{:refer js.react-native.helper-theme/prepThemeSingle :added "4.0" :unchecked true}
(fact "prepares the static style and dynamic transform function"
  ^:hidden
  
  (!.js
   (var indicators {:pressing  0.1
                    :hovering  0.1
                    :active    0.1
                    :disabled  0.1
                    :highlighted 0.1})
   (var chord   {:pressing true
                 :hovering true
                 :active   true
                 :disabled true
                 :highlighted true})
   (var [styleStatic
         transformFn]
        (helper-theme/prepThemeSingle
            {:theme {:bgNormal      "#f4f4f4"
                     :bgHovered       0.2
                     :bgPressed       0.6
                     :bgDisabled      "#ccc"
                     :bgHighlighted   "yellow"
                     :bgActive        "#555"
                    
                     :fgNormal        "#333"
                     :fgHovered       0.2
                     :fgPressed       "#ccc"
                     :fgDisabled      "#888"
                     :fgHighlighted   "black"
                     :fgActive        "white"}
             :themePipeline {:fg {:initial "default"
                                  :stages  ["highlighted"
                                            "hovering"
                                            "pressing"
                                            "disabled"]
                                  :values  {:hovering (fn:> [hovering #{focusing}]
                                                        (j/max hovering focusing))}}
                             :bg {:initial "default"
                                  :stages  ["highlighted"
                                            "hovering"
                                            "pressing"
                                            "disabled"]
                                  :values {:hovering (fn:> [hovering #{focusing}]
                                                       (j/max hovering focusing))}}}
             :transformations
             {:bg (fn [#{hovering
                         pressing}]
                    (return {:style {:opacity pressing}}))
              :fg (fn [#{hovering
                         pressing}]
                    (return {:style {:fontWeight (+ 10 (* 5 pressing))}}))}}
            "fg"
            ["color"]))
   {:static styleStatic
    :dynamic (transformFn indicators chord)})
  => {"dynamic"
      {"style" [{"fontWeight" 10.5} {"color" "hsl(0,0.00%,12.53%)"}]},
      "static" [{"fontWeight" 10} {"color" "hsl(0,0.00%,20.00%)"}]})
