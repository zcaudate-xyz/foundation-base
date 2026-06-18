(ns hara.runtime.neovim-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.lang.type-shared :as shared]
            [hara.runtime.neovim.impl :as impl]
            [std.lib.env :as env]))

(l/script- :lua
  {:runtime :neovim
   :test-mode true})

(fact:global {:skip (not (env/program-exists? "nvim"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer hara.runtime.neovim.impl/neovim :added "4.1"}
(fact "starts and stops a neovim runtime"

      (let [rt (impl/neovim {})]
        [(boolean rt)
         (boolean (impl/raw-eval-neovim rt "return 42"))
         (do (std.lib.component/stop rt)
             true)])
  => [true true true])

^{:refer hara.runtime.neovim.impl/raw-eval-neovim :added "4.1"}
(fact "evaluates lua in neovim"
  (let [rt (impl/neovim {})]
    (try
      [(impl/raw-eval-neovim rt "return 1 + 2 + 3")
       (number? (impl/raw-eval-neovim rt "return vim.api.nvim_create_buf(false, true)"))]
      (finally
        (std.lib.component/stop rt))))
  => [6 true])

^{:refer hara.lang/script- :added "4.1"}
(fact "uses neovim runtime through hara.lang"
  (try
    [(!.lua (+ 1 2 3))
     (number? (!.lua (vim.api.nvim_create_buf false true)))]
    (finally
      (l/rt:stop :lua)))
  => [6 true])

^{:refer hara.runtime.neovim.impl/neovim-shared :added "4.1"}
(fact "two shared neovim runtimes with the same id share the process"
  (let [rt1 (impl/neovim-shared:create {:id :shared-neovim-test})
        rt2 (impl/neovim-shared:create {:id :shared-neovim-test})]
    (try
      (std.lib.component/start rt1)
      (std.lib.component/start rt2)
      [(= (shared/rt-get-inner rt1) (shared/rt-get-inner rt2))
       (boolean (:process (shared/rt-get-inner rt1)))
       (impl/raw-eval-neovim (shared/rt-get-inner rt1) "return 1 + 2 + 3")]
      (finally
        (std.lib.component/stop rt1)
        (std.lib.component/stop rt2))))
  => [true true 6])

(fact "stopping one shared neovim runtime keeps the process alive"
  (let [rt1 (impl/neovim-shared:create {:id :shared-neovim-ref-test})
        rt2 (impl/neovim-shared:create {:id :shared-neovim-ref-test})]
    (try
      (std.lib.component/start rt1)
      (std.lib.component/start rt2)
      (std.lib.component/stop rt1)
      (impl/raw-eval-neovim (shared/rt-get-inner rt2) "return 1 + 2 + 3")
      (finally
        (std.lib.component/stop rt2))))
  => 6)

(fact "shared neovim runtimes with different ids do not share a process"
  (let [rt1 (impl/neovim-shared:create {:id :neovim-a})
        rt2 (impl/neovim-shared:create {:id :neovim-b})]
    (try
      (std.lib.component/start rt1)
      (std.lib.component/start rt2)
      (not= (shared/rt-get-inner rt1) (shared/rt-get-inner rt2))
      (finally
        (std.lib.component/stop rt1)
        (std.lib.component/stop rt2))))
  => true)
