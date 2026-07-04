(ns indigo.server.api-prompt-test
  (:require [indigo.server.api-prompt :refer :all]
            [clojure.string])
  (:use code.test))

^{:refer indigo.server.api-prompt/with-prompt-fn :added "4.0"}
(fact "is a function"
  (fn? with-prompt-fn)
  => true)

^{:refer indigo.server.api-prompt/to-js-prompt :added "4.0"}
(fact "returns a prompt containing expected sections and the body"
  (with-redefs [clojure.core/slurp (fn [f] (str "MOCK: " f))]
    (let [out (to-js-prompt "body content")]
      [(clojure.string/includes? out "SYSTEM PROMPT START")
       (clojure.string/includes? out "USER PROMPT START")
       (clojure.string/includes? out "MOCK: .prompts/plans/translate_js.md")
       (clojure.string/includes? out "body content")]))
  => [true true true true])

^{:refer indigo.server.api-prompt/to-plpgsql-prompt :added "4.0"}
(fact "returns a prompt containing expected sections and the body"
  (with-redefs [clojure.core/slurp (fn [f] (str "MOCK: " f))]
    (let [out (to-plpgsql-prompt "body content")]
      [(clojure.string/includes? out "SYSTEM PROMPT START")
       (clojure.string/includes? out "USER PROMPT START")
       (clojure.string/includes? out "MOCK: .prompts/plans/translate_pg.md")
       (clojure.string/includes? out "body content")]))
  => [true true true true])

^{:refer indigo.server.api-prompt/to-jsxc-prompt :added "4.0"}
(fact "returns a prompt containing expected sections and the body"
  (with-redefs [clojure.core/slurp (fn [f] (str "MOCK: " f))]
    (let [out (to-jsxc-prompt "body content")]
      [(clojure.string/includes? out "SYSTEM PROMPT START")
       (clojure.string/includes? out "USER PROMPT START")
       (clojure.string/includes? out "MOCK: .prompts/plans/translate_jsxc.md")
       (clojure.string/includes? out "body content")]))
  => [true true true true])

^{:refer indigo.server.api-prompt/to-python-prompt :added "4.0"}
(fact "returns a prompt containing expected sections and the body"
  (with-redefs [clojure.core/slurp (fn [f] (str "MOCK: " f))]
    (let [out (to-python-prompt "body content")]
      [(clojure.string/includes? out "SYSTEM PROMPT START")
       (clojure.string/includes? out "USER PROMPT START")
       (clojure.string/includes? out "MOCK: .prompts/plans/translate_python.md")
       (clojure.string/includes? out "body content")]))
  => [true true true true])