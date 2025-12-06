(ns code.tool.translate.js-dsl-integration-test
  (:use code.test)
  (:require [code.tool.translate.js-dsl :as sut]
            [std.lib :as h]
            [std.json :as json]
            [std.fs :as fs]
            [std.lang :as l]))

(l/script- :js
  {:runtime :basic
   :import [["@babel/parser" :as BabelParser]]})

(defn.js parse-code
  [code]
  (return
   (JSON.stringify
    (BabelParser.parse code
                       {:sourceType "module"
                        :plugins ["jsx" "typescript" "classProperties" "objectRestSpread"]})
    nil 2)))

(comment

  (first
   (keys (std.fs/list
          "../Smalltalkinterfacedesign/src"
          {:recursive true
           :include [".tsx$"]})))
  
  (parse-code
   (slurp "/Users/chris/Development/greenways/Smalltalkinterfacedesign/src/components/ui/hover-card.tsx"))
  
  (let [source   "../Smalltalkinterfacedesign/src"
        target   "test-data/code.tool.translate/sample/smalltalkinterfacedesign"
        files  (keys (std.fs/list
                      source
                      {:recursive true
                       :include [".tsx$"]}))]
    (doseq [f files]
      (let [ast  (parse-code (slurp f))
            rel  (std.fs/relativize source f)
            file (std.fs/path target (str rel ".json"))
            _    (std.fs/create-directory
                  (std.fs/parent file))]
        (spit file ast))))
  
  (let [target   "test-data/code.tool.translate/sample/smalltalkinterfacedesign"
        files  (keys (std.fs/list
                      target
                      {:recursive true
                       :include [".tsx.json$"]}))]
    (doseq [f files]
      (let [ast  (std.json/read
                  (slurp f)
                  std.json/+keyword-mapper+)]
        (std.lib/prn f)
        (code.tool.translate.js-dsl/translate-node ast)))))
