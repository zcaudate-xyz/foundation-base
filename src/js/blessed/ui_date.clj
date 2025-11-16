(ns js.blessed.ui-date
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.react   :as r]
             [js.blessed :as b]
             [js.blessed.ui-style :as ui-style]
             [js.blessed.ui-core :as ui-core]]})

(defn.js TimePicker
  "Constructs a TimePicker"
  {:added "4.0"}
  ([props]
   (let [#{color
           minuteQuarters
           minuteHidden
           minuteLabel
           minute setMinute
           hourLabel
           hour  setHour} props
         tprops (ui-style/getTopProps props)
         formatFn (fn:> [n]
                    (+ " " (j/padEnd (j/padStart (+ "" n)
                                                 2
                                                 "0")
                                     4)))
         _ (:= hourLabel   (or hourLabel "h"))
         _ (:= minuteLabel (or minuteLabel "m"))
         minuteProps (j/assign {:key "0"
                                :left 10
                                :format formatFn
                                :value minute
                                :setValue setMinute
                                :colWidth 4
                                :colCount 6
                                :color color
                                :width 10}
                               (:? minuteQuarters {:colCount 4 :end 45 :start 0 :step 15} {:end 59 :start 0 :step 1}))]
     (return [:box #{(:.. tprops)}
              [:% ui-core/NumberGridBox
               {:left 0
                :start 0
                :end 23
                :format formatFn
                :value hour
                :setValue setHour
                :colWidth 4
                :colCount 6
                :color color
                :width 10}]
              [:box {:left 5
                     :top 0
                     :shrink true
                     :style {:bg "black"
                             :fg color}
                     :content hourLabel}]
              (:? (not minuteHidden) [[:% ui-core/NumberGridBox #{(:.. minuteProps)}] [:box {:content minuteLabel :key "1" :left (+ 14 (k/len hourLabel)) :shrink true :style {:bg "black" :fg color} :top 0}]])]))))

(defn.js DatePicker
  "Constructs a DatePicker"
  {:added "4.0"}
  ([props]
   (let [#{color

           yearLabel
           year setYear

           monthLabel
           month  setMonth
           
           dayLabel
           day   setDay} props
         tprops (j/assign
                 {:bg "black"}
                 (ui-style/getTopProps props))
         _ (:= dayLabel    (or dayLabel ""))
         _ (:= monthLabel  (or monthLabel "M"))
         _ (:= yearLabel   (or yearLabel "Y"))]
     (return 
      [:box #{(:.. tprops)}
       [:% ui-core/NumberGridBox
        {:left 0
         :start 1
         :end (:? (j/some [1 3 5 7 8 10 12] (fn:> [x] (== x month))) 31 (:? (j/some [4 6 9 11] (fn:> [x] (== x month))) 30 (:? (== 0 (mod year 4)) 29 28)))
         :format (fn:> [s] (+ " " (j/padStart (+ "" s) 3 " ")))
         :value day
         :setValue setDay
         :colWidth 4
         :colCount 5
         :color color
         :width 10}]
       [:box {:left 5
              :top 0
              :shrink true
              :style {:bg "black"
                      :fg color}
              :content dayLabel}]
       [:% ui-core/NumberGridBox
        {:start 1
         :end 12
         :format (fn [i]
                   (return (-> (k/get-key ["JAN"
                                           "FEB"
                                           "MAR"
                                           "APR"
                                           "MAY"
                                           "JUN"
                                           "JUL"
                                           "AUG"
                                           "SEP"
                                           "OCT"
                                           "NOV"
                                           "DEC"]
                                          (- i 1))
                               (j/padStart 4))))
         :left  10 #_(+ 2 (k/len dayLabel))
         :value month
         :setValue setMonth
         :colWidth 5
         :colCount 3
         :color color
         :width 10}]
       #_[:box {:left (+ 2 (k/len dayLabel) 20)
              :top 0
              :height 3
              :width 3
              :bg "black"}
        [:box {:top 0
               :left 1
               :shrink true
               :style {:bg "black"
                       :fg color}
               :content monthLabel}]]
       [:% ui-core/Spinner
        {:left 20
         :pad 6
         :value year
         :setValue setYear
         :color color
         :width 15}]]))))

(defn.js DurationPicker
  "Constructs a DurationPicker"
  {:added "4.0"}
  ([props]
   (let [#{color
           dayLabel
           hourLabel
           minuteLabel
           day   setDay
           width} props
         tprops (ui-style/getTopProps props)
         formatFn (fn:> [n]
                    (+ " " (j/padEnd (j/padStart (+ "" n)
                                                 2
                                                 " ")
                                     4)))
         _ (:= dayLabel    (or dayLabel "d"))
         _ (:= hourLabel   (or hourLabel "h"))
         _ (:= minuteLabel (or minuteLabel "m"))
         hprops (j/assign (ui-style/omitLayoutProps props)
                          {:width (:? width (- width 10))}
                          #{minuteLabel
                             hourLabel})]
     (return [:box #{(:.. tprops)}
              [:% ui-core/NumberGridBox
               {:left 0
                :start 0
                :end 31
                :format formatFn
                :value day
                :setValue setDay
                :colWidth 4
                :colCount 4
                :color color
                :width 10}]
              [:box {:left 5
                     :top 0
                     :shrink true
                     :style {:bg "black"
                             :fg color}
                     :content dayLabel}]
              [:box {:style {:bg "black"}
                     :left 10}
               [:% -/TimePicker #{(:.. hprops)}]]]))))
