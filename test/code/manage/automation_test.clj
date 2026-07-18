(ns code.manage.automation-test
  (:require [code.manage.automation :as automation]
            [code.manage.unit.template :as template]
            [code.project :as project])
  (:use code.test))

^{:refer code.manage.automation/sha256 :added "4.1"}
(fact "creates a stable lowercase digest"
  (automation/sha256 "foundation")
  => "2bd8b7b0d83e0ec92abdf6f714ca199a7874d08636965471035c4dcd1b38b6fb")

^{:refer code.manage.automation/finding-id :added "4.1"}
(fact "finding identity does not depend on source line numbers"
  (automation/finding-id :missing-test 'code.sample/value)
  => "missing-test:code.sample/value")

^{:refer code.manage.automation/section-namespaces :added "4.1"}
(fact "section selectors respect namespace boundaries and explicit dotted prefixes"
  (with-redefs [template/source-namespaces
                (fn [_ _] '[code code.manage codec xt.lang xtbench.lua])]
    [(automation/section-namespaces '[code] {})
     (automation/section-namespaces '[xt.] {})])
  => [['code 'code.manage]
      ['xt.lang]])

^{:refer code.manage.automation/baseline-diff :added "4.1"}
(fact "classifies baseline, new, and resolved findings"
  (automation/baseline-diff ["missing:a" "missing:c"]
                            [{:id "missing:a"} {:id "todo:b"}])
  => {:baseline ["missing:a"]
      :new ["todo:b"]
      :resolved ["missing:c"]})

^{:refer code.manage.automation/report :added "4.1"}
(fact "new-only policy gates only findings outside the reviewed baseline"
  (with-redefs [automation/load-config
                (fn [_] {:schema-version 1
                         :policy :new-only
                         :sections {:code {:selector '[code]
                                           :test-command "lein test :with [code]"}}})
                project/file-lookup (fn [_] {})
                automation/section-namespaces (fn [_ _] '[code.sample])
                automation/namespace-findings
                (fn [_ _ _] [{:id "missing-test:code.sample/value"
                               :kind :missing-test}])]
    (select-keys (automation/report :code
                                    {:project {:root "." :name 'foundation-base}})
                 [:section :policy :counts :exit]))
  => {:section "code"
      :policy "new-only"
      :counts {:total 1 :baseline 0 :unchanged 0 :new 1 :resolved 0}
      :exit 1})
