(ns js.lib.color
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :js
  {:import [["@csstools/convert-colors" :as [* Colors]]]})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Colors"
                                   :tag "js"}]
  [hex2ciede
   hex2contrast
   hex2hsl
   hex2hsv
   hex2hwb
   hex2lab
   hex2lch
   hex2rgb
   hex2xyz
   hsl2ciede
   hsl2contrast
   hsl2hex
   hsl2hsv
   hsl2hwb
   hsl2lab
   hsl2lch
   hsl2rgb
   hsl2xyz
   hsv2ciede
   hsv2contrast
   hsv2hex
   hsv2hsl
   hsv2hwb
   hsv2lab
   hsv2lch
   hsv2rgb
   hsv2xyz
   hwb2ciede
   hwb2contrast
   hwb2hex
   hwb2hsl
   hwb2hsv
   hwb2lab
   hwb2lch
   hwb2rgb
   hwb2xyz
   keyword2ciede
   keyword2contrast
   keyword2hex
   keyword2hsl
   keyword2hsv
   keyword2hwb
   keyword2lab
   keyword2lch
   keyword2rgb
   lab2ciede
   lab2contrast
   lab2hsl
   lab2hsv
   lab2hwb
   lab2lch
   lab2rgb
   lab2xyz
   lch2ciede
   lch2contrast
   lch2hex
   lch2hsl
   lch2hsv
   lch2hwb
   lch2lab
   lch2rgb
   lch2xyz
   rgb2ciede
   rgb2contrast
   rgb2hex
   rgb2hsl
   rgb2hsv
   rgb2hwb
   rgb2lab
   rgb2lch
   rgb2xyz
   xyz2ciede
   xyz2contrast
   xyz2hex
   xyz2hsl
   xyz2hsv
   xyz2hwb
   xyz2lab
   xyz2lch
   xyz2rgb])
