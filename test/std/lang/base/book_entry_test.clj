(ns std.lang.base.book-entry-test
  (:use code.test)
  (:require [std.lang.base.book-entry :refer :all]))

^{:refer std.lang.base.book-entry/book-entry? :added "4.0"}
(fact "checks if object is a book entry"
  ^:hidden
  (book-entry? (book-entry {})) => true)

^{:refer std.lang.base.book-entry/book-entry :added "4.0"}
(fact "creates a book entry"
  ^:hidden

  ;;
  ;; defn.<> specification
  ;;
  (book-entry {:lang :lua
               :id 'identity-fn
               :module 'L.core
               :section :code
               :form '(defn identity-fn [x] x)
               :form-input '(defn identity-fn [x] x)
               :deps #{}
               :namespace 'L.core
               :declared false})
  => book-entry?

  ;;
  ;; defmacro.<> specification
  ;;
  (book-entry {:lang :lua
               :id 'identity
               :module 'L.core
               :section :fragment
               :form '(defn identity [x] x)
               :form-input '(defn identity [x] x)
               :template (fn [x] x)
               :standalone '(fn [x] (return x))
               :deps #{}
               :namespace 'L.core
               :declared false})
  => book-entry?)
