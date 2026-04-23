(ns std.lang.base.library-snapshot-prep-test
  (:require [std.lang.base.book :as b]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.emit-prep-lua-test :as prep]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.util :as ut])
  (:use code.test))

(def +book-x-empty+
  (b/book {:lang :x
           :meta    (b/book-meta {})
           :grammar (grammar/grammar :x
                      (grammar/to-reserved (grammar/build))
                      helper/+default+)}))

(def +book-redis-empty+
  (b/book {:lang :lua.redis
           :parent :lua
            :meta    (b/book-meta {})
           :grammar (grammar/grammar :redis
                       (grammar/to-reserved (grammar/build))
                       helper/+default+)}))

(def +x-module+
  (b/book-module
   {:id      'x.core
    :lang    :x
    :link    '{- x.core}}))

(def +x-fragment-add+
  (b/book-entry {:lang :x
                     :id 'add
                     :module 'x.core
                     :section :fragment
                     :form       '(defmacro add [x y] (list '+ x y))
                     :template   (fn [x y] (list '+ x y))
                     :standalone true
                     :namespace 'x.core}))

(def +x-fragment-sub+
  (b/book-entry {:lang :x
                     :id 'sub
                     :module 'x.core
                     :section :fragment
                     :template    (fn [x y] (list '- x y))
                     :standalone '(fn [x y] (return (- x y)))
                     :namespace 'x.core}))

(def +x-code-identity-fn+
  (b/book-entry {:lang :x
                 :id 'identity-fn
                 :module 'x.core
                 :section :code
                 :form '(defn identity-fn [x] (return x))
                 :form-input '(defn identity-fn [x] (return x))
                 :deps #{}
                 :namespace 'x.core
                 :declared false}))

(def +book-x+
  (-> +book-x-empty+
      (b/set-module +x-module+)
      second
      (b/set-entry +x-fragment-add+)
      second
      (b/set-entry +x-fragment-sub+)
      second
      (b/set-entry +x-code-identity-fn+)
      second))

(def +snap+
  (-> (snap/snapshot {})
      (snap/add-book (assoc prep/+book-min+ :parent :x))
      (snap/add-book +book-x+)
      (snap/add-book +book-redis-empty+)))
