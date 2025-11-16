(ns std.lang.model.spec-js.meta
  (:require [std.string :as str]
            [std.lib :as h]
            [std.fs :as fs]
            [std.lang.base.book :as book]
            [std.lang.base.book-module :as module]
            [std.lang.base.util :as ut]))

;;
;; MODULE
;;

(defn js-module-import-async
  [name link]
  (let [{:keys [ns as refer]} link]
    (list 'const as
          (list 'new
                'Proxy
                (list 'import name)
                '{:get (fn [esm key]
                         (return (. esm
                                    (then (fn [m]
                                            (return {"__esMODULE" true
                                                     :default (. m default [key])}))))))}))))

(defn js-module-import
  "outputs the js module import from"
  {:added "4.0"}
  ([name link mopts]
   (let [{:keys [emit]} mopts
         async-ns (set (-> emit :static :import/async))
         {:keys [ns as refer]} link
         {:lang/keys [format]} emit]
     (case format
       :none nil
       (:global)   (let [sym (if (vector? as)
                               (last as)
                               as)]
                     (list 'Object.defineProperty '!:G (str sym) 
                           {:value (list 'require (str name))}))
       (:commonjs) (let [sym (if (vector? as)
                               (last as)
                               as)]
                     (list 'const sym := (list 'require (str name))))
       (let [#_#_as (if (= :link (:import emit))
                      ['* as]
                      as)
             imports (cond-> []
                       as    (conj (if (vector? as)
                                     (list :- (first as)
                                           :as (last as))
                                     as))
                       refer (conj (set refer)))]
         (cond (async-ns ns)
               (js-module-import-async name link)
               
               :else
               (if (empty? imports)
                 (list :- :import (str "'" name "'"))
                 (list :- :import
                       (list 'quote imports)
                       :from (str "'" name "'")))))))))

(defn js-module-export
  "outputs the js module export form"
  {:added "4.0"}
  ([module mopts]
   (let [{:keys [emit]} mopts
         {:lang/keys [format]} emit
         table  (->> (module/module-entries module
                                            std.lib/T #_#{:defn
                                              :def
                                              :defclass})
                     (cons 'tab))]
     (h/prn table)
     (case format
       (:none
        :global)  nil
       :commonjs  (list := 'module.exports table)
       (list :- :export :default table)))))

(defn js-module-link
  "gets the relative js based module"
  {:added "4.0"}
  ([ns graph]
   (let [parent-rel (fn [path]
                      (let [idx (.lastIndexOf (str path) ".")
                            idx (if (neg? idx)
                                  (count path)
                                  idx)]
                        (subs (str path) 0 idx)))
         {:keys [base root-ns]} graph
         root-rel     (parent-rel (str root-ns))
         is-ext       (not (str/starts-with? (name ns) root-rel))
         is-ext-base  (not (str/starts-with? (name base) root-rel))
         base-path    (-> (str/replace (name base) #"\." "/")
                          (fs/parent))
         interim (cond (or (not is-ext)
                           is-ext-base)
                       (let [ns-path   (str/replace (name ns) #"\." "/")]
                         (str (fs/relativize base-path ns-path)))
                       
                       :else
                       (let [ns-path   (str/replace (name root-ns) #"\." "/")
                             ns-path   (subs ns-path 0 (.lastIndexOf ns-path "/"))
                             interim   (str (fs/relativize base-path ns-path))]
                         (str interim "/" (str/replace (name ns) #"\." "/"))))]
     (cond (str/starts-with? interim ".")
           interim

           (str/starts-with? interim "/")
           (str "." interim)

           (empty? interim)
           (str "../" (fs/file-name base-path))
           
           :else
           (str "./" interim)))))

(def +meta+
  (book/book-meta
   {:module-current (fn [])
    :module-import  #'js-module-import
    :module-export  #'js-module-export
    :module-link    #'js-module-link}))
