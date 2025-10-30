(ns rt.solidity.grammar
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as emit-common]
            [std.lang.base.emit-fn :as emit-fn]
            [std.lang.base.emit-block :as emit-block]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.grammar-spec :as grammar-spec]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.string :as str]
            [std.lib :as h])
  (:refer-clojure :exclude [require]))

(def +visibility+
  [:public
   :private
   :external
   :internal])

(def +modifiers+
  [:pure
   :view
   :payable
   :constant
   :immutable
   :anonymous
   :indexed
   :virtual
   :override])

(def +types+
  [:uint
   :bool
   :address
   :bytes32])

(defn sol-util-types
  "format sol types"
  {:added "4.0"}
  [v]
  (cond (or (keyword? v)
            (vector? v)
            (string? v))
        (h/strn v)
        
        :else v))

(defn sol-map-key
  "formats sol map key"
  {:added "4.0"}
  ([key grammar mopts]
   (str/replace-all
    (h/strn key)
    "-"
    "_")))

(defn sol-keyword-fn
  "no typecast, straight forward print"
  {:added "4.0"}
  [form grammar mopts]
  (emit-common/*emit-fn* (apply list :- form) grammar mopts))

(defn sol-def
  "creates a definition string"
  {:added "4.0"}
  [[_ sym & [value]] grammar mopts]
  (let [typestr  (emit-fn/emit-fn-type sym nil grammar mopts)]
    (str typestr " " (emit-common/*emit-fn* sym grammar mopts)
         (if value
           (str " = " (emit-common/*emit-fn* value grammar mopts))
           "")
         ";")))

(defn sol-fn-elements
  "creates elements for function"
  {:added "4.0"}
  [sym args body grammar mopts]
  (let [block    (emit-fn/emit-fn-block :defn grammar)
        typestr  (emit-fn/emit-fn-type sym nil grammar mopts) 
        preamble (emit-fn/emit-fn-preamble [:defn sym args]
                                           grammar
                                           mopts)
        codebody (emit-block/emit-block-body nil block body grammar mopts)]
    [typestr preamble codebody]))

(defn sol-emit-returns
  "emits returns"
  {:added "4.0"}
  [[_ returns] grammar mopts]
  (emit-common/*emit-fn*
   (list 'returns
         (cond (and (list? returns)
                    (= 'quote (first returns)))
               (list 'quote (vec (second returns)))
               
               (vector? returns)
               (apply list :- returns)

               :else (list :- returns)))
   grammar mopts))

(defn sol-defn
  "creates def contstructor form"
  {:added "4.0"}
  [[_ sym args & body :as form] grammar mopts]
  (let [{:static/keys [returns
                       modifiers]} (meta sym)
        [typestr preamble codebody] (sol-fn-elements sym args body grammar mopts)]
    (str/join " " (filter not-empty ["function" preamble typestr
                                     (if returns
                                       (sol-emit-returns [nil returns] grammar mopts))
                                     codebody]))))

(defn sol-defconstructor
  "creates the constructor"
  {:added "4.0"}
  [[_ sym args & body :as form] grammar mopts]
  (let [[typestr preamble codebody] (sol-fn-elements 'constructor args body grammar mopts)]
    (str/join " " (filter not-empty [preamble typestr codebody]))))

(defn sol-defevent
  "creates an event"
  {:added "4.0"}
  [[_ sym & args] grammar mopts]
  (let [preamble (emit-fn/emit-fn-preamble [:defn sym (vec (mapcat identity args))]
                                           grammar
                                           mopts)]
    (str "event " preamble ";")))

(defn sol-tf-var-ext
  "transforms a var"
  {:added "4.0"}
  [[_ sym & args]]
  (if (list? sym)
    (if (= 'quote (first sym))
      (list := (list :- sym) (last args))
      (list := (apply list :- sym) (last args)))
    (apply list 'var* sym args)))

(defn sol-tf-mapping
  "transforms mapping call"
  {:added "4.0"}
  [[_ [from to]]]
  (list 'mapping (list :- from "=>" to)))

(defn sol-defstruct
  "transforms a defstruct call"
  {:added "4.0"}
  [[_ sym & args]]
  (let [vars (map #(apply list 'var %) args)]
    (list :% (list :- "struct" sym) (list :- "{\n")
          (list \| (apply list 'do vars))
          (list :- "\n}"))))

(defn sol-defaddress
  "transforms a defaddress call"
  {:added "4.0"}
  [[_ sym value]]
  (let [modifiers (mapv sol-util-types (:- (meta sym)))]
    (list :% (apply list :- "address"
                    (cond-> (conj modifiers sym)
                      value (conj value)))
          \;)))

(defn sol-defenum
  "transforms a defenum call"
  {:added "4.0"}
  [[_ sym values]]
  (list :% (list :- "enum" sym
                 (list :- "{")
                 (list 'quote (vec values))
                 (list :- "}"))))

(defn sol-defmapping
  "transforms a mapping call"
  {:added "4.0"}
  [[_ sym [from to]]]
  (let [modifiers (mapv sol-util-types (:- (meta sym)))]
    (list :% (apply list :- (list :mapping [from to])
                    (conj modifiers sym))
          \;)))

(defn sol-definterface
  "transforms a definterface call"
  {:added "4.0"}
  [[_ sym body] grammar mopts]
  (let [fn-stct (fn [sym args]
                  (str "struct " sym " {\n"
                       (str/indent (str/join "\n"
                                             (map (fn [arr]
                                                    (str (str/join " " (map h/strn arr))
                                                         ";"))
                                                  args))
                                   2)
                       "\n}"))
        fn-enum (fn [sym args]
                  (str "enum " sym " { " (str/join ", " (map h/strn args)) " }"))
        fn-func (fn [sym args]
                  (let [{:static/keys [returns]} (meta sym)
                        [typestr preamble _] (sol-fn-elements sym args body grammar mopts)]
                    (str/join " " (filter not-empty ["function" preamble typestr
                                                     (str (if returns
                                                            (sol-emit-returns [nil returns] grammar mopts))
                                                          ";")]))))
        fns (->> (partition 2 body)
                 (map (fn [[sym args]]
                        (case (:type (meta sym))
                          :struct (fn-stct sym args)
                          :enum   (fn-enum sym args)
                          (fn-func sym args))))
                 (str/join "\n"))]
    (str "interface " sym " {\n"
         (str/indent fns 2)
         "\n}")))

(def +features+
  (-> (grammar/build :include [:builtin
                               :builtin-global
                               :builtin-module
                               :builtin-helper
                               :free-control
                               :free-literal
                               :math
                               :compare
                               :counter
                               :class
                               :bit
                               :logic
                               :return
                               :data-table
                               :data-shortcuts
                               :vars
                               :fn
                               :control-base
                               :control-general
                               :top-base
                               :top-global
                               :top-declare
                               :for
                               :coroutine
                               :macro
                               :macro-arrow
                               :macro-let
                               :macro-xor
                               :macro-forange
                               :macro-case])
      (grammar/build:override
       {:var       {:symbol '#{var*}}
        :defn      {:emit    #'sol-defn
                    :static/type :function}
        :def       {:emit    #'sol-def}})
      
      (grammar/build:extend
       {:delete    {:op :delete  :symbol  '#{delete} :raw "delete" :emit :prefix}
        :emit      {:op :emit    :symbol  '#{emit}  :raw "emit" :emit :prefix}
        :mapping   {:op :mapping  :symbol #{:mapping}
                    :macro #'sol-tf-mapping :emit :macro}
        :blank     {:op :blank    :symbol  '#{_} :raw ""
                    :value true :emit :throw}})
      
      (grammar/build:extend
       {:definterface   {:op :definterface :symbol '#{definterface}
                         :type :def :section :header
                         :emit   #'sol-definterface
                         :static/type :interface}
        :defconstructor {:op :defcontructor :symbol '#{defconstructor}
                         :type :def :section :code
                         :emit    #'sol-defconstructor
                         :static/type :contructor}
        :defstruct   {:op :defstruct :symbol '#{defstruct}
                      :type :def :section :code :emit :macro
                      :macro  #'sol-defstruct
                      :static/type :struct}
        :defevent    {:op :defevent :symbol '#{defevent}
                      :type :def :section :code
                      :emit    #'sol-defevent
                      :static/type :event}
        :defenum     {:op :defenum :symbol '#{defenum}
                      :type :def :section :code :emit :macro
                      :macro  #'sol-defenum
                      :static/type :enum}
        :defaddress   {:op :defaddress :symbol '#{defaddress}
                       :type :def :section :code :emit :macro
                       :macro   #'sol-defaddress
                       :static/type :address}

        :defmapping   {:op :defmapping :symbol '#{defmapping}
                       :type :def :section :code :emit :macro
                       :macro   #'sol-defmapping
                       :static/type :mapping}
        
        :var-ext     {:op :var-ext   :symbol '#{var}
                      :macro  #'sol-tf-var-ext :type :macro}})))

(def +template+
  (->> {:banned #{}
        :highlight '#{return break}
        :default {:modifier  {:vector-last false}
                  :function  {:raw ""}
                  :invoke    {:keyword-fn #'sol-keyword-fn}}
        :token   {:keyword    {:custom (fn [x]
                                         (h/strn x))}
                  :symbol     {:replace {\: "__"}}}
        :data    {:map-entry {:space " " :key-fn #'sol-map-key}
                  :free       {:start ""  :end "" :sep ", "}
                  :tuple     {:start "(" :end ")" :sep ", "}}
        :block  {:for       {:parameter {:sep ","}}}
        :define {:def       {:raw ""}}}
       (h/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :sol
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta
   {:module-current   (fn [])
    :module-import    (fn [name _ opts]  
                        (h/$ (:- "#include" ~name)))
    :module-export    (fn [{:keys [as refer]} opts])}))

(def +book+
  (book/book {:lang :solidity
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
