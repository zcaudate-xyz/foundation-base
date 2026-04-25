(ns std.lang.base.script-plugin-test
  (:require [std.lang.base.runtime :as rt]
            [std.lang.base.script-plugin :as plugin]
            [std.lang.model.spec-js :as js])
  (:use code.test))
(rt/install-lang! :js)

^{:refer std.lang.base.script-plugin/get-plugin :added "4.0"}
(fact "gets a registered script support plugin"
  (plugin/get-plugin :defvar)
  => map?)

^{:refer std.lang.base.script-plugin/plugin-symbol :added "4.0"}
(fact "derives support macro symbols from grammar tags"
  (plugin/plugin-symbol :defvar js/+grammar+)
  => 'defvar.js)

^{:refer std.lang.base.script-plugin/defvar-fn :added "4.0"}
(fact "creates getter and reset forms for defvar support"
  (let [out (plugin/defvar-fn '(defvar.js JS_SAMPLE [] (return 1))
                              "js"
                              'JS_SAMPLE
                              nil
                              nil
                              '([]
                                (return 1)))]
    [(-> out first first)
     (-> out first second)
     (-> out second first)
     (-> out second second)])
  => '[defn.js JS_SAMPLE defn.js JS_SAMPLE-reset])

^{:refer std.lang.base.script-plugin/intern-plugins :added "4.0"}
(fact "interns declared support plugins"
  (let [ns-sym 'std.lang.base.script-plugin-test.support
        _      (when (find-ns ns-sym)
                 (remove-ns ns-sym))
        _      (create-ns ns-sym)]
    (try
      (binding [*ns* (the-ns ns-sym)]
        (refer 'clojure.core)
        (plugin/intern-plugins :js js/+grammar+ [:defvar]))
      (finally
        (remove-ns ns-sym))))
  => '[#{defvar.js} #{defvar.js}]

  (let [ns-sym 'std.lang.base.script-plugin-test.manual
        _      (when (find-ns ns-sym)
                 (remove-ns ns-sym))
        _      (create-ns ns-sym)]
    (try
      (binding [*ns* (the-ns ns-sym)]
        (refer 'clojure.core)
        (require '[xt.lang.common-runtime :as rt :refer [defvar.js]])
        [(plugin/intern-plugins :js js/+grammar+ [:defvar])
         (-> (macroexpand-1 '(defvar.js JS_SAMPLE [] (return 1)))
             first
             first)])
      (finally
        (remove-ns ns-sym))))
  => '[[#{} #{defvar.js}] defn.js])
