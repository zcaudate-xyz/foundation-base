(ns hara.lang.base.compile-test
  (:require [xt.lang.common-data]
             [xt.lang.common-lib]
             [hara.rt.postgres.test.scratch-v1 :as scratch]
             [std.fs :as fs]
             [hara.lang.base.compile :refer :all]
             [hara.lang.base.impl :as impl]
             [hara.lang.base.library :as lib]
             [hara.lang.base.library-snapshot :as snap]
             [hara.lang.model.spec-js.ts :as ts]
             [std.make :as make]
             [std.make.compile :as compile]
             [xt.lang.common-math :as math])
  (:use code.test))

(defn artifact-producer-fixture
  [{:keys [runtime-output runtime-body main]}]
  [{:output (str runtime-output ".artifact")
    :body (str main "::" runtime-body)}])

(fact "compiles a single module with sidecar artifacts"

  (make/with:mock-compile
    (compile-module-single
     {:lang :js
      :root   ".build"
      :target "src"
      :file   "pkg/file.js"
      :main   'xt.lang.common-math
      :layout :flat
      :entry {:label true}
      :emit  {:artifacts [#'ts/module-dts-artifact]}}))
  => (contains-in
      [[".build/src/pkg/file.js" string?]
       [".build/src/pkg/file.d.ts" string?]]))

^{:refer hara.lang.base.compile/compile-script :added "4.0"}
(fact "compiles a script"

  (make/with:mock-compile
    (compile-script {:lang :lua
                     :root   ".build"
                     :target "src"
                     :file   "pkg/file.lua"
                     :main   xt.lang.common-math/gcd
                     :layout :flat
                     :entry {:label true}}))
  => [".build/src/pkg/file.lua"
      "function gcd(a,b){\n  return (0 == b) ? a : gcd(b,a % b);\n}\n\nfunction gcd(a,b){\n  return (0 == b) ? a : gcd(b,a % b);\n}"])

^{:refer hara.lang.base.compile/resolve-artifact-producer :added "4.1"}
(fact "resolves artifact producers from vars, symbols and functions"
  [(fn? (resolve-artifact-producer #'artifact-producer-fixture))
   (fn? (resolve-artifact-producer 'hara.lang.base.compile-test/artifact-producer-fixture))
   (fn? (resolve-artifact-producer artifact-producer-fixture))
   (nil? (resolve-artifact-producer :missing))]
  => [true true true true])

^{:refer hara.lang.base.compile/artifact-descriptor-seq :added "4.1"}
(fact "normalizes nested artifact descriptors into a flat sequence"
  (vec (artifact-descriptor-seq [{:output "a"}
                                 [{:output "b"}]
                                 [{:output "c"}
                                  [{:output "d"}]]]))
  => [{:output "a"}
      {:output "b"}
      {:output "c"}
      {:output "d"}])

^{:refer hara.lang.base.compile/compile-module-artifacts :added "4.1"}
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

^{:refer hara.lang.base.compile/specialization-descriptor :added "4.1"}
(fact "normalizes specialization descriptors"
  (specialization-descriptor
   {:lang :lua
    :source-module 'source.core
    :target-module 'target.core
    :bindings {'cache 'backend.core}})
  => (contains {:lang :lua
                :source 'source.core
                :target 'target.core
                :bindings {'cache 'backend.core}
                :compile-type :graph}))

^{:refer hara.lang.base.compile/compile-module-specialization :added "4.1"}
(fact "installs a specialization before compiling"
  (let [[status opts]
        (with-redefs [lib/install-module-specialized! (fn [_ lang source target opts]
                                                        [lang source target opts])
                      compile-module-graph (fn [opts]
                                             [:compiled opts])]
          (compile-module-specialization
           {:library :mock
            :lang :lua
            :source 'source.core
            :target 'target.core
            :bindings {'cache 'backend.core}
            :root ".build"
            :target-dir "src"}))]
    [status
     (:lang opts)
     (:main opts)
     (:root opts)
     (:target-dir opts)])
  => '[:compiled :lua target.core ".build" "src"])

^{:refer hara.lang.base.compile/compile-module-specializations :added "4.1"}
(fact "compiles batches of specialization descriptors"
  (with-redefs [compile-module-specialization identity]
    (compile-module-specializations [{:source 'a.core :target 'a.out}
                                     {:source 'b.core :target 'b.out}]
                                    {:lang :lua}))
  => '[{:lang :lua :source a.core :target a.out}
       {:lang :lua :source b.core :target b.out}])

^{:refer hara.lang.base.compile/compile-module-single :added "4.0"}
(fact "compiles a single module"

  (make/with:mock-compile
    (compile-module-single
     {:lang :lua
      :root   ".build"
      :target "src"
      :file   "pkg/file.lua"
      :main   'xt.lang.common-math
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

^{:refer hara.lang.base.compile/compile-module-create-links :added "4.0"}
(fact "creates links for modules"
  (compile-module-create-links '[a.b a.c] 'a {})
  => (contains {'a.b (contains {:label "b"}) 'a.c (contains {:label "c"})}))

^{:refer hara.lang.base.compile/compile-module-directory-selected :added "4.0"}
(fact "compiles the directory based on sorted imports"
  (make/with:mock-compile
    (compile-module-directory-selected
      :directory
      ['xt.lang.common-math]
      {:lang :lua :main 'xt.lang.common-math :root ".build" :target "src"}))
  => (contains {:files pos-int?})

  (compile/with:mock-compile
    (compile/with:compile-filter #{'xt.lang.common-math}
      (compile-module-directory-selected
       :directory
       ['xt.lang.common-math 'xt.lang.common-data]
       {:lang :lua :main 'xt.lang.common-math :root ".build" :target "src"})))
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

^{:refer hara.lang.base.compile/compile-module-directory :added "4.0"}
(fact "compiles a directory"
  (with-redefs [fs/select (constantly ["src/xt/lang/common_lib.clj"])
                fs/file-namespace (constantly 'xt.lang.common-math)]
    (make/with:mock-compile
      (compile-module-directory
       {:lang :lua
        :root ".build"
        :target "src"
        :main 'xt.lang.common-math})))
  => (contains {:files pos-int?}))

^{:refer hara.lang.base.compile/compile-module-prep :added "4.0"}
(fact "precs the single entry point setup"
  (compile-module-prep {:lang :lua :main 'xt.lang.common-math})
  => vector?)

^{:refer hara.lang.base.compile/compile-module-root :added "4.0"}
(fact "compiles module.root"
  (make/with:mock-compile
    (compile-module-root
     {:lang :lua
       :root   ".build"
       :target "src"
       :main   'xt.lang.common-math}))
  => (contains {:files pos-int?}))

^{:refer hara.lang.base.compile/compile-module-graph :added "4.0"}
(fact "compiles a module graph"

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

^{:refer hara.lang.base.compile/compile-module-schema :added "4.0"}
(fact "compiles all namespaces into a single file (for sql)"

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
