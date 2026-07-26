tahto/common/emit_prep_js_test.clj:1:(ns tahto.common.emit-prep-js-test
  (:require [tahto.base.book :as b]
tahto/common/emit_prep_js_test.clj:3:            [tahto.common.emit-common :as common]
tahto/common/emit_prep_js_test.clj:4:            [tahto.common.emit-helper :as helper]
tahto/common/emit_prep_js_test.clj:5:            [tahto.common.grammar :as grammar]
tahto/common/emit_prep_js_test.clj:6:            [tahto.common.util :as ut]
tahto/common/emit_prep_js_test.clj:7:            [tahto.model.spec-js :as js]
tahto/common/emit_prep_js_test.clj:8:            [tahto.model.spec-js.meta :as js-meta]
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


