(ns code.translate.smalltalk-samples-test
  (:use code.test)
  (:require [code.translate.js-dsl :as sut]
            [std.fs :as fs]
            [std.json :as json]
            [std.lib :as h]))

(def sample-dir "test-data/code.translate/sample/smalltalkinterfacedesign")

(fact "translates all smalltalk samples without error"
  (let [files (keys (fs/list sample-dir {:recursive true :include [".tsx.json$"]}))]
    (doseq [f files]
      (let [ast (json/read (slurp f) json/+keyword-mapper+)]
        (sut/translate-node ast)
        => any))))
