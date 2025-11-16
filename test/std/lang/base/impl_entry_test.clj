(ns std.lang.base.impl-entry-test
  (:use code.test)
  (:require [std.lang.base.impl-entry :as entry]
            [std.lang.base.book-entry :as e]
            [std.lang.base.book :as b]
            [std.lang.base.emit :as emit]
            [std.lang.base.emit-prep-lua-test :as prep]
            [std.lib :as h]))

^{:refer std.lang.base.impl-entry/create-common :added "4.0"}
(fact "create entry common keys from metadata"
  ^:hidden
  
  (entry/create-common {:lang :lua
                        :namespace 'L.core
                        :module 'L.core
                        :line 1
                        :time 1})
  => '{:lang :lua, :namespace L.core, :module L.core, :line 1, :time 1})

^{:refer std.lang.base.impl-entry/create-code-raw :added "4.0"}
(fact "creates a raw entry compatible with submit"
  ^:hidden
  
  (entry/create-code-raw
   '(defn add-fn
      "hello"
      {:a 1}
      [a b]
      (return (+ a b)))
   (get-in @emit/+test-grammar+ [:reserved 'defn])
   {:lang :lua
    :namespace 'L.core
    :module 'L.core})
  => (contains [{:a 1, :doc "hello"}
                b/book-entry?]))

^{:refer std.lang.base.impl-entry/create-code-base :added "4.0"}
(fact "creates the base code entry"
  ^:hidden
  
  (entry/create-code-base
   '(defn add-fn
      [a b]
      (return (+ a b)))
   {:lang :lua
    :namespace 'L.core
    :module 'L.core}
   @emit/+test-grammar+)
  => e/book-entry?)

^{:refer std.lang.base.impl-entry/create-code-hydrate :added "4.0"
  :setup [(def +entry+
            (entry/create-code-base
             '(defn add-fn
                [a b]
                (return (-/add a (-/add a 1))))
             {:lang :lua
              :namespace 'L.core
              :module 'L.core}
             @emit/+test-grammar+))]}
(fact "hydrates the forms"
  ^:hidden
  
  (-> (entry/create-code-hydrate +entry+
                                 (get-in @emit/+test-grammar+ [:reserved 'defn])
                                 @emit/+test-grammar+
                                 (:modules prep/+book-min+)
                                 '{:id L.core
                                   :alias {}
                                   :link  {- L.core}})
      :form)
  => '(defn add-fn [a b] (return (+ a (+ a 1)))))

^{:refer std.lang.base.impl-entry/create-code :added "4.0"}
(fact "creates the code entry"
  ^:hidden
  
  (-> (entry/create-code '(defn add-fn
                            [a b]
                            (return (-/add a (-/identity-fn b))))
                         {:lang :lua
                          :namespace 'L.core
                          :module 'L.core}
                         prep/+book-min+
                         {})
      :form)
  => '(defn add-fn [a b] (return (+ a (L.core/identity-fn b)))))

^{:refer std.lang.base.impl-entry/create-fragment :added "4.0"}
(fact "creates a fragment"
  ^:hidden
  
  (entry/create-fragment
   '(def$ G G)
   {:lang :lua
    :namespace 'L.core
    :module 'L.core})
  => e/book-entry?)

^{:refer std.lang.base.impl-entry/create-macro :added "4.0"}
(fact "creates a macro"
  ^:hidden
  
  (entry/create-macro
   '(defmacro mul [a b] (list '* a b))
   {:lang :lua
    :namespace 'L.core
    :module 'L.core})
  => e/book-entry?)

^{:refer std.lang.base.impl-entry/with:cache-none :added "4.0"}
(fact "skips the cache")

^{:refer std.lang.base.impl-entry/with:cache-force :added "4.0"}
(fact "forces the cache to update")

^{:refer std.lang.base.impl-entry/emit-entry-raw :added "4.0"
  :setup [(def +entry+ (entry/create-code '(defn add-fn
                                             [a b]
                                             (return (-/add a (-/identity-fn b))))
                                          {:lang :lua
                                           :namespace (h/ns-sym)
                                           :module 'L.core}
                                          prep/+book-min+
                                          '{:id L.util
                                            :alias {}
                                            :link  {- L.core}}))]}
(fact "emits using the raw entry"
  ^:hidden
  
  (entry/emit-entry-raw @emit/+test-grammar+
                        +entry+
                        '{:module {:internal #{L.core}}
                          :layout :full})
  => "function L_core____add_fn(a,b){\n  return a + L_core____identity_fn(b);\n}")

^{:refer std.lang.base.impl-entry/emit-entry-cached :added "4.0"
  :setup [(def +book+
            (-> prep/+book-min+
                (b/set-entry (entry/create-fragment
                              '(def$ G G)
                              {:lang :lua
                               :namespace (h/ns-sym)
                               :module 'L.core}))
                second
                ((fn [book]
                   (b/set-entry book
                                (entry/create-code
                                 '(defn add-fn
                                    [a b]
                                    (return (-/add -/G (-/identity-fn (-/add a b)))))
                                 {:lang :lua
                                  :namespace (h/ns-sym)
                                  :module 'L.core}
                                 book))))
                second))]}
(fact "emits using a potentially cached entry"
  ^:hidden
  
  (entry/emit-entry-cached {:grammar (:grammar +book+)
                            :entry (get-in +book+ '[:modules L.core :code add-fn])
                            :mopts {:layout :full
                                    :module (assoc (get-in +book+ '[:modules L.core])
                                                   :display :brief)}})
  => "function L_core____add_fn(a,b){\n  return G + L_core____identity_fn(a + b);\n}")

^{:refer std.lang.base.impl-entry/emit-entry-label :added "4.0"}
(fact "emits the entry label"
  ^:hidden
  
  (entry/emit-entry-label (:grammar +book+)
                          (get-in +book+ '[:modules L.core :code add-fn]))
  => "// L.core/add-fn [] ")

^{:refer std.lang.base.impl-entry/emit-entry :added "4.0"}
(fact "emits a given entry"
  ^:hidden
  
  (entry/emit-entry (:grammar +book+)
                    (get-in +book+ '[:modules L.core :code add-fn])
                    {:layout :full
                     :module (assoc (get-in +book+ '[:modules L.core])
                                    :display :brief)
                     :emit {:label true}})
  => "// L.core/add-fn [] \nfunction L_core____add_fn(a,b){\n  return G + L_core____identity_fn(a + b);\n}")

(comment
  (./import))


^{:refer std.lang.base.impl-entry/create-fragment-hydrate :added "4.0"}
(fact "TODO")