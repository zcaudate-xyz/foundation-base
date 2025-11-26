(ns std.block
  (:require [std.block.base :as base]
            [std.block.construct :as construct]
            [std.block.grid :as grid]
            [std.block.parse :as parse]
            [std.block.type :as type]
            [std.block.highlight :as highlight]
            [std.block.layout :as layout]
            [std.lib :as h])
  (:refer-clojure :exclude [type next replace string newline comment]))

(h/intern-in  base/block?
              base/expression?
              [type           base/block-type]
              [tag            base/block-tag]
              [string         base/block-string]
              [length         base/block-length]
              [width          base/block-width]
              [height         base/block-height]
              [prefixed       base/block-prefixed]
              [suffixed       base/block-suffixed]
              [verify         base/block-verify]
              [value          base/block-value]
              [value-string   base/block-value-string]
              [children       base/block-children]
              [info           base/block-info]

              construct/block
              construct/void
              construct/space
              construct/spaces
              construct/newline
              construct/newlines
              construct/tab
              construct/tabs
              construct/comment
              construct/uneval
              construct/cursor
              construct/contents
              construct/container
              construct/root
              construct/max-width

              parse/parse-string
              parse/parse-root
              parse/parse-first

              [layout layout/layout-main]
              
              [void?      type/void-block?]
              [code?      type/code-block?]
              [space?     type/space-block?]
              [linebreak? type/linebreak-block?]
              [linespace? type/linespace-block?]
              [eof?       type/eof-block?]
              [comment?   type/comment-block?]
              [token?     type/token-block?]
              [container? type/container-block?]
              [modifier?  type/modifier-block?])
