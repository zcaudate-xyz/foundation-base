(ns std.lang.manage.xtalk-scaffold-test
  (:require [clojure.string :as str]
            [std.fs :as fs]
            [std.lang.manage.xtalk-scaffold :refer :all])
  (:use code.test))

(def +grammar-entry+
  {:op :x-get-key
   :category :xtalk-custom
   :canonical-symbol 'x:get-key
   :macro 'std.lang.base.grammar-xtalk/tf-get-key
   :cases [{:id :basic
            :input '(x:get-key obj "a")
            :expect {:xtalk '(. obj ["a"])}}
           {:id :default
            :input '(x:get-key obj "a" "DEFAULT")
            :expect {:xtalk '(or (. obj ["a"]) "DEFAULT")}}]})

(def runtime-test-forms
  (read-string
   "[(ns xt.lang.common-lib-test
       (:require [std.lang :as l]
                 [xt.lang.common-lib :as k])
       (:use code.test))
      (do
        (l/script- :js {:runtime :basic})
        (l/script- :lua {:runtime :basic}))
      (fact:global {:setup [(l/rt:restart)]})
      (fact \"identity function\"
        ^:hidden
        (!.js (k/identity 1))
        => 1
        (!.lua (k/identity 1))
        => 1)
      (fact \"wrapped runtime form\"
        (set (!.js (k/example)))
        => #{1}
        (set (!.lua (k/example)))
        => #{1})
      (fact \"vector runtime form\"
        [(!.js (k/a)) (!.js (k/b))]
        => [1 2]
        [(!.lua (k/a)) (!.lua (k/b))]
        => [1 2])
      (fact \"placeholder\")]"))

(def runtime-template-forms
  (read-string
   "[(ns xtbench.js.lang.common-lib-test
        (:require [std.lang :as l]
                  [xt.lang.common-lib :as k])
        (:use code.test))
       (l/script- :js {:runtime :basic})
      (fact:global {:setup [(l/rt:restart)]})
      ^{:refer xt.lang.common-lib/identity :added \"4.0\"}
      (fact \"identity function\"
        ^:hidden
        (!.js (k/identity 1))
        => 1)
       (fact \"wrapped runtime form\"
        (set (!.js (k/example)))
        => #{1})]"))

(def canonical-runtime-template-forms
  (read-string
   "[(ns xt.lang.common-lib-test
       (:require [std.lang :as l]
                 [xt.lang.common-lib :as k])
       (:use code.test))
      (l/script- :lua {:runtime :basic})
      (fact:global {:setup [(l/rt:restart)]})
      ^{:refer xt.lang.common-lib/identity :added \"4.1\"}
      (fact \"identity function\"
        ^:hidden
        (!.lua (k/identity 1))
        => 1)
      (fact \"wrapped runtime form\"
        (set (!.lua (k/example)))
        => #{1})]"))

(def blocked-runtime-template-forms
  (read-string
   "[(ns xt.lang.common-notify-test
       (:require [std.lang :as l]
                 [xt.lang.common-notify :as notify]
                 [xt.lang.common-repl :as repl])
       (:use code.test))
      (l/script- :js {:runtime :basic})
      (fact \"notify helper\"
        (notify/wait-on :js
          (repl/notify 1))
        => 1)]"))

(defn with-temp-runtime-suite-file
  [forms f]
  (let [root (str (fs/create-tmpdir "xtalk-scaffold"))
        path (str (fs/path root "sample_test.clj"))]
    (try
      (spit path (render-top-level-forms forms))
      (f path)
      (finally
        (fs/delete root)))))

(defn with-temp-xtlang-root
  [entries f]
  (let [root (str (fs/create-tmpdir "xtalk-scaffold-root"))]
    (try
      (doseq [[rel-path forms] entries]
        (let [path (str (fs/path root rel-path))]
          (fs/create-directory (fs/parent path))
          (spit path (render-top-level-forms forms))))
      (f root)
      (finally
        (fs/delete root)))))

^{:refer std.lang.manage.xtalk-scaffold/read-xtalk-ops :added "4.1"}
(fact "reads xtalk ops from path"
  (fn? read-xtalk-ops)
  => true)

^{:refer std.lang.manage.xtalk-scaffold/quoted-form-string :added "4.1"}
(fact "quotes forms"
  (quoted-form-string '(+ 1 2))
  => "'(+ 1 2)")

^{:refer std.lang.manage.xtalk-scaffold/grammar-entry? :added "4.1"}
(fact "recognizes grammar-backed xtalk entries"
  (grammar-entry? +grammar-entry+)
  => true)

^{:refer std.lang.manage.xtalk-scaffold/grammar-entries :added "4.1"}
(fact "filters grammar entries"
  (count (grammar-entries [+grammar-entry+]))
  => 1)

^{:refer std.lang.manage.xtalk-scaffold/macro-added :added "4.1"}
(fact "returns macro added metadata"
  (macro-added 'std.lang.base.grammar-xtalk/tf-get-key)
  => string?)

^{:refer std.lang.manage.xtalk-scaffold/case-xtalk-expect :added "4.1"}
(fact "extracts expected xtalk form"
  (case-xtalk-expect {:expect {:xtalk '(+ 1 2)}})
  => '(+ 1 2))

^{:refer std.lang.manage.xtalk-scaffold/render-grammar-assertion :added "4.1"}
(fact "renders grammar assertion"
  (str/includes? (render-grammar-assertion 'std.lang.base.grammar-xtalk/tf-get-key
                                           {:input '(x:get-key obj "a")
                                            :expect {:xtalk '(. obj ["a"])}})
                 "tf-get-key")
  => true)

^{:refer std.lang.manage.xtalk-scaffold/render-grammar-fact :added "4.1"}
(fact "renders grammar fact block"
  (str/includes? (render-grammar-fact +grammar-entry+) "(fact")
  => true)

^{:refer std.lang.manage.xtalk-scaffold/render-grammar-test-file :added "4.1"}
(fact "renders grammar xtalk tests from canonical cases"
  (let [out (render-grammar-test-file [+grammar-entry+])]
    [(str/includes? out "(ns std.lang.base.grammar-xtalk-ops-test")
     (str/includes? out "tf-get-key")
     (str/includes? out "(tf-get-key '(x:get-key obj \"a\"))")
     (str/includes? out "=> '(. obj [\"a\"])")
     (str/includes? out "(tf-get-key '(x:get-key obj \"a\" \"DEFAULT\"))")])
  => [true true true true true])

^{:refer std.lang.manage.xtalk-scaffold/grammar-test-path :added "4.1"}
(fact "builds grammar test path"
  (string? (grammar-test-path {:root "."}))
  => true)

^{:refer std.lang.manage.xtalk-scaffold/scaffold-xtalk-grammar-tests :added "4.1"}
(fact "scaffold function is callable"
  (fn? scaffold-xtalk-grammar-tests)
  => true)

^{:refer std.lang.manage.xtalk-scaffold/normalize-runtime-lang :added "4.1"}
(fact "normalizes runtime aliases"
  (normalize-runtime-lang :ruby)
  => :rb)

^{:refer std.lang.manage.xtalk-scaffold/runtime-lang-config :added "4.1"}
(fact "returns runtime config"
  (map? (runtime-lang-config :js))
  => true

  (runtime-lang-config :php)
  => {:script :php
      :dispatch '!.php
      :suffix "php"
      :runtime :basic
      :check-mode :realtime})

^{:refer std.lang.manage.xtalk-scaffold/runtime-script-lang :added "4.1"}
(fact "returns runtime script language"
  (runtime-script-lang :js)
  => :js)

^{:refer std.lang.manage.xtalk-scaffold/runtime-dispatch-symbol :added "4.1"}
(fact "returns runtime dispatch symbol"
  (runtime-dispatch-symbol :js)
  => '!.js)

^{:refer std.lang.manage.xtalk-scaffold/runtime-lang-suffix :added "4.1"}
(fact "returns runtime suffix"
  (runtime-lang-suffix :ruby)
  => "rb"

  (runtime-lang-suffix :php)
  => "php")

^{:refer std.lang.manage.xtalk-scaffold/read-top-level-forms :added "4.1"}
(fact "read-top-level-forms is callable"
  (fn? read-top-level-forms)
  => true)

^{:refer std.lang.manage.xtalk-scaffold/runtime-expr-lang :added "4.1"}
(fact "infers runtime from expression"
  (runtime-expr-lang '(!.js (k/a)))
  => :js)

^{:refer std.lang.manage.xtalk-scaffold/fact-form? :added "4.1"}
(fact "detects fact form"
  (fact-form? '(fact "x" 1 => 1))
  => true)

^{:refer std.lang.manage.xtalk-scaffold/fact-global-form? :added "4.1"}
(fact "detects fact:global form"
  (fact-global-form? '(fact:global {:setup []}))
  => true)

^{:refer std.lang.manage.xtalk-scaffold/script-form? :added "4.1"}
(fact "detects script form"
  (script-form? '(l/script- :js {}))
  => true)

^{:refer std.lang.manage.xtalk-scaffold/expand-top-level-form :added "4.1"}
(fact "expands do forms"
  (expand-top-level-form '(do (a) (b)))
  => '((a) (b)))

^{:refer std.lang.manage.xtalk-scaffold/replace-ns-name :added "4.1"}
(fact "replaces ns declaration symbol"
  (second (replace-ns-name '(ns a.b (:use code.test)) 'x.y))
  => 'x.y)

^{:refer std.lang.manage.xtalk-scaffold/runtime-test-ns :added "4.1"}
(fact "creates runtime test ns"
  (runtime-test-ns 'xt.lang.common-lib-test :js)
  => 'xtbench.js.lang.common-lib-test)

^{:refer std.lang.manage.xtalk-scaffold/render-top-level-forms :added "4.1"}
(fact "renders top-level forms"
  (string? (render-top-level-forms '[(ns a.b) (def x 1)]))
  => true)

^{:refer std.lang.manage.xtalk-scaffold/attach-leading-meta :added "4.1"}
(fact "attaches leading metadata"
  (meta (first (attach-leading-meta '((+ 1 2)) {:hidden true})))
  => (contains {:hidden true}))

^{:refer std.lang.manage.xtalk-scaffold/commented-form? :added "4.1"}
(fact "detects commented forms by metadata"
  (commented-form? (with-meta '(+ 1 2) {:comment true}))
  => true)

^{:refer std.lang.manage.xtalk-scaffold/test-file-path :added "4.1"}
(fact "builds a test file path"
  (string? (test-file-path {:root "." :test-paths ["test"]} 'a.b-test))
  => true

  (str/ends-with? (test-file-path {:root "." :test-paths ["test"]}
                                  'xtbench.js.lang.common-lib-test)
                  "/xtbench/js/lang/common_lib_test.clj")
  => true)

^{:refer std.lang.manage.xtalk-scaffold/infer-runtime-lang :added "4.1"}
(fact "infers runtime language from forms"
  (infer-runtime-lang '((l/script- :js {:runtime :basic})))
  => :js)

^{:refer std.lang.manage.xtalk-scaffold/runtime-script-langs :added "4.1"}
(fact "collects distinct script languages from template forms"
  (runtime-script-langs runtime-template-forms)
  => [:js]

  (set (runtime-script-langs runtime-test-forms))
  => #{:js :lua})

^{:refer std.lang.manage.xtalk-scaffold/single-runtime-template-lang :added "4.1"}
(fact "detects single-runtime templates"
  (single-runtime-template-lang runtime-template-forms)
  => :js

  (single-runtime-template-lang runtime-test-forms)
  => nil)

^{:refer std.lang.manage.xtalk-scaffold/runtime-suffixed-test-ns? :added "4.1"}
(fact "detects runtime suffixed test namespaces"
  [(runtime-suffixed-test-ns? 'xt.lang.common-lib-js-test)
   (runtime-suffixed-test-ns? 'xtbench.js.lang.common-lib-test)
   (runtime-suffixed-test-ns? 'xt.lang.common-lib-test)
   (runtime-suffixed-test-ns? 'xt.lang.common-lib-dt-test)]
  => [true true false true])

^{:refer std.lang.manage.xtalk-scaffold/runtime-template-supported? :added "4.1"}
(fact "blocks twostep suite generation for runtime-coupled templates"
  [(runtime-template-supported? canonical-runtime-template-forms :dart)
   (runtime-template-supported? blocked-runtime-template-forms :dart)
   (runtime-template-supported? runtime-test-forms :dart)]
  => [true false false])

^{:refer std.lang.manage.xtalk-scaffold/replace-runtime-symbol :added "4.1"}
(fact "replaces runtime dispatch symbol"
  (replace-runtime-symbol '(!.js (k/a)) '!.js '!.lua)
  => '(!.lua (k/a)))

^{:refer std.lang.manage.xtalk-scaffold/replace-string-value :added "4.1"}
(fact "replaces string value recursively"
  (replace-string-value '{:a "x" :b ["x"]} "x" "y")
  => '{:a "y" :b ["y"]})

^{:refer std.lang.manage.xtalk-scaffold/transform-script-form :added "4.1"}
(fact "transforms script form language"
  (transform-script-form '(l/script- :js {:runtime :basic}) :js :lua)
  => '(l/script- :lua {:runtime :basic}))

^{:refer std.lang.manage.xtalk-scaffold/template-runtime-test-ns :added "4.1"}
(fact "templates runtime test namespace"
  (template-runtime-test-ns 'xtbench.js.lang.common-lib-test :js :rb)
  => 'xtbench.rb.lang.common-lib-test)

^{:refer std.lang.manage.xtalk-scaffold/template-runtime-test-forms :added "4.1"}
(fact "templates a runtime test from js to ruby"
  (let [out-forms (template-runtime-test-forms runtime-template-forms :js :ruby)
        out (render-top-level-forms out-forms)]
    [(= :rb (normalize-runtime-lang :ruby))
      (= 'xtbench.rb.lang.common-lib-test
         (second (first out-forms)))
      (= :ruby (second (second out-forms)))
      (str/includes? out "xtbench.rb.lang.common-lib-test")
      (str/includes? out "(l/script- :ruby")
      (str/includes? out "!.rb")
      (not (str/includes? out "!.js"))
      (str/includes? out ":refer xt.lang.common-lib/identity")])
  => [true true true true true true true true])

^{:refer std.lang.manage.xtalk-scaffold/split-fact-form :added "4.1"}
(fact "splits a mixed runtime fact form"
  (let [{:keys [shared langs]} (split-fact-form '(fact "x" (!.js (k/a)) => 1 (!.lua (k/a)) => 1) [:js :lua])]
    [(some? shared) (set (keys langs))])
  => [true #{:js :lua}])

^{:refer std.lang.manage.xtalk-scaffold/separate-runtime-test-forms :added "4.1"}
(fact "splits a multi-runtime test namespace into per-language forms"
  (let [{:keys [shared by-lang]}
        (separate-runtime-test-forms runtime-test-forms [:js :lua])
        js-form (some #(when (= "identity function" (second %)) %) (get by-lang :js))
        lua-form (some #(when (= "identity function" (second %)) %) (get by-lang :lua))
        shared-out (render-top-level-forms shared)
        js-out (render-top-level-forms (get by-lang :js))
        lua-out (render-top-level-forms (get by-lang :lua))]
    [(str/includes? shared-out "xt.lang.common-lib-test")
     (str/includes? shared-out "(fact \"placeholder\")")
      (not (str/includes? shared-out "wrapped runtime form"))
      (str/includes? js-out "xtbench.js.lang.common-lib-test")
      (str/includes? js-out "(l/script- :js")
      (= true (:hidden (meta (nth js-form 2))))
      (str/includes? js-out "!.js")
      (not (str/includes? js-out "!.lua"))
      (str/includes? js-out "wrapped runtime form")
      (str/includes? js-out "vector runtime form")
      (str/includes? lua-out "xtbench.lua.lang.common-lib-test")
      (str/includes? lua-out "(l/script- :lua")
      (= true (:hidden (meta (nth lua-form 2))))
      (str/includes? lua-out "!.lua")
      (not (str/includes? lua-out "!.js"))
      (str/includes? lua-out "wrapped runtime form")
      (str/includes? lua-out "vector runtime form")])
  => [true true true true true true true true true true true true true true true true true])

^{:refer std.lang.manage.xtalk-scaffold/canonical-runtime-source-test-ns :added "4.1"}
(fact "normalizes generated runtime namespaces back to canonical test namespaces"
  [(canonical-runtime-source-test-ns 'xtbench.js.lang.common-lib-test)
   (canonical-runtime-source-test-ns 'xt.lang.common-lib-js-test)
   (canonical-runtime-source-test-ns 'xt.lang.common-lib-test)]
  => '[xt.lang.common-lib-test
       xt.lang.common-lib-test
       xt.lang.common-lib-test])

^{:refer std.lang.manage.xtalk-scaffold/separate-runtime-tests :added "4.1"}
(fact "separate-runtime-tests is callable"
  (fn? separate-runtime-tests)
  => true)

^{:refer std.lang.manage.xtalk-scaffold/scaffold-runtime-template :added "4.1"}
(fact "scaffold-runtime-template is callable"
  (fn? scaffold-runtime-template)
  => true)

^{:refer std.lang.manage.xtalk-scaffold/xtlang-runtime-suite-sources :added "4.1"}
(fact "finds eligible xt.lang templates for twostep bulk compilation"
  (with-temp-xtlang-root
    {"test/xt/lang/common_lib_test.clj" canonical-runtime-template-forms
     "test/xt/lang/common_notify_test.clj" blocked-runtime-template-forms
     "test/xt/lang/common_lib_js_test.clj" runtime-template-forms}
    (fn [root]
      (let [sources (xtlang-runtime-suite-sources {:root root
                                                   :input-root "test/xt/lang"
                                                   :lang :dart})
            source  (first sources)]
        [(= 1 (count sources))
         (= 'xt.lang.common-lib-test (:ns source))
         (= :lua (:from-lang source))
         (= :dart (:lang source))
         (= :twostep (:runtime-type source))
         (= :batched (:check-mode source))])))
  => [true true true true true true])

^{:refer std.lang.manage.xtalk-scaffold/compile-xtlang-runtime-bulk-suites :added "4.1"}
(fact "exports and compiles eligible xt.lang templates into dart bulk payloads"
  (with-temp-xtlang-root
    {"test/xt/lang/common_lib_test.clj" canonical-runtime-template-forms
     "test/xt/lang/common_notify_test.clj" blocked-runtime-template-forms}
    (fn [root]
      (let [{:keys [lang count outputs]}
             (compile-xtlang-runtime-bulk-suites nil {:root root
                                                      :input-root "test/xt/lang"
                                                      :lang :dart})
             output (first outputs)]
        [(= :dart lang)
         (= 1 count)
         (= :lua (:from-lang output))
         (= 2 (:suite-count output))
         (= 2 (:bulk-count output))
         (= :twostep (:runtime-type output))
         (= :batched (:check-mode output))
         (str/ends-with? (:suite-path output) "_suite.edn")
         (str/ends-with? (:bulk-path output) "-dt-bulk.edn")])))
  => [true true true true true true true true true])


^{:refer std.lang.manage.xtalk-scaffold/runtime-type :added "4.1"}
(fact "returns configured runtime implementation type"
  [(runtime-type :js)
   (runtime-type :dart)]
  => [:basic :twostep])

^{:refer std.lang.manage.xtalk-scaffold/runtime-check-mode :added "4.1"}
(fact "returns configured runtime verification mode"
  [(runtime-check-mode :php)
   (runtime-check-mode :dart)]
  => [:realtime :batched])

^{:refer std.lang.manage.xtalk-scaffold/runtime-suite-groups :added "4.1"}
(fact "groups runtimes by check mode"
  (runtime-suite-groups [:ruby :dart :php :js])
  => '{:batched [:dart]
       :realtime [:js :php :rb]})

^{:refer std.lang.manage.xtalk-scaffold/canonical-suite-path :added "4.1"}
(fact "derives the canonical suite path from a test file"
  (canonical-suite-path "test/xt/lang/common_lib_test.clj")
  => "test/xt/lang/common_lib_suite.edn")

^{:refer std.lang.manage.xtalk-scaffold/runtime-bulk-path :added "4.1"}
(fact "derives the per-language bulk suite path"
  (runtime-bulk-path "test/xt/lang/common_lib_suite.edn" :dart)
  => "test/xt/lang/common_lib_suite-dt-bulk.edn")

^{:refer std.lang.manage.xtalk-scaffold/form-line-info :added "4.1"}
(fact "extracts line and column metadata from forms"
  (form-line-info (with-meta '(+ 1 2)
                    {:line 10 :column 3 :end-line 10 :end-column 9}))
  => '{:line 10
       :column 3
       :end-line 10
       :end-column 9})

^{:refer std.lang.manage.xtalk-scaffold/merge-language-exceptions :added "4.1"}
(fact "merges nested language exception maps"
  (merge-language-exceptions {:dart {:expect 10}}
                             {:dart {:skip true}
                              :go {:expect 20}})
  => '{:dart {:expect 10
              :skip true}
       :go {:expect 20}})

^{:refer std.lang.manage.xtalk-scaffold/form-language-exceptions :added "4.1"}
(fact "reads exception metadata from either supported key"
  [(form-language-exceptions (with-meta '(+ 1 2) {:lang-exceptions {:dart {:skip true}}}))
   (form-language-exceptions (with-meta '(+ 1 2) {:exceptions {:go {:expect 20}}}))]
  => '[{:dart {:skip true}}
       {:go {:expect 20}}])

^{:refer std.lang.manage.xtalk-scaffold/strip-runtime-dispatch :added "4.1"}
(fact "removes runtime dispatch wrappers recursively"
  [(strip-runtime-dispatch '(!.js (k/a)))
   (strip-runtime-dispatch '[(!.js (k/a)) (!.lua (k/b))])]
  => '[(k/a)
       [(k/a) (k/b)]])

^{:refer std.lang.manage.xtalk-scaffold/fact-assertion-forms :added "4.1"}
(fact "extracts assertion pairs from fact bodies"
  (fact-assertion-forms '(fact "x" (!.js (k/a)) => 1 "note" (!.lua (k/b)) => 2))
  => '[[(!.js (k/a)) 1]
       [(!.lua (k/b)) 2]])

^{:refer std.lang.manage.xtalk-scaffold/canonical-case-id :added "4.1"}
(fact "builds stable case ids from namespace, title and index"
  (canonical-case-id 'xt.lang.common-lib-test "identity function" 2)
  => "xt.lang.common-lib-test::identity-function::2")

^{:refer std.lang.manage.xtalk-scaffold/fact-runtime-cases :added "4.1"}
(fact "extracts canonical runtime cases from a fact"
  (let [fact-form (with-meta
                    '(fact "identity function"
                       ^{:lang-exceptions {:dart {:expect 10}}}
                       (!.js (+ 1 2))
                       => 3
                       (!.lua (+ 2 3))
                       => 5)
                    {:line 20})
        cases (fact-runtime-cases 'xt.lang.common-lib-test fact-form :js)]
    [(count cases)
     (:id (first cases))
     (:form (first cases))
     (:expect (first cases))
     (:exceptions (first cases))])
  => [1
      "xt.lang.common-lib-test::identity-function::0"
      '(+ 1 2)
      3
      {:dart {:expect 10}}])

^{:refer std.lang.manage.xtalk-scaffold/canonical-runtime-suite-forms :added "4.1"}
(fact "converts runtime tests into canonical suite data"
  (let [suite (canonical-runtime-suite-forms runtime-test-forms :lua)]
    [(:ns suite)
     (:lang suite)
     (:runtime-type suite)
     (:check-mode suite)
     (count (:cases suite))
     (mapv :form (:cases suite))])
  => '[xt.lang.common-lib-test
       :lua
       :basic
       :realtime
       3
       [(k/identity 1)
        (set (k/example))
        [(k/a) (k/b)]]])

^{:refer std.lang.manage.xtalk-scaffold/case-language-config :added "4.1"}
(fact "applies language-specific overrides to cases"
  (case-language-config {:expect 30
                         :form '(+ 10 20)
                         :exceptions {:dart {:skip true
                                             :expect 10
                                             :form '(* 2 5)}}}
                        :dart)
  => '{:skip true
       :expect 10
       :form (* 2 5)
       :exception {:skip true
                   :expect 10
                   :form (* 2 5)}})

^{:refer std.lang.manage.xtalk-scaffold/compile-runtime-bulk-suite :added "4.1"}
(fact "compiles canonical suites into bulk payloads"
  (let [suite (canonical-runtime-suite-forms runtime-test-forms :js)
        bulk (compile-runtime-bulk-suite suite :dart)]
    [(:lang bulk)
     (:runtime-type bulk)
     (:check-mode bulk)
     (mapv :value (:bulk-form bulk))
     (mapv :expect (:verify bulk))])
  => [:dart
      :twostep
      :batched
      '[(k/identity 1)
        (set (k/example))
        [(k/a) (k/b)]]
      '[1 #{1} [1 2]]])

^{:refer std.lang.manage.xtalk-scaffold/export-runtime-suite :added "4.1"}
(fact "exports runtime tests from a file to canonical suite EDN"
  (with-temp-runtime-suite-file
    runtime-test-forms
    (fn [path]
      (let [{:keys [output-path suite count]} (export-runtime-suite nil {:input-path path
                                                                         :lang :lua})]
        [(str/ends-with? output-path "_suite.edn")
         (= :lua (:lang suite))
         (= 3 count)])))
  => [true true true])

^{:refer std.lang.manage.xtalk-scaffold/compile-runtime-bulk :added "4.1"}
(fact "reads canonical suite EDN files and emits batched runtime payloads"
  (with-temp-runtime-suite-file
    runtime-test-forms
    (fn [path]
      (let [{:keys [output-path]} (export-runtime-suite nil {:input-path path
                                                             :lang :js
                                                             :write true})
            {:keys [bulk count]} (compile-runtime-bulk nil {:input-path output-path
                                                            :lang :dart})]
        [(:lang bulk)
         (:check-mode bulk)
         count])))
  => [:dart :batched 3])

^{:refer std.lang.manage.xtalk-scaffold/transform-script-runtime :added "4.1"}
(fact "updates script runtime options for the target language"
  (transform-script-runtime '(l/script- :js {:runtime :basic :layout :flat}) :dart)
  => '(l/script- :js {:runtime :twostep :layout :flat}))
