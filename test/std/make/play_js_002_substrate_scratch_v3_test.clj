(ns std.make.play-js-002-substrate-scratch-v3-test
  (:use code.test)
  (:require [clojure.string :as str]
            [std.fs :as fs]
            [std.make :as make]
            [std.make.common :as common]))

(load-file "src-build/play/js_002_substrate_scratch_v3/build.clj")

^{:refer play.js-002-substrate-scratch-v3.build/PROJECT :added "4.1"}
(fact "the scratch-v3 playground emits Hara DSL substrate examples"
  (let [project (common/make-config
                 (assoc @(:instance play.js-002-substrate-scratch-v3.build/PROJECT)
                        :root "src-build/play/js_002_substrate_scratch_v3"
                        :build ".build/test-play-substrate-scratch-v3"))
        out-dir (common/make-dir project)]
    (try
      (make/build-all project)
      (let [module-output (->> (file-seq (java.io.File. (str out-dir "/public")))
                               (filter #(.endsWith (.getName ^java.io.File %) ".js"))
                               (map slurp)
                               (str/join "\n"))]
        {:files (every? true?
                        (map (fn [path]
                               (fs/exists? (str out-dir "/" path)))
                             play.js-002-substrate-scratch-v3.build/+expected-files+))
         :schema-binding (boolean (re-find #"scratch_v3" module-output))
         :substrate-node (boolean (re-find #"play-scratch-v3-client" module-output))
         :currency-model (boolean (re-find #"currency-all" module-output))
         :profile-model (boolean (re-find #"profile-by-account" module-output))
         :wallet-model (boolean (re-find #"wallet-by-owner" module-output))})
      (finally
        (common/make-dir-teardown project))))
  => {:files true
      :schema-binding true
      :substrate-node true
      :currency-model true
      :profile-model true
      :wallet-model true})
