(ns lang.base.provenance-test
  (:use code.test)
  (:require [lang.base.provenance :refer :all]))

^{:refer lang.base.provenance/module-id :added "4.1"}
(fact "extracts module ids from maps and symbols"
  [(module-id {:id 'demo.core})
   (module-id 'demo.core)]
  => '[demo.core demo.core])

^{:refer lang.base.provenance/namespace-id :added "4.1"}
(fact "normalizes namespace references"
  [(namespace-id *ns*)
   (namespace-id 'demo.core)]
  => '[lang.base.provenance-test demo.core])

^{:refer lang.base.provenance/line-of :added "4.1"}
(fact "finds line metadata across nested values"
  (let [form (with-meta '(+ 1 2) {:line 12})]
    [(line-of nil)
     (line-of form)
     (line-of {:form form})])
  => [nil 12 12])

^{:refer lang.base.provenance/compact :added "4.1"}
(fact "drops nil values from maps"
  (compact {:a 1 :b nil :c 3})
  => '{:a 1 :c 3})

^{:refer lang.base.provenance/frame :added "4.1"}
(fact "builds a compact provenance frame"
  (let [form (with-meta '(boom 1) {:line 33})]
    (frame {:lang/provenance {:lang/phase :emit/form}
            :lang/subsystem :probe/op
            :lang/module {:id 'demo.core}
            :lang/namespace *ns*
            :lang/form form}))
  => '{:lang/phase :emit/form
       :lang/subsystem :probe/op
       :lang/module demo.core
       :lang/namespace lang.base.provenance-test
       :lang/line 33
       :lang/form (boom 1)})

^{:refer lang.base.provenance/provenance :added "4.1"}
(fact "merges provenance inputs into a single frame"
  (provenance {:lang/module 'demo.core}
              {:lang/line 10}
              {:lang/subsystem :emit/direct})
  => '{:lang/module demo.core
       :lang/line 10
       :lang/subsystem :emit/direct})

^{:refer lang.base.provenance/provenance-stack :added "4.1"}
(fact "returns stack frames from explicit stacks or a single frame"
  [(provenance-stack {:lang/provenance-stack [{:lang/module 'demo.core}
                                                 {:lang/module 'demo.next}]})
   (provenance-stack {:lang/module 'demo.core})]
  => '[[{:lang/module demo.core}
        {:lang/module demo.next}]
       [{:lang/module demo.core}]])

^{:refer lang.base.provenance/same-site? :added "4.1"}
(fact "compares provenance frames by site fields"
  [(same-site? {:lang/phase :emit/form
                :lang/subsystem :probe
                :lang/module 'demo.core
                :lang/line 10}
               {:lang/phase :emit/form
                :lang/subsystem :probe
                :lang/module 'demo.core
                :lang/line 10
                :lang/form '(ignored)})
   (same-site? {:lang/phase :emit/form}
               {:lang/phase :emit/direct})]
  => [true false])

^{:refer lang.base.provenance/append-frame :added "4.1"}
(fact "appends only distinct provenance sites"
  [(append-frame [{:lang/phase :emit/form}] {:lang/phase :emit/form})
   (append-frame [{:lang/phase :emit/form}] {:lang/phase :emit/direct})]
  => '[[{:lang/phase :emit/form}]
       [{:lang/phase :emit/form}
        {:lang/phase :emit/direct}]])

^{:refer lang.base.provenance/with-provenance :added "4.1"}
(fact "threads merged provenance into mopts"
  (with-provenance {:lang :lua}
                   {:lang/module 'demo.core}
                   {:lang/line 10})
  => '{:lang :lua
       :lang/provenance {:lang/module demo.core
                             :lang/line 10}})

^{:refer lang.base.provenance/error-with-provenance :added "4.1"}
(fact "wraps throwables with merged provenance data"
  (try
    (throw (ex-info "inner"
                    {:inner true
                     :lang/provenance {:lang/phase :emit/form
                                           :lang/module 'demo.inner}}))
    (catch Throwable t
      (let [^Throwable wrapped (error-with-provenance
                                "wrap"
                                {:outer true
                                 :lang/phase :emit/direct
                                 :lang/module 'demo.outer}
                                t)]
        [(.getMessage wrapped)
         (select-keys (ex-data wrapped)
                      [:inner
                       :outer
                       :lang/phase
                       :lang/module
                       :lang/wrapped
                       :lang/cause-message])])))
  => '["wrap: inner"
       {:inner true
        :outer true
        :lang/phase :emit/form
        :lang/module demo.inner
        :lang/wrapped true
        :lang/cause-message "inner"}])

^{:refer lang.base.provenance/throw-with-provenance :added "4.1"}
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
