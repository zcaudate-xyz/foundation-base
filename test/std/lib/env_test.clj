(ns std.lib.env-test
  (:use code.test)
  (:require [std.lib.env :refer :all])
  (:refer-clojure :exclude [*ns* prn require ns pr with-out-str]))

^{:refer std.lib.env/ns-sym :added "3.0"}
(fact "returns the namespace symbol"
  ^:hidden
  
  (ns-sym)
  => 'std.lib.env-test)

^{:refer std.lib.env/ns-get :added "3.0"}
(fact "gets a symbol in the current namespace"
  ^:hidden
  
  (ns-get 'std.lib.env "ns-get")
  => fn?)

^{:refer std.lib.env/require :added "3.0"}
(fact "a concurrency safe require"
  (require 'std.lib.env)
  => nil)

^{:refer std.lib.env/dev? :added "3.0"}
(fact "checks if current environment is dev"
  ^:hidden
  
  (dev?)
  => boolean?)

^{:refer std.lib.env/sys:resource :added "3.0"}
(fact "finds a resource on class path"
  ^:hidden
  
  (sys:resource "std/lib.clj")
  => java.net.URL)

^{:refer std.lib.env/sys:resource-cached :added "4.0"}
(fact "caches the operation on a resource call"
  ^:hidden

  (sys:resource-cached (atom {})
                       "scratch.clj"
                       slurp)
  => string?)

^{:refer std.lib.env/sys:resource-content :added "3.0"}
(fact "reads the content"
  ^:hidden
  
  (sys:resource-content "scratch.clj")
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
(fact "closes any object implementing `java.io.Closable`"
  (close (java.io.StringReader. "hello"))
  => nil)

^{:refer std.lib.env/local:set :added "3.0"}
(fact "sets the local functions"
  (local:set :test (fn [] :hello))
  => map?)

^{:refer std.lib.env/local:clear :added "3.0"}
(fact "clears the local functions"
  (local:clear :test)
  => map?)

^{:refer std.lib.env/local :added "3.0"}
(fact "applies the local function"
  (local:set :test (fn [] :hello))
  (local :test)
  => :hello)

^{:refer std.lib.env/p :added "3.0"}
(fact "shortcut to `(local :println)` "
  (with-out-str (p "hello"))
  => "hello\n")

^{:refer std.lib.env/pp-str :added "4.0"}
(fact "pretty prints a string"
  (pp-str {:a 1})
  => "{:a 1}")

^{:refer std.lib.env/pp-fn :added "4.0"}
(fact "the pp print function "
  (with-out-str (pp-fn {:a 1}))
  => (any string? nil?))

^{:refer std.lib.env/pp :added "3.0"}
(fact "shortcut to `(local :pprint)` "
  (with-out-str (pp {:a 1}))
  => string?)

^{:refer std.lib.env/do:pp :added "4.0"}
(fact "doto `pp`"
  (with-out-str (do:pp {:a 1}))
  => string?)

^{:refer std.lib.env/pl-add-lines :added "3.0"}
(fact "helper function for pl"
  (pl-add-lines "hello")
  => (any string? nil?))

^{:refer std.lib.env/pl :added "3.0"}
(fact "print with lines"
  (with-out-str (pl "hello"))
  => string?)

^{:refer std.lib.env/do:pl :added "4.0"}
(fact "doto `pl`"
  (with-out-str (do:pl "hello"))
  => string?)

^{:refer std.lib.env/pl:fn :added "4.0"}
(fact "creates a pl function"
  ((pl:fn) "hello")
  => nil)

^{:refer std.lib.env/prn :added "3.0"}
(fact "`prn` but also includes namespace and file info"
  (with-out-str (prn "hello"))
  => string?)

^{:refer std.lib.env/do:prn :added "4.0"}
(fact "doto `prn`"
  (with-out-str (do:prn "hello"))
  => string?)

^{:refer std.lib.env/prn:fn :added "4.0"}
(fact "creates a prn function"
  ((prn:fn) "hello")
  => nil)

^{:refer std.lib.env/prf :added "4.0"}
(fact "pretty prints with format"
  (with-out-str (prf "hello"))
  => string?)

^{:refer std.lib.env/meter :added "3.0"}
(fact "measure and prints time taken for a form"
  (with-out-str (meter (+ 1 2)))
  => string?)

^{:refer std.lib.env/meter-out :added "4.0"}
(fact "measures and output meter"
  (first (meter-out (+ 1 2)))
  => (any vector? number?))

^{:refer std.lib.env/throwable-string :added "3.0"}
(fact "creates a string from a throwable"

  (throwable-string (ex-info "ERROR" {}))
  => string?)

^{:refer std.lib.env/explode :added "3.0"}
(comment "prints the stacktrace for an exception"

  (explode (throw (ex-info "Error" {}))))

^{:refer std.lib.env/match-filter :added "4.0"}
(fact "matches given a range of filters"
  ^:hidden
  
  (match-filter #"ello" 'hello)
  => true

  (match-filter #"^ello" 'hello)
  => false

  (match-filter 'code 'code.test)
  => true

  (match-filter 'hara 'spirit.common)
  => false)


^{:refer std.lib.env/dbg-print :added "4.0"}
(fact "prints debug info with namespace and location"
  (with-out-str (dbg-print "hello" {:line 1 :column 1} "world"))
  => string?)

^{:refer std.lib.env/dbg :added "4.0"}
(fact "debug macro that prints the form and its result"
  (with-out-str (dbg (+ 1 2)))
  => string?)

^{:refer std.lib.env/with:dbg :added "4.0"}
(fact "macro to bind *debug* flag"
  (with:dbg true (dbg-global))
  => true)

^{:refer std.lib.env/dbg-global :added "4.0"}
(fact "access or set the global *debug* flag"
  (dbg-global)
  => boolean?)

^{:refer std.lib.env/dbg:add-filters :added "4.0"}
(fact "adds filters to the debug allowlist"
  (dbg:add-filters :test)
  => set?)

^{:refer std.lib.env/dbg:remove-filters :added "4.0"}
(fact "removes filters from the debug allowlist"
  (dbg:remove-filters :test)
  => (complement #(contains? % :test)))

^{:refer std.lib.env/with-system :added "4.0"}
(fact "executes body with system print"
  (with-system (println "hello"))
  => nil)

^{:refer std.lib.env/with-out-str :added "4.0"}
(fact "captures output to string using system print"
  (with-out-str (print "hello"))
  => "hello")

^{:refer std.lib.env/pr :added "4.0"}
(fact "shortcut to (local :print ...)"
  (with-out-str (pr "hello"))
  => "hello")

^{:refer std.lib.env/wrap-print :added "4.0"}
(fact "wraps a function to print its result"
  ((wrap-print +) 1 2)
  => 3)


^{:refer std.lib.env/prfn :added "4.1"}
(fact "TODO")