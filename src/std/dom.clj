(ns std.dom
  (:require [std.dom.common :as common]
            [std.dom.component :as component]
            [std.dom.diff :as diff]
            [std.dom.event :as event]
            [std.dom.find :as find]
            [std.dom.impl :as impl]
            [std.dom.invoke :as invoke]
            [std.dom.item :as item]
            [std.dom.local :as local]
            [std.dom.react :as react]
            [std.dom.type :as type]
            [std.dom.update :as update]
            [std.lib.foundation]))

(std.lib.foundation/intern-in common/dom-attach
             common/dom-children
             common/dom-compile
             common/dom-create
             common/dom-detach
             common/dom-item
             common/dom-item?
             common/dom-equal?
             common/dom-metaclass
             common/dom-metaprops
             common/dom-metatype
             common/dom-split-props
             common/dom-top
             common/dom-trigger
             common/dom-vector?
             common/dom?
             common/dom-assert

             component/defcomp
             component/dom-state-handler
             
             diff/dom-diff
             diff/dom-ops

             event/event-handler
             event/handle-event

             find/dom-match?
             find/dom-find
             find/dom-find-all
             
             impl/dom-init
             impl/dom-remove
             impl/dom-render
             impl/dom-rendered
             impl/dom-replace

             item/item-constructor
             item/item-setters
             item/item-getters
             item/item-access
             item/item-create
             item/item-props-set
             item/item-props-delete
             item/item-update
             item/item-cleanup

             local/local
             local/local-dom
             local/local-dom-state
             local/local-parent
             local/local-parent-state
             local/dom-send-local
             local/dom-set-local
             
             react/react
             react/dom-set-state
             
             type/metaclass
             type/metaclass-add
             type/metaclass-remove
             type/metaprops
             type/metaprops-add
             type/metaprops-remove
             type/metaprops-install
             
             update/dom-apply
             update/dom-update)
