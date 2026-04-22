(ns std.dom.impl-test
  (:require [std.dom.common :as base]
            [std.dom.impl :refer :all]
            [std.dom.item :as item]
            [std.dom.mock :as mock])
  (:use code.test))

^{:refer std.dom.impl/dom-render :added "3.0"}
(fact "enables rendering of dom ui"

  (-> (base/dom-create :mock/pane {} ["hello"])
      (dom-render)
      (base/dom-format))
  => [:+ :mock/pane "hello"]

  (-> (base/dom-create :mock/pane {:dom/init {:children ["hello"]}})
      (dom-render)
      (base/dom-item)
      (mock/mock-format))
  => [:mock/pane "hello"])

^{:refer std.dom.impl/dom-render-default :added "3.0"}
(fact "default implementation of dom-render. throws exception"

  (-> (base/dom-create :mock/pane {} ["hello"])
      (dom-render-default)
      (base/dom-item))
  => mock/mock?)

^{:refer std.dom.impl/dom-init :added "3.0"}
(fact "renders the dom element if input is dom and not rendered"

  (dom-init 1) => 1

  (base/dom-format (dom-init (base/dom-create :mock/pane)))
  => [:+ :mock/pane])

^{:refer std.dom.impl/dom-rendered :added "3.0"}
(fact "renders the dom and returns the actual element"

  (dom-rendered [:mock/pane])
  => mock/mock?)

^{:refer std.dom.impl/dom-remove :added "3.0"}
(fact "provides an extensible interface removing rendered elem from dom"

  (-> (base/dom-create :mock/pane {} ["hello"])
      (dom-render)
      (dom-remove)
      (base/dom-format))
  => [:- :mock/pane "hello"])

^{:refer std.dom.impl/dom-remove-default :added "3.0"}
(fact "default implementation of dom-remove."

  (-> (base/dom-create :mock/pane {} ["hello"])
      (dom-render)
      (dom-remove-default)
      (base/dom-format))
  => [:- :mock/pane "hello"])

^{:refer std.dom.impl/dom-replace :added "3.0"}
(fact "replaces one dom element with another"

  (-> (base/dom-create :mock/pane {} ["hello"])
      (dom-render)
      (dom-replace (base/dom-create :mock/pane {} ["world"]))
      (base/dom-item)
      :props)
  => {:children ["world"]})

^{:refer std.dom.impl/dom-replace-default :added "3.0"}
(fact "default implementation of dom-remove."

  (-> (base/dom-create :mock/pane {} ["hello"])
      (dom-render)
      (dom-replace-default (base/dom-create :mock/pane {} ["world"]))
      (base/dom-item)
      :props)
  => {:children ["world"]})
