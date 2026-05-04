(ns hara.common.provenance-test
  (:use code.test)
  (:require [hara.common.provenance :refer :all]))

^{:refer hara.common.provenance/module-id :added "4.1"}
(fact "extracts module ids from maps and symbols"
  [(module-id {:id 'demo.core})
   (module-id 'demo.core)]
  => '[demo.core demo.core])

^{:refer hara.common.provenance/namespace-id :added "4.1"}
(fact "normalizes namespace references"
  [(namespace-id *ns*)
   (namespace-id 'demo.core)]
  => '[hara.common.provenance-test demo.core])

^{:refer hara.common.provenance/line-of :added "4.1"}
(fact "finds line metadata across nested values"
  (let [form (with-meta '(+ 1 2) {:line 12})]
    [(line-of nil)
     (line-of form)
     (line-of {:form form})])
  => [nil 12 12])

^{:refer hara.common.provenance/compact :added "4.1"}
(fact "drops nil values from maps"
  (compact {:a 1 :b nil :c 3})
  => '{:a 1 :c 3})

^{:refer hara.common.provenance/frame :added "4.1"}
(fact "builds a compact provenance frame"
  (let [form (with-meta '(boom 1) {:line 33})]
    (frame {:hara/provenance {:hara/phase :emit/form}
            :hara/subsystem :probe/op
            :hara/module {:id 'demo.core}
            :hara/namespace *ns*
            :hara/form form}))
  => '{:hara/phase :emit/form
       :hara/subsystem :probe/op
       :hara/module demo.core
       :hara/namespace hara.common.provenance-test
       :hara/line 33
       :hara/form (boom 1)})

^{:refer hara.common.provenance/provenance :added "4.1"}
(fact "merges provenance inputs into a single frame"
  (provenance {:hara/module 'demo.core}
              {:hara/line 10}
              {:hara/subsystem :emit/direct})
  => '{:hara/module demo.core
       :hara/line 10
       :hara/subsystem :emit/direct})

^{:refer hara.common.provenance/provenance-stack :added "4.1"}
(fact "returns stack frames from explicit stacks or a single frame"
  [(provenance-stack {:hara/provenance-stack [{:hara/module 'demo.core}
                                                 {:hara/module 'demo.next}]})
   (provenance-stack {:hara/module 'demo.core})]
  => '[[{:hara/module demo.core}
        {:hara/module demo.next}]
       [{:hara/module demo.core}]])

^{:refer hara.common.provenance/same-site? :added "4.1"}
(fact "compares provenance frames by site fields"
  [(same-site? {:hara/phase :emit/form
                :hara/subsystem :probe
                :hara/module 'demo.core
                :hara/line 10}
               {:hara/phase :emit/form
                :hara/subsystem :probe
                :hara/module 'demo.core
                :hara/line 10
                :hara/form '(ignored)})
   (same-site? {:hara/phase :emit/form}
               {:hara/phase :emit/direct})]
  => [true false])

^{:refer hara.common.provenance/append-frame :added "4.1"}
(fact "appends only distinct provenance sites"
  [(append-frame [{:hara/phase :emit/form}] {:hara/phase :emit/form})
   (append-frame [{:hara/phase :emit/form}] {:hara/phase :emit/direct})]
  => '[[{:hara/phase :emit/form}]
       [{:hara/phase :emit/form}
        {:hara/phase :emit/direct}]])

^{:refer hara.common.provenance/with-provenance :added "4.1"}
(fact "threads merged provenance into mopts"
  (with-provenance {:lang :lua}
                   {:hara/module 'demo.core}
                   {:hara/line 10})
  => '{:lang :lua
       :hara/provenance {:hara/module demo.core
                             :hara/line 10}})

^{:refer hara.common.provenance/error-with-provenance :added "4.1"}
(fact "wraps throwables with merged provenance data"
  (try
    (throw (ex-info "inner"
                    {:inner true
                     :hara/provenance {:hara/phase :emit/form
                                           :hara/module 'demo.inner}}))
    (catch Throwable t
      (let [^Throwable wrapped (error-with-provenance
                                "wrap"
                                {:outer true
                                 :hara/phase :emit/direct
                                 :hara/module 'demo.outer}
                                t)]
        [(.getMessage wrapped)
         (select-keys (ex-data wrapped)
                      [:inner
                       :outer
                       :hara/phase
                       :hara/module
                       :hara/wrapped
                       :hara/cause-message])])))
  => '["wrap: inner"
       {:inner true
        :outer true
        :hara/phase :emit/form
        :hara/module demo.inner
        :hara/wrapped true
        :hara/cause-message "inner"}])

^{:refer hara.common.provenance/throw-with-provenance :added "4.1"}
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
