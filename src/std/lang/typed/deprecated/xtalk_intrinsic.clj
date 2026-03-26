;; Deprecated snapshot of the pre-rebuild xtalk typed engine.
;; Keep for reference while the new core is rebuilt around grammar-derived builtin typing.

(ns std.lang.typed.deprecated.xtalk-intrinsic
  (:require [std.lang.typed.xtalk-common :as types]))

(defn intrinsic-result
  [callbacks type errors]
  ((:result callbacks) type errors))

(defn unary-bool
  [[_ arg] ctx callbacks]
  (let [arg-out ((:infer-type callbacks) arg ctx)]
    (intrinsic-result callbacks
                      types/+bool-type+
                      (:errors arg-out))))

(defn unary-int
  [[_ arg] ctx callbacks]
  (let [arg-out ((:infer-type callbacks) arg ctx)]
    (intrinsic-result callbacks
                      types/+int-type+
                      (:errors arg-out))))

(defn str-returning
  [[_ & args] ctx callbacks]
  (let [arg-outs (mapv #((:infer-type callbacks) % ctx) args)]
    (intrinsic-result callbacks
                      types/+str-type+
                      (mapcat :errors arg-outs))))

(defn array-of-strings
  [[_ & args] ctx callbacks]
  (let [arg-outs (mapv #((:infer-type callbacks) % ctx) args)]
    (intrinsic-result callbacks
                      {:kind :array
                       :item types/+str-type+}
                      (mapcat :errors arg-outs))))

(defn obj-keys
  [[_ arg] ctx callbacks]
  (let [arg-out ((:infer-type callbacks) arg ctx)]
    (intrinsic-result callbacks
                      {:kind :array
                       :item types/+str-type+}
                      (:errors arg-out))))

(defn arrayify
  [[_ arg] ctx callbacks]
  (let [arg-out ((:infer-type callbacks) arg ctx)]
    (intrinsic-result callbacks
                      ((:arrayify-type callbacks) (:type arg-out) ctx)
                      (:errors arg-out))))

(defn nth-like
  [[_ arg] ctx callbacks index]
  (let [arg-out ((:infer-type callbacks) arg ctx)
        arg-type ((:resolve-type callbacks) (:type arg-out) ctx)
        out-type (cond
                   (= :array (:kind arg-type))
                   (types/maybe-type (:item arg-type))

                   (= :tuple (:kind arg-type))
                   (or (nth (:types arg-type) index nil)
                       types/+unknown-type+)

                   :else
                   types/+unknown-type+)]
    (intrinsic-result callbacks out-type (:errors arg-out))))

(defn first-item
  [form ctx callbacks]
  (nth-like form ctx callbacks 0))

(defn second-item
  [form ctx callbacks]
  (nth-like form ctx callbacks 1))

(defn const-fn
  [[_ value] ctx callbacks]
  (let [value-out ((:infer-type callbacks) value ctx)]
    (intrinsic-result callbacks
                      {:kind :fn
                       :inputs [types/+unknown-type+]
                       :output (:type value-out)}
                      (:errors value-out))))

(defn obj-assign
  [form ctx callbacks]
  ((:infer-obj-assign callbacks) form ctx))

(defn make-container
  [form ctx callbacks]
  ((:infer-make-container callbacks) form ctx))

(defn blank-container
  [form ctx callbacks]
  ((:infer-blank-container callbacks) form ctx))

(def +intrinsic-rules+
  {'std.lang.typed.xtalk-intrinsic/arrayify arrayify
   'std.lang.typed.xtalk-intrinsic/not-empty? unary-bool
   'std.lang.typed.xtalk-intrinsic/is-empty? unary-bool
   'std.lang.typed.xtalk-intrinsic/const-fn const-fn
   'std.lang.typed.xtalk-intrinsic/make-container make-container
   'std.lang.typed.xtalk-intrinsic/blank-container blank-container})

(defn infer-intrinsic
  [form ctx callbacks]
  (when-let [rule (get +intrinsic-rules+ (first form))]
    (rule form ctx callbacks)))
