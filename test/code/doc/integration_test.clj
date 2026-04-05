(ns code.doc.integration-test
  (:require [clojure.java.io :as io]
            [code.doc.executive :as executive])
  (:use code.test))

(defn temp-dir []
  (str (.toFile (java.nio.file.Files/createTempDirectory "code-doc-integration" (make-array java.nio.file.attribute.FileAttribute 0)))))

^{:refer code.doc.executive/render :added "4.1"}
(fact "renders a small foundation-code site end-to-end"
  (let [root (temp-dir)
        docs-dir (io/file root "src-doc" "documentation")
        _ (.mkdirs docs-dir)
        _ (spit (io/file docs-dir "sample.clj")
                "(ns documentation.sample)\n[[:hero {:title \"Sample\" :subtitle \"A demo page\"}]]\n[[:chapter {:title \"Intro\"}]]\n\"Hello from code.doc\"")
        project {:root root
                 :url "https://example.com/repo"
                 :version "1.0.0"
                 :publish {:template {:author "Example"
                                      :email "example@example.com"
                                      :site-label "foundation-code"}
                            :sites {:foundation.code {:theme "foundation"
                                                      :output "public/foundation-code"
                                                      :pages {'index {:base "home.html"
                                                                      :input "src-doc/documentation/sample.clj"
                                                                      :title "Sample"
                                                                      :subtitle "A demo page"}}}}}}
        lookup (executive/all-pages project)]
    (executive/init-template :foundation.code {:write true} (-> project :publish :sites) project)
    (executive/deploy-template :foundation.code {:write true} (-> project :publish :sites) project)
    (executive/render 'foundation.code/index {:write true} lookup project)
    (slurp (io/file root "public" "foundation-code" "index.html")))
  => (contains "Hello from code.doc" "hero" "foundation-code"))
