(ns js.react-native.helper-roller
  (:require [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.common-lib :as k] [xt.lang.spec-base :as xt] [xt.lang.common-data :as xtd] [js.react :as r] [js.react-native.animate :as a] [js.react-native.model-roller :as model-roller]]})

(defn.js useRoller
  "roller model for slider and spinner"
  {:added "4.0"}
  [#{[(:= index 0)
      (:= radius 10)
      items
      divisions]}]
  (var labels   (r/const (xtd/arr-map (xtd/arr-range divisions)
                                     (fn:> [i] (new a/Value i)))))
  (var labelsLu (r/const (xtd/arr-juxt labels
                                      (fn:> [v] (+ "i" v._value))
                                      k/identity)))
  (var indexRef (r/ref index))
  (r/watch [index]
    (r/curr:set indexRef index))
  (var offset    (a/useIndexIndicator
                  index
                  {:default {:duration 150}}
                  (fn [progress #{status}]
                    (when (== status "stopped")
                      (model-roller/roller-set-values
                       labels
                       divisions
                       (r/curr indexRef)
                        (xt/x:len items))))))
  (var modelFn (r/const (model-roller/roller-model divisions radius)))
  (r/init []
    (model-roller/roller-set-values
     labels
     divisions
     index
     (xt/x:len items)))
  (return #{labels
            labelsLu
            offset
            modelFn}))
