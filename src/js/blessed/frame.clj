(ns js.blessed.frame
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core    :as j]
             [js.react   :as r :include [:fn]]
             [js.lib.chalk :as chalk]
             [js.blessed.frame-console :as frame-console]
             [js.blessed.frame-linemenu :as frame-linemenu]
             [js.blessed.frame-sidemenu :as frame-sidemenu]
             [js.blessed.frame-status :as frame-status]]})


