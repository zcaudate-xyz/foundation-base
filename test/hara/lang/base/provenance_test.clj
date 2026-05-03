(ns hara.lang.base.provenance-test
  (:use code.test)
  (:require [hara.lang.base.provenance :refer :all]))

^{:refer hara.lang.base.provenance/module-id :added "4.1"}
(fact "extracts module ids from maps and symbols"
  [(module-id {:id 'demo.core})
   (module-id 'demo.core)]
  => '[demo.core demo.core])

^{:refer hara.lang.base.provenance/namespace-id :added "4.1"}
(fact "normalizes namespace references"
  [(namespace-id *ns*)
   (namespace-id 'demo.core)]
  => '[hara.lang.base.provenance-test demo.core])

^{:refer hara.lang.base.provenance/line-of :added "4.1"}
(fact "finds line metadata across nested values"
  (let [form (with-meta '(+ 1 2) {:line 12})]
    [(line-of nil)
     (line-of form)
     (line-of {:form form})])
  => [nil 12 12])

^{:refer hara.lang.base.provenance/compact :added "4.1"}
(fact "drops nil values from maps"
  (compact {:a 1 :b nil :c 3})
  => '{:a 1 :c 3})

^{:refer hara.lang.base.provenance/frame :added "4.1"}
(fact "builds a compact provenance frame"
  (let [form (with-meta '(boom 1) {:line 33})]
    (frame {:hara.lang/provenance {:hara.lang/phase :emit/form}
            :hara.lang/subsystem :probe/op
            :hara.lang/module {:id 'demo.core}
            :hara.lang/namespace *ns*
            :hara.lang/form form}))
  => '{:hara.lang/phase :emit/form
       :hara.lang/subsystem :probe/op
       :hara.lang/module demo.core
       :hara.lang/namespace hara.lang.base.provenance-test
       :hara.lang/line 33
       :hara.lang/form (boom 1)})

^{:refer hara.lang.base.provenance/provenance :added "4.1"}
(fact "merges provenance inputs into a single frame"
  (provenance {:hara.lang/module 'demo.core}
              {:hara.lang/line 10}
              {:hara.lang/subsystem :emit/direct})
  => '{:hara.lang/module demo.core
       :hara.lang/line 10
       :hara.lang/subsystem :emit/direct})

^{:refer hara.lang.base.provenance/provenance-stack :added "4.1"}
(fact "returns stack frames from explicit stacks or a single frame"
  [(provenance-stack {:hara.lang/provenance-stack [{:hara.lang/module 'demo.core}
                                                 {:hara.lang/module 'demo.next}]})
   (provenance-stack {:hara.lang/module 'demo.core})]
  => '[[{:hara.lang/module demo.core}
        {:hara.lang/module demo.next}]
       [{:hara.lang/module demo.core}]])

^{:refer hara.lang.base.provenance/same-site? :added "4.1"}
(fact "compares provenance frames by site fields"
  [(same-site? {:hara.lang/phase :emit/form
                :hara.lang/subsystem :probe
                :hara.lang/module 'demo.core
                :hara.lang/line 10}
               {:hara.lang/phase :emit/form
                :hara.lang/subsystem :probe
                :hara.lang/module 'demo.core
                :hara.lang/line 10
                :hara.lang/form '(ignored)})
   (same-site? {:hara.lang/phase :emit/form}
               {:hara.lang/phase :emit/direct})]
  => [true false])

^{:refer hara.lang.base.provenance/append-frame :added "4.1"}
(fact "appends only distinct provenance sites"
  [(append-frame [{:hara.lang/phase :emit/form}] {:hara.lang/phase :emit/form})
   (append-frame [{:hara.lang/phase :emit/form}] {:hara.lang/phase :emit/direct})]
  => '[[{:hara.lang/phase :emit/form}]
       [{:hara.lang/phase :emit/form}
        {:hara.lang/phase :emit/direct}]])

^{:refer hara.lang.base.provenance/with-provenance :added "4.1"}
(fact "threads merged provenance into mopts"
  (with-provenance {:lang :lua}
                   {:hara.lang/module 'demo.core}
                   {:hara.lang/line 10})
  => '{:lang :lua
       :hara.lang/provenance {:hara.lang/module demo.core
                             :hara.lang/line 10}})

^{:refer hara.lang.base.provenance/error-with-provenance :added "4.1"}
(fact "wraps throwables with merged provenance data"
  (try
    (throw (ex-info "inner"
                    {:inner true
                     :hara.lang/provenance {:hara.lang/phase :emit/form
                                           :hara.lang/module 'demo.inner}}))
    (catch Throwable t
      (let [^Throwable wrapped (error-with-provenance
                                "wrap"
                                {:outer true
                                 :hara.lang/phase :emit/direct
                                 :hara.lang/module 'demo.outer}
                                t)]
        [(.getMessage wrapped)
         (select-keys (ex-data wrapped)
                      [:inner
                       :outer
                       :hara.lang/phase
                       :hara.lang/module
                       :hara.lang/wrapped
                       :hara.lang/cause-message])])))
  => '["wrap: inner"
       {:inner true
        :outer true
        :hara.lang/phase :emit/form
        :hara.lang/module demo.inner
        :hara.lang/wrapped true
        :hara.lang/cause-message "inner"}])

^{:refer hara.lang.base.provenance/throw-with-provenance :added "4.1"}
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
