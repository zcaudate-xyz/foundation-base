(ns js.react.ext-view-test
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]
            [std.fs :as fs]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.lang.event-view :as event-view]
             [js.core :as j]
             [js.react.ext-view :as ext-view]]})

(fact:global
 {:setup    [(l/rt:restart :js)]
  :teardown  [(l/rt:stop)]})

^{:refer js.react.ext-view/throttled-setter :added "4.0" :unchecked true}
(fact "creates a throttled setter which only updates after a delay"
  ^:hidden

  (notify/wait-on :js
    (var i 0)
    (var [throttle-fn throttle]
         (ext-view/throttled-setter
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
         (ext-view/throttled-setter
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

^{:refer js.react.ext-view/refresh-view :added "4.0" :unchecked true}
(fact "refreshes the view"
  ^:hidden
  
  (j/<!
   (do:>
    (var v (event-view/create-view
            (fn:> [x] (j/future-delayed [100]
                        (return {:value x})))
            {}
            [3]
            {:value 0}))
    (event-view/init-view v)
    (return (ext-view/refresh-view v))))
  => {"::" "view.run"
      "post" [false],
      "main" [true {"value" 3}],
      "pre" [false]})

^{:refer js.react.ext-view/refresh-args :added "4.0" :unchecked true}
(fact "refreshes the view view args"
  ^:hidden
  
  (j/<!
   (do:>
    (var v (event-view/create-view
            (fn:> [x] (j/future-delayed [100]
                        (return {:value x})))
            {}
            [3]
            {:value 0}))
    (event-view/init-view v)
    (return (ext-view/refresh-args v [10]))))
  => {"::" "view.run"
      "post" [false],
      "main" [true {"value" 10}],
      "pre" [false],})

^{:refer js.react.ext-view/refresh-view-remote :added "4.0" :unchecked true}
(fact "refreshes view using remote function")

^{:refer js.react.ext-view/refresh-args-remote :added "4.0" :unchecked true}
(fact "refreshes view using remote function with new args")

^{:refer js.react.ext-view/refresh-view-sync :added "4.0" :unchecked true}
(fact "refreshes view using sync function")

^{:refer js.react.ext-view/refresh-args-sync :added "4.0" :unchecked true}
(fact "refreshes view using args function")

^{:refer js.react.ext-view/make-view :added "4.0" :unchecked true}
(fact "makes and initialises view"
  ^:hidden
  
  (j/<! (. (ext-view/make-view
            (fn:> [x] (j/future-delayed [100]
                        (return {:value x})))
            {}
            [3]
            {:value 0})
           ["init"]))
  => {"post" [false],
      "main" [true {"value" 3}],
      "pre" [false],
      "::" "view.run"})

^{:refer js.react.ext-view/makeViewRaw :added "4.0" :unchecked true}
(fact "makes a react compatible view without r/const")

^{:refer js.react.ext-view/makeView :added "4.0" :unchecked true}
(fact "makes a react compatible view")

^{:refer js.react.ext-view/initViewBase :added "4.0" :unchecked true}
(fact "initialises the view listener")

^{:refer js.react.ext-view/listenView :added "4.0" :unchecked true}
(fact "creates the most basic views")

^{:refer js.react.ext-view/listenViewOutput :added "4.0" :unchecked true}
(fact "creates listeners on the output")

^{:refer js.react.ext-view/listenViewThrottled :added "4.0" :unchecked true}
(fact "creates the throttled listener")

^{:refer js.react.ext-view/wrap-pending :added "4.0" :unchecked true}
(fact "wraps function, setting pending flag")

^{:refer js.react.ext-view/refreshArgsFn :added "4.0" :unchecked true}
(fact "creates the refresh args function")

^{:refer js.react.ext-view/useRefreshArgs :added "4.0" :unchecked true}
(fact "refreshes args on the view")

^{:refer js.react.ext-view/listenSuccess :added "4.0" :unchecked true}
(fact "listens to the successful output")

^{:refer js.react.ext-view/handler-base :added "0.1"}
(fact "constructs a base handler")

^{:refer js.react.ext-view/oneshot-fn :added "0.1"}
(fact "creates a oneshot function"
  ^:hidden
  
  (!.js (var f (ext-view/oneshot-fn))
        [(f) (f) (f)])
  => [true false false])

^{:refer js.react.ext-view/input-disabled? :added "0.1"}
(fact "checks if input has been disabled (context method)"
  ^:hidden
  
  (ext-view/input-disabled? {:input {:disabled true}})
  => true

  (ext-view/input-disabled? {})
  => true

  (ext-view/input-disabled? {:input {}})
  => nil)

^{:refer js.react.ext-view/input-data :added "0.1"}
(fact "gets the input data (context method)"
  ^:hidden
  
  (ext-view/input-data {})
  => nil

  (ext-view/input-data {:input {:data 1}})
  => 1)

^{:refer js.react.ext-view/input-data-nil? :added "0.1"}
(fact "ensures that disabled flag or a nil input returns true"
  ^:hidden
  
  (ext-view/input-data-nil? {})
  => true
  
  (ext-view/input-data-nil? {:input {:data 1
                                      :disabled true}})
  => true)

^{:refer js.react.ext-view/output-empty? :added "0.1"}
(fact "checks that view is empty (context method)"
  ^:hidden
  
  (ext-view/output-empty? {:view {:output {:current nil}}})
  => true

  (ext-view/output-empty? {:view {:output {:current []}}})
  => true

  (ext-view/output-empty? {:view {:output {:current [1 2 3]}}})
  => false)
