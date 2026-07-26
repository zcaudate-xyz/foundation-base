tahto/common/provenance_test.clj:1:(ns tahto.common.provenance-test
  (:use code.test)
tahto/common/provenance_test.clj:3:  (:require [tahto.common.provenance :refer :all]))

tahto/common/provenance_test.clj:5:^{:refer tahto.common.provenance/module-id :added "4.1"}
(fact "extracts module ids from maps and symbols"
  [(module-id {:id 'demo.core})
   (module-id 'demo.core)]
  => '[demo.core demo.core])

tahto/common/provenance_test.clj:11:^{:refer tahto.common.provenance/namespace-id :added "4.1"}
(fact "normalizes namespace references"
  [(namespace-id *ns*)
   (namespace-id 'demo.core)]
tahto/common/provenance_test.clj:15:  => '[tahto.common.provenance-test demo.core])

tahto/common/provenance_test.clj:17:^{:refer tahto.common.provenance/line-of :added "4.1"}
(fact "finds line metadata across nested values"
  (let [form (with-meta '(+ 1 2) {:line 12})]
    [(line-of nil)
     (line-of form)
     (line-of {:form form})])
  => [nil 12 12])

tahto/common/provenance_test.clj:25:^{:refer tahto.common.provenance/compact :added "4.1"}
(fact "drops nil values from maps"
  (compact {:a 1 :b nil :c 3})
  => '{:a 1 :c 3})

tahto/common/provenance_test.clj:30:^{:refer tahto.common.provenance/frame :added "4.1"}
(fact "builds a compact provenance frame"
  (let [form (with-meta '(boom 1) {:line 33})]
tahto/common/provenance_test.clj:33:    (frame {:tahto/provenance {:tahto/phase :emit/form}
tahto/common/provenance_test.clj:34:            :tahto/subsystem :probe/op
tahto/common/provenance_test.clj:35:            :tahto/module {:id 'demo.core}
tahto/common/provenance_test.clj:36:            :tahto/namespace *ns*
tahto/common/provenance_test.clj:37:            :tahto/form form}))
tahto/common/provenance_test.clj:38:  => '{:tahto/phase :emit/form
tahto/common/provenance_test.clj:39:       :tahto/subsystem :probe/op
tahto/common/provenance_test.clj:40:       :tahto/module demo.core
tahto/common/provenance_test.clj:41:       :tahto/namespace tahto.common.provenance-test
tahto/common/provenance_test.clj:42:       :tahto/line 33
tahto/common/provenance_test.clj:43:       :tahto/form (boom 1)})

tahto/common/provenance_test.clj:45:^{:refer tahto.common.provenance/provenance :added "4.1"}
(fact "merges provenance inputs into a single frame"
tahto/common/provenance_test.clj:47:  (provenance {:tahto/module 'demo.core}
tahto/common/provenance_test.clj:48:              {:tahto/line 10}
tahto/common/provenance_test.clj:49:              {:tahto/subsystem :emit/direct})
tahto/common/provenance_test.clj:50:  => '{:tahto/module demo.core
tahto/common/provenance_test.clj:51:       :tahto/line 10
tahto/common/provenance_test.clj:52:       :tahto/subsystem :emit/direct})

tahto/common/provenance_test.clj:54:^{:refer tahto.common.provenance/provenance-stack :added "4.1"}
(fact "returns stack frames from explicit stacks or a single frame"
tahto/common/provenance_test.clj:56:  [(provenance-stack {:tahto/provenance-stack [{:tahto/module 'demo.core}
tahto/common/provenance_test.clj:57:                                                 {:tahto/module 'demo.next}]})
tahto/common/provenance_test.clj:58:   (provenance-stack {:tahto/module 'demo.core})]
tahto/common/provenance_test.clj:59:  => '[[{:tahto/module demo.core}
tahto/common/provenance_test.clj:60:        {:tahto/module demo.next}]
tahto/common/provenance_test.clj:61:       [{:tahto/module demo.core}]])

tahto/common/provenance_test.clj:63:^{:refer tahto.common.provenance/same-site? :added "4.1"}
(fact "compares provenance frames by site fields"
tahto/common/provenance_test.clj:65:  [(same-site? {:tahto/phase :emit/form
tahto/common/provenance_test.clj:66:                :tahto/subsystem :probe
tahto/common/provenance_test.clj:67:                :tahto/module 'demo.core
tahto/common/provenance_test.clj:68:                :tahto/line 10}
tahto/common/provenance_test.clj:69:               {:tahto/phase :emit/form
tahto/common/provenance_test.clj:70:                :tahto/subsystem :probe
tahto/common/provenance_test.clj:71:                :tahto/module 'demo.core
tahto/common/provenance_test.clj:72:                :tahto/line 10
tahto/common/provenance_test.clj:73:                :tahto/form '(ignored)})
tahto/common/provenance_test.clj:74:   (same-site? {:tahto/phase :emit/form}
tahto/common/provenance_test.clj:75:               {:tahto/phase :emit/direct})]
  => [true false])

tahto/common/provenance_test.clj:78:^{:refer tahto.common.provenance/append-frame :added "4.1"}
(fact "appends only distinct provenance sites"
tahto/common/provenance_test.clj:80:  [(append-frame [{:tahto/phase :emit/form}] {:tahto/phase :emit/form})
tahto/common/provenance_test.clj:81:   (append-frame [{:tahto/phase :emit/form}] {:tahto/phase :emit/direct})]
tahto/common/provenance_test.clj:82:  => '[[{:tahto/phase :emit/form}]
tahto/common/provenance_test.clj:83:       [{:tahto/phase :emit/form}
tahto/common/provenance_test.clj:84:        {:tahto/phase :emit/direct}]])

tahto/common/provenance_test.clj:86:^{:refer tahto.common.provenance/with-provenance :added "4.1"}
(fact "threads merged provenance into mopts"
  (with-provenance {:lang :lua}
tahto/common/provenance_test.clj:89:                   {:tahto/module 'demo.core}
tahto/common/provenance_test.clj:90:                   {:tahto/line 10})
  => '{:lang :lua
tahto/common/provenance_test.clj:92:       :tahto/provenance {:tahto/module demo.core
tahto/common/provenance_test.clj:93:                             :tahto/line 10}})

tahto/common/provenance_test.clj:95:^{:refer tahto.common.provenance/error-with-provenance :added "4.1"}
(fact "wraps throwables with merged provenance data"
  (try
    (throw (ex-info "inner"
                    {:inner true
tahto/common/provenance_test.clj:100:                     :tahto/provenance {:tahto/phase :emit/form
tahto/common/provenance_test.clj:101:                                           :tahto/module 'demo.inner}}))
    (catch Throwable t
      (let [^Throwable wrapped (error-with-provenance
                                "wrap"
                                {:outer true
tahto/common/provenance_test.clj:106:                                 :tahto/phase :emit/direct
tahto/common/provenance_test.clj:107:                                 :tahto/module 'demo.outer}
                                t)]
        [(.getMessage wrapped)
         (select-keys (ex-data wrapped)
                      [:inner
                       :outer
tahto/common/provenance_test.clj:113:                       :tahto/phase
tahto/common/provenance_test.clj:114:                       :tahto/module
tahto/common/provenance_test.clj:115:                       :tahto/wrapped
tahto/common/provenance_test.clj:116:                       :tahto/cause-message])])))
  => '["wrap: inner"
       {:inner true
        :outer true
tahto/common/provenance_test.clj:120:        :tahto/phase :emit/form
tahto/common/provenance_test.clj:121:        :tahto/module demo.inner
tahto/common/provenance_test.clj:122:        :tahto/wrapped true
tahto/common/provenance_test.clj:123:        :tahto/cause-message "inner"}])

tahto/common/provenance_test.clj:125:^{:refer tahto.common.provenance/throw-with-provenance :added "4.1"}
(fact "throws wrapped provenance exceptions"
  (try
    (throw (ex-info "inner" {:inner true}))
    (catch Throwable t
      (try
        (throw-with-provenance "wrap" {:outer true} t)
        (catch Throwable wrapped
          [(.getMessage ^Throwable wrapped)
           (:outer (ex-data wrapped))]))))
  => ["wrap: inner" true])
