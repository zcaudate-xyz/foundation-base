(ns hara.runtime.neovim-test
  (:require [hara.lang :as l]
            [hara.runtime.neovim.impl :as impl]
            [hara.runtime.basic.type-common :as common])
  (:use code.test))

(def +nvim-available+
  (delay (common/program-exists? "nvim")))

(l/script- :lua
  {:runtime :neovim})

^{:refer hara.runtime.neovim.impl/neovim :added "4.1"}
(fact "starts and stops a neovim runtime"
  (when @+nvim-available+
    (let [rt (impl/neovim {})]
      [(boolean rt)
       (boolean (impl/raw-eval-neovim rt "return 42"))
       (do (std.lib.component/stop rt)
           true)]))
  => (when @+nvim-available+
       [true true true]))

^{:refer hara.runtime.neovim.impl/raw-eval-neovim :added "4.1"}
(fact "evaluates lua in neovim"
  (when @+nvim-available+
    (let [rt (impl/neovim {})]
      (try
        [(impl/raw-eval-neovim rt "return 1 + 2 + 3")
         (number? (impl/raw-eval-neovim rt "return vim.api.nvim_create_buf(false, true)"))]
        (finally
          (std.lib.component/stop rt)))))
  => (when @+nvim-available+
       [6 true]))

^{:refer hara.lang/script- :added "4.1"}
(fact "uses neovim runtime through hara.lang"
  (when @+nvim-available+
    (try
      [(!.lua (+ 1 2 3))
       (number? (!.lua (vim.api.nvim_create_buf false true)))]
      (finally
        (l/rt:stop :lua))))
  => (when @+nvim-available+
       [6 true]))
