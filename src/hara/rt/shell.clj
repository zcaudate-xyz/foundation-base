(ns hara.rt.shell
  (:require [hara.rt.shell.interface-basic :as basic]
            [hara.rt.shell.interface-remote :as remote]
            [hara.rt.shell.suite-core :as suite]
            [hara.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [if cat]))

(f/intern-in suite/emit
             suite/ls
             suite/man
             suite/echo
             suite/cat
             suite/pwd
             suite/nc
             suite/nc:port-check
             suite/apropos
             suite/if
             suite/>>
             suite/!
             suite/notify-form
             suite/notify

             basic/with:single-line)
