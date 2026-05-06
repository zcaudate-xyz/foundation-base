(ns js.react-native.ui-radio-box
  (:require [hara.lang :as l]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.react-native :as n]
             [js.react-native.physical-base :as physical-base]
             [js.react-native.helper-theme-default :as helper-theme-default]
             [js.react-native.helper-theme :as helper-theme]]})

(defn.js radioBoxTheme
  "creates a radio box theme"
  {:added "4.0"}
  [#{[theme
      themePipeline
      transformations
      (:.. rprops)]}]
  (var __theme (xt/x:obj-assign {} helper-theme-default/RadioBoxDefaultTheme theme))
  (var __themePipeline (xt/x:obj-assign {} helper-theme-default/BinaryDefaultPipeline themePipeline))
  (var #{outside inside} transformations)
  (var [bgStyleStatic bgTransformFn]
       (helper-theme/prepThemeCombined
        #{[:theme __theme
           :themePipeline __themePipeline
           :transformations outside
           (:.. rprops)]}))
  (var [fgStyleStatic fgTransformFn]
       (helper-theme/prepThemeSingle
        #{[:theme __theme
           :themePipeline __themePipeline
           :transformations (xt/x:obj-assign
                             {:fg (fn:> [#{active}]
                                    {:style {:opacity   active
                                             :transform [{:scale (+ 0.2 (* 0.8 active))}]}})}
                             inside)
           (:.. rprops)]}
        "fg"
        ["backgroundColor"]))
  (return #{bgStyleStatic bgTransformFn
            fgStyleStatic fgTransformFn}))

(defn.js RadioBox
  "creates a radio box"
  {:added "0.1"}
  [#{[selected
      setSelected
      theme
      themePipeline
      insideProps
      insideStyle
      outsideProps
      outsideStyle
      (:= transformations {})
      (:= size 24)
      (:= sizeInside 10)
      (:= inner [])
      (:.. rprops)]}]
  (var #{bgStyleStatic bgTransformFn
         fgStyleStatic fgTransformFn} (-/radioBoxTheme
                                       #{[theme
                                          themePipeline
                                          transformations
                                          (:.. rprops)]}))
  (return
   [:% physical-base/TouchableBinary
    #{[:active selected
       :onPress (fn []
                  (when setSelected
                    (setSelected (not selected))))
       :inner [(xt/x:obj-assign
                {:component n/View
                 :key "outside"
                 :style [{:borderRadius (/ size 2)
                          :height size
                          :width size
                          :borderStyle "solid" 
                          :borderWidth 2}
                         bgStyleStatic
                         (:.. (xtd/arrayify outsideStyle))]
                 :transformations  bgTransformFn}
                outsideProps)
               (xt/x:obj-assign
                {:component n/View
                 :key "inside"
                 :style [{:position "absolute"
                          :borderRadius (/ sizeInside 2)
                          :top  (/ (- size sizeInside) 2)
                          :left (/ (- size sizeInside) 2)
                          :height sizeInside
                          :width sizeInside}
                         fgStyleStatic
                         (:.. (xtd/arrayify insideStyle))]
                 :transformations fgTransformFn}
                insideProps)
               (:.. inner)]
       (:.. rprops)]}]))

