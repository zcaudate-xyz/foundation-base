(ns js.blessed
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [merge list map]))

(l/script :js
  {:require [[js.react :as r]]
   :import [["blessed" :as Blessed]
            ["blessed-contrib" :as [* BlessedContrib]]
            ["react-blessed" :as ReactBlessed]
            ["react-blessed-contrib" :as [* ReactBlessedContrib]]
            ["drawille" :as Drawille]
            ["bresenham" :as Bresenham]]})

;;
;; Blessed Contrib
;;

(def +blessed+
  '[ANSIImage BigText Box Button Checkbox Element FileManager
    Form Image Input Layout Line List ListBar ListTable Listbar
    Loading Log Message Node OverlayImage PNG Program ProgressBar
    Prompt Question RadioButton RadioSet Screen ScrollableBox
    ScrollableText Table Terminal Text Textarea Textbox Tput Video
    
    aliases ansiimage asort attrToBinary bigtext box button
    checkbox classes cleanTags colors dropUnicode element
    escape filemanager findFile form generateTags helpers hsort
    image input layout line list listbar listtable loading log
    merge message node overlayimage parseTags png program progressbar
    prompt question radiobutton radioset screen scrollablebox
    scrollabletext sprintf stripTags table terminal text textarea
    textbox tput tryRead unicode video widget])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Blessed"
                                   :tag "js"}]
  +blessed+)

;;
;; Blessed Contrib
;;

(def +blessed-contrib+
  '[InputBuffer OutputBuffer bar canvas carousel createScreen
    donut gauge gaugeList grid lcd line log map markdown picture
    serverError sparkline stackedBar table tree])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "BlessedContrib"
                                   :tag "js"}]
  +blessed-contrib+)


;;
;; React Blessed
;;

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactBlessed"
                                   :tag "js"}]
  [createBlessedRenderer
   [renderBlessed render]])

;;
;; React Blessed Contrib
;;

(def +react-blessed-contrib+
  '[Bar Canvas Carousel Donut Gauge GaugeList Grid GridItem
    Lcd Line Log Map Markdown Picture Sparkline StackedBar
    Table Tree createBlessedComponent])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ReactBlessedContrib"
                                   :tag "js"}]
  +react-blessed-contrib+)


;;
;; Drawille
;;

(defmacro.js canvas
  "creates a drawille canvas"
  {:added "4.0"}
  [w h]
  (list 'new 'Drawille w h))

(def$.js line Bresenham)

;;
;;
;;

(defn.js createScreen
  "creates a screen"
  {:added "4.0"}
  ([title options]
   (const s (-/screen
             (Object.assign
              {:autoPadding true
               :smartCSR true
               :useBCE true
               :sendFocus true
               :dockBorders true
               :grabKeys true
               :debug true
               :title title
               :cursor {:artificial true,
                        :shape {:bg "yellow"
                                :fg "white",
                                :bold true}
                        :blink true}}
              options)))
   (s.key ["q" "C-c" "Esc"]
            (fn []
              (. this (destroy))))
   (return s)))

(defn.js ^{:style/indent 1}
  run
  "runs the component in the shell"
  {:added "4.0"}
  [element title options]
  (-/renderBlessed element (-/createScreen title options)))

