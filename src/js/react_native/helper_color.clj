(ns js.react-native.helper-color
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :require [[js.core :as j]
             [xt.lang.util-color :as c]
             [xt.lang.base-lib :as k]]})

;;
;; hsl function
;;

(defn.js toHSL
  "converts vector to HSL prop"
  {:added "4.0"}
  [x]
  (cond (k/is-string? x)
        (return x)

        (k/arr? x)
        (do (var [h s l] x)
            (return (+ "hsl("
                       (j/floor h) ","
                       (j/toFixed s 2) "%,"
                       (j/toFixed l 2) "%" ")")))))

(defn.js hsl-parse-raw
  "converts string to hsl"
  {:added "4.0"}
  [s n parseFn]
  (var curr (j/substring s n))
  (var arr  (j/split curr #"[,\(\)\%\s]"))
  (return (-> (j/filter arr (fn:> [s] (> (k/len s) 0)))
              (j/splice 0 3)
              (j/map (fn:> [s] (parseFn s))))))

(defn.js hsl-parse
  "parses hsl value from string"
  {:added "4.0"}
  [s]
  (var parse-rgb
       (fn [str n]
         (return (c/rgb->hsl (-/hsl-parse-raw str n j/parseInt)))))
  
  (cond (j/startsWith s "#")
        (return (c/hex->hsl s))
        
        (j/startsWith s "rgb(")
        (return (parse-rgb s 3))
        
        (j/startsWith s "rgba(")
        (return (parse-rgb s 4))
        
        (j/startsWith s "hsl(")
        (return (-/hsl-parse-raw s 3 j/parseFloat))
        
        (j/startsWith s "hsla(")
        (return (-/hsl-parse-raw s 4 j/parseFloat))
        
        :else
        (try 
          (return (c/named->hsl s))
          (catch e (return [0 0 0])))))

(defn.js hsl
  "general convertion to hsl"
  {:added "4.0"}
  [s]
  (cond (k/arr? s)
        (return s)

        (k/is-string? s)
        (return (-/hsl-parse s))

        :else
        (return s)))

;;
;;
;;

(defn.js interpolateScalar
  "interpolates two scalar values"
  {:added "4.0"}
  [from to fraction]
  (return (+ (or from 0) (* fraction (- (or to 0)
                                        (or from 0))))))

(defn.js interpolateValue
  "interpolates given a function"
  {:added "4.0"}
  [from to fraction]
  (cond (k/fn? from)
        (return (-/interpolateScalar (from to) to fraction))

        (k/fn? to)
        (return (-/interpolateScalar from (to from) fraction))

        :else
        (return (-/interpolateScalar from to fraction))))

(defn.js interpolateNum
  "creates a interpolation for number"
  {:added "4.0"}
  [v num]
  (cond (and (< num 1)
             (> num 0))
        (return (* num v))
        
        (and (> num -1)
             (< num 0))
        (return (+ 100 (* (- 100 v) num)))
        
        :else
        (return (+ num v))))

(defn.js interpolateColorArray
  "creates a color array if a digit"
  {:added "4.0"}
  [from to]
  (cond (k/is-number? to)
        (return [from
                 [(. from [0])
                  (. from [1])
                  (-/interpolateNum (. from [2]) to)]])

        (k/is-number? from)
        (return [[(. to [0])
                  (. to [1])
                  (-/interpolateNum (. to [2]) from)]
                 to])

        (k/fn? to)
        (return [from
                 [(. from [0])
                  (. from [1])
                  to]])

        (k/fn? from)
        (return [[(. to [0])
                  (. to [1])
                  from]
                 to])

        (k/nil? to)
        (return [from from])

        (k/nil? from)
        (return [to to])

        (== (. to [1]) 0)
        (return [from
                 [(. from [0])
                  (. to [1])
                  (. to [2])]])
        
        (== (. from [1]) 0)
        (return [[(. to [0])
                  (. from [1])
                  (. from [2])]
                 to])
        
        :else
        (return [from to])))

(defn.js interpolateColor
  "interpolates color given function or"
  {:added "4.0"}
  [from to fraction]
  (var [fromArr toArr] (-/interpolateColorArray
                        (-/hsl from)
                        (-/hsl to)))
  
  (when fromArr
    (return (j/map fromArr
                   (fn:> [v i] (-/interpolateValue
                                v
                                (. toArr [i])
                                
                                fraction)))))
  (return (or toArr [0 0 0])))

(defn.js interpolate
  "interpolates a range of values"
  {:added "4.0"}
  [arr num]
  (var i (j/max 0 (j/ceil (- num 1))))
  (var fraction (- num i))
  (var from (. arr [i]))
  (var to   (. arr [(+ i 1)]))
  (return (-/interpolateColor from
                              to
                              fraction)))

(defn.js rotateHue
  "rotates hue"
  {:added "4.0"}
  [color fraction]
  (var [h s l] (-/hsl color))
  (return (-/toHSL [(mod (+ h (* 360 fraction))
                         360)
                    s l])))

(defn.js saturate
  "interpolates the saturation for the color"
  {:added "4.0"}
  [color fraction]
  (var [h s l] (-/hsl color))
  (return (-/toHSL [h (-/interpolateNum s fraction) l])))

(defn.js lighten
  "interpolates the lightness for the color"
  {:added "4.0"}
  [color fraction]
  (var [h s l] (-/hsl color))
  (return (-/toHSL [h s (-/interpolateNum l fraction)])))

(defn.js transform
  "transforms the color through a fraction array"
  {:added "4.0"}
  [color [hf sf lf]]
  (var [h s l] (-/hsl color))
  (return (-/toHSL [(mod (+ h (* 360 hf))
                         360)
                    (-/interpolateNum s sf)
                    (-/interpolateNum l lf)])))

;;
;;
;;

(defn.js mix
  "converts a range of values to hsl"
  {:added "4.0"}
  [arr num]
  (return (-/toHSL (-/interpolate arr num))))

(defn.js toRGB
  "transforms a hsl string to rgb"
  {:added "4.0"}
  [s]
  (var arr (c/hsl->rgb (-/hsl-parse s)))
  (return arr (+ "#" (-> arr
                         (j/map (fn:> [x]
                                  (j/padStart (. x (toString 16))
                                              "0"
                                              2)))
                         (j/join "")))))

