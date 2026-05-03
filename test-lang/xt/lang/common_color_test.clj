(ns xt.lang.common-color-test
  (:require [std.json :as json]
            [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-color :as color]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-color :as color]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-color :as color]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-color/named->rgb :added "4.0"}
(fact "named color to rgb"

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

^{:refer xt.lang.common-color/hex->n :added "4.0"}
(fact "hex to rgb val"

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

^{:refer xt.lang.common-color/n->hex :added "4.0"}
(fact "converts an rgb to hex"

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

^{:refer xt.lang.common-color/hex->rgb :added "4.0"}
(fact "converts a hex value to rgb array"

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

^{:refer xt.lang.common-color/rgb->hex :added "4.0"}
(fact "converts rgb to hex"

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

^{:refer xt.lang.common-color/rgb->hue :added "4.0"}
(fact "helper function for rgb->hsl")

^{:refer xt.lang.common-color/rgb->hsl :added "4.0"
  :setup [(def +out+
            (contains-in
             [[(approx 180) (approx 100) (approx 19.6078)]
              [0 0 (approx 0)]
              [0 0 100]
              [0 0 (approx 99.607)]
              [(approx 60) 100 (approx 25.098)]
              [(approx 120) 100 (approx 25.098)]]))]}
(fact "converts rgb to hsl"

  (!.js [(color/rgb->hsl [0 100 100] nil)
         (color/rgb->hsl [0 0 0] nil)
         (color/rgb->hsl [255 255 255] nil)
         (color/rgb->hsl [254 254 254] nil)
         (color/rgb->hsl [128 128 0] nil)
         (color/rgb->hsl [0 128 0] nil)])
  => +out+

  (!.lua [(color/rgb->hsl [0 100 100] nil)
          (color/rgb->hsl [0 0 0] nil)
          (color/rgb->hsl [255 255 255] nil)
          (color/rgb->hsl [254 254 254] nil)
          (color/rgb->hsl [128 128 0] nil)
          (color/rgb->hsl [0 128 0] nil)])
  => +out+

  (!.py [(color/rgb->hsl [0 100 100] nil)
         (color/rgb->hsl [0 0 0] nil)
         (color/rgb->hsl [255 255 255] nil)
         (color/rgb->hsl [254 254 254] nil)
         (color/rgb->hsl [128 128 0] nil)
         (color/rgb->hsl [0 128 0] nil)])
  => +out+)

^{:refer xt.lang.common-color/hue->v :added "4.0"}
(fact "converts a hue to a value"

  (color/hue->v 20 30 0)
  => 20)

^{:refer xt.lang.common-color/hsl->rgb :added "4.0"}
(fact "converts hsl to rgb"

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

^{:refer xt.lang.common-color/named->hsl :added "4.0"}
(fact "converts a named color to hsl"

  (!.js
    (color/named->hsl "firebrick"))
  => (contains [(approx 0) (approx 67.9245) (approx 41.5686)])

  (!.lua
    (color/named->hsl "firebrick"))
  => (contains [(approx 0) (approx 67.9245) (approx 41.5686)])

  (!.py
    (color/named->hsl "firebrick"))
  => (contains [(approx 0) (approx 67.9245) (approx 41.5686)]))

^{:refer xt.lang.common-color/named->hex :added "4.0"}
(fact "converts a named color to hex"

  (!.js
    (color/named->hex "firebrick"))
  => "#B22222"

  (!.lua
    (color/named->hex "firebrick"))
  => "#B22222"

  (!.py
    (color/named->hex "firebrick"))
  => "#B22222")

^{:refer xt.lang.common-color/hex->hsl :added "4.0"}
(fact "converts a hex to hsl"

  (!.js
    (color/hex->hsl "#B22222"))
  => (contains [(approx 0) (approx 67.9245) (approx 41.5686)])

  (!.lua
    (color/hex->hsl "#B22222"))
  => (contains [(approx 0) (approx 67.9245) (approx 41.5686)])

  (!.py
    (color/hex->hsl "#B22222"))
  => (contains [(approx 0) (approx 67.9245) (approx 41.5686)]))

(comment
  (s/seedgen-benchadd '[xt.lang.common-color] {:lang [:dart :julia :ruby] :write true})
  (s/seedgen-langadd '[xt.lang.common-color] {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-color {:lang [:lua :python] :write true}))
