(ns xt.lang.base-runtime-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-runtime :as rt]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.base-runtime :as rt]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.base-runtime :as rt]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.base-runtime/xt-exists? :added "4.0"
  :setup [(l/rt:restart)]}
(fact "checks that the xt map exists"
  ^:hidden
  
  (!.js
   [(rt/xt-exists?)
    (rt/xt)
    (rt/xt-exists?)])
  => (contains-in [false {"config" {}, "spaces" {}, "::" "xt"} true])

  (!.lua
   [(rt/xt-exists?)
    (rt/xt)
    (rt/xt-exists?)])
  => (contains-in [false {"config" {}, "spaces" {}, "::" "xt"} true])

  (!.py
   [(rt/xt-exists?)
    (rt/xt)
    (rt/xt-exists?)])
  => (contains-in [false {"config" {}, "spaces" {}, "::" "xt"} true]))

^{:refer xt.lang.base-runtime/xt-create :added "4.0"}
(fact "creates an empty xt structure")

^{:refer xt.lang.base-runtime/xt :added "4.0"}
(fact "gets the current xt or creates a new one")

^{:refer xt.lang.base-runtime/xt-current :added "4.0"}
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

^{:refer xt.lang.base-runtime/xt-purge :added "4.0"}
(fact "empties the current xt")

^{:refer xt.lang.base-runtime/xt-purge-config :added "4.0"}
(fact "clears all `:config` entries")

^{:refer xt.lang.base-runtime/xt-purge-spaces :added "4.0"}
(fact "clears all `:spaces` entries")

^{:refer xt.lang.base-runtime/xt-lookup-id :added "4.0"}
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

^{:refer xt.lang.base-runtime/xt-config-list :added "4.0"}
(fact "lists all config entries in the xt")

^{:refer xt.lang.base-runtime/xt-config-set :added "4.0"}
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

^{:refer xt.lang.base-runtime/xt-config-del :added "4.0"}
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

^{:refer xt.lang.base-runtime/xt-config :added "4.0"}
(fact "gets a config entry")

^{:refer xt.lang.base-runtime/xt-space-list :added "4.0"}
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

^{:refer xt.lang.base-runtime/xt-space-del :added "4.0"}
(fact "deletes a space")

^{:refer xt.lang.base-runtime/xt-space :added "4.0"}
(fact "gets a space")

^{:refer xt.lang.base-runtime/xt-space-clear :added "4.0"}
(fact "clears all items in the space")

^{:refer xt.lang.base-runtime/xt-item-del :added "4.0"}
(fact "deletes a single item in the space")

^{:refer xt.lang.base-runtime/xt-item-trigger :added "4.0"}
(fact "triggers as item")

^{:refer xt.lang.base-runtime/xt-item-set :added "4.0"}
(fact "sets a single item in the space")

^{:refer xt.lang.base-runtime/xt-item :added "4.0"}
(fact "gets an xt item by module and key")

^{:refer xt.lang.base-runtime/xt-item-get :added "4.0"}
(fact "gets an xt item or sets a default if not exist")

^{:refer xt.lang.base-runtime/xt-var-entry :added "4.0"}
(fact "gets the var entry")

^{:refer xt.lang.base-runtime/xt-var :added "4.0"}
(fact "gets an xt item"

  (!.lua
   (rt/xt-var "-/hello")))

^{:refer xt.lang.base-runtime/xt-var-set :added "4.0"}
(fact "sets the var")

^{:refer xt.lang.base-runtime/xt-var-trigger :added "4.0"}
(fact "triggers the var")

^{:refer xt.lang.base-runtime/xt-add-watch :added "4.0"}
(fact "adds a watch")

^{:refer xt.lang.base-runtime/xt-remove-watch :added "4.0"}
(fact "removes a watch")

^{:refer xt.lang.base-runtime/defvar-fn :added "4.0"}
(fact "helper function for defvar macros")

^{:refer xt.lang.base-runtime/defvar.xt :added "4.0"}
(fact "shortcut for a xt getter and a reset var")

^{:refer xt.lang.base-runtime/defvar.js :added "4.0"}
(fact  "shortcut for a js getter and a reset var")

^{:refer xt.lang.base-runtime/defvar.lua :added "4.0"}
(fact "shortcut for a lua getter and a reset var")


^{:refer xt.lang.base-runtime/defvar.py :added "4.0"}
(fact "shortcut for a python getter and a reset var")