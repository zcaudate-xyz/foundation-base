(ns std.lang.model.spec-c
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.emit-fn :as emit-fn]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.string :as str]
            [std.lib :as h]))

(defn tf-define
  "not sure if this is needed (due to defmacro) but may be good for source to source"
  {:added "4.0"}
  [[_ sym & more :as form]]
  (let [[args body] (if (= 1 (count more))
                      [nil (first more)]
                      more)]
    (if args
      (list :- "#define" (list :% sym (list 'quote
                                            (apply list args)))
            body)
      (list :- "#define" sym body))))

(defn tf-struct
  "transforms struct definition"
  {:added "4.0"}
  [[_ sym fields]]
  (let [fields (partition 2 fields)
        out (map (fn [[type name]]
                   (list :- type (str name ";")))
                 fields)]
    (list :- "struct" sym
          (list :- "{"
                (list \\
                      \\ (list \| (apply list 'do out)))
                (list :- "\n};")))))

(defn tf-enum
  "transforms enum definition"
  {:added "4.0"}
  [[_ sym fields]]
  (list :- "enum" sym
        (list :- "{")
        (list 'quote fields)
        (list :- "};")))

(defn tf-typedef
  "transforms typedef"
  {:added "4.0"}
  [[_ type alias]]
  (let [alias-str (ut/sym-default-str alias)]
    (list :- "typedef" type (str alias-str ";"))))

(defn to-c-type
  "converts to c type"
  {:added "4.0"}
  [t]
  (cond (keyword? t) (name t)
        (symbol? t) (name t)
        (vector? t) (str/join " " (map to-c-type t))
        :else (str t)))

(defn c-sanitize
  "sanitizes a symbol for c"
  {:added "4.0"}
  [s]
  (-> (str s)
      (str/replace "-" "_")
      (str/replace "." "_")
      (str/replace "/" "____")))

(defn c-fn-args
  "custom C function arguments emission"
  {:added "4.0"}
  [[_ args] grammar mopts]
  (let [args (if (and (list? args) (= 'quote (first args)))
               (second args)
               args)
        args (if (vector? args)
               (if (vector? (first args))
                 args
                 (partition 2 args))
               [args])]
    (str "("
         (str/join ", "
                   (map (fn [arg]
                          (if (sequential? arg)
                            (let [[type name] arg]
                              (str (c-sanitize (to-c-type type))
                                   " "
                                   (emit/emit-main name grammar mopts)))
                            (emit/emit-main arg grammar mopts)))
                        args))
         ")")))



(defn emit-defn
  "custom defn for C"
  {:added "4.0"}
  [[_ sym args & body] grammar mopts]
  (let [ret-type (or (-> sym meta :tag) (-> sym meta :-) "void")
        ret-type (to-c-type ret-type)
        
        module (:module mopts)
        sym-str (if (and module (symbol? sym) (not (namespace sym)))
                  (c-sanitize (symbol (name (:id module)) (name sym)))
                  (c-sanitize sym))
        
        args-str (c-fn-args [:c-args args] grammar mopts)
        
        body-str (emit/emit-main (list \\ \\ (list \| (apply list 'do body))) grammar mopts)]
    
    (str ret-type " " sym-str " " args-str " "
         "{\n"
         body-str
         "\n}")))

(defn tf-arrow
  "transforms arrow ->"
  {:added "4.0"}
  [[_ left right]]
  (list :% left (list :- "->" right)))

(defn tf-sizeof
  "transforms sizeof"
  {:added "4.0"}
  [[_ val]]
  (list :- "sizeof" val))

(def +features+
  (-> (grammar/build :exclude [:data-shortcuts
                               :control-try-catch
                               :class
                               :macro-arrow])
      (grammar/build:override
       {:defn    {:emit #'emit-defn}})
      (grammar/build:extend
       {:c-args  {:op :c-args :symbol #{:c-args} :emit #'c-fn-args}
        :define  {:op :define  :symbol '#{define}  :macro #'tf-define
                  :type :def :emit :macro
                  :section :code :priority 1}
        :arrow   {:op :arrow :symbol #{'-> 'arrow} :emit :infix :raw "->"}
        :sizeof  {:op :sizeof :symbol #{'sizeof} :emit :prefix :raw "sizeof" :type :free}
        :struct  {:op :struct :symbol #{'struct} :emit :macro :macro #'tf-struct :section :code :type :def}
        :enum    {:op :enum   :symbol #{'enum}   :emit :macro :macro #'tf-enum :section :code :type :def}
        :typedef {:op :typedef :symbol #{'typedef} :emit :macro :macro #'tf-typedef :section :code :type :def}})))

(def +template+
  (->> {:banned #{:set :map :regex}
        :highlight '#{return break}
        :default {:function  {:raw ""}}
        :data    {:vector    {:start "{" :end "}" :space ""}
                  :tuple     {:start "(" :end ")" :space ""}}
        :block  {:for       {:parameter {:sep ","}}
                 :script    {:start "" :end ""}}
        :define {:def       {:raw ""}}}
       (h/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :c
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta
   {:module-current   (fn [])
    :module-import    (fn [name _ opts]  
                        (h/$ (:- "#include" ~name)))
    :module-export    (fn [{:keys [as refer]} opts])}))

(def +book+
  (book/book {:lang :c
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
