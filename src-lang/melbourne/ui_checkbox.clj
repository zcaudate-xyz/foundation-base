(ns melbourne.ui-checkbox
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r :include [:fn]]
             [js.react-native :as n]
             [js.react-native.ui-check-box :as ui-check-box]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [melbourne.base-palette :as base-palette]
             [melbourne.base-theme :as base-theme]
             [melbourne.base-font :as base-font]
             [melbourne.ui-static :as ui-static]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js CheckBox
  "creates a checkbox"
  {:added "0.1"}
  [#{[design
      variant
      style
      theme
      (:.. rprops)]}]
  (var __variant
       (data/obj-assign-nested
        {:fg   {:key "neutral"}
         :bg   {:key "background"}
         :pressed {:bg {:key "primary"}}
         :highlighted {:fg {:key "neutral"}
                       :bg {:key "background"
                            :tone "darken"
                            :ratio 1}}
         :active  {:fg {:key "background"}
                   :bg {:key "primary"}}}
        variant))
  (var __style (base-font/getFontStyle (or (. __variant font)
                                             "h6")))
  (var __theme  (Object.assign (base-theme/themeUiInput
                           (base-palette/designPalette design)
                           __variant)
                          theme))
  (return
   [:% ui-check-box/CheckBox
    #{[:theme __theme
       :outlined true
       :style [{:paddingHorizontal 3
                }
               __style
               (:.. (data/arrayify style))]
       (:.. rprops)]}]))

(defn.js CheckGroupIndexed
  "creates a group of check boxes"
  {:added "0.1"}
  ([#{[design
       variant
       theme
       items
       setIndices
       indices
       onChange
       style
       styleText
       styleContainer
       (:= itemProps [])
       (:= format lib/identity)]}]
   (var itemFn
        (fn [value i]
          (return [:% n/View
                   {:key   value
                    :style {:flexDirection "row"
                            :alignItems "center"
                            :padding 2}}
                   [:% -/CheckBox
                    #{[design
                       variant
                       theme
                       style
                       :selected (. indices [i])
                       :onPress (fn []
                                  (var changed
                                       (j/map indices
                                              (fn [e ei]
                                                (return (:? (== ei i) (not e) e)))))
                                  (setIndices changed)
                                  (if onChange (onChange changed)))
                       (:.. (or (. itemProps [i])
                                {}))]}]
                   [:% ui-static/Text
                    {:design design
                     :variant (or (data/get-in design
                                            ["variant" "text"])
                                  {:fg {:key "neutral"
                                        :mix "primary"
                                        :ratio 4}})
                     :style styleText}
                    (format value i)]])))
   (return [:% n/View
            {:style styleContainer}
            (j/map items itemFn)])))

(defn.js CheckGroup
  "creates a group of check boxes"
  {:added "0.1"}
  ([#{[data
       valueFn
       values
       setValues
       (:.. rprops)]}]
   (let [#{setIndices
           items
           indices} (r/convertIndices #{data
                                        valueFn
                                        values
                                        setValues})]
     (return [:% -/CheckGroupIndexed
              #{[setIndices
                 items
                 indices
                 (:.. rprops)]}]))))

(def.js MODULE (!:module))

(comment
  )
