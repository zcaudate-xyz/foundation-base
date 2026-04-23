(ns std.lang.base.impl-template-test
  (:require [std.lang.base.book :as b]
             [std.lang.base.emit-helper :as helper]
             [std.lang.base.emit-prep-lua-test :as prep]
             [std.lang.base.grammar :as grammar]
             [std.lang.base.impl-template :refer :all]
             [std.lang.base.impl-entry :as entry]
             [std.lib.env :as env]
             [std.lib.walk :as walk])
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

(defn template-child-rewrite
  [form _ _]
  (walk/postwalk (fn [x]
                   (if (= x '(return value))
                     '(return (+ value 2))
                     x))
                 form))

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
   (assoc helper/+default+ :rewrite [#'template-child-rewrite])))

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

  (select-keys +template-parent-entry+ [:static/template :deps :form])
  => '{:static/template true
       :deps #{L.core/probe-hard-link}
       :form (defn template-fn [value] (L.core/probe-hard-link value))})

(fact "template entries restage for child deps and child emit"

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
        "function template_fn(value){\n  return value + 2;\n}"])


^{:refer std.lang.base.impl-template/infer-static-template :added "4.1"}
(fact "infers template restaging when a hard-link is used"
  (infer-static-template +template-parent-grammar+
                         '(defn template-fn [value]
                            (x:probe value)))
  => true

  (infer-static-template +template-parent-grammar+
                         '(defn plain-fn [value]
                            (return value)))
  => false)

^{:refer std.lang.base.impl-template/create-code-state :added "4.1"}
(fact "hydrates and stages a code entry for the current grammar"
  (select-keys
   (create-code-state (dissoc +template-parent-entry+
                              :hmeta
                              :deps
                              :deps-fragment
                              :deps-native
                              :xtalk-ops
                              :xtalk-profiles
                              :polyfill-modules
                              :static/template
                              :form)
                      (get-in +template-parent-grammar+ [:reserved 'defn])
                      +template-parent-grammar+
                      (:modules +template-helper-book+)
                      '{:lang :template
                        :module {:id L.core
                                 :alias {}
                                 :link {- L.core}}})
   [:form :deps :deps-fragment :deps-native :xtalk-ops :xtalk-profiles :polyfill-modules :static/template])
  => '{:form (defn template-fn [value] (L.core/probe-hard-link value))
       :deps #{L.core/probe-hard-link}
       :deps-fragment #{}
       :deps-native {}
       :xtalk-ops #{}
       :xtalk-profiles #{}
       :polyfill-modules #{}
       :static/template true})

^{:refer std.lang.base.impl-template/cached-code-state :added "4.1"}
(fact "restages template entries using the per-entry cache"
  (select-keys
   (cached-code-state +template-parent-entry+
                       (get-in +template-parent-grammar+ [:reserved 'defn])
                       +template-parent-grammar+
                      (:modules +template-helper-book+)
                       '{:lang :template
                         :module {:id L.core
                                  :alias {}
                                  :link {- L.core}}})
   [:form :deps :static/template])
  => '{:form (defn template-fn [value] (return (+ value 2)))
        :deps #{}
        :static/template true})

^{:refer std.lang.base.impl-template/cached-entry-deps :added "4.1"}
(fact "returns restaged code dependencies for the current language"
  (cached-entry-deps {:modules (:modules +template-helper-book+)
                      :grammar +template-parent-grammar+
                      :lang :template}
                     +template-parent-entry+)
  => #{})
