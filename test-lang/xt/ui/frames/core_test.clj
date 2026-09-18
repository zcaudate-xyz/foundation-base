(ns xt.ui.frames.core-test
  (:use code.test)
  (:require [lang.core :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.ui.frames.core :as frame]]})

^{:refer xt.ui.frames.core/spec :added "4.1"}
(fact "frame specifications preserve identity and default regions and options"
  (!.js (frame/spec "users" "table" nil nil))
  => {"id" "users" "kind" "table" "regions" {} "opts" {}}
  (!.js (frame/spec "users" "table" {"toolbar" "custom"} {"title" "Users"}))
  => {"id" "users" "kind" "table" "regions" {"toolbar" "custom"} "opts" {"title" "Users"}})

^{:refer xt.ui.frames.core/region :added "4.1"}
(fact "region lookup returns supplied content or the explicit fallback"
  (!.js
   (var current (frame/spec "users" "table" {"toolbar" ["custom"] "empty" []} nil))
   [(frame/region current "toolbar" ["fallback"])
    (frame/region current "empty" ["fallback"])
    (frame/region current "missing" ["fallback"])])
  => [["custom"] [] ["fallback"]])

^{:refer xt.ui.frames.core/override :added "4.1"}
(fact "overrides merge regions into a deep copy without mutating the source frame"
  (!.js
   (var current (frame/spec "users" "table" {"toolbar" "old" "footer" "keep"} {"nested" {"title" "Users"}}))
   (var next (frame/override current {"toolbar" "new" "body" "content"}))
   (xt/x:set-key (. next ["opts"] ["nested"]) "title" "Changed")
   [current next (frame/override current nil)])
  => [{"id" "users" "kind" "table" "regions" {"toolbar" "old" "footer" "keep"} "opts" {"nested" {"title" "Users"}}}
      {"id" "users" "kind" "table" "regions" {"toolbar" "new" "footer" "keep" "body" "content"} "opts" {"nested" {"title" "Changed"}}}
      {"id" "users" "kind" "table" "regions" {"toolbar" "old" "footer" "keep"} "opts" {"nested" {"title" "Users"}}}])
