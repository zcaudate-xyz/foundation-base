tahto/common/util_test.clj:1:(ns tahto.common.util-test
tahto/common/util_test.clj:2:  (:require [tahto.common.provenance :as provenance]
tahto/common/util_test.clj:3:            [tahto.common.util :refer :all])
  (:use code.test))

tahto/common/util_test.clj:6:^{:refer tahto.common.util/sym-id :added "3.0"}
(fact "gets the symbol id"

  (sym-id 'L.core/identity)
  => 'identity)

tahto/common/util_test.clj:12:^{:refer tahto.common.util/sym-module :added "3.0"}
(fact "gets the symbol namespace"

  (sym-module 'L.core/identity)
  => 'L.core)

tahto/common/util_test.clj:18:^{:refer tahto.common.util/sym-pair :added "3.0"}
(fact "gets the symbol pair"

  (sym-pair 'L.core/identity)
  => '[L.core identity])

tahto/common/util_test.clj:24:^{:refer tahto.common.util/sym-full :added "3.0"}
(fact "creates a full symbol"

  (sym-full 'L.core 'identity)
  => 'L.core/identity)

tahto/common/util_test.clj:30:^{:refer tahto.common.util/sym-default-str :added "4.0"}
(fact "default fast symbol conversion"

  (sym-default-str :helloWorld)
  => "helloWorld"

  (sym-default-str :hello-World)
  => "hello_World")

tahto/common/util_test.clj:39:^{:refer tahto.common.util/sym-default-inverse-str :added "4.0"}
(fact "inverses the symbol string"

  (sym-default-inverse-str "hello_world")
  => "hello-world")

tahto/common/util_test.clj:45:^{:refer tahto.common.util/hashvec? :added "4.0"}
(fact "checks for hash vec"

  (hashvec? #{[1 2 3]})
  => true)

tahto/common/util_test.clj:51:^{:refer tahto.common.util/doublevec? :added "4.0"}
(fact "checks for double vec"

  (doublevec? [[1 2 3]])
  => true)

tahto/common/util_test.clj:57:^{:refer tahto.common.util/lang-context :added "4.0"}
(fact "creates the lang context"

  (lang-context :lua)
  => :lang/lua)

tahto/common/util_test.clj:63:^{:refer tahto.common.util/lang-rt-list :added "4.0"}
(fact "lists rt in a namespace"

  (lang-rt-list)
  => coll?)

tahto/common/util_test.clj:69:^{:refer tahto.common.util/lang-rt :added "4.0"}
(fact "getn the runtime contexts in a map"

  (lang-rt)
  => map?)

tahto/common/util_test.clj:75:^{:refer tahto.common.util/lang-rt-default :added "4.0"}
(fact "gets the default runtime function"
  (lang-rt-default (lang-pointer :lua {:module 'L.core}))
  => any?)

tahto/common/util_test.clj:80:^{:refer tahto.common.util/lang-pointer :added "4.0"}
(fact "creates a lang pointer"

  (into {} (lang-pointer :lua {:module 'L.core}))
  => {:context :lang/lua, :module 'L.core, :lang :lua,
tahto/common/util_test.clj:85:      :context/fn #'tahto.common.util/lang-rt-default})

tahto/common/util_test.clj:87:^{:refer tahto.common.util/module-id :added "4.1"}
(fact "gets the module id from a module symbol or map"
  (module-id {:id 'L.core})
  => 'L.core

  (module-id 'L.core)
  => 'L.core)

tahto/common/util_test.clj:95:^{:refer tahto.common.util/entry-summary :added "4.1"}
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

tahto/common/util_test.clj:115:^{:refer tahto.common.provenance/provenance :added "4.1"}
(fact "normalises provenance fields"
  (let [form (with-meta '(boom-op 1 2 3) {:line 17})]
    (provenance/provenance
tahto/common/util_test.clj:119:     {:tahto/module {:id 'L.core}
tahto/common/util_test.clj:120:      :tahto/namespace *ns*
tahto/common/util_test.clj:121:      :tahto/form form
tahto/common/util_test.clj:122:      :tahto/subsystem :test/direct}))
tahto/common/util_test.clj:123:  => '{:tahto/module L.core
tahto/common/util_test.clj:124:       :tahto/namespace tahto.common.util-test
tahto/common/util_test.clj:125:       :tahto/line 17
tahto/common/util_test.clj:126:       :tahto/form (boom-op 1 2 3)
tahto/common/util_test.clj:127:       :tahto/subsystem :test/direct})

tahto/common/util_test.clj:129:^{:refer tahto.common.provenance/with-provenance :added "4.1"}
(fact "threads provenance through mopts"
  (-> {:lang :lua}
tahto/common/util_test.clj:132:      (provenance/with-provenance {:tahto/phase :emit/direct}
tahto/common/util_test.clj:133:                                  {:tahto/module 'L.core})
tahto/common/util_test.clj:134:      :tahto/provenance)
tahto/common/util_test.clj:135:  => '{:tahto/phase :emit/direct
tahto/common/util_test.clj:136:       :tahto/module L.core})

tahto/common/util_test.clj:138:^{:refer tahto.common.util/error-with-context :added "4.1"}
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
tahto/common/util_test.clj:149:        :tahto/wrapped true
tahto/common/util_test.clj:150:         :tahto/cause-class "clojure.lang.ExceptionInfo"
tahto/common/util_test.clj:151:         :tahto/cause-message "inner"
tahto/common/util_test.clj:152:         :tahto/cause-data {:inner true}}])

(fact "wrapped tahto.core errors keep merged provenance"
  (let [form (with-meta '(boom-op 1 2 3) {:line 33})]
    (try
      (throw (ex-info "inner"
                      {:probe true
tahto/common/util_test.clj:159:                       :tahto/provenance {:tahto/phase :emit/form
tahto/common/util_test.clj:160:                                             :tahto/subsystem :inner/op
tahto/common/util_test.clj:161:                                             :tahto/form form}}))
      (catch Throwable t
        (let [data (ex-data (error-with-context "wrap"
tahto/common/util_test.clj:164:                                                {:tahto/phase :emit/direct
tahto/common/util_test.clj:165:                                                 :tahto/subsystem :outer/direct
tahto/common/util_test.clj:166:                                                 :tahto/module 'L.core}
                                                t))]
          {:probe (:probe data)
tahto/common/util_test.clj:169:           :phase (:tahto/phase data)
tahto/common/util_test.clj:170:           :subsystem (:tahto/subsystem data)
tahto/common/util_test.clj:171:           :module (:tahto/module data)
tahto/common/util_test.clj:172:           :line (:tahto/line data)
tahto/common/util_test.clj:173:           :stack (mapv (juxt :tahto/phase :tahto/subsystem)
tahto/common/util_test.clj:174:                        (:tahto/provenance-stack data))}))))
  => '{:probe true
       :phase :emit/form
       :subsystem :inner/op
       :module L.core
       :line 33
       :stack [[:emit/form :inner/op]
               [:emit/direct :outer/direct]]})

tahto/common/util_test.clj:183:^{:refer tahto.common.util/throw-with-context :added "4.1"}
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
tahto/common/util_test.clj:196:        :tahto/wrapped true
tahto/common/util_test.clj:197:        :tahto/cause-class "clojure.lang.ExceptionInfo"
tahto/common/util_test.clj:198:        :tahto/cause-message "inner"
tahto/common/util_test.clj:199:        :tahto/cause-data {:inner true}}])
