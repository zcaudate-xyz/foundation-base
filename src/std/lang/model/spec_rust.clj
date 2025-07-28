(ns std.lang.model.spec-rust
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.emit-fn :as fn]
            [std.lang.base.emit-data :as data]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.string :as str]
            [std.lib :as h]))

(defn rst-typesystem
  "TODO"
  {:added "4.0"}
  [arr grammar mopts]
  (let [{:keys [sep] :as opts} (helper/get-options grammar [:data :vector])
        [sym & more] (rest arr)]
    (str sym "<" (str/join ","
                           (map (fn [input]
                                  (cond (string? input)
                                        input

                                        :else
                                        (emit/emit-main input grammar mopts)))
                                more))">")))

(defn rst-vector
  "TODO"
  {:added "4.0"}
  ([arr grammar mopts]
   (let [sym (first arr)]
     (cond (= :> sym)
           (rst-typesystem arr grammar mopts)
           
           :else
           (data/emit-coll :vector arr grammar mopts)))))

(defn rst-attributes
  "TODO"
  {:added "4.0"}
  [sym]
  (let [lines (:% (meta sym))]
    (str/join ""
              (map (fn [l] (str l "\n"))
                   lines))))

(defn rst-defenum
  "TODO"
  {:added "4.0"}
  [[_ sym values]]
  (list :%
        (list :- (rst-attributes sym))
        (list :- "enum" sym
              (list :- "{")
              (list 'quote (vec values))
              (list :- "}"))))

(defn rst-deftrait
  "TODO"
  {:added "4.0"}
  [[_ sym & body]]
  (let [body (h/postwalk (fn [x]
                           (if (and (list? x)
                                    (= 'fn (first x))
                                    (= 3 (count x)))
                             (apply list 'fn (with-meta (second x)
                                               (merge (meta (second x))
                                                      {:header true}))
                                    (drop 2 x))
                             x))
                         body)]
    (list :%
          (list :- (rst-attributes sym))
          (list :- "trait" sym
                (list :- "{"
                      (list \\
                            \\ (list \| (apply list 'do body))))
                (list :- "\n}")))))

(defn rst-defimpl
  "TODO"
  {:added "4.0"}
  ([[_ sym inherit & body]]
   (list :%
         (list :- (rst-attributes sym))
         (list :- "impl" (first inherit) "for" (last inherit)
               (list :- "{")
               (list \\
                     \\ (list \|
                              (apply list 'do body)))
               (list :- "\n}")))))

(defn rst-new
  "TODO"
  {:added "4.0"}
  [[_ sym & args] grammar mopts]
  (let [items (map (fn [[k v]]
                     (str (h/strn k) ": " (emit/emit-main v grammar mopts)))
                   (partition 2 args))]
    (str (emit/emit-main sym grammar mopts)
         "{" (str/join ", " items) "}")))

(defn rst-exec
  "TODO"
  {:added "4.0"}
  [[_ & body] grammar mopts]
  (str "{"
       (emit/emit-main
        (list \\
              \\(list \| (apply list 'do body))) grammar mopts)
       "\n}"))

(defn rst-defstruct
  "TODO"
  {:added "4.0"}
  [[_ sym items] grammar mopts]
  (str (rst-attributes sym)
       (cond (empty? items)
             (str "struct " sym ";")

             
             (and (vector? items)
                  (symbol? (first items)))
             (str "struct " sym "[\n"
                  (str/join "\n  " items)
                  "\n]")
             
             :else
             (let [strs (mapcat (fn [args]
                                  (fn/emit-fn-preamble-args
                                   :fn args
                                   grammar mopts))
                                items)]
               (str "struct " sym "{" (str/join ", " strs) "}")))))

(def +features+
  (-> (grammar/build)
      (grammar/build:override
       {:var        {:symbol '#{var} :raw "let" }
        :range      {:raw ".."}
        :new        {:symbol #{'new}
                     :emit  #'rst-new
                     :value true :raw "new"}})
      (grammar/build:extend
       {:ptr-quote     {:op :ptr-quote  :symbol '#{:'}   :raw "'" :emit :pre}
        :ptr-qderef    {:op :ptr-qderef :symbol '#{:&'}  :raw "&'" :emit :pre}
        :range-pattern {:op :range-pattern :symbol '#{:to=}  :raw "..=" :emit :between}
        :cast          {:op :cast :symbol '#{cast}  :raw "_" :emit :between}
        :as            {:op :as :symbol '#{:as}  :raw "as" :emit :infix}
        :unsafe {:op :unsafe
                 :symbol '#{unsafe} :type :block
                 :block {:raw "unsafe"
                         :main    #{:body}
                         :control []}}
        :exec      {:op :exec
                    :symbol '#{exec}  :raw ""
                    :emit  #'rst-exec}})
      (grammar/build:extend
       {:defstruct  {:op :defstruct :symbol '#{defstruct}
                     :type :def :section :code 
                     :emit  #'rst-defstruct
                     :static/type :struct}
        :deftrait   {:op :deftrait :symbol '#{deftrait}
                     :type :def :section :code :emit :macro
                     :macro  #'rst-deftrait
                     :static/type :trait}
        :defenum    {:op :defenum :symbol '#{defenum}
                     :type :def :section :code :emit :macro
                     :macro  #'rst-defenum
                     :static/type :enum}
        :defimpl    {:op :defimpl :symbol '#{defimpl}
                     :type :def :section :code :emit :macro
                     :macro  #'rst-defimpl
                     :static/type :impl}})))

(def +sym-replace+
  {\- "_"
   \= "_eq"
   #_#_#_#_
   \< "_lt"
   \> "_gt"})

(def +template+
  (-> (emit/default-grammar)
      (h/merge-nested
       {:banned #{:set :map :regex}
        :highlight '#{return break}
        :default {:modifier  {:vector-last false}
                  :function  {:prefix "fn"
                              :raw ""
                              :args {:sep ", "}}
                  :typehint  {:enabled true :assign "->" :space " " :after true}
                  :invoke    {:reversed true
                              :hint ":"
                              :space ""
                              :static "::"}}
        :token    {:keyword   {:custom #'name}}
        :data     {:vector    {:custom #'rst-vector}
                  :tuple      {:start "" :end "" :space ""}}
        :block    {:for       {:parameter {:sep ","}}}
        :function {:defn      {:raw ""
                               :args  {:start "(" :end ")" :space ""}}
                   :function  {:raw ""
                               :args      {:start "|" :end "|" :space ""}
                               :body      {:start "" :end "" :append true}}}
        :define   {:defglobal {:raw "let"}
                   :def       {:raw "let"}
                   :declare   {:raw "let"}}})
      (assoc-in [:token :symbol :replace] +sym-replace+)))

(def +grammar+
  (grammar/grammar :rs
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta
   {:module-current   (fn [])
    :module-import    (fn [name _ opts]  
                        (list :- "#include" name))
    :module-export    (fn [{:keys [as refer]} opts])}))

(def +book+
  (book/book {:lang :rust
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))





  
