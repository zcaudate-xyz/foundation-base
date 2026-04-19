(ns walkthrough.std-lang-00-basic
  ;; Welcome to the basic std.lang tutorial.
  ;; We will go through how to write and link code together
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]))

;;
;; Lets setup the first runtime. This is a bit like an `ns` tag and it takes keywords that are familiar
;; like `:require` and `:import`.
;; 
;; `l/script-` is used to create the testing runtime context. `l/script` is used in the source code context.
;;

(l/script- :js
  {:require [[xt.lang.common-lib :as k]]})

;;
;; !.js creates a free context where expressions can be evaluated
;;

(fact "trying out !.js macro"
  
  (!.js
   (+ 1 2 3))
  => "1 + 2 + 3;"

  (!.js
   (fn [] (return (+ 1 2 3))))
  => "function (){\n  return 1 + 2 + 3;\n}"

  (!.js
   (k/obj-pick {:a 1 :b 2} ["a"]))
  => "k.obj_pick({\"a\":1,\"b\":2},[\"a\"]);")

;;
;; def.js saves a `:def` entry in the book
;;

(fact "trying out def.js macro"
  ^:hidden
  
  (def.js a (+ 1 2 3))
  => #'walkthrough.std-lang-00-basic/a

  (!.js -/a)
  => "a;"
  
  (type a)
  => std.lib.context.pointer.Pointer

  (into {} a)
  => {:context :lang/js
      :lang :js
      :id 'a
      :module 'walkthrough.std-lang-00-basic
      :section :code
      :context/fn #'std.lang.base.util/lang-rt-default}

  (type @a)
  => std.lang.base.book_entry.BookEntry
  
  (into {} @a)
  => (contains-in
      {:op-key :def
       :form-input '(def a (+ 1 2 3))
       :section :code
       :standalone nil
       :template nil
       :op 'def
       :module 'walkthrough.std-lang-00-basic
       :lang :js
       :id 'a
       :declared nil
       :display :default
       :form '(def a (+ 1 2 3))
       :namespace 'walkthrough.std-lang-00-basic
       :deps #{}}))

;;
;; defn.js saves a `:defn` entry in the book
;;

(fact "trying out defn.js macro"
  ^:hidden
  
  (defn.js hello
    [a b]
    (return (+ a b)))
  => #'walkthrough.std-lang-00-basic/hello

  (hello 1 2)
  => "hello(1,2)"
  
  ;;
  ;; ENTRY
  ;;
  
  (type hello)
  => std.lib.context.pointer.Pointer
  
  (into {} hello)
  => {:context :lang/js
      :lang :js
      :id 'hello
      :module 'walkthrough.std-lang-00-basic
      :section :code
      :context/fn #'std.lang.base.util/lang-rt-default}

  (into {} @hello)
  => (contains-in
      {:op-key :defn
       :form-input '(defn hello [a b] (return (+ a b)))
       :section :code
       :standalone nil
       :template nil
       :op 'defn
       :module 'walkthrough.std-lang-00-basic
       :lang :js
       :id 'hello
       :declared nil
       :display :default
       :form '(defn hello [a b] (return (+ a b)))
       :doc ""
       :namespace 'walkthrough.std-lang-00-basic
       :deps #{}}))


;;
;; Function composition 
;;
;; Only symbols containing namespaces are considered `-/` for this namespace
;;

(fact "linking pointers and dependencies"
  
  (defn.js world
    [c]
    (return (k/abs (+ (-/hello) -/a c))))
  => #'walkthrough.std-lang-00-basic/world

  (world -10)
  => "world(-10)"
  
  ;;
  ;; ENTRY
  ;;
  
  (into {} @world)
  => (contains-in
      {:op-key :defn
       :form-input '(defn world [c] (return (k/abs (+ (-/hello) -/a c))))
       :section :code
       :standalone nil
       :template nil
       :op 'defn
       :module 'walkthrough.std-lang-00-basic
       :lang :js
       :id 'world
       :declared nil
       :display :default
       :form '(defn world
                [c]
                (return
                 (x:m-abs
                  (+
                  (walkthrough.std-lang-00-basic/hello)
                  walkthrough.std-lang-00-basic/a
                  c))))
       :doc ""
       :namespace 'walkthrough.std-lang-00-basic
       :deps
       '#{walkthrough.std-lang-00-basic/hello
          walkthrough.std-lang-00-basic/a}}))

;;
;; def$.js saves a `:fragment` entry in the book
;;

(fact "def$.js will create a replacable fragment"
  
  (def$.js hello-fragment (+ 1 2 3))

  (!.js
   -/hello-fragment)
  => "1 + 2 + 3;"

  (into {} @hello-fragment)
  => (contains-in
      {:form-input nil,
       :section :fragment,
       :standalone nil,
       :template nil,
       :op 'def$,
       :module 'walkthrough.std-lang-00-basic,
       :lang :js,
       :priority nil,
       :id 'hello-fragment,
       :static/return nil,
       :declared nil,
       :display :default,
       :form '(+ 1 2 3),
       :namespace 'walkthrough.std-lang-00-basic,
       :deps nil}))


;;
;; defmacro.js saves a `:fragment` entry in the book
;;

(fact "defmacro.js will create a macro (evaled on function creation)"
  
  (defmacro.js double-add
    [a b] (list '+ a a b b))
  
  (!.js
   (-/double-add 1 2))
  => "1 + 1 + 2 + 2;"

  (into {} @double-add)
  => (contains-in
      {:form-input nil,
       :section :fragment,
       :standalone nil,
       :template fn?,
       :op 'defmacro,
       :module 'walkthrough.std-lang-00-basic,
       :lang :js,
       :priority nil,
       :id 'double-add,
       :static/return nil,
       :declared nil,
       :display :default,
       :form '(fn [a b] (list '+ a a b b)),
       :namespace 'walkthrough.std-lang-00-basic,
       :deps nil}))
