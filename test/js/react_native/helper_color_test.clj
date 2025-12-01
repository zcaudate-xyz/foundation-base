(ns js.react-native.helper-color-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[js.core :as j]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.react-native.helper-color :as c]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.react-native.helper-color/toHSL :added "4.0" :unchecked true}
(fact "converts vector to HSL prop"
  ^:hidden
  
  (c/toHSL [1 2 0.3])
  => "hsl(1,2.00%,0.30%)")

^{:refer js.react-native.helper-color/hsl-parse-raw :added "4.0" :unchecked true}
(fact "converts string to hsl"
  ^:hidden
  
  (c/hsl-parse-raw "rgb(255,0,255)"
                   3
                   j/parseInt)
  => [255 0 255]

  (c/hsl-parse-raw "rbga(255,0,255)"
                   4
                   j/parseInt)
  => [255 0 255]

  (c/hsl-parse-raw "hsl(300, 100%, 0.5%)"
                   3
                   j/parseFloat)
  => [300 100 0.5])

^{:refer js.react-native.helper-color/hsl-parse :added "4.0" :unchecked true}
(fact "parses hsl value from string"
  ^:hidden
  
  (c/hsl-parse "rgb(255,0,255)")
  =>[300 100 50]
  
  
  (c/hsl-parse "rgb(255,100,255)")
  => [300 100 69.6078431372549]
  
  (c/hsl-parse "rgb(254,254,254)")
  => [0 0 99.6078431372549]

  (c/hsl-parse "hsl(300, 100%, 0.5%)")
  => [300 100 0.5])

^{:refer js.react-native.helper-color/hsl :added "4.0" :unchecked true}
(fact "general convertion to hsl"
  ^:hidden
  
  (c/hsl "red")
  => [0 100 50]

  (c/hsl "blue")
  => [240 100 50]

  (c/hsl "yellow")
  => [60 100 50]

  (c/hsl "#eee")
  => [0 0 93.33333333333333]

  (c/hsl "#ddd")
  => [0 0 86.66666666666667])

^{:refer js.react-native.helper-color/interpolateScalar :added "4.0" :unchecked true}
(fact "interpolates two scalar values"
  ^:hidden
  
  (c/interpolateScalar 100 80 0.2)
  => 96

  (c/interpolateScalar 100 0 0.2)
  => 80

  (c/interpolateScalar 0 100 0.2)
  => 20)

^{:refer js.react-native.helper-color/interpolateValue :added "4.0" :unchecked true}
(fact "interpolates given a function"
  ^:hidden
  
  (!.js
   (c/interpolateValue 100 (fn:> [x] (* x 0.5)) 0.6))
  => 70

  (!.js
   (c/interpolateValue  (fn:> [x] (* x 0.5)) 100 0.6))
  => 80

  (!.js
   (c/interpolateValue  50 100 0.6))
  => 80)

^{:refer js.react-native.helper-color/interpolateNum :added "4.0" :unchecked true}
(fact "creates a interpolation for number"
  ^:hidden
  
  (c/interpolateNum 10 0.12)
  => 1.2

  (c/interpolateNum 10 0.01)
  => 0.1

  (c/interpolateNum 10 0.99)
  => 9.9
  
  (c/interpolateNum 10 -0.12)
  => 89.2

  (c/interpolateNum 10 -0.01)
  => 99.1

  (c/interpolateNum 10 -0.99)
  => 10.900000000000006

  (c/interpolateNum 10 5)
  => 15

  (c/interpolateNum 10 -5)
  => 5)

^{:refer js.react-native.helper-color/interpolateColorArray :added "4.0" :unchecked true}
(fact "creates a color array if a digit"
  ^:hidden
  
  (c/interpolateColorArray [100 0 10] 10)
  => [[100 0 10]
      [100 0 20]]

  (c/interpolateColorArray -10 [100 0 60])
  => [[100 0 50]
      [100 0 60]]
  
  (!.js
   (c/interpolateColorArray (fn:> [x] (* x 0.5)) [100 0 60]))
  => [[100 0 nil] [100 0 60]])

^{:refer js.react-native.helper-color/interpolateColor :added "4.0" :unchecked true}
(fact "interpolates color given function or "
  ^:hidden
  
  (!.js
   (c/interpolateColor "red" "green"
                       0.8))
  => [96 100 30.07843137254902]
  
  
  (!.js
   (c/interpolateColor (fn:> [x] (* x 0.5)) [100 0 60]
                       0.8))
  => [100 0 54]
  

  (!.js
   (c/interpolateColor 10 [100 0 60]
                       0.8))
  => [100 0 62])

^{:refer js.react-native.helper-color/interpolate :added "4.0" :unchecked true}
(fact "interpolates a range of values"
  ^:hidden
  
  (!.js
   (c/interpolate ["red" "green" "yellow"]
                  0.8))
  => [96 100 30.07843137254902]

  (!.js
   (c/interpolate ["red" "green" "yellow"]
                  1.8))
  => [72 100 45.01960784313725])

^{:refer js.react-native.helper-color/rotateHue :added "4.0" :unchecked true}
(fact "rotates hue"
  ^:hidden
  
  (!.js
   (c/rotateHue "red" 0))
  => "hsl(0,100.00%,50.00%)"

  (!.js
   (c/rotateHue "red" 0.5))
  => "hsl(180,100.00%,50.00%)")

^{:refer js.react-native.helper-color/saturate :added "4.0" :unchecked true}
(fact "interpolates the saturation for the color"
  ^:hidden
  
  (!.js
   (c/saturate "red" 0.4))
  => "hsl(0,40.00%,50.00%)"

  (!.js
   (c/saturate "red" -0.4))
  => "hsl(0,100.00%,50.00%)")

^{:refer js.react-native.helper-color/lighten :added "4.0" :unchecked true}
(fact "interpolates the lightness for the color"
  ^:hidden
  
  (!.js
   (c/lighten "red" 0.4))
  => "hsl(0,100.00%,20.00%)"

  (!.js
   (c/lighten "red" -0.4))
  => "hsl(0,100.00%,80.00%)")

^{:refer js.react-native.helper-color/transform :added "4.0" :unchecked true}
(fact "transforms the color through a fraction array"
  ^:hidden
  
  (!.js
   (c/transform "red" [0.2 0.4 0.3]))
  => "hsl(72,40.00%,15.00%)")

^{:refer js.react-native.helper-color/mix :added "4.0" :unchecked true}
(fact "converts a range of values to hsl"
  ^:hidden
  
  (!.js
   (c/mix ["red" "green" "yellow"]
          0.8))
  => "hsl(96,100.00%,30.08%)"

  (!.js
   (c/mix ["red" "green" "yellow"]
          1.8))
  => "hsl(72,100.00%,45.02%)")

^{:refer js.react-native.helper-color/toRGB :added "4.0" :unchecked true}
(fact "transforms a hsl string to rgb"
  ^:hidden
  
  (!.js
   (c/toRGB
    "hsl(72,100.00%,45.02%)"))
  => "#b8e60")
