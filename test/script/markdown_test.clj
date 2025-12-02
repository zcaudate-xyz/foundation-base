(ns script.markdown-test
  (:use code.test)
  (:require [script.markdown :refer :all]))

^{:refer script.markdown/parse :added "3.0"}
(fact "parses markdown to html"

  (parse "# hello")
  => "<h1>hello</h1>")

^{:refer script.markdown/parse-metadata :added "3.0"}
(fact "parses markdown metadata"

  (parse-metadata "title: hello\n\n# hello")
  => {:title ["hello"]})
