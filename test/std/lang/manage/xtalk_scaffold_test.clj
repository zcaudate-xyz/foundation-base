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
   "[(ns xt.sample.base-lib-test
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

(def runtime-test-forms-with-top-level
  (read-string
   "[(ns xt.sample.base-view-test
        (:require [std.lang :as l]
                  [xt.db.base-view :as v])
        (:use code.test))
      (l/script- :js {:runtime :basic})
      (l/script- :lua {:runtime :basic})
      (fact:global {:setup [(l/rt:restart)
                            (l/rt:scaffold :js)
                            (l/rt:scaffold :lua)]})
      (def +views+ (!.js (v/example)))
      (fact \"baseline\"
        (def +tables+ (!.js (v/example)))
        (!.lua (v/example))
        => +tables+)]"))

(def runtime-template-forms
  (read-string
   "[(ns xtbench.js.sample.base-lib-test
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
   "[(ns xt.sample.base-lib-test
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
   "[(ns xt.sample.common-notify-test
         (:require [std.lang :as l]
                   [xt.lang.common-notify :as notify]
                   [xt.lang.common-repl :as repl])
        (:use code.test))
       (l/script- :js {:runtime :basic})
        (fact \"notify helper\"
         (notify/wait-on :js
            (repl/notify 1))
          => 1)]"))

(def lua-empty-vector-runtime-forms
  (read-string
   "[(ns xt.sample.lua-empty-vector-test
         (:require [std.lang :as l])
         (:use code.test))
       (l/script- :js {:runtime :basic})
       (l/script- :lua {:runtime :basic})
       (fact \"normalizes lua empty vectors\"
         (!.js {:queued []})
         => {\"queued\" []}
         (!.lua {:queued []})
         => {\"queued\" []})]"))

(def lua-nil-map-runtime-forms
  (read-string
   "[(ns xt.sample.lua-nil-map-test
         (:require [std.lang :as l])
        (:use code.test))
      (l/script- :js {:runtime :basic})
      (l/script- :lua {:runtime :basic})
      (fact \"normalizes lua nil map entries\"
        (!.js {:meta {\"listener/id\" \"b2\"}
               :pred nil})
        => {\"meta\" {\"listener/id\" \"b2\"}
            \"pred\" nil}
        (!.lua {:meta {\"listener/id\" \"b2\"}
                :pred nil})
         => {\"meta\" {\"listener/id\" \"b2\"}
             \"pred\" nil})]"))

(def foreign-prefix-runtime-forms
  (read-string
   "[(ns xt.sample.sql-util-test
         (:require [std.lang :as l]
                   [xt.lang.common-spec :as xt]
                   [xt.db.sql-util :as ut])
         (:use code.test))
       (l/script- :js {:runtime :basic})
       (l/script- :lua {:runtime :basic})
       (fact \"encodes a value to sql\"
         ^:hidden
         (!.js (xt/x:json-encode 100000000000000000))
         (!.lua (string.format \"%0.f\" 100000000000000000))
         (!.js (ut/encode-value 100000000000000000))
         => \"'100000000000000000'\"
         (!.lua (ut/encode-value 100000000000000000))
         => \"'100000000000000000'\") ]"))

(def template-forms-with-helper-runtime
  (read-string
   "[(ns xtbench.js.sample.sql-call-test
          (:require [std.lang :as l])
          (:use code.test))
       (l/script- :postgres {:runtime :jdbc.client
                             :config {:dbname \"test-scratch\"}})
       (l/script- :js {:runtime :basic})
       (fact:global {:setup [(l/rt:restart)]})
        (fact \"placeholder\"
          ^:hidden
          (!.js 1)
          => 1)]"))

(def js-sqlite-template-forms
  (read-string
   "[(ns xtbench.js.sample.sql-sqlite-test
         (:require [std.lang :as l]
                   [xt.lang.common-notify :as notify])
         (:use code.test))
       (l/script- :js {:runtime :basic
                       :require [[xt.sys.conn-dbsql :as dbsql]
                                 [js.lib.driver-sqlite :as js-sqlite]]})
       (fact \"sqlite helper\"
         (notify/wait-on :js
           (dbsql/connect {:constructor js-sqlite/connect-constructor}
                          {:success (fn [conn] conn)}))
         => 1)]"))

(def split-runtime-reference-forms
  (read-string
   "[(ns xt.sample.common-notify-test
        (:require [std.lang :as l]
                  [xt.lang.common-notify :as notify]
                  [xt.lang.common-repl :as repl])
        (:use code.test))
      (l/script- :js {:runtime :basic})
      (l/script- :lua {:runtime :basic})
      (fact \"notify helper\"
        (notify/wait-on :js (repl/notify 1))
        => 1
        (notify/wait-on :lua (repl/notify 1))
        => 1)
      (fact \"captured helper\"
        [(notify/captured :js)
         (:id (l/rt :js))]
        => [nil nil]
        [(notify/captured :lua)
         (:id (l/rt :lua))]
         => [nil nil])]"))

(def metadata-setup-runtime-forms
  (read-string
   "[(ns xt.sample.cache-meta-test
        (:require [std.lang :as l])
        (:use code.test))
      (l/script- :js {:runtime :basic})
      (l/script- :lua {:runtime :basic})
      ^{:refer xt.sample.cache-meta/setup :added \"4.1\"
        :setup [(def +account+ (!.js {:id 1}))]}
      (fact \"retargets metadata setup\"
        ^:hidden
        (!.js +account+)
        => {:id 1}
        (!.lua +account+)
        => {:id 1})]"))

(def alias-gated-runtime-forms
  (read-string
   "[(ns xt.sample.alias-gated-test
        (:require [std.lang :as l])
        (:use code.test))
      (l/script- :js {:runtime :basic
                      :require [[xt.lang.common-repl :as repl]]})
      (l/script- :lua {:runtime :basic})
      (fact \"js-only helper\"
        ^:hidden
        (!.js (repl/notify 1))
        => 1)]"))

(def lua-specific-template-forms
  (read-string
   "[(ns xt.sample.common-proto-test
       (:require [std.lang :as l])
       (:use code.test))
      (l/script- :lua {:runtime :basic
                       :require [[xt.lang.common-lib :as k]]})
      (fact \"lua-only helper\"
        (!.lua
         (var mt (k/proto-create {:world \"hello\"}))
         (var a {})
         (setmetatable a mt)
         true)
        => true)]"))

(def runtime-test-forms-with-unsupported
  (read-string
   "[(ns xt.sample.base-mixed-test
       (:require [std.lang :as l]
                 [xt.db.base-view :as v])
       (:use code.test))
      (l/script- :js {:runtime :basic})
      (l/script- :lua {:runtime :basic})
      (def +mixed+ [(!.js (v/example)) (!.lua (v/example))])
      (fact \"mixed assertion\"
        [(!.js (v/example)) (!.lua (v/example))]
        => [1 1])]"))

(defn with-temp-runtime-suite-file
  [forms f]
  (let [root (str (fs/create-tmpdir "xtalk-scaffold"))
        path (str (fs/path root "sample_test.clj"))]
    (try
      (spit path (render-top-level-forms forms))
      (f path)
      (finally
        (fs/delete root)))))

(defn with-temp-runtime-source-file
  [source f]
  (let [root (str (fs/create-tmpdir "xtalk-scaffold-source"))
        path (str (fs/path root "sample_test.clj"))]
    (try
      (spit path source)
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
  (runtime-test-ns 'xt.sample.base-lib-test :js)
  => 'xtbench.js.sample.base-lib-test)

^{:refer std.lang.manage.xtalk-scaffold/render-top-level-forms :added "4.1"}
(fact "renders top-level forms"
  (string? (render-top-level-forms '[(ns a.b) (def x 1)]))
  => true

  (let [out (render-top-level-forms
             [(with-meta
                '(fact "x"
                   (!.js 1)
                   => 1)
                {:line 10
                 :column 2
                 :hidden true
                 :refer (with-meta 'xt.lang.common-lib/identity
                          {:line 9 :column 1})
                 :added "4.1"})])]
    [(str/includes? out ":line")
     (str/includes? out ":column")
     (str/includes? out ":refer xt.lang.common-lib/identity")
     (str/includes? out ":added \"4.1\"")
     (str/includes? out ":hidden true")])
  => [false false true true true])

^{:refer std.lang.manage.xtalk-scaffold/attach-leading-meta :added "4.1"}
(fact "attaches leading metadata"
  (meta (first (attach-leading-meta '((+ 1 2)) {:hidden true})))
  => (contains {:hidden true}))

^{:refer std.lang.manage.xtalk-scaffold/commented-form? :added "4.1"}
(fact "detects commented forms by metadata"
  (commented-form? (with-meta '(+ 1 2) {:comment true}))
  => true

  (commented-form? '(comment (+ 1 2)))
  => true)

^{:refer std.lang.manage.xtalk-scaffold/test-file-path :added "4.1"}
(fact "builds a test file path"
  (string? (test-file-path {:root "." :test-paths ["test"]} 'a.b-test))
  => true

  (str/ends-with? (test-file-path {:root "." :test-paths ["test"]}
                                  'xtbench.js.sample.base-lib-test)
                  "/test/xtbench/js/sample/base_lib_test.clj")
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
  [(runtime-suffixed-test-ns? 'xt.sample.base-lib-js-test)
   (runtime-suffixed-test-ns? 'xtbench.js.sample.base-lib-test)
   (runtime-suffixed-test-ns? 'xt.sample.base-lib-test)
   (runtime-suffixed-test-ns? 'xt.lang.common-lib-dt-test)]
  => [true true false true])

^{:refer std.lang.manage.xtalk-scaffold/runtime-template-supported? :added "4.1"}
(fact "blocks twostep suite generation for runtime-coupled templates"
  [(runtime-template-supported? canonical-runtime-template-forms :dart)
      (runtime-template-supported? blocked-runtime-template-forms :dart)
      (runtime-template-supported? runtime-test-forms :dart)
      (runtime-template-supported? lua-specific-template-forms :lua)
      (runtime-template-supported? lua-specific-template-forms :js)
      (runtime-template-supported? js-sqlite-template-forms :lua)
      (runtime-template-supported? js-sqlite-template-forms :python)
      (runtime-template-supported? js-sqlite-template-forms :dart)]
  => [true true false true false false false false])

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
  (template-runtime-test-ns 'xtbench.js.sample.base-lib-test :js :rb)
  => 'xtbench.rb.sample.base-lib-test)

^{:refer std.lang.manage.xtalk-scaffold/template-runtime-test-forms :added "4.1"}
(fact "templates a runtime test from js to ruby"
  (let [out-forms (template-runtime-test-forms runtime-template-forms :js :ruby)
        out (render-top-level-forms out-forms)]
    [(= :rb (normalize-runtime-lang :ruby))
      (= 'xtbench.rb.sample.base-lib-test
         (second (first out-forms)))
      (= :ruby (second (second out-forms)))
      (str/includes? out "xtbench.rb.sample.base-lib-test")
      (str/includes? out "(l/script- :ruby")
      (str/includes? out "!.rb")
      (not (str/includes? out "!.js"))
      (str/includes? out ":refer xt.lang.common-lib/identity")])
  => [true true true true true true true true])

^{:refer std.lang.manage.xtalk-scaffold/template-runtime-test-forms :added "4.1"}
(fact "retargets host-side runtime references in template output"
  (let [out (render-top-level-forms
             (template-runtime-test-forms blocked-runtime-template-forms :js :lua))]
    [(str/includes? out "(notify/wait-on :lua ")
     (not (str/includes? out "(notify/wait-on :js "))])
  => [true true])

^{:refer std.lang.manage.xtalk-scaffold/split-fact-form :added "4.1"}
(fact "splits a mixed runtime fact form"
  (let [{:keys [shared langs]} (split-fact-form '(fact "x" (!.js (k/a)) => 1 (!.lua (k/a)) => 1) [:js :lua])]
    [(some? shared) (set (keys langs))])
  => [true #{:js :lua}])

^{:refer std.lang.manage.xtalk-scaffold/split-fact-form :added "4.1"}
(fact "synthesizes missing runtime clauses from portable source clauses"
  (let [{:keys [langs]} (split-fact-form '(fact "x" (!.js (k/a)) => 1 (!.lua (k/a)) => 1)
                                         [:js :lua :python :dart])
        py-out (render-top-level-forms [(get langs :python)])
        dt-out (render-top-level-forms [(get langs :dart)])]
    [(contains? langs :python)
     (contains? langs :dart)
     (str/includes? py-out "!.py")
     (not (str/includes? py-out "!.js"))
     (str/includes? dt-out "!.dt")
     (not (str/includes? dt-out "!.lua"))])
  => [true true true true true true])

^{:refer std.lang.manage.xtalk-scaffold/split-fact-form :added "4.1"}
(fact "does not synthesize from runtime-specific helper aliases"
  (let [{:keys [langs]} (split-fact-form '(fact "x" (!.js (j/future-delayed [10] (return 1))) => 1)
                                         [:js :python :dart])]
    (set (keys langs)))
  => #{:js})

^{:refer std.lang.manage.xtalk-scaffold/separate-runtime-test-forms :added "4.1"}
(fact "retargets host-side runtime references in split output"
  (let [{:keys [by-lang]} (separate-runtime-test-forms split-runtime-reference-forms [:js :lua])
        js-out (render-top-level-forms (get by-lang :js))
        lua-out (render-top-level-forms (get by-lang :lua))]
    [(str/includes? js-out "(notify/wait-on :js)")
     (not (str/includes? js-out "(notify/wait-on :lua)"))
     (str/includes? js-out "(notify/captured :js)")
     (not (str/includes? js-out "(notify/captured :lua)"))
     (str/includes? js-out "(l/rt :js)")
     (not (str/includes? js-out "(l/rt :lua)"))
     (str/includes? lua-out "(notify/wait-on :lua)")
     (not (str/includes? lua-out "(notify/wait-on :js)"))])
  => [true true true true true true true true])

^{:refer std.lang.manage.xtalk-scaffold/separate-runtime-test-forms :added "4.1"}
(fact "retargets metadata setup forms in split output"
  (let [{:keys [by-lang]} (separate-runtime-test-forms metadata-setup-runtime-forms [:js :lua])
        lua-out (render-top-level-forms (get by-lang :lua))]
    [(str/includes? lua-out ":setup [(def +account+ (!.lua {:id 1}))]")
     (not (str/includes? lua-out ":setup [(def +account+ (!.js {:id 1}))]"))])
  => [true true])

^{:refer std.lang.manage.xtalk-scaffold/separate-runtime-test-forms :added "4.1"}
(fact "does not synthesize split output when target runtime lacks required aliases"
  (let [{:keys [by-lang]} (separate-runtime-test-forms alias-gated-runtime-forms [:js :lua])]
    [(contains? by-lang :js)
     (contains? by-lang :lua)])
  => [true false])

^{:refer std.lang.manage.xtalk-scaffold/classify-split-form :added "4.1"}
(fact "classifies split-relevant forms"
  [(classify-split-form '(fact:global {:setup [(l/rt:restart) (l/rt:scaffold :js)]}))
   (classify-split-form '(def +views+ (!.js (k/example))))
   (classify-split-form '(fact "x" (def +tables+ (!.js (k/example))) (!.lua (k/example)) => +tables+))]
  => '[#{:fact-global}
       #{:top-level-shared}
       #{:runtime-fact :runtime-prefix}])

^{:refer std.lang.manage.xtalk-scaffold/separate-runtime-test-forms :added "4.1"}
(fact "splits a multi-runtime test namespace into per-language forms"
  (let [{:keys [shared by-lang]}
        (separate-runtime-test-forms runtime-test-forms [:js :lua])
        js-form (some #(when (= "identity function" (second %)) %) (get by-lang :js))
        lua-form (some #(when (= "identity function" (second %)) %) (get by-lang :lua))
        shared-out (render-top-level-forms shared)
        js-out (render-top-level-forms (get by-lang :js))
        lua-out (render-top-level-forms (get by-lang :lua))]
    [(str/includes? shared-out "xt.sample.base-lib-test")
     (str/includes? shared-out "(fact \"placeholder\")")
      (not (str/includes? shared-out "wrapped runtime form"))
      (str/includes? js-out "xtbench.js.sample.base-lib-test")
      (str/includes? js-out "(l/script- :js")
      (= true (:hidden (meta (nth js-form 2))))
      (str/includes? js-out "!.js")
      (not (str/includes? js-out "!.lua"))
      (str/includes? js-out "wrapped runtime form")
      (str/includes? js-out "vector runtime form")
      (str/includes? lua-out "xtbench.lua.sample.base-lib-test")
      (str/includes? lua-out "(l/script- :lua")
      (= true (:hidden (meta (nth lua-form 2))))
      (str/includes? lua-out "!.lua")
      (not (str/includes? lua-out "!.js"))
       (str/includes? lua-out "wrapped runtime form")
       (str/includes? lua-out "vector runtime form")])
  => [true true true true true true true true true true true true true true true true true])

^{:refer std.lang.manage.xtalk-scaffold/separate-runtime-test-forms :added "4.1"}
(fact "preserves top-level helpers and normalizes split runtime output"
  (let [{:keys [by-lang]}
        (separate-runtime-test-forms runtime-test-forms-with-top-level [:js :lua])
        js-out (render-top-level-forms (get by-lang :js))
        lua-out (render-top-level-forms (get by-lang :lua))]
    [(str/includes? js-out "(def +views+ (!.js (v/example)))")
     (str/includes? lua-out "(def +views+ (!.lua (v/example)))")
     (str/includes? js-out "(l/rt:scaffold :js)")
     (not (str/includes? js-out "(l/rt:scaffold :lua)"))
     (str/includes? lua-out "(l/rt:scaffold :lua)")
     (not (str/includes? lua-out "(l/rt:scaffold :js)"))
     (str/includes? lua-out "(def +tables+ (!.lua (v/example)))")
     (not (str/includes? lua-out "(def +tables+ (!.js"))])
  => [true true true true true true true true])

^{:refer std.lang.manage.xtalk-scaffold/canonical-runtime-source-test-ns :added "4.1"}
(fact "normalizes generated runtime namespaces back to canonical test namespaces"
  [(canonical-runtime-source-test-ns 'xtbench.js.sample.base-lib-test)
   (canonical-runtime-source-test-ns 'xt.sample.base-lib-js-test)
   (canonical-runtime-source-test-ns 'xt.sample.base-lib-test)]
  => '[xt.sample.base-lib-test
       xt.sample.base-lib-test
       xt.sample.base-lib-test])

^{:refer std.lang.manage.xtalk-scaffold/separate-runtime-tests :added "4.1"}
(fact "separate-runtime-tests is callable"
  (fn? separate-runtime-tests)
  => true)

^{:refer std.lang.manage.xtalk-scaffold/separate-runtime-tests :added "4.1"}
(fact "separate-runtime-tests does not overwrite the source seed by default"
  (with-temp-runtime-suite-file
    runtime-test-forms
    (fn [path]
      (let [before (slurp path)]
       (separate-runtime-tests nil {:input-path path
                                    :langs [:js :lua]
                                    :write true})
        (= before (slurp path)))))
  => true)

^{:refer std.lang.manage.xtalk-scaffold/separate-runtime-tests :added "4.1"}
(fact "separate-runtime-tests normalizes empty vector expectations for lua"
  (with-temp-runtime-suite-file
    lua-empty-vector-runtime-forms
    (fn [path]
      (let [{:keys [outputs]}
            (separate-runtime-tests nil {:input-path path
                                         :langs [:lua]
                                         :write true})
            lua-path (->> outputs
                          (filter #(= :lua (:lang %)))
                          first
                          :path)
            lua-output (slurp lua-path)]
        (boolean (re-find #"\{\"queued\"\s+\{\}\}" lua-output)))))
  => true)

^{:refer std.lang.manage.xtalk-scaffold/separate-runtime-tests :added "4.1"}
(fact "separate-runtime-tests drops nil-valued lua map entries from expectations"
  (with-temp-runtime-suite-file
    lua-nil-map-runtime-forms
    (fn [path]
      (let [{:keys [outputs]}
            (separate-runtime-tests nil {:input-path path
                                         :langs [:lua]
                                         :write true})
            lua-path (->> outputs
                          (filter #(= :lua (:lang %)))
                          first
                          :path)
            lua-output (slurp lua-path)]
        (and (str/includes? lua-output "{\"meta\" {\"listener/id\" \"b2\"}}")
             (not (str/includes? lua-output "\"pred\" nil"))))))
  => true)

^{:refer std.lang.manage.xtalk-scaffold/separate-runtime-tests :added "4.1"}
(fact "separate-runtime-tests keeps foreign runtime prefixes out of other language outputs"
  (with-temp-runtime-suite-file
    foreign-prefix-runtime-forms
    (fn [path]
      (let [{:keys [outputs]}
            (separate-runtime-tests nil {:input-path path
                                         :langs [:js :lua]
                                         :write true})
            js-output (->> outputs
                           (filter #(= :js (:lang %)))
                           first
                           :path
                           slurp)
            lua-output (->> outputs
                            (filter #(= :lua (:lang %)))
                            first
                            :path
                            slurp)]
        [(not (str/includes? js-output "string.format"))
         (= 1 (count (re-seq #"xt/x:json-encode 100000000000000000" js-output)))
         (str/includes? lua-output "string.format")])))
  => [true true true])

^{:refer std.lang.manage.xtalk-scaffold/scaffold-runtime-template :added "4.1"}
(fact "scaffold-runtime-template is callable"
  (fn? scaffold-runtime-template)
  => true)

^{:refer std.lang.manage.xtalk-scaffold/scaffold-runtime-template :added "4.1"}
(fact "scaffold-runtime-template supports input-path without :ns"
  (with-temp-runtime-suite-file
    runtime-template-forms
    (fn [path]
      (let [{:keys [source-ns target-ns from-lang lang content]}
            (scaffold-runtime-template nil {:input-path path
                                            :output-path (str path ".out")
                                            :lang :lua})]
        [source-ns
         target-ns
         from-lang
         lang
         (str/includes? content ":line")
         (str/includes? content ":column")])))
  => '[xtbench.js.sample.base-lib-test
       xtbench.lua.sample.base-lib-test
       :js
       :lua
        false
        false])

^{:refer std.lang.manage.xtalk-scaffold/scaffold-runtime-template :added "4.1"}
(fact "scaffold-runtime-template preserves helper runtime configs on non-target scripts"
  (with-temp-runtime-suite-file
    template-forms-with-helper-runtime
    (fn [path]
      (let [{:keys [content]}
            (scaffold-runtime-template nil {:input-path path
                                            :output-path (str path ".out")
                                            :lang :js})]
        [(str/includes? content "(l/script-\n :postgres\n {:runtime :jdbc.client")
         (str/includes? content "(l/script-\n :js\n {:runtime :basic") ])))
  => [true true])

^{:refer std.lang.manage.xtalk-scaffold/scaffold-runtime-template :added "4.1"}
(fact "scaffold-runtime-template supports namespace patterns for batch generation"
  (with-temp-xtlang-root
    {"test/xt/lang/common_lib_test.clj" canonical-runtime-template-forms
     "test/xt/lang/common_notify_test.clj" blocked-runtime-template-forms}
    (fn [root]
      (let [{:keys [pattern lang count outputs]}
            (scaffold-runtime-template nil {:root root
                                            :input-root "test/xt/lang"
                                            :ns 'xt.lang.*
                                            :lang :dart})]
        [pattern
         lang
         count
         (mapv :source-ns outputs)
         (mapv :target-ns outputs)])))
  => '["xt.lang.*"
        :dart
        1
        [xt.sample.base-lib-test]
        [xtbench.dart.sample.base-lib-test]])

^{:refer std.lang.manage.xtalk-scaffold/scaffold-runtime-template :added "4.1"}
(fact "scaffold-runtime-template preserves source formatting"
  (with-temp-runtime-source-file
    "(ns xtbench.js.sample.base-lib-test\n  (:require [std.lang :as l]\n            [xt.lang.common-lib :as k])\n  (:use code.test))\n\n(l/script- :js {:runtime :basic})\n\n(fact:global {:setup [(l/rt:restart)]})\n\n^{:refer xt.lang.common-lib/identity :added \"4.0\"}\n(fact \"identity function\"\n  ^:hidden\n  (!.js (k/identity 1))\n  => 1)\n"
    (fn [path]
      (let [{:keys [content]}
            (scaffold-runtime-template nil {:input-path path
                                            :output-path (str path ".out")
                                            :lang :lua})]
        [(str/includes? content "(fact \"identity function\"\n  ^:hidden\n  (!.lua (k/identity 1))\n  => 1)")
         (str/includes? content "(l/script- :lua {:runtime :basic})")
         (not (str/includes? content "(fact \"identity function\" (!.lua"))])))
  => [true true true])

^{:refer std.lang.manage.xtalk-scaffold/scaffold-runtime-template :added "4.1"}
(fact "scaffold-runtime-template keeps the real common_lib seed scaffoldable for js and lua"
  (let [{js-content :content
         source-ns :source-ns
         from-lang :from-lang
         js-target-ns :target-ns}
        (scaffold-runtime-template nil {:input-path "test/xt/lang/common_lib_test.clj"
                                        :lang :js})
        {lua-content :content
         lua-target-ns :target-ns}
        (scaffold-runtime-template nil {:input-path "test/xt/lang/common_lib_test.clj"
                                        :lang :lua})]
    [source-ns
     from-lang
     js-target-ns
     (str/includes? js-content "xtbench.js.lang.common-lib-test")
     (str/includes? js-content "xt.lang.common-lib/proto-create")
     (str/includes? js-content "(k/proto-set a mt nil)")
     lua-target-ns
     (str/includes? lua-content "xtbench.lua.lang.common-lib-test")
     (str/includes? js-content ":require [[xt.lang.common-lib :as k]]")
     (str/includes? js-content "xt.lang.common-lib/type-native")
     (str/includes? js-content "xt.lang.common-lib/wrap-callback")
     (> (count (re-seq #"\(fact " js-content)) 20)])
  => '[xt.lang.common-lib-test
       :lua
       xtbench.js.lang.common-lib-test
       true
       true
       true
       xtbench.lua.lang.common-lib-test
       true
       true
       true
       true
       true])

^{:refer std.lang.manage.xtalk-scaffold/scaffold-runtime-template :added "4.1"}
(fact "scaffold-runtime-template renders the real common_lib seed for python and dart"
  (let [{py-target-ns :target-ns
         py-content :content}
        (scaffold-runtime-template nil {:input-path "test/xt/lang/common_lib_test.clj"
                                        :lang :python})
        {dt-target-ns :target-ns
         dt-content :content}
        (scaffold-runtime-template nil {:input-path "test/xt/lang/common_lib_test.clj"
                                        :lang :dart})]
    [py-target-ns
     (str/includes? py-content "xtbench.python.lang.common-lib-test")
     (not (str/includes? py-content "xt.lang.common-lib/proto-create"))
     (not (str/includes? py-content "(k/proto-set a mt nil)"))
     dt-target-ns
     (str/includes? dt-content "xtbench.dart.lang.common-lib-test")
     (not (str/includes? dt-content "xt.lang.common-lib/proto-create"))
     (not (str/includes? dt-content "xt.lang.common-lib/proto-set"))])
  => '[xtbench.python.lang.common-lib-test
       true
       true
       true
       xtbench.dart.lang.common-lib-test
       true
       true
       true])

^{:refer std.lang.manage.xtalk-scaffold/scaffold-runtime-template :added "4.1"}
(fact "scaffold-runtime-template drops unsupported common_data facts for python and dart"
  (let [{py-target-ns :target-ns
         py-content :content}
        (scaffold-runtime-template nil {:input-path "test/xt/lang/common_data_test.clj"
                                        :lang :python})
        {dt-target-ns :target-ns
         dt-content :content}
        (scaffold-runtime-template nil {:input-path "test/xt/lang/common_data_test.clj"
                                        :lang :dart})]
    [py-target-ns
     (str/includes? py-content "xtbench.python.lang.common-data-test")
     (str/includes? py-content "xt.lang.common-data/from-flat")
     (str/includes? py-content "xt.lang.common-data/memoize-key")
     (not (str/includes? py-content ":lang-exceptions"))
     dt-target-ns
     (str/includes? dt-content "xtbench.dart.lang.common-data-test")
     (not (str/includes? dt-content "xt.lang.common-data/arr-tail"))
     (not (str/includes? dt-content "xt.lang.common-data/clone-nested"))
     (not (str/includes? dt-content ":lang-exceptions"))])
  => '[xtbench.python.lang.common-data-test
        true
        true
        false
        true
        xtbench.dart.lang.common-data-test
        true
        true
        true
        true])

^{:refer std.lang.manage.xtalk-scaffold/scaffold-runtime-template :added "4.1"}
(fact "scaffold-runtime-template renders the real common_math seed for python and dart"
  (let [{py-target-ns :target-ns
         py-content :content}
        (scaffold-runtime-template nil {:input-path "test/xt/lang/common_math_test.clj"
                                        :lang :python})
        {dt-target-ns :target-ns
         dt-content :content}
        (scaffold-runtime-template nil {:input-path "test/xt/lang/common_math_test.clj"
                                        :lang :dart})]
    [py-target-ns
     (str/includes? py-content "xtbench.python.lang.common-math-test")
     (str/includes? py-content "xt.lang.common-math/log10")
     (str/includes? py-content "xt.lang.common-math/round")
     dt-target-ns
     (str/includes? dt-content "xtbench.dart.lang.common-math-test")
     (str/includes? dt-content "xt.lang.common-math/log10")
     (str/includes? dt-content "xt.lang.common-math/round")])
  => '[xtbench.python.lang.common-math-test
       true
       true
       true
       xtbench.dart.lang.common-math-test
       true
       true
       true])

^{:refer std.lang.manage.xtalk-scaffold/diagnose-runtime-generation :added "4.1"}
(fact "diagnoses split generation rewrites and expected outputs"
  (with-temp-runtime-suite-file
    runtime-test-forms-with-top-level
    (fn [path]
      (let [{:keys [mode expected-success? script-langs outputs warnings unsupported summary]}
            (diagnose-runtime-generation nil {:input-path path
                                              :langs [:js :lua]})]
        [mode
         expected-success?
         script-langs
         (mapv (juxt :lang :status :script-present :fact-count) outputs)
         (set (map :code warnings))
         unsupported
         (:rewritten-count summary)])))
  => '[:split
       true
       [:js :lua]
       [[:js :expected-pass true 2]
        [:lua :expected-pass true 2]]
       #{:normalized-runtime-setup
         :retargeted-top-level-runtime
         :retargeted-runtime-prefix}
       []
       3])

^{:refer std.lang.manage.xtalk-scaffold/diagnose-runtime-generation :added "4.1"}
(fact "diagnoses unsupported split seed patterns"
  (with-temp-runtime-suite-file
    runtime-test-forms-with-unsupported
    (fn [path]
      (let [{:keys [mode expected-success? unsupported]}
            (diagnose-runtime-generation nil {:input-path path
                                              :langs [:js :lua]})]
        [mode
         expected-success?
         (set (map :code unsupported))])))
  => '[:split
       false
       #{:mixed-runtime-top-level
         :mixed-runtime-assertion}])

^{:refer std.lang.manage.xtalk-scaffold/diagnose-runtime-generation :added "4.1"}
(fact "diagnoses blocked template generation for twostep runtimes"
  (with-temp-runtime-suite-file
    blocked-runtime-template-forms
    (fn [path]
      (let [{:keys [mode from-lang requested-lang expected-success? unsupported]}
            (diagnose-runtime-generation nil {:input-path path
                                              :lang :dart})]
        [mode
         from-lang
         requested-lang
         expected-success?
         (mapv :code unsupported)
         (-> unsupported first :blockers)])))
  => '[:template
        :js
        :dart
        true
        []
        nil])

^{:refer std.lang.manage.xtalk-scaffold/diagnose-runtime-generation :added "4.1"}
(fact "diagnoses the real common_lib seed as portable across generated runtimes"
  (let [{:keys [mode from-lang requested-lang expected-success? eligible-langs unsupported]}
        (diagnose-runtime-generation nil {:input-path "test/xt/lang/common_lib_test.clj"
                                          :lang :js})]
    [mode
     from-lang
     requested-lang
     expected-success?
     eligible-langs
     unsupported])
  => '[:template
       :lua
       :js
       true
       [:js :lua :python :r :php :dart]
       []])

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
          (= 'xt.sample.base-lib-test (:ns source))
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
  (canonical-case-id 'xt.sample.base-lib-test "identity function" 2)
  => "xt.sample.base-lib-test::identity-function::2")

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
        cases (fact-runtime-cases 'xt.sample.base-lib-test fact-form :js)]
    [(count cases)
     (:id (first cases))
     (:form (first cases))
     (:expect (first cases))
     (:exceptions (first cases))])
  => [1
      "xt.sample.base-lib-test::identity-function::0"
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
  => '[xt.sample.base-lib-test
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
  [(transform-script-runtime '(l/script- :js {:runtime :basic :layout :flat}) :js)
   (transform-script-runtime '(l/script- :js {:runtime :basic :layout :flat}) :dart)
   (transform-script-runtime '(l/script- :dart {:runtime :basic :layout :flat}) :dart)]
  => '[(l/script- :js {:runtime :basic :layout :flat})
       (l/script- :js {:runtime :basic :layout :flat})
       (l/script- :dart {:runtime :twostep :layout :flat})])
