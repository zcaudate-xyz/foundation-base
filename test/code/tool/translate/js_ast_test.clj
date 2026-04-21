(ns code.tool.translate.js-ast-test
  (:require [code.test :refer :all]
            [code.tool.translate.js-ast :as js-ast]
            [std.fs :as fs]
            [std.json :as json]
            [std.lib.os :as os]
            [std.make :as make]))

^{:refer code.tool.translate.js-ast/initialise :added "4.1"}
(fact "initialises the npm project"

  (with-redefs [os/sh identity]
    (js-ast/initialise))
  => {:root ".build/code.tool.js-ast"
      :args ["npm" "install"]})

^{:refer code.tool.translate.js-ast/translate-ast :added "4.1"}
(fact "generates ast from js files"

  (let [tmp-input (fs/create-tmpfile "var x = 1;")
        tmp-output (str tmp-input ".json")
        ast-json "{\"type\":\"File\",\"program\":{\"type\":\"Program\",\"body\":[]},\"comments\":[]}"]
    (with-redefs [make/build-all (fn [target] target)
                  os/sh (fn [{:keys [args] :as opts}]
                          (when-let [output-file (nth args 3 nil)]
                            (spit output-file ast-json))
                          (assoc opts :out ast-json))]
      [(json/read (:out (js-ast/translate-ast (str tmp-input))))
       (do (js-ast/translate-ast (str tmp-input) tmp-output)
           (json/read (slurp tmp-output)))]))
  => [{"type" "File" "program" {"type" "Program" "body" []} "comments" []}
      {"type" "File" "program" {"type" "Program" "body" []} "comments" []}])

^{:refer code.tool.translate.js-ast/generate-ast :added "4.1"}
(fact "generates ast using the build-ast runner (alias for translate-ast)"

  (let [tmp-input (fs/create-tmpfile "var y = 2;")
        ast-json "{\"type\":\"File\",\"program\":{\"type\":\"Program\",\"body\":[]},\"comments\":[]}"]
    (with-redefs [make/build-all (fn [target] target)
                  os/sh (fn [{:keys [args] :as opts}]
                          (assoc opts :out ast-json))]
      (json/read (:out (js-ast/translate-ast (str tmp-input))))))
  => {"type" "File" "program" {"type" "Program" "body" []} "comments" []})
