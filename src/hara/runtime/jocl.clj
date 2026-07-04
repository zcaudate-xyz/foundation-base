(ns hara.runtime.jocl
  (:require [hara.runtime.jocl.common :as common]
            [hara.runtime.jocl.exec :as exec]
            [hara.runtime.jocl.meta :as meta]
            [hara.runtime.jocl.type :as type]
            [hara.runtime.jocl.runtime :as rt]
            [std.lib.foundation :as h]))

(h/intern-in meta/platform:default
             meta/platform-info
             meta/device-info
             meta/kernel-info
             meta/queue-info
             meta/context-info
             meta/program-info

             meta/device:cpu
             meta/device:gpu

             exec/exec
             rt/jocl
             rt/jocl:create)

