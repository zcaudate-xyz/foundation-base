(ns jvm.reflect.print
  (:require [clojure.string]
            [std.lib.env :as env]
            [std.lib.invoke :as invoke]
            [std.object.element :as element]
            [std.print :as print]
            [std.string.common :as common])
  (:refer-clojure :exclude [instance?]))

(defonce +col-lengths+
  {:name      30
   :type      30
   :params    40
   :modifiers 20})

(defonce +col-options+
  {:spacing 1
   :padding 0})

(defonce +col-colors+
  {:type        {:weight :bold   :highlight :white}
   :params      {:weight :normal :highlight :yellow}
   :modifiers   {:weight :normal :highlight :blue}})

(defonce +col-alignment+  (cons :right (repeat :left)))

(defonce +matrix+
  {:instance
   {:title "INSTANCE"    :predicate element/instance?    :highlight :cyan}
   :static
   {:title "STATIC"      :predicate element/static?      :highlight :magenta}
   :public
   {:title "PUBLIC"      :predicate element/public?      :weight :bold}
   :private
   {:title "PRIVATE"     :predicate element/private?}
   :protected
   {:title "PROTECTED"   :predicate element/protected?}
   :plain
   {:title "PLAIN"       :predicate element/plain?}
   :constructor
   {:title "CONSTRUCTOR" :predicate element/constructor? :labels [:params] :alignment [:left] :highlight :green}
   :field
   {:title "FIELD"       :predicate element/field?       :labels [:name :type :modifiers]}
   :method
   {:title "METHOD"      :predicate element/method?      :labels [:name :type :params :modifiers]}})

(def +shorthand+
  [["java.lang.reflect." "j.l.r."]
   ["java.lang.annotation." "j.l.a."]
   ["java.lang." ""]
   ["java.security." "j.sc."]
   ["java.fx." "j.fx."]
   ["java.io." "j.io."]
   ["java.net." "j.n."]
   ["java.util." "j.u."]
   ["java." "j."]
   ["clojure.lang." ""]
   ["clojure." "c."]
   ["jdk.internal.reflect" "jdk.i.r."]
   ["jdk.internal." "jdk.i."]
   ["sun.reflect.annotation." "sun.r.a."]
   ["sun.reflect.generics.repository" "sun.r.g.r"]
   ["sun.reflect.generics.factory" "sun.r.g.f"]
   ["sun.reflect.generics." "sun.r.g."]
   ["scala.collection.immutable" "s.c.i"]
   ["scala.collection.mutable" "s.c.m"]
   ["scala.collection.parallel.immutable" "s.c.p.i"]
   ["scala.collection.parallel.mutable" "s.c.p.m"]
   ["scala.collection" "s.c"]
   ["scala." "s."]
   ["hara." "h."]])

(defn format-type
  "returns a nice looking version of the class
 
   (format-type String) => \"String\"
 
   (format-type (type {})) => \"PersistentArrayMap\"
 
   (format-type Byte/TYPE) => \"byte\"
 
   (format-type (type (char-array ()))) => \"char[]\""
  {:added "3.0"}
  ([class]
   (format-type class +shorthand+))
  ([^Class class shorthand]
   (cond (.isArray class)
         (str (format-type (.getComponentType class)) "[]")

         (.isPrimitive class)
         (str class)

         :else
         (reduce (fn [^String s [^String full ^String short]]
                   (.replace s full short))
                 (.getName class)
                 shorthand))))

(defn order-modifiers
  "orders elements based on modifiers for printing"
  {:added "3.0"}
  ([modifiers]
   (cond-> modifiers
     (:final modifiers) (->> (disj modifiers :final)
                             (sort)
                             (cons :final))
     :then (-> sort (common/joinl " ")))))

(defonce +customiser+
  {:params #(mapv format-type %)
   :type   format-type
   :modifiers (fn [modifiers]
                (->> (keys +matrix+)
                     (apply disj modifiers)
                     (order-modifiers)))})

(defn col-color
  "returns the column color"
  {:added "3.0"}
  ([i highlight runtime access label colors matrix]
   (let [selection  (fn [k]
                      (->> (map (comp k matrix) [runtime access])
                           (cons (if (zero? i)
                                   (if (= k :highlight) highlight)
                                   (k (get colors label))))
                           (filter identity)
                           (first)))
         highlight (selection :highlight)
         weight    (selection :weight)]
     (disj (set [highlight weight]) nil))))

(defn col-settings
  "returns the column settings"
  {:added "3.0"}
  ([ks]
   (col-settings ks {}))
  ([[elem runtime access] {:keys [matrix lengths colors]}]
   (let [matrix  (merge +matrix+ matrix)
         lengths (merge +col-lengths+ lengths)
         colors  (merge +col-colors+ colors)
         {:keys [labels alignment highlight]
          :or   {alignment +col-alignment+}} (get matrix elem)]
     (map-indexed (fn [i label]
                    {:id     label
                     :align  (nth alignment i)
                     :length (get lengths label)
                     :color  (col-color i highlight runtime access label colors matrix)})
                  labels))))

(invoke/definvoke class-elements
  "returns all class elements for a given category"
  {:added "3.0"}
  [:memoize]
  ([elems ks]
   (class-elements elems ks {}))
  ([elems [k & more :as ks] opts]
   (let [matrix (merge +matrix+ opts)]
     (cond (empty? ks)
           elems

           (empty? more)
           (filter (get-in matrix [k :predicate]) elems)

           :else
           (let [nelems (class-elements elems [k] opts)]
             (class-elements nelems more opts))))))

(defn print-elements
  "prints all elements in a given category"
  {:added "3.0"}
  ([title elems col-settings]
   (print-elements title elems col-settings {}))
  ([title elems col-settings {:keys [options]}]
   (let [options (merge +col-options+ options)]
     (when (seq elems)
       (env/local :print "\n")
       (print/print-subtitle title)
       (env/local :print "\n"))
     (doseq [elem elems]
       (let [row (map (fn [{:keys [id]}]
                        (let [data (get elem id)
                              func (get +customiser+ id)]
                          (if func
                            (func data)
                            data)))
                      col-settings)]
         (print/print-row row (merge options {:columns col-settings})))))))

(defn category-title
  "creates a category title"
  {:added "3.0"}
  ([ks]
   (category-title ks {}))
  ([ks {:keys [matrix class]}]
   (let [matrix (merge +matrix+ matrix)
         title  (-> (map (comp :title matrix) (reverse ks))
                    (common/joinl " "))]
     title)))

(defn print-classname
  "prints the classname with title"
  {:added "3.0"}
  ([title ^Class cls]
   (print/print-title (str title " - " (clojure.string/upper-case (.getName cls))))))

(defn print-category
  "prints a given category"
  {:added "3.0"}
  ([elems ks]
   (print-category elems ks {}))
  ([elems ks opts]
   (let [categories [:public :private :protected :plain]
         selems     (mapcat (fn [k]
                              (class-elements elems (conj ks k)))
                            categories)]
     (if (seq selems)
       (print/print-title (category-title ks opts)))
     (mapv (fn [k]
             (print-elements (category-title [k] opts)
                             (class-elements elems (conj ks k))
                             (col-settings (conj ks k))
                             {:options {:padding 5}}))
           categories))))

(defn print-class
  "prints a given class"
  {:added "3.0"}
  ([elems]
   (print-class elems {}))
  ([elems opts]
   (mapv (fn [ks]
           (print-category elems ks opts))
         [[:constructor]
          [:field :static]
          [:method :static]
          [:field  :instance]
          [:method :instance]])))

(defonce +element-comp+
  (juxt :name
        (comp (fn [params]
                (if-let [p (first params)]
                  (format-type p)
                  ""))
              :params)
        (comp count :params)))

(defn sort-elements
  "sorts elements given a comparator"
  {:added "3.0"}
  ([elems]
   (sort-by +element-comp+ elems)))
