(ns std.html-test
  (:use code.test)
  (:require [std.html :refer :all])
  (:import (org.jsoup.nodes Element)
           (org.jsoup.select Elements)))

(def +content+
  (str "<html id=3><head><title>First parse</title></head>"
       "<body><p>Parsed HTML into a doc.</p></body></html>"))

^{:refer std.html/node->tree :added "3.0"}
(fact "converts a Jsoup node to tree"

  (-> (parse "<body><div>hello</div>world</body>")
      (node->tree))
  => [:body [:div "hello"] "world"])

^{:refer std.html/tree->node :added "3.0"}
(fact "converts a tree to a Jsoup node"

  (tree->node [:body [:div "hello"] "world"])
  => org.jsoup.nodes.Element)

^{:refer std.html/parse :added "3.0"}
(fact "reads a Jsoup node from string"

  (parse "<body><div>hello</div>world</body>")
  => org.jsoup.nodes.Element)

^{:refer std.html/inline :added "4.0"}
(fact "emits function without any newlines"

  (inline (html [:body [:div "hello"] "world"]))
  => "<body>  <div>hello</div>world</body>")

^{:refer std.html/tighten :added "3.0"}
(fact "removes lines for elements that contain no internal elements"

  (tighten "<b>\nhello\n</b>")
  => "<b>hello</b>")

^{:refer std.html/generate :added "3.0"}
(fact "generates string html for element"

  (generate (tree->node [:body [:div "hello"] "world"]))
  => "<body>\n  <div>hello</div>world\n</body>")

^{:refer std.html/html :added "3.0"}
(fact "converts either node or tree representation to a html string"

  (html [:body [:div "hello"] "world"])
  => "<body>\n  <div>hello</div>world\n</body>")

^{:refer std.html/html-inline :added "4.0"}
(fact "emits body with no newline"

  (html-inline [:body [:div "hello"] "world"])
  => "<body>  <div>hello</div>world</body>")

^{:refer std.html/node :added "3.0"}
(fact "converts either a string or tree representation to a Jsoup node"

  (node [:body [:div "hello"] "world"])
  => org.jsoup.nodes.Element)

^{:refer std.html/tree :added "3.0"}
(fact "converts either a string or node representation into a tree"

  (tree +content+)
  => [:html {:id "3"}
      [:head [:title "First parse"]]
      [:body [:p "Parsed HTML into a doc."]]])

^{:refer std.html/select :added "3.0"}
(fact "applies css selector to node"
  (let [node (parse "<body><div class='foo'>hello</div></body>")]
    (select node ".foo")
    => (partial instance? Elements)))

^{:refer std.html/select-first :added "3.0"}
(fact "gets the first match given selector and node"
  (let [node (parse "<body><div class='foo'>hello</div></body>")]
    (select-first node ".foo")
    => (partial instance? Element)))
