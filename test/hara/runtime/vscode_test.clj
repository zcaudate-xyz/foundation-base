(ns hara.runtime.vscode-test
  (:use code.test)
  (:require [hara.lang :as h]
            [hara.lang.type-shared :as shared]
            [hara.runtime.vscode :as vscode]
            [hara.runtime.vscode.impl :as impl]
            [std.lib.env :as env]))

(fact:global {:skip (not (env/program-exists? "code"))})

^{:refer hara.runtime.vscode/vscode :added "4.1" :timeout 60000}
(fact "starts and stops a vscode runtime"
  (let [rt (vscode/vscode {})]
    [(boolean rt)
     (boolean (vscode/raw-eval-vscode rt "1 + 2 + 3"))
     (do (std.lib.component/stop rt)
         true)])
  => [true true true])

^{:refer hara.runtime.vscode/raw-eval-vscode :added "4.1" :timeout 60000}
(fact "evaluates js in vscode"
  (let [rt (vscode/vscode {})]
    (try
      [(vscode/raw-eval-vscode rt "1 + 2 + 3")
       (vscode/raw-eval-vscode rt "typeof Array")]
      (finally
        (std.lib.component/stop rt))))
  => [6 "function"])

^{:refer hara.runtime.vscode.impl/vscode-shared:create :added "4.1" :timeout 60000}
(fact "two shared vscode runtimes with the same id share the process"
  (let [rt1 (impl/vscode-shared:create {:id :shared-vscode-test})
        rt2 (impl/vscode-shared:create {:id :shared-vscode-test})]
    (try
      (std.lib.component/start rt1)
      (std.lib.component/start rt2)
      [(= (shared/rt-get-inner rt1) (shared/rt-get-inner rt2))
       (boolean (:process (shared/rt-get-inner rt1)))
       (impl/raw-eval-vscode (shared/rt-get-inner rt1) "1 + 2 + 3")]
      (finally
        (std.lib.component/stop rt1)
        (std.lib.component/stop rt2))))
  => [true true 6])

^{:timeout 60000}
(fact "stopping one shared vscode runtime keeps the process alive"
  (let [rt1 (impl/vscode-shared:create {:id :shared-vscode-ref-test})
        rt2 (impl/vscode-shared:create {:id :shared-vscode-ref-test})]
    (try
      (std.lib.component/start rt1)
      (std.lib.component/start rt2)
      (std.lib.component/stop rt1)
      (impl/raw-eval-vscode (shared/rt-get-inner rt2) "1 + 2 + 3")
      (finally
        (std.lib.component/stop rt2))))
  => 6)

^{:timeout 120000}
(fact "shared vscode runtimes with different ids do not share a process"
  (let [rt1 (impl/vscode-shared:create {:id :vscode-a})
        rt2 (impl/vscode-shared:create {:id :vscode-b})]
    (try
      (std.lib.component/start rt1)
      (std.lib.component/start rt2)
      (not= (shared/rt-get-inner rt1) (shared/rt-get-inner rt2))
      (finally
        (std.lib.component/stop rt1)
        (std.lib.component/stop rt2))))
  => true)
