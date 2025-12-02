(ns js.react-native.model-roller-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script- :js
    {:runtime :basic
     :require [[js.react-native.model-roller :as  model-roller]
               [js.core :as j]
               [xt.lang.base-lib :as k]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.react-native.model-roller/roller-model :added "4.0" :unchecked true}
(fact "constructs a roller model"
  ^:hidden
  
  (!.js
   (j/map (k/arr-range 4)
          (model-roller/roller-model 4 10)))
  => [{"offset" 0,
       "translate" 0,
       "scale" 1,
       "visible" true,
       "theta" 0,
       "raw" 0}
      {"offset" 1,
       "translate" 10,
       "scale" 6.123233995736766E-17,
       "visible" false,
       "theta" 1.5707963267948966,
       "raw" 1}
      {"offset" 2,
       "translate" 1.2246467991473533E-15,
       "scale" 1,
       "visible" false,
       "theta" 3.141592653589793,
       "raw" 2}
      {"offset" 3,
       "translate" -10,
       "scale" 1.8369701987210297E-16,
       "visible" false,
       "theta" 4.71238898038469,
       "raw" 3}]

  (!.js
   (j/map (k/arr-range 10)
          (model-roller/roller-model 10 10)))
  => [{"offset" 0,
       "translate" 0,
       "scale" 1,
       "visible" true,
       "theta" 0,
       "raw" 0}
      {"offset" 1,
       "translate" 5.877852522924732,
       "scale" 0.8090169943749475,
       "visible" true,
       "theta" 0.6283185307179586,
       "raw" 1}
      {"offset" 2,
       "translate" 9.510565162951535,
       "scale" 0.30901699437494745,
       "visible" true,
       "theta" 1.2566370614359172,
       "raw" 2}
      {"offset" 3,
       "translate" 9.510565162951536,
       "scale" 0.30901699437494734,
       "visible" false,
       "theta" 1.8849555921538759,
       "raw" 3}
      {"offset" 4,
       "translate" 5.877852522924733,
       "scale" 0.8090169943749473,
       "visible" false,
       "theta" 2.5132741228718345,
       "raw" 4}
      {"offset" 5,
       "translate" 1.2246467991473533E-15,
       "scale" 1,
       "visible" false,
       "theta" 3.141592653589793,
       "raw" 5}
      {"offset" 6,
       "translate" -5.87785252292473,
       "scale" 0.8090169943749475,
       "visible" false,
       "theta" 3.7699111843077517,
       "raw" 6}
      {"offset" 7,
       "translate" -9.510565162951535,
       "scale" 0.30901699437494756,
       "visible" false,
       "theta" 4.39822971502571,
       "raw" 7}
      {"offset" 8,
       "translate" -9.510565162951536,
       "scale" 0.30901699437494723,
       "visible" true,
       "theta" 5.026548245743669,
       "raw" 8}
      {"offset" 9,
       "translate" -5.877852522924734,
       "scale" 0.8090169943749473,
       "visible" true,
       "theta" 5.654866776461628,
       "raw" 9}])

^{:refer js.react-native.model-roller/roller-shifted-norm :added "4.0" :unchecked true}
(fact "finds the shifted-norm for an index at center"
  ^:hidden
  
  (!.js
   (j/map (k/arr-range 4)
          (fn:> [index]
            (j/map (k/arr-range 4)
                   (fn:> [center]
                     (model-roller/roller-shifted-norm 4 index center))))))
  => [[0 -1 -2 1]
      [1 0 -1 -2]
      [-2 1 0 -1]
      [-1 -2 1 0]]

  (!.js
   (j/map (k/arr-range 7)
          (fn:> [index]
            (j/map (k/arr-range 7)
                   (fn:> [center]
                     (model-roller/roller-shifted-norm 7 index center))))))
  => [[0 -1 -2 -3 3 2 1]
      [1 0 -1 -2 -3 3 2]
      [2 1 0 -1 -2 -3 3]
      [3 2 1 0 -1 -2 -3]
      [-3 3 2 1 0 -1 -2]
      [-2 -3 3 2 1 0 -1]
      [-1 -2 -3 3 2 1 0]])     

^{:refer js.react-native.model-roller/roller-shifted-index :added "4.0" :unchecked true}
(fact "finds shifted index for roller divisions"
  ^:hidden
  
  (!.js
   (j/map (k/arr-range 7)
          (fn:> [roller-index]
            (j/map (k/arr-range 10)
                   (fn:> [input-raw]
                     (model-roller/roller-shifted-index
                      7
                      roller-index
                      input-raw
                      10))))))
  => [[0 0 0 0 7 7 7 7 7 7]
      [1 1 1 1 1 8 8 8 8 8]
      [2 2 2 2 2 2 9 9 9 9]
      [3 3 3 3 3 3 3 0 0 0]
      [7 4 4 4 4 4 4 4 1 1]
      [8 8 5 5 5 5 5 5 5 2]
      [9 9 9 6 6 6 6 6 6 6]]

  (!.js
   (j/map (k/arr-range 6)
          (fn:> [roller-index]
            (j/map (k/arr-range 10)
                   (fn:> [input-raw]
                     (model-roller/roller-shifted-index
                      6
                      roller-index
                      input-raw
                      10))))))
  => [[0 0 0 0 6 6 6 6 6 6]
      [1 1 1 1 1 7 7 7 7 7]
      [2 2 2 2 2 2 8 8 8 8]
      [7 3 3 3 3 3 3 9 9 9]
      [8 8 4 4 4 4 4 4 0 0]
      [9 9 9 5 5 5 5 5 5 1]])

^{:refer js.react-native.model-roller/roller-set-values :added "4.0" :unchecked true}
(fact "sets roller values given array of animated values")
