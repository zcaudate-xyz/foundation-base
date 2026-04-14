(ns xt.lang.common-runtime-test
  (:use code.test)
  (:require [std.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-runtime :as rt]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-runtime :as rt]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-runtime :as rt]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-runtime/xt-exists? :added "4.0"
  :setup [(l/rt:restart)]}
(fact "checks that the xt map exists"
  ^:hidden
  
  (!.js
   [(rt/xt-exists?)
    (rt/xt-ensure)
    (rt/xt-exists?)])
  => (contains-in [false {"config" {}, "spaces" {}, "::" "xt"} true])

  (!.lua
   [(rt/xt-exists?)
    (rt/xt-ensure)
    (rt/xt-exists?)])
  => (contains-in [false {"config" {}, "spaces" {}, "::" "xt"} true])

  (!.py
   [(rt/xt-exists?)
    (rt/xt-ensure)
    (rt/xt-exists?)])
  => (contains-in [false {"config" {}, "spaces" {}, "::" "xt"} true]))

^{:refer xt.lang.common-runtime/xt-create :added "4.0"}
(fact "creates an empty xt structure"
  ^:hidden
  
  (!.js
   [(do (rt/xt-purge)
        (rt/xt-create))
    (rt/xt-current)])
  => (contains-in [{"config" {}, "spaces" {}, "::" "xt"}
                   nil])

  (!.lua
   [(do (rt/xt-purge)
        (rt/xt-create))
    (rt/xt-current)])
  => (contains-in [{"config" {}, "spaces" {}, "::" "xt"}
                   nil])

  (!.py
   [(do (rt/xt-purge)
        (rt/xt-create))
    (rt/xt-current)])
  => (contains-in [{"config" {}, "spaces" {}, "::" "xt"}
                   nil]))

^{:refer xt.lang.common-runtime/xt :added "4.0"}
(fact "gets the current xt or creates a new one"
  ^:hidden
  
  (!.js
   (rt/xt-purge)
   [(rt/xt-current)
    (rt/xt-ensure)
    (rt/xt-current)])
  => (contains-in [nil
                   {"config" {}, "spaces" {}, "::" "xt"}
                   {"config" {}, "spaces" {}, "::" "xt"}])

  (!.lua
   (rt/xt-purge)
   [(rt/xt-current)
    (rt/xt-ensure)
    (rt/xt-current)])
  => (contains-in [nil
                   {"config" {}, "spaces" {}, "::" "xt"}
                   {"config" {}, "spaces" {}, "::" "xt"}])

  (!.py
   (rt/xt-purge)
   [(rt/xt-current)
    (rt/xt-ensure)
    (rt/xt-current)])
  => (contains-in [nil
                   {"config" {}, "spaces" {}, "::" "xt"}
                   {"config" {}, "spaces" {}, "::" "xt"}]))

^{:refer xt.lang.common-runtime/xt-current :added "4.0"}
(fact "gets the current xt"
  ^:hidden
  
  (!.js
   (rt/xt-purge)
   (rt/xt-current))
  => nil
  
  (!.lua
   (rt/xt-purge)
   (rt/xt-current))
  => nil

  (!.py
   (rt/xt-purge)
   (rt/xt-current))
  => nil)

^{:refer xt.lang.common-runtime/xt-purge :added "4.0"}
(fact "empties the current xt"
  ^:hidden
  
  (!.js
   (rt/xt-ensure)
   [(rt/xt-purge)
    (rt/xt-current)])
  => (contains-in [{"config" {}, "spaces" {}, "::" "xt"}
                   nil])

  (!.lua
   (rt/xt-ensure)
   [(rt/xt-purge)
    (rt/xt-current)])
  => (contains-in [{"config" {}, "spaces" {}, "::" "xt"}
                   nil])

  (!.py
   (rt/xt-ensure)
   [(rt/xt-purge)
    (rt/xt-current)])
  => (contains-in [{"config" {}, "spaces" {}, "::" "xt"}
                   nil]))

^{:refer xt.lang.common-runtime/xt-purge-config :added "4.0"}
(fact "clears all `:config` entries"
  ^:hidden
  
  (!.js
   (rt/xt-purge-config)
   (rt/xt-config-set "test.module" {:host "127.0.0.1"})
   [(rt/xt-config-list)
    (rt/xt-purge-config)
    (rt/xt-config-list)])
  => [["test.module"]
      [true {"test.module" {"host" "127.0.0.1"}}]
      []]

  (!.lua
   (rt/xt-purge-config)
   (rt/xt-config-set "test.module" {:host "127.0.0.1"})
   [(rt/xt-config-list)
    (rt/xt-purge-config)
    (rt/xt-config-list)])
  => [["test.module"]
      [true {"test.module" {"host" "127.0.0.1"}}]
      []]

  (!.py
   (rt/xt-purge-config)
   (rt/xt-config-set "test.module" {:host "127.0.0.1"})
   [(rt/xt-config-list)
    (rt/xt-purge-config)
    (rt/xt-config-list)])
  => [["test.module"]
      [true {"test.module" {"host" "127.0.0.1"}}]
      []])

^{:refer xt.lang.common-runtime/xt-purge-spaces :added "4.0"}
(fact "clears all `:spaces` entries"
  ^:hidden
  
  (!.js
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1})
   [(rt/xt-space-list)
    (rt/xt-purge-spaces)
    (rt/xt-space-list)])
  => [["test.module"]
      [true {"test.module" {"hello" {"value" {"a" 1}, "watch" {}}}}]
      []]

  (!.lua
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1})
   [(rt/xt-space-list)
    (rt/xt-purge-spaces)
    (rt/xt-space-list)])
  => [["test.module"]
      [true {"test.module" {"hello" {"value" {"a" 1}, "watch" {}}}}]
      []]

  (!.py
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1})
   [(rt/xt-space-list)
    (rt/xt-purge-spaces)
    (rt/xt-space-list)])
  => [["test.module"]
      [true {"test.module" {"hello" {"value" {"a" 1}, "watch" {}}}}]
      []])

^{:refer xt.lang.common-runtime/xt-lookup-id :added "4.0"}
(fact "gets the runtime id for pointer-like objects"
  ^:hidden
  
  (!.js
   (rt/xt-lookup-id {}))
  => integer?
  
  (!.lua
   (rt/xt-lookup-id {}))
  => integer?

  (!.py
   (rt/xt-lookup-id {}))
  => integer?)

^{:refer xt.lang.common-runtime/xt-config-list :added "4.0"}
(fact "lists all config entries in the xt"
  ^:hidden
  
  (set
   (!.js
    (rt/xt-purge-config)
    (rt/xt-config-set "test.one" 1)
    (rt/xt-config-set "test.two" 2)
    (rt/xt-config-list)))
  => #{"test.one" "test.two"}

  (set
   (!.lua
    (rt/xt-purge-config)
    (rt/xt-config-set "test.one" 1)
    (rt/xt-config-set "test.two" 2)
    (rt/xt-config-list)))
  => #{"test.one" "test.two"}

  (set
   (!.py
    (rt/xt-purge-config)
    (rt/xt-config-set "test.one" 1)
    (rt/xt-config-set "test.two" 2)
    (rt/xt-config-list)))
  => #{"test.one" "test.two"})

^{:refer xt.lang.common-runtime/xt-config-set :added "4.0"}
(fact "sets the config for a module"
  ^:hidden
  
  (!.js
   (rt/xt-purge-config)
   [(rt/xt-config-set "test.module" {:host "127.0.0.1"
                                     :port 1234})
    (rt/xt-config-list)])
  => [[true nil] ["test.module"]]
  
  (!.lua
   (rt/xt-purge-config)
   [(rt/xt-config-set "test.module" {:host "127.0.0.1"
                                     :port 1234})
    (rt/xt-config-list)])
  => [[true] ["test.module"]]

  (!.py
   (rt/xt-purge-config)
   [(rt/xt-config-set "test.module" {:host "127.0.0.1"
                                     :port 1234})
    (rt/xt-config-list)])
  => [[true nil] ["test.module"]])

^{:refer xt.lang.common-runtime/xt-config-del :added "4.0"}
(fact "deletes a single xt config entry"
  ^:hidden
  
  (!.js
   (rt/xt-purge-config)
   [(rt/xt-config-set "test.module" {:host "127.0.0.1"
                                     :port 1234})
    (rt/xt-config-del "test.module")
    (rt/xt-config "test.module")])
  => [[true nil]
      [true {"host" "127.0.0.1", "port" 1234}]
      nil]

  (!.lua
   (rt/xt-purge-config)
   [(rt/xt-config-set "test.module" {:host "127.0.0.1"
                                     :port 1234})
    (rt/xt-config-del "test.module")
    (rt/xt-config "test.module")])
  => [[true]
      [true {"host" "127.0.0.1", "port" 1234}]]

  (!.py
   (rt/xt-purge-config)
   [(rt/xt-config-set "test.module" {:host "127.0.0.1"
                                     :port 1234})
    (rt/xt-config-del "test.module")
    (rt/xt-config "test.module")])
  => [[true nil]
      [true {"host" "127.0.0.1", "port" 1234}]
      nil])

^{:refer xt.lang.common-runtime/xt-config :added "4.0"}
(fact "gets a config entry"
  ^:hidden
  
  (!.js
   (rt/xt-purge-config)
   (rt/xt-config-set "test.module" {:host "127.0.0.1"
                                    :port 1234})
   (rt/xt-config "test.module"))
  => {"host" "127.0.0.1", "port" 1234}

  (!.lua
   (rt/xt-purge-config)
   (rt/xt-config-set "test.module" {:host "127.0.0.1"
                                    :port 1234})
   (rt/xt-config "test.module"))
  => {"host" "127.0.0.1", "port" 1234}

  (!.py
   (rt/xt-purge-config)
   (rt/xt-config-set "test.module" {:host "127.0.0.1"
                                    :port 1234})
   (rt/xt-config "test.module"))
  => {"host" "127.0.0.1", "port" 1234})

^{:refer xt.lang.common-runtime/xt-space-list :added "4.0"}
(fact "lists all spaces in the xt"
  ^:hidden
  
  (!.js
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1 :b 2})
   (rt/xt-space-list))
  => ["test.module"]

  (!.lua
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1 :b 2})
   (rt/xt-space-list))
  => ["test.module"]

  (!.py
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1 :b 2})
   (rt/xt-space-list))
  => ["test.module"])

^{:refer xt.lang.common-runtime/xt-space-del :added "4.0"}
(fact "deletes a space"
  ^:hidden
  
  (!.js
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1})
   [(rt/xt-space-del "test.module")
    (rt/xt-space-list)])
  => [[true {"hello" {"value" {"a" 1}, "watch" {}}}]
      []]

  (!.lua
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1})
   [(rt/xt-space-del "test.module")
    (rt/xt-space-list)])
  => [[true {"hello" {"value" {"a" 1}, "watch" {}}}]
      []]

  (!.py
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1})
   [(rt/xt-space-del "test.module")
    (rt/xt-space-list)])
  => [[true {"hello" {"value" {"a" 1}, "watch" {}}}]
      []])

^{:refer xt.lang.common-runtime/xt-space :added "4.0"}
(fact "gets a space"
  ^:hidden
  
  (!.js
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1 :b 2})
   (rt/xt-space "test.module"))
  => {"hello" {"value" {"a" 1, "b" 2}, "watch" {}}}

  (!.lua
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1 :b 2})
   (rt/xt-space "test.module"))
  => {"hello" {"value" {"a" 1, "b" 2}, "watch" {}}}

  (!.py
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1 :b 2})
   (rt/xt-space "test.module"))
  => {"hello" {"value" {"a" 1, "b" 2}, "watch" {}}})

^{:refer xt.lang.common-runtime/xt-space-clear :added "4.0"}
(fact "clears all items in the space"
  ^:hidden
  
  (!.js
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" 42)
   [(rt/xt-space-clear "test.module")
    (rt/xt-space "test.module")])
  => [[true {"hello" {"value" 42, "watch" {}}}]
      {}]

  (!.lua
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" 42)
   [(rt/xt-space-clear "test.module")
    (rt/xt-space "test.module")])
  => [[true {"hello" {"value" 42, "watch" {}}}]
      {}]

  (!.py
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" 42)
   [(rt/xt-space-clear "test.module")
    (rt/xt-space "test.module")])
  => [[true {"hello" {"value" 42, "watch" {}}}]
      {}])

^{:refer xt.lang.common-runtime/xt-item-del :added "4.0"}
(fact "deletes a single item in the space"
  ^:hidden
  
  (!.js
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" 42)
   [(rt/xt-item-del "test.module" "hello")
    (x:nil? (rt/xt-item "test.module" "hello"))])
  => [[true {"value" 42, "watch" {}}]
      true]

  (!.lua
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" 42)
   [(rt/xt-item-del "test.module" "hello")
    (x:nil? (rt/xt-item "test.module" "hello"))])
  => [[true {"value" 42, "watch" {}}]
      true]

  (!.py
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" 42)
   [(rt/xt-item-del "test.module" "hello")
    (x:nil? (rt/xt-item "test.module" "hello"))])
  => [[true {"value" 42, "watch" {}}]
      true])

^{:refer xt.lang.common-runtime/xt-item-trigger :added "4.0"}
(fact "triggers as item"
  ^:hidden
  
  (!.js
   (rt/xt-purge-spaces)
   (rt/xt-var-set "-/watch.out" nil)
   (rt/xt-item-set "test.module" "hello" 42)
   (rt/xt-add-watch "test.module/hello"
                    "main"
                    (fn [v s]
                      (rt/xt-var-set "-/watch.out" {"value" v "path" s})))
   [(rt/xt-item-trigger "test.module" "hello")
    (rt/xt-var "-/watch.out")])
  => [["main"]
      {"value" 42, "path" "test.module/hello"}]

  (!.lua
   (rt/xt-purge-spaces)
   (rt/xt-var-set "-/watch.out" nil)
   (rt/xt-item-set "test.module" "hello" 42)
   (rt/xt-add-watch "test.module/hello"
                    "main"
                    (fn [v s]
                      (rt/xt-var-set "-/watch.out" {"value" v "path" s})))
   [(rt/xt-item-trigger "test.module" "hello")
    (rt/xt-var "-/watch.out")])
  => [["main"]
      {"value" 42, "path" "test.module/hello"}]

  (!.py
   (rt/xt-purge-spaces)
   (rt/xt-var-set "-/watch.out" nil)
   (rt/xt-item-set "test.module" "hello" 42)
   (rt/xt-add-watch "test.module/hello"
                    "main"
                    (fn [v s]
                      (rt/xt-var-set "-/watch.out" {"value" v "path" s})))
   [(rt/xt-item-trigger "test.module" "hello")
    (rt/xt-var "-/watch.out")])
  => [["main"]
      {"value" 42, "path" "test.module/hello"}])

^{:refer xt.lang.common-runtime/xt-item-set :added "4.0"}
(fact "sets a single item in the space"
  ^:hidden
  
  (!.js
   (rt/xt-purge-spaces)
   [(rt/xt-item-set "test.module" "hello" 42)
    (rt/xt-item "test.module" "hello")])
  => [[true {"value" 42, "watch" {}}]
      42]

  (!.lua
   (rt/xt-purge-spaces)
   [(rt/xt-item-set "test.module" "hello" 42)
    (rt/xt-item "test.module" "hello")])
  => [[true {"value" 42, "watch" {}}]
      42]

  (!.py
   (rt/xt-purge-spaces)
   [(rt/xt-item-set "test.module" "hello" 42)
    (rt/xt-item "test.module" "hello")])
  => [[true {"value" 42, "watch" {}}]
      42])

^{:refer xt.lang.common-runtime/xt-item :added "4.0"}
(fact "gets an xt item by module and key"
  ^:hidden
  
  (!.js
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1})
   (rt/xt-item "test.module" "hello"))
  => {"a" 1}

  (!.lua
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1})
   (rt/xt-item "test.module" "hello"))
  => {"a" 1}

  (!.py
   (rt/xt-purge-spaces)
   (rt/xt-item-set "test.module" "hello" {:a 1})
   (rt/xt-item "test.module" "hello"))
  => {"a" 1})

^{:refer xt.lang.common-runtime/xt-item-get :added "4.0"}
(fact "gets an xt item or sets a default if not exist"
  ^:hidden
  
  (!.js
   (rt/xt-purge-spaces)
   [(rt/xt-item-get "test.module" "hello" (fn [] 1))
    (rt/xt-item-get "test.module" "hello" (fn [] 2))])
  => [1 1]

  (!.lua
   (rt/xt-purge-spaces)
   [(rt/xt-item-get "test.module" "hello" (fn [] 1))
    (rt/xt-item-get "test.module" "hello" (fn [] 2))])
  => [1 1]

  (!.py
   (rt/xt-purge-spaces)
   [(rt/xt-item-get "test.module" "hello" (fn [] 1))
    (rt/xt-item-get "test.module" "hello" (fn [] 2))])
  => [1 1])

^{:refer xt.lang.common-runtime/xt-var-entry :added "4.0"}
(fact "gets the var entry"
  ^:hidden
  
  (!.js
   (rt/xt-purge-spaces)
   (rt/xt-var-set "test.module/hello" 42)
   (rt/xt-var-entry "test.module/hello"))
  => {"value" 42, "watch" {}}

  (!.lua
   (rt/xt-purge-spaces)
   (rt/xt-var-set "test.module/hello" 42)
   (rt/xt-var-entry "test.module/hello"))
  => {"value" 42, "watch" {}}

  (!.py
   (rt/xt-purge-spaces)
   (rt/xt-var-set "test.module/hello" 42)
   (rt/xt-var-entry "test.module/hello"))
  => {"value" 42, "watch" {}})

^{:refer xt.lang.common-runtime/xt-var :added "4.0"}
(fact "gets an xt item"
  ^:hidden
  
  (!.js
   (rt/xt-purge-spaces)
   (rt/xt-var-set "test.module/hello" {:a 1})
   (rt/xt-var "test.module/hello"))
  => {"a" 1}

  (!.lua
   (rt/xt-purge-spaces)
   (rt/xt-var-set "test.module/hello" {:a 1})
   (rt/xt-var "test.module/hello"))
  => {"a" 1}

  (!.py
   (rt/xt-purge-spaces)
   (rt/xt-var-set "test.module/hello" {:a 1})
   (rt/xt-var "test.module/hello"))
  => {"a" 1})

^{:refer xt.lang.common-runtime/xt-var-set :added "4.0"}
(fact "sets the var"
  ^:hidden
  
  (!.js
   (rt/xt-purge-spaces)
   [(rt/xt-var-set "test.module/hello" 42)
    (rt/xt-var-set "test.module/hello" nil)
    (x:nil? (rt/xt-var "test.module/hello"))])
  => [[true {"value" 42, "watch" {}}]
      [true {"value" 42, "watch" {}}]
      true]

  (!.lua
   (rt/xt-purge-spaces)
   [(rt/xt-var-set "test.module/hello" 42)
    (rt/xt-var-set "test.module/hello" nil)
    (x:nil? (rt/xt-var "test.module/hello"))])
  => [[true {"value" 42, "watch" {}}]
      [true {"value" 42, "watch" {}}]
      true]

  (!.py
   (rt/xt-purge-spaces)
   [(rt/xt-var-set "test.module/hello" 42)
    (rt/xt-var-set "test.module/hello" nil)
    (x:nil? (rt/xt-var "test.module/hello"))])
  => [[true {"value" 42, "watch" {}}]
      [true {"value" 42, "watch" {}}]
      true])

^{:refer xt.lang.common-runtime/xt-var-trigger :added "4.0"}
(fact "triggers the var"
  ^:hidden
  
  (!.js
   (rt/xt-purge-spaces)
   (rt/xt-var-set "-/watch.out" nil)
   (rt/xt-var-set "test.module/hello" 42)
   (rt/xt-add-watch "test.module/hello"
                    "main"
                    (fn [v s]
                      (rt/xt-var-set "-/watch.out" {"value" v "path" s})))
   [(rt/xt-var-trigger "test.module/hello")
    (rt/xt-var "-/watch.out")])
  => [["main"]
      {"value" 42, "path" "test.module/hello"}]

  (!.lua
   (rt/xt-purge-spaces)
   (rt/xt-var-set "-/watch.out" nil)
   (rt/xt-var-set "test.module/hello" 42)
   (rt/xt-add-watch "test.module/hello"
                    "main"
                    (fn [v s]
                      (rt/xt-var-set "-/watch.out" {"value" v "path" s})))
   [(rt/xt-var-trigger "test.module/hello")
    (rt/xt-var "-/watch.out")])
  => [["main"]
      {"value" 42, "path" "test.module/hello"}]

  (!.py
   (rt/xt-purge-spaces)
   (rt/xt-var-set "-/watch.out" nil)
   (rt/xt-var-set "test.module/hello" 42)
   (rt/xt-add-watch "test.module/hello"
                    "main"
                    (fn [v s]
                      (rt/xt-var-set "-/watch.out" {"value" v "path" s})))
   [(rt/xt-var-trigger "test.module/hello")
    (rt/xt-var "-/watch.out")])
  => [["main"]
      {"value" 42, "path" "test.module/hello"}])

^{:refer xt.lang.common-runtime/xt-add-watch :added "4.0"}
(fact "adds a watch"
  ^:hidden
  
  (!.js
   (rt/xt-purge-spaces)
   (rt/xt-var-set "-/watch.add" nil)
   (rt/xt-var-set "test.module/hello" 1)
   [(rt/xt-add-watch "test.module/hello"
                     "main"
                     (fn [v s]
                       (rt/xt-var-set "-/watch.add" {"value" v "path" s})))
    (do (rt/xt-var-set "test.module/hello" 2)
        (rt/xt-var "-/watch.add"))])
  => [true
      {"value" 2, "path" "test.module/hello"}]

  (!.lua
   (rt/xt-purge-spaces)
   (rt/xt-var-set "-/watch.add" nil)
   (rt/xt-var-set "test.module/hello" 1)
   [(rt/xt-add-watch "test.module/hello"
                     "main"
                     (fn [v s]
                       (rt/xt-var-set "-/watch.add" {"value" v "path" s})))
    (do (rt/xt-var-set "test.module/hello" 2)
        (rt/xt-var "-/watch.add"))])
  => [true
      {"value" 2, "path" "test.module/hello"}]

  (!.py
   (rt/xt-purge-spaces)
   (rt/xt-var-set "-/watch.add" nil)
   (rt/xt-var-set "test.module/hello" 1)
   [(rt/xt-add-watch "test.module/hello"
                     "main"
                     (fn [v s]
                       (rt/xt-var-set "-/watch.add" {"value" v "path" s})))
    (do (rt/xt-var-set "test.module/hello" 2)
        (rt/xt-var "-/watch.add"))])
  => [true
      {"value" 2, "path" "test.module/hello"}])

^{:refer xt.lang.common-runtime/xt-remove-watch :added "4.0"}
(fact "removes a watch"
  ^:hidden
  
  (!.js
   (rt/xt-purge-spaces)
   (rt/xt-var-set "-/watch.remove" nil)
   (rt/xt-var-set "test.module/hello" 1)
   (rt/xt-add-watch "test.module/hello"
                    "main"
                    (fn [v s]
                      (rt/xt-var-set "-/watch.remove" {"value" v "path" s})))
   [(rt/xt-remove-watch "test.module/hello" "main")
    (do (rt/xt-var-trigger "test.module/hello")
        (x:nil? (rt/xt-var "-/watch.remove")))])
  => [true true]

  (!.lua
   (rt/xt-purge-spaces)
   (rt/xt-var-set "-/watch.remove" nil)
   (rt/xt-var-set "test.module/hello" 1)
   (rt/xt-add-watch "test.module/hello"
                    "main"
                    (fn [v s]
                      (rt/xt-var-set "-/watch.remove" {"value" v "path" s})))
   [(rt/xt-remove-watch "test.module/hello" "main")
    (do (rt/xt-var-trigger "test.module/hello")
        (x:nil? (rt/xt-var "-/watch.remove")))])
  => [true true]

  (!.py
   (rt/xt-purge-spaces)
   (rt/xt-var-set "-/watch.remove" nil)
   (rt/xt-var-set "test.module/hello" 1)
   (rt/xt-add-watch "test.module/hello"
                    "main"
                    (fn [v s]
                      (rt/xt-var-set "-/watch.remove" {"value" v "path" s})))
   [(rt/xt-remove-watch "test.module/hello" "main")
    (do (rt/xt-var-trigger "test.module/hello")
        (x:nil? (rt/xt-var "-/watch.remove")))])
  => [true true])

^{:refer xt.lang.common-runtime/defvar-fn :added "4.0"}
(fact "helper function for defvar macros"
  (let [out (rt/defvar-fn '(rt/defvar.js JS_SAMPLE [] (return 1))
                          "js"
                          'JS_SAMPLE
                          nil
                          nil
                          '([]
                            (return 1)))]
    [(-> out first first)
     (-> out first second)
     (-> out second first)
     (-> out second second)])
  => '[defn.js JS_SAMPLE defn.js JS_SAMPLE-reset])

^{:refer xt.lang.common-runtime/defvar.xt :added "4.0"}
(fact "shortcut for a xt getter and a reset var"
  (let [out (macroexpand-1 '(rt/defvar.xt XT_SAMPLE [] (return 1)))]
    [(-> out first first)
     (-> out first second)
     (-> out second second)])
  => '[defn.xt XT_SAMPLE XT_SAMPLE-reset])

^{:refer xt.lang.common-runtime/defvar.js :added "4.0"}
(fact "shortcut for a js getter and a reset var"
  (let [out (macroexpand-1 '(rt/defvar.js JS_SAMPLE [] (return 1)))]
    [(-> out first first)
     (-> out first second)
     (-> out second second)])
  => '[defn.js JS_SAMPLE JS_SAMPLE-reset])

^{:refer xt.lang.common-runtime/defvar.lua :added "4.0"}
(fact "shortcut for a lua getter and a reset var"
  (let [out (macroexpand-1 '(rt/defvar.lua LUA_SAMPLE [] (return 1)))]
    [(-> out first first)
     (-> out first second)
     (-> out second second)])
  => '[defn.lua LUA_SAMPLE LUA_SAMPLE-reset])


^{:refer xt.lang.common-runtime/defvar.py :added "4.0"}
(fact "shortcut for a python getter and a reset var"
  (let [out (macroexpand-1 '(rt/defvar.py PY_SAMPLE [] (return 1)))]
    [(-> out first first)
     (-> out first second)
     (-> out second second)])
  => '[defn.python PY_SAMPLE PY_SAMPLE-reset])
