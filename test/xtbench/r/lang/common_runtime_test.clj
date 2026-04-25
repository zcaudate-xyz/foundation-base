(ns xtbench.r.lang.common-runtime-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.common-runtime :as rt]))

(l/script- :r
  {:runtime :basic
   :require [[xt.lang.common-runtime :as rt]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-runtime/xt-exists? :added "4.0"
  :setup [(l/rt:restart)]}
(fact "checks that the xt map exists"

  (!.r
   [(rt/xt-exists?)
    (rt/xt-ensure)
    (rt/xt-exists?)])
  => (contains-in [false {"config" {}, "spaces" {}, "::" "xt"} true]))

^{:refer xt.lang.common-runtime/xt-create :added "4.0"}
(fact "creates an empty xt structure"

  (!.r
   [(do (rt/xt-purge)
        (rt/xt-create))
    (rt/xt-current)])
  => (contains-in [{"config" {}, "spaces" {}, "::" "xt"}
                   nil]))

^{:refer xt.lang.common-runtime/xt-ensure :added "4.1"}
(fact "makes sure the xt state is alive"

  (!.r
   (rt/xt-purge)
   (rt/xt-ensure))
  => (contains-in {"config" {}, "spaces" {}, "::" "xt"}))

^{:refer xt.lang.common-runtime/xt-current :added "4.0"}
(fact "gets the current xt"

  (!.r
   (rt/xt-purge)
   (rt/xt-current))
  => nil)

^{:refer xt.lang.common-runtime/xt-purge :added "4.0"}
(fact "empties the current xt"

  (!.r
   (rt/xt-ensure)
   [(rt/xt-purge)
    (or (rt/xt-current) "NA")])
  => (contains-in [{"config" {}, "spaces" {}, "::" "xt"}
                    "NA"]))

^{:refer xt.lang.common-runtime/xt-purge-config :added "4.0"}
(fact "clears all `:config` entries"

  (!.r
   (rt/xt-purge-config)
   (rt/xt-config-set "test.module" {:host "127.0.0.1"})
   [(rt/xt-config-list)
    (rt/xt-purge-config)
    (rt/xt-config-list)])
  => (contains [["test.module"]
                [true {"test.module" {"host" "127.0.0.1"}}]
                empty?]))

^{:refer xt.lang.common-runtime/xt-purge-spaces :added "4.0"}
(fact "clears all `:spaces` entries"

  (!.r
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1})
   [(rt/xt-space-list)
    (rt/xt-purge-spaces)
    (rt/xt-space-list)])
  => (contains [["test.module"]
                [true {"test.module" {"hello" {"value" {"a" 1}, "watch" {}}}}]
                empty?]))

^{:refer xt.lang.common-runtime/xt-lookup-id :added "4.0"}
(fact "gets the runtime id for pointer-like objects"

  (!.r
   (rt/xt-lookup-id {}))
  => integer?)

^{:refer xt.lang.common-runtime/xt-config-list :added "4.0"}
(fact "lists all config entries in the xt"

  (!.r
    (rt/xt-purge-config)
    (rt/xt-config-set "test.one" 1)
    (rt/xt-config-set "test.two" 2)
    (rt/xt-config-list))
  => (just ["test.one" "test.two"] :in-any-order))

^{:refer xt.lang.common-runtime/xt-config-set :added "4.0"}
(fact "sets the config for a module"

  (!.r
   (rt/xt-purge-config)
   [(rt/xt-config-set "test.module" {:host "127.0.0.1"
                                     :port 1234})
    (rt/xt-config-list)])
  => (contains-in [[true] ["test.module"]]))

^{:refer xt.lang.common-runtime/xt-config-del :added "4.0"}
(fact "deletes a single xt config entry"

  (!.r
   (rt/xt-purge-config)
   [(rt/xt-config-set "test.module" {:host "127.0.0.1"
                                     :port 1234})
    (rt/xt-config-del "test.module")
    (or (rt/xt-config "test.module") "NA")])
  => (contains-in
      [[true]
       [true {"host" "127.0.0.1", "port" 1234}]
       "NA"]))

^{:refer xt.lang.common-runtime/xt-config :added "4.0"}
(fact "gets a config entry"

  (!.r
   (rt/xt-purge-config)
   (rt/xt-config-set "test.module" {:host "127.0.0.1"
                                    :port 1234})
   (rt/xt-config "test.module"))
  => {"host" "127.0.0.1", "port" 1234})

^{:refer xt.lang.common-runtime/xt-space-list :added "4.0"}
(fact "lists all spaces in the xt"

  (!.r
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1 :b 2})
   (rt/xt-space-list))
  => ["test.module"])

^{:refer xt.lang.common-runtime/xt-space-del :added "4.0"}
(fact "deletes a space"

  (!.r
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1})
   [(rt/xt-space-del "test.module")
    (rt/xt-space-list)])
  => (contains-in [[true {"hello" {"value" {"a" 1}, "watch" {}}}]
                   empty?]))

^{:refer xt.lang.common-runtime/xt-space :added "4.0"}
(fact "gets a space"

  (!.r
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1 :b 2})
   (rt/xt-space "test.module"))
  => {"hello" {"value" {"a" 1, "b" 2}, "watch" {}}})

^{:refer xt.lang.common-runtime/xt-space-clear :added "4.0"}
(fact "clears all items in the space"

  (!.r
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" 42)
   [(rt/xt-space-clear "test.module")
    (rt/xt-space "test.module")])
  => [[true {"hello" {"value" 42, "watch" {}}}]
      {}])

^{:refer xt.lang.common-runtime/xt-item-del :added "4.0"}
(fact "deletes a single item in the space"

  (!.r
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" 42)
   [(rt/xt-item-del "test.module" "hello")
    (x:nil? (rt/xt-item "test.module" "hello"))])
  => [[true {"value" 42, "watch" {}}]
      true])

^{:refer xt.lang.common-runtime/xt-item-trigger :added "4.0"}
(fact "triggers as item"

  (!.r
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" 42)
   (rt/xt-add-watch "test.module/hello"
                    "main"
                    (fn [v s]
                      (return v)))
   (rt/xt-item-trigger "test.module" "hello"))
  => ["main"])

^{:refer xt.lang.common-runtime/xt-item-set :added "4.0"}
(fact "sets a single item in the space"

  (!.r
   (rt/xt-purge-spaces)
   [(rt/xt-item-set "test.module" "hello" 42)
    (rt/xt-item "test.module" "hello")])
  => [[true {"value" 42, "watch" {}}]
      42])

^{:refer xt.lang.common-runtime/xt-item :added "4.0"}
(fact "gets an xt item by module and key"

  (!.r
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1})
   (rt/xt-item "test.module" "hello"))
  => {"a" 1})

^{:refer xt.lang.common-runtime/xt-item-get :added "4.0"}
(fact "gets an xt item or sets a default if not exist"

  (!.r
   (rt/xt-purge-spaces)
   [(rt/xt-item-get "test.module" "hello" (fn [] (return 1)))
    (rt/xt-item-get "test.module" "hello" (fn [] (return 2)))])
  => [1 1])

^{:refer xt.lang.common-runtime/xt-var-entry :added "4.0"}
(fact "gets the var entry"

  (!.r
   (rt/xt-purge-spaces)
   (rt/xt-var-set "test.module/hello" 42)
   (rt/xt-var-entry "test.module/hello"))
  => {"value" 42, "watch" {}})

^{:refer xt.lang.common-runtime/xt-var :added "4.0"}
(fact "gets an xt item"

  (!.r
   (rt/xt-purge-spaces)
   (rt/xt-var-set "test.module/hello" {:a 1})
   (rt/xt-var "test.module/hello"))
  => {"a" 1})

^{:refer xt.lang.common-runtime/xt-var-set :added "4.0"}
(fact "sets the var"

  (!.r
   (rt/xt-purge-spaces)
   [(rt/xt-var-set "test.module/hello" 42)
    (rt/xt-var-set "test.module/hello" nil)
    (x:nil? (rt/xt-var "test.module/hello"))])
  => [[true {"value" 42, "watch" {}}]
      [true {"value" 42, "watch" {}}]
      true])

^{:refer xt.lang.common-runtime/xt-var-trigger :added "4.0"}
(fact "triggers the var"

  (!.r
   (rt/xt-purge-spaces)
   (rt/xt-var-set "test.module/hello" 42)
   (rt/xt-add-watch "test.module/hello"
                    "main"
                    (fn [v s]
                      (return v)))
   (rt/xt-var-trigger "test.module/hello"))
  => ["main"])

^{:refer xt.lang.common-runtime/xt-add-watch :added "4.0"}
(fact "adds a watch"

  (!.r
   (rt/xt-purge-spaces)
   (rt/xt-var-set "test.module/hello" 1)
   [(rt/xt-add-watch "test.module/hello"
                     "main"
                     (fn [v s]
                       (return v)))
    (rt/xt-var-trigger "test.module/hello")])
  => [true
      ["main"]])

^{:refer xt.lang.common-runtime/xt-remove-watch :added "4.0"}
(fact "removes a watch"

  (!.r
   (rt/xt-purge-spaces)
   (rt/xt-var-set "test.module/hello" 1)
   (rt/xt-add-watch "test.module/hello"
                    "main"
                    (fn [v s]
                      (return v)))
   [(rt/xt-remove-watch "test.module/hello" "main")
    (rt/xt-var-trigger "test.module/hello")])
  => (contains [true empty?]))

(comment
  (s/seedgen-langadd 'xt.lang.common-runtime {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-runtime {:lang [:lua :python] :write true}))
