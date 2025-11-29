(ns std.tailwind.analysis-test
  (:require [std.tailwind.analysis :refer :all]
            [std.lib :as h]
            [std.string :as str]
            [std.lang.base.book :as book]
            [code.test :refer :all]))

(fact "find-returns"
  (find-returns '(defn.js Foo [] (return (div "hello"))))
  => '((div "hello")))

(fact "to-hiccup"
  (to-hiccup '(div {:className "foo"} "bar"))
  => [:div {:class "foo"} "bar"]

  (to-hiccup '(T/Stack {:className "p-2"} (T/Text "Hello")))
  => [:T/Stack {:class "p-2"} [:T/Text {} "Hello"]])

(fact "estimate-layout"
  (let [code '(defn.js MyComp []
                (return (div {:className "w-10 h-10 border"} "Hi")))
        [layout] (estimate-layout code)]
    (str/includes? layout "+--------+") => true
    (str/includes? layout "|Hi      |") => true))

(def +mock-book+
  {:modules
   {'my.module {:link {'T 'js.tamagui} :id 'my.module}
    'js.tamagui {:code {'Stack {:form '(defn.js Stack [] (return (div {:class "w-5 h-5 border"} "S")))}}
                 :id 'js.tamagui}}})

(fact "resolve-symbol"
  (with-redefs [book/get-module (fn [b id] (get-in b [:modules id]))]
    (resolve-symbol +mock-book+ 'my.module 'T/Stack)
    => 'js.tamagui/Stack
    (resolve-symbol +mock-book+ 'my.module 'Local)
    => 'my.module/Local))

(fact "estimate-layout with dependencies"
  (with-redefs [book/get-module (fn [b id] (get-in b [:modules id]))
                book/get-code-entry (fn [b id] (get-in b [:modules (symbol (namespace id)) :code (symbol (name id))]))]

    (let [code '(defn.js MyComp []
                  (return (T/Stack {:class "w-10 h-10 border"} "C")))
          [layout] (estimate-layout code {:module 'my.module :book +mock-book+})]
      (str/includes? layout "+--------+") => true
      (str/includes? layout "+---+") => true
      (str/includes? layout "|S  |") => true)))


^{:refer std.tailwind.analysis/find-returns :added "4.0"}
(fact "finds return statements in AST"
  (find-returns '(fn [] (return 1))) => '(1)
  (find-returns '(do (return 1) (return 2))) => '(1 2))

^{:refer std.tailwind.analysis/normalize-props :added "4.0"}
(fact "normalizes className to class"
  (normalize-props {:className "foo"}) => {:class "foo"}
  (normalize-props {:class "bar"}) => {:class "bar"}
  (normalize-props {:id "baz"}) => {:id "baz"})

^{:refer std.tailwind.analysis/get-book :added "4.0"}
(fact "gets the language book"
  (get-book :js) => (any nil? map?))

^{:refer std.tailwind.analysis/resolve-symbol :added "4.0"}
(fact "resolves symbol in module context"
  (resolve-symbol nil nil 'foo) => 'foo)

^{:refer std.tailwind.analysis/fetch-component-form :added "4.0"}
(fact "fetches component form from book"
  (fetch-component-form nil 'foo) => nil)

^{:refer std.tailwind.analysis/to-hiccup :added "4.0"}
(fact "converts dsl to hiccup"
  (to-hiccup [:div "hi"]) => [:div "hi"]
  (to-hiccup '(span "ho")) => [:span {} "ho"])

^{:refer std.tailwind.analysis/estimate-layout :added "4.0"}
(fact "estimates layout from code"
  (estimate-layout '(return (div "hi"))) => (any seq?))
