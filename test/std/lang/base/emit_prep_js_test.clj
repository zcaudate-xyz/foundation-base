(ns std.lang.base.emit-prep-js-test
  (:use code.test)
  (:require [std.lang.base.book :as b]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.util :as ut]
            [std.lang.model.spec-js :as js]
            [std.lang.model.spec-js.meta :as js-meta]
            [std.lib :as h]))

(def +book-empty+
  (b/book {:lang :js
           :meta js-meta/+meta+
           :grammar (grammar/grammar :js
                      (grammar/to-reserved (grammar/build))
                      helper/+default+)}))

(def +core-module+
  (b/book-module
   {:id       'JS.core
    :lang     :js
    :link     '{- JS.core}}))

(def +core-fragment-add+
  (b/book-entry {:lang :js
                 :id 'add
                 :module 'JS.core
                 :section :fragment
                 :form       '(fn [x y] (list '+ x y))
                 :template   (fn [x y] (list '+ x y))
                 :standalone true
                 :namespace (h/ns-sym)}))

(def +core-code-identity-fn+
  (b/book-entry {:lang :js
                 :id 'identity-fn
                 :module 'JS.core
                 :section :code
                 :form '(defn identity-fn [x] (return x))
                 :form-input '(defn identity-fn [x] (return x))
                 :deps #{}
                 :namespace (h/ns-sym)
                 :declared false}))

(def +book-min+
  (-> +book-empty+
      (b/set-module +core-module+)
      second
      (b/set-entry +core-fragment-add+)
      second
      (b/set-entry +core-code-identity-fn+)
      second))


