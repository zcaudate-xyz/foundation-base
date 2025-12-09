(ns indigo.client.ui-radix
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r :include [:dom :fn]]
             [xt.lang.base-lib :as k]
             [xt.lang.base-client :as client]]
   })
