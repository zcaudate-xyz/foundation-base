(ns hara.model.spec-js-test
  (:require [hara.lang :as l]
            [hara.model.spec-js :refer :all]
            [std.string.prose :as prose])
  (:use code.test))

^{:refer hara.model.spec-js/emit-html :added "4.0"}
(fact "emits html"

  (emit-html [:hello] +grammar+ {})
  => "<hello></hello>")

^{:refer hara.model.spec-js/js-regex :added "4.0"}
(fact "outputs the js regex"

  (js-regex #"abc")
  => "/abc/")

^{:refer hara.model.spec-js/js-map-key :added "4.0"}
(fact "emits a map key"

  (js-map-key 'hello +grammar+ {})
  => "[hello]"

  (js-map-key '(+ hello world) +grammar+ {})
  => "[(+ hello world)]"

  (js-map-key :hello +grammar+ {})
  => "\"hello\""

  (js-map-key :hello-world +grammar+ {})
  => "\"hello_world\"")

^{:refer hara.model.spec-js/js-vector :added "4.0"}
(fact "emits a js vector"

  (js-vector [1 2 3 4] +grammar+ {})
  => "[1,2,3,4]"

  (js-vector [:div {} "hello"] +grammar+ {})
  => "(\n  <div>hello</div>)")

^{:refer hara.model.spec-js/js-map :added "4.0"}
(fact "emits a js map"

  (js-map {:hello-world "hello"} +grammar+ {})
  => "{\"hello_world\":\"hello\"}"

  (l/emit-as :js
             '[{:# [hello-world]}])
  => "{hello_world}")

^{:refer hara.model.spec-js/js-set :added "4.0"}
(fact "emits a js set"

  (js-set '#{...x y {:a 1 :b 2}} +grammar+ {})
  => "{...x,y,\"a\":1,\"b\":2}"

  (js-set '#{...x y {a 1 :b 2}} +grammar+ {})
  => "{...x,y,[a]:1,\"b\":2}"

  (js-set '#{[...x y (% a) 1 :b 2]} +grammar+ {})
  => "(tab ...x y (% a) 1 :b 2)")

^{:refer hara.model.spec-js/js-defclass :added "4.0"}
(fact "creates a defclass function"

  (hara.lang/emit-as
   :js [(js-defclass '(defclass.js Try
                        [:- React.Component]

                        ^{:- [:static]}
                        (fn getDerivedStateFromError [error]
                          (return #{error {:hasError true}}))

                        (fn constructor []
                          (:= (. this state)
                              {:hasError false
                               :error nil}))

                        (fn render []
                          (if (. this state hasError)
                            (return (. this props fallback))
                            (return (. this props children)))))
                     )])
  => (prose/|
      "class Try extends React.Component{"
      "  static getDerivedStateFromError(error){"
      "    return {error,\"hasError\":true};"
      "  }"
      "  constructor() {"
      "    this.state = {\"hasError\":false,\"error\":null};"
      "  }"
      "  render() {"
      "    if(this.state.hasError){"
      "      return this.props.fallback;"
      "    }"
      "    else{"
      "      return this.props.children;"
      "    }"
      "  }"
      "}"))

^{:refer hara.model.spec-js/js-tf-var-let :added "4.0"}
(fact "outputs the let keyword"

  (js-tf-var-let '(var a 1))
  => '(var* :let a := 1))

^{:refer hara.model.spec-js/js-tf-var-const :added "4.0"}
(fact "outputs the const keyword"

  (js-tf-var-const '(const a 1))
  => '(var* :const a := 1))

^{:refer hara.model.spec-js/js-tf-for-object :added "4.0"}
(fact "custom for:object code"

  (js-tf-for-object '(for:object [[k v] {:a 1}]
                              [k v]))
  => '(for [(var* :let [k v]) :of (Object.entries {:a 1})] [k v]))

^{:refer hara.model.spec-js/js-tf-for-array :added "4.0"}
(fact "custom for:array code"

  (js-tf-for-array '(for:array [e [1 2 3 4 5]]
                             [k v]))
  => '(for [(var* :let e) :of (% [1 2 3 4 5])] [k v])

  (js-tf-for-array '(for:array [[i e] arr]
                            [k v]))
  => '(for [(var* :let i := 0) (< i (. arr length))
            (:++ i)] (var* :let e (. arr [i])) [k v]))

^{:refer hara.model.spec-js/js-tf-for-iter :added "4.0"}
(fact "custom for:iter code"

  (js-tf-for-iter '(for:iter [e iter]
                          e))
  => '(for [(var* :let e) :of (% iter)] e))


^{:refer hara.model.spec-js/js-tf-prototype-create :added "4.1"}
(fact "creates js prototypes")

^{:refer hara.model.spec-js/js-tf-prototype-method :added "4.1"}
(fact "calls js prototype methods")
