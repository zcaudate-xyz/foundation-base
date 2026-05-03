(ns hara.lang.base.script-control-test
  (:require [hara.lang :as l]
            [hara.lang.base.runtime :as rt]
            [hara.lang.base.script-control :refer :all]
            [hara.lang.base.util :as ut]
            [hara.lang.model.spec-lua]
            [std.lib.context.registry :as reg]
            [std.lib.context.space :as space])
  (:use code.test))

^{:refer hara.lang.base.script-control/script-rt-get :added "4.0"}
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

^{:refer hara.lang.base.script-control/script-rt-stop :added "4.0"}
(fact "stops the current runtime"

  (script-rt-stop :lua) => any?)

^{:refer hara.lang.base.script-control/script-rt-restart :added "4.0"}
(fact "restarts a given runtime"

  (script-rt-restart :lua)
  => map?)

^{:refer hara.lang.base.script-control/script-rt-oneshot-eval :added "4.0"}
(comment "oneshot evals a statement"

  (script-rt-oneshot-eval
   :default
   :lua ['(return 1)])
  => "return 1")

^{:refer hara.lang.base.script-control/script-rt-oneshot :added "4.0"}
(fact "for use with the defmacro.! function"

  (script-rt-oneshot
   :default
   (ut/lang-pointer :lua {:module 'L.core}) [])
  => (throws))
