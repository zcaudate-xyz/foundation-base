(ns std.lang.base.script-control-test
  (:use code.test)
  (:require [std.lang.base.script-control :refer :all]
            [std.lang.base.runtime :as rt]
            [std.lib :as h]
            [std.lang.base.util :as ut]
            [std.lang.model.spec-lua]))

(rt/install-lang! :lua)

^{:refer std.lang.base.script-control/script-rt-get :added "4.0"}
(fact "gets the current runtime"

  (rt/install-lang! :lua)
  (script-rt-get :lua :default {})
  => rt/rt-default?

  (h/p:space-context-list)
  => (contains '[:lang/lua])

  (h/p:registry-rt-list :lang/lua)
  => (contains '(:default))

  
  (do (script-rt-stop :lua)
      (h/p:space-rt-active))
  => [])

^{:refer std.lang.base.script-control/script-rt-stop :added "4.0"}
(fact "stops the current runtime"
  (script-rt-stop :lua) => any?)

^{:refer std.lang.base.script-control/script-rt-restart :added "4.0"}
(fact "restarts a given runtime"
  (script-rt-restart :lua) => rt/rt-default?)

^{:refer std.lang.base.script-control/script-rt-oneshot-eval :added "4.0"}
(fact "oneshot evals a statement"
  (script-rt-oneshot-eval :default :lua ['(return 1)]) => "return 1")

^{:refer std.lang.base.script-control/script-rt-oneshot :added "4.0"}
(fact "for use with the defmacro.! function"
  (script-rt-oneshot :default (ut/lang-pointer :lua {:module 'L.core}) []) => (throws))
