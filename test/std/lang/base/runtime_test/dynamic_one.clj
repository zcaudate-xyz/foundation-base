(ns std.lang.base.runtime-test.dynamic-one
  (:use code.test)
  (:require [std.lang :as l]))

(l/script :js
  {:dynamic true
   :require [[xt.lang.base-lib :as k]]})

(defn.js ^{:static/template :T.string}
  hello-id []
  (return (. k (+ 1 2))))
