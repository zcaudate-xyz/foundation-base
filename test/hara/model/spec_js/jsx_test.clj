(ns hara.model.spec-js.jsx-test
  (:require [hara.model.spec-js :as js]
            [hara.model.spec-js.jsx :refer :all]
            [std.string.prose :as prose])
  (:use code.test))

^{:refer hara.model.spec-js.jsx/jsx-key-fn :added "4.0"}
(fact "converts jsx key")

^{:refer hara.model.spec-js.jsx/jsx-arr-prep :added "4.0"}
(fact "prepares jsx array"

  (jsx-arr-prep [:div {:className 'a}
                 [:p '(hello 1 2 3)]]
                js/+grammar+
                {})
  => '[:div {:className a} [:p (hello 1 2 3)]])

^{:refer hara.model.spec-js.jsx/jsx-standardise-params :added "4.0"}
(fact "converts back to a :className a :class"

  (jsx-standardise-params {:class ["hello" "world"]})
  => {:className "hello world"})

^{:refer hara.model.spec-js.jsx/jsx-arr-norm :added "4.0"}
(fact "normalises the jsx array"

  (jsx-arr-norm [:div])
  => [:div {} nil])

^{:refer hara.model.spec-js.jsx/emit-jsx-map-params :added "4.0"}
(fact "emits jsx map params"

  (emit-jsx-map-params '{:a (+ 1 2 3) :b "abc"}
                       js/+grammar+
                       {})
  => '("a={1 + 2 + 3}" "b=\"abc\""))

^{:refer hara.model.spec-js.jsx/emit-jsx-set-params :added "4.0"}
(fact "emits jsx set params"

  (emit-jsx-set-params '#{a b c}
                       js/+grammar+
                       {})
  => '(("a={a}" "c={c}" "b={b}"))

  (emit-jsx-set-params '#{[a b (:.. c)]}
                       js/+grammar+
                       {})
  => '(("a={a}" "b={b}") "{...c}"))

^{:refer hara.model.spec-js.jsx/emit-jsx-params :added "4.0"}
(fact "emits jsx params"

  (emit-jsx-params {:a 1 :b 2}
                   js/+grammar+
                   {})
  => [" " "a={1} b={2}"]

  (emit-jsx-params '#{[a b (:.. c)]}
                   js/+grammar+
                   {})
  => [" " "a={a} b={b} {...c}"])

^{:refer hara.model.spec-js.jsx/emit-jsx-inner :added "4.0"}
(fact "emits the inner blocks for a jsx form")

^{:refer hara.model.spec-js.jsx/emit-jsx-raw :added "4.0"}
(fact "emits the jsx transform"

  (emit-jsx-raw [:div {:className 'a}
                 [:p '(hello 1 2 3)]]
                js/+grammar+
                {})
  => "(\n  <div className={a}><p>{hello(1,2,3)}</p></div>)"

  (emit-jsx-raw '[:div {:# [a b c]
                        :.. props}
                  [:p (hello 1 2 3)]]
                js/+grammar+
                {})
  => "(\n  <div a={a} b={b} c={c} {...props}><p>{hello(1,2,3)}</p></div>)"

  (emit-jsx-raw '[:div {:# [(:= a "hello") b c]
                        :.. props}
                  [:p (hello 1 2 3)]]
                js/+grammar+
                {})
  => "(\n  <div a=\"hello\" b={b} c={c} {...props}><p>{hello(1,2,3)}</p></div>)"

  (emit-jsx-raw '[:div {:# [(:= a "hello") b c]
                        :d "hello"
                        :.. props}
                  [:p (hello 1 2 3)]]
                js/+grammar+
                {})
  => "(\n  <div d=\"hello\" a=\"hello\" b={b} c={c} {...props}><p>{hello(1,2,3)}</p></div>)")

^{:refer hara.model.spec-js.jsx/emit-jsx :added "4.0"}
(fact "can perform addition transformation if [:grammar :jsx] is false"

  (emit-jsx [:div {:className 'a}
             [:p '(hello 1 2 3)]]
            js/+grammar+
            {:emit {:lang/jsx false}})
  => (prose/|
      "React.createElement("
      "  \"div\","
      "  {\"className\":a},"
      "  React.createElement(\"p\",{},hello(1,2,3))"
      ")"))
