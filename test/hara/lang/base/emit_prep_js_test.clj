(ns hara.common.emit-prep-js-test
  (:require [hara.lang.base.book :as b]
            [hara.common.emit-common :as common]
            [hara.common.emit-helper :as helper]
            [hara.common.grammar :as grammar]
            [hara.common.util :as ut]
            [hara.model.spec-js :as js]
            [hara.model.spec-js.meta :as js-meta]
            [std.lib.env :as env])
  (:use code.test))

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
                 :namespace (env/ns-sym)}))

(def +core-code-identity-fn+
  (b/book-entry {:lang :js
                 :id 'identity-fn
                 :module 'JS.core
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
      (b/set-entry +core-code-identity-fn+)
      second))


