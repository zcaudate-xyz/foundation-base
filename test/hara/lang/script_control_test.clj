(ns hara.lang.script-control-test
  (:require [hara.lang :as l]
            [hara.lang.runtime :as rt]
            [hara.lang.script-control :refer :all]
            [hara.common.util :as ut]
            [hara.model.spec-lua]
            [std.lib.context.registry :as reg]
            [std.lib.context.space :as space])
  (:use code.test))

^{:refer hara.lang.script-control/script-rt-get :added "4.0"}
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

^{:refer hara.lang.script-control/script-rt-stop :added "4.0"}
(fact "stops the current runtime"

  (script-rt-stop :lua) => any?)

^{:refer hara.lang.script-control/script-rt-restart :added "4.0"}
(fact "restarts a given runtime"

  (script-rt-restart :lua)
  => map?)

^{:refer hara.lang.script-control/script-rt-oneshot-eval :added "4.0"}
(comment "oneshot evals a statement"

  (script-rt-oneshot-eval
   :default
   :lua ['(return 1)])
  => "return 1")

^{:refer hara.lang.script-control/script-rt-oneshot :added "4.0"}
(fact "for use with the defmacro.! function"

  (script-rt-oneshot
   :default
   (ut/lang-pointer :lua {:module 'L.core}) [])
  => (throws))


^{:refer hara.lang.script-control/script-rt-prep :added "4.0"}
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