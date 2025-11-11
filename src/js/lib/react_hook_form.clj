(ns js.lib.react-hook-form
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:macro-only true
   :bundle  {:default [["react-hook-form" :as [* ReactHookForm]]]}
   :import [["react-hook-form" :as [* ReactHookForm]]]
   :require [[js.react :as r]
             [xt.lang.base-lib :as k]]})
