(ns std.lang.model.spec-js.jsx-test
  (:use code.test)
  (:require [std.lang.model.spec-js.jsx :refer :all]
            [std.lang.model.spec-js :as js]))

^{:refer std.lang.model.spec-js.jsx/jsx-key-fn :added "4.0"}
(fact "converts jsx key")

^{:refer std.lang.model.spec-js.jsx/jsx-arr-prep :added "4.0"}
(fact "prepares jsx array"

  (jsx-arr-prep [:div {:className 'a}
                 [:p '(hello 1 2 3)]]
                js/+grammar+
                {})
  => '[:div {:className a} [:p (hello 1 2 3)]])

^{:refer std.lang.model.spec-js.jsx/jsx-standardise-params :added "4.0"}
(fact "converts back to a :className a :class"
  ^:hidden
  
  (jsx-standardise-params {:class ["hello" "world"]})
  => {:className "hello world"})

^{:refer std.lang.model.spec-js.jsx/jsx-arr-norm :added "4.0"}
(fact "normalises the jsx array"

  (jsx-arr-norm [:div])
  => [:div {} nil])

^{:refer std.lang.model.spec-js.jsx/emit-jsx-map-params :added "4.0"}
(fact "emits jsx map params"
  
  (emit-jsx-map-params '{:a (+ 1 2 3) :b "abc"}
                       js/+grammar+
                       {})
  => '("a={1 + 2 + 3}" "b=\"abc\""))

^{:refer std.lang.model.spec-js.jsx/emit-jsx-set-params :added "4.0"}
(fact "emits jsx set params"

  (emit-jsx-set-params '#{a b c}
                       js/+grammar+
                       {})
  => '(("a={a}" "c={c}" "b={b}"))

  (emit-jsx-set-params '#{[a b (:.. c)]}
                       js/+grammar+
                       {})
  => '(("a={a}" "b={b}") "{...c}"))

^{:refer std.lang.model.spec-js.jsx/emit-jsx-params :added "4.0"}
(fact "emits jsx params"
  
  (emit-jsx-params {:a 1 :b 2}
                   js/+grammar+
                   {})
  => [" " "a={1} b={2}"]

  (emit-jsx-params '#{[a b (:.. c)]}
                   js/+grammar+
                   {})
  => [" " "a={a} b={b} {...c}"])

^{:refer std.lang.model.spec-js.jsx/emit-jsx-inner :added "4.0"}
(fact "emits the inner blocks for a jsx form")

^{:refer std.lang.model.spec-js.jsx/emit-jsx-raw :added "4.0"}
(fact "emits the jsx transform"
  
  (emit-jsx-raw [:div {:className 'a}
                 [:p '(hello 1 2 3)]]
                js/+grammar+
                {})
  => "(\n  <div className={a}><p>{hello(1,2,3)}</p></div>)")

^{:refer std.lang.model.spec-js.jsx/emit-jsx :added "4.0"}
(fact "can perform addition transformation if [:grammar :jsx] is false"

  (emit-jsx [:div {:className 'a}
             [:p '(hello 1 2 3)]]
            js/+grammar+
            {:emit {:lang/jsx false}})
  => (std.string/|
      "React.createElement("
      "  \"div\","
      "  {\"className\":a},"
      "  React.createElement(\"p\",{},hello(1,2,3))"
      ")"))
