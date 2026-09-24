(ns lang.core.global-init-test
  (:use code.test)
  (:require [clojure.string :as string]
            [lang.base.book-module :as module]
            [lang.core :as l]
            [lang.core.impl :as impl]
            [lang.core.impl-entry :as entry]
            [lang.core.library :as lib]
            [lang.core.pointer :as pointer]
            [lang.core.script-macro :as macro]
            [lang.model.builtin.spec-js :as js]
            [lang.model.builtin.spec-xtalk :as xtalk]))

^{:refer lang.core.pointer/ptr-invoke-string :added "4.1"}
(fact "includes global initializer dependencies only under with:global-init"
  (let [library (doto (lib/library {})
                  (lib/add-book! (assoc xtalk/+book+ :modules {}))
                  (lib/add-book! (assoc js/+book+ :modules {})))
        options {:lang :js
                 :namespace 'lang.core.global-init-test
                 :module 'sample.ui}
        helper-entry (entry/create-code-base
                      '(defn make-sample [] (return 7))
                      options
                      (:grammar js/+book+))
        global-entry (entry/create-code-base
                      '(defglobal sample (-/make-sample))
                      options
                      (:grammar js/+book+))
        _ (lib/add-module!
           library
           (module/book-module
            {:lang :js
             :id 'sample.ui
             :code {(:id helper-entry) helper-entry
                    (:id global-entry) global-entry}}))
        _ (lib/add-module!
           library
           (module/book-module {:lang :js :id 'sample.main}))
        default (impl/with:library [library]
                  (macro/intern-!-fn :js ['sample.ui/sample] {}))
        binding-value (l/with:global-init pointer/*global-init*)
        initialized (impl/with:library [library]
                      (l/with:global-init
                        (macro/intern-!-fn :js ['sample.ui/sample] {})))]
    binding-value => true

    default
    => "globalThis[\"sample_ui$$sample\"]"

    (string/includes? initialized
                      "globalThis[\"sample_ui$$sample\"] = make_sample();")
    => true

    (string/includes? initialized "function make_sample(){")
    => true))
