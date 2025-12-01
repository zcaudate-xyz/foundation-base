(ns repro-script
  (:require [std.lang.base.script-control :as sc]
            [std.lang.base.runtime :as rt]
            [std.lib.context.space :as space]
            [std.lib.resource :as res]
            [std.lib :as h]))

(defn -main []
  (println "Installing lua...")
  (rt/install-lang! :lua)

  (println "Getting runtime...")
  (def rt (sc/script-rt-get :lua :default {}))

  (println "Runtime type:" (type rt))
  (println "Runtime value:" rt)
  (println "Is default?" (rt/rt-default? rt))

  (println "Checking spec...")
  (println "Spec:" (res/res:spec-get :hara/lang.rt))

  (println "Checking rt-default...")
  (def r (rt/rt-default {:a 1}))
  (println "rt-default result type:" (type r))
  (println "rt-default result:" r))
