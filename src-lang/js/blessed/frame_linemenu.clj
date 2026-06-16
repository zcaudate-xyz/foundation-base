(ns js.blessed.frame-linemenu
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt] [js.react :as r] [js.lib.chalk :as chalk] [xt.lang.common-data :as xtd] [xt.lang.spec-base :as xt]]})

(def.js lineNormal
  {:hover {:fg "black"
           :bg "white"
           :bold false}
   :bold false
   :fg "white"
   :bg "black"})
   
(def.js lineSelected
  {:hover {:bg "gray"
           :fg "yellow"
           :bold true}
   :bold true
   :bg "black"
   :fg "yellow"})

(defn.js LineButton
  "creates a line frame-linemenu button"
  {:added "4.0"}
  ([#{[label
       index
       selected
       route
       setRoute
       refLink
       (:.. rprops)]}]
   (let [content (+ (chalk/inverse (+ " " index " ")) "  " label  "  ")]
     (return
      [:button #{[:ref refLink
                  :shrink true
                  :mouse true
                  :keys true
                  :content content
                  :style   (:? selected -/lineSelected -/lineNormal)
                  :onClick (fn []
                             (setRoute route))
                  (:.. rprops)]}]))))

(defn.js layoutMenu
  "helper function for LineMenu"
  {:added "4.0"}
  ([items]
   (let [entries (xt/x:arr-filter items (fn:> [e] (:? (k/is-array? e.hidden) (not (e.hidden)) (not e.hidden))))
         lens     (xt/x:arr-map entries (fn:> [e] (xt/x:len e.label)))
         lefts    (. lens
                     (reduce (fn [acc l]
                               (. acc (push (+ (xtd/last acc) l 8)))
                               (return acc))
                             [0]))]
      (return (xt/x:arr-map entries (fn [e i]
                                (let [name  (or e.route (. e.label (toLowerCase)))
                                      left  (. lefts [i])
                                      width (- (. lefts [(+ i 1)])
                                               left)]
                                  (return #{...e name left width}))))))))

(defn.js LineMenu
  "creates a line menu"
  {:added "4.0"}
  ([#{[entries
       route
       setRoute
       (:.. rprops)]}]
   (var box (r/ref nil))
   (r/init []
     (. (r/curr box)
        (onScreenEvent "keypress"
                       (fn [_ key]
                         (let [e (-> entries
                                     (xt/x:arr-filter 
                                      (fn [e]
                                        (return (== e.index key.name))))
                                     (xtd/first))]
                           (when e
                             (setRoute e.route))))))
     (return (fn []
               (. (r/curr box) (free)))))
   (return
    [:box #{[:ref box
             :shrink true
             :style {:bg "black"}
             (:.. rprops)]}
     (xt/x:arr-map entries (fn [e]
                      (return
                       [:% -/LineButton #{[:key e.route
                                           :selected (== route e.route)
                                           setRoute
                                           (:.. e)]}])))])))
