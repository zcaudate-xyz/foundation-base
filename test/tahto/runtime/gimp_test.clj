(ns tahto.runtime.gimp-test
  (:require [tahto.core :as l]
            [tahto.core.script-control :as script-control]
            [tahto.runtime.gimp :as gimp]
            [tahto.runtime.gimp.impl :as impl]
            [std.lib.component :as component]
            [std.lib.env :as env])
  (:use code.test))

(l/script- :python
  {:runtime :gimp
   :test-mode true})

(defn- gimp-runtime-available? []
  (try
    (and (or (env/program-exists? "gimp")
             (env/program-exists? "gimp-console"))
         (do (script-control/script-rt-get :python :gimp {})
             true))
    (catch Throwable _
      false)))

(fact:global
 {:skip (not (gimp-runtime-available?))
  :setup    [(l/rt:restart :python)]
  :teardown [(l/rt:stop :python)]})


^{:refer tahto.runtime.gimp.impl/gimp :added "4.1"}
(fact "starts and stops a gimp runtime"
  (let [rt (impl/gimp {})]
    [(boolean rt)
     (boolean (impl/raw-eval-gimp rt "OUT = 1 + 2 + 3"))
     (do (component/stop rt)
         true)])
  => [true true true])

^{:refer tahto.runtime.gimp.impl/raw-eval-gimp :added "4.1"}
(fact "evaluates python in gimp"
  (let [rt (impl/gimp {})]
    (try
      [(impl/raw-eval-gimp rt "OUT = 1 + 2 + 3")
       (string? (impl/raw-eval-gimp rt "OUT = str(Gimp.version())"))]
      (finally
        (component/stop rt))))
  => [6 true])

^{:refer tahto.core/script- :added "4.1"}
(fact "uses gimp runtime through tahto.core"
  [(!.py (+ 1 2 3))
   (string? @(!.py (str (Gimp.version))))]
  => [6 true])
