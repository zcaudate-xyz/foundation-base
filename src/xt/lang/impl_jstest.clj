(ns xt.lang.impl-jstest
  (:require [std.lang :as l  :refer [defspec.xt]]))

(l/script- :js
  {:require [[xt.lang.common-lib :as lib]]})

(defn.js hello
  [a b]
  (xt/x:m-sin)
  (return
   (lib/T)))
