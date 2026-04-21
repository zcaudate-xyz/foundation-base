(ns xt.sample.train-002-test
  (:use code.test)
  (:require [std.lang :as l]))


^{:seedgen/scaffold         {:python true}}
(l/script+ [:db :postgres])   ;; this is a scaffold. any non (!.<lang> ...) and (notify/wait-on <lang>) is {:all true} by default. 

^{:seedgen/root         {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]]})

(fact:global ;; this should be a scaffold form
 {:setup [(l/rt:restart)    
          (!.js (+ 3 4 5))  ;; this foundational
          (!.lua (+ 1 2 3)) ;; this is derived and can be removed
          ]
  :teardown [(l/rt:stop)]})


(def +a+ (inc 1))      ;; this should be a scaffold form 

(l/! :db (+ 1 2 3))    ;; this is also a scaffold form

^{:refer xt.lang.common-spec/for:array :added "4.1"
  :setup    [(def +a+ (+ 1 2 3))
             (!.js (+ 1 2 3))
             ^{:seedgen/derived   {:lua true}} ;; this is derived the meta is optional
             (!.lua (+ 1 2 3))]
  :teardown [(!.js (+ 1 2 3))]}
(fact "iterates arrays in order"
  
  (!.js               ;; this is foundation
    (var out [])
    (xt/for:array [e [1 2 3 4]]
      (when (> e 3)
        (break))
      (xt/x:arr-push out e))
    out)
  => [1 2 3]


  (!.lua              ;; this is derived and can be removed
    (var out [])
    (xt/for:array [e [1 2 3 4]]
      (when (> e 3)
        (break))
      (xt/x:arr-push out e))
    out)
  => [1 2 3])


;;
;; seedgen-readforms should be able to parse and classify this file to be able to add more information to the existing code.framework/analyse datastructure 
;;
;; - split out the fact form innto :seedgen/root and :seedgen/derived testcases
;; - fact:global level setup and teardown need special treatment to identity seedgen/root and seedgen/scaffold
;; - fact level setup and teardown need special treatment to identity seedgen/root and seedgen/scaffold
;; - toplevel forms need special treatment to identity seedgen/root and seedgen/scaffold
;;
;;
;;
;;
;;
;;
;;
;; any that in not  without l/script-  ^{:seedgen/scaffold  {:all true}} by default unless  
