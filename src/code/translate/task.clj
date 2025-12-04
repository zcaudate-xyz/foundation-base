(ns code.translate.task
  (:require [std.fs :as fs]
            [std.json :as json]
            [std.lib :as h]
            [code.translate.js-dsl :as dsl]))

(defn translate-samples []
  (let [target "test-data/code.translate/sample/szncampaigncenter"
        files (keys (fs/list target {:recursive true :include [".tsx.json$"]}))]
    (doseq [f files]
      (try
        (let [ast (json/read (slurp f) json/+keyword-mapper+)
              out-file (str f ".cljs")
              res (dsl/translate-node ast)]
          (h/prn "Translating" f "->" out-file)
          (spit out-file (with-out-str (clojure.pprint/pprint res))))
        (catch Exception e
          (h/prn "Error translating" f)
          (h/prn (ex-data e))
          (throw e))))))
