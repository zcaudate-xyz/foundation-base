(ns js.react-native.model-roller
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.core :as j]
             [xt.lang.base-lib :as k]]})

;;
;; ROLLER
;;
;; The roller is based on a cylindrical component
;; rotating on a single axis split into `n` number
;; of equal divisions containing content.
;;
;; It is assumed that the roller is facing the user
;; and the backside of the roller is not visible.
;;
;; The model is a single function that takes an offset,
;; and will return the translation and scaling, and
;; visibility given a division at the offset position.
;;
;; ROLLER SHIFTING
;;
;; Shifting is needed in order to allocate more data to
;; the roller than it has divisions.
;;
;; For example, the roller may be split into 5 divisions
;; for effect on screen, but it needs to show 8 items
;;
;; A, B, C, D, E
;;
;; I0, I1, I2, I3, I4, I5, I6, I7
;;
;; Shifiting helps to allocate data to the roller in a
;; windowing fashion so that the shifted divisions gets
;; the necessary data it needs when displaying.
;;


(defn.js roller-model
  "constructs a roller model"
  {:added "4.0"}
  [divisions radius]
  (var UNIT     (* 2 (/ j/PI divisions)))
  (var HALF_PI  (/ j/PI 2))
  (return (fn [offset]
            (var raw     (k/mod-pos offset divisions))
            (var theta   (* raw UNIT))
            (var visible (or (< (j/abs theta) HALF_PI)
                             (> (j/abs theta) (* 3 HALF_PI))))
            (return {:raw   raw
                     :visible visible
                     :offset offset
                     :theta theta
                     :translate (* radius (j/sin theta))
                     :scale     (j/abs (j/cos theta))}))))


(defn.js roller-shifted-norm
  "finds the shifted-norm for an index at center"
  {:added "4.0"}
  [divisions
   roller-index
   center]
  (var shifted      (- roller-index center))
  (var shifted-mod  (k/mod-pos shifted divisions))
  (var shifted-norm (:? (< shifted-mod (/ divisions 2))
                        shifted-mod
                        (- shifted-mod divisions)))
  (return shifted-norm))

(defn.js roller-shifted-index
  "finds shifted index for roller divisions"
  {:added "4.0"}
  [divisions
   roller-index
   input-raw
   input-total]
  (var input-index    (k/mod-pos input-raw input-total))
  (var center         (k/mod-pos input-raw divisions))
  (var shifted-norm   (-/roller-shifted-norm divisions roller-index center))
  (var shifted-index  (k/mod-pos (+ input-index
                                    shifted-norm)
                                 input-total))
  #_(console.log #{input-raw
                 input-total
                 roller-index
                 shifted-index
                 center
                 input-index
                 shifted-norm})
  (return shifted-index))

(defn.js roller-set-values
  "sets roller values given array of animated values"
  {:added "4.0"}
  [ind-array
   divisions
   input-raw
   input-total]
  (k/for:array [[i ind] ind-array]
    (var shifted (-/roller-shifted-index
                  divisions
                  i
                  input-raw
                  input-total))
    (when (not= shifted ind._value)
      (. ind (setValue shifted))))
  (return true))

