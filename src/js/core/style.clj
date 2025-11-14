(ns js.core.style
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :js)

(def +flex-container+
  {:writing-mode #{"horizontal-tb"
                   "vertical-rl"
                   "vertical-lr"
                   "sideways-rl"
                   "sideways-lr"}
   ;; enabled `flex`
   :display #{"flex" "inline-flex"}

   :flexDirection #{;; (default) items will be left to right in `ltr`; right to left in `rtl`
                    "row"
                    ;; reverse of `row`
                    "row-reverse"
                    ;; items will be top to bottom
                    "column"
                    ;; items will be bottom to top
                    "column-reverse"}
   :flexWrap      #{;; (default) items will be on one line
                    "nowrap"       
                    ;; items will wrap onto multiple lines, top to bottom
                    "wrap"         
                    ;; items will wrap onto multiple lines, bottom to top
                    "wrap-reverse"}
   :flexFlow       [:flexDirection :flexWrap]

   ;; aligns along the main axis
   :justifyContent #{"flex-start"
                     "flex-end"
                     "center"
                     "space-between"
                     "space-around"
                     "space-evenly"}
   ;; aligns along the cross axis
   :alignItems     #{"flex-start"
                     "flex-end"
                     "center"
                     "stretch"
                     "baseline"}
   ;; spacing along the cross axis
   :alignContent   #{"flex-start"
                     "flex-end"
                     "center"
                     "stretch"
                     "space-between"
                     "space-around"
                     "space-evenly"}})

(def +flex-item+
  {;; controls the order in which children are arranged
   :order      number?
   :flexGrow    pos?
   :flexShrink  pos?
   :flexBasis  #{"auto" "content"}
   :flex  [:flexGrow :flexShrink :flexBasis]
   ;; overrides alignItems of parent
   :alignSelf #{"auto"
                "flex-start"
                "flex-end"
                "center"
                "stretch"
                "baseline"}})

;;
;;
;;

(defmacro.js rect
  "creates a rect"
  {:added "4.0"}
  [width & [height]]
  {:width width
   :height (or height width)})

(defmacro.js padding
  "creates a padding"
  {:added "4.0"}
  ([top & [right bottom left]]
   {:paddingTop top
    :paddingRight  (or right top)
    :paddingBottom (or bottom top)
    :paddingLeft   (or left right top)}))

(defmacro.js margin
  "creates a margin"
  {:added "4.0"}
  ([top & [right bottom left]]
   {:marginTop top
    :marginRight  (or right top)
    :marginBottom (or bottom top)
    :marginLeft   (or left right top)}))

(defmacro.js align
  "creates an alignment"
  {:added "4.0"}
  ([major & [minor content]]
   (cond-> {:flex 1
            :justifyContent major
            :alignItems "center"}
     minor (assoc :alignItems minor)
     content (assoc :alignContent content))))

(defmacro.js pos
  "creates a position"
  {:added "4.0"}
  ([position & [m]]
   (cond-> {:position position}
     m (merge m))))

(defmacro.js fillAbsolute
  "creates a fill absolute style"
  {:added "4.0"}
  ([]
   {:position "absolute"
    :top 0
    :bottom 0
    :left 0
    :right 0}))

(defmacro.js centered
  "creates a centered object"
  {:added "4.0"}
  []
  {:justifyContent "center"
   :alignContent "center"
   :alignItems "center"})


;;
;; Color
;;

(defn hue->rgb
  "helper function for hsl->rbg"
  {:added "4.0"}
  [v1 v2 vH]
  (let [vH (cond
             (< vH 0) (inc vH)
             (> vH 1) (dec vH)
             :else vH)
        r (cond
            (< (* 6 vH) 1) (+ v1 (* (- v2 v1) 6 vH))
            (< (* 2 vH) 1) v2
            (< (* 3 vH) 2) (+ v1 (* (- v2 v1) 6 (- (/ 2 3) vH)))
            :else v1)]
    r))

(defn- round
  "Rounds to the nearest int."
  [^Double doub]
  (int (Math/round doub)))

(defn hsl->rgb-fn
  "hsl to rbg array"
  {:added "4.0"}
  [{:keys [h s l]}]
  (let [h (/ h 360)
        s (/ s 100)
        l (/ l 100)]
    (if (= s 0)
      [(* 255 l) (* 255 l) (* 255 l)]
      (let [var-2 (if (< l 0.5) (* l (inc s)) (- (+ l s) (* s l)))
            var-1 (- (* 2 l) var-2)]
        [(round (* 255 (hue->rgb var-1 var-2 (+ h (/ 1 3)))))
         (round (* 255 (hue->rgb var-1 var-2 h)))
         (round (* 255 (hue->rgb var-1 var-2 (- h (/ 1 3)))))]))))

(defmacro.js hsl->rgb
  "converts hsl to rbg"
  {:added "4.0"}
  [m]
  (let [s (->> (hsl->rgb-fn m)
               (map #(Integer/toString % 16))
               (apply str "#"))]
    s))
