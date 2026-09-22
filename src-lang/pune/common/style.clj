(ns pune.common.style
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/pune-style
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[xt.lang.common-data :as data]]
   :export [MODULE]})

(def.js Light
  {:surface "#ffffff"
   :surfaceMuted "#f8fafc"
   :border "#e2e8f0"
   :text "#0f172a"
   :muted "#64748b"
   :accent "#2563eb"})

(def.js Dark
  {:surface "#0f172a"
   :surfaceMuted "#1e293b"
   :border "#334155"
   :text "#f8fafc"
   :muted "#cbd5e1"
   :accent "#60a5fa"})

(defn.js tokens
  "returns the compact Pune surface tokens for a design"
  [design]
  (return (:? (== (data/get-in design ["type"]) "dark")
              -/Dark
              -/Light)))

(defn.js controlStyle
  "returns the shared Pune control style for a design"
  [design]
  (var theme (-/tokens design))
  (return {:backgroundColor (. theme surface)
           :borderColor (. theme border)
           :color (. theme text)}))

(def.js MODULE (!:module))
