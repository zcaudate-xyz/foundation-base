(ns pune.ui-depthchart
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react-native :as n :include [:fn :svg]]
             [js.react :as r :include [:fn]]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [melbourne.base-palette :as base-palette]
             [pune.ui-sparkline :as ui-sparkline]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js get-depth-histogram
  "gets the histogram depth"
  {:added "0.1"}
  [domain lu step cmp]
  (var out [0])
  (var i (data/first domain))
  (while (cmp i (data/last domain))
    (var x (+ (data/last out)
              (or (. lu [i]) 0)))
    (x:arr-push out x)
    (:= i (+ i step)))
  (return out))

(defn.js get-depth-data
  "gets the histogram data"
  {:added "0.1"}
  [offers]
  (var #{buy sell} offers)
  (var buy-domain  (:? (data/is-empty? buy)
                         []
                         [(data/first (data/last buy))
                          (data/first (data/first buy))]))
  (var sell-domain (:? (data/is-empty? sell) []
                       [(data/first (data/last sell))
                        (data/first (data/first sell))]))
  (var max-steps  (Math.max (:? (data/is-empty? buy-domain)
                             0
                             (- (data/second buy-domain)
                                (data/first buy-domain)))
                         (:? (data/is-empty? sell-domain)
                             0
                             (- (data/second sell-domain)
                                (data/first sell-domain)))
                         10))
  (var max-depth  (Math.max (data/arr-foldl buy
                                      (fn:> [acc [_ vol]]
                                        (+ acc vol))
                                      0)
                         (data/arr-foldl sell
                                      (fn:> [acc [_ vol]]
                                        (+ acc vol))
                                      0)
                         100))
  
  (var buy-lu  (data/arr-juxt buy  data/first data/second))
  (var sell-lu (data/arr-juxt sell data/first data/second))
  (var buy-hist
       (:? (data/is-empty? buy)
           (data/arr-repeat 0 max-steps)
           (-/get-depth-histogram [(data/first buy-domain)
                                   (+ (data/first buy-domain)
                                      max-steps)]
                                  buy-lu 1 lib/lte)))
  (var sell-hist
       (:? (data/is-empty? sell)
           (data/arr-repeat 0 max-steps)
           (-/get-depth-histogram [(data/last sell-domain)
                                   (- (data/last sell-domain)
                                      max-steps)]
                                  sell-lu -1 lib/gte)))
  
  (return #{buy-domain sell-domain max-depth max-steps buy-lu sell-lu
            buy-hist sell-hist}))

(defn.js MarketDepthChart
  "market ladder row"
  {:added "0.1"}
  [#{[design
      offers
      #_market
      control]}]
  (var #{[(:= allotment 100)
          (:= decimal 0)
          (:= trade "buy")
          (:= prediction "yes")
          fraction]} control)
  
  #_
  (:= offers (or offers (base-market/live-offers-rate market
                                                      allotment
                                                      prediction
                                                      20)))
  (var m (-/get-depth-data offers))
  (var #{max-depth buy-hist sell-hist} m)
  (return
   [:% n/Row
    [:% ui-sparkline/Sparkline
     #{[:design design
        :variant {:bg {:key (:? (== prediction "yes") "primary" "error")
                       :mix "background"
                       :ratio 3}
                  :fg {:key "neutral"}}
        :style {:paddingVertical 4
                  :paddingLeft 2}
        :pathStyle {:strokeWidth 1}
        :height 12
        :width  55
        :maxValue max-depth
        :minValue 1
        :values (data/arr-reverse sell-hist)]}]
    [:% ui-sparkline/Sparkline
     #{[:design design
        :variant {:bg {:key "neutral"
                       :mix "background"
                       :ratio 3}
                  :fg {:key "neutral"}}
        :style {:paddingVertical 4
                :paddingRight 2}
        :pathStyle {:strokeWidth 1}
        :height 12
        :width  50
        :maxValue max-depth
        :minValue 1
        :values buy-hist]}]]))

(def.js MODULE (!:module))
