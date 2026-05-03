(ns hara.lang.base.script-macro-provenance-test
  (:require [hara.lang.base.book-module :as module]
            [hara.lang.base.impl :as impl]
            [hara.lang.base.library :as lib]
            [hara.lang.base.pointer :as ptr]
            [hara.lang.base.runtime :as rt]
            [hara.lang.base.script-macro :as macro]
            [hara.lang.model.spec-xtalk :as xtalk])
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
        {:phase     (:hara.lang/phase data)
         :subsystem (:hara.lang/subsystem data)
         :module    (:hara.lang/module data)
         :entry     (-> data :hara.lang/entry :symbol)
         :line      (:hara.lang/line data)
         :form      (:hara.lang/form data)
         :stack     (mapv (juxt :hara.lang/phase
                                :hara.lang/subsystem)
                          (:hara.lang/provenance-stack data))}))))

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
        :subsystem :hara.lang.base.emit-top-level/emit-form
        :module xt.lang.common-lib
        :entry xt.lang.common-lib/fail-type-native
        :line 320
        :form (x:type-native obj)}
       [[:emit/form :hara.lang.base.emit-top-level/emit-form]
        [:emit/entry :hara.lang.base.impl-entry/emit-entry-raw]]])

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
        :subsystem :hara.lang.base.emit-top-level/emit-form
        :module xt.lang.common-lib
        :entry xt.lang.common-lib/fail-arr-push
        :line 340
        :form (x:arr-push out (f e))}
       [[:emit/form :hara.lang.base.emit-top-level/emit-form]
        [:emit/entry :hara.lang.base.impl-entry/emit-entry-raw]]])
