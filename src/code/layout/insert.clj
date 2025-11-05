(ns code.layout.insert
  (:require [std.lib.zip :as zip]
            [code.edit :as edit]
            [std.lib :as h]
            [code.layout.estimate :as est]
            [code.layout.primitives :as p]))

(declare insert-form)


(defn insert-
  [{:keys [stack code options]
    :as state}
   form])

(defn insert-form
  [{:keys [stack code options]
    :as state}
   form]
  (let [entry (last stack)]
    ))







(defn layout-form-insert
  "inserts a single element"
  {:added "4.0"}
  [{:keys [stack code options]
    :as state} loc]
  (let [{:keys [indent
                in-multiline]
         :as flags}  (last stack)
        elem         (zip/get loc)
        max-width    (get-max-width elem)
        multiline?   (estimate-multiline elem)
        code         (if in-multiline
                       (-> code
                           (edit/insert-newline)
                           (edit/insert-space (layout-form-get-indent flags)))
                       code)]
    (h/prn code elem)
    (cond (not multiline?)
          [(assoc state :code   (edit/insert-token  code elem))
           (zip/step-next loc)]

          :else
          (layout-form-insert-multiline state loc))))


(defn layout-form-initial
  "generates the initial state"
  {:added "4.0"}
  [opts]
  {:code    (edit/parse-string "")
   :stack   [{:type :root :indent (or (:indent opts) 0)
              :position 0}]
   :options opts})

(defn layout-form
  "layout a form"
  {:added "4.0"}
  [form & [{:keys [indent ruleset]
            :as opts}]]
  (loop [state    (layout-form-initial opts)
         loc      (zip/form-zip form)]
    (let [[new-state new-loc] (layout-form-insert state loc)]
      (if (nil? new-loc)
        (recur new-state new-loc)
        (:code new-state)))))
