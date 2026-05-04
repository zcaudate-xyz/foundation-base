(ns hara.lang.impl-entry-test
  (:require [hara.lang.book :as b]
              [hara.lang.book-entry :as e]
             [hara.common.emit :as emit]
             [hara.common.emit-prep-lua-test :as prep]
             [hara.lang.impl-entry :as entry]
             [hara.lang.library-snapshot :as snap]
             [hara.lang.library-snapshot-prep-test :as lprep]
              [std.lib.env :as env])
  (:use code.test))

(defn make-entry
  []
  (entry/create-code-base
   '(defn add-fn
      [a b]
      (return (-/add a (-/add a 1))))
   {:lang :lua
    :namespace 'L.core
    :module 'L.core}
   @emit/+test-grammar+))

(fact "hydrate hooks can enrich the returned entry"

  (let [entry (make-entry)]
    (-> (entry/create-code-hydrate entry
                                   (assoc (get-in @emit/+test-grammar+ [:reserved 'defn])
                                          :hydrate-hook (fn [entry]
                                                          (assoc entry :probe/value true)))
                                   @emit/+test-grammar+
                                   (:modules prep/+book-min+)
                                   '{:id L.core
                                     :alias {}
                                     :link  {- L.core}})
        :probe/value))
  => true)

(fact "xtalk metadata is derived from the hydrated form"

  (let [entry (make-entry)]
    (-> (entry/create-code-hydrate
         (assoc entry :form-input '(do raw))
         {:hydrate (fn [_ _ _]
                     [nil '(do (x:nil? data))])}
         @emit/+test-grammar+
         (:modules prep/+book-min+)
         '{:id L.core
           :alias {}
           :link  {- L.core}})
        (select-keys [:xtalk-ops :xtalk-profiles :polyfill-modules])))
  => '{:xtalk-ops #{:x-nil?}
       :xtalk-profiles #{:xtalk-common}
       :polyfill-modules #{}})

(fact "entry emit failures include entry context"

  (let [grammar (assoc-in @emit/+test-grammar+
                          [:reserved 'boom-op]
                          {:op :boom-op
                           :emit (fn [_ _ _]
                                   (throw (ex-info "boom" {:probe true})))})
        entry   (assoc (entry/create-code-base
                        '(defn explode-fn
                           [a]
                           (return (boom-op a)))
                        {:lang :lua
                         :namespace (env/ns-sym)
                         :module 'L.core
                         :line 42}
                        grammar)
                       :form '(defn explode-fn
                                [a]
                                (return (boom-op a))))]
    (try
      (entry/emit-entry-raw grammar
                            entry
                            '{:layout :full})
      nil
      (catch Throwable t
        (let [data (ex-data t)]
          {:probe (:probe data)
           :phase (:hara/phase data)
           :subsystem (:hara/subsystem data)
           :lang (:hara/lang data)
           :module (:hara/module data)
           :entry (-> data :hara/entry :symbol)
           :line (:hara/line data)
           :stack (mapv (juxt :hara/phase :hara/subsystem)
                        (:hara/provenance-stack data))}))))
  => '{:probe true
        :phase :emit/form
        :subsystem :hara.common.emit-top-level/emit-form
        :lang :lua
        :module L.core
        :entry L.core/explode-fn
        :line 42
        :stack [[:emit/form :hara.common.emit-top-level/emit-form]
                [:emit/entry :hara.lang.impl-entry/emit-entry-raw]]})

(fact "emits template entries using merged parent modules from the snapshot"

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

^{:refer hara.lang.impl-entry/create-common :added "4.0"}
(fact "create entry common keys from metadata"

  (entry/create-common {:lang :lua
                        :namespace 'L.core
                        :module 'L.core
                        :line 1
                        :time 1})
  => '{:lang :lua, :namespace L.core, :module L.core, :line 1, :time 1})

^{:refer hara.lang.impl-entry/create-code-raw :added "4.0"}
(fact "creates a raw entry compatible with submit"

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
                b/book-entry?])

  (def template-sum {:sum 3})
  (def +template-sum+ {:sum 4})

  (let [[_ entry]
        (entry/create-code-raw
         '(defn add-fn
            [a b]
            (return (@! template-sum)))
         (get-in @emit/+test-grammar+ [:reserved 'defn])
         {:lang :lua
          :namespace 'L.core
          :module 'L.core})]
    (:form-input entry))
   => '(defn add-fn
         [a b]
        (return {:sum 3}))

  (let [[_ entry]
        (entry/create-code-raw
         '(defn add-fn
            [a b]
            (return @+template-sum+))
         (get-in @emit/+test-grammar+ [:reserved 'defn])
         {:lang :lua
          :namespace 'L.core
          :module 'L.core})]
    (:form-input entry))
  => '(defn add-fn
        [a b]
        (return {:sum 4})))

^{:refer hara.lang.impl-entry/create-code-base :added "4.0"}
(fact "creates the base code entry"

  (entry/create-code-base
   '(defn add-fn
      [a b]
      (return (+ a b)))
   {:lang :lua
    :namespace 'L.core
    :module 'L.core}
   @emit/+test-grammar+)
  => e/book-entry?)

^{:refer hara.lang.impl-entry/hydrate-form :added "4.1"}
(fact "hydrates input forms through the reserved hydrate hook"

  (entry/hydrate-form '(do raw)
                      {:hydrate (fn [_ _ _]
                                  [{:probe true}
                                   '(do (x:get-in data ["a"]))])}
                      {})
  => '[{:probe true}
       (do (x:get-in data ["a"]))])

^{:refer hara.lang.impl-entry/create-code-hydrate :added "4.0"
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

  (-> (entry/create-code-hydrate +entry+
                                 (get-in @emit/+test-grammar+ [:reserved 'defn])
                                 @emit/+test-grammar+
                                 (:modules prep/+book-min+)
                                 '{:id L.core
                                   :alias {}
                                   :link  {- L.core}})
      :form)
  => '(defn add-fn [a b] (return (+ a (+ a 1)))))

^{:refer hara.lang.impl-entry/prepare-code-entry :added "4.1"}
(fact "TODO")

^{:refer hara.lang.impl-entry/create-code :added "4.0"}
(fact "creates the code entry"

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

^{:refer hara.lang.impl-entry/create-fragment :added "4.0"}
(fact "creates a fragment"

  (entry/create-fragment
   '(def$ G G)
   {:lang :lua
    :namespace 'L.core
    :module 'L.core})
  => e/book-entry?)

^{:refer hara.lang.impl-entry/create-fragment-hydrate :added "4.0"}
(fact "hydrates the forms"
  "placeholder for tests")

^{:refer hara.lang.impl-entry/create-macro :added "4.0"}
(fact "creates a macro"

  (entry/create-macro
   '(defmacro mul [a b] (list '* a b))
   {:lang :lua
    :namespace 'L.core
    :module 'L.core})
  => e/book-entry?)

^{:refer hara.lang.impl-entry/with:cache-none :added "4.0"}
(fact "skips the cache"
  (entry/with:cache-none entry/*cache-none*) => true)

^{:refer hara.lang.impl-entry/with:cache-force :added "4.0"}
(fact "forces the cache to update"
  (entry/with:cache-force entry/*cache-force*) => true)

^{:refer hara.lang.impl-entry/emit-entry-raw :added "4.0"
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

  (entry/emit-entry-raw @emit/+test-grammar+
                        +entry+
                        '{:module {:internal #{L.core}}
                          :layout :full})
  => "function L_core____add_fn(a,b){\n  return a + L_core____identity_fn(b);\n}")

^{:refer hara.lang.impl-entry/emit-entry-cached :added "4.0"
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

  (entry/emit-entry-cached {:grammar (:grammar +book+)
                            :entry (get-in +book+ '[:modules L.core :code add-fn])
                            :mopts {:layout :full
                                    :module (assoc (get-in +book+ '[:modules L.core])
                                                   :display :brief)}})
  => "function L_core____add_fn(a,b){\n  return G + L_core____identity_fn(a + b);\n}")

^{:refer hara.lang.impl-entry/emit-entry-label :added "4.0"}
(fact "emits the entry label"

  (entry/emit-entry-label (:grammar +book+)
                          (get-in +book+ '[:modules L.core :code add-fn]))
  => "// L.core/add-fn [] ")

^{:refer hara.lang.impl-entry/emit-entry :added "4.0"}
(fact "emits a given entry"

  (entry/emit-entry (:grammar +book+)
                    (get-in +book+ '[:modules L.core :code add-fn])
                    {:layout :full
                     :module (assoc (get-in +book+ '[:modules L.core])
                                    :display :brief)
                     :emit {:label true}})
  => "// L.core/add-fn [] \nfunction L_core____add_fn(a,b){\n  return G + L_core____identity_fn(a + b);\n}")

(comment
  (./import))