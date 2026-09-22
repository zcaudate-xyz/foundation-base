(ns melbourne.tama-theme
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:export [MODULE]})

(defn.js themeName
  "maps a Slim design to a Tamagui theme name"
  [design]
  (var #{type color} (or design {}))
  (var mode (:? (== type "dark") "dark" "light"))
  (var accent (or color "accent"))
  (return
   (:? (or (== accent "accent")
           (== accent "blue")
           (== accent "red")
           (== accent "yellow")
           (== accent "green")
           (== accent "black")
           (== accent "white"))
       (+ mode "_" accent)
       mode)))

(def.js MODULE (!:module))
