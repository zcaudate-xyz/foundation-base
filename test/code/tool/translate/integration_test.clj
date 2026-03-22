(ns code.tool.translate.sample-test
  (:require [code.tool.translate.c-dsl :as c-dsl]
            [code.tool.translate.js-dsl :as js-dsl]
            [std.fs :as fs]
            [std.json :as json]
            [std.lib.env :as env])
  (:use code.test))

(defn translate-samples []
  (let [target "test-data/code.tool.translate/sample/szncampaigncenter"
        files (keys (fs/list target {:recursive true :include [".tsx.json$"]}))]
    (doseq [f files]
      (try
        (let [ast (json/read (slurp f) json/+keyword-mapper+)
              res (js-dsl/translate-node ast)]
          (env/prn "Translating" f "->" res))
        (catch Exception e
          (env/prn "Error translating" f)
          (env/prn (ex-data e))
          (throw e))))))

^{:refer code.tool.translate/TRANSLATE_JS :adopt true :added "4.1"}
(fact "TODO"
  ^:hidden
  
  (env/with-out-str
    (translate-samples))
  => string?)


