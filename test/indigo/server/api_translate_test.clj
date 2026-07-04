(ns indigo.server.api-translate-test
  (:require [indigo.server.api-translate :refer :all]
            [indigo.server.api-prompt :as prompt]
            [clojure.string])
  (:use code.test))

^{:refer indigo.server.api-translate/to-layout :added "4.1"}
(fact "formats a single hara.lang form as a string"
  (to-layout "(defn foo [x] (+ x 1))")
  => "(defn foo [x] (+ x 1))")

^{:refer indigo.server.api-translate/to-heal :added "4.1"}
(fact "heals a Clojure code snippet into a formatted string"
  (to-heal "(defn foo [x] (+ x 1))")
  => "(defn foo [x] (+ x 1))")

^{:refer indigo.server.api-translate/from-html :added "4.1"}
(fact "converts simple HTML to a Clojure data structure string"
  (from-html "<div>hello</div>")
  => "[:div \"hello\"]")

^{:refer indigo.server.api-translate/to-html :added "4.1"}
(fact "converts a hara.lang form string to HTML"
  (to-html "[:div {} \"hello\"]")
  => "<div>hello</div>")

^{:refer indigo.server.api-translate/to-plpgsql-dsl :added "4.1"}
(fact "delegates to the plpgsql prompt builder"
  (let [calls (atom [])]
    (with-redefs [prompt/with-prompt-fn (fn [pfn body] (swap! calls conj [pfn body]))]
      (to-plpgsql-dsl "body")
      @calls))
  => [[prompt/to-plpgsql-prompt "body"]])

^{:refer indigo.server.api-translate/to-jsxc-dsl :added "4.1"}
(fact "delegates to the jsxc prompt builder"
  (let [calls (atom [])]
    (with-redefs [prompt/with-prompt-fn (fn [pfn body] (swap! calls conj [pfn body]))]
      (to-jsxc-dsl "body")
      @calls))
  => [[prompt/to-jsxc-prompt "body"]])

^{:refer indigo.server.api-translate/to-js-dsl :added "4.1"}
(fact "delegates to the js prompt builder"
  (let [calls (atom [])]
    (with-redefs [prompt/with-prompt-fn (fn [pfn body] (swap! calls conj [pfn body]))]
      (to-js-dsl "body")
      @calls))
  => [[prompt/to-js-prompt "body"]])

^{:refer indigo.server.api-translate/to-python-dsl :added "4.1"}
(fact "delegates to the python prompt builder"
  (let [calls (atom [])]
    (with-redefs [prompt/with-prompt-fn (fn [pfn body] (swap! calls conj [pfn body]))]
      (to-python-dsl "body")
      @calls))
  => [[prompt/to-python-prompt "body"]])