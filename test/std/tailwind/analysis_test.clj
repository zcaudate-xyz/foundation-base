(ns std.tailwind.analysis-test
  (:require [std.tailwind.analysis :refer :all]
            [std.lib :as h]
            [std.string :as str]
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
