(ns std.lang.base.workspace-test
  (:use code.test)
  (:require [std.lang.base.workspace :as w]
            [std.lang :as l]
            [std.lib :as h]
            [xt.lang.base-lib :as k]
            [std.string :as str]))

^{:refer std.lang.base.workspace/sym-entry :added "4.0"}
(fact "gets the entry using a symbol"
  ^:hidden
  
  (w/sym-entry :xtalk `k/arr-map)
  => map?)

^{:refer std.lang.base.workspace/sym-pointer :added "4.0"}
(fact "converts to a pointer map"
  ^:hidden
  
  (w/sym-pointer :xtalk `k/arr-map)
  => '{:lang :xtalk
       :module xt.lang.base-lib,
       :section :code
       :id arr-map})

^{:refer std.lang.base.workspace/module-entries :added "4.0"}
(fact "gets all module entries"

  (w/module-entries :xtalk 'xt.lang.base-lib identity)
  => coll?)

^{:refer std.lang.base.workspace/emit-ptr :added "4.0"}
(fact "emits the poiner as a string"
  ^:hidden
  
  (w/emit-ptr k/arr-map)
  => "function arr_map(arr,f){\n  out = [];\n  for:array([e,arr],x:arr_push(out,f(e)));\n  return out;\n}")

^{:refer std.lang.base.workspace/ptr-clip :added "4.0"}
(fact "copies pointer text to clipboard"
  ^:hidden
  
  (do (w/ptr-clip k/arr-map)
      (h/paste))
  => "function arr_map(arr,f){\n  out = [];\n  for:array([e,arr],x:arr_push(out,f(e)));\n  return out;\n}")

^{:refer std.lang.base.workspace/ptr-print :added "4.0"}
(fact "copies pointer text to clipboard"
  ^:hidden
  
  (-> (w/ptr-print k/arr-map)
      (std.concurrent.print/with-out-str))
  => string?)

^{:refer std.lang.base.workspace/ptr-setup :added "4.0"}
(fact "calls setup on a pointer")

^{:refer std.lang.base.workspace/ptr-teardown :added "4.0"}
(fact "calls teardown on a pointer")

^{:refer std.lang.base.workspace/ptr-setup-deps :added "4.0"}
(fact "calls setup on a pointer and all dependencies")

^{:refer std.lang.base.workspace/ptr-teardown-deps :added "4.0"}
(fact "calls teardown on pointer all dependencies")

^{:refer std.lang.base.workspace/rt-resolve :added "4.0"}
(fact "resolves an rt given keyword"
  ^:hidden
  
  (w/rt-resolve :xtalk)
  => map?
  
  (w/rt-resolve (w/rt-resolve :xtalk))
  => map?)

^{:refer std.lang.base.workspace/emit-module :added "4.0"}
(fact "emits the entire module"
  ^:hidden
  
  (w/emit-module (l/rt 'xt.lang.base-lib :xtalk))
  => string?)

^{:refer std.lang.base.workspace/print-module :added "4.0"}
(fact "emits and prints out the module"

  (std.print/with-out-str
    (w/print-module (l/rt 'xt.lang.base-lib :xtalk)))
  => string?)

^{:refer std.lang.base.workspace/rt:module :added "4.0"}
(fact "gets the book module for a runtime"
  ^:hidden
  
  (w/rt:module (l/rt 'xt.lang.base-lib :xtalk))
  => map?)

^{:refer std.lang.base.workspace/rt:module-purge :added "4.0"}
(fact "purges the current workspace")

^{:refer std.lang.base.workspace/rt:inner :added "4.0"}
(fact "gets the inner client for a shared runtime")

^{:refer std.lang.base.workspace/rt:restart :added "4.0"}
(fact "restarts the shared runtime")

^{:refer std.lang.base.workspace/intern-macros :added "4.0"}
(fact "interns all macros from one namespace to another")
