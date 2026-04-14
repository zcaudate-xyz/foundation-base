(ns xt.lang.impl-jstest
  (:require [std.lang :as l  :refer [defspec.xt]]))

(l/script- :js {})

(defn.js hello
  [a b]
  (xt/x:m-sin)
  (return true))
