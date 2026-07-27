(ns tahto.base.util-test
  (:require [tahto.base.provenance :as provenance]
            [tahto.base.util :refer :all])
  (:use code.test))

^{:refer tahto.base.util/sym-id :added "3.0"}
(fact "gets the symbol id"

  (sym-id 'L.core/identity)
  => 'identity)

^{:refer tahto.base.util/sym-module :added "3.0"}
(fact "gets the symbol namespace"

  (sym-module 'L.core/identity)
  => 'L.core)

^{:refer tahto.base.util/sym-pair :added "3.0"}
(fact "gets the symbol pair"

  (sym-pair 'L.core/identity)
  => '[L.core identity])

^{:refer tahto.base.util/sym-full :added "3.0"}
(fact "creates a full symbol"

  (sym-full 'L.core 'identity)
  => 'L.core/identity)

^{:refer tahto.base.util/sym-default-str :added "4.0"}
(fact "default fast symbol conversion"

  (sym-default-str :helloWorld)
  => "helloWorld"

  (sym-default-str :hello-World)
  => "hello_World")

^{:refer tahto.base.util/sym-default-inverse-str :added "4.0"}
(fact "inverses the symbol string"

  (sym-default-inverse-str "hello_world")
  => "hello-world")

^{:refer tahto.base.util/hashvec? :added "4.0"}
(fact "checks for hash vec"

  (hashvec? #{[1 2 3]})
  => true)

^{:refer tahto.base.util/doublevec? :added "4.0"}
(fact "checks for double vec"

  (doublevec? [[1 2 3]])
  => true)

^{:refer tahto.base.util/lang-context :added "4.0"}
(fact "creates the lang context"

  (lang-context :lua)
  => :lang/lua)

^{:refer tahto.base.util/lang-rt-list :added "4.0"}
(fact "lists rt in a namespace"

  (lang-rt-list)
  => coll?)

^{:refer tahto.base.util/lang-rt :added "4.0"}
(fact "getn the runtime contexts in a map"

  (lang-rt)
  => map?)

^{:refer tahto.base.util/lang-rt-default :added "4.0"}
(fact "gets the default runtime function"
  (lang-rt-default (lang-pointer :lua {:module 'L.core}))
  => any?)

^{:refer tahto.base.util/lang-pointer :added "4.0"}
(fact "creates a lang pointer"

  (into {} (lang-pointer :lua {:module 'L.core}))
  => {:context :lang/lua, :module 'L.core, :lang :lua,
      :context/fn #'tahto.base.util/lang-rt-default})

^{:refer tahto.base.util/module-id :added "4.1"}
(fact "gets the module id from a module symbol or map"
  (module-id {:id 'L.core})
  => 'L.core

  (module-id 'L.core)
  => 'L.core)

^{:refer tahto.base.util/entry-summary :added "4.1"}
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

^{:refer tahto.base.provenance/provenance :added "4.1"}
(fact "normalises provenance fields"
  (let [form (with-meta '(boom-op 1 2 3) {:line 17})]
    (provenance/provenance
     {:tahto/module {:id 'L.core}
      :tahto/namespace *ns*
      :tahto/form form
      :tahto/subsystem :test/direct}))
  => '{:tahto/module L.core
       :tahto/namespace tahto.base.util-test
       :tahto/line 17
       :tahto/form (boom-op 1 2 3)
       :tahto/subsystem :test/direct})

^{:refer tahto.base.provenance/with-provenance :added "4.1"}
(fact "threads provenance through mopts"
  (-> {:lang :lua}
      (provenance/with-provenance {:tahto/phase :emit/direct}
                                  {:tahto/module 'L.core})
      :tahto/provenance)
  => '{:tahto/phase :emit/direct
       :tahto/module L.core})

^{:refer tahto.base.util/error-with-context :added "4.1"}
(fact "wraps exceptions with tahto.core context"
  (try
    (throw (ex-info "inner" {:inner true}))
    (catch Throwable t
      (let [^Throwable wrapped (error-with-context "wrap" {:outer true} t)]
        [(.getMessage wrapped)
         (ex-data wrapped)])))
  => '["wrap: inner"
       {:inner true
        :outer true
        :tahto/wrapped true
         :tahto/cause-class "clojure.lang.ExceptionInfo"
         :tahto/cause-message "inner"
         :tahto/cause-data {:inner true}}])

(fact "wrapped tahto.core errors keep merged provenance"
  (let [form (with-meta '(boom-op 1 2 3) {:line 33})]
    (try
      (throw (ex-info "inner"
                      {:probe true
                       :tahto/provenance {:tahto/phase :emit/form
                                             :tahto/subsystem :inner/op
                                             :tahto/form form}}))
      (catch Throwable t
        (let [data (ex-data (error-with-context "wrap"
                                                {:tahto/phase :emit/direct
                                                 :tahto/subsystem :outer/direct
                                                 :tahto/module 'L.core}
                                                t))]
          {:probe (:probe data)
           :phase (:tahto/phase data)
           :subsystem (:tahto/subsystem data)
           :module (:tahto/module data)
           :line (:tahto/line data)
           :stack (mapv (juxt :tahto/phase :tahto/subsystem)
                        (:tahto/provenance-stack data))}))))
  => '{:probe true
       :phase :emit/form
       :subsystem :inner/op
       :module L.core
       :line 33
       :stack [[:emit/form :inner/op]
               [:emit/direct :outer/direct]]})

^{:refer tahto.base.util/throw-with-context :added "4.1"}
(fact "throws wrapped exceptions with tahto.core context"
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
        :tahto/wrapped true
        :tahto/cause-class "clojure.lang.ExceptionInfo"
        :tahto/cause-message "inner"
        :tahto/cause-data {:inner true}}])
