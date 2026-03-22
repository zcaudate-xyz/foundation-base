(ns std.dom.find-test
  (:require [std.dom :as dom]
            [std.dom.find :refer :all]
            [std.dom.mock])
  (:use code.test))

^{:refer std.dom.find/dom-match? :added "3.0"}
(fact "tests dom to match on either function or value"
  
  (dom-match? (dom/dom-compile [:mock/pane {:hello "world"}])
              :hello
              string?)
  => true)

^{:refer std.dom.find/dom-find-props :added "3.0"}
(fact "find dom element within props"

  (-> (dom/dom-compile [:mock/pane {:child [:mock/pane {:tag "A"}]}])
      :props
      (dom-find-props :tag "A")
      str read-string)
  => [:- :mock/pane {:tag "A"}])

^{:refer std.dom.find/dom-find :added "3.0"}
(fact "find dom element"

  (-> (dom/dom-compile [:mock/pane {:children [[:mock/pane {:tag "A"}]
                                               [:mock/pane {:tag "B"}]]}])
      (dom-find :tag identity)
      str read-string)
  => [:- :mock/pane {:tag "A"}]^:hidden

  (-> (dom/dom-compile [:mock/pane {:children [[:mock/pane {:tag "A"}]
                                               [:mock/pane {:tag "B"}]]}])
      (dom-find :tag "B")
      str read-string)
  => [:- :mock/pane {:tag "B"}])

^{:refer std.dom.find/dom-find-all-props :added "3.0"}
(fact "finds all dom elements within props"

  (-> (dom/dom-compile [:mock/pane {:children [[:mock/pane {:tag "A"}]
                                               [:mock/pane {:tag "B"}]]}])
      :props
      (dom-find-all-props :tag string? (atom []))
      str read-string)
  => [[:- :mock/pane {:tag "A"}] [:- :mock/pane {:tag "B"}]])

^{:refer std.dom.find/dom-find-all :added "3.0"}
(fact "finds all matching dom elements"

  (-> (dom/dom-compile [:mock/pane {:children [[:mock/pane {:tag "A"}]
                                               [:mock/pane {:tag "B"}]]}])
      (dom-find-all :tag string?)
      str read-string)
  => [[:- :mock/pane {:tag "A"}] [:- :mock/pane {:tag "B"}]])


(comment

  (def -content-
        [{:uuid "4cd6b1b0-2319-49b3-bb85-72105f161ba3"
          :type :doc/markdown
          :text "hello world"}
         {:uuid "3ab2f5d6-2599-43aa-84dd-dc99010309e7"
          :type :doc/markdown
          :text "foo bar baz"}
         {:uuid "bc4939b4-067a-4648-a85d-a02802b7cab8"
          :type :doc/markdown
          :text "start again, start again"}])
  
  (def -data-
    {:title    "hello"
     :subtitle "world"
     :date     "18 March 2019"
     :order    (mapv :uuid -content-)
     :content  (zipmap (mapv :uuid -content-) -content-)})
  
  (def -document- (atom -data-))

  (def -dom- (-> (base/dom-compile [:dian/editor-page {:document -document-}])
                 (impl/dom-init)))
  
  (time (dom-find-all -dom-
                      :state
                      container.registry.h/atom?))
  
  (time (dom-find -dom-
                  :class
                  ["editor-container"]))
  
  (-> (base/dom-compile [:dian/editor-page {:document (atom {})}])
      (impl/dom-init)
      :shadow)
  
  
  

  )
