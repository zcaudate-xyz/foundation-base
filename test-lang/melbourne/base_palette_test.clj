(ns melbourne.base-palette-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [js.react-native.helper-color :as c]
             [melbourne.base-palette :as base-palette]]
   :export [MODULE]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

^{:refer melbourne.base-palette/createPalette :added "4.0"}
(fact "gets the palette given type and color"
  ^:hidden
  
  (base-palette/createPalette "light" "purple")
  => {"mainWarn" "#ffffe0",
      "mainBackground" "#eee",
      "mainError" "#9c1f1f",
      "mainPrimary" "#5f4abd",
      "isDark" false,
      "mainNeutral" "#333"})

^{:refer melbourne.base-palette/toneFlatten :added "4.0"}
(fact "flattens the color to the center"
  ^:hidden
  
  (base-palette/toneFlatten "#5f4abd" true)
  => "hsl(250,46.56%,61.25%)")

^{:refer melbourne.base-palette/toneDiminish :added "4.0"}
(fact "diminishes the color to the center"
  ^:hidden

  (base-palette/toneDiminish "#5f4abd" true)
  => "hsl(250,46.56%,52.54%)")

^{:refer melbourne.base-palette/toneAugment :added "4.0"}
(fact "augments the color to the edge"
  ^:hidden

  (base-palette/toneAugment "#5f4abd" true)
  => "hsl(250,46.56%,46.41%)")

^{:refer melbourne.base-palette/toneSharpen :added "4.0"}
(fact "sharpens the color to the edge"
  ^:hidden

  (base-palette/toneSharpen "#5f4abd" true)
  => "hsl(250,46.56%,41.25%)")

^{:refer melbourne.base-palette/toneRatio :added "4.0"}
(fact "gets the tone ratio either for musical modes, ratios and whole tones"
  ^:hidden
  
  (base-palette/toneRatio "lydian")
  => 0.5

  (base-palette/toneRatio 6)
  => 0.75)

^{:refer melbourne.base-palette/getColorRaw :added "4.0"}
(fact "gets a color given palette, key, tone and additional parameters"
  ^:hidden
  
  (!.js
   (base-palette/getColorRaw
    (base-palette/createPalette "light" "purple")
    "primary"
    "augment"))
  => "hsl(250,46.56%,56.41%)")

^{:refer melbourne.base-palette/getColor :added "4.0"}
(fact "gets the color given a map"
  ^:hidden

  (!.js
   (base-palette/getColor
    (base-palette/createPalette "light" "purple")
    {:key "primary"
     :tone "augment"}))
  => "hsl(250,46.56%,56.41%)")

^{:refer melbourne.base-palette/invertDesign :added "4.0"}
(fact "inverts the design type"
  ^:hidden
  
  (base-palette/invertDesign {:type "light"})
  => {"invert" true, "type" "light"})

^{:refer melbourne.base-palette/designPalette :added "4.0"}
(fact "gets the palette given design"
  ^:hidden
  
  (base-palette/designPalette {:type "light"})
  => {"mainWarn" "#ffffe0",
      "mainBackground" "#eee",
      "mainError" "#9c1f1f",
      "mainPrimary" "#378E75",
      "isDark" false,
      "mainNeutral" "#333"})

^{:refer melbourne.base-palette/getPalette :added "4.0"}
(fact "gets either the palette if exists or generates from design"
  ^:hidden
  
  (base-palette/getPalette {:type "light"})
  => map?)
