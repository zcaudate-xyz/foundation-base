(ns tahto.runtime.neovim
  (:require [std.lib :as h]
            [tahto.runtime.neovim.impl :as impl])
  (:refer-clojure :exclude [eval]))

(h/intern-in
 impl/neovim
 impl/neovim:create
 impl/raw-eval-neovim)
