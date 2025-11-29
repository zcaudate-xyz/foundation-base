(ns code.test.manage-test
  (:require [code.test.manage :refer :all]
            [code.test :refer [fact contains]]
            [code.test.base.runtime :as rt]
            [code.project :as project]
            [code.test.base.executive :as executive]))

^{:refer code.test.manage/fact:global-map :added "3.0"}
(fact "sets and gets the global map"
  ^:hidden
  
  (rt/with-new-context {}
    (fact:global-map *ns* {:a 1})
    (rt/get-global *ns*))
  => (contains {:a 1}))

^{:refer code.test.manage/fact:global-fn :added "3.0"}
(fact "global getter and setter"
  ^:hidden
  
  (rt/with-new-context {}
    (fact:global-fn :set {:a 1})
    (fact:global-fn :get))
  => {:a 1})

^{:refer code.test.manage/fact:global :added "3.0"}
(fact "fact global getter and setter"
  ^:hidden
  
  (rt/with-new-context {}
    (fact:global :set {:a 1})
    (fact:global :get))
  => {:a 1})

^{:refer code.test.manage/fact:ns-load :added "3.0"}
(fact "loads a test namespace"
  ^:hidden
  
  (with-redefs [project/in-context (fn [f] (f))
                executive/load-namespace (fn [ns & _] ns)]
    (fact:ns-load 'my.ns))
  => 'my.ns)

^{:refer code.test.manage/fact:ns-unload :added "3.0"}
(fact "unloads a test namespace"
  ^:hidden
  
  (with-redefs [project/in-context (fn [f] (f))
                executive/unload-namespace (fn [ns & _] ns)]
    (fact:ns-unload 'my.ns))
  => 'my.ns)

^{:refer code.test.manage/fact:ns-alias :added "3.0"}
(fact "imports all aliases into current namespace"
  ^:hidden
  
  (with-redefs [ns-aliases (fn [_] {'alias (find-ns 'clojure.core)})
                clojure.core/require (fn [& _] nil)]
    (fact:ns-alias *ns*))
  => [['clojure.core :as 'alias]])

^{:refer code.test.manage/fact:ns-unalias :added "3.0"}
(fact "removes all aliases from current namespace"
  ^:hidden
  
  (with-redefs [ns-aliases (fn [_] {'alias (find-ns 'clojure.core)})
                clojure.core/ns-unalias (fn [& _] nil)]
    (fact:ns-unalias *ns*))
  => [['clojure.core :as 'alias]])

^{:refer code.test.manage/fact:ns-intern :added "3.0"}
(fact "imports all interns into current namespace"
  ^:hidden
  
  (with-redefs [ns-interns (fn [_] {'sym #'print})
                std.lib/intern-var (fn [& _] nil)]
    (fact:ns-intern *ns*))
  => [nil])

^{:refer code.test.manage/fact:ns-unintern :added "3.0"}
(fact "removes all interns into current namespace"
  ^:hidden
  
  (with-redefs [ns-interns (fn [_] {'sym #'print})
                clojure.core/ns-unmap (fn [& _] nil)]
    (fact:ns-unintern *ns*))
  => ['sym])

^{:refer code.test.manage/fact:ns-import :added "3.0"}
(fact "loads, imports and aliases current namespace"
  ^:hidden
  
  (with-redefs [rt/add-link (fn [_] nil)
                fact:ns-load (fn [_] nil)
                fact:ns-alias (fn [_] nil)
                fact:global-fn (fn [_] nil)]
    (fact:ns-import 'my.ns))
  => nil)

^{:refer code.test.manage/fact:ns-unimport :added "3.0"}
(fact "unload, unimports and unalias current namespace"
  ^:hidden
  
  (with-redefs [fact:global-fn (fn [_] nil)
                fact:ns-unalias (fn [_] nil)
                fact:ns-unload (fn [_] nil)
                rt/remove-link (fn [_] nil)]
    (fact:ns-unimport 'my.ns))
  => nil)

^{:refer code.test.manage/fact:ns-fn :added "3.0"}
(fact "fact ns getter and setter"
  ^:hidden
  
  (with-redefs [rt/add-link (fn [_] :link)]
    (fact:ns-fn '((:link my.ns))))
  => [[:link]])

^{:refer code.test.manage/fact:ns :added "3.0"}
(fact "provides a macro for managing namespace-related operations within facts"
  ;; (code.test.manage/fact:ns (:link my.ns))
  ;; => [[:link]]
  true => true)
