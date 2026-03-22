(ns std.lib.system
  (:require [std.lib.foundation :as h]
            [std.lib.system.array :as array]
            [std.lib.system.common :as common]
            [std.lib.system.display :as display]
            [std.lib.system.partial :as partial]
            [std.lib.system.scaffold :as scaffold]
            [std.lib.system.type :as type]))

(h/intern-in   common/primitive?
               common/system?

               array/array
               array/array?

               type/system

               partial/valid-subcomponents
               partial/subsystem
               partial/wait
               partial/wait-for

               scaffold/scaffold:clear
               scaffold/scaffold:registered
               scaffold/scaffold:create
               scaffold/scaffold:new
               scaffold/scaffold:restart
               scaffold/scaffold:register
               scaffold/scaffold:all
               scaffold/scaffold:start
               scaffold/scaffold:stop
               scaffold/scaffold:deregister
               scaffold/scaffold:current
               scaffold/scaffold:stop-all)
