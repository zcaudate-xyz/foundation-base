(ns js.lib.mustache
  (:require [lang.core :as l])
  (:refer-clojure :exclude [keyword]))

(l/script :js
  {:import [["mustache" :as Mustache]]})


(def$.js renderTemplate Mustache.render)
