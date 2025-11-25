(ns code.test.base.runtime-test
  (:use code.test)
  (:require [code.test.base.runtime :as rt]
            [code.test.base.context :as ctx]
            [std.lib :as h]))

(fact "Run tests in a temporary context"

  (ctx/with-context {:registry (atom {})}
    (rt/set-global {:a 1})
    (rt/get-global))
  => {:a 1})

(fact "Demonstrate that temporary context does not affect the global context"

  (rt/get-global)
  => nil)

(fact "Test purge-all within a context"

  (ctx/with-context {:registry (atom {})}
    (rt/set-global 'code.test.base.runtime-test {:a 1})
    (rt/purge-all 'code.test.base.runtime-test)
    (rt/get-global 'code.test.base.runtime-test))
  => nil)

(fact "Test get-global and set-global within a context"

  (ctx/with-context {:registry (atom {})}
    (rt/set-global 'code.test.base.runtime-test {:b 2})
    (rt/get-global 'code.test.base.runtime-test))
  => {:b 2})

(fact "Test update-global within a context"

  (ctx/with-context {:registry (atom {})}
    (rt/set-global 'code.test.base.runtime-test {:c 3})
    (rt/update-global 'code.test.base.runtime-test #(assoc % :d 4))
    (rt/get-global 'code.test.base.runtime-test))
  => {:c 3 :d 4})

(fact "Test link functions within a context"

  (ctx/with-context {:registry (atom {})}
    (rt/add-link 'code.test.base.runtime-test 'my-link)
    (rt/list-links 'code.test.base.runtime-test)
    => #{'my-link}
    (rt/remove-link 'code.test.base.runtime-test 'my-link)
    (rt/list-links 'code.test.base.runtime-test))
  => #{})

(fact "Test fact functions within a context"

  (ctx/with-context {:registry (atom {})}
    (def fact-id (rt/fact-id {:refer 'my-fact}))
    (rt/set-fact fact-id {:data "test-fact"})
    (rt/get-fact fact-id :data)
    => "test-fact"
    (rt/remove-fact fact-id)
    (rt/get-fact fact-id))
  => nil)
