(ns code.test.base.runtime-test
  (:use code.test)
  (:require [code.test.base.runtime :as rt]
            [std.lib :as h]))

(fact "Run tests in a temporary context"

  (rt/with-new-context {}
    (rt/set-global {:a 1})
    (rt/get-global))
  => {:a 1})

(fact "Demonstrate that temporary context does not affect the global context"

  (rt/get-global)
  => nil)

(fact "Test purge-all within a context"
  
  (rt/with-new-context {:registry (atom {})}
    (rt/set-global 'code.test.base.runtime-test {:a 1})
    (rt/purge-all 'code.test.base.runtime-test)
    (rt/get-global 'code.test.base.runtime-test))
  => nil)

(fact "Test get-global and set-global within a context"

  (rt/with-new-context {:registry (atom {})}
    (rt/set-global 'code.test.base.runtime-test {:b 2})
    (rt/get-global 'code.test.base.runtime-test))
  => {:b 2})

(fact "Test update-global within a context"

  (rt/with-new-context {:registry (atom {})}
    (rt/set-global 'code.test.base.runtime-test {:c 3})
    (rt/update-global 'code.test.base.runtime-test #(assoc % :d 4))
    (rt/get-global 'code.test.base.runtime-test))
  => {:c 3 :d 4})

(fact "Test link functions within a context"

  (rt/with-new-context {:registry (atom {})}
    (rt/add-link 'code.test.base.runtime-test 'my-link)
    (rt/list-links 'code.test.base.runtime-test)
    => #{'my-link}
    (rt/remove-link 'code.test.base.runtime-test 'my-link)
    (rt/list-links 'code.test.base.runtime-test))
  => #{})

(fact "Test fact functions within a context"

  (rt/with-new-context {:registry (atom {})}
    (def fact-id (rt/fact-id {:refer 'my-fact}))
    (rt/set-fact fact-id {:data "test-fact"})
    (rt/get-fact fact-id :data)
    => "test-fact"
    (rt/remove-fact fact-id)
    (rt/get-fact fact-id))
  => nil)


^{:refer code.test.base.runtime/purge-all :added "4.0"}
(fact "purges all facts from namespace"
  (rt/with-new-context {:registry (atom {'my.ns {:facts {:a 1} :flags {:b 2}}})}
    (rt/purge-all 'my.ns)
    (rt/all-facts 'my.ns))
  => nil)

^{:refer code.test.base.runtime/get-global :added "4.0"}
(fact "gets the global settings for namespace"
  (rt/with-new-context {:registry (atom {'my.ns {:global {:a 1}}})}
    (rt/get-global 'my.ns))
  => {:a 1}

  (rt/with-new-context {:registry (atom {'my.ns {:global {:a {:b 2}}}})}
    (rt/get-global 'my.ns :a :b))
  => 2)

^{:refer code.test.base.runtime/set-global :added "4.0"}
(fact "sets the global settings for namespace"
  (rt/with-new-context {:registry (atom {})}
    (rt/set-global 'my.ns {:check {:setup '[(prn "hello")]}})
    (rt/get-global 'my.ns))
  => {:check {:setup '[(prn "hello")]}})

^{:refer code.test.base.runtime/update-global :added "4.0"}
(fact "updates global data"
  (rt/with-new-context {:registry (atom {'my.ns {:global {:a 1}}})}
    (rt/update-global 'my.ns assoc :b 2)
    (rt/get-global 'my.ns))
  => {:a 1 :b 2})

^{:refer code.test.base.runtime/list-links :added "4.0"}
(fact "lists ns links"
  (rt/with-new-context {:registry (atom {'my.ns {:links #{'other.ns}}})}
    (rt/list-links 'my.ns))
  => #{'other.ns})

^{:refer code.test.base.runtime/clear-links :added "4.0"}
(fact "clear ns links"
  (rt/with-new-context {:registry (atom {'my.ns {:links #{'other.ns}}})}
    (rt/clear-links 'my.ns)
    (rt/list-links 'my.ns))
  => nil)

^{:refer code.test.base.runtime/add-link :added "4.0"}
(fact "add ns link"
  (rt/with-new-context {:registry (atom {})}
    (rt/add-link 'my.ns 'other.ns)
    (rt/list-links 'my.ns))
  => #{'other.ns})

^{:refer code.test.base.runtime/remove-link :added "4.0"}
(fact "remove ms link"
  (rt/with-new-context {:registry (atom {'my.ns {:links #{'other.ns}}})}
    (rt/remove-link 'my.ns 'other.ns)
    (rt/list-links 'my.ns))
  => #{})

^{:refer code.test.base.runtime/all-facts :added "4.0"}
(fact "retrieves a list of all the facts in a namespace"
  (rt/with-new-context {:registry (atom {'my.ns {:facts {:f1 1}}})}
    (keys (rt/all-facts 'my.ns)))
  => '(:f1))

^{:refer code.test.base.runtime/list-facts :added "4.0"}
(fact "lists all facts in current namespace"
  (rt/with-new-context {:registry (atom {'my.ns {:facts {'test-f1 {:id 'test-f1 :line 10}
                                                         'test-f2 {:id 'test-f2 :line 20}}}})}
    (rt/list-facts 'my.ns))
  => '(test-f1 test-f2))

^{:refer code.test.base.runtime/purge-facts :added "4.0"}
(fact "purges all facts in the namespace (for reload)"
  (rt/with-new-context {:registry (atom {'my.ns {:facts {:a 1} :flags {:b 2}}})}
    (rt/purge-facts 'my.ns)
    (rt/list-facts 'my.ns))
  => [])

^{:refer code.test.base.runtime/parse-args :added "4.0"}
(fact "helper function for variable args"
  (rt/parse-args 'my.ns nil :arg [:more])
  => ['my.ns nil '(:arg :more)])

^{:refer code.test.base.runtime/get-fact :added "4.0"}
(fact "gets a fact"
  (rt/with-new-context {:registry (atom {'my.ns {:facts {'f1 {:data "hello"}}}})}
    (rt/get-fact 'my.ns 'f1 :data))
  => "hello")

^{:refer code.test.base.runtime/set-fact :added "4.0"}
(fact "sets the entire data on a fact"
  (rt/with-new-context {:registry (atom {})}
    (rt/set-fact 'my.ns 'f1 {:data "hello"})
    (rt/get-fact 'my.ns 'f1))
  => {:data "hello"})

^{:refer code.test.base.runtime/set-in-fact :added "4.0"}
(fact "sets the property on a fact"
  (rt/with-new-context {:registry (atom {})}
    (rt/set-in-fact 'my.ns 'f1 [:data] "hello")
    (rt/get-fact 'my.ns 'f1 :data))
  => "hello")

^{:refer code.test.base.runtime/get-flag :added "4.0"}
(fact "checks if the setup flag has been set"
  (rt/with-new-context {:registry (atom {'my.ns {:flags {'f1 {:setup true}}}})}
    (rt/get-flag 'my.ns 'f1 :setup))
  => true)

^{:refer code.test.base.runtime/set-flag :added "4.0"}
(fact "sets the setup flag"
  (rt/with-new-context {:registry (atom {})}
    (rt/set-flag 'my.ns 'f1 :setup true)
    (rt/get-flag 'my.ns 'f1 :setup))
  => true)

^{:refer code.test.base.runtime/update-fact :added "4.0"}
(fact "updates a fact given a function"
  (rt/with-new-context {:registry (atom {'my.ns {:facts {'f1 {:count 1}}}})}
    (rt/update-fact 'my.ns 'f1 update-in [:count] inc)
    (rt/get-fact 'my.ns 'f1 :count))
  => 2)

^{:refer code.test.base.runtime/remove-fact :added "4.0"}
(fact "removes a fact from namespace"
  (rt/with-new-context {:registry (atom {'my.ns {:facts {'f1 {}}}})}
    (rt/remove-fact 'my.ns 'f1)
    (rt/get-fact 'my.ns 'f1))
  => nil)

^{:refer code.test.base.runtime/teardown-fact :added "4.0"}
(fact "runs the teardown hook"
  (rt/with-new-context {:registry (atom {'my.ns {:facts {'f1 {:function {:teardown (fn [] 6)}}}}})}
    (rt/teardown-fact 'my.ns 'f1))
  => 6)

^{:refer code.test.base.runtime/setup-fact :added "4.0"}
(fact "runs the setup hook"
  (rt/with-new-context {:registry (atom {'my.ns {:facts {'f1 {:function {:setup (fn [] 6)}}}}})}
    (rt/setup-fact 'my.ns 'f1))
  => 6)

^{:refer code.test.base.runtime/exec-thunk :added "4.0"}
(fact "executes the fact thunk (only the check"
  (rt/exec-thunk {:function {:thunk (fn [] 1)}})
  => 1)

^{:refer code.test.base.runtime/exec-slim :added "4.0"}
(fact "executes the fact slim (only the body"
  (rt/exec-slim {:function {:slim (fn [] 1)}})
  => 1)

^{:refer code.test.base.runtime/no-dots :added "4.0"}
(fact "removes dots and slash from the string"
  (rt/no-dots "a.b/c")
  => "a_b__c")

^{:refer code.test.base.runtime/fact-id :added "4.0"}
(fact "returns an id from fact data"
  (rt/fact-id {:refer 'code.test.base.runtime/fact-id})
  => 'test-code_test_base_runtime__fact_id)

^{:refer code.test.base.runtime/find-fact :added "4.0"}
(fact "the fact that is associated with a given line"
  (rt/with-new-context {:registry (atom {'my.ns {:facts {'f1 {:id 'f1 :line 10 :type :core}
                                                         'f2 {:id 'f2 :line 20 :type :core}}}})}
    (:id (rt/find-fact 'my.ns {:line 15})))
  => 'f1)

^{:refer code.test.base.runtime/run-op :added "4.0"}
(fact "common runtime functions for easy access"
  (rt/with-new-context {:registry (atom {'my.ns {:facts {'f1 {:id 'f1 :line 10 :type :core}}
                                                 :flags {'f1 {:setup true}}}})}
    (with-redefs [h/ns-sym (constantly 'my.ns)]
      (rt/run-op {:line 10} :setup?)))
  => true)
