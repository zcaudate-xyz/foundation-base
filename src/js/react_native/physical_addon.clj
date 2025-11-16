(ns js.react-native.physical-addon
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.core :as j]
             [js.react-native :as n :include [:fn]]
             [xt.lang.base-lib :as k]]})

(defn.js tagBase
  "base for tag single and tag all"
  {:added "4.0"}
  [#{[style
      (:.. rprops)]}]
  (return
   #{[:component n/TextInput
      :editable false
      :style [{:cursor "default"
               :width 50
               :fontSize 12}
              (n/PlatformSelect {:ios {:fontFamily "Courier"}
                                 :default {:fontFamily "monospace"}})
	      (n/PlatformSelect {:web {:userSelect "none"}})
              (:.. (j/arrayify style))]
      (:.. rprops)]}))

(defn.js tagSingle
  "display a single indicator"
  {:added "4.0"}
  [#{[indicator
      transformations
      (:.. rprops)]}]
  (return (j/assign (-/tagBase rprops)
                    {:transformations
                     (j/assign {indicator (fn [v]
                                            (return {:value (j/toFixed v 4)}))}
                               transformations)})))

(defn.js tagAll
  "display all indicators"
  {:added "4.0"}
  [props]
  (var #{[transformations
          keys
          (:.. rprops)]} (or props {}))
  (return (j/assign (-/tagBase rprops)
                    {:multiline true
                     :transformations
                     (fn [m]
                       (var display (:? keys
                                        (k/obj-pick m keys)
                                        m))
                       (return {:value (n/format-entry display)}))})))

