(ns tahto.core.library-snapshot-prep-test
  (:require [tahto.base.book :as b]
tahto/lang/library_snapshot_prep_test.clj:3:            [tahto.common.emit-common :as common]
tahto/lang/library_snapshot_prep_test.clj:4:            [tahto.common.emit-helper :as helper]
tahto/lang/library_snapshot_prep_test.clj:5:            [tahto.common.emit-prep-lua-test :as prep]
tahto/lang/library_snapshot_prep_test.clj:6:            [tahto.common.grammar :as grammar]
            [tahto.core.library-snapshot :as snap]
tahto/lang/library_snapshot_prep_test.clj:8:            [tahto.common.util :as ut])
  (:use code.test))

(def +book-x-empty+
  (b/book {:lang :x
           :meta    (b/book-meta {})
           :grammar (grammar/grammar :x
                      (grammar/to-reserved (grammar/build))
                      helper/+default+)}))

(def +book-lua-redis-empty+
  (b/book {:lang :lua.redis
           :parent :lua
           :meta    (b/book-meta {})
           :grammar (grammar/grammar :lua.redis
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
      (snap/add-book +book-lua-redis-empty+)))
