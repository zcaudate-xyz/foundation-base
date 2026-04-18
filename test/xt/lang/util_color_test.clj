(ns xt.lang.util-color-test
  (:require [std.json :as json]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.util-color :as color]
             [js.core :as j]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.util-color :as color]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.util-color :as color]]})

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.util-color :as color]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-color/named->rgb :added "4.0"}
(fact "named color to rgb"
  ^:hidden
  
  (!.js
   [(color/named->rgb "aqua")
    (color/named->rgb "magenta")
    (color/named->rgb "WRONG")])
  => [[0 255 255] [255 0 255] [0 0 0]]
  
  (!.lua
   [(color/named->rgb "aqua")
    (color/named->rgb "magenta")
    (color/named->rgb "WRONG")])
  => [[0 255 255] [255 0 255] [0 0 0]]

  (!.py
   [(color/named->rgb "aqua")
    (color/named->rgb "magenta")
    (color/named->rgb "WRONG")])
  => [[0 255 255] [255 0 255] [0 0 0]])

^{:refer xt.lang.util-color/hex->n :added "4.0"}
(fact "hex to rgb val"
  ^:hidden
  
  (!.js
   [(color/hex->n "0")
    (color/hex->n "1")
    (color/hex->n "2")
    (color/hex->n "a")
    (color/hex->n "X")
    (color/hex->n "e")])
  => [0 1 2 10 0 14]
  
  (!.lua
   [(color/hex->n "0")
    (color/hex->n "1")
    (color/hex->n "2")
    (color/hex->n "a")
    (color/hex->n "X")
    (color/hex->n "e")])
  => [0 1 2 10 0 14]

  (!.py
   [(color/hex->n "0")
    (color/hex->n "1")
    (color/hex->n "2")
    (color/hex->n "a")
    (color/hex->n "X")
    (color/hex->n "e")])
  => [0 1 2 10 0 14])

^{:refer xt.lang.util-color/n->hex :added "4.0"}
(fact "converts an rgb to hex"
  ^:hidden
  
  (!.js
   [(color/n->hex 13)
    (color/n->hex 113)
    (color/n->hex 256)
    (color/n->hex -3)])
  => ["0D" "71" "00" "0D"]

  (!.lua
   [(color/n->hex 13)
    (color/n->hex 113)
    (color/n->hex 256)
    (color/n->hex -3)])
  => ["0D" "71" "00" "0D"]

  (!.py
   [(color/n->hex 13)
    (color/n->hex 113)
    (color/n->hex 256)
    (color/n->hex -3)])
  => ["0D" "71" "00" "0D"])

^{:refer xt.lang.util-color/hex->rgb :added "4.0"}
(fact "converts a hex value to rgb array"
  ^:hidden
  
  (!.js
   [(color/hex->rgb "#aaa")
    (color/hex->rgb "#45f981")
    (color/hex->rgb "#222222")])
  => [[170 170 170] [69 249 129] [34 34 34]]
  

  (!.lua
   [(color/hex->rgb "#aaa")
    (color/hex->rgb "#45f981")
    (color/hex->rgb "#222222")])
  => [[170 170 170] [69 249 129] [34 34 34]]

  (!.py
   [(color/hex->rgb "#aaa")
    (color/hex->rgb "#45f981")
    (color/hex->rgb "#222222")])
  => [[170 170 170] [69 249 129] [34 34 34]])

^{:refer xt.lang.util-color/rgb->hex :added "4.0"}
(fact "converts rgb to hex"
  ^:hidden
  
  (!.js
   [(color/rgb->hex (color/hex->rgb "#aaa"))
    (color/rgb->hex (color/hex->rgb "#45f981"))
    (color/rgb->hex (color/hex->rgb "#222222"))])
  => ["#AAAAAA" "#45F981" "#222222"]

  (!.lua
   [(color/rgb->hex (color/hex->rgb "#aaa"))
    (color/rgb->hex (color/hex->rgb "#45f981"))
    (color/rgb->hex (color/hex->rgb "#222222"))])
  => ["#AAAAAA" "#45F981" "#222222"]

  (!.py
   [(color/rgb->hex (color/hex->rgb "#aaa"))
    (color/rgb->hex (color/hex->rgb "#45f981"))
    (color/rgb->hex (color/hex->rgb "#222222"))])
  => ["#AAAAAA" "#45F981" "#222222"])

^{:refer xt.lang.util-color/rgb->hue :added "4.0"}
(fact "helper function for rgb->hsl")

^{:refer xt.lang.util-color/rgb->hsl :added "4.0"}
(fact "converts rgb to hsl"
  ^:hidden
  
  (!.js [(color/rgb->hsl [0 100 100])
         (color/rgb->hsl [0 0 0])
         (color/rgb->hsl [255 255 255])
         (color/rgb->hsl [254 254 254])
         (color/rgb->hsl [128 128 0])
         (color/rgb->hsl [0 128 0])])
  => [[180 99.99999999999999 19.607843137254903]
      [0 0 0]
      [0 0 100]
      [0 0 99.6078431372549]
      [60 100 25.098039215686274]
      [120 100 25.098039215686274]]
  
  
  (!.lua [(color/rgb->hsl [0 100 100])
          (color/rgb->hsl [0 0 0])
          (color/rgb->hsl [255 255 255])
          (color/rgb->hsl [128 128 0])
          (color/rgb->hsl [0 128 0])])
  => [[180 100 19.607843137255]
      [0 0 0]
      [0 0 100]
      [60 100 25.098039215686]
      [120 100 25.098039215686]]
  

  (!.py [(color/rgb->hsl [0 100 100] nil)
         (color/rgb->hsl [0 0 0] nil)
         (color/rgb->hsl [255 255 255] nil)
         (color/rgb->hsl [128 128 0] nil)
         (color/rgb->hsl [0 128 0] nil)])
  => [[180.0 99.99999999999999 19.607843137254903]
      [0 0 0.0]
      [0 0 100]
      [60.0 100 25.098039215686274]
      [120.0 100 25.098039215686274]])

^{:refer xt.lang.util-color/hue->v :added "4.0"}
(fact "converts a hue to a value"
  ^:hidden
  
  (color/hue->v 20 30 0)
  => 20)

^{:refer xt.lang.util-color/hsl->rgb :added "4.0"}
(fact "converts hsl to rgb"
  ^:hidden
  
  (!.js [(color/hsl->rgb (color/rgb->hsl [0 100 100] nil))
         (color/hsl->rgb (color/rgb->hsl [0 0 0] nil))
         (color/hsl->rgb (color/rgb->hsl [255 255 255] nil))
         (color/hsl->rgb (color/rgb->hsl [128 128 0] nil))
         (color/hsl->rgb (color/rgb->hsl [0 128 0] nil))])
  => [[0 100 100] [0 0 0] [255 255 255] [128 128 0] [0 128 0]]
  
  (!.lua [(color/hsl->rgb (color/rgb->hsl [0 100 100] nil))
         (color/hsl->rgb (color/rgb->hsl [0 0 0] nil))
         (color/hsl->rgb (color/rgb->hsl [255 255 255] nil))
         (color/hsl->rgb (color/rgb->hsl [128 128 0] nil))
         (color/hsl->rgb (color/rgb->hsl [0 128 0] nil))])
  => [[0 100 100] [0 0 0] [255 255 255] [128 128 0] [0 128 0]]

  (!.py [(color/hsl->rgb (color/rgb->hsl [0 100 100] nil))
         (color/hsl->rgb (color/rgb->hsl [0 0 0] nil))
         (color/hsl->rgb (color/rgb->hsl [255 255 255] nil))
         (color/hsl->rgb (color/rgb->hsl [128 128 0] nil))
         (color/hsl->rgb (color/rgb->hsl [0 128 0] nil))])
  => [[0 100 100] [0 0 0] [255 255 255] [128 128 0] [0 128 0]])

^{:refer xt.lang.util-color/named->hsl :added "4.0"}
(fact "converts a named color to hsl"
  ^:hidden
  
  (!.js
   (color/named->hsl "firebrick"))
  => [0 67.9245283018868 41.568627450980394])

^{:refer xt.lang.util-color/named->hex :added "4.0"}
(fact "converts a named color to hex"
  ^:hidden
  
  (!.js
   (color/named->hex "firebrick"))
  => "#B22222")

^{:refer xt.lang.util-color/hex->hsl :added "4.0"}
(fact "converts a hex to hsl"
  ^:hidden

  (!.js
   (color/hex->hsl "#B22222"))
  => [0 67.9245283018868 41.568627450980394])
