(ns std.lang.base.impl-entry-test
  (:require [std.lang.base.book :as b]
              [std.lang.base.book-entry :as e]
             [std.lang.base.emit :as emit]
             [std.lang.base.emit-prep-lua-test :as prep]
             [std.lang.base.impl-entry :as entry]
             [std.lang.base.library-snapshot :as snap]
             [std.lang.base.library-snapshot-prep-test :as lprep]
             [std.lib.env :as env])
  (:use code.test))

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

^{:refer std.lang.base.impl-entry/hydrate-form :added "4.1"}
(fact "hydrates input forms through the reserved hydrate hook"
  ^:hidden

  (entry/hydrate-form '(do raw)
                      {:hydrate (fn [_ _ _]
                                  [{:probe true}
                                   '(do (x:get-in data ["a"]))])}
                      {})
  => '[{:probe true}
       (do (x:get-in data ["a"]))])

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

(fact "hydrate hooks can enrich the returned entry"
  ^:hidden

  (-> (entry/create-code-hydrate +entry+
                                 (assoc (get-in @emit/+test-grammar+ [:reserved 'defn])
                                        :hydrate-hook (fn [entry]
                                                        (assoc entry :probe/value true)))
                                 @emit/+test-grammar+
                                 (:modules prep/+book-min+)
                                 '{:id L.core
                                   :alias {}
                                   :link  {- L.core}})
      :probe/value)
  => true)

(fact "xtalk metadata is derived from the hydrated form"
  ^:hidden

  (-> (entry/create-code-hydrate
       (assoc +entry+ :form-input '(do raw))
       {:hydrate (fn [_ _ _]
                   [nil '(do (x:nil? data))])}
       @emit/+test-grammar+
       (:modules prep/+book-min+)
       '{:id L.core
         :alias {}
         :link  {- L.core}})
      (select-keys [:xtalk-ops :xtalk-profiles :polyfill-modules]))
  => '{:xtalk-ops #{:x-nil?}
       :xtalk-profiles #{:xtalk-common}
       :polyfill-modules #{}})

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
(fact "skips the cache"
  (entry/with:cache-none entry/*cache-none*) => true)

^{:refer std.lang.base.impl-entry/with:cache-force :added "4.0"}
(fact "forces the cache to update"
  (entry/with:cache-force entry/*cache-force*) => true)

^{:refer std.lang.base.impl-entry/emit-entry-raw :added "4.0"
  :setup [(def +entry+ (entry/create-code '(defn add-fn
                                             [a b]
                                             (return (-/add a (-/identity-fn b))))
                                          {:lang :lua
                                           :namespace (env/ns-sym)
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
                               :namespace (env/ns-sym)
                               :module 'L.core}))
                second
                ((fn [book]
                   (b/set-entry book
                                (entry/create-code
                                 '(defn add-fn
                                    [a b]
                                    (return (-/add -/G (-/identity-fn (-/add a b)))))
                                 {:lang :lua
                                  :namespace (env/ns-sym)
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

(fact "emits template entries using merged parent modules from the snapshot"
  ^:hidden

  (let [child-module (b/book-module {:id 'L.core
                                     :lang :lua
                                     :link '{p x.core
                                             - L.core}})
        child-book   (-> (b/book {:lang :lua
                                  :meta (:meta prep/+book-empty+)
                                  :grammar (:grammar prep/+book-empty+)
                                  :parent :x})
                         (b/set-module child-module)
                         second)
        snapshot-0   (-> (snap/snapshot {})
                         (snap/add-book lprep/+book-x+)
                         (snap/add-book child-book))
        merged-book-0 (snap/get-book snapshot-0 :lua)
        child-entry-0 (entry/create-code-base
                       '(defn call-parent [a]
                          (return (p/add a 1)))
                       {:lang :lua
                        :namespace (env/ns-sym)
                        :module 'L.core}
                       (:grammar merged-book-0))
        snapshot-1   (second (snap/set-entry snapshot-0 child-entry-0))
        merged-book-1 (snap/get-book snapshot-1 :lua)
        child-entry-1 (assoc (get-in merged-book-1 '[:modules L.core :code call-parent])
                             :static/template true)]
    (entry/emit-entry (:grammar merged-book-1)
                      child-entry-1
                      {:snapshot snapshot-1
                       :layout :full
                       :emit {:label false}}))
  => "function L_core____call_parent(a){\n  return a + 1;\n}")

(comment
  (./import))


^{:refer std.lang.base.impl-entry/create-fragment-hydrate :added "4.0"}
(fact "hydrates the forms"
  "placeholder for tests")
