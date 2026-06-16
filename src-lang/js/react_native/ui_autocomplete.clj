(ns js.react-native.ui-autocomplete
  (:require [hara.lang :as l])
  (:use code.test))

(l/script :js
  {:require [[js.react :as r] [js.react-native :as n] [js.react-native.ui-tooltip :as ui-tooltip] [js.react.ext-view :as ext-view] [xt.lang.spec-base :as xt] [xt.lang.common-data :as xtd] [xt.lang.common-tree :as xtt]]})

(defn.js AutocompleteModal
  "creates the autocomplete modal display"
  {:added "4.0"}
  [#{[hostRef
      visible
      setVisible
      isBusy
      styleContainer
      entries
      (:= componentBusy n/View)
      (:= componentEmpty n/View)
      component
      (:.. rprops)]}]
  (var [dims setDims] (r/local {}))
  (r/watch [visible]
    (n/measureRef hostRef setDims))
  (return
   [:% ui-tooltip/Tooltip
    {:hostRef hostRef
     :visible visible
     :setVisible setVisible
     :position "bottom"
     :alignment "start"
     :arrow {:placement "none"}}
    [:% n/View
     {:style [{:width (. dims width)}
              (:.. (xtd/arrayify styleContainer))]}
     (:? isBusy
         (r/% componentBusy rprops)

         (xtd/is-empty? entries)
         (r/% componentEmpty rprops)

         :else
         (xt/x:arr-map entries
                (fn [entry i]
                  (return
                   (r/% component (xt/x:obj-assign #{entry {:key i}}
                                            rprops))))))]]))

(defn.js Autocomplete
  "creates the autocomplete"
  {:added "4.0"}
  [#{[sourceView
      sourceInput
      (:.. rprops)]}]
  (var entries (ext-view/listenView sourceView "success"))
  (var isBusy  (ext-view/listenView sourceView "pending"))
  (var refInput (r/ref))
  (r/watch [sourceInput isBusy]
    (when (and (not isBusy)
               (not (xtt/eq-nested sourceInput (. refInput current))))
      (ext-view/refresh-args sourceView sourceInput)
      (r/curr:set refInput sourceInput)))
  (return
   (r/% -/AutocompleteModal
        (xt/x:obj-assign #{entries isBusy} rprops))))
