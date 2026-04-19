(ns std.lang.base.script-macro-provenance-test
  (:require [std.lang.base.book-module :as module]
            [std.lang.base.impl :as impl]
            [std.lang.base.library :as lib]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.runtime :as rt]
            [std.lang.base.script-macro :as macro]
            [std.lang.model.spec-xtalk :as xtalk])
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
        {:phase     (:std.lang/phase data)
         :subsystem (:std.lang/subsystem data)
         :module    (:std.lang/module data)
         :entry     (-> data :std.lang/entry :symbol)
         :line      (:std.lang/line data)
         :form      (:std.lang/form data)
         :stack     (mapv (juxt :std.lang/phase
                                :std.lang/subsystem)
                          (:std.lang/provenance-stack data))}))))

(fact "defn.xt failures report provenance for abstract calls"
  ^:hidden

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
        :subsystem :std.lang.base.emit-top-level/emit-form
        :module xt.lang.common-lib
        :entry xt.lang.common-lib/fail-type-native
        :line 320
        :form (x:type-native obj)}
       [[:emit/form :std.lang.base.emit-top-level/emit-form]
        [:emit/entry :std.lang.base.impl-entry/emit-entry-raw]]])

(fact "defn.xt failures keep entry provenance when nested forms explode"
  ^:hidden

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
        :subsystem :std.lang.base.emit-top-level/emit-form
        :module xt.lang.common-lib
        :entry xt.lang.common-lib/fail-arr-push
        :line 340
        :form (x:arr-push out (f e))}
       [[:emit/form :std.lang.base.emit-top-level/emit-form]
        [:emit/entry :std.lang.base.impl-entry/emit-entry-raw]]])
