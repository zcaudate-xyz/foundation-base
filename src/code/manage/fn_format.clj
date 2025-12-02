(ns code.manage.fn-format
  (:require [code.framework :as base]
            [std.string :as str]
            [code.query :as query]
            [code.edit :as nav]
            [std.lib.zip :as zip]
            [std.block.construct :as construct]
            [code.project :as project]))

(defn list-transform
  "transforms `(.. [] & body)` to `(.. ([] & body))`"
  {:added "3.0"}
  ([nav]
   (let [nav (-> nav nav/left)
         exprs (nav/right-expressions nav)
         nav (loop [nav nav]
               (if (nav/right-expression nav)
                 (recur (nav/delete nav))
                 nav))
         head (take 2 exprs)
         tail (vec (drop 2 exprs))
         new-list (construct/block (apply list head))]
     (let [nav (-> nav
                   (zip/insert-right new-list)
                   (nav/right) ;; Use nav/right instead of zip/step-right to avoid NPE
                   (nav/insert-newline)
                   (nav/insert-space 2))]
       (if (seq tail)
         (-> nav
             (nav/down)
             (nav/right-most)
             (nav/insert-newline)
             (nav/insert-space 3)
             (nav/insert-all tail)
             (nav/up)
             (nav/tighten-right))
         (nav/tighten-right nav))))))

(defn fn:list-forms
  "query to find `defn` and `defmacro` forms with a vector"
  {:added "3.0"}
  ([nav]
   (query/modify nav
                 [(list '#{defn defmacro} '_ '^:%? string? '^:%? map?  vector? '| '& '_)]
                 list-transform)))

(defn fn:defmethod-forms
  "query to find `defmethod` forms with a vector"
  {:added "3.0"}
  ([nav]
   (query/modify nav
                 [(list 'defmethod '_ '_  vector? '| '& '_)]
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
