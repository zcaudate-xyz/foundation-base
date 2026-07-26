(ns tahto.core.script-macro-provenance-test
  (:require [tahto.base.book-module :as module]
            [tahto.core.impl :as impl]
            [tahto.core.library :as lib]
            [tahto.core.pointer :as ptr]
            [tahto.core.runtime :as rt]
            [tahto.core.script-macro :as macro]
tahto/lang/script_macro_provenance_test.clj:8:            [tahto.model.spec-xtalk :as xtalk])
  (:use code.test))

(rt/install-lang! :xtalk)

(defn- xtalk-top-level-context
  []
  (let [xlib (lib/library {})]
    (lib/add-book! xlib (assoc xtalk/+book+ :modules {}))
    (lib/add-module! xlib (module/book-module {:lang :xtalk
                                               :id 'xt.lang.common-lib}))
    [xlib
     ['defn (get-in (lib/get-book xlib :xtalk)
                    [:grammar :reserved 'defn])]]))

(defn- capture-provenance
  [f]
  (try
    (f)
    nil
    (catch Throwable t
      (let [data (ex-data t)]
tahto/lang/script_macro_provenance_test.clj:30:        {:phase     (:tahto/phase data)
tahto/lang/script_macro_provenance_test.clj:31:         :subsystem (:tahto/subsystem data)
tahto/lang/script_macro_provenance_test.clj:32:         :module    (:tahto/module data)
tahto/lang/script_macro_provenance_test.clj:33:         :entry     (-> data :tahto/entry :symbol)
tahto/lang/script_macro_provenance_test.clj:34:         :line      (:tahto/line data)
tahto/lang/script_macro_provenance_test.clj:35:         :form      (:tahto/form data)
tahto/lang/script_macro_provenance_test.clj:36:         :stack     (mapv (juxt :tahto/phase
tahto/lang/script_macro_provenance_test.clj:37:                                :tahto/subsystem)
tahto/lang/script_macro_provenance_test.clj:38:                          (:tahto/provenance-stack data))}))))

(fact "defn.xt failures report provenance for abstract calls"

  (let [[xlib reserved] (xtalk-top-level-context)
        bad-call (with-meta '(x:type-native obj)
                   {:line 330})
        fn-form  (with-meta
                   (list 'defn.xt
                         'fail-type-native
                         '[obj]
                         (list 'return bad-call))
                   {:module 'xt.lang.common-lib
                    :line 320})]
    (impl/with:library [xlib]
      (let [fn-var (macro/intern-top-level-fn
                    :xtalk
                    reserved
                    fn-form
                    {})
            out    (capture-provenance #(ptr/ptr-display @fn-var {}))]
        [(select-keys out [:phase
                           :subsystem
                           :module
                           :entry
                           :line
                           :form])
         [(first (:stack out))
          (last (:stack out))]])))
  => '[{:phase :emit/form
tahto/lang/script_macro_provenance_test.clj:68:        :subsystem :tahto.common.emit-top-level/emit-form
        :module xt.lang.common-lib
        :entry xt.lang.common-lib/fail-type-native
        :line 320
        :form (x:type-native obj)}
tahto/lang/script_macro_provenance_test.clj:73:       [[:emit/form :tahto.common.emit-top-level/emit-form]
        [:emit/entry :tahto.core.impl-entry/emit-entry-raw]]])

(fact "defn.xt failures keep entry provenance when nested forms explode"

  (let [[xlib reserved] (xtalk-top-level-context)
        bad-call  (with-meta '(x:arr-push out (f e))
                    {:line 347})
        loop-form (list 'for:array
                        '[e arr]
                        bad-call)
        fn-form   (with-meta
                    (list 'defn.xt
                          'fail-arr-push
                          '[arr f]
                          '(var out := [])
                          loop-form
                          '(return out))
                    {:module 'xt.lang.common-lib
                     :line 340})]
    (impl/with:library [xlib]
      (let [fn-var (macro/intern-top-level-fn
                    :xtalk
                    reserved
                    fn-form
                    {})
            out    (capture-provenance #(ptr/ptr-display @fn-var {}))]
        [(select-keys out [:phase
                           :subsystem
                           :module
                           :entry
                           :line
                           :form])
         [(first (:stack out))
          (last (:stack out))]])))
  => '[{:phase :emit/form
tahto/lang/script_macro_provenance_test.clj:109:        :subsystem :tahto.common.emit-top-level/emit-form
        :module xt.lang.common-lib
        :entry xt.lang.common-lib/fail-arr-push
        :line 340
        :form (x:arr-push out (f e))}
tahto/lang/script_macro_provenance_test.clj:114:       [[:emit/form :tahto.common.emit-top-level/emit-form]
        [:emit/entry :tahto.core.impl-entry/emit-entry-raw]]])
