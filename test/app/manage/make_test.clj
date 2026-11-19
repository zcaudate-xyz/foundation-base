(ns app.manage.make-test
  (:require [app.manage :as app] [app.manage.make :as make]
            [clojure.java.io :as io])
  (:use code.test))

^{:refer app.manage.make/target :added "4.1"}
(fact "std.make renders in memory and the manager owns all writes"
  (let [root (.toFile (java.nio.file.Files/createTempDirectory "app-make-"
                       (make-array java.nio.file.attribute.FileAttribute 0)))
        project {:root (str root) :targets
                 {:assets (make/target {:sources [] :outputs ["generated"]
                                       :sections [{:type :raw :file "generated/page.html"
                                                   :main ["<h1>Hello</h1>"]}]})}}]
    (try
      (let [rendered (app/render project)
            before (.exists (io/file root "generated"))]
        (app/generate project {:write true})
        [(get-in rendered [:artifacts "generated/page.html"]) before
         (slurp (io/file root "generated/page.html")) (:status (app/check project))])
      (finally (doseq [file (reverse (file-seq root))] (.delete file)))))
  => ["<h1>Hello</h1>" false "<h1>Hello</h1>" :ok])
