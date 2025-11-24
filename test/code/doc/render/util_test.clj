(ns code.doc.render.util-test
  (:use code.test)
  (:require [code.doc.render.util :refer :all]))

^{:refer code.doc.render.util/adjust-indent :added "3.0"}
(fact "adjusts indents of multiple lines"

  (adjust-indent "\n    a\n    b\n    c"
                 2)
  => "\n  a\n  b\n  c")

^{:refer code.doc.render.util/basic-html-escape :added "3.0"}
(fact "escape html tags for output"

  (basic-html-escape "<>")
  => "&lt;&gt;")

^{:refer code.doc.render.util/basic-html-unescape :added "3.0"}
(fact "unescape html output for rendering"
  (basic-html-unescape "&amp;lt;") => "&lt;")

^{:refer code.doc.render.util/join-string :added "3.0"}
(fact "join string in the form of vector or string"

  (join-string "hello") => "hello"

  (join-string ["hello" " " "world"]) => "hello world")

^{:refer code.doc.render.util/markup :added "3.0"}
(fact "creates html from markdown script"

  (markup "#title")
  => "<h1>title</h1>")
