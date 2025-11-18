(ns code.dev.prompt.translate-figma-make
  (:require [std.lib :as h]
            [std.lang :as l]))

;; Figma Make

(l/emit-script
 '($ hello world again) {:lang :js})

(l/emit-script
 '(% [1 2 3 4]) {:lang :js})

(l/emit-script
 '(<  x.y.z xyz) {:lang :js})

(l/emit-script
 '(<= (x.y.z) xyz) {:lang :js})

(l/emit-script
 '(> (x.arr.reduce
      init-fn
      arr) xyz) {:lang :js})

(l/emit-script
 '(>= (x.arr.reduce
       init-fn
       arr) xyz) {:lang :js})

(l/emit-script 'undefined {:lang :js})


;; Comparison Operators
(l/emit-script '(not a) {:lang :js})
(l/emit-script '(<= a b) {:lang :js})
(l/emit-script '(== a b) {:lang :js})
(l/emit-script '(=== a b) {:lang :js})
(l/emit-script '(not= a b) {:lang :js})
(l/emit-script '(!== a b) {:lang :js})
(l/emit-script '(> a b) {:lang :js})
(l/emit-script '(>= a b) {:lang :js})

;; Assignment/Arithmetic Operators
(l/emit-script '(:= a 10) {:lang :js})
(l/emit-script '(:++ a) {:lang :js})
(l/emit-script '(:-- a ) {:lang :js})
(l/emit-script '(:+= a 2) {:lang :js})
(l/emit-script '(:-= a 2) {:lang :js})
(l/emit-script '(:*= a 2) {:lang :js})

(l/emit-script '(== Nan x) {:lang :js})



(l/emit-script '(+ a b) {:lang :js})
(l/emit-script '(- a b) {:lang :js})
(l/emit-script '(* a b) {:lang :js})
(l/emit-script '(/ a b) {:lang :js})

(l/emit-script '(:? condition true-val false-val) {:lang :js})

(l/emit-script '(:= a (- a b)) {:lang :js})

(l/emit-script '#{(:.. obj)} {:lang :js}) ;; Spread operator for objects
(l/emit-script '#{[a b(:.. obj)]} {:lang :js}) ;; Spread operator for objects
(l/emit-script '[(:.. arr)] {:lang :js}) ;; Spread operator for arrays


;; Special Keywords/Literals
(l/emit-script 'NaN {:lang :js})
(l/emit-script '(. obj prop) {:lang :js})
(l/emit-script '(. obj [key]) {:lang :js})

;; Control Flow/Functions/Other
(l/emit-script '(await (some-async-fn)) {:lang :js})
(l/emit-script '(b:& a b) {:lang :js})
(l/emit-script '(b:<< a b) {:lang :js})
(l/emit-script '(b:>> a b) {:lang :js})
(l/emit-script '(b:xor a b) {:lang :js})
(l/emit-script '(b:| a b) {:lang :js})
(l/emit-script '(break) {:lang :js}) ;; Used within loops
(l/emit-script '(cond (== x 1) "one" :else "other") {:lang :js})
(l/emit-script '(del (. obj prop)) {:lang :js})
(l/emit-script '(do (console.log "hello") (console.log "world")) {:lang :js})
(l/emit-script '(fn [x] (return (+ x 1))) {:lang :js})
(l/emit-script '(for [(:= i 0) (< i 3) (:++ i)] (console.log i)) {:lang :js})
(l/emit-script '(if condition (console.log "true") (console.log "false")) {:lang :js})
(l/emit-script '(when condition
                  (console.log "true")
                  (console.log "more")) {:lang :js})
(l/emit-script '(instanceof obj Type) {:lang :js})
(l/emit-script '(var x 1) {:lang :js}) ;; 'let' is replaced by 'var' or 'const'
(l/emit-script '(mod a b) {:lang :js})
(l/emit-script '(case val
                  "a" (return a)
                  "b" (do (:= x 1)
                          (break))) {:lang :js})
(l/emit-script '(new Constructor args) {:lang :js})
(l/emit-script '(not condition) {:lang :js})
(l/emit-script '(not= a b) {:lang :js})
(l/emit-script '(or a b) {:lang :js})
(l/emit-script '(pow base exp) {:lang :js})
(l/emit-script '(return value) {:lang :js})
(l/emit-script '(super.method) {:lang :js}) ;; Used in class inheritance
(l/emit-script '(. this prop) {:lang :js}) ;; Used in object/class context
(l/emit-script '(throw (new Error "message")) {:lang :js})
(l/emit-script '(try (do-something) (catch e (console.log e))) {:lang :js})

(l/emit-script '(when condition (console.log "action")) {:lang :js})
(l/emit-script '(while condition (console.log "loop")) {:lang :js})
(l/emit-script '(b:xor a b) {:lang :js}) ;; Bitwise XOR
(l/emit-script '(yield value) {:lang :js}) ;; Used in generator functions
```

(l/emit-script '(typeof v) {:lang :js})
(l/emit-script '(instanceof  x cls) {:lang :js})
(l/emit-script '... {:lang :js})
(l/emit-script '(. hello [1]) {:lang :js})
(l/emit-script '(. hello [1] world (again)) {:lang :js})
(l/emit-script '(. this world (again)) {:lang :js})
(l/emit-script '(Array.from (. node children (values))) {:lang :js})
(l/emit-script '(async (fn [#{[a b]}] (return a))) {:lang :js})
(l/emit-script '(await (fn [#{[a b]}] (return a))) {:lang :js})




and
 await
 b:&
 b:<<
 b:>>
 b:xor
 b:|
 br*
 break
 case
 
 cond
 
 del
 do
 fn


 for  
 if
 instanceof
 let
 mod
 new
 not
 not=
 or
 pow
 return
 super
 switch
 tab
 this
 throw
 try
 typeof
 undefined

 when
 while
 xor
 yield
 


(sort-by str (keys (:reserved (l/grammar :js))))


(defmacro defrule
  [& args])




(defrule :covert
  ;; When refering to a previously defined <element> in the same namespace,
  ;; explicity link to it with -/<element>, using '-' for the current namespace.
  {:from          ["let x = 1"
                   "var x = 1"
                   "const x = 1"]
   :to            (var x 1)})

(defrule :alts
  ;; When refering to a previously defined <element> in the same namespace,
  ;; explicity link to it with -/<element>, using '-' for the current namespace.
  {:from           
   :correct        (defn.js HelloFn [] (return -/Hello))})

(defrule :correction
  ;; When refering to a previously defined <element> in the same namespace,
  ;; explicity link to it with -/<element>, using '-' for the current namespace.
  {:incorrect      (defn.js HelloFn [] (return Hello))
   :correct        (defn.js HelloFn [] (return -/Hello))})
