(ns std.lang.base.script-control-test
  (:require [std.lang :as l]
            [std.lang.base.runtime :as rt]
            [std.lang.base.script-control :refer :all]
            [std.lang.base.util :as ut]
            [std.lang.model.spec-lua]
            [std.lib.context.registry]
            [std.lib.context.space])
  (:use code.test))

^{:refer std.lang.base.script-control/script-rt-get :added "4.0"}
(fact "gets the current runtime"
  ^:hidden
  
  (script-rt-get :lua :default {})
  => map?

  (std.lib.context.space/space:context-list)
  => (contains '[:lang/lua])

  (std.lib.context.registry/registry-rt-list :lang/lua)
  => (contains '(:default))

  
  (do (script-rt-stop :lua)
      (std.lib.context.space/space:rt-active))
  => [])

^{:refer std.lang.base.script-control/script-rt-stop :added "4.0"}
(fact "stops the current runtime"
  ^:hidden
  
  (script-rt-stop :lua) => any?)

^{:refer std.lang.base.script-control/script-rt-restart :added "4.0"}
(fact "restarts a given runtime"
  ^:hidden

  (script-rt-restart :lua)
  => map?)

^{:refer std.lang.base.script-control/script-rt-oneshot-eval :added "4.0"}
(comment "oneshot evals a statement"
  ^:hidden
  
  (script-rt-oneshot-eval
   :default
   :lua ['(return 1)])
  => "return 1")

^{:refer std.lang.base.script-control/script-rt-oneshot :added "4.0"}
(fact "for use with the defmacro.! function"
  ^:hidden

  (script-rt-oneshot
   :default
   (ut/lang-pointer :lua {:module 'L.core}) [])
  => (throws))
