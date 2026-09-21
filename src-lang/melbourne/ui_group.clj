(ns melbourne.ui-group
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [melbourne.ui-toggle-button :as ui-toggle-button]]
   :export [MODULE]})

;;
;; GROUP
;;

(defn.js EnumMultiIndexed
  "creates a multi-select horizontal tab bar"
  {:added "0.1"}
  ([#{[design
       variant
       theme
       items
       setIndices
       indices
       style
       onChange
       styleContainer
       (:= itemProps [])
       (:= format lib/identity)]}]
   (var itemFn
        (fn [value i]
          (return [:% ui-toggle-button/ToggleButton
                   #{[:key (+ value "-" i)
                      design variant theme
                      :style [{:marginHorizontal 5}
                              (:.. (data/arrayify style))]
                      :text (format value i)
                      :selected (. indices [i])
                      :outlined (. indices [i])
                      :onPress (fn []
                                 (var changed
                                      (j/map indices
                                             (fn [e ei]
                                               (return (:? (== ei i) (not e) e)))))
                                 (setIndices changed)
                                 (if onChange (onChange changed)))
                      (:.. (or (. itemProps [i])
                               {}))]}])))
   (return [:% n/Row
            {:style [{:margin 5}
                     (:.. (data/arrayify styleContainer))]}
            (j/map items itemFn)])))

(defn.js EnumMulti
  "creates a multi-select horizontal tab bar"
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
     (return [:% -/EnumMultiIndexed
              #{[setIndices
                 items
                 indices
                 (:.. rprops)]}]))))

;;
;; GROUP
;;

(defn.js TabsIndexed
  "creates a indexed horizontal tab bar"
  {:added "0.1"}
  ([#{[design
       variant
       theme
       items
       setIndex
       index
       style
       onChange
       styleContainer
       outlined
       (:= itemProps [])
       (:= format lib/identity)]}]
   (var itemFn
        (fn [value i]
          (return [:% ui-toggle-button/ToggleButton
                   #{[:key (+ value "-" i)
                      design variant theme
                      :style [{:marginHorizontal 5}
                              (:.. (data/arrayify style))]
                      :text (format value i)
                      :selected (== i index)
                      :onPress (fn []
                                 (when (not= i index)
                                   (setIndex i)
                                   (if onChange (onChange i))))
                      :outlined (and outlined (== i index))
                      :transformations {:bg nil}
                      (:.. (or (. itemProps [i])
                               {}))]}])))
   (return [:% n/Row
            {:style [{:margin 5}
                     (:.. (data/arrayify styleContainer))]}
            (j/map items itemFn)])))

(defn.js Tabs
  "creates a horizontal tab bar"
  {:added "0.1"}
  ([#{[data
       valueFn
       value
       setValue
       allowNotFound
       (:.. rprops)]}]
   (var #{setIndex
          items
          index} (r/convertIndex #{data
                                   valueFn
                                   value
                                   setValue
                                   allowNotFound}))
   (return [:% -/TabsIndexed
            #{[setIndex
               items
               index
               (:.. rprops)]}])))

;;
;; LIST
;;

(defn.js ListIndexed
  "creates a indexed list"
  {:added "0.1"}
  ([#{[design
       variant
       theme
       items
       style
       onChange
       onPress
       index
       setIndex
       styleContainer
       transformations
       (:= itemProps [])
       (:= format lib/identity)]}]
   (var outlined (data/get-in design ["theme" "active" "outlined"]))
   (var itemFn
        (fn [e]
          (var #{item} e)
          (var i (xt/x:get-key e "index"))
          (return [:% ui-toggle-button/ToggleButton
                   #{[:key i
                      design variant theme
                      :text (format item i)
                      :style [{:marginVertical 5
                               :alignItems "center"
                               :justifyContent "center"}
                              (:.. (data/arrayify style))]
                      :selected (== i index)
                      :onPress (fn []
                                 (when (not= i index)
                                   (setIndex i)
                                   (if onChange (onChange i)))
                                 (if onPress (onPress)))
                      :outlined (and outlined (== i index))
                      transformations
                      (:.. (or (. itemProps [i])
                               {}))]}])))
   (return [:% n/FlatList
            {:style styleContainer
             :data  items
             :keyExtractor lib/identity
             :renderItem itemFn}])))

(defn.js List
  "creates a list"
  {:added "0.1"}
  ([#{[data
       valueFn
       value
       setValue
       allowNotFound
       (:.. rprops)]}]
   (var #{setIndex
           items
           index} (r/convertIndex #{data
                                    valueFn
                                    value
                                    setValue
                                    allowNotFound}))
   (return [:% -/ListIndexed
            #{[setIndex
               items
               index
               (:.. rprops)]}])))

(def.js MODULE (!:module))
