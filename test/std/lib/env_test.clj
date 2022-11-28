(ns std.lib.env-test
  (:use code.test)
  (:require [std.lib.env :refer :all])
  (:refer-clojure :exclude [*ns* prn require ns]))

^{:refer std.lib.env/ns-sym :added "3.0"}
(fact "returns the namespace symbol"

  (ns-sym)
  => 'std.lib.env-test)

^{:refer std.lib.env/ns-get :added "3.0"}
(fact "gets a symbol in the current namespace"

  (ns-get 'std.lib.env "ns-get"))

^{:refer std.lib.env/require :added "3.0"}
(fact "a concurrency safe require")

^{:refer std.lib.env/dev? :added "3.0"}
(fact "checks if current environment is dev"

  (dev?)
  => boolean?)

^{:refer std.lib.env/sys:resource :added "3.0"}
(fact "finds a resource on class path"

  (sys:resource "std/lib.clj")
  => java.net.URL)

^{:refer std.lib.env/sys:resource-cached :added "4.0"}
(fact "caches the operation on a resource call"

  (sys:resource-cached (atom {})
                       "project.clj"
                       slurp)
  => string?)

^{:refer std.lib.env/sys:resource-content :added "3.0"}
(fact "reads the content"

  (sys:resource-content "project.clj")
  => string?)

^{:refer std.lib.env/sys:ns-url :added "4.0"}
(fact "gets the url for ns"
  ^:hidden
  (sys:ns-url 'std.lib.env)
  => java.net.URL)

^{:refer std.lib.env/sys:ns-file :added "4.0"}
(fact "gets the file for ns"
  ^:hidden
  (sys:ns-file 'std.lib.env)
  => string?)

^{:refer std.lib.env/sys:ns-dir :added "4.0"}
(fact "gets the dir for ns"
  ^:hidden
  (sys:ns-dir 'std.lib.env)
  => string?)

^{:refer std.lib.env/close :added "3.0"}
(fact "closes any object implementing `java.io.Closable`")

^{:refer std.lib.env/local:set :added "3.0"}
(fact "sets the local functions")

^{:refer std.lib.env/local:clear :added "3.0"}
(fact "clears the local functions")

^{:refer std.lib.env/local :added "3.0"}
(fact "applies the local function")

^{:refer std.lib.env/p :added "3.0"}
(fact "shortcut to `(local :println)` ")

^{:refer std.lib.env/pp-str :added "4.0"}
(fact "pretty prints a string")

^{:refer std.lib.env/pp-fn :added "4.0"}
(fact "the pp print function ")

^{:refer std.lib.env/pp :added "3.0"}
(fact "shortcut to `(local :pprint)` ")

^{:refer std.lib.env/do:pp :added "4.0"}
(fact "doto `pp`")

^{:refer std.lib.env/pl-add-lines :added "3.0"}
(fact "helper function for pl")

^{:refer std.lib.env/pl :added "3.0"}
(fact "print with lines")

^{:refer std.lib.env/do:pl :added "4.0"}
(fact "doto `pl`")

^{:refer std.lib.env/pl:fn :added "4.0"}
(fact "creates a pl function")

^{:refer std.lib.env/prn :added "3.0"}
(fact "`prn` but also includes namespace and file info")

^{:refer std.lib.env/do:prn :added "4.0"}
(fact "doto `prn`")

^{:refer std.lib.env/prn:fn :added "4.0"}
(fact "creates a prn function")

^{:refer std.lib.env/prf :added "4.0"}
(fact "pretty prints with format")

^{:refer std.lib.env/meter :added "3.0"}
(fact "measure and prints time taken for a form")

^{:refer std.lib.env/meter-out :added "4.0"}
(fact "measures and output meter")

^{:refer std.lib.env/throwable-string :added "3.0"}
(fact "creates a string from a throwable"

  (throwable-string (ex-info "ERROR" {}))
  => string?)

^{:refer std.lib.env/explode :added "3.0"}
(comment "prints the stacktrace for an exception"

  (explode (throw (ex-info "Error" {}))))
