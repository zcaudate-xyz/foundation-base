(ns code.manage.unit.walk
  (:require [std.string :as str]
            [code.framework :as base]
            [code.query :as query]
            [std.block :as block]
            [std.block.navigate :as nav]))

(defn walk-string
  "helper function for file manipulation for string output"
  {:added "3.0"}
  ([s var references action-fn]
   (walk-string s var references action-fn (atom [])))
  ([s var references action-fn syms]
   (let [nav  (-> (nav/parse-root s)
                  (nav/down))
         nsp  (-> (query/$ nav [(ns | _ & _)] {:walk :top})
                  first)
         wrap-save (fn [f]
                     (fn [nav]
                       (swap! syms conj (nav/value nav))
                       (f nav)))
         action (wrap-save (action-fn nsp references))
         nav (-> nav
                 (query/modify (base/import-selector var)
                               action
                               {:walk :top}))]
     (nav/root-string nav))))

(defn walk-file
  "helper function for file manipulation used by import and purge"
  {:added "3.0"}
  ([file var references action-fn]
   (let [s    (slurp file)
         syms (atom [])]
     (->> (walk-string s var references action-fn syms)
          (spit file))
     @syms)))
