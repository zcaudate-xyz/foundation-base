(ns indigo.server.api-translate
  (:require [std.block.heal.core :as heal]
            [std.html :as html]
            [std.lib :as h]))

(defn to-heal [source]
  (heal/heal-content source))

(defn from-html [source]
  (pr-str (html/tree source)))

(defn to-html [dsl-str]
  (let [form (read-string dsl-str)]
    (html/html form)))
