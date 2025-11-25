(ns std.lang.base.compile-test
  (:use code.test)
  (:require [std.lang.base.compile :refer :all]
            [std.lang.base.impl :as impl]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.make :as make]
            [xt.lang.base-lib :as k]
            [xt.lang.base-iter :as it]
            [rt.postgres.script.scratch :as scratch]))

^{:refer std.lang.base.compile/compile-script :added "4.0"}
(fact "compiles a script"
  ^:hidden
  
  (make/with:mock-compile
    (compile-script {:lang :lua
                     :root   ".build"
                     :target "src"
                     :file   "pkg/file.lua"
                     :main   xt.lang.base-lib/gcd
                     :layout :flat
                     :entry {:label true}}))
  => [".build/src/pkg/file.lua"
      "function gcd(a,b){\n  return (0 == b) ? a : gcd(b,a % b);\n}\n\nfunction gcd(a,b){\n  return (0 == b) ? a : gcd(b,a % b);\n}"])

^{:refer std.lang.base.compile/compile-module-single :added "4.0"}
(fact "compiles a single module"
  ^:hidden
  
  (make/with:mock-compile
    (compile-module-single
     {:lang :lua
      :root   ".build"
      :target "src"
      :file   "pkg/file.lua"
      :main   'xt.lang.base-lib
      :layout :flat
      :entry {:label true}
      :emit  {:static {:header true}
              :code {:transforms [(fn [out static]
                                    (if (:header static)
                                      (str "HEADER\n\n" out)
                                      out))]}}}))
  => (contains-in
      [".build/src/pkg/file.lua"
       string?]))

^{:refer std.lang.base.compile/compile-module-graph :added "4.0"}
(fact "compiles a module graph"
  ^:hidden
  
  (make/with:mock-compile
    (compile-module-graph
     {:lang :lua
      :root   ".build"
      :target "src"
      :file   "pkg/file.lua"
      :main   'xt.lang.base-iter
      :layout :flat
      :entry {:label true}}))
  => (contains-in
      {:files 1, :status :changed, :written [[string?]]}))

^{:refer std.lang.base.compile/compile-module-directory :added "4.0"}
(fact "compiles a directory"
  "placeholder for tests")

^{:refer std.lang.base.compile/compile-module-schema :added "4.0"}
(fact "compiles all namespaces into a single file (for sql)"
  ^:hidden
  
  (make/with:mock-compile
    (compile-module-schema
     {:lang   :postgres
      :root   ".build"
      :target "src"
      :file   "pkg/schema.sql"
      :main   'rt.postgres.script.scratch
      :layout :flat
      :entry {:label true}}))
  => (contains-in [".build/src/pkg/schema.sql"
                   string?]))


^{:refer std.lang.base.compile/compile-module-directory-selected :added "4.0"}
(fact "compiles the directory based on sorted imports"
  "placeholder for tests")

^{:refer std.lang.base.compile/compile-module-prep :added "4.0"}
(fact "precs the single entry point setup"
  "placeholder for tests")

^{:refer std.lang.base.compile/compile-module-root :added "4.0"}
(fact "compiles module.root"
  "placeholder for tests")

^{:refer std.lang.base.compile/compile-module-create-links :added "4.0"}
(fact "creates links for modules"
  "placeholder for tests")
