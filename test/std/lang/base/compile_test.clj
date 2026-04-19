(ns std.lang.base.compile-test
  (:require [xt.lang.common-data]
             [xt.lang.common-lib]
             [rt.postgres.test.scratch-v1 :as scratch]
             [std.fs :as fs]
             [std.lang.base.compile :refer :all]
             [std.lang.base.impl :as impl]
             [std.lang.base.library :as lib]
             [std.lang.base.library-snapshot :as snap]
             [std.lang.model.spec-js.ts :as ts]
             [std.make :as make]
             [std.make.compile :as compile]
             [xt.lang.common-iter :as it]
             [xt.lang.common-lib :as k])
  (:use code.test))

(defn artifact-producer-fixture
  [{:keys [runtime-output runtime-body main]}]
  [{:output (str runtime-output ".artifact")
    :body (str main "::" runtime-body)}])

^{:refer std.lang.base.compile/compile-script :added "4.0"}
(fact "compiles a script"
  ^:hidden
  
  (make/with:mock-compile
    (compile-script {:lang :lua
                     :root   ".build"
                     :target "src"
                     :file   "pkg/file.lua"
                     :main   xt.lang.common-lib/gcd
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
      :main   'xt.lang.common-lib
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

(fact "compiles a single module with sidecar artifacts"
  ^:hidden

  (make/with:mock-compile
    (compile-module-single
     {:lang :js
      :root   ".build"
      :target "src"
      :file   "pkg/file.js"
      :main   'xt.lang.common-lib
      :layout :flat
      :entry {:label true}
      :emit  {:artifacts [#'ts/module-dts-artifact]}}))
  => (contains-in
      [[".build/src/pkg/file.js" string?]
       [".build/src/pkg/file.d.ts" string?]]))

^{:refer std.lang.base.compile/compile-module-graph :added "4.0"}
(fact "compiles a module graph"
  ^:hidden
  
  (make/with:mock-compile
    (compile-module-graph
     {:lang :lua
      :root   ".build"
      :target "src"
      :file   "pkg/file.lua"
      :main   'xt.lang.common-data
      :layout :flat
      :entry {:label true}}))
  => (contains-in
      {:files 1, :status :changed, :written [[string?]]}))

^{:refer std.lang.base.compile/compile-module-directory :added "4.0"}
(fact "compiles a directory"
  (with-redefs [fs/select (constantly ["src/xt/lang/common_lib.clj"])
                fs/file-namespace (constantly 'xt.lang.common-lib)]
    (make/with:mock-compile
      (compile-module-directory
       {:lang :lua
        :root ".build"
        :target "src"
        :main 'xt.lang.common-lib})))
  => (contains {:files pos-int?}))

^{:refer std.lang.base.compile/compile-module-schema :added "4.0"}
(fact "compiles all namespaces into a single file (for sql)"
  ^:hidden
  
  (make/with:mock-compile
    (compile-module-schema
     {:lang   :postgres
      :root   ".build"
      :target "src"
      :file   "pkg/schema.sql"
      :main   'rt.postgres.test.scratch-v1
      :layout :flat
      :entry {:label true}}))
  => (contains-in [".build/src/pkg/schema.sql"
                   string?]))


^{:refer std.lang.base.compile/compile-module-directory-selected :added "4.0"}
(fact "compiles the directory based on sorted imports"
  (make/with:mock-compile
    (compile-module-directory-selected
      :directory
      ['xt.lang.common-lib]
      {:lang :lua :main 'xt.lang.common-lib :root ".build" :target "src"}))
  => (contains {:files pos-int?})

  (compile/with:mock-compile
    (compile/with:compile-filter #{'xt.lang.common-lib}
      (compile-module-directory-selected
       :directory
       ['xt.lang.common-lib 'xt.lang.common-data]
       {:lang :lua :main 'xt.lang.common-lib :root ".build" :target "src"})))
  => (contains {:files 1})

  (compile/with:mock-compile
    (compile-module-directory-selected
      :directory
      ['xt.lang.common-data]
      {:lang :js
       :main 'xt.lang.common-data
       :root ".build"
       :target "src"
       :emit {:artifacts [#'ts/module-dts-artifact]}}))
  => (contains {:files pos-int?}))

^{:refer std.lang.base.compile/compile-module-prep :added "4.0"}
(fact "precs the single entry point setup"
  (compile-module-prep {:lang :lua :main 'xt.lang.common-lib})
  => vector?)

^{:refer std.lang.base.compile/compile-module-root :added "4.0"}
(fact "compiles module.root"
  (make/with:mock-compile
    (compile-module-root
     {:lang :lua
       :root   ".build"
       :target "src"
       :main   'xt.lang.common-lib}))
  => (contains {:files pos-int?}))

^{:refer std.lang.base.compile/compile-module-create-links :added "4.0"}
(fact "creates links for modules"
  (compile-module-create-links '[a.b a.c] 'a {})
  => (contains {'a.b (contains {:label "b"}) 'a.c (contains {:label "c"})}))


^{:refer std.lang.base.compile/resolve-artifact-producer :added "4.1"}
(fact "resolves artifact producers from vars, symbols and functions"
  [(fn? (resolve-artifact-producer #'artifact-producer-fixture))
   (fn? (resolve-artifact-producer 'std.lang.base.compile-test/artifact-producer-fixture))
   (fn? (resolve-artifact-producer artifact-producer-fixture))
   (nil? (resolve-artifact-producer :missing))]
  => [true true true true])

^{:refer std.lang.base.compile/artifact-descriptor-seq :added "4.1"}
(fact "normalizes nested artifact descriptors into a flat sequence"
  (vec (artifact-descriptor-seq [{:output "a"}
                                 [{:output "b"}]
                                 [{:output "c"}
                                  [{:output "d"}]]]))
  => [{:output "a"}
      {:output "b"}
      {:output "c"}
      {:output "d"}])

^{:refer std.lang.base.compile/compile-module-artifacts :added "4.1"}
(fact "includes the primary output and any sidecar artifacts"
  (let [artifacts (compile-module-artifacts
                   "BODY"
                   "pkg/file.js"
                   {:main 'demo.core
                    :emit {:artifacts [#'artifact-producer-fixture]}}
                   {:module 'demo.core})]
    [(mapv :output artifacts)
     (mapv :body artifacts)])
  => [["pkg/file.js"
       "pkg/file.js.artifact"]
      ["BODY"
       "demo.core::BODY"]])
