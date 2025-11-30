(ns std.block.editor-test
  (:use code.test)
  (:require [std.block.editor :refer :all]
            [std.lib.zip :as zip]
            [std.block.base :as base]
            [std.block.construct :as construct]
            [std.dom.common :as dom]))

^{:refer std.block.editor/create-editor :added "4.0"}
(fact "creates a new editor instance"
  (let [editor (create-editor "(+ 1 2)")]
    editor => (fn [x] (instance? std.block.editor.Editor x))
    (:zip editor) => zip/zipper?))

^{:refer std.block.editor/move-next :added "4.0"}
(fact "moves cursor to the next element"
  (let [editor (create-editor "(+ 1 2)")
        editor (move-in editor)  ;; Enter list. Points to `+`.
        editor (move-next editor)] ;; Move to ` ` (space).
    (base/block-string (zip/right-element (:zip editor))) => " "))

^{:refer std.block.editor/move-prev :added "4.0"}
(fact "moves cursor to the previous element"
  (let [editor (create-editor "(+ 1 2)")
        editor (move-in editor) ;; at `+`
        editor (move-next editor) ;; at ` `
        editor (move-prev editor)] ;; at `+`
    (base/block-string (zip/right-element (:zip editor))) => "+"))

^{:refer std.block.editor/move-in :added "4.0"}
(fact "moves cursor inside a container"
  (let [editor (create-editor "(+ 1 2)")
        editor (move-in editor)]
    (base/block-string (zip/right-element (:zip editor))) => "+"))

^{:refer std.block.editor/move-out :added "4.0"}
(fact "moves cursor out of a container"
  (let [editor (create-editor "(+ 1 2)")
        editor (move-in editor)
        editor (move-out editor)]
    (base/container? (zip/right-element (:zip editor))) => true))

^{:refer std.block.editor/wrap :added "4.0"}
(fact "wraps current element in a new container"
  (let [editor (create-editor "1")
        editor (wrap editor :list)]
    (base/block-string (zip/right-element (:zip editor))) => "(1)"))

^{:refer std.block.editor/insert-right :added "4.0"}
(fact "inserts a new element to the right"
  (let [editor (create-editor "(+ 1)")
        editor (move-in editor)
        editor (move-next editor)
        editor (move-next editor)
        editor (move-next editor)
        editor (insert-right editor 2)]
    (base/block-string (zip/root-element (:zip editor))) => "(+ 12)"))

^{:refer std.block.editor/block->dom :added "4.0"}
(fact "converts block to dom structure"
  (let [block (construct/block '(+ 1))
        dom (block->dom block [] [])]
    (dom/dom-metatype dom) => :dom/element
    (count (:children (dom/dom-children dom))) => 3))

^{:refer std.block.editor/get-zipper-path :added "4.0"}
(fact "gets current focus node"
  (let [editor (create-editor "1")]
    (base/block-string (get-zipper-path (:zip editor))) => "1"))

^{:refer std.block.editor/render-view :added "4.0"}
(fact "renders editor state to dom"
  (let [editor (create-editor "(+ 1)")
        view (render-view editor)]
    (dom/dom-metatype view) => :dom/element))
