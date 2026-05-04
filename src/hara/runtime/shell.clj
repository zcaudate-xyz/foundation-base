(ns hara.runtime.shell
  (:require [hara.runtime.shell.interface-basic :as basic]
            [hara.runtime.shell.interface-remote :as remote]
            [hara.runtime.shell.suite-core :as suite]
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
