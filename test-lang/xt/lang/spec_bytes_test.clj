(ns xt.lang.spec-bytes-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true :langs [:python :lua]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-base/x:bytes-new :added "4.1"}
(fact "creates, copies, slices, mutates, and converts unsigned bytes"

  (!.js
    (var source (xt/x:bytes-new [0 127 255]))
    (var copy (xt/x:bytes-copy source))
    (var mutated (xt/x:bytes-set copy 0 9))
    (var slice (xt/x:bytes-slice source 1 3))
    [(xt/x:bytes-count source) (xt/x:bytes-get source 2)
     (xt/x:bytes-get copy 0) (xt/x:bytes-get slice 0)
     (xt/x:bytes-u8 -1) (xt/x:bytes-s8 255) (== mutated copy)])
  => [3 255 9 127 255 -1 true]

  (!.py
    (var source (xt/x:bytes-new [0 127 255]))
    (var copy (xt/x:bytes-copy source))
    (var mutated (xt/x:bytes-set copy 0 9))
    (var slice (xt/x:bytes-slice source 1 3))
    [(xt/x:bytes-count source) (xt/x:bytes-get source 2)
     (xt/x:bytes-get copy 0) (xt/x:bytes-get slice 0)
     (xt/x:bytes-u8 -1) (xt/x:bytes-s8 255) (== mutated copy)])
  => [3 255 9 127 255 -1 true]

  (!.lua
    (var source (xt/x:bytes-new [0 127 255]))
    (var copy (xt/x:bytes-copy source))
    (var mutated (xt/x:bytes-set copy 0 9))
    (var slice (xt/x:bytes-slice source 1 3))
    [(xt/x:bytes-count source) (xt/x:bytes-get source 2)
     (xt/x:bytes-get copy 0) (xt/x:bytes-get slice 0)
     (xt/x:bytes-u8 -1) (xt/x:bytes-s8 255) (== mutated copy)])
  => [3 255 9 127 255 -1 true])

^{:refer xt.lang.spec-base/x:str-encode :added "4.1"}
(fact "round trips UTF-8 through the bytes category"

  (!.js
    (var encoded (xt/x:str-encode "hé"))
    [(xt/x:str-decode encoded) (xt/x:bytes-count encoded)
     (xt/x:bytes-get encoded 1)])
  => ["hé" 3 195]

  (!.py
    (var encoded (xt/x:str-encode "hé"))
    [(xt/x:str-decode encoded) (xt/x:bytes-count encoded)
     (xt/x:bytes-get encoded 1)])
  => ["hé" 3 195]

  (!.lua
    (var encoded (xt/x:str-encode "hé"))
    [(xt/x:str-decode encoded) (xt/x:bytes-count encoded)
     (xt/x:bytes-get encoded 1)])
  => ["hé" 3 195])

^{:refer xt.lang.spec-base/x:bit-not :added "4.1"}
(fact "normalizes bit complement to signed 32-bit values"

  (!.js [(xt/x:bit-not 0) (xt/x:bit-not 2147483647)])
  => [-1 -2147483648]

  (!.py [(xt/x:bit-not 0) (xt/x:bit-not 2147483647)])
  => [-1 -2147483648]

  (!.lua [(xt/x:bit-not 0) (xt/x:bit-not 2147483647)])
  => [-1 -2147483648])
