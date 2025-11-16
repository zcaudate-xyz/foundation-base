(ns js.lib.mustache
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [keyword]))

(l/script :js
  {:bundle   {:default [["mustache" :as Mustache]]}})


(def$.js renderTemplate Mustache.render)
