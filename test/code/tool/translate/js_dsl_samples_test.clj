(ns code.tool.translate.js-dsl-samples-test
  (:require [code.tool.translate.js-dsl :as sut]
            [std.lib :as h]
            [std.fs :as fs]
            [std.json :as json]
            [code.test :refer [fact]]
            [clojure.string :as str]))

(defn translate-sample [file]
  (let [json-content (json/read (slurp (str file)) json/+keyword-mapper+)]
    (sut/translate-file json-content 'sample.ns)))

(fact "translate all samples"
  (let [root "test-data/code.tool.translate/sample"
        files (->> (fs/select root)
                   (map str)
                   (filter #(str/ends-with? % ".tsx.json")))]
    (doseq [f files]
      (h/prn "Translating" f)
      (translate-sample f))
    (count files) => pos?))

(fact "translate App.tsx export default"
  (let [file "test-data/code.tool.translate/sample/szncampaigncenter/App.tsx.json"
        json-content (json/read (slurp file) json/+keyword-mapper+)
        res (sut/translate-file json-content 'sample.ns)
        res-str (str res)]
    (str/includes? res-str "export :default") => true))
