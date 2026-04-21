(ns std.lang.base.workspace-test
  (:require [xt.lang.common-data :as data]
             [xt.lang.common-lib :as cl]
             [std.lang :as l]
             [std.lang.base.workspace :as w]
             [std.lib.env :as env]
             [std.lib.os :as os])
  (:refer-clojure :exclude [cat print])
  (:use code.test))

(l/script- :xtalk
  {:require [[xt.lang.common-lib :as cl]
             [xt.lang.common-data :as data]]})

(defn.xt workspace-noop
  []
  (return (cl/noop)))

^{:refer std.lang.base.workspace/sym-entry :added "4.0"}
(fact "gets the entry using a symbol"

  (w/sym-entry :xtalk `cl/noop)
  => map?)

^{:refer std.lang.base.workspace/sym-pointer :added "4.0"}
(fact "converts to a pointer map"

  (w/sym-pointer :xtalk `cl/noop)
  => '{:lang :xtalk
       :module xt.lang.common-lib,
       :section :code
       :id noop})

^{:refer std.lang.base.workspace/module-entries :added "4.0"}
(fact "gets all module entries"

  (w/module-entries :xtalk 'xt.lang.common-lib identity)
  => coll?)

^{:refer std.lang.base.workspace/emit-ptr :added "4.0"}
(fact "emits the poiner as a string"

  (w/emit-ptr cl/noop)
  => string?)

^{:refer std.lang.base.workspace/ptr-clip :added "4.0"}
(comment "copies pointer text to clipboard"

  (do (w/ptr-clip cl/noop)
      (os/paste))
  => string?)

^{:refer std.lang.base.workspace/ptr-print :added "4.0"}
(fact "copies pointer text to clipboard"

  (-> (w/ptr-print cl/noop)
       (env/with-out-str))
  => string?)

^{:refer std.lang.base.workspace/ptr-setup :added "4.0"}
(fact "calls setup on a pointer"
  (w/ptr-setup cl/noop) => any?)

^{:refer std.lang.base.workspace/ptr-teardown :added "4.0"}
(fact "calls teardown on a pointer"
  (w/ptr-teardown cl/noop) => any?)

^{:refer std.lang.base.workspace/ptr-setup-deps :added "4.0"}
(fact "calls setup on a pointer and all dependencies"
  (w/ptr-setup-deps cl/noop) => any?)

^{:refer std.lang.base.workspace/ptr-teardown-deps :added "4.0"}
(fact "calls teardown on pointer all dependencies"
  (w/ptr-teardown-deps cl/noop) => any?)

^{:refer std.lang.base.workspace/rt-resolve :added "4.0"}
(fact "resolves an rt given keyword"

  (w/rt-resolve :xtalk)
  => map?

  (w/rt-resolve (w/rt-resolve :xtalk))
  => map?)

^{:refer std.lang.base.workspace/emit-module :added "4.0"}
(fact "emits the entire module"

  (w/emit-module (l/rt 'std.lang.base.workspace-test :xtalk))
  => string?)

^{:refer std.lang.base.workspace/print-module :added "4.0"}
(fact "emits and prints out the module"

  (env/with-out-str
    (w/print-module (l/rt 'std.lang.base.workspace-test :xtalk)))
  => string?)

^{:refer std.lang.base.workspace/rt:module :added "4.0"}
(fact "gets the book module for a runtime"

  (w/rt:module (l/rt 'xt.lang.common-lib :xtalk))
  => map?)

^{:refer std.lang.base.workspace/rt:module-purge :added "4.0"}
(fact "purges the current workspace"
  (w/rt:module-purge (l/rt 'xt.lang.common-lib :xtalk)) => any?)

^{:refer std.lang.base.workspace/rt:inner :added "4.0"}
(fact "gets the inner client for a shared runtime"
  (w/rt:inner :xtalk) => any?)

^{:refer std.lang.base.workspace/rt:restart :added "4.0"}
(fact "restarts the shared runtime"
  (w/rt:restart :xtalk) => any?)

^{:refer std.lang.base.workspace/intern-macros :added "4.0"}
(fact "interns all macros from one namespace to another"
  (w/intern-macros :xtalk 'xt.lang.common-lib) => map?)


^{:refer std.lang.base.workspace/ptr-display-str :added "4.0"}
(fact "copies pointer text to clipboard"
  (w/ptr-display-str cl/noop) => string?)

^{:refer std.lang.base.workspace/rt:module-meta :added "4.0"}
(fact "gets the book module for a runtime"
  (w/rt:module-meta :xtalk) => map?)
