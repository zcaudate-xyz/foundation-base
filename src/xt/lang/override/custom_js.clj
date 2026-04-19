(ns xt.lang.override.custom-js
  (:require [std.lang :as l]))

(l/script :js xt.lang)

(defmacro.js ^{:static/override true}
  pad-left
  "override for pad left"
  {:added "4.0"}
  [s n ch]
  (list '. s (list 'padStart n ch)))

(defmacro.js ^{:static/override true}
  pad-right
  "override for pad right"
  {:added "4.0"}
  [s n ch]
  (list '. s (list 'padEnd n ch)))

(defmacro.js ^{:static/override true}
  arr-each
  "override for arr each"
  {:added "4.0"}
  [arr f]
  (list '. arr (list 'forEach f)))

(defmacro.js ^{:static/override true}
  arr-every
  "override for every"
  {:added "4.0"}
  [arr pred]
  (list '. arr (list 'every pred)))

(defmacro.js ^{:static/override true}
  arr-some
  "override for some"
  {:added "4.0"}
  [arr pred]
  (list '. arr (list 'some pred)))

(defmacro.js ^{:static/override true}
  arr-foldl
  "override for foldl"
  {:added "4.0"}
  [arr f init]
  (list '. arr (list 'reduce f init)))

(defmacro.js ^{:static/override true}
  arr-foldr
  "override for foldr"
  {:added "4.0"}
  [arr f init]
  (list '. arr (list 'reduceRight f init)))


(comment
  
  (arr-each [1 2 3 4] 'a))
