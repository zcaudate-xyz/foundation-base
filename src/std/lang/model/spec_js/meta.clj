(ns std.lang.model.spec-js.meta
  (:require [std.string :as str]
            [std.lib :as h]
            [std.fs :as fs]
            [std.lang.base.book :as book]
            [std.lang.base.util :as ut]))

;;
;; MODULE
;;

(defn js-module-import
  "outputs the js module import from"
  {:added "4.0"}
  ([name link mopts]
   (let [{:keys [ns as refer]} link
         {:lang/keys [imports]} (:emit mopts)
         async-ns (set (-> mopts :emit :static :import/async))]
     (case imports
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
       (let [imports (cond-> []
                       as    (conj (if (vector? as)
                                     (list :- (first as) :as (last as))
                                     as))
                       refer (conj (set refer)))]
         (cond (async-ns ns)
               (list 'const as
                     (list 'new
                           'Proxy
                           (list 'import name)
                           '{:get (fn [esm key]
                                    (return (. esm
                                               (then (fn [m]
                                                       (return {"__esMODULE" true
                                                                :default (. m default [key])}))))))}))
               
               :else
               (list :- :import
                     (list 'quote imports)
                     :from (str "'" name "'"))))))))

(defn js-module-export
  "outputs the js module export form"
  {:added "4.0"}
  ([{:keys [as refer]} mopts]
   (let [{:lang/keys [exports]} (:emit mopts)]
     (case exports
       :none nil
       :commonjs (list := 'module.exports as)
       (list :- :export :default as)))))

(defn js-module-link
  "gets the relative js based module"
  {:added "4.0"}
  ([ns options]
   (let [parent-rel (fn [path]
                      (let [idx (.lastIndexOf (str path) ".")
                            idx (if (neg? idx)
                                  (count path)
                                  idx)]
                        (subs (str path) 0 idx)))
         {:keys [base root-ns]} options
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
