(ns hara.runtime.neovim-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.neovim.impl :as impl]
            [std.lib.env :as env]))

(l/script- :lua
  {:runtime :neovim})

(fact:global {:skip (not (env/program-exists? "nvim"))})

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
