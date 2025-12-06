(ns code.framework.link.cljs
  (:require [code.framework.link.clj :as clj]
            [code.framework.link.common :as common]
            [std.print :as print]
            [std.lib :as h]))

(defmethod common/-file-linkage :cljs
  ([file]
   (try
     (let [[[_ ns & body] & forms]
           (read-string (str "[" (slurp file) "]"))]
       {:exports #{[:cljs ns]}
        :imports (h/union
                  (->> body
                       (mapcat #(clj/get-namespaces % [:use :require]))
                       (map (fn [clj] [:cljs clj]))
                       set)
                  (->> body
                       (mapcat #(clj/get-namespaces % [:use-macros :require-macros]))
                       (map (fn [clj] [:clj clj]))
                       set))})
     (catch Throwable t
       (print/println "FILE FAILED:" file)))))

(defmethod common/-file-linkage :cljc
  ([file]
   (let [[[_ ns & body] & forms]
         (try
           (read-string {:read-cond :allow} (str "[" (slurp file) "]"))
           (catch Throwable t
             (throw (ex-info "Read failed." {:file file}))))]
     {:exports #{[:cljs ns] [:clj ns]}
      :imports (h/union
                (->> body
                     (mapcat #(clj/get-namespaces % [:require]))
                     (mapcat (fn [clj] [[:cljs clj] [:clj clj]]))
                     set)
                (->> body
                     (mapcat #(clj/get-namespaces % [:use-macros :require-macros]))
                     (map (fn [clj] [:clj clj]))
                     set))})))
