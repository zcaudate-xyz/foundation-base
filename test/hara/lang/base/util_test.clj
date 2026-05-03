(ns hara.lang.base.util-test
  (:require [hara.lang.base.provenance :as provenance]
            [hara.lang.base.util :refer :all])
  (:use code.test))

^{:refer hara.lang.base.util/sym-id :added "3.0"}
(fact "gets the symbol id"

  (sym-id 'L.core/identity)
  => 'identity)

^{:refer hara.lang.base.util/sym-module :added "3.0"}
(fact "gets the symbol namespace"

  (sym-module 'L.core/identity)
  => 'L.core)

^{:refer hara.lang.base.util/sym-pair :added "3.0"}
(fact "gets the symbol pair"

  (sym-pair 'L.core/identity)
  => '[L.core identity])

^{:refer hara.lang.base.util/sym-full :added "3.0"}
(fact "creates a full symbol"

  (sym-full 'L.core 'identity)
  => 'L.core/identity)

^{:refer hara.lang.base.util/sym-default-str :added "4.0"}
(fact "default fast symbol conversion"

  (sym-default-str :helloWorld)
  => "helloWorld"

  (sym-default-str :hello-World)
  => "hello_World")

^{:refer hara.lang.base.util/sym-default-inverse-str :added "4.0"}
(fact "inverses the symbol string"

  (sym-default-inverse-str "hello_world")
  => "hello-world")

^{:refer hara.lang.base.util/hashvec? :added "4.0"}
(fact "checks for hash vec"

  (hashvec? #{[1 2 3]})
  => true)

^{:refer hara.lang.base.util/doublevec? :added "4.0"}
(fact "checks for double vec"

  (doublevec? [[1 2 3]])
  => true)

^{:refer hara.lang.base.util/lang-context :added "4.0"}
(fact "creates the lang context"

  (lang-context :lua)
  => :lang/lua)

^{:refer hara.lang.base.util/lang-rt-list :added "4.0"}
(fact "lists rt in a namespace"

  (lang-rt-list)
  => coll?)

^{:refer hara.lang.base.util/lang-rt :added "4.0"}
(fact "getn the runtime contexts in a map"

  (lang-rt)
  => map?)

^{:refer hara.lang.base.util/lang-rt-default :added "4.0"}
(fact "gets the default runtime function"
  (lang-rt-default (lang-pointer :lua {:module 'L.core}))
  => any?)

^{:refer hara.lang.base.util/lang-pointer :added "4.0"}
(fact "creates a lang pointer"

  (into {} (lang-pointer :lua {:module 'L.core}))
  => {:context :lang/lua, :module 'L.core, :lang :lua,
      :context/fn #'hara.lang.base.util/lang-rt-default})

^{:refer hara.lang.base.util/module-id :added "4.1"}
(fact "gets the module id from a module symbol or map"
  (module-id {:id 'L.core})
  => 'L.core

  (module-id 'L.core)
  => 'L.core)

^{:refer hara.lang.base.util/entry-summary :added "4.1"}
(fact "returns a concise entry summary"
  (entry-summary {:lang :lua
                  :module 'L.core
                  :namespace 'L.core
                  :id 'add
                  :section :fragment
                  :line 10
                  :op 'def$
                  :op-key :def$})
  => '{:op-key :def$
       :symbol L.core/add
       :section :fragment
       :op def$
       :module L.core
       :lang :lua
        :line 10
        :id add
        :namespace L.core})

^{:refer hara.lang.base.provenance/provenance :added "4.1"}
(fact "normalises provenance fields"
  (let [form (with-meta '(boom-op 1 2 3) {:line 17})]
    (provenance/provenance
     {:hara.lang/module {:id 'L.core}
      :hara.lang/namespace *ns*
      :hara.lang/form form
      :hara.lang/subsystem :test/direct}))
  => '{:hara.lang/module L.core
       :hara.lang/namespace hara.lang.base.util-test
       :hara.lang/line 17
       :hara.lang/form (boom-op 1 2 3)
       :hara.lang/subsystem :test/direct})

^{:refer hara.lang.base.provenance/with-provenance :added "4.1"}
(fact "threads provenance through mopts"
  (-> {:lang :lua}
      (provenance/with-provenance {:hara.lang/phase :emit/direct}
                                  {:hara.lang/module 'L.core})
      :hara.lang/provenance)
  => '{:hara.lang/phase :emit/direct
       :hara.lang/module L.core})

^{:refer hara.lang.base.util/error-with-context :added "4.1"}
(fact "wraps exceptions with hara.lang context"
  (try
    (throw (ex-info "inner" {:inner true}))
    (catch Throwable t
      (let [^Throwable wrapped (error-with-context "wrap" {:outer true} t)]
        [(.getMessage wrapped)
         (ex-data wrapped)])))
  => '["wrap: inner"
       {:inner true
        :outer true
        :hara.lang/wrapped true
         :hara.lang/cause-class "clojure.lang.ExceptionInfo"
         :hara.lang/cause-message "inner"
         :hara.lang/cause-data {:inner true}}])

(fact "wrapped hara.lang errors keep merged provenance"
  (let [form (with-meta '(boom-op 1 2 3) {:line 33})]
    (try
      (throw (ex-info "inner"
                      {:probe true
                       :hara.lang/provenance {:hara.lang/phase :emit/form
                                             :hara.lang/subsystem :inner/op
                                             :hara.lang/form form}}))
      (catch Throwable t
        (let [data (ex-data (error-with-context "wrap"
                                                {:hara.lang/phase :emit/direct
                                                 :hara.lang/subsystem :outer/direct
                                                 :hara.lang/module 'L.core}
                                                t))]
          {:probe (:probe data)
           :phase (:hara.lang/phase data)
           :subsystem (:hara.lang/subsystem data)
           :module (:hara.lang/module data)
           :line (:hara.lang/line data)
           :stack (mapv (juxt :hara.lang/phase :hara.lang/subsystem)
                        (:hara.lang/provenance-stack data))}))))
  => '{:probe true
       :phase :emit/form
       :subsystem :inner/op
       :module L.core
       :line 33
       :stack [[:emit/form :inner/op]
               [:emit/direct :outer/direct]]})

^{:refer hara.lang.base.util/throw-with-context :added "4.1"}
(fact "throws wrapped exceptions with hara.lang context"
  (try
    (throw (ex-info "inner" {:inner true}))
    (catch Throwable t
      (try
        (throw-with-context "wrap" {:outer true} t)
        (catch Throwable wrapped
          [(.getMessage wrapped)
           (ex-data wrapped)]))))
  => '["wrap: inner"
       {:inner true
        :outer true
        :hara.lang/wrapped true
        :hara.lang/cause-class "clojure.lang.ExceptionInfo"
        :hara.lang/cause-message "inner"
        :hara.lang/cause-data {:inner true}}])
