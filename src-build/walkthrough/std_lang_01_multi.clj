(ns walkthrough.std-lang-01-multi
  ;; Will will create a multi lang environment
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

;;
;; Creating a JS runtime:
;;

(l/script- :js
  {:require [[xt.lang.common-lib :as k]]})

(fact "js runtime"
  ^:hidden
  
  (!.js
   (+ 1 2 3))
  => "1 + 2 + 3;"

  (defn.js hello
    []
    (return
     (+ 1 2 3)))

  (l/emit-ptr hello)
  => "function hello(){\n  return 1 + 2 + 3;\n}"

  (str hello)
  => (std.string/|
      "![:lang/js :default walkthrough.std-lang-01-multi/hello :code]"
      "function hello(){"
      "  return 1 + 2 + 3;"
      "}"))

;;
;; Creating a LUA runtime:
;;

(l/script- :lua
  {:require [[xt.lang.common-lib :as k]]})

(fact "lua runtime"
  ^:hidden
  
  (!.lua
   (+ 1 2 3))
  => "1 + 2 + 3"

  (defn.lua world
    []
    (return
     (+ 1 2 3)))

  (l/emit-ptr world)
  => "local function world()\n  return 1 + 2 + 3\nend"

  (str world)
  => (std.string/|
      "![:lang/lua :default walkthrough.std-lang-01-multi/world :code]"
      "local function world()"
      "  return 1 + 2 + 3"
      "end"))


;;
;; Creating a python runtime:
;;

(l/script- :python
  {:require [[xt.lang.common-lib :as k]]})


(fact "python runtime"
  ^:hidden
  
  (!.py
   (+ 1 2 3))
  => "1 + 2 + 3"

  (defn.py again
    []
    (return
     (+ 1 2 3)))
  
  (l/emit-ptr again)
  => "def again():\n  return 1 + 2 + 3"

  (str again)
  => (std.string/|
      "![:lang/python :default walkthrough.std-lang-01-multi/again :code]"
      "def again():"
      "  return 1 + 2 + 3"))


;;
;; Creating a R runtime:
;;

(l/script- :r
  {:require [[xt.lang.common-lib :as k]]})

(fact "r runtime"
  ^:hidden
  
  (!.R
   (+ 1 2 3))
  => "1 + 2 + 3;"

  (def.R stuff
    (fn []
      (return
       (+ 1 2 3))))
  
  (l/emit-ptr stuff)
  => "stuff <- function (){\n  return(1 + 2 + 3);\n};"

  (str stuff)
  => (std.string/|
      "![:lang/r :default walkthrough.std-lang-01-multi/stuff :code]"
      "stuff <- function (){"
      "  return(1 + 2 + 3);"
      "};"))


;;
;; l/script+ creates runtimes in the annex context if more than one runtimes of the same language
;; are needed in the name namespace
;;

(l/script+ [:env1 :python]
  {:require [[xt.lang.common-lib :as k]]})


(l/! [:env1]
  (fn [] (return (+ 1 2 3))))
=> "(lambda : 1 + 2 + 3)"


(l/script+ [:env2 :js]
  {:require [[xt.lang.common-lib :as k]]})

(l/! [:env2]
  (fn [] (return (+ 1 2 3))))
=> "function (){\n  return 1 + 2 + 3;\n}"
