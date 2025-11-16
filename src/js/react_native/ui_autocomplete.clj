(ns js.react-native.ui-autocomplete
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core :as j]
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [js.react-native.ui-tooltip :as ui-tooltip]
             [js.react.ext-view :as ext-view]
             [xt.lang.base-lib :as k]]})

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
              (:.. (k/arrayify styleContainer))]}
     (:? isBusy
         (r/% componentBusy rprops)

         (k/is-empty? entries)
         (r/% componentEmpty rprops)

         :else
         (j/map entries
                (fn [entry i]
                  (return
                   (r/% component (j/assign #{entry {:key i}}
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
               (not (k/eq-nested sourceInput (. refInput current))))
      (ext-view/refresh-args sourceView sourceInput)
      (r/curr:set refInput sourceInput)))
  (return
   (r/% -/AutocompleteModal
        (j/assign #{entries isBusy} rprops))))

