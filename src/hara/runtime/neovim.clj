(ns hara.runtime.neovim
  (:require [std.lib :as h]
            [hara.runtime.neovim.impl :as impl])
  (:refer-clojure :exclude [eval]))

(h/intern-in
 impl/neovim
 impl/neovim:create
 impl/raw-eval-neovim)
