(ns std.lang.base.preprocess-assign
  (:require [clojure.string]
            [std.lang.base.provenance :as provenance]
            [std.lang.base.util :as ut]
            [std.lib.collection :as collection]
            [std.lib.context.pointer :as ptr]
            [std.lib.foundation :as f]
            [std.lib.walk :as walk]))


(comment

  ;; -------
  ;; return case
  (return (x:type-native obj))

  ;;
  (do (when (== obj nil) (return nil))
      (var t := (typeof obj))
      (if (== t "object")
        (cond (Array.isArray obj)
              (return "array")

              :else
              (do
                (var tn := (. obj ["constructor"] ["name"]))
                (if (== tn "Object")
                  (return "object")
                  (return tn))))
        (return t)))
  
  ;; -------
  ;; assign case
  (var a (x:type-native obj))
  (:= a  (x:type-native obj))

  ;;
  (var a nil)
  (do (when (== obj nil) (return nil))
      (var t := (typeof obj))
      (if (== t "object")
        (cond (Array.isArray obj)
              (:= a "array")

              :else
              (do
                (var tn := (. obj ["constructor"] ["name"]))
                (if (== tn "Object")
                  (:= a "object")
                  (:= a tn))))
        (:= a t)))

  ;; -------
  ;; general usage case
  (f (g (x:type-native obj)))

  (var type-native-fn
       (fn type-native-lambda [obj]
         (when (== obj nil) (return nil))
         (var t := (typeof obj))
         (if (== t "object")
           (cond (Array.isArray obj)
                 (return "array")

                 :else
                 (do
                   (var tn := (. obj ["constructor"] ["name"]))
                   (if (== tn "Object")
                     (return "object")
                     (return tn))))
           (return t))))
  (f (g (type-native-fn obj)))

  ;; -------
  ;; standalone
  x:type-native

  ;;
  (fn type-native-lambda [obj]
    (when (== obj nil) (return nil))
    (var t := (typeof obj))
    (if (== t "object")
      (cond (Array.isArray obj)
            (return "array")

            :else
            (do
              (var tn := (. obj ["constructor"] ["name"]))
              (if (== tn "Object")
                (return "object")
                (return tn))))
      (return t)))
  )

