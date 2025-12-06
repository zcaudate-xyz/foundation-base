(ns code.manage.fn-format
  (:require [code.framework :as base]
            [std.string :as str]
            [code.query :as query]
            [std.block.navigate :as nav]
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
   (let [nav (loop [n nav]
               (let [node (zip/get n)]
                 (if (and (block/container? node)
                          (= :vector (block/block-tag node)))
                   n
                   (if (zip/at-right-most? n)
                     n
                     (recur (zip/step-right n))))))
         nav (loop [n nav]
               (if (zip/at-left-most? n)
                 n
                 (let [l (zip/step-left n)]
                   (if (#{:space :newline :linespace} (block/block-tag (zip/get l)))
                     (recur l)
                     n))))
         right-blocks (zip/right-elements nav)
         exprs (filter block/expression? right-blocks)
         head (take 2 exprs)
         tail (vec (drop 2 exprs))
         new-list (construct/block (apply list head))
         nav (assoc nav :right '())]
     (let [nav (-> nav
                   (zip/insert-right new-list)
                   (zip/insert-left (construct/newline))
                   (as-> n (reduce zip/insert-left n (construct/spaces 2))))]
       (if (seq tail)
         (-> nav
             (nav/down)
             (nav/right-most)
             (nav/insert-newline)
             (nav/insert-space 3)
             (nav/insert-all tail)
             (nav/up))
         nav)))))

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
