(ns js.blessed.frame
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [js.react :as r]
             [js.lib.chalk :as chalk]
             [js.blessed.frame-console :as frame-console]
             [js.blessed.frame-linemenu :as frame-linemenu]
             [js.blessed.frame-sidemenu :as frame-sidemenu]
             [js.blessed.frame-status :as frame-status]]})


