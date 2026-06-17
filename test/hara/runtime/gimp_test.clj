(ns hara.runtime.gimp-test
  (:require [hara.runtime.gimp.impl :as impl]
            [std.lib.component :as component]
            [std.lib.env :as env])
  (:use code.test))

(fact:global {:skip (not (or (env/program-exists? "gimp")
                             (env/program-exists? "gimp-console")))})

^{:refer hara.runtime.gimp.impl/gimp :added "4.1"}
(fact "starts and stops a gimp runtime"
  (let [rt (impl/gimp {})]
    [(boolean rt)
     (boolean (impl/raw-eval-gimp rt "OUT = 1 + 2 + 3"))
     (do (component/stop rt)
         true)])
  => [true true true])

^{:refer hara.runtime.gimp.impl/raw-eval-gimp :added "4.1"}
(fact "evaluates python in gimp"
  (let [rt (impl/gimp {})]
    (try
      [(impl/raw-eval-gimp rt "OUT = 1 + 2 + 3")
       (string? (impl/raw-eval-gimp rt "OUT = str(pdb.gimp_version())"))]
      (finally
        (component/stop rt))))
  => [6 true])
