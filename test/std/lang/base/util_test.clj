(ns std.lang.base.util-test
  (:require [std.lang.base.provenance :as provenance]
            [std.lang.base.util :refer :all])
  (:use code.test))

^{:refer std.lang.base.util/sym-id :added "3.0"}
(fact "gets the symbol id"
  ^:hidden

  (sym-id 'L.core/identity)
  => 'identity)

^{:refer std.lang.base.util/sym-module :added "3.0"}
(fact "gets the symbol namespace"
  ^:hidden

  (sym-module 'L.core/identity)
  => 'L.core)

^{:refer std.lang.base.util/sym-pair :added "3.0"}
(fact "gets the symbol pair"

  (sym-pair 'L.core/identity)
  => '[L.core identity])

^{:refer std.lang.base.util/sym-full :added "3.0"}
(fact "creates a full symbol"
  ^:hidden

  (sym-full 'L.core 'identity)
  => 'L.core/identity)

^{:refer std.lang.base.util/sym-default-str :added "4.0"}
(fact "default fast symbol conversion"
  ^:hidden
  
  (sym-default-str :helloWorld)
  => "helloWorld"

  (sym-default-str :hello-World)
  => "hello_World")

^{:refer std.lang.base.util/sym-default-inverse-str :added "4.0"}
(fact "inverses the symbol string"
  ^:hidden
  
  (sym-default-inverse-str "hello_world")
  => "hello-world")

^{:refer std.lang.base.util/hashvec? :added "4.0"}
(fact "checks for hash vec"
  ^:hidden

  (hashvec? #{[1 2 3]})
  => true)

^{:refer std.lang.base.util/doublevec? :added "4.0"}
(fact "checks for double vec"
  ^:hidden

  (doublevec? [[1 2 3]])
  => true)

^{:refer std.lang.base.util/lang-context :added "4.0"}
(fact "creates the lang context"
  ^:hidden

  (lang-context :lua)
  => :lang/lua)

^{:refer std.lang.base.util/lang-rt-list :added "4.0"}
(fact "lists rt in a namespace"
  ^:hidden
  
  (lang-rt-list)
  => coll?)

^{:refer std.lang.base.util/lang-rt :added "4.0"}
(fact "getn the runtime contexts in a map"
  ^:hidden
  
  (lang-rt)
  => map?)

^{:refer std.lang.base.util/lang-rt-default :added "4.0"}
(fact "gets the default runtime function"
  (lang-rt-default (lang-pointer :lua {:module 'L.core}))
  => any?)

^{:refer std.lang.base.util/lang-pointer :added "4.0"}
(fact "creates a lang pointer"
  ^:hidden
  
  (into {} (lang-pointer :lua {:module 'L.core}))
  => {:context :lang/lua, :module 'L.core, :lang :lua,
      :context/fn #'std.lang.base.util/lang-rt-default})

^{:refer std.lang.base.util/module-id :added "4.1"}
(fact "gets the module id from a module symbol or map"
  (module-id {:id 'L.core})
  => 'L.core

  (module-id 'L.core)
  => 'L.core)

^{:refer std.lang.base.util/entry-summary :added "4.1"}
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

^{:refer std.lang.base.provenance/provenance :added "4.1"}
(fact "normalises provenance fields"
  (let [form (with-meta '(boom-op 1 2 3) {:line 17})]
    (provenance/provenance
     {:std.lang/module {:id 'L.core}
      :std.lang/namespace *ns*
      :std.lang/form form
      :std.lang/subsystem :test/direct}))
  => '{:std.lang/module L.core
       :std.lang/namespace std.lang.base.util-test
       :std.lang/line 17
       :std.lang/form (boom-op 1 2 3)
       :std.lang/subsystem :test/direct})

^{:refer std.lang.base.provenance/with-provenance :added "4.1"}
(fact "threads provenance through mopts"
  (-> {:lang :lua}
      (provenance/with-provenance {:std.lang/phase :emit/direct}
                                  {:std.lang/module 'L.core})
      :std.lang/provenance)
  => '{:std.lang/phase :emit/direct
       :std.lang/module L.core})

^{:refer std.lang.base.util/error-with-context :added "4.1"}
(fact "wraps exceptions with std.lang context"
  (try
    (throw (ex-info "inner" {:inner true}))
    (catch Throwable t
      [(.getMessage (error-with-context "wrap" {:outer true} t))
       (ex-data (error-with-context "wrap" {:outer true} t))]))
  => '["wrap: inner"
       {:inner true
        :outer true
        :std.lang/wrapped true
         :std.lang/cause-class "clojure.lang.ExceptionInfo"
         :std.lang/cause-message "inner"
         :std.lang/cause-data {:inner true}}])

(fact "wrapped std.lang errors keep merged provenance"
  (let [form (with-meta '(boom-op 1 2 3) {:line 33})]
    (try
      (throw (ex-info "inner"
                      {:probe true
                       :std.lang/provenance {:std.lang/phase :emit/form
                                             :std.lang/subsystem :inner/op
                                             :std.lang/form form}}))
      (catch Throwable t
        (let [data (ex-data (error-with-context "wrap"
                                                {:std.lang/phase :emit/direct
                                                 :std.lang/subsystem :outer/direct
                                                 :std.lang/module 'L.core}
                                                t))]
          {:probe (:probe data)
           :phase (:std.lang/phase data)
           :subsystem (:std.lang/subsystem data)
           :module (:std.lang/module data)
           :line (:std.lang/line data)
           :stack (mapv (juxt :std.lang/phase :std.lang/subsystem)
                        (:std.lang/provenance-stack data))}))))
  => '{:probe true
       :phase :emit/form
       :subsystem :inner/op
       :module L.core
       :line 33
       :stack [[:emit/form :inner/op]
               [:emit/direct :outer/direct]]})

^{:refer std.lang.base.util/throw-with-context :added "4.1"}
(fact "throws wrapped exceptions with std.lang context"
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
        :std.lang/wrapped true
        :std.lang/cause-class "clojure.lang.ExceptionInfo"
        :std.lang/cause-message "inner"
        :std.lang/cause-data {:inner true}}])
