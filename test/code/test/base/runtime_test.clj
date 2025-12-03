(ns code.test.base.runtime-test
  (:use code.test)
  (:require [code.test.base.runtime :as rt]
            [std.lib :as h]))

^{:refer code.test.base.runtime/new-context :added "4.1"}
(fact "creates a new context"

  (rt/new-context)
  => map?)

^{:refer code.test.base.runtime/with-new-context :added "4.1"}
(fact "override functions with new context"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:facts {:a 1} :flags {:b 2}}})}
    (rt/purge-all 'my.ns))
  => 'my.ns)

^{:refer code.test.base.runtime/purge-all :added "4.0"}
(fact "purges all facts from namespace"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:facts {:a 1} :flags {:b 2}}})}
    (rt/purge-all 'my.ns)
    (rt/all-facts 'my.ns))
  => nil)

^{:refer code.test.base.runtime/get-global :added "4.0"}
(fact "gets the global settings for namespace"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:global {:a 1}}})}
    (rt/get-global 'my.ns))
  => {:a 1}

  (rt/with-new-context {:registry (atom {'my.ns {:global {:a {:b 2}}}})}
    (rt/get-global 'my.ns :a :b))
  => 2)

^{:refer code.test.base.runtime/set-global :added "4.0"}
(fact "sets the global settings for namespace"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {})}
    (rt/set-global 'my.ns {:check {:setup '[(prn "hello")]}})
    (rt/get-global 'my.ns))
  => {:check {:setup '[(prn "hello")]}})

^{:refer code.test.base.runtime/update-global :added "4.0"}
(fact "updates global data"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:global {:a 1}}})}
    (rt/update-global 'my.ns assoc :b 2)
    (rt/get-global 'my.ns))
  => {:a 1 :b 2})

^{:refer code.test.base.runtime/list-links :added "4.0"}
(fact "lists ns links"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:links #{'other.ns}}})}
    (rt/list-links 'my.ns))
  => #{'other.ns})

^{:refer code.test.base.runtime/clear-links :added "4.0"}
(fact "clear ns links"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:links #{'other.ns}}})}
    (rt/clear-links 'my.ns)
    (rt/list-links 'my.ns))
  => nil)

^{:refer code.test.base.runtime/add-link :added "4.0"}
(fact "add ns link"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {})}
    (rt/add-link 'my.ns 'other.ns)
    (rt/list-links 'my.ns))
  => #{'other.ns})

^{:refer code.test.base.runtime/remove-link :added "4.0"}
(fact "remove ms link"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:links #{'other.ns}}})}
    (rt/remove-link 'my.ns 'other.ns)
    (rt/list-links 'my.ns))
  => #{})

^{:refer code.test.base.runtime/all-facts :added "4.0"}
(fact "retrieves a list of all the facts in a namespace"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:facts {:f1 1}}})}
    (keys (rt/all-facts 'my.ns)))
  => '(:f1))

^{:refer code.test.base.runtime/list-facts :added "4.0"}
(fact "lists all facts in current namespace"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:facts {'test-f1 {:id 'test-f1 :line 10}
                                                         'test-f2 {:id 'test-f2 :line 20}}}})}
    (rt/list-facts 'my.ns))
  => '(test-f1 test-f2))

^{:refer code.test.base.runtime/purge-facts :added "4.0"}
(fact "purges all facts in the namespace (for reload)"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:facts {:a 1} :flags {:b 2}}})}
    (rt/purge-facts 'my.ns)
    (rt/list-facts 'my.ns))
  => [])

^{:refer code.test.base.runtime/parse-args :added "4.0"}
(fact "helper function for variable args"
  ^:hidden
  
  (rt/parse-args 'my.ns nil :arg [:more])
  => ['my.ns nil '(:arg :more)])

^{:refer code.test.base.runtime/get-fact :added "4.0"}
(fact "gets a fact"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:facts {'f1 {:data "hello"}}}})}
    (rt/get-fact 'my.ns 'f1 :data))
  => "hello")

^{:refer code.test.base.runtime/set-fact :added "4.0"}
(fact "sets the entire data on a fact"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {})}
    (rt/set-fact 'my.ns 'f1 {:data "hello"})
    (rt/get-fact 'my.ns 'f1))
  => {:data "hello"})

^{:refer code.test.base.runtime/set-in-fact :added "4.0"}
(fact "sets the property on a fact"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {})}
    (rt/set-in-fact 'my.ns 'f1 [:data] "hello")
    (rt/get-fact 'my.ns 'f1 :data))
  => "hello")

^{:refer code.test.base.runtime/get-flag :added "4.0"}
(fact "checks if the setup flag has been set"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:flags {'f1 {:setup true}}}})}
    (rt/get-flag 'my.ns 'f1 :setup))
  => true)

^{:refer code.test.base.runtime/set-flag :added "4.0"}
(fact "sets the setup flag"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {})}
    (rt/set-flag 'my.ns 'f1 :setup true)
    (rt/get-flag 'my.ns 'f1 :setup))
  => true)

^{:refer code.test.base.runtime/update-fact :added "4.0"}
(fact "updates a fact given a function"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:facts {'f1 {:count 1}}}})}
    (rt/update-fact 'my.ns 'f1 update-in [:count] inc)
    (rt/get-fact 'my.ns 'f1 :count))
  => 2)

^{:refer code.test.base.runtime/remove-fact :added "4.0"}
(fact "removes a fact from namespace"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:facts {'f1 {}}}})}
    (rt/remove-fact 'my.ns 'f1)
    (rt/get-fact 'my.ns 'f1))
  => nil)

^{:refer code.test.base.runtime/teardown-fact :added "4.0"}
(fact "runs the teardown hook"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:facts {'f1 {:function {:teardown (fn [] 6)}}}}})}
    (rt/teardown-fact 'my.ns 'f1))
  => 6)

^{:refer code.test.base.runtime/setup-fact :added "4.0"}
(fact "runs the setup hook"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:facts {'f1 {:function {:setup (fn [] 6)}}}}})}
    (rt/setup-fact 'my.ns 'f1))
  => 6)

^{:refer code.test.base.runtime/exec-thunk :added "4.0"}
(fact "executes the fact thunk (only the check"
  ^:hidden
  
  (rt/exec-thunk {:function {:thunk (fn [] 1)}})
  => 1)

^{:refer code.test.base.runtime/exec-slim :added "4.0"}
(fact "executes the fact slim (only the body"
  ^:hidden
  
  (rt/exec-slim {:function {:slim (fn [] 1)}})
  => 1)

^{:refer code.test.base.runtime/no-dots :added "4.0"}
(fact "removes dots and slash from the string"
  ^:hidden
  
  (rt/no-dots "a.b/c")
  => "a_b__c")

^{:refer code.test.base.runtime/fact-id :added "4.0"}
(fact "returns an id from fact data"
  ^:hidden
  
  (rt/fact-id {:refer 'code.test.base.runtime/fact-id})
  => 'test-code_test_base_runtime__fact_id)

^{:refer code.test.base.runtime/find-fact :added "4.0"}
(fact "the fact that is associated with a given line"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:facts {'f1 {:id 'f1 :line 10 :type :core}
                                                         'f2 {:id 'f2 :line 20 :type :core}}}})}
    (:id (rt/find-fact 'my.ns {:line 15})))
  => 'f1)

^{:refer code.test.base.runtime/run-op :added "4.0"}
(fact "common runtime functions for easy access"
  ^:hidden
  
  (rt/with-new-context {:registry (atom {'my.ns {:facts {'f1 {:id 'f1 :line 10 :type :core}}
                                                 :flags {'f1 {:setup true}}}})}
    (with-redefs [h/ns-sym (constantly 'my.ns)]
      (rt/run-op {:line 10} :setup?)))
  => true)
