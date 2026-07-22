(ns hara.typed.xtalk-lint-test
  (:use code.test)
  (:require [hara.typed.xtalk-lint :refer :all]))

(defn fixture
  [form]
  (with-meta form {:file "fixture.clj" :line 12 :column 4}))

^{:refer hara.typed.xtalk-lint/canonical-head :added "4.1"}
(fact "canonicalizes namespaced XTalk heads"
  [(canonical-head 'xt/x:get-key)
   (canonical-head 'x:get-key)
   (canonical-head :?)]
  => '[x:get-key x:get-key :?])

^{:refer hara.typed.xtalk-lint/lint-form :added "4.1"}
(fact "rejects block forms in value positions"
  (let [errors (lint-form (fixture '(var value (if test 1 2)))
                          :statement
                          {:file "fixture.clj"})]
    [(mapv :code errors)
     (select-keys (-> errors first :loc) [:file :line])
     (-> errors first :message)])
  => '[[:XT001]
       {:file "fixture.clj" :line 18}
       "block form if is not valid in value position; use :? for value conditionals"])

^{:refer hara.typed.xtalk-lint/lint-form :added "4.1" :id canonical-conditional}
(fact "accepts value conditionals"
  (lint-form (fixture '(var value (:? test 1 2))) :statement {})
  => [])

^{:refer hara.typed.xtalk-lint/lint-form :added "4.1" :id redundant-access}
(fact "suggests dot access for simple x-get-key forms"
  (let [warnings (lint-form (fixture '(return (xt/x:get-key m "name")))
                            :statement {})]
    [(mapv :code warnings)
     (:suggestion (first warnings))])
  => '[[:XT002]
       (. m ["name"])])

^{:refer hara.typed.xtalk-lint/lint-form :added "4.1" :id nested-dot-access}
(fact "flattens nested dot access chains"
  (let [form '(. (. (. a ["name"]) ["hello"]) (run 1 2 3))
        diagnostics (lint-form form :statement {})]
    [(mapv :code diagnostics)
     (:suggestion (first diagnostics))])
  => '[[:XT007]
       (. a ["name"] ["hello"] (run 1 2 3))])

^{:refer hara.typed.xtalk-lint/lint-form :added "4.1" :id defaulted-access}
(fact "keeps explicit x-get-key defaults"
  (lint-form '(return (xt/x:get-key m "name" "unknown")) :statement {})
  => [])

^{:refer hara.typed.xtalk-lint/lint-form :added "4.1" :id fn-arrow-canonical}
(fact "suggests canonical fn for nil fn:> callbacks"
  (let [diagnostics (lint-form
                     (fixture '(fn:> [id data t meta] nil))
                     :value
                     {})
        canonical (lint-form
                   '(fn [id data t meta] (return nil))
                   :value
                   {})]
    [(mapv :code diagnostics)
     (:suggestion (first diagnostics))
     canonical])
  => '[[:XT003]
       (fn [id data t meta] (return nil))
       []])

^{:refer hara.typed.xtalk-lint/lint-top-form :added "4.1" :id fn-arrow-fact}
(fact "finds nil fn:> callbacks inside facts"
  (let [diagnostics (lint-top-form
                     '(fact "callback" (!.js (fn:> [id data t meta] nil)))
                     {:file "fixture.clj"})]
    [(mapv :code diagnostics)
     (:suggestion (first diagnostics))])
  => '[[:XT003]
       (fn [id data t meta] (return nil))])

^{:refer hara.typed.xtalk-lint/lint-top-form :added "4.1" :id nested-dot-fact}
(fact "finds nested dot access inside facts"
  (let [diagnostics (lint-top-form
                     '(fact "nested access"
                        (. (. (. a ["name"]) ["hello"]) (run 1 2 3)))
                     {})]
    [(mapv :code diagnostics)
     (:suggestion (first diagnostics))])
  => '[[:XT007]
       (. a ["name"] ["hello"] (run 1 2 3))])

^{:refer hara.typed.xtalk-lint/lint-form :added "4.1" :id destructuring-collision}
(fact "detects destructuring field collisions after snake-case normalization"
  (let [errors (lint-form '(var #{arr-name arr_name} m) :statement {})]
    [(mapv :code errors)
     (:field (first errors))
     (:canonical (first errors))])
  => '[[:XT004] "arr_name" arr-name])

^{:refer hara.typed.xtalk-lint/simple-destructuring-source? :added "4.1"}
(fact "only permits repeatable sources for direct destructuring"
  [(simple-destructuring-source? 'containers)
   (simple-destructuring-source? '(. containers ["listeners"]))
   (simple-destructuring-source? '(get-containers))
   (simple-destructuring-source? '(. (get-containers) ["listeners"]))]
  => '[true true false false])

^{:refer hara.typed.xtalk-lint/lint-form :added "4.1" :id destructuring-source}
(fact "makes destructuring source restrictions explicit"
  (let [set-diagnostics (lint-form '(var #{listeners} (get-containers)) :statement {})
        vec-diagnostics (lint-form '(var [a b] (get-values)) :statement {})]
    [(mapv :code set-diagnostics)
     (mapv :code vec-diagnostics)
     (:message (first set-diagnostics))
     (lint-form '(var #{listeners} containers) :statement {})
     (lint-form '(var [a b] (. values ["items"])) :statement {})])
  => '[[:XT006]
       [:XT006]
       "destructuring var sources must be a symbol or a simple dot access (. symbol [key]); bind complex expressions to a symbol first"
       []
       []])

^{:refer hara.typed.xtalk-lint/lint-form :added "4.1" :id loop-context}
(fact "treats namespaced loop macros as statement contexts"
  (lint-form '(xt/for:array [e arr]
                (when test
                  (x:arr-push out e)))
             :statement {})
  => [])

^{:refer hara.typed.xtalk-lint/lint-file :added "4.1"}
(fact "lints a real XTalk source file with inherited file locations"
  (let [diagnostics (lint-file "src-lang/xt/lang/common_data.clj")]
    [(count diagnostics)
     (set (map :code diagnostics))
     (set (map :file (map :loc diagnostics)))])
  => [0 #{} #{}])

^{:refer hara.typed.xtalk-lint/summarize :added "4.1"}
(fact "summarizes diagnostics by severity and code"
  (summarize [{:code :XT001 :severity :error}
              {:code :XT002 :severity :warning}
              {:code :XT002 :severity :warning}])
  => {:total 3
      :errors 1
      :warnings 2
      :codes {:XT001 1 :XT002 2}})
