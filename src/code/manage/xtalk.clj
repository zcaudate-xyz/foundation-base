(ns code.manage.xtalk
    (:require [std.lang.manage]))

(defn- call-manage
    [sym args]
    (if-let [v (ns-resolve 'std.lang.manage sym)]
        (apply v args)
        (throw (ex-info "Missing std.lang.manage compatibility target"
                                        {:symbol sym}))))

(defn xtalk-model-inventory
    [& args]
    (call-manage 'xtalk-model-inventory args))

(defn xtalk-runtime-inventory
    [& args]
    (call-manage 'xtalk-runtime-inventory args))

(defn xtalk-spec-inventory
    [& args]
    (call-manage 'xtalk-spec-inventory args))

(defn xtalk-test-inventory
    [& args]
    (call-manage 'xtalk-test-inventory args))

(defn xtalk-language-status
    [& args]
    (call-manage 'xtalk-language-status args))

(defn xtalk-coverage-summary
    [& args]
    (call-manage 'xtalk-coverage-summary args))

(defn xtalk-status
    [& args]
    (call-manage 'xtalk-status args))

(defn xtalk-model-status
    [& args]
    (call-manage 'xtalk-model-status args))

(defn xtalk-runtime-status
    [& args]
    (call-manage 'xtalk-runtime-status args))

(defn xtalk-spec-status
    [& args]
    (call-manage 'xtalk-spec-status args))

(defn xtalk-test-status
    [& args]
    (call-manage 'xtalk-test-status args))

(defn xtalk-categories
    [& args]
    (call-manage 'xtalk-categories args))

(defn xtalk-op-map
    [& args]
    (call-manage 'xtalk-op-map args))

(defn xtalk-symbols
    [& args]
    (call-manage 'xtalk-symbols args))

(defn installed-languages
    [& args]
    (call-manage 'installed-languages args))

(defn audit-languages
    [& args]
    (call-manage 'audit-languages args))

(defn support-matrix
    [& args]
    (call-manage 'support-matrix args))

(defn missing-by-language
    [& args]
    (call-manage 'missing-by-language args))

(defn missing-by-feature
    [& args]
    (call-manage 'missing-by-feature args))

(defn visualize-support
    [& args]
    (call-manage 'visualize-support args))

(defn generate-xtalk-ops
    [& args]
    (call-manage 'generate-xtalk-ops args))

(defn scaffold-xtalk-grammar-tests
    [& args]
    (call-manage 'scaffold-xtalk-grammar-tests args))

(defn separate-runtime-tests
    [& args]
    (call-manage 'separate-runtime-tests args))

(defn scaffold-runtime-template
    [& args]
    (call-manage 'scaffold-runtime-template args))

(defn export-runtime-suite
    [& args]
    (call-manage 'export-runtime-suite args))

(defn compile-runtime-bulk
    [& args]
    (call-manage 'compile-runtime-bulk args))
