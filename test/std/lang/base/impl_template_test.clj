(ns std.lang.base.impl-template-test
  (:require [std.lang.base.book :as b]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.emit-prep-lua-test :as prep]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.impl-entry :as entry]
            [std.lib.env :as env])
  (:use code.test))

(def +template-build+
  (grammar/build:extend
   (grammar/build)
   {:x-probe {:op :x-probe
              :symbol #{'x:probe}
              :emit :hard-link
              :raw 'L.core/probe-hard-link}}))

(def +template-parent-grammar+
  (grammar/grammar :template-parent
                   (grammar/to-reserved +template-build+)
                   helper/+default+))

(def +template-child-grammar+
  (grammar/grammar
   :template-child
   (grammar/to-reserved
    (grammar/build:override
     +template-build+
     {:x-probe {:emit :macro
                :type :template
                :macro (fn [[_ value]]
                         (list 'return value))}}))
   helper/+default+))

(def +template-module+
  (b/book-module
   {:id 'L.core
    :lang :template
    :link '{- L.core}}))

(def +template-namespace+
  (env/ns-sym))

(def +template-helper-book+
  (let [book {:grammar +template-child-grammar+
              :modules {'L.core +template-module+}}]
    (-> (b/book {:lang :template
                 :meta (:meta prep/+book-empty+)
                 :grammar +template-child-grammar+})
        (b/set-module +template-module+)
        second
        (b/set-entry
         (entry/create-code
          '(defn probe-hard-link [value]
             (return (+ value 1)))
          {:lang :template
           :namespace +template-namespace+
           :module 'L.core}
          book))
        second)))

(def +template-parent-entry+
  (let [base (entry/create-code-base
              '(defn template-fn [value]
                 (x:probe value))
              {:lang :template
               :namespace +template-namespace+
               :module 'L.core}
              +template-parent-grammar+)]
    (entry/create-code-hydrate base
                               (get-in +template-parent-grammar+ [:reserved 'defn])
                               +template-parent-grammar+
                               (:modules +template-helper-book+)
                               '{:lang :template
                                 :id L.core
                                 :alias {}
                                 :link {- L.core}})))

(fact "hard-link usage is inferred as :static/template during hydration"
  ^:hidden

  (select-keys +template-parent-entry+ [:static/template :deps :form])
  => '{:static/template true
       :deps #{L.core/probe-hard-link}
       :form (defn template-fn [value] (L.core/probe-hard-link value))})

(fact "template entries restage for child deps and child emit"
  ^:hidden

  (let [book (-> +template-helper-book+
                 (b/set-entry +template-parent-entry+)
                 second)
        snapshot {:template {:book book}}]
    [(b/get-code-deps book 'L.core/template-fn)
     (entry/emit-entry (:grammar book)
                       (get-in book '[:modules L.core :code template-fn])
                       {:lang :template
                        :snapshot snapshot
                        :module (assoc (get-in book '[:modules L.core])
                                       :display :brief)
                        :emit {:label false}})])
  => '[#{}
        "function template_fn(value){\n  return value;\n}"])
