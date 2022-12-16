(ns std.lang.model.spec-js.html
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as common]
            [std.lang.base.util :as ut]
            [std.html :as html]
            [std.string :as str]
            [std.lib :as h]))

(defn wrap-indent-inner
  "increase indentation in walk inner"
  {:added "4.0"}
  ([f]
   (fn [form]
     (if (and (vector? form)
              (keyword? (first form)))
       (binding [common/*indent* (+ common/*indent* 2)]
         (f form))
       (f form)))))

(defn wrap-indent-outer
  "decrese indentation in walk outer"
  {:added "4.0"}
  ([f]
   (fn [form]
     (if (and (vector? form)
              (keyword? (first form)))
       (binding [common/*indent* (- common/*indent* 2)]
         (f form))
       (f form)))))

(defn prewalk-indent
  "preserves indentations"
  {:added "4.0"}
  ([f form]
   (h/walk (wrap-indent-inner (partial prewalk-indent f))
           (wrap-indent-outer identity) (f form))))

(defn prepare-html
  "prepares the html, embedding any new scripts"
  {:added "4.0"}
  ([tree nsp]
   tree
   #_(prewalk-indent
    (fn [form]
      (if (and (vector? form)
               (symbol? (first form))
               (str/starts-with? (first form) "%"))
        (let [ptr @(resolve (second form))
              {:keys [module id lang]} ptr]
          (str (common/with-indent [2]
                 (str (common/newline-indent)
                      (common/emit-main (:form (lib/lib:get-entry lang (ut/sym-full module id)))
                                        (lib/lib:get-grammar lang)
                                        nsp)))
               (common/newline-indent)))
        form))
    tree)))

(defn emit-html
  "emits the html
 
   (emit-html [:a [:b [:c]]]
              {} {})
   => \"<a><b>\\n    <c></c></b></a>\""
  {:added "4.0"}
  ([arr _ nsp]
   (let [tree (prepare-html arr nsp)]
     (html/html tree))))



