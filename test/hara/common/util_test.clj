(ns hara.common.util-test
  (:require [hara.common.provenance :as provenance]
            [hara.common.util :refer :all])
  (:use code.test))

^{:refer hara.common.util/sym-id :added "3.0"}
(fact "gets the symbol id"

  (sym-id 'L.core/identity)
  => 'identity)

^{:refer hara.common.util/sym-module :added "3.0"}
(fact "gets the symbol namespace"

  (sym-module 'L.core/identity)
  => 'L.core)

^{:refer hara.common.util/sym-pair :added "3.0"}
(fact "gets the symbol pair"

  (sym-pair 'L.core/identity)
  => '[L.core identity])

^{:refer hara.common.util/sym-full :added "3.0"}
(fact "creates a full symbol"

  (sym-full 'L.core 'identity)
  => 'L.core/identity)

^{:refer hara.common.util/sym-default-str :added "4.0"}
(fact "default fast symbol conversion"

  (sym-default-str :helloWorld)
  => "helloWorld"

  (sym-default-str :hello-World)
  => "hello_World")

^{:refer hara.common.util/sym-default-inverse-str :added "4.0"}
(fact "inverses the symbol string"

  (sym-default-inverse-str "hello_world")
  => "hello-world")

^{:refer hara.common.util/hashvec? :added "4.0"}
(fact "checks for hash vec"

  (hashvec? #{[1 2 3]})
  => true)

^{:refer hara.common.util/doublevec? :added "4.0"}
(fact "checks for double vec"

  (doublevec? [[1 2 3]])
  => true)

^{:refer hara.common.util/lang-context :added "4.0"}
(fact "creates the lang context"

  (lang-context :lua)
  => :lang/lua)

^{:refer hara.common.util/lang-rt-list :added "4.0"}
(fact "lists rt in a namespace"

  (lang-rt-list)
  => coll?)

^{:refer hara.common.util/lang-rt :added "4.0"}
(fact "getn the runtime contexts in a map"

  (lang-rt)
  => map?)

^{:refer hara.common.util/lang-rt-default :added "4.0"}
(fact "gets the default runtime function"
  (lang-rt-default (lang-pointer :lua {:module 'L.core}))
  => any?)

^{:refer hara.common.util/lang-pointer :added "4.0"}
(fact "creates a lang pointer"

  (into {} (lang-pointer :lua {:module 'L.core}))
  => {:context :lang/lua, :module 'L.core, :lang :lua,
      :context/fn #'hara.common.util/lang-rt-default})

^{:refer hara.common.util/module-id :added "4.1"}
(fact "gets the module id from a module symbol or map"
  (module-id {:id 'L.core})
  => 'L.core

  (module-id 'L.core)
  => 'L.core)

^{:refer hara.common.util/entry-summary :added "4.1"}
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

^{:refer hara.common.provenance/provenance :added "4.1"}
(fact "normalises provenance fields"
  (let [form (with-meta '(boom-op 1 2 3) {:line 17})]
    (provenance/provenance
     {:hara/module {:id 'L.core}
      :hara/namespace *ns*
      :hara/form form
      :hara/subsystem :test/direct}))
  => '{:hara/module L.core
       :hara/namespace hara.common.util-test
       :hara/line 17
       :hara/form (boom-op 1 2 3)
       :hara/subsystem :test/direct})

^{:refer hara.common.provenance/with-provenance :added "4.1"}
(fact "threads provenance through mopts"
  (-> {:lang :lua}
      (provenance/with-provenance {:hara/phase :emit/direct}
                                  {:hara/module 'L.core})
      :hara/provenance)
  => '{:hara/phase :emit/direct
       :hara/module L.core})

^{:refer hara.common.util/error-with-context :added "4.1"}
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
        :hara/wrapped true
         :hara/cause-class "clojure.lang.ExceptionInfo"
         :hara/cause-message "inner"
         :hara/cause-data {:inner true}}])

(fact "wrapped hara.lang errors keep merged provenance"
  (let [form (with-meta '(boom-op 1 2 3) {:line 33})]
    (try
      (throw (ex-info "inner"
                      {:probe true
                       :hara/provenance {:hara/phase :emit/form
                                             :hara/subsystem :inner/op
                                             :hara/form form}}))
      (catch Throwable t
        (let [data (ex-data (error-with-context "wrap"
                                                {:hara/phase :emit/direct
                                                 :hara/subsystem :outer/direct
                                                 :hara/module 'L.core}
                                                t))]
          {:probe (:probe data)
           :phase (:hara/phase data)
           :subsystem (:hara/subsystem data)
           :module (:hara/module data)
           :line (:hara/line data)
           :stack (mapv (juxt :hara/phase :hara/subsystem)
                        (:hara/provenance-stack data))}))))
  => '{:probe true
       :phase :emit/form
       :subsystem :inner/op
       :module L.core
       :line 33
       :stack [[:emit/form :inner/op]
               [:emit/direct :outer/direct]]})

^{:refer hara.common.util/throw-with-context :added "4.1"}
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
        :hara/wrapped true
        :hara/cause-class "clojure.lang.ExceptionInfo"
        :hara/cause-message "inner"
        :hara/cause-data {:inner true}}])
