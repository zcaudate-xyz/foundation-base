(ns lang.runtime.annex.neovim
  (:require [std.lib :as h]
            [lang.runtime.annex.neovim.impl :as impl])
  (:refer-clojure :exclude [eval]))

(h/intern-in
 impl/neovim
 impl/neovim:create
 impl/raw-eval-neovim)
