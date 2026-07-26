(ns tahto.core.script-control-test
  (:require [tahto.core :as l]
            [tahto.core.runtime :as rt]
            [tahto.core.script-control :refer :all]
tahto/lang/script_control_test.clj:5:            [tahto.common.util :as ut]
tahto/lang/script_control_test.clj:6:            [tahto.model.spec-lua]
            [std.lib.context.registry :as reg]
            [std.lib.context.space :as space])
  (:use code.test))

^{:refer tahto.core.script-control/script-rt-get :added "4.0"}
(fact "gets the current runtime"

  (script-rt-get :lua :default {})
  => map?

  (space/space:context-list)
  => (contains '[:lang/lua])

  (reg/registry-rt-list :lang/lua)
  => (contains '(:default))


  (do (script-rt-stop :lua)
      (space/space:rt-active))
  => [])

^{:refer tahto.core.script-control/script-rt-stop :added "4.0"}
(fact "stops the current runtime"

  (script-rt-stop :lua) => any?)

^{:refer tahto.core.script-control/script-rt-restart :added "4.0"}
(fact "restarts a given runtime"

  (script-rt-restart :lua)
  => map?)

^{:refer tahto.core.script-control/script-rt-oneshot-eval :added "4.0"}
(comment "oneshot evals a statement"

  (script-rt-oneshot-eval
   :default
   :lua ['(return 1)])
  => "return 1")

^{:refer tahto.core.script-control/script-rt-oneshot :added "4.0"}
(fact "for use with the defmacro.! function"

  (script-rt-oneshot
   :default
   (ut/lang-pointer :lua {:module 'L.core}) [])
  => (throws))


^{:refer tahto.core.script-control/script-rt-prep :added "4.0"}
(fact "prepares a runtime context without starting it"

  (do (script-rt-stop :lua)
      (space/space:context-unset (ut/lang-context :lua))
      (space/space:rt-active))
  => []

  (let [[sp ctx] (script-rt-prep :lua :default {})]
    [(space/space? sp) ctx])
  => [true :lang/lua]

  (space/space:context-list)
  => (contains '[:lang/lua])

  (do (script-rt-stop :lua)
      (space/space:context-unset (ut/lang-context :lua))
      (space/space:rt-active))
  => [])