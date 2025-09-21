(ns xt.lang.util-color
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]
   :export [MODULE]})

(def.xt HEX
  (tab ["0" 0]
       ["1" 1]
       ["2" 2]
       ["3" 3]
       ["4" 4]
       ["5" 5]
       ["6" 6]
       ["7" 7]
       ["8" 8]
       ["9" 9]
       ["A" 10]
       ["B" 11]
       ["C" 12]
       ["D" 13]
       ["E" 14]
       ["F" 15]))

(def.xt LU
  (tab [0 "0"]
       [1 "1"]
       [2 "2"]
       [3 "3"]
       [4 "4"]
       [5 "5"]
       [6 "6"]
       [7 "7"]
       [8 "8"]
       [9 "9"]
       [10 "A"]
       [11 "B"]
       [12 "C"]
       [13 "D"]
       [14 "E"]
       [15 "F"]))

(def.xt NAMED
  (tab :aliceblue [240 248 255]
       :antiquewhite [250 235 215]
       :aqua [0 255 255]
       :aquamarine [127 255 212]
       :azure [240 255 255]
       :beige [245 245 220]
       :bisque [255 228 196]
       :black [0 0 0]
       :blanchedalmond [255 235 205]
       :blue [0 0 255]
       :blueviolet [138 43 226]
       :brown [165 42 42]
       :burlywood [222 184 135]
       :cadetblue [95 158 160]
       :chartreuse [127 255 0]
       :chocolate [210 105 30]
       :coral [255 127 80]
       :cornflowerblue [100 149 237]
       :cornsilk [255 248 220]
       :crimson [220 20 60]
       :cyan [0 255 255]
       :darkblue [0 0 139]
       :darkcyan [0 139 139]
       :darkgoldenrod [184 134 11]
       :darkgray [169 169 169]
       :darkgreen [0 100 0]
       :darkgrey [169 169 169]
       :darkkhaki [189 183 107]
       :darkmagenta [139 0 139]
       :darkolivegreen [85 107 47]
       :darkorange [255 140 0]
       :darkorchid [153 50 204]
       :darkred [139 0 0]
       :darksalmon [233 150 122]
       :darkseagreen [143 188 143]
       :darkslateblue [72 61 139]
       :darkslategray [47 79 79]
       :darkslategrey [47 79 79]
       :darkturquoise [0 206 209]
       :darkviolet [148 0 211]
       :deeppink [255 20 147]
       :deepskyblue [0 191 255]
       :dimgray [105 105 105]
       :dimgrey [105 105 105]
       :dodgerblue [30 144 255]
       :firebrick [178 34 34]
       :floralwhite [255 250 240]
       :forestgreen [34 139 34]
       :fuchsia [255 0 255]
       :gainsboro [220 220 220]
       :ghostwhite [248 248 255]
       :gold [255 215 0]
       :goldenrod [218 165 32]
       :gray [128 128 128]
       :green [0 128 0]
       :greenyellow [173 255 47]
       :grey [128 128 128]
       :honeydew [240 255 240]
       :hotpink [255 105 180]
       :indianred [205 92 92]
       :indigo [75 0 130]
       :ivory [255 255 240]
       :khaki [240 230 140]
       :lavender [230 230 250]
       :lavenderblush [255 240 245]
       :lawngreen [124 252 0]
       :lemonchiffon [255 250 205]
       :lightblue [173 216 230]
       :lightcoral [240 128 128]
       :lightcyan [224 255 255]
       :lightgoldenrodyellow [250 250 210]
       :lightgray [211 211 211]
       :lightgreen [144 238 144]
       :lightgrey [211 211 211]
       :lightpink [255 182 193]
       :lightsalmon [255 160 122]
       :lightseagreen [32 178 170]
       :lightskyblue [135 206 250]
       :lightslategray [119 136 153]
       :lightslategrey [119 136 153]
       :lightsteelblue [176 196 222]
       :lightyellow [255 255 224]
       :lime [0 255 0]
       :limegreen [50 205 50]
       :linen [250 240 230]
       :magenta [255 0 255]
       :maroon [128 0 0]
       :mediumaquamarine [102 205 170]
       :mediumblue [0 0 205]
       :mediumorchid [186 85 211]
       :mediumpurple [147 112 219]
       :mediumseagreen [60 179 113]
       :mediumslateblue [123 104 238]
       :mediumspringgreen [0 250 154]
       :mediumturquoise [72 209 204]
       :mediumvioletred [199 21 133]
       :midnightblue [25 25 112]
       :mintcream [245 255 250]
       :mistyrose [255 228 225]
       :moccasin [255 228 181]
       :navajowhite [255 222 173]
       :navy [0 0 128]
       :oldlace [253 245 230]
       :olive [128 128 0]
       :olivedrab [107 142 35]
       :orange [255 165 0]
       :orangered [255 69 0]
       :orchid [218 112 214]
       :palegoldenrod [238 232 170]
       :palegreen [152 251 152]
       :paleturquoise [175 238 238]
       :palevioletred [219 112 147]
       :papayawhip [255 239 213]
       :peachpuff [255 218 185]
       :peru [205 133 63]
       :pink [255 192 203]
       :plum [221 160 221]
       :powderblue [176 224 230]
       :purple [128 0 128]
       :rebeccapurple [102 51 153]
       :red [255 0 0]
       :rosybrown [188 143 143]
       :royalblue [65 105 225]
       :saddlebrown [139 69 19]
       :salmon [250 128 114]
       :sandybrown [244 164 96]
       :seagreen [46 139 87]
       :seashell [255 245 238]
       :sienna [160 82 45]
       :silver [192 192 192]
       :skyblue [135 206 235]
       :slateblue [106 90 205]
       :slategray [112 128 144]
       :slategrey [112 128 144]
       :snow [255 250 250]
       :springgreen [0 255 127]
       :steelblue [70 130 180]
       :tan [210 180 140]
       :teal [0 128 128]
       :thistle [216 191 216]
       :tomato [255 99 71]
       :transparent [0 0 0]
       :turquoise [64 224 208]
       :violet [238 130 238]
       :wheat [245 222 179]
       :white [255 255 255]
       :whitesmoke [245 245 245]
       :yellow [255 255 0]
       :yellowgreen [154 205 50]))

(defn.xt named->rgb
  "named color to rgb"
  {:added "4.0"}
  [s]
  (return (or (k/get-key -/NAMED s)
              [0 0 0])))

(defn.xt hex->n
  "hex to rgb val"
  {:added "4.0"}
  [s]
  (return (or (k/get-key -/HEX (k/to-uppercase s))
              0)))

(defn.xt n->hex
  "converts an rgb to hex"
  {:added "4.0"}
  [n]
  (var v1 (k/quot n 16))
  (var v0 (k/mod-pos n 16))
  (return (k/cat (or (k/get-key -/LU v1) "0")
                 (or (k/get-key -/LU v0) "0"))))

(defn.xt hex->rgb
  "converts a hex value to rgb array"
  {:added "4.0"}
  [s]
  (var val-fn (fn:> [s start finish]
                (-/hex->n (k/substring s start finish))))
  (when (not (k/starts-with? s "#"))
    (k/err "Not a valid hex color."))
  (when (== 4 (k/len s))
    (var r (val-fn s 1 2))
    (var g (val-fn s 2 3))
    (var b (val-fn s 3 4))
    (return [(+ (* r 16) r)
             (+ (* g 16) g)
             (+ (* b 16) b)]))
  (when (== 7 (k/len s))
    (var r1 (val-fn s 1 2))
    (var r0 (val-fn s 2 3))
    (var g1 (val-fn s 3 4))
    (var g0 (val-fn s 4 5))
    (var b1 (val-fn s 5 6))
    (var b0 (val-fn s 6 7))
    (return [(+ (* r1 16) r0)
             (+ (* g1 16) g0)
             (+ (* b1 16) b0)]))
  (return [0 0 0]))

(defn.xt rgb->hex
  "converts rgb to hex"
  {:added "4.0"}
  [rgb]
  (var [r g b] rgb)
  (return (k/cat "#"
                 (-/n->hex r)
                 (-/n->hex g)
                 (-/n->hex b))))

(defn.xt rgb->hue
  "helper function for rgb->hsl"
  {:added "4.0"}
  [r g b value delta fallback]
  (when (== 0 delta)
    (return fallback))

  (var segment)
  (cond (== value r)
        (:= segment (/ (- g b) delta))
        
        (== value g)
        (:= segment (/ (- b r) delta))

        :else
        (:= segment (/ (- r g) delta)))
  

  (var shift)
  (cond (== value r)
        (if (< segment 0)
          (:= shift 6)
          (:= shift 0))

        (== value g)
        (:= shift 2)
        
        :else
        (:= shift 4))
  
  (return (* 60 (+ segment shift))))

(defn.xt rgb->hsl
  "converts rgb to hsl"
  {:added "4.0"}
  [rgb fallback]
  (var [r g b] rgb)
  (var value (k/max r g b))
  (var whiteness (k/min r g b))
  (var delta (- value whiteness))
  (var h (-/rgb->hue r g b value delta (or fallback 0)))
  (var l (/ (* 100
               (+ value whiteness) 0.5)
            255))
  (var s nil)
  (if (== delta 0)
    (:= s 0)
    (:= s (/ (* (/ delta 255) 10000)
             (- 100 (k/abs (- (* 2 l) 100))))))
  (return [h
           (k/min 100 s)
           (k/min 100 l)]))

(defn.xt hue->v
  "converts a hue to a value"
  {:added "4.0"}
  [t1 t2 h]
  (cond (< h 0) (:= h (+ h 1))
        (> h 1) (:= h (- h 1)))

  (cond (< (* 6 h) 1)
        (return (+ t1 (* (- t2 t1) 6 h)))

        (< (* 2 h) 1)
        (return t2)

        (< (* 3 h) 2)
        (return (+ t1 (* (- t2 t1) 6 (- (/ 2 3) h))))

        :else (return t1)))

(defn.xt hsl->rgb
  "converts hsl to rgb"
  {:added "4.0"}
  [hsl]
  (var [hi si li] hsl)
  (var h (/ hi 360))
  (var s (/ si 100))
  (var l (/ li 100))
  (if (== s 0)
    (return [(k/round (* 255 l))
             (k/round (* 255 l))
             (k/round (* 255 l))])
    (let [t2 (:? (< l 0.5)
                 (* l (+ s 1))
                 (- (+ l s) (* s l)))
          t1 (- (* 2 l) t2)]
      (return [(k/round (* 255 (-/hue->v t1 t2 (+ h (/ 1 3)))))
               (k/round (* 255 (-/hue->v t1 t2 h)))
               (k/round (* 255 (-/hue->v t1 t2 (- h (/ 1 3)))))]))))

(defn.xt named->hsl
  "converts a named color to hsl"
  {:added "4.0"}
  [s]
  (return (-/rgb->hsl (-/named->rgb s))))

(defn.xt named->hex
  "converts a named color to hex"
  {:added "4.0"}
  [s]
  (return (-/rgb->hex (-/named->rgb s))))

(defn.xt hex->hsl
  "converts a hex to hsl"
  {:added "4.0"}
  [s]
  (return (-/rgb->hsl (-/hex->rgb s))))

(def.xt MODULE (!:module))
