(ns xt.lang.common-trace-test
  (:use code.test)
  (:require [hara.lang             :as l]
            [xt.lang.common-trace :as trace]))

^{:seedgen/root {:all true, :langs [:js lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-trace :as trace]
             [xt.lang.spec-base :as xt]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-trace :as trace]
             [xt.lang.spec-base :as xt]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-trace :as trace]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-trace/meta:info-fn :added "4.1"}
(fact "returns default metadata fields and merges overrides"

  (let [m (trace/meta:info-fn {:sample true})]
    [(:sample m)
     (string? (:meta/fn m))
     (nil? (:meta/line m))])
  => [true true true])

^{:refer xt.lang.common-trace/meta:info :added "4.1"}
(fact "injects metadata into emitted js values"

  (!.js
    (var info (trace/meta:info {:sample true}))
    [(xt/x:get-key info "sample")
     (xt/x:not-nil? (xt/x:get-key info "meta/fn"))
     (xt/x:not-nil? (xt/x:get-key info "meta/line"))])
  => [true true true]

  (!.lua
    (var info (trace/meta:info {:sample true}))
    [(xt/x:get-key info "sample")
     (xt/x:not-nil? (xt/x:get-key info "meta/fn"))
     (xt/x:not-nil? (xt/x:get-key info "meta/line"))])
  => [true true true]

  (!.py
    (var info (trace/meta:info {:sample true}))
    [(xt/x:get-key info "sample")
     (xt/x:not-nil? (xt/x:get-key info "meta/fn"))
     (xt/x:not-nil? (xt/x:get-key info "meta/line"))])
  => [true true true])

^{:refer xt.lang.common-trace/LOG! :added "4.1"}
(fact "prints and returns nil in js"

  (!.js
    (xt/x:nil? (trace/LOG! "hello")))
  => true

  (!.lua
    (xt/x:nil? (trace/LOG! "hello")))
  => true

  (!.py
    (xt/x:nil? (trace/LOG! "hello")))
  => true)

^{:refer xt.lang.common-trace/trace-log :added "4.1"}
(fact "returns the current trace log"

  ^{:seedgen/base {:lua {:expect {}}}}
  (!.js
    (trace/trace-log-clear)
    (trace/trace-log))
  => []

  (!.lua
    (trace/trace-log-clear)
    (trace/trace-log))
  => {}

  (!.py
    (trace/trace-log-clear)
    (trace/trace-log))
  => [])

^{:refer xt.lang.common-trace/trace-log-clear :added "4.1"}
(fact "clears the trace log"

  ^{:seedgen/base {:lua {:expect {}}}}
  (!.js
    (trace/trace-log-add "a" "one" {})
    (trace/trace-log-add "b" "two" {})
    (trace/trace-log-clear))
  => []

  (!.lua
    (trace/trace-log-add "a" "one" {})
    (trace/trace-log-add "b" "two" {})
    (trace/trace-log-clear))
  => {}

  (!.py
    (trace/trace-log-add "a" "one" {})
    (trace/trace-log-add "b" "two" {})
    (trace/trace-log-clear))
  => [])

^{:refer xt.lang.common-trace/trace-log-add :added "4.1"}
(fact "adds a trace entry and merges options"

  (!.js
    (trace/trace-log-clear)
    (var n (trace/trace-log-add {"a" 1} "alpha" {"extra" 2}))
    (var entry (trace/trace-last-entry nil))
    [n
     (xt/x:get-key entry "tag")
     (xt/x:get-key (xt/x:get-key entry "data") "a")
     (xt/x:get-key entry "extra")])
  => [1 "alpha" 1 2]

  (!.lua
    (trace/trace-log-clear)
    (var n (trace/trace-log-add {"a" 1} "alpha" {"extra" 2}))
    (var entry (trace/trace-last-entry nil))
    [n
     (xt/x:get-key entry "tag")
     (xt/x:get-key (xt/x:get-key entry "data") "a")
     (xt/x:get-key entry "extra")])
  => [1 "alpha" 1 2]

  (!.py
    (trace/trace-log-clear)
    (var n (trace/trace-log-add {"a" 1} "alpha" {"extra" 2}))
    (var entry (trace/trace-last-entry nil))
    [n
     (xt/x:get-key entry "tag")
     (xt/x:get-key (xt/x:get-key entry "data") "a")
     (xt/x:get-key entry "extra")])
  => [1 "alpha" 1 2])

^{:refer xt.lang.common-trace/trace-filter :added "4.1"}
(fact "filters trace entries by tag"

  (!.js
    (trace/trace-log-clear)
    (trace/trace-log-add 1 "alpha" {})
    (trace/trace-log-add 2 "beta" {})
    (trace/trace-log-add 3 "alpha" {})
    (var tagged (trace/trace-filter "alpha"))
    [(xt/x:len tagged)
     (xt/x:get-key (xt/x:first tagged) "data")
     (xt/x:get-key (xt/x:last tagged) "data")])
  => [2 1 3]

  (!.lua
    (trace/trace-log-clear)
    (trace/trace-log-add 1 "alpha" {})
    (trace/trace-log-add 2 "beta" {})
    (trace/trace-log-add 3 "alpha" {})
    (var tagged (trace/trace-filter "alpha"))
    [(xt/x:len tagged)
     (xt/x:get-key (xt/x:first tagged) "data")
     (xt/x:get-key (xt/x:last tagged) "data")])
  => [2 1 3]

  (!.py
    (trace/trace-log-clear)
    (trace/trace-log-add 1 "alpha" {})
    (trace/trace-log-add 2 "beta" {})
    (trace/trace-log-add 3 "alpha" {})
    (var tagged (trace/trace-filter "alpha"))
    [(xt/x:len tagged)
     (xt/x:get-key (xt/x:first tagged) "data")
     (xt/x:get-key (xt/x:last tagged) "data")])
  => [2 1 3])

^{:refer xt.lang.common-trace/trace-last-entry :added "4.1"}
(fact "returns the last entry overall or for a tag"

  (!.js
    (trace/trace-log-clear)
    (trace/trace-log-add 1 "alpha" {})
    (trace/trace-log-add 2 "beta" {})
    (trace/trace-log-add 3 "alpha" {})
    (var any-entry (trace/trace-last-entry nil))
    (var beta-entry (trace/trace-last-entry "beta"))
    [(xt/x:get-key any-entry "data")
     (xt/x:get-key beta-entry "data")])
  => [3 2]

  (!.lua
    (trace/trace-log-clear)
    (trace/trace-log-add 1 "alpha" {})
    (trace/trace-log-add 2 "beta" {})
    (trace/trace-log-add 3 "alpha" {})
    (var any-entry (trace/trace-last-entry nil))
    (var beta-entry (trace/trace-last-entry "beta"))
    [(xt/x:get-key any-entry "data")
     (xt/x:get-key beta-entry "data")])
  => [3 2]

  (!.py
    (trace/trace-log-clear)
    (trace/trace-log-add 1 "alpha" {})
    (trace/trace-log-add 2 "beta" {})
    (trace/trace-log-add 3 "alpha" {})
    (var any-entry (trace/trace-last-entry nil))
    (var beta-entry (trace/trace-last-entry "beta"))
    [(xt/x:get-key any-entry "data")
     (xt/x:get-key beta-entry "data")])
  => [3 2])

^{:refer xt.lang.common-trace/trace-data :added "4.1"}
(fact "returns the logged data values in order"

  (!.js
    (trace/trace-log-clear)
    (trace/trace-log-add 1 "alpha" {})
    (trace/trace-log-add 2 "beta" {})
    (trace/trace-data nil))
  => [1 2]

  (!.lua
    (trace/trace-log-clear)
    (trace/trace-log-add 1 "alpha" {})
    (trace/trace-log-add 2 "beta" {})
    (trace/trace-data nil))
  => [1 2]

  (!.py
    (trace/trace-log-clear)
    (trace/trace-log-add 1 "alpha" {})
    (trace/trace-log-add 2 "beta" {})
    (trace/trace-data nil))
  => [1 2])

^{:refer xt.lang.common-trace/trace-last :added "4.1"}
(fact "returns the last logged data overall or for a tag"

  (!.js
    (trace/trace-log-clear)
    (trace/trace-log-add 1 "alpha" {})
    (trace/trace-log-add 2 "beta" {})
    [(trace/trace-last nil)
     (trace/trace-last "alpha")])
  => [2 1]

  (!.lua
    (trace/trace-log-clear)
    (trace/trace-log-add 1 "alpha" {})
    (trace/trace-log-add 2 "beta" {})
    [(trace/trace-last nil)
     (trace/trace-last "alpha")])
  => [2 1]

  (!.py
    (trace/trace-log-clear)
    (trace/trace-log-add 1 "alpha" {})
    (trace/trace-log-add 2 "beta" {})
    [(trace/trace-last nil)
     (trace/trace-last "alpha")])
  => [2 1])

^{:refer xt.lang.common-trace/TRACE! :added "4.1"}
(fact "records trace entries with metadata"

  (!.js
    (trace/trace-log-clear)
    (var count (trace/TRACE! "hello" "alpha"))
    (var entry (trace/trace-last-entry "alpha"))
    [count
     (xt/x:get-key entry "data")
     (xt/x:get-key entry "tag")
     (xt/x:not-nil? (xt/x:get-key entry "ns"))
     (xt/x:not-nil? (xt/x:get-key entry "line"))
     (xt/x:not-nil? (xt/x:get-key entry "column"))])
  => [1 "hello" "alpha" true true true]

  (!.lua
    (trace/trace-log-clear)
    (var count (trace/TRACE! "hello" "alpha"))
    (var entry (trace/trace-last-entry "alpha"))
    [count
     (xt/x:get-key entry "data")
     (xt/x:get-key entry "tag")
     (xt/x:not-nil? (xt/x:get-key entry "ns"))
     (xt/x:not-nil? (xt/x:get-key entry "line"))
     (xt/x:not-nil? (xt/x:get-key entry "column"))])
  => [1 "hello" "alpha" true true true]

  (!.py
    (trace/trace-log-clear)
    (var count (trace/TRACE! "hello" "alpha"))
    (var entry (trace/trace-last-entry "alpha"))
    [count
     (xt/x:get-key entry "data")
     (xt/x:get-key entry "tag")
     (xt/x:not-nil? (xt/x:get-key entry "ns"))
     (xt/x:not-nil? (xt/x:get-key entry "line"))
     (xt/x:not-nil? (xt/x:get-key entry "column"))])
  => [1 "hello" "alpha" true true true])

^{:refer xt.lang.common-trace/trace-run :added "4.1"}
(fact "runs a function after clearing the trace log"

  (!.js
    (trace/trace-log-add "stale" "old" {})
    (var out
         (trace/trace-run
          (fn []
            (trace/trace-log-add "fresh-1" "a" {})
            (trace/trace-log-add "fresh-2" "b" {}))))
    [(xt/x:len out)
     (xt/x:get-key (xt/x:first out) "data")
     (xt/x:get-key (xt/x:last out) "data")])
  => [2 "fresh-1" "fresh-2"]

  (!.lua
    (trace/trace-log-add "stale" "old" {})
    (var out
         (trace/trace-run
          (fn []
            (trace/trace-log-add "fresh-1" "a" {})
            (trace/trace-log-add "fresh-2" "b" {}))))
    [(xt/x:len out)
     (xt/x:get-key (xt/x:first out) "data")
     (xt/x:get-key (xt/x:last out) "data")])
  => [2 "fresh-1" "fresh-2"]

  (!.py
    (trace/trace-log-add "stale" "old" {})
    (var out
         (trace/trace-run
          (fn []
            (trace/trace-log-add "fresh-1" "a" {})
            (trace/trace-log-add "fresh-2" "b" {}))))
    [(xt/x:len out)
     (xt/x:get-key (xt/x:first out) "data")
     (xt/x:get-key (xt/x:last out) "data")])
  => [2 "fresh-1" "fresh-2"])

^{:refer xt.lang.common-trace/RUN! :added "4.1"}
(fact "emits a trace-run wrapper around traced forms")

(comment
  (s/snapto '[xt.lang.common-trace])
  
  (s/seedgen-langadd '[xt.lang.common-trace] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.lang.common-trace] {:lang [:lua :python] :write true}))
