(ns walkthrough.std-lang-02-live
  ;; Will will create a multi lang environment
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

;;
;; Creating a JS runtime:
;;

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]]})

(fact "js runtime"
  ^:hidden
  
  (!.js
   (+ 1 2 3))
  => 6
  
  (defn.js hello
    []
    (return
     (+ 1 2 3)))

  (hello)
  => 6
  
  (!.js (+ (-/hello)
           (-/hello)))
  => 12

  ;;
  ;; print out what was sent to the repl
  ;;
  
  (std.concurrent.print/with-out-str
    (l/with:print-all
      (!.js (+ (-/hello)
               (-/hello)))))
  => string?
  
  ;;
  ;; `^*` is shortcut for print-all
  ;;
  
  (std.concurrent.print/with-out-str
    ^*(!.js (+ (-/hello)
               (-/hello))))
  => string?)

;;
;; Creating a LUA runtime:
;;

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]]})

(fact "lua runtime"
  ^:hidden
  
  (!.lua
   (+ 1 2 3))
  => 6

  (defn.lua world
    []
    (return
     (+ 1 2 3)))
  
  (world)
  => 6

  (!.lua (+ (-/world)
            (-/world)))
  => 12
  
  (std.concurrent.print/with-out-str
    (l/with:print-all
      (!.lua (+ (-/world)
                (-/world)))))
  => string?)


;;
;; Creating a python runtime:
;;

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]]})

(fact "python runtime"
  ^:hidden
  
  (!.py
   (+ 1 2 3))
  => 6

  (defn.py again
    []
    (return
     (+ 1 2 3)))
  
  (again)
  => 6

  (!.py (+ (-/again)
           (-/again)))
  => 12

  (std.concurrent.print/with-out-str
    (l/with:print-all
      (!.py (+ (-/again)
               (-/again)))))
  => string?)

;;
;; Creating a R runtime:
;;

(l/script- :r
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]]})

(fact "r runtime"
  ^:hidden
  
  (!.R
   (+ 1 2 3))
  => 6

  (def.R stuff
    (fn []
      (return
       (+ 1 2 3))))

  (stuff)
  => 6

  (!.R (+ (-/stuff)
          (-/stuff)))
  => 12

  (std.concurrent.print/with-out-str
    (l/with:print-all
      (!.R (+ (-/stuff)
              (-/stuff)))))
  => string?)

(fact "Altogether now"
  ^:hidden

  ;;
  ;; execution on all 4 runtimes, then combining result with clojure
  ;;
  
  (+ (-/hello)
     (-/world)
     (-/again)
     (-/stuff))
  => 24)


