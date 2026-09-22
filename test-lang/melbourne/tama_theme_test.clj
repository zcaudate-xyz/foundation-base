(ns melbourne.tama-theme-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]
            [melbourne.tama-theme]))

^{:refer melbourne.tama-theme/themeName :added "4.1"}
(fact "maps Slim design modes and accents to Tamagui themes"
  (let [entry (l/sym-entry :js 'melbourne.tama-theme/themeName)
        form  (pr-str (:form entry))]
    (clojure.string/includes? form "(:?") => true
    (clojure.string/includes? form "(+ mode") => true
    (clojure.string/includes? form "mode") => true
    (clojure.string/includes? form "accent") => true))
