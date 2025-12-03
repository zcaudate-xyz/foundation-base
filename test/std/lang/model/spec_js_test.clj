(ns std.lang.model.spec-js-test
  (:use code.test)
  (:require [std.lang.model.spec-js :refer :all]
            [std.lang :as l]
            [std.lib :as h]))

^{:refer std.lang.model.spec-js/emit-html :added "4.0"}
(fact "emits html"
  ^:hidden
  
  (emit-html [:hello] +grammar+ {})
  => "<hello></hello>")

^{:refer std.lang.model.spec-js/js-regex :added "4.0"}
(fact "outputs the js regex"
  ^:hidden
  
  (js-regex #"abc")
  => "/abc/")

^{:refer std.lang.model.spec-js/js-map-key :added "4.0"}
(fact "emits a map key"
  ^:hidden
  
  (js-map-key 'hello +grammar+ {})
  => "[hello]"

  (js-map-key '(+ hello world) +grammar+ {})
  => "[(+ hello world)]"

  (js-map-key :hello +grammar+ {})
  => "\"hello\""

  (js-map-key :hello-world +grammar+ {})
  => "\"hello_world\"")

^{:refer std.lang.model.spec-js/js-vector :added "4.0"}
(fact "emits a js vector"
  ^:hidden
  
  (js-vector [1 2 3 4] +grammar+ {})
  => "[1,2,3,4]"

  (js-vector [:div {} "hello"] +grammar+ {})
  => "(\n  <div>hello</div>)")

^{:refer std.lang.model.spec-js/js-map :added "4.0"}
(fact "emits a js map"
  ^:hidden

  (js-map {:hello-world "hello"} +grammar+ {})
  => "{\"hello_world\":\"hello\"}"

  (l/emit-as :js
             '[{:# [hello-world]}])
  => "{hello_world}")

^{:refer std.lang.model.spec-js/js-set :added "4.0"}
(fact "emits a js set"
  ^:hidden

  (js-set '#{...x y {:a 1 :b 2}} +grammar+ {})
  => "{...x,y,\"a\":1,\"b\":2}"
  
  (js-set '#{...x y {a 1 :b 2}} +grammar+ {})
  => "{...x,y,[a]:1,\"b\":2}"

  (js-set '#{[...x y (% a) 1 :b 2]} +grammar+ {})
  => "(tab ...x y (% a) 1 :b 2)")

^{:refer std.lang.model.spec-js/js-defclass :added "4.0"}
(fact "creates a defclass function"
  ^:hidden
  
  (std.lang/emit-as
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
  => (std.string/|
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

^{:refer std.lang.model.spec-js/tf-var-let :added "4.0"}
(fact "outputs the let keyword"
  ^:hidden
  
  (tf-var-let '(var a 1))
  => '(var* :let a := 1))

^{:refer std.lang.model.spec-js/tf-var-const :added "4.0"}
(fact "outputs the const keyword"
  ^:hidden
  
  (tf-var-const '(const a 1))
  => '(var* :const a := 1))

^{:refer std.lang.model.spec-js/tf-for-object :added "4.0"}
(fact "custom for:object code"

  (tf-for-object '(for:object [[k v] {:a 1}]
                              [k v]))
  => '(for [(var* :let [k v]) :of (Object.entries {:a 1})] [k v]))

^{:refer std.lang.model.spec-js/tf-for-array :added "4.0"}
(fact "custom for:array code"

  (tf-for-array '(for:array [e [1 2 3 4 5]]
                             [k v]))
  => '(for [(var* :let e) :of (% [1 2 3 4 5])] [k v])

  (tf-for-array '(for:array [[i e] arr]
                            [k v]))
  => '(for [(var* :let i := 0) (< i (. arr length))
            (:++ i)] (var* :let e (. arr [i])) [k v]))

^{:refer std.lang.model.spec-js/tf-for-iter :added "4.0"}
(fact "custom for:iter code"
  
  (tf-for-iter '(for:iter [e iter]
                          e))
  => '(for [(var* :let e) :of (% iter)] e))

^{:refer std.lang.model.spec-js/tf-for-return :added "4.0"}
(fact "for return transform"
  ^:hidden
  
  (tf-for-return '(for:return [[ok err] (call (x:callback))]
                              {:success (return ok)
                               :error   (return err)}))
  => '(call (fn [err ok]
              (if err
                (return err)
                (return ok)))))

^{:refer std.lang.model.spec-js/tf-for-try :added "4.0"}
(fact "for try transform"
  ^:hidden

  (tf-for-try '(for:try [[ok err] (call (x:callback))]
                        {:success (return ok)
                         :error   (return err)}))
  => '(try (var ok := (call (x:callback)))
           (return ok)
           (catch err (return err))))

^{:refer std.lang.model.spec-js/tf-for-async :added "4.0"}
(fact  "for async transform"
  ^:hidden

  (tf-for-async '(for:async [[ok err] (call (x:callback))]
                            {:success (return ok)
                             :error   (return err)
                             :finally (return true)}))
  => '(. (new Promise (fn [resolve reject]
                        (resolve (call (x:callback)))))
         (then (fn [ok] (return ok)))
         (catch (fn [err] (return err)))
         (finally (fn [] (return true)))))
