(ns code.tool.translate.sample-test
  (:use code.test)
  (:require [std.fs :as fs]
            [std.json :as json]
            [std.lib :as h]
            [code.tool.translate.js-dsl :as js-dsl]
            [code.tool.translate.c-dsl :as c-dsl]))

(defn translate-samples []
  (let [target "test-data/code.tool.translate/sample/szncampaigncenter"
        files (keys (fs/list target {:recursive true :include [".tsx.json$"]}))]
    (doseq [f files]
      (try
        (let [ast (json/read (slurp f) json/+keyword-mapper+)
              res (js-dsl/translate-node ast)]
          (h/prn "Translating" f "->" res))
        (catch Exception e
          (h/prn "Error translating" f)
          (h/prn (ex-data e))
          (throw e))))))

^{:refer code.tool.translate/TRANSLATE_JS :adopt true :added "4.1"}
(fact "TODO"
  ^:hidden
  
  (h/with-out-str
    (translate-samples))
  => string?)


