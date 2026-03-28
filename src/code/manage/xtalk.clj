(ns code.manage.xtalk
  (:require [code.manage.xtalk-audit :as audit]
            [code.manage.xtalk-ops :as ops]
            [code.manage.xtalk-scaffold :as scaffold]
            [std.lib.foundation :as f]))

(f/intern-in
 audit/xtalk-categories
 audit/xtalk-op-map
 audit/xtalk-symbols
 audit/installed-languages
 audit/audit-languages
 audit/feature-status
 audit/support-matrix
 audit/missing-by-language
 audit/missing-by-feature
 audit/visualize-support
 ops/var->symbol
 ops/xtalk-category-map
 ops/read-xtalk-ops
 ops/compact-entry
 ops/symbol-doc
 ops/inventory-entry
 ops/inventory-entries
 ops/ops-path
 ops/render-xtalk-ops
 ops/generate-xtalk-ops
 scaffold/quoted-form-string
 scaffold/grammar-entry?
 scaffold/grammar-entries
 scaffold/macro-added
 scaffold/case-xtalk-expect
 scaffold/render-grammar-assertion
 scaffold/render-grammar-fact
 scaffold/render-grammar-test-file
 scaffold/grammar-test-path
 scaffold/scaffold-xtalk-grammar-tests
 scaffold/read-top-level-forms
 scaffold/runtime-expr-lang
 scaffold/normalize-runtime-lang
 scaffold/runtime-lang-config
 scaffold/runtime-script-lang
 scaffold/runtime-dispatch-symbol
 scaffold/runtime-lang-suffix
 scaffold/fact-form?
 scaffold/fact-global-form?
 scaffold/script-form?
 scaffold/expand-top-level-form
 scaffold/replace-ns-name
 scaffold/runtime-test-ns
 scaffold/infer-runtime-lang
 scaffold/replace-runtime-symbol
 scaffold/transform-script-form
 scaffold/template-runtime-test-ns
 scaffold/template-runtime-test-forms
 scaffold/render-top-level-forms
 scaffold/split-fact-form
 scaffold/separate-runtime-test-forms
 scaffold/separate-runtime-tests
 scaffold/scaffold-runtime-template)
