(ns std.lang.base.provenance-test
  (:use code.test)
  (:require [std.lang.base.provenance :refer :all]))

^{:refer std.lang.base.provenance/module-id :added "4.1"}
(fact "extracts module ids from maps and symbols"
  [(module-id {:id 'demo.core})
   (module-id 'demo.core)]
  => '[demo.core demo.core])

^{:refer std.lang.base.provenance/namespace-id :added "4.1"}
(fact "normalizes namespace references"
  [(namespace-id *ns*)
   (namespace-id 'demo.core)]
  => '[std.lang.base.provenance-test demo.core])

^{:refer std.lang.base.provenance/line-of :added "4.1"}
(fact "finds line metadata across nested values"
  (let [form (with-meta '(+ 1 2) {:line 12})]
    [(line-of nil)
     (line-of form)
     (line-of {:form form})])
  => [nil 12 12])

^{:refer std.lang.base.provenance/compact :added "4.1"}
(fact "drops nil values from maps"
  (compact {:a 1 :b nil :c 3})
  => '{:a 1 :c 3})

^{:refer std.lang.base.provenance/frame :added "4.1"}
(fact "builds a compact provenance frame"
  (let [form (with-meta '(boom 1) {:line 33})]
    (frame {:std.lang/provenance {:std.lang/phase :emit/form}
            :std.lang/subsystem :probe/op
            :std.lang/module {:id 'demo.core}
            :std.lang/namespace *ns*
            :std.lang/form form}))
  => '{:std.lang/phase :emit/form
       :std.lang/subsystem :probe/op
       :std.lang/module demo.core
       :std.lang/namespace std.lang.base.provenance-test
       :std.lang/line 33
       :std.lang/form (boom 1)})

^{:refer std.lang.base.provenance/provenance :added "4.1"}
(fact "merges provenance inputs into a single frame"
  (provenance {:std.lang/module 'demo.core}
              {:std.lang/line 10}
              {:std.lang/subsystem :emit/direct})
  => '{:std.lang/module demo.core
       :std.lang/line 10
       :std.lang/subsystem :emit/direct})

^{:refer std.lang.base.provenance/provenance-stack :added "4.1"}
(fact "returns stack frames from explicit stacks or a single frame"
  [(provenance-stack {:std.lang/provenance-stack [{:std.lang/module 'demo.core}
                                                 {:std.lang/module 'demo.next}]})
   (provenance-stack {:std.lang/module 'demo.core})]
  => '[[{:std.lang/module demo.core}
        {:std.lang/module demo.next}]
       [{:std.lang/module demo.core}]])

^{:refer std.lang.base.provenance/same-site? :added "4.1"}
(fact "compares provenance frames by site fields"
  [(same-site? {:std.lang/phase :emit/form
                :std.lang/subsystem :probe
                :std.lang/module 'demo.core
                :std.lang/line 10}
               {:std.lang/phase :emit/form
                :std.lang/subsystem :probe
                :std.lang/module 'demo.core
                :std.lang/line 10
                :std.lang/form '(ignored)})
   (same-site? {:std.lang/phase :emit/form}
               {:std.lang/phase :emit/direct})]
  => [true false])

^{:refer std.lang.base.provenance/append-frame :added "4.1"}
(fact "appends only distinct provenance sites"
  [(append-frame [{:std.lang/phase :emit/form}] {:std.lang/phase :emit/form})
   (append-frame [{:std.lang/phase :emit/form}] {:std.lang/phase :emit/direct})]
  => '[[{:std.lang/phase :emit/form}]
       [{:std.lang/phase :emit/form}
        {:std.lang/phase :emit/direct}]])

^{:refer std.lang.base.provenance/with-provenance :added "4.1"}
(fact "threads merged provenance into mopts"
  (with-provenance {:lang :lua}
                   {:std.lang/module 'demo.core}
                   {:std.lang/line 10})
  => '{:lang :lua
       :std.lang/provenance {:std.lang/module demo.core
                             :std.lang/line 10}})

^{:refer std.lang.base.provenance/error-with-provenance :added "4.1"}
(fact "wraps throwables with merged provenance data"
  (try
    (throw (ex-info "inner"
                    {:inner true
                     :std.lang/provenance {:std.lang/phase :emit/form
                                           :std.lang/module 'demo.inner}}))
    (catch Throwable t
      (let [^Throwable wrapped (error-with-provenance
                                "wrap"
                                {:outer true
                                 :std.lang/phase :emit/direct
                                 :std.lang/module 'demo.outer}
                                t)]
        [(.getMessage wrapped)
         (select-keys (ex-data wrapped)
                      [:inner
                       :outer
                       :std.lang/phase
                       :std.lang/module
                       :std.lang/wrapped
                       :std.lang/cause-message])])))
  => '["wrap: inner"
       {:inner true
        :outer true
        :std.lang/phase :emit/form
        :std.lang/module demo.inner
        :std.lang/wrapped true
        :std.lang/cause-message "inner"}])

^{:refer std.lang.base.provenance/throw-with-provenance :added "4.1"}
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
