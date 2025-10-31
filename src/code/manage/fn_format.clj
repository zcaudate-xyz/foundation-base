(ns code.manage.fn-format
  (:require [code.framework :as base]
            [std.string :as str]
            [code.query :as query]
            [code.edit :as nav]
            [code.project :as project]))

(defn list-transform
  "transforms `(.. [] & body)` to `(.. ([] & body))`"
  {:added "3.0"}
  ([nav]
   (let [nav (-> nav nav/left)
         exprs (nav/right-expressions nav)
         nav (loop [nav nav]
               (if-let [next (try (nav/delete nav)
                                  (catch Throwable t))]
                 (recur next)
                 nav))]
     (-> nav
         (nav/insert ())
         (nav/insert-newline)
         (nav/insert-space 2)
         (nav/down)
         (nav/insert-all (take 2 exprs))
         (nav/insert-newline)
         (nav/insert-space 3)
         (nav/insert-all (vec (drop 2 exprs)))
         (nav/up)
         (nav/tighten-right)))))

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
