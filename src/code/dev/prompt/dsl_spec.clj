(ns code.dev.prompt.dsl-spec
  (:require [std.lib :as h]
            [std.lang :as l]))

(s/layout
 "# **Javascript (JS) DSL Specification**

**Objective:** This document defines the syntax, conventions, and constraints for writing code using the Clojure-based Javascript DSL (JS DSL), specifically for generating React applications with the Tamagui UI library, based on the std.lang transpiler and the sznui project's patterns.")

(defn construct-item
  [form & [alts]]
  {:dsl (str (s/layout form))
   :js  (vec (concat [(l/emit-script form {:lang :js})]
                     alts))})

(defn construct-alts
  [form & [alts]]
  {:dsl (str (s/layout form))
   :js  (vec (concat [(l/emit-script form {:lang :js})]
                     alts))})

(defn construct-
  [form & [alts]]
  {:dsl (str (s/layout form))
   :js  (vec (concat [(l/emit-script form {:lang :js})]
                     alts))})



;;
;; Datastructures

(construct-item nil)

(construct-item 'undefined)

(construct-item true)

(construct-item false)
(construct-item "hello")
(construct-item 123)
(construct-item 45.6)
(construct-item #"^he.*llo$")
(construct-item '(:.. props))
(construct-item 'Array.from)
(construct-item '(. Array from))
(construct-item '(obj.item.doSomething 1 2))
(construct-item '(. obj item (doSomething 1 2)))



;;
(construct-item '(+ 1 2 3))

;;
(construct-item '(- 1 2 3))

(construct-item '(var x 1)
                ["var x = 1"
                 "const x = 1"])
(construct-item '(fn []
                   (return 1))
                ["() => 1"])
(construct-item '(fn [s]
                   (return (* s 1)))
                ["(s) => s * 1"])


(construct-item '(fn [(:= a 1)
                      (:= b 2)]
                   (return (* a b))))
(construct-item '(fn [#{[a b (:.. props)]}]
                   (return (* a b props.item.c))))


(construct-item '(fn:> 1)
                ["function (){\n  return 1;\n}"])

{:dsl "(fn [] (return 1))", :js ["function (){\n  return 1;\n}" []]}

