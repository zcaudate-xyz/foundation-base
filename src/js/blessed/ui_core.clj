(ns js.blessed.ui-core
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.react :as r]
             [js.blessed :as b]
             [js.blessed.ui-style :as ui-style]]})

(defn.js Enclosed
  "constructs a box with label"
  {:added "4.0"}
  [#{[label
      children
      (:.. rprops)]}]
  (return
   [:box #{[:width 30
            :height 8
            :content label
            (:.. rprops)]}
    children]))

(defn.js SmallLabel
  "Constructs a Small Label"
  {:added "4.0"}
  ([props]
   (return
    [:text #{{:bg "black"
              :shrink true
              :fg "yellow"}
             (:.. props)}])))

(defn.js MinimalButton
  "Constructs a Minimal Button"
  {:added "4.0"}
  ([#{[color
       disabled
       hidden
       busy
       content
       onClick
       (:.. rprops)]}]
   (let [tprops (ui-style/getTopProps rprops)]
     (return
      [:box #{(:.. tprops)}
       [:button {:shrink true
                 :hidden (or hidden disabled busy)
                 :mouse true
                 :style (ui-style/styleMinimal color)
                 :content content
                 :onClick onClick}]
       [:box    {:shrink true
                 :hidden (or hidden (not (or disabled busy)))
                 :style  {:fg "blue" :bg "black"}
                 :content content}]]))))

(defn.js SmallButton
  "Constructs a Small Button"
  {:added "4.0"}
  ([props]
   (var tprops (ui-style/getTopProps props))
   (var #{color busy disabled content onClick} props)
   (return
    [:box #{(:.. tprops)}
     [:button {:shrink true
               :bold true
               :height 1
               :hidden (or disabled busy)
               :mouse true
               :style (ui-style/styleSmall color)
               :content (+ " " content " ")
               :onClick onClick}]
     [:box   {:shrink true
              :height 1
              :hidden (not (or disabled busy))
              :style  {:bg "black" :fg "blue"}
              :content (+ " " content " ")}]])))

(defn.js ToggleButton
  "Constructs a toggle button"
  {:added "4.0"}
  ([props]
   (var tprops (ui-style/getTopProps props))
   (var #{color busy selected setSelected disabled content} props)
   (return
    [:box #{(:.. tprops)}
     [:button {:shrink true
               :bold true
               :height 1
               :hidden (or disabled busy)
               :mouse true
               :style (:? selected
                          (ui-style/styleSmall color)
                          {:bg "black" :fg "white"})
               :content (+ " " content " ")
               :onClick (fn:> (setSelected (not selected)))}]
     [:box   {:shrink true
              :height 1
              :hidden (not (or disabled busy))
              :style  {:bg "black" :fg "blue"}
              :content (+ " " content " ")}]])))


(defn.js BigButton
  "Constructs a Big Button"
  {:added "4.0"}
  ([props]
   (let [tprops (ui-style/getTopProps props)
         #{color disabled busy content onClick} props]
     (return
      [:box #{(:.. tprops)}
       [:button {:border {}
                 :shrink true
                 :bold true
                 :height 3
                 :hidden (or disabled busy)
                 :mouse true
                 :style (ui-style/styleInvert color)
                 :content (+ " " content " ")
                 :onClick onClick}]
       [:button {:shrink  true
                 :height  3
                 :border  {}
                 :hidden  (and (not disabled)
                               (not busy))
                 :style   (:? disabled ui-style/styleInvertDisabled ui-style/styleInvertBusy) 
                 :content (+ " " content " ")}]]))))

(defn.js BigCheckBox
  "Constructs a Big Checkbox"
  {:added "4.0"}
  ([props]
   (let [tprops (ui-style/getTopProps props)
         #{color
           borderColor
           selected setSelected onChange} props
         [internal setInternal] (r/local (or selected false))
         toggleFn (fn []
                    (let [val (not internal)]
                      (setInternal val)
                      (if setSelected  (setSelected val))
                      (if onChange (onChange val))))]
     (r/run []
       (if (and (k/is-boolean? selected)
                (not= internal selected))
         (setInternal selected)))
     (return [:button {:shrink true
                       :left   props.left
                       :bottom props.bottom
                       :right  props.right
                       :top    props.top
                       :mouse true
                       :inputOnFocus true
                       :style {:fg color
                               :bg "black"
                               :border {:fg (or borderColor color)
                                        :bg "black"}}
                       :border {:type "line"}
                       :onClick toggleFn
                       :content (:? internal " ■ " "   ")}]))))

;;
;; Single State
;;

(defn.js ToggleSwitch
  "Constructs a Toggle"
  {:added "4.0"}
  ([props]
   (let [#{color disabled
           reverse
           selected
           setSelected
           onChange
           textOn
           textOff
           initial} props
         _ (:= textOn  (or textOn "on"))
         _ (:= textOff (or textOff "off"))
         tprops (ui-style/getTopProps props)
         [internal setInternal] (r/local (or selected false))
         display nil
         _  (if reverse
              (:= display (not internal))
              (:= display internal))
         
         toggleFn (fn []
                    (let [val (not internal)]
                      (setInternal val)
                      (if setSelected  (setSelected val))
                      (if onChange (onChange val))))]
     (r/run []
       (if (and (k/is-boolean? selected)
                (not= internal selected))
         (setInternal selected)))
     (return [:box #{(:.. tprops)}
              [:button {:shrink true
                        :mouse true
                        :style (:? display ui-style/styleInvertDisabled (ui-style/styleSmall "white"))
                        :content (+ " " textOff " ")
                        :onClick toggleFn}]
              [:button {:left (+ 2 (j/length textOff))
                        :shrink true
                        :mouse true
                        :style (:? display (ui-style/styleSmall color) ui-style/styleInvertDisabled)
                        :content (+ " " textOn " ")
                        :onClick toggleFn}]]))))

(defn.js Spinner
  "Constructs a Spinner"
  {:added "4.0"}
  ([props]
   (let [#{max min step pad decimal width
           textColor
           bgColor
           color
           borderColor
           onChange initial value setValue
           vertical} props
         [internal setInternal] (r/local (or initial min 0))
         incIdx (fn []
                  (let [ninternal (+ internal (or step 1))]
                    (if (and (k/is-number? max) (> ninternal max))
                      nil
                      (do (setInternal ninternal)
                          (if setValue   (setValue ninternal))
                          (if onChange (onChange ninternal))))))
         decIdx (fn []
                  (let [ninternal (- internal (or step 1))]
                    (if (and (k/is-number? min) (< ninternal min))
                      nil
                      (do (setInternal ninternal)
                          (if setValue   (setValue ninternal))
                          (if onChange (onChange ninternal))))))
         _ (r/run []
             (if (and (k/is-number? value)
                      (not= internal value))
               (setInternal value)))
         bprops (k/obj-assign-nested
                 {:mouse true
                  #_#_:border {:type "line"}
                  :shrink true
                  :height (:? vertical 3 1)
                  :style {:fg (or textColor "white")
                          :bg (or bgColor "black")
                          :bold true}
                  :content (j/padStart (:? (k/not-nil? decimal) (j/toFixed internal decimal) (+ "" internal))
                                       (or pad 0))}
                 (ui-style/getLayout props))
         decProps (j/assign {:shrink true
                             :mouse true
                             :style (ui-style/styleSmall color)
                             :onClick decIdx
                             :content " - "}
                            (:? vertical {:right 1 :top 1} {:right 0 :top 0}))
         incProps (j/assign {:shrink true
                             :mouse true
                             :onClick incIdx
                             :style (ui-style/styleSmall color)
                             :content " + "}
                            (:? vertical {:right 1 :top -1} {:right 4 :top 0}))]
     (return [:box #{(:.. bprops)}
              [:button #{(:.. incProps)}]
              [:button #{(:.. decProps)}]]))))

(defn.js EnumBoxIndexed
  "Constructs a EnumBoxIndexed"
  {:added "4.0"}
  ([props]
   (let [#{items
           textColor
           color
           pad 
           borderColor
           onChange
           index setIndex} props
         [internal setInternal] (r/local (or index 0))
         
         incIdx (fn []
                  (let [ni (mod (+ (k/len items) (+ internal 1))
                                (k/len items))]
                    (setInternal ni)
                    (if onChange (onChange ni))
                    (if setIndex (setIndex ni))))
         decIdx (fn []
                  (let [ni (mod (+ (k/len items) (- internal 1))
                                (k/len items))]
                    (setInternal ni)
                    (if onChange (onChange ni))
                    (if setIndex (setIndex ni))))
         _ (r/run []
            (if (and (k/is-number? index)
                     (not= internal index))
              (setInternal index)))
         bprops (k/obj-assign-nested
                {:mouse true
                 :shrink true
                 :style {:fg (or textColor "white")
                         :bg "black"
                         :bold true}
                 :onClick incIdx
                 :content (+ " " (j/padStart (k/get-key items internal)
                                             (or pad 0)))}
                (ui-style/getLayout props))]
     (return [:button #{(:.. bprops)}
              [:button {:shrink true
                        :top 0
                        :right 4
                        :mouse true
                        :style (ui-style/styleSmall color)
                        :onClick decIdx
                        :content " < "}]
              [:button {:top 0
                        :right 0
                        :shrink true
                        :mouse true
                        :onClick incIdx
                        :style (ui-style/styleSmall color)
                        :content " > "}]]))))

(defn.js EnumBox
  "Constructs a EnumBox"
  {:added "4.0"}
  ([#{[data
       valueFn
       value
       setValue
       (:.. rprops)]}]
   (let [#{setIndex
           items
           index} (r/convertIndex #{data
                                    valueFn
                                    value
                                    setValue})]
     (return [:% -/EnumBoxIndexed
              #{[setIndex
                 items
                 index
                 (:.. rprops)]}]))))

;;
;;
;;

(defn.js displayDropdown
  "helper function for dropdown"
  {:added "4.0"}
  [ref modal items opts]
  (let [#{screen lpos} (r/curr ref)
        closeFn (fn []
                  (do (modal.current.destroy)
                      (r/curr:set modal nil)))
        _ (do (if (r/curr modal)
                (return)))
        prompt (b/list {:parent screen
                        :top lpos.yl
                        :height (k/len items)
                        :mouse true
                        :keys true
                        :inputOnFocus true
                        :padding {:left 1 :right 1}
                        :left (+ lpos.xi)
                        :width (- lpos.xl lpos.xi)
                        :style {:bg "gray"
                                :selected {:bg (or (k/get-key opts "color")
                                                   "yellow")
                                           :fg "black"}}
                        :items items})]
    (do (. prompt (on "select" (fn [e i]
                                 (opts.select e i)
                                 (closeFn))))
        (. prompt (focus))
        (r/curr:set modal prompt))))

(defn.js DropdownIndexed
  "Constructs a Dropdown"
  {:added "4.0"}
  ([props]
   (let [#{items
           textColor
           color
           pad
           format
           borderColor
           onChange
           index setIndex} props
         [internal setInternal] (r/local (or index 0))

         formatFn (fn [val]
                    (return
                     (:? format (format val) (j/padStart val (or pad 0)))))
         
         modal (r/ref)
         ref   (r/ref)
         displayFn (fn []
                     (-/displayDropdown ref modal
                                        (k/arr-map items formatFn)
                                        {:color color
                                         :select (fn [e i]
                                                   (setInternal i)
                                                   (if setIndex (setIndex i))
                                                 (if onChange (onChange i)))}))
         tprops   (ui-style/getTopProps props)
         content  (formatFn (k/get-key items internal)) 
         
         _ (r/run []
            (if (and (k/is-number? index)
                     (not= internal index))
              (setInternal index)))]
     (return [:box #{(:.. tprops)}
              [:button {:ref ref
                        :height 1
                        :mouse true
                        :keys true
                        :inputOnFocus true
                        :style {:fg (or textColor "white")
                                :bg "black"
                                :bold true}
                        :onClick displayFn
                        :content (+ " " content)}
               [:button {:top 0
                         :right 0
                         :height 1
                         :shrink true
                         :mouse true
                         :onClick displayFn
                         :style (ui-style/styleSmall color)
                         :content " ▼ "}]]]))))

(defn.js Dropdown
  "Constructs a Dropdown"
  {:added "4.0"}
  ([#{[data
       valueFn
       value
       setValue
       (:.. rprops)]}]
   (let [#{setIndex
           items
           index} (r/convertIndex #{data
                                    valueFn
                                    value
                                    setValue})]
     (return [:% -/DropdownIndexed
              #{[setIndex
                 items
                 index
                 (:.. rprops)]}]))))

;;
;; Text Box
;;

(defn.js TextBox
  "Constructs a Dropdown"
  {:added "4.0"}
  ([props]
   (let [#{color disabled
           textColor
           error
           bold
           censor
           width
           initial
           onChange
           content
           setContent
           proxy} props
         [internal setInternal] (r/local (or initial ""))
         tprops (ui-style/getTopProps props) 
         style {:fg (or textColor "white")
                :bg "black"
                :bold bold}
         ref (or proxy (r/ref))]
     (r/init []
       (if initial
         (. (r/curr ref) (setValue initial))))

     (r/watch [content]
       (when (and (r/curr ref)
                  (not= internal content))

         (. (r/curr ref) (setValue content))
         (setInternal content)))

     (return [:box #{(:.. tprops)}
              [:box {:shrink true}
               [:textbox {:ref ref
                          :mouse true
                          :keys true
                          :hidden disabled
                          :inputOnFocus true
                          :shrink true
                          :width width
                          :censor censor
                          :style style
                          :on-keypress (fn [e]
                                         (let [curr (r/curr ref)]
                                           (j/delayed [5]
                                             (when curr
                                               (let [val (. curr ["value"])]
                                                 (if setContent (setContent val))
                                                 (if onChange (onChange val))
                                                 (setInternal val))))))}]
               [:box {:hidden (not disabled)
                      :shrink true
                      :style {:bg "black"
                              :fg "gray"
                              :bold bold}
                      :width width
                      :content (:? censor "" content)}]
               (:? error [:box {:align "right" :content (+ " " (k/get-key error "message") " ") :right 1 :shrink true :top 0 :width (+ 2 ((k/get-key error "message")))}])]]))))

(defn.js TextDisplay
  "Displays text as content"
  {:added "4.0"}
  ([props]
   (let [bprops (k/obj-assign-nested
                 {:bg "black"
                  :mouse true
                  :scrollable true
                  :scrollbar ui-style/styleScrollBar}
                 props)]
     (return [:box #{(:.. bprops)}]))))

;;
;;
;;

(defn.js displayNumberGrid
  "helper function for NumberGridBox"
  {:added "4.0"}
  [ref modal opts]
  (let [#{screen lpos} (r/curr ref)
        #{format start step end colCount color colWidth} opts
        _ (if (r/curr modal) (return))
        numbers (k/arr-range [start (+ end step) step])
        _ (:= color (or color "yellow"))
        grid (b/box {:parent screen
                     :top lpos.yl
                     :height (j/ceil (/ (k/len numbers) colCount))
                     :mouse true
                     :keys true
                     :inputOnFocus true
                     :left  (+ lpos.xi)
                     :width (+ 1 (* colCount (+ 1 colWidth)))
                     :style {:bg "gray"
                             :selected {:bg color
                                        :fg "black"}}})
        gridItems (-> numbers
                      (j/map (fn [n i]
                               (let [button (b/box {:parent grid
                                                    :top    (Math.floor (/ i colCount))
                                                    :height 1
                                                    :left  (+ 1 (* (mod i colCount)
                                                                   (+ 1 colWidth)))
                                                    :width colWidth
                                                    :mouse true
                                                    :keys true
                                                    :content (format n)
                                                    :inputOnFocus true
                                                    :style {:fg color
                                                            :bg "gray"
                                                            :hover {:fg color
                                                                    :bg  "black"}
                                                            :selected {:bg color
                                                                       :fg "black"}}})]
                                 (. button  (on "click" (fn []
                                                          (opts.select i)
                                                          (closeFn))))))))
        closeFn (fn []
                  (do (modal.current.destroy)
                      (r/curr:set modal nil)))]
    (do (r/curr:set modal grid))))

(defn.js NumberGridBox
  "Constructs a NumberGridBox"
  {:added "4.0"}
  ([props]
   (let [#{format width start
           step
           textFn
           textColor
           color
           borderColor
           end
           colCount colWidth onChange value setValue} props
         _ (:= start (or start 0))
         _ (:= step  (or step 1))
         forwardFn (fn:> [internal] (+ (* internal step) (or start 0)))
         reverseFn (fn:> [value] (Math.round (/ (- value (or start 0))
                                                step)))
         [internal setInternal] (r/local 0)
         formatFn (or format (fn [x] (return (+ "" x))))
         modal (r/ref)
         ref   (r/ref)
         displayFn (fn []
                     (-/displayNumberGrid ref modal
                                          {:start start
                                           :step step
                                           :end end
                                           :color color
                                           :format formatFn
                                           :colCount colCount
                                           :colWidth colWidth
                                           :select (fn [i]
                                                     (setInternal i)
                                                     (if setValue (setValue (forwardFn i)))
                                                     (if onChange (onChange (forwardFn i))))}))
         _ (r/run []
             (if (and (k/is-number? value)
                      (not= internal (reverseFn value)))
               (setInternal (reverseFn value))))
         tprops (ui-style/getTopProps props)]
     (return [:box #{(:.. tprops)}
              [:button {:ref ref
                        :width width
                        :mouse true
                        :keys true
                        :inputOnFocus true
                        :shrink true
                        :style {:fg (or textColor "white")
                                :bg "black"
                                :bold true}
                        :onClick displayFn
                        :content (+ " " (formatFn (+ start (* step internal))))}
               [:button {:top 0
                         :right 0
                         :height 1
                         :shrink true
                         :mouse true
                         :onClick displayFn
                         :style (ui-style/styleSmall color)
                         :content " ▼ "}]]]))))
