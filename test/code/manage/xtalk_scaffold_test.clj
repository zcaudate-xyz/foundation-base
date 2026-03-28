(ns code.manage.xtalk-scaffold-test
  (:require [clojure.string :as str]
            [code.manage.xtalk-scaffold :as xtalk-scaffold])
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

(fact "recognizes grammar-backed xtalk entries"
  (xtalk-scaffold/grammar-entry? +grammar-entry+)
  => true)

(fact "renders grammar xtalk tests from canonical cases"
  (let [out (xtalk-scaffold/render-grammar-test-file [+grammar-entry+])]
    [(str/includes? out "(ns std.lang.base.grammar-xtalk-ops-test")
     (str/includes? out "tf-get-key")
     (str/includes? out "(tf-get-key '(x:get-key obj \"a\"))")
     (str/includes? out "=> '(. obj [\"a\"])")
     (str/includes? out "(tf-get-key '(x:get-key obj \"a\" \"DEFAULT\"))")])
  => [true true true true true])

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

(fact "splits a multi-runtime test namespace into per-language forms"
  (let [{:keys [shared by-lang]}
        (xtalk-scaffold/separate-runtime-test-forms runtime-test-forms [:js :lua])
        js-form (some #(when (= "identity function" (second %)) %) (get by-lang :js))
        lua-form (some #(when (= "identity function" (second %)) %) (get by-lang :lua))
        shared-out (xtalk-scaffold/render-top-level-forms shared)
        js-out (xtalk-scaffold/render-top-level-forms (get by-lang :js))
        lua-out (xtalk-scaffold/render-top-level-forms (get by-lang :lua))]
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

(fact "templates a runtime test from js to ruby"
  (let [out-forms (xtalk-scaffold/template-runtime-test-forms runtime-template-forms :js :ruby)
        out (xtalk-scaffold/render-top-level-forms out-forms)]
    [(= :rb (xtalk-scaffold/normalize-runtime-lang :ruby))
     (= 'xt.lang.base-lib-rb-test
        (second (first out-forms)))
     (= :ruby (second (second out-forms)))
     (str/includes? out "xt.lang.base-lib-rb-test")
     (str/includes? out "(l/script- :ruby")
     (str/includes? out "!.rb")
     (not (str/includes? out "!.js"))
     (str/includes? out ":refer xt.lang.base-lib/identity")])
  => [true true true true true true true true])
