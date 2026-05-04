(ns hara.common.emit-prep-lua-test
  (:require [hara.lang.base.book :as b]
            [hara.common.emit-common :as common]
            [hara.common.emit-helper :as helper]
            [hara.common.grammar :as grammar]
            [hara.common.util :as ut]
            [hara.model.spec-lua :as lua]
            [std.lib.env :as env])
  (:use code.test))

(def +book-empty+
  (b/book {:lang :lua
           :meta lua/+meta+
           :grammar (grammar/grammar :lua
                      (grammar/to-reserved (grammar/build))
                      helper/+default+)}))

(def +core-module+
  (b/book-module
   {:id       'L.core
    :lang     :lua
    :link     '{- L.core}}))

(def +core-fragment-add+
  (b/book-entry {:lang :lua
                 :id 'add
                 :module 'L.core
                 :section :fragment
                 :form       '(fn [x y] (list '+ x y))
                 :template   (fn [x y] (list '+ x y))
                 :standalone true
                 :namespace (env/ns-sym)}))

(def +core-fragment-sub+
  (b/book-entry {:lang :lua
                 :id 'sub
                 :module 'L.core
                 :section :fragment
                 :template    (fn [x y] (list '- x y))
                 :standalone '(fn [x y] (return (- x y)))
                 :namespace (env/ns-sym)}))

(def +core-code-identity-fn+
  (b/book-entry {:lang :lua
                 :id 'identity-fn
                 :module 'L.core
                 :section :code
                 :form '(defn identity-fn [x] (return x))
                 :form-input '(defn identity-fn [x] (return x))
                 :deps #{}
                 :namespace (env/ns-sym)
                 :declared false}))

(def +book-min+
  (-> +book-empty+
      (b/set-module +core-module+)
      second
      (b/set-entry +core-fragment-add+)
      second
      (b/set-entry +core-fragment-sub+)
      second
      (b/set-entry +core-code-identity-fn+)
      second))



;;
;;
;;
;;

