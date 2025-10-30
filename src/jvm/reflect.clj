(ns jvm.reflect
  (:require [std.print.format.report :as report]
            [std.object.query :as query]
            [std.object.query.input :as input]
            [std.object.element :as element]
            [std.object.element.common :as common]
            [std.pretty :as pretty]
            [jvm.reflect.print :as print]
            [std.lib :as h])
  (:refer-clojure :exclude [.% .%> .? .?* .?> .* .*> .&]))

(defmacro .%
  "Lists class information
   (.% String)
   => (contains {:modifiers #{:instance :public :final :class},
                 :name \"java.lang.String\"})"
  {:added "3.0"}
  ([obj]
   `(element/class-info ~obj)))

(defn print-hierarchy
  "helper function to `.%>`"
  {:added "3.0"}
  ([tree]
   (-> (h/postwalk (fn [form]
                     (cond (class? form)
                           (.getName ^Class form)

                           (set? form)
                           (vec (sort form))

                           :else form))
                   tree)
       (std.print/print-tree-graph))))

(defmacro .%>
  "Lists the class and interface hierarchy for the class
 
   (.%> String)
   => [java.lang.String
       [java.lang.Object
        #{java.lang.constant.ConstantDesc
          java.io.Serializable
          java.lang.Comparable
          java.lang.constant.Constable
          java.lang.CharSequence}]]"
  {:added "3.0"}
  ([obj]
   `(let [~'result (element/class-hierarchy ~obj)]
      (print-hierarchy ~'result)
      ~'result)))

(defmacro .&
  "pretty prints  the data representation of a value
 
   (.& {:a 1 :b 2 :c 3})"
  {:added "3.0"}
  ([obj]
   `(let [~'res (query/delegate ~obj)]
      (h/local :println (str "\n#" (type ~obj)))
      (doto ~'res
        (->> (into {})
             (pretty/pprint-str)
             (h/local :println))))))

(defn query-printed
  "prints the output on the result to display
 
   (query-printed \"CLASS\" query/query-class String [#\"^c\" :name])
 
   (query-printed \"CLASS\" query/query-class String [#\"^c\"])"
  {:added "3.0"}
  ([type query-fn obj selectors]
   (let [cls (common/context-class obj)
         elems (query-fn obj (input/args-convert selectors))
         title (str "QUERY "
                    type
                    (if (seq selectors) (str " " selectors)))]
     (cond (common/element? elems)
           elems

           :else
           (when (and (seq elems) (common/element? (first elems)))
             (print/print-classname title cls)
             (when (or (= query-fn query/query-supers)
                       (= query-fn query/query-hierarchy))
               (h/local :print "\n")
               (print-hierarchy (element/class-hierarchy cls)))
             (print/print-class elems)))
     elems)))

(defmacro .?
  "queries the java view of the class declaration
 
   (.? String  #\"^c\" :name)
   => (contains [\"charAt\"])
   ;;[\"charAt\" \"chars\" \"checkBoundsBeginEnd\"
   ;; \"checkBoundsOffCount\" \"checkIndex\" \"checkOffset\"
   ;; \"codePointAt\" \"codePointBefore\" \"codePointCount\" \"codePoints\"
   ;; \"coder\" \"compareTo\" \"compareToIgnoreCase\" \"concat\" \"contains\"
   ;; \"contentEquals\" \"copyValueOf\"]"
  {:added "3.0"}
  ([obj & selectors]
   `(query-printed "INSTANCE" query/query-class ~obj (vector ~@selectors))))

(defmacro .?>
  "queries the the class hierarchy
 
   (.?> String  #\"^to\")"
  {:added "3.0"}
  ([obj & selectors]
   `(query-printed "SUPERS" query/query-supers ~obj (vector ~@selectors))))

(defmacro .?*
  "queries the the class hierarchy
 
   (.?* String  #\"^to\")"
  {:added "3.0"}
  ([obj & selectors]
   `(query-printed "HIERARCHY" query/query-hierarchy ~obj (vector ~@selectors))))

(defmacro .*
  "lists what methods could be applied to a particular instance
 
   (.* \"abc\" #\"^to\")
 
   (.* String :name #\"^to\")
   => (contains [\"toString\"])"
  {:added "3.0"}
  ([obj & selectors]
   `(query-printed "INSTANCE" query/query-instance ~obj (vector ~@selectors))))

(defmacro .*>
  "lists what methods could be applied to a particular instance
 
   (.*> \"abc\" #\"^to\")
 
   (.*> String :name #\"^to\")
   => (contains [\"toString\"])"
  {:added "3.0"}
  ([obj & selectors]
   `(query-printed "INSTANCE" query/query-instance ~obj (vector ~@selectors))))

(defmacro .>
  "Threads the first input into the rest of the functions. Same as `->` but
    allows access to private fields using both `:keyword` and `.symbol` lookup:
 
   (.> \"abcd\" :value String.) => \"abcd\"
 
   (.> \"abcd\" .value String.) => \"abcd\"
 
   (comment \"BROKEN\"
     (def -b- (byte-array (.getBytes \"world\")))
     (let [a  \"hello\"
           _  (.> \"hello\" (.value -b-))]
       a)
     => \"world\")"
  {:added "3.0"}
  ([obj] obj)
  ([obj method]
   (cond (not (list? method))
         `(.> ~obj (~method))

         (list? method)
         (let [[method & args] method]
           (cond
             (#{'.% '.%>} method)
             `(~(symbol (str "std.object.element/" method)) ~obj ~@args)

             (#{'.* '.? '.&} method)
             `(~(symbol (str "std.object.query/" method)) ~obj ~@args)

             (and (symbol? method) (.startsWith (name method) "."))
             `(query/apply-element ~obj ~(subs (name method) 1) ~(vec args))

             (keyword? method)
             `(or (~method ~obj ~@args)
                  (let [nm# ~(subs (str method) 1)]
                    (if (some #{nm#} (query/query-instance ~obj [:name]))
                      (query/apply-element ~obj nm# ~(vec args)))))

             :else
             `(~method ~obj ~@args)))))
  ([obj method & more]
   `(.> (.> ~obj ~method) ~@more)))

