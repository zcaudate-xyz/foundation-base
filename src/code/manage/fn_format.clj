(ns code.manage.fn-format
  (:require [code.framework :as base]
            [std.string :as str]
            [code.query :as query]
            [code.edit :as nav]
            [std.lib.zip :as zip]
            [std.block.construct :as construct]
            [std.block.base :as block]
            [code.project :as project]))

(defn manual-step-right
  "helper to bypass zip/step-right crash"
  {:added "3.0"}
  [{:keys [left right] :as zip}]
  (let [elem (first right)]
    (-> zip
        (assoc :left  (cons elem left))
        (assoc :right (rest right))
        (nav/update-step-right elem))))

(defn list-transform
  "transforms `(.. [] & body)` to `(.. ([] & body))`"
  {:added "3.0"}
  ([nav]
   (println "DEBUG: nav node:" (block/block-string (zip/node nav)))
   (let [nav (-> nav nav/left)
         [nav right-blocks] (if (#{:space :newline} (block/block-tag (zip/right-element nav)))
                              [(nav/left nav) (rest (zip/right-elements (nav/left nav)))]
                              [nav (zip/right-elements nav)])
         exprs (filter block/expression? right-blocks)
         head (take 2 exprs)
         tail (vec (drop 2 exprs))
         new-list (construct/block (apply list head))
         nav (assoc nav :right '())]
     (let [nav (-> nav
                   (zip/insert-right new-list)
                   (as-> n (reduce zip/insert-right n (reverse (construct/spaces 2))))
                   (zip/insert-right (construct/newline))
                   (manual-step-right) ;; newline
                   (manual-step-right) ;; space 1
                   (manual-step-right) ;; space 2
                   (manual-step-right))] ;; new-list
       (if (seq tail)
         (-> nav
             (nav/down)
             (nav/right-most)
             (nav/insert-newline)
             (nav/insert-space 3)
             (nav/insert-all tail)
             (nav/up))
         (do (println "DEBUG: left:" (map block/block-string (:left nav)))
             nav))))))

(defn fn:list-forms
  "query to find `defn` and `defmacro` forms with a vector"
  {:added "3.0"}
  ([nav]
   (query/modify nav
                 [(list '#{defn defmacro} '_ '^:%? string? '^:%? map? '| vector? '& '_)]
                 list-transform)))

(defn fn:defmethod-forms
  "query to find `defmethod` forms with a vector"
  {:added "3.0"}
  ([nav]
   (query/modify nav
                 [(list 'defmethod '_ '_ '| vector? '& '_)]
                 list-transform)))

(defn fn-format
  "function to refactor the arglist and body"
  {:added "3.0"}
  ([ns params lookup project]
   (let [edits [fn:list-forms]]
     ;;(prn edits)
     (base/refactor-code ns (assoc params :edits edits) lookup project))))

(comment
  (project/in-context)
  (fn-format 'code.manage.fn-format
             {:print {:function true}
              :write true}
             (project/file-lookup)
             (project/project)))
