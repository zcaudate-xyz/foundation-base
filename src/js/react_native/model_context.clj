(ns js.react-native.model-context
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-lib :as k]]})


;;
;; STATIC
;;

(defn.js innerCoordinate
  "gets the plane position"
  {:added "4.0"}
  [#{[position
      parent
      height
      width
      (:= margin 0)]}]
  (:= height (or height (- (. parent height) (* 2 margin))))
  (:= width  (or width  (- (. parent width)  (* 2 margin))))
  (var out #{width height})
  (cond (== position "centered")
        (j/assign out {:top  (/ (- (. parent height) height) 2)
                       :left (/ (- (. parent width) width) 2)})
        
        (== position "top")
        (j/assign out {:top  margin
                       :left (/ (- (. parent width) width) 2)})
        
        (== position "bottom")
        (j/assign out {:top  (- (. parent height) height margin)
                       :left (/ (- (. parent width) width) 2)})
        
        (== position "left")
        (j/assign out {:top   (/ (- (. parent height) height) 2)
                       :left  margin})

        (== position "right")
        (j/assign out {:top   (/ (- (. parent height) height) 2)
                       :left  (- (. parent width) width margin)})

        (== position "top_right")
        (j/assign out {:top   margin
                       :left  (- (. parent width) width margin)})

        (== position "bottom_right")
        (j/assign out {:top   (- (. parent height) height margin)
                       :left  (- (. parent width) width margin)})
        
        (== position "bottom_left")
        (j/assign out {:top   (- (. parent height) height margin)
                       :left  margin})
        
        (== position "top_left")
        (j/assign out {:top   margin
                       :left  margin}))
  (return out))

;;
;; CONTEXT (RELATIVE)
;;

(defn.js contextCoordinateMain
  "gets the context main coordinate"
  {:added "4.0"}
  [#{position
     margin
     host
     content}]
  (cond (== position "top")
        (return {:top  (- 0 margin (. content height))})

        (== position "bottom")
        (return {:top (+ margin (. host height))})

        (== position "left")
        (return {:left (- 0 margin (. content width))})

        (== position "left_edge")
        (return {:left margin})
        
        (== position "right")
        (return {:left (+ margin (. host width))})

        (== position "right_edge")
        (return {:left (- (. host width)
                          margin
                          (. content width))})
        
        (== position "centered")
        (return {:left (/ (- (. host width)
                             (. content width))
                          2)})))

(defn.js contextCoordinateCross
  "gets the context cross coordinate"
  {:added "4.0"}
  [#{position
     alignment
     marginCross
     host
     content}]
  (cond (or (== position "top")
            (== position "bottom"))
        (cond (== alignment "start")
              (return {:left marginCross})

              (== alignment "center")
              (return {:left (/ (- (. host width)
                                   (. content width))
                                2)})
              
              (== alignment "end")
              (return {:left (- (. host width)
                                (. content width)
                                marginCross)}))
        
        :else
        (cond (== alignment "start")
              (return {:top marginCross})

              (== alignment "center")
              (return {:top (/ (- (. host height)
                                  (. content height))
                               2)})
              
              (== alignment "end")
              (return {:top (- (. host height)
                               (. content height)
                               marginCross)}))))

(defn.js contextCoordinate
  "gets the context coordinate"
  {:added "4.0"}
  [#{[position
      alignment
      (:= margin 0)
      (:= marginCross 0)
      host
      content]}]
  (var e #{position
           alignment
           margin
           marginCross
           host
           content})
  (return (j/assign (-/contextCoordinateMain e)
                    (-/contextCoordinateCross e))))




;;
;; TRANSLATION
;;

(defn.js getTranslationOffset
  "gets the plane transition function"
  {:added "4.0"}
  [#{[transition
      position
      height
      width
      (:= margin 0)]}]
  (var [translateKey magnitude]
       (:? (== transition "from_bottom")
           ["translateY" (+ margin margin height)]
           
           (== transition "from_left")
           ["translateX" (- 0 margin margin width)]
           
           (== transition "from_right")
           ["translateX" (+ margin margin width)]
           
           (== transition "from_top")
           ["translateY" (- 0 margin margin height)]

           :else
           []))
  (return [translateKey magnitude]))

(defn.js getScalarFn
  "gets a scalar function from input"
  {:added "4.0"}
  [effect key]
  (var scalar (k/get-key (or effect {}) key))
  (if (k/is-number? scalar)
    (return (fn:> [visible] (k/mix scalar 1 visible)))))


;;
;; ANIMATE
;;

(def.js ANIMATE
  {:from-top    {:aspect "height"
                 :transform "translateY"
                 :type   "negative"
                 :counter "from_bottom"}
   :from-bottom {:aspect "height"
                 :transform "translateY"
                 :type   "positive"
                 :counter "from_top"}
   :from-left   {:aspect "width"
                 :transform "translateX"
                 :type   "negative"
                 :counter "from_right"}
   :from-right  {:aspect "width"
                 :transform "translateX"
                 :type   "positive"
                 :counter "from_left"}
   :flip-horizontal {:transform "rotateY"
                     :out (fn [progress]
                            (+ (k/mix 0 j/PI progress)
                               "rad"))
                     :in  (fn [progress]
                            (+ (k/mix (- j/PI) 0 progress)
                               "rad"))
                     :counter "flip_vertical"}
   :flip-vertical   {:transform "rotateX"
                     :out (fn [progress]
                            (+ (k/mix 0 j/PI progress)
                               "rad"))
                     :in  (fn [progress]
                            (+ (k/mix (- j/PI) 0 progress)
                               "rad"))
                     :counter "flip_horizontal"}})

(def.js EFFECTS
  {:zoom   {:default 0.3
            :transform "scale"}
   :fade   {:default 0.3
            :aspect "opacity"}})

(defn.js animateOffset
  "gets the animated offset"
  {:added "4.0"}
  [#{margin height width}
   entry]
  (var #{type aspect} entry)
  (var offset (+ margin margin (:? (== aspect "width")
                                   width
                                          height)))
  (when (== type "negative")
    (:= offset (- offset)))
  (return offset))

(defn.js animateIn
  "creates the animateIn function"
  {:added "4.0"}
  [#{[transition
      height
      width
      (:= margin 0)]}]
  (var entry (k/get-key -/ANIMATE transition))
  (when (not entry)
    (return (fn:>)))
  (var #{transform} entry)
  (var createInFn
       (fn [entry]
         (var offset (-/animateOffset #{margin height width}
                                      entry))
         (return (fn [progress]
                   (return
                    {transform (k/mix offset
                                      0
                                      progress)})))))
  (return
   (or (k/get-key entry "in")
       (createInFn entry))))

(defn.js animateOut
  "creates the animateOut function"
  {:added "4.0"}
  [#{[transition
      height
      width
      (:= margin 0)]}]
  (var entry (k/get-key -/ANIMATE transition))
  (when (not entry)
    (return (fn:>)))
  (var #{transform} entry)
  (var createInFn
       (fn [entry]
         (var offset (-/animateOffset #{margin height width}
                                      entry))
         (return (fn [progress]
                   (return
                    {transform (k/mix 0
                                      offset
                                      progress)})))))
  (return
   (or (k/get-key entry "out")
       (createInFn entry))))


(comment
  
  

  (defn.js innerTransition
    "gets the plane transition function"
    {:added "4.0"}
    [#{transition
       position
       margin
       height
       width}]
    (var [transform magnitude]
         (:? (== transition "from_bottom")
             ["translateY" (+ margin margin height)]
             
             (== transition "from_left")
             ["translateX" (- 0 margin margin width)]
             
             (== transition "from_right")
             ["translateX" (+ margin margin width)]
             
             (== transition "from_top")
             ["translateY" (- 0 margin margin height)]

             :else
             ))
    (return [transform magnitude])))


(comment

  (fn [progress chord]
    (var #{even
           changing} chord)
    (return (:? changing
                {:style
                 (:? even
                     (-/transitionOut
                      (j/assign #{progress}
                                chord))
                     (-/transitionIn
                      (j/assign #{progress}
                                chord))
                     )})))
  
  (defn.js 
    "creates a transition in effect"
    {:added "4.0"}
    [#{[transition
        height
        width
        progress
        (:= margin 0)]}]
    (var [transform magnitude]
         (:? (== transition "from_top")
             ["translateY" (k/mix (- height) 0 progress)]
             
             (== transition "from_bottom")
             ["translateY" (k/mix height 0 progress)]

             (== transition "from_right")
             ["translateX" (k/mix width 0 progress)]
             
             (== transition "from_left")
             ["translateX" (k/mix (- width) 0 progress)]
             
             (== transition "flip_horizontal")
             ["rotateY" (+ (k/mix (- j/PI) 0 progress)
                           "rad")]
             
             (== transition "flip_vertical")
             ["rotateX" (+ (k/mix (- j/PI) 0 progress)
                           "rad")]
             
             :else ["translateX" 0]))
    (return [transform magnitude]))

  (defn.js transitionOut
    "craetes a transition out effect"
    {:added "4.0"}
    [#{[transition
        (:= fade 0.3)
        height
        width
        progress]}]
    (var opacity 0)
    
    (:= opacity (:? (not= transition "fade")
                    (k/mix 1 fade progress)
                    
                    :else (k/mix 1 0 (j/min 1 (* 2 progress)))))
    (var [direction amount]
         (:? (== transition "from_top")
             ["translateY" (k/mix 0 height progress)]
             
             (== transition "from_bottom")
             ["translateY" (k/mix 0 (- height) progress)]

             (== transition "from_right")
             ["translateX" (k/mix 0 (- width) progress)]
             
             (== transition "from_left")
             ["translateX" (k/mix 0 width progress)]
             
             (== transition "flip_horizontal")
             ["rotateY" (+ (k/mix 0 j/PI progress)
                           "rad")]

             (== transition "flip_vertical")
             ["rotateX" (+ (k/mix 0 j/PI progress)
                           "rad")]

             :else
             ["translateX" 0]))
    (return
     {:opacity opacity
      :transform [{direction amount}]}))



  ;;
  ;;
  ;;

  
  )
