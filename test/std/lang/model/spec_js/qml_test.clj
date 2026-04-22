(ns std.lang.model.spec-js.qml-test
  (:require [std.lang :as l]
            [std.lang.model.spec-js :as js]
            [std.lang.model.spec-js.qml :as qml]
            [std.string.prose :as prose])
  (:use code.test))

^{:refer std.lang.model.spec-js.qml/qml-props? :added "4.0"}
(fact "checks if data is a qml prop"

  (qml/qml-props? #{[:hello 1]})
  => true

  (qml/qml-props? {:a 1 :b 2})
  => true

  (qml/qml-props? [])
  => false)

^{:refer std.lang.model.spec-js.qml/qml-container? :added "4.0"}
(fact "checks if item is a qml container"

  (qml/qml-container? [:qml/Item #{}])
  => true

  (qml/qml-container? 1)
  => false)

^{:refer std.lang.model.spec-js.qml/classify-props :added "4.0"}
(fact "classifies props"

  (qml/classify-props #{[:a 1 :b [:qml/Item]]}
                      qml/classify)
  => [[:a {:type :qml/value, :value 1}]
      [:b {:type :qml/container, :title "Item", :props [], :children []}]])

^{:refer std.lang.model.spec-js.qml/classify-container :added "4.0"}
(fact "classifies a container"

  (qml/classify-container
   [:qml/Window #{[:a 1 :b [:qml/Item]]}
    '(fn hello [] (+ 1 2))]
   qml/classify)
  => '{:type :qml/container,
       :title "Window",
       :props
       [[:a {:type :qml/value, :value 1}]
        [:b
         {:type :qml/container,
          :title "Item",
          :props [],
          :children []}]],
       :children [{:type :qml/value, :value (fn hello [] (+ 1 2))}]})

^{:refer std.lang.model.spec-js.qml/classify :added "4.0"}
(fact "classifies the data structure"

  (qml/classify
   '[:qml/Item
     #{[:width  bg.width
        :height bg.height]}
     [:qml/Image
      {:id bg
       :source "assets/background.png"}]
     [:qml/MouseAria
      #{[:id bg
         :onClicked (% (:= circle.x 84)
                        (:= box.rotation 0))]}

      (var :int colorIndex := (j/floor (* 3 (j/random))))
      (var someList := [1 2 3 4])

      (fn _test_transformed []
        (:+= circle.x 20))

      (fn _test_overlap []
        (:+= circle.x 20))]])
  => '{:type :qml/container,
       :title "Item",
       :props
       [[:width {:type :qml/value, :value bg.width}]
        [:height {:type :qml/value, :value bg.height}]],
       :children
       [{:type :qml/container,
         :title "Image",
         :props
         [[:id {:type :qml/value, :value bg}]
          [:source {:type :qml/value, :value "assets/background.png"}]],
         :children []}
        {:type :qml/container,
         :title "MouseAria",
         :props
         [[:id {:type :qml/value, :value bg}]
          [:onClicked
           {:type :qml/value,
            :value (% (:= circle.x 84) (:= box.rotation 0))}]],
         :children
         [{:type :qml/value,
           :value (var :int colorIndex := (j/floor (* 3 (j/random))))}
          {:type :qml/value, :value (var someList := [1 2 3 4])}
          {:type :qml/value,
           :value (fn _test_transformed [] (:+= circle.x 20))}
          {:type :qml/value,
           :value (fn _test_overlap [] (:+= circle.x 20))}]}]})

^{:refer std.lang.model.spec-js.qml/emit-value :added "4.0"}
(fact "emits a value string"

  (l/with:emit
   (qml/emit-value '{:type :qml/value,
                     :value (% (:= circle.x 84)
                               (:= box.rotation 0))}
                   js/+grammar+
                   {}))
  => " {\n  circle.x = 84;\n  box.rotation = 0;\n}")

^{:refer std.lang.model.spec-js.qml/emit-container :added "4.0"}
(fact "emits a container string"

  (l/with:emit
   (qml/emit-container '{:type :qml/container,
                         :title "MouseAria",
                         :props
                         [[:id {:type :qml/value, :value bg}]
                          [:onClicked
                           {:type :qml/value,
                            :value (% (:= circle.x 84) (:= box.rotation 0))}]],
                         :children
                         [{:type :qml/value,
                           :value (var :int colorIndex := (Math.floor (* 3 (Math.random))))}
                          {:type :qml/value, :value (var someList := [1 2 3 4])}
                          {:type :qml/value,
                           :value (fn _test_transformed [] (:+= circle.x 20))}
                          {:type :qml/value,
                           :value (fn _test_overlap [] (:+= circle.x 20))}]}
                       js/+grammar+
                       {}))
  => (prose/|
      "MouseAria {"
      "  id: bg"
      "  onClicked:  {"
      "    circle.x = 84;"
      "    box.rotation = 0;"
      "  }"
      "  property int colorIndex : Math.floor(3 * Math.random())"
      "  property someList : [1,2,3,4]"
      "  _test_transformed() {"
      "    circle.x += 20;"
      "  }"
      "  _test_overlap() {"
      "    circle.x += 20;"
      "  }"
      "}"))

^{:refer std.lang.model.spec-js.qml/emit-node :added "4.0"}
(fact "emits either container or value string"

  (l/with:emit
   (qml/emit-node '{:type :qml/container,
                    :title "Item",
                    :props
                    [[:width {:type :qml/value, :value bg.width}]
                     [:height {:type :qml/value, :value bg.height}]],
                    :children
                    [{:type :qml/container,
                      :title "Image",
                      :props
                      [[:id {:type :qml/value, :value bg}]
                       [:source {:type :qml/value, :value "assets/background.png"}]],
                      :children []}
                     {:type :qml/container,
                      :title "MouseAria",
                      :props
                      [[:id {:type :qml/value, :value bg}]
                       [:onClicked
                        {:type :qml/value,
                         :value (% (:= circle.x 84) (:= box.rotation 0))}]],
                      :children
                      [{:type :qml/value,
                        :value (var :int colorIndex := (Math.floor (* 3 (Math.random))))}
                       {:type :qml/value, :value (var someList := [1 2 3 4])}
                       {:type :qml/value,
                        :value (fn _test_transformed [] (:+= circle.x 20))}
                       {:type :qml/value,
                        :value (fn _test_overlap [] (:+= circle.x 20))}]}]}
                  js/+grammar+
                  {}))
  => (prose/|
      "Item {"
      "  width: bg.width"
      "  height: bg.height"
      "  Image {"
      "    id: bg"
      "    source: \"assets/background.png\""
      "  }"
      "  MouseAria {"
      "    id: bg"
      "    onClicked:  {"
      "      circle.x = 84;"
      "      box.rotation = 0;"
      "    }"
      "    property int colorIndex : Math.floor(3 * Math.random())"
      "    property someList : [1,2,3,4]"
      "    _test_transformed() {"
      "      circle.x += 20;"
      "    }"
      "    _test_overlap() {"
      "      circle.x += 20;"
      "    }"
      "  }"
      "}"))

^{:refer std.lang.model.spec-js.qml/emit-qml :added "4.0"}
(fact "emits a qml string"

  (l/with:emit
   (qml/emit-qml [:qml/Window #{[:a 1 :b [:qml/Item]]}
                  '(fn hello [] (+ 1 2))]
                 js/+grammar+
                 {}))
  (prose/|
   "Window {"
   "  a: 1"
   "  b: Item {"
   "    "
   "  }"
   "  hello(){"
   "    1 + 2;"
   "  }"
   "}"))
