(ns melbourne.addon-debug
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react :as r :include [:fn]]
             [js.react-native :as n]]
   :export [MODULE]})

(defn.js addonCounter
  "creates a debug counter"
  {:added "4.0"}
  []
  (return
   {:component n/View
    :style {:position "absolute"
            :left 5
            :top -5}
    :children [[:% n/Text
                {:key "debug"
                 :style [{:backgroundColor "red"
                          :fontSize 9
                          :color "white"
                          :padding 2}
                         (n/PlatformSelect {:ios {:fontFamily "Courier"}
                                            :default {:fontFamily "monospace"}})]}
                (+ "COUNT: " ((r/useGetCount)))]]}))

(def.js MODULE (!:module))
