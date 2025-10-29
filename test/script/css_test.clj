(ns script.css-test
  (:use code.test)
  (:require [script.css :refer :all]))

^{:refer script.css/generate-style :added "3.0"}
(fact "creates a single-line style string from a map of style attributes (e.g., {:bold true} -> 'bold: true;')"

  (generate-style {:bold true})
  => "bold: true;")

^{:refer script.css/generate-css :added "3.0"}
(fact "generates a CSS stylesheet string from a list of selector-style map pairs"

  (generate-css [[:node {:bold true
                         :color "black"}]
                 [:h1  {:align :left}]])
  => "node {\n  bold: true;\n  color: black;\n}\nh1 {\n  align: left;\n}")

^{:refer script.css/parse-pair :added "3.0"}
(fact "parses a single CSS style string (e.g., \"bold: true\") into a keyword-string pair (e.g., [:bold \"true\"])")

  (parse-pair "bold: true")
  => [:bold "true"])

^{:refer script.css/parse-rule :added "3.0"}
(fact "parses a single CSS rule from a stylesheet string, used as a helper for `parse-css`")

^{:refer script.css/parse-css :added "3.0"}
(fact "parses a CSS stylesheet string into a list of rules, where each rule is a vector of a selector keyword and a map of style attributes"

  (parse-css "node {\n  bold: true;\n  color: black;\n}\nh1 {\n  align: left;\n}")
  => [[:node {:bold "true", :color "black"}]
      [:h1 {:align "left"}]])

^{:refer script.css/parse-style :added "3.0"}
(fact "parses a CSS style string (e.g., \"bold: true;\") into a map of style attributes (e.g., {:bold \"true\"})"

  (parse-style "bold: true;")
  => {:bold "true"})