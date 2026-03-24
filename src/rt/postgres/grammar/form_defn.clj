(ns rt.postgres.grammar.form-defn
  (:require [rt.postgres.grammar.common :as common]
            [rt.postgres.grammar.form-let :as form-let]
            [rt.postgres.grammar.meta :as meta]
            [std.lang.base.emit-common :as emit-common]
            [std.lang.base.emit-fn :as fn]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar-spec :as grammar-spec]
            [std.lang.base.util :as ut]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]))

(defn pg-defnformat
  "formats a defn form"
  {:added "4.0"}
  ([form]
   (let [[mdefn [op sym args & body]] (grammar-spec/format-defn form)
         targs (helper/emit-typed-args args {})
         msym  (assoc (common/pg-sym-meta sym)
                      :static/input targs)]
     [(merge mdefn msym)
      (apply list
             op
             (with-meta sym msym)
             args
             body)])))

(def +pg-defnlang-types+                   
  {:default  {:tag "plpgsql"  :lang :postgres}  
   :sql      {:tag "sql"      :lang :postgres}
   :js       {:tag "plv8"     :lang :js}  
   :python   {:tag "plpython3u"}                
   :lua      {:tag "pllua"}})

(defn pg-defn
  "creates the complete defn"
  {:added "4.0"}
  [[_ sym args & body :as form] grammar mopts]
  (let [block    (fn/emit-fn-block :defn grammar)
        typestr  (fn/emit-fn-type sym nil grammar mopts) 
        preamble (fn/emit-fn-preamble [:defn sym args]
                                      #_block
                                      grammar
                                      mopts)
        {:static/keys [schema props return language]
         :or {language :default}} (meta sym)
        {:keys [tag lang] :as m :or {lang language}} (get +pg-defnlang-types+ language)
        schstr (if (not-empty schema)
                 (str "\"" schema "\"" "."))
        fnstr  (str schstr preamble)

        ;; -- SQL formatting
        body   (if (= :sql language)
                 (if (= 1 (count body))
                   (if (vector? (last body))
                     body
                     [[:select (last body)]])
                   (f/error "Not an sql expression" {:value body}))
                 body)
        
        ;; -- PLPGSQL formatting
        bfull  (if (or (not (= language :default))
                       (and (= 1 (count body))
                            (collection/form? (first body))
                            ('#{let let:block} (ffirst body))))
                 [\\
                  \\ (list \| (list '!:lang {:lang lang}
                                    (apply list 'do body)))
                  \\]
                 (concat [\\ :begin]
                         [\\ (list \| (apply list 'do body))]
                         [\\ :end \;]))]
    (binding [form-let/*input-syms* (volatile! (set (filter symbol? args)))]
      (emit-common/*emit-fn*
       (vec (concat `[:create-or-replace :function (:- ~fnstr)
                      :returns (:- ~typestr) :as :$$]
                    bfull
                    [\\ :$$ :language (list '% tag)]
                    props
                    [\;]))
       grammar
       mopts))))
