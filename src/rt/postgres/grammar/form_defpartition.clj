(ns rt.postgres.grammar.form-defpartition
  (:require [rt.postgres.grammar.common :as common]
            [std.lang.base.grammar-spec :as grammar-spec]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.book :as book]
            [std.string :as str]
            [std.lib :as h]))

(defn pg-partition-name
  "constructs partition name"
  {:added "4.0"}
  [base val stack]
  (let [parts (concat [base] (reverse stack) [val])]
    (clojure.string/join "__" (map h/strn parts))))

(defn pg-partition-def
  "recursive definition for partition"
  {:added "4.0"}
  [parent-sym base-name current-spec remaining-specs stack]
  (let [use  (or (:use current-spec) (:on current-spec))
        in   (or (:in current-spec)  (:for current-spec))
        {:keys [default schema]} current-spec
        next-spec (first remaining-specs)
        next-col  (when-let [u (or (:use next-spec) (:on next-spec))]
                    (clojure.string/replace (name u) "-" "_"))
        
        ;; Parent info
        p-ns (namespace parent-sym)
        [p-sch p-tab] (if p-ns
                        [p-ns (name parent-sym)]
                        (let [parts (clojure.string/split (name parent-sym) #"\.")]
                          (if (> (count parts) 1)
                            [(first parts) (last parts)]
                            [nil (name parent-sym)])))
        
        full-parent (if p-sch
                      (list '. #{p-sch} #{p-tab})
                      #{p-tab})
        
        ;; Current level schema
        curr-sch (or schema p-sch)]
    
    (if default
      (let [new-name (pg-partition-name base-name "$DEFAULT" stack)
            new-sym  (if curr-sch (symbol curr-sch new-name) (symbol new-name))
            full-new (if curr-sch (list '. #{curr-sch} #{new-name}) #{new-name})]
        (list
         (vec (concat [:create-table :if-not-exists full-new
                       :partition-of full-parent :default]
                      (if next-col
                        [:partition-by :list (list 'quote (list (symbol next-col)))]
                        [])))))
      
      (mapcat (fn [val]
                (let [new-name (pg-partition-name base-name val stack)
                      new-sym  (if curr-sch (symbol curr-sch new-name) (symbol new-name))
                      full-new (if curr-sch (list '. #{curr-sch} #{new-name}) #{new-name})]
                  
                  (concat
                   [(vec (concat [:create-table :if-not-exists full-new
                                  :partition-of full-parent
                                  :for :values :in (list 'quote (list val))]
                                 (if next-col
                                   [:partition-by :list (list 'quote (list (symbol next-col)))]
                                   [])))]
                   (if (seq remaining-specs)
                     (pg-partition-def new-sym base-name next-spec (rest remaining-specs) (cons val stack))
                     nil))))
              in))))

(defn pg-defpartition
  "defpartition block"
  {:added "4.0"}
  ([form]
   (let [mopts (preprocess/macro-opts)
         [mdefn [_ sym parents specs]] (grammar-spec/format-defn form)
         
         parents (if (vector? parents) parents [parents])
         
         all-statements
         (mapcat (fn [parent-sym]
                   (let [parent-sym (if (vector? parent-sym) (first parent-sym) parent-sym)
                         
                         ;; Try to find schema from parent entry
                         p-en  (if (symbol? parent-sym)
                                 (let [snapshot (:snapshot mopts)
                                       lang     (:lang mopts)
                                       book     (if snapshot (snap/get-book snapshot lang))
                                       module-id (or (namespace parent-sym) (:module mopts))
                                       entry-id  (name parent-sym)]
                                   (if book
                                     (book/get-base-entry book (symbol module-id) (symbol entry-id) :code))))
                         
                         parent-schema (or (:static/schema p-en) (namespace parent-sym))
                         base-name     (name parent-sym)
                         
                         ;; Update parent with found schema for SQL generation
                         parent-sql-sym (if parent-schema
                                          (symbol parent-schema base-name)
                                          (symbol base-name))]
                     (pg-partition-def parent-sql-sym base-name (first specs) (rest specs) [])))
                 parents)]
     (apply list 'do all-statements))))
