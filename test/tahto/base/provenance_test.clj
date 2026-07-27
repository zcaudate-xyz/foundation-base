(ns tahto.base.provenance-test
  (:use code.test)
  (:require [tahto.base.provenance :refer :all]))

^{:refer tahto.base.provenance/module-id :added "4.1"}
(fact "extracts module ids from maps and symbols"
  [(module-id {:id 'demo.core})
   (module-id 'demo.core)]
  => '[demo.core demo.core])

^{:refer tahto.base.provenance/namespace-id :added "4.1"}
(fact "normalizes namespace references"
  [(namespace-id *ns*)
   (namespace-id 'demo.core)]
  => '[tahto.base.provenance-test demo.core])

^{:refer tahto.base.provenance/line-of :added "4.1"}
(fact "finds line metadata across nested values"
  (let [form (with-meta '(+ 1 2) {:line 12})]
    [(line-of nil)
     (line-of form)
     (line-of {:form form})])
  => [nil 12 12])

^{:refer tahto.base.provenance/compact :added "4.1"}
(fact "drops nil values from maps"
  (compact {:a 1 :b nil :c 3})
  => '{:a 1 :c 3})

^{:refer tahto.base.provenance/frame :added "4.1"}
(fact "builds a compact provenance frame"
  (let [form (with-meta '(boom 1) {:line 33})]
    (frame {:tahto/provenance {:tahto/phase :emit/form}
            :tahto/subsystem :probe/op
            :tahto/module {:id 'demo.core}
            :tahto/namespace *ns*
            :tahto/form form}))
  => '{:tahto/phase :emit/form
       :tahto/subsystem :probe/op
       :tahto/module demo.core
       :tahto/namespace tahto.base.provenance-test
       :tahto/line 33
       :tahto/form (boom 1)})

^{:refer tahto.base.provenance/provenance :added "4.1"}
(fact "merges provenance inputs into a single frame"
  (provenance {:tahto/module 'demo.core}
              {:tahto/line 10}
              {:tahto/subsystem :emit/direct})
  => '{:tahto/module demo.core
       :tahto/line 10
       :tahto/subsystem :emit/direct})

^{:refer tahto.base.provenance/provenance-stack :added "4.1"}
(fact "returns stack frames from explicit stacks or a single frame"
  [(provenance-stack {:tahto/provenance-stack [{:tahto/module 'demo.core}
                                                 {:tahto/module 'demo.next}]})
   (provenance-stack {:tahto/module 'demo.core})]
  => '[[{:tahto/module demo.core}
        {:tahto/module demo.next}]
       [{:tahto/module demo.core}]])

^{:refer tahto.base.provenance/same-site? :added "4.1"}
(fact "compares provenance frames by site fields"
  [(same-site? {:tahto/phase :emit/form
                :tahto/subsystem :probe
                :tahto/module 'demo.core
                :tahto/line 10}
               {:tahto/phase :emit/form
                :tahto/subsystem :probe
                :tahto/module 'demo.core
                :tahto/line 10
                :tahto/form '(ignored)})
   (same-site? {:tahto/phase :emit/form}
               {:tahto/phase :emit/direct})]
  => [true false])

^{:refer tahto.base.provenance/append-frame :added "4.1"}
(fact "appends only distinct provenance sites"
  [(append-frame [{:tahto/phase :emit/form}] {:tahto/phase :emit/form})
   (append-frame [{:tahto/phase :emit/form}] {:tahto/phase :emit/direct})]
  => '[[{:tahto/phase :emit/form}]
       [{:tahto/phase :emit/form}
        {:tahto/phase :emit/direct}]])

^{:refer tahto.base.provenance/with-provenance :added "4.1"}
(fact "threads merged provenance into mopts"
  (with-provenance {:lang :lua}
                   {:tahto/module 'demo.core}
                   {:tahto/line 10})
  => '{:lang :lua
       :tahto/provenance {:tahto/module demo.core
                             :tahto/line 10}})

^{:refer tahto.base.provenance/error-with-provenance :added "4.1"}
(fact "wraps throwables with merged provenance data"
  (try
    (throw (ex-info "inner"
                    {:inner true
                     :tahto/provenance {:tahto/phase :emit/form
                                           :tahto/module 'demo.inner}}))
    (catch Throwable t
      (let [^Throwable wrapped (error-with-provenance
                                "wrap"
                                {:outer true
                                 :tahto/phase :emit/direct
                                 :tahto/module 'demo.outer}
                                t)]
        [(.getMessage wrapped)
         (select-keys (ex-data wrapped)
                      [:inner
                       :outer
                       :tahto/phase
                       :tahto/module
                       :tahto/wrapped
                       :tahto/cause-message])])))
  => '["wrap: inner"
       {:inner true
        :outer true
        :tahto/phase :emit/form
        :tahto/module demo.inner
        :tahto/wrapped true
        :tahto/cause-message "inner"}])

^{:refer tahto.base.provenance/throw-with-provenance :added "4.1"}
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
