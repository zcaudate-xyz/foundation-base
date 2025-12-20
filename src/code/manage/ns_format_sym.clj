(ns code.manage.ns-format-sym
  (:require [code.framework :as base]
            [code.query :as query]
            [std.block :as block]
            [std.block.navigate :as nav]
            [std.lib :as h]
            [std.string :as str]))

(defn alias:expand-tb
  "expands tb alias to type-base"
  {:added "3.0"}
  [nav]
  (query/modify nav
                [{:or ['ns 'env/init]}
                 {:first :require}
                 '|
                 ['szndb.core.type-base :as 'tb]]
                (fn [nav]
                  (nav/replace nav ['szndb.core.type-base :as 'type-base]))))

(defn usage:expand-tb
  "expands tb/ usage to type-base/"
  {:added "3.0"}
  [nav]
  (query/modify nav
                symbol?
                (fn [nav]
                  (let [s (nav/value nav)]
                    (if (and (symbol? s)
                             (str/starts-with? (str s) "tb/"))
                      (nav/replace nav (symbol (str/replace-first (str s) "tb/" "type-base/")))
                      nav)))))

(defn ns-format-sym
  "formats symbols in namespace"
  {:added "3.0"}
  ([ns params lookup project]
   (let [edits [alias:expand-tb
                usage:expand-tb]]
     (base/refactor-code ns (assoc params :edits edits) lookup project))))
