(ns std.lang.manage.xtalk-scaffold-test
  (:require [clojure.string :as str]
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
   "[(ns xt.lang.base-lib-test
       (:require [std.lang :as l]
                 [xt.lang.base-lib :as k])
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
   "[(ns xt.lang.base-lib-js-test
       (:require [std.lang :as l]
                 [xt.lang.base-lib :as k])
       (:use code.test))
      (l/script- :js {:runtime :basic})
      (fact:global {:setup [(l/rt:restart)]})
      ^{:refer xt.lang.base-lib/identity :added \"4.0\"}
      (fact \"identity function\"
        ^:hidden
        (!.js (k/identity 1))
        => 1)
      (fact \"wrapped runtime form\"
        (set (!.js (k/example)))
        => #{1})]"))

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
  (runtime-test-ns 'xt.lang.base-lib-test :js)
  => 'xt.lang.base-lib-js-test)

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
  => true)

^{:refer std.lang.manage.xtalk-scaffold/infer-runtime-lang :added "4.1"}
(fact "infers runtime language from forms"
  (infer-runtime-lang '((l/script- :js {:runtime :basic})))
  => :js)

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
  (template-runtime-test-ns 'xt.lang.base-lib-js-test :js :rb)
  => 'xt.lang.base-lib-rb-test)

^{:refer std.lang.manage.xtalk-scaffold/template-runtime-test-forms :added "4.1"}
(fact "templates a runtime test from js to ruby"
  (let [out-forms (template-runtime-test-forms runtime-template-forms :js :ruby)
        out (render-top-level-forms out-forms)]
    [(= :rb (normalize-runtime-lang :ruby))
     (= 'xt.lang.base-lib-rb-test
        (second (first out-forms)))
     (= :ruby (second (second out-forms)))
     (str/includes? out "xt.lang.base-lib-rb-test")
     (str/includes? out "(l/script- :ruby")
     (str/includes? out "!.rb")
     (not (str/includes? out "!.js"))
     (str/includes? out ":refer xt.lang.base-lib/identity")])
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
    [(str/includes? shared-out "(ns xt.lang.base-lib-test")
     (str/includes? shared-out "(fact \"placeholder\")")
     (not (str/includes? shared-out "wrapped runtime form"))
     (str/includes? js-out "(ns xt.lang.base-lib-js-test")
     (str/includes? js-out "(l/script- :js")
     (= true (:hidden (meta (nth js-form 2))))
     (str/includes? js-out "!.js")
     (not (str/includes? js-out "!.lua"))
     (str/includes? js-out "wrapped runtime form")
     (str/includes? js-out "vector runtime form")
     (str/includes? lua-out "(ns xt.lang.base-lib-lua-test")
     (str/includes? lua-out "(l/script- :lua")
     (= true (:hidden (meta (nth lua-form 2))))
     (str/includes? lua-out "!.lua")
     (not (str/includes? lua-out "!.js"))
     (str/includes? lua-out "wrapped runtime form")
     (str/includes? lua-out "vector runtime form")])
  => [true true true true true true true true true true true true true true true true true])

^{:refer std.lang.manage.xtalk-scaffold/separate-runtime-tests :added "4.1"}
(fact "separate-runtime-tests is callable"
  (fn? separate-runtime-tests)
  => true)

^{:refer std.lang.manage.xtalk-scaffold/scaffold-runtime-template :added "4.1"}
(fact "scaffold-runtime-template is callable"
  (fn? scaffold-runtime-template)
  => true)
