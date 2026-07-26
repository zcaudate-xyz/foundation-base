(ns tahto.runtime.shell
  (:require [tahto.runtime.shell.interface-basic :as basic]
            [tahto.runtime.shell.interface-remote :as remote]
            [tahto.runtime.shell.suite-core :as suite]
            [tahto.core :as l]
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
