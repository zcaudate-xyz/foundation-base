(ns hara.runtime.gimp-test
  (:require [hara.lang :as l]
            [hara.runtime.gimp.impl :as impl]
            [std.lib.component :as component]
            [std.lib.env :as env])
  (:use code.test))

(l/script- :python
  {:runtime :gimp
   :test-mode true})

(fact:global
 {:skip (not (or (env/program-exists? "gimp")
                 (env/program-exists? "gimp-console")))
  :setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})


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
       (string? (impl/raw-eval-gimp rt "OUT = str(Gimp.version())"))]
      (finally
        (component/stop rt))))
  => [6 true])

^{:refer hara.lang/script- :added "4.1"}
(fact "uses gimp runtime through hara.lang"
  (try
    [(!.py (+ 1 2 3))
     (string? @(!.py (str (Gimp.version))))]
    (finally
      (l/rt:stop :python)))
  => [6 true])
