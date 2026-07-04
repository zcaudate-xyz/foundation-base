(ns js.react.ext-model-test
  (:require [std.fs :as fs]
            [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.event.base-model :as event-model]
             [js.react.ext-model :as ext-model]]})

(fact:global
 {:setup    [(l/rt:restart :js)]
  :teardown  [(l/rt:stop)]})

^{:refer js.react.ext-model/throttled-setter :added "4.0" :unchecked true}
(fact "creates a throttled setter which only updates after a delay"

  (notify/wait-on :js
    (var i 0)
    (var [throttle-fn throttle]
         (ext-model/throttled-setter
          (fn []
            (when (== i 1)
              (repl/notify i))
            (:= i (+ i 1)))
          200))
    (throttle-fn {})
    (throttle-fn {}))
  => 1

  (notify/wait-on :js
    (var i 0)
    (var [throttle-fn throttle]
         (ext-model/throttled-setter
          (fn []
            (when (== i 1)
              (repl/notify i))
            (:= i (+ i 1)))
          200))
    (throttle-fn {})
    (throttle-fn {})
    (throttle-fn {})
    (throttle-fn {})
    (throttle-fn {}))
  => 1)

^{:refer js.react.ext-model/refresh-view :added "4.0" :unchecked true}
(fact "refreshes the view"

  (notify/wait-on :js
    (. (do:>
        (var v (event-model/create-model
                (fn:> [x] (new Promise
                               (fn [resolve]
                                 (setTimeout
                                  (fn []
                                    (resolve {:value x}))
                                  100))))
                {}
                [3]
                {:value 0}))
        (event-model/init-model v)
        (return (ext-model/refresh-view v)))
       (then (repl/>notify))))
  => {"::" "model.run"
      "post" [false],
      "main" [true {"value" 3}],
      "pre" [false]})

^{:refer js.react.ext-model/refresh-args :added "4.0" :unchecked true}
(fact "refreshes the view view args"

  (notify/wait-on :js
    (. (do:>
        (var v (event-model/create-model
                (fn:> [x] (new Promise
                               (fn [resolve]
                                 (setTimeout
                                  (fn []
                                    (resolve {:value x}))
                                  100))))
                {}
                [3]
                {:value 0}))
        (event-model/init-model v)
        (return (ext-model/refresh-args v [10])))
       (then (repl/>notify))))
  => {"::" "model.run"
      "post" [false],
      "main" [true {"value" 10}],
      "pre" [false],})

^{:refer js.react.ext-model/refresh-view-remote :added "4.0" :unchecked true}
(fact "refreshes view using remote function")

^{:refer js.react.ext-model/refresh-args-remote :added "4.0" :unchecked true}
(fact "refreshes view using remote function with new args")

^{:refer js.react.ext-model/refresh-view-sync :added "4.0" :unchecked true}
(fact "refreshes view using sync function")

^{:refer js.react.ext-model/refresh-args-sync :added "4.0" :unchecked true}
(fact "refreshes view using args function")

^{:refer js.react.ext-model/make-view :added "4.0" :unchecked true}
(fact "makes and initialises view"

  (notify/wait-on :js
    (. (. (ext-model/make-view
           (fn:> [x] (new Promise
                          (fn [resolve]
                            (setTimeout
                             (fn []
                               (resolve {:value x}))
                             100))))
           {}
           [3]
           {:value 0})
          ["init"])
       (then (repl/>notify))))
  => {"post" [false],
      "main" [true {"value" 3}],
      "pre" [false],
      "::" "model.run"})

^{:refer js.react.ext-model/makeViewRaw :added "4.0" :unchecked true}
(fact "makes a react compatible view without r/const")

^{:refer js.react.ext-model/makeView :added "4.0" :unchecked true}
(fact "makes a react compatible view")

^{:refer js.react.ext-model/initViewBase :added "4.0" :unchecked true}
(fact "initialises the view listener")

^{:refer js.react.ext-model/listenView :added "4.0" :unchecked true}
(fact "creates the most basic views")

^{:refer js.react.ext-model/listenViewOutput :added "4.0" :unchecked true}
(fact "creates listeners on the output")

^{:refer js.react.ext-model/listenViewThrottled :added "4.0" :unchecked true}
(fact "creates the throttled listener")

^{:refer js.react.ext-model/wrap-pending :added "4.0" :unchecked true}
(fact "wraps function, setting pending flag")

^{:refer js.react.ext-model/refreshArgsFn :added "4.0" :unchecked true}
(fact "creates the refresh args function")

^{:refer js.react.ext-model/useRefreshArgs :added "4.0" :unchecked true}
(fact "refreshes args on the view")

^{:refer js.react.ext-model/listenSuccess :added "4.0" :unchecked true}
(fact "listens to the successful output")

^{:refer js.react.ext-model/handler-base :added "0.1"}
(fact "constructs a base handler")

^{:refer js.react.ext-model/oneshot-fn :added "0.1"}
(fact "creates a oneshot function"

  (!.js (var f (ext-model/oneshot-fn))
        [(f) (f) (f)])
  => [true false false])

^{:refer js.react.ext-model/input-disabled? :added "0.1"}
(fact "checks if input has been disabled (context method)"

  (ext-model/input-disabled? {:input {:disabled true}})
  => true

  (ext-model/input-disabled? {})
  => true

  (ext-model/input-disabled? {:input {}})
  => nil)

^{:refer js.react.ext-model/input-data :added "0.1"}
(fact "gets the input data (context method)"

  (ext-model/input-data {})
  => nil

  (ext-model/input-data {:input {:data 1}})
  => 1)

^{:refer js.react.ext-model/input-data-nil? :added "0.1"}
(fact "ensures that disabled flag or a nil input returns true"

  (ext-model/input-data-nil? {})
  => true

  (ext-model/input-data-nil? {:input {:data 1
                                      :disabled true}})
  => true)

^{:refer js.react.ext-model/output-empty? :added "0.1"}
(fact "checks that view is empty (context method)"

  (ext-model/output-empty? {:view {:output {:current nil}}})
  => true

  (ext-model/output-empty? {:view {:output {:current []}}})
  => true

  (ext-model/output-empty? {:view {:output {:current [1 2 3]}}})
  => false)
