(ns js.lib.mustache
  (:require [std.lang :as l])
  (:refer-clojure :exclude [keyword]))

(l/script :js
  {:import [["mustache" :as Mustache]]})


(def$.js renderTemplate Mustache.render)
