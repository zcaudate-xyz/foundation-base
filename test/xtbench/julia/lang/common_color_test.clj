(ns xtbench.julia.lang.common-color-test
  (:require [std.json :as json]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :julia
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-color :as color]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-color/named->rgb :added "4.0"}
(fact "named color to rgb"

  (!.julia
    [(color/named->rgb "aqua")
     (color/named->rgb "magenta")
     (color/named->rgb "WRONG")])
  => [[0 255 255] [255 0 255] [0 0 0]])

^{:refer xt.lang.common-color/hex->n :added "4.0"}
(fact "hex to rgb val"

  (!.julia
    [(color/hex->n "0")
     (color/hex->n "1")
     (color/hex->n "2")
     (color/hex->n "a")
     (color/hex->n "X")
     (color/hex->n "e")])
  => [0 1 2 10 0 14])

^{:refer xt.lang.common-color/n->hex :added "4.0"}
(fact "converts an rgb to hex"

  (!.julia
    [(color/n->hex 13)
     (color/n->hex 113)
     (color/n->hex 256)
     (color/n->hex -3)])
  => ["0D" "71" "00" "0D"])

^{:refer xt.lang.common-color/hex->rgb :added "4.0"}
(fact "converts a hex value to rgb array"

  (!.julia
    [(color/hex->rgb "#aaa")
     (color/hex->rgb "#45f981")
     (color/hex->rgb "#222222")])
  => [[170 170 170] [69 249 129] [34 34 34]])

^{:refer xt.lang.common-color/rgb->hex :added "4.0"}
(fact "converts rgb to hex"

  (!.julia
    [(color/rgb->hex (color/hex->rgb "#aaa"))
     (color/rgb->hex (color/hex->rgb "#45f981"))
     (color/rgb->hex (color/hex->rgb "#222222"))])
  => ["#AAAAAA" "#45F981" "#222222"])

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

  (!.julia [(color/rgb->hsl [0 100 100] nil)
         (color/rgb->hsl [0 0 0] nil)
         (color/rgb->hsl [255 255 255] nil)
         (color/rgb->hsl [254 254 254] nil)
         (color/rgb->hsl [128 128 0] nil)
         (color/rgb->hsl [0 128 0] nil)])
  => +out+)

^{:refer xt.lang.common-color/hsl->rgb :added "4.0"}
(fact "converts hsl to rgb"

  (!.julia [(color/hsl->rgb (color/rgb->hsl [0 100 100] nil))
         (color/hsl->rgb (color/rgb->hsl [0 0 0] nil))
         (color/hsl->rgb (color/rgb->hsl [255 255 255] nil))
         (color/hsl->rgb (color/rgb->hsl [128 128 0] nil))
         (color/hsl->rgb (color/rgb->hsl [0 128 0] nil))])
  => [[0 100 100] [0 0 0] [255 255 255] [128 128 0] [0 128 0]])

^{:refer xt.lang.common-color/named->hsl :added "4.0"}
(fact "converts a named color to hsl"

  (!.julia
    (color/named->hsl "firebrick"))
  => (contains [(approx 0) (approx 67.9245) (approx 41.5686)]))

^{:refer xt.lang.common-color/named->hex :added "4.0"}
(fact "converts a named color to hex"

  (!.julia
    (color/named->hex "firebrick"))
  => "#B22222")

^{:refer xt.lang.common-color/hex->hsl :added "4.0"}
(fact "converts a hex to hsl"

  (!.julia
    (color/hex->hsl "#B22222"))
  => (contains [(approx 0) (approx 67.9245) (approx 41.5686)]))

(comment
  (s/seedgen-benchadd '[xt.lang.common-color] {:lang [:dart :julia :ruby] :write true})
  (s/seedgen-langadd '[xt.lang.common-color] {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-color {:lang [:lua :python] :write true}))
