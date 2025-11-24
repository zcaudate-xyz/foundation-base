(ns std.lib.system.scaffold-test
  (:use code.test)
  (:require [std.lib.system.scaffold :refer :all]))

^{:refer std.lib.system.scaffold/scaffold:register :added "3.0"}
(fact "registers a scaffold in the namespace"
  (resolve 'scaffold:register) => var?)

^{:refer std.lib.system.scaffold/scaffold:deregister :added "3.0"}
(fact "deregisters a scaffold in the namespace"
  (resolve 'scaffold:deregister) => var?)

^{:refer std.lib.system.scaffold/scaffold:current :added "3.0"}
(fact "returns the current scaffold")

^{:refer std.lib.system.scaffold/scaffold:create :added "3.0"}
(fact "creates a system")

^{:refer std.lib.system.scaffold/scaffold:new :added "3.0"}
(fact "creates and starts a system")

^{:refer std.lib.system.scaffold/scaffold:stop :added "3.0"}
(fact "stops the system")

^{:refer std.lib.system.scaffold/scaffold:start :added "3.0"}
(fact "starts the system")

^{:refer std.lib.system.scaffold/scaffold:clear :added "3.0"}
(fact "clears the current system")

^{:refer std.lib.system.scaffold/scaffold:restart :added "3.0"}
(fact "restarts the system")

^{:refer std.lib.system.scaffold/scaffold:registered :added "3.0"}
(fact "lists all registered scaffolds")

^{:refer std.lib.system.scaffold/scaffold:all :added "3.0"}
(fact "lists all running scaffolds")

^{:refer std.lib.system.scaffold/scaffold:stop-all :added "3.0"}
(fact "kills all running scaffolds")

(comment
  ^{:refer std.lib.system.scaffold/wait-for :added "3.0"}
  (fact "waits for components of the system to be ready"))
