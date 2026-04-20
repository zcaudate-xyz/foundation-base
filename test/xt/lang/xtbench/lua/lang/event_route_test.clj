(ns
 xtbench.lua.lang.event-route-test
 (:require
  [std.json :as json]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic,
  :require
  [[xt.lang.common-lib :as k]
   [xt.lang.common-data :as xtd]
   [xt.lang.common-spec :as xt]
   [xt.lang.event-route :as route]
   [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.event-route/interim-from-url, :added "4.0"}
(fact
 "creates interim from url"
 ^{:hidden true}
 (!.lua
  [(route/interim-from-url "hello/world?id=1&type=name")
   (route/interim-from-url "?id=1")
   (route/interim-from-url "hello?")])
 =>
 [{"params" {"[\"hello\",\"world\"]" {"id" "1", "type" "name"}},
   "path" ["hello" "world"]}
  {"params" {"{}" {"id" "1"}}, "path" {}}
  {"params" {}, "path" ["hello"]}])

^{:refer xt.lang.event-route/interim-to-url,
  :added "4.0",
  :setup
  [(def
    +out+
    [{"params" {"id" "1", "type" "name"}, "path" ["hello" "world"]}
     "?id=1"
     "hello"])]}
(fact
 "creates url from interim"
 ^{:hidden true}
 (!.lua
  [(route/interim-to-url
    {"params" {(xt/x:json-encode ["hello" "world"]) {"type" "name"}},
     "path" ["hello" "world"]})
   (route/interim-to-url {"params" {"{}" {"id" "1"}}, "path" []})
   (route/interim-to-url {"params" {}, "path" ["hello"]})])
 =>
 ["hello/world?type=name" "?id=1" "hello"])

^{:refer xt.lang.event-route/path-to-tree, :added "4.0"}
(fact
 "turns a path to tree"
 ^{:hidden true}
 (!.lua (route/path-to-tree ["hello" "world"]))
 =>
 {"{}" "hello", "[\"hello\"]" "world"})

^{:refer xt.lang.event-route/interim-to-tree,
  :added "4.0",
  :setup
  [(def
    +out+
    [{"params" {"[]" {"id" "1", "type" "name"}},
      "[]" "hello",
      "[\"hello\"]" "world"}
     {"params" {"[]" {"id" "1", "type" "name"}},
      "[]" "hello",
      "[\"hello\"]" "world"}])]}
(fact
 "converts interim to tree"
 ^{:hidden true}
 (!.lua
  [(route/interim-to-tree
    {"params" {"{}" {"id" "1", "type" "name"}},
     "path" ["hello" "world"]})
   (route/interim-to-tree
    {"params" {"{}" {"id" "1", "type" "name"}},
     "path" ["hello" "world"]}
    true)])
 =>
 [{"params" {"{}" {"id" "1", "type" "name"}},
   "{}" "hello",
   "[\"hello\"]" "world"}
  {"params" {"{}" {"id" "1", "type" "name"}},
   "{}" "hello",
   "[\"hello\"]" "world"}])

^{:refer xt.lang.event-route/path-from-tree, :added "4.0"}
(fact
 "gets the path from tree"
 ^{:hidden true}
 (!.lua (route/path-from-tree {"{}" "hello", "[\"hello\"]" "world"}))
 =>
 ["hello" "world"])

^{:refer xt.lang.event-route/interim-from-tree, :added "4.0"}
(fact
 "converts interim from tree"
 ^{:hidden true}
 (!.lua
  (route/interim-from-tree
   {"params" {"{}" {"id" "1", "type" "name"}},
    "{}" "hello",
    "[\"hello\"]" "world"}))
 =>
 {"params" {"{}" {"id" "1", "type" "name"}}, "path" ["hello" "world"]})

^{:refer xt.lang.event-route/changed-params-raw, :added "4.0"}
(fact
 "checks for changed params"
 ^{:hidden true}
 (!.lua
  [(route/changed-params-raw
    {"id" "1", "type" "name"}
    {"id" "1", "type" "hello"})
   (route/changed-params-raw
    {"id" "1", "type" "name"}
    {"type" "hello"})])
 =>
 [{"type" true} {"id" true, "type" true}])

^{:refer xt.lang.event-route/changed-params, :added "4.0"}
(fact
 "gets diff between params"
 ^{:hidden true}
 (!.lua
  [(route/changed-params
    {:params {"{}" {"id" "1", "type" "name"}}}
    {:params {"{}" {"id" "1", "type" "hello"}}})
   (route/changed-params
    {:params {"{}" {"id" "1", "type" "name"}}}
    {:params {"{}" {"type" "hello"}}})])
 =>
 [{"type" true} {"id" true, "type" true}])

^{:refer xt.lang.event-route/changed-path-raw, :added "4.0"}
(fact
 "checks that path has changed"
 ^{:hidden true}
 (!.lua
  [(route/changed-path-raw ["hello" "world"] ["hello"])
   (route/changed-path-raw ["hello"] ["hello" "world"])
   (route/changed-path-raw ["hello" "world"] ["hello" "again"])])
 =>
 [{} {"[\"hello\"]" true} {"[\"hello\"]" true}])

^{:refer xt.lang.event-route/changed-path, :added "4.0"}
(fact
 "gets changed routes"
 ^{:hidden true}
 (!.lua
  [(route/changed-path
    {"{}" "hello", "[\"hello\"]" "world"}
    {"{}" "hello", "[\"hello\"]" "foo"})
   (route/changed-path
    {"{}" "hello", "[\"hello\"]" "world"}
    {"{}" "world"})])
 =>
 [{"[\"hello\"]" true} {"{}" true}])

^{:refer xt.lang.event-route/get-url, :added "4.0"}
(fact
 "gets the url for the route"
 ^{:hidden true}
 (!.lua
  [(route/get-url (route/make-route "hello"))
   (route/get-url (route/make-route "hello?id=1"))
   (route/get-url (route/make-route "?id=1"))
   (route/get-url (route/make-route "hello?name=2"))])
 =>
 ["hello" "hello?id=1" "?id=1" "hello?name=2"])

^{:refer xt.lang.event-route/get-segment, :added "4.0"}
(fact
 "gets the value for a segment segment"
 ^{:hidden true}
 (!.lua
  [(route/get-segment (route/make-route "hello") [])
   (route/get-segment (route/make-route "hello/a") ["hello"])
   (route/get-segment (route/make-route "hello/a/b/c") ["hello" "a"])
   (route/get-segment (route/make-route "hello/a") ["wrong"])
   (route/get-segment (route/make-route "hello") ["hello"])])
 =>
 ["hello" "a" "b"])

^{:refer xt.lang.event-route/get-param, :added "4.0"}
(fact
 "gets the param value"
 ^{:hidden true}
 (!.lua
  (route/get-param (route/make-route "hello?auth=sign_in") "auth"))
 =>
 "sign_in")

^{:refer xt.lang.event-route/get-all-params, :added "4.0"}
(fact
 "gets all params in the route"
 ^{:hidden true}
 (!.lua
  (route/get-all-params (route/make-route "hello?a=1&b=2") ["hello"]))
 =>
 {"a" "1", "b" "2"})

^{:refer xt.lang.event-route/make-route, :added "4.0"}
(fact
 "makes a route"
 ^{:hidden true}
 (!.lua (route/make-route "hello"))
 =>
 {"::" "event.route",
  "tree" {"params" {}, "{}" "hello"},
  "history" {},
  "listeners" {}})

^{:refer xt.lang.event-route/add-url-listener,
  :added "4.0",
  :setup
  [(def
    +out+
    {"callback" "<function>",
     "pred" "<function>",
     "meta" {"listener/id" "a1", "listener/type" "route.url"}})]}
(fact
 "adds a url listener"
 ^{:hidden true}
 (!.lua
  (var r (route/make-route "hello"))
  (xtd/tree-get-data (route/add-url-listener r "a1" (fn:>))))
 =>
 +out+
 (!.lua
  (var r (route/make-route "hello"))
  (xtd/tree-get-data (route/add-url-listener r "a1" (fn:>))))
 =>
 +out+)

^{:refer xt.lang.event-route/add-path-listener,
  :added "4.0",
  :setup
  [(def
    +out+
    {"callback" "<function>",
     "pred" "<function>",
     "meta"
     {"listener/id" "a1",
      "route/path" [],
      "listener/type" "route.path"}})]}
(fact
 "adds a path listener"
 ^{:hidden true}
 (!.lua
  (var r (route/make-route "hello"))
  (xtd/tree-get-data (route/add-path-listener r [] "a1" (fn:>))))
 =>
 {"callback" "<function>",
  "pred" "<function>",
  "meta"
  {"listener/id" "a1", "route/path" {}, "listener/type" "route.path"}}
 (!.lua
  (var r (route/make-route "hello"))
  (xtd/tree-get-data (route/add-path-listener r [] "a1" (fn:>))))
 =>
 {"callback" "<function>",
  "pred" "<function>",
  "meta"
  {"listener/id" "a1", "route/path" {}, "listener/type" "route.path"}})

^{:refer xt.lang.event-route/add-param-listener,
  :added "4.0",
  :setup
  [(def
    +out+
    {"callback" "<function>",
     "pred" "<function>",
     "meta"
     {"listener/id" "a1",
      "route/param" "auth",
      "listener/type" "route.param"}})]}
(fact
 "adds a param listener"
 ^{:hidden true}
 (!.lua
  (var r (route/make-route "hello"))
  (xtd/tree-get-data (route/add-param-listener r "auth" "a1" (fn:>))))
 =>
 +out+)

^{:refer xt.lang.event-route/add-full-listener,
  :added "4.0",
  :setup
  [(def
    +out+
    {"callback" "<function>",
     "pred" "<function>",
     "meta"
     {"listener/id" "a1",
      "route/param" "auth",
      "route/path" ["hello"],
      "listener/type" "route.full"}})]}
(fact
 "adds a full listener"
 ^{:hidden true}
 (!.lua
  (var r (route/make-route "hello"))
  (xtd/tree-get-data
   (route/add-full-listener r ["hello"] "auth" "a1" (fn:>))))
 =>
 +out+)

^{:refer xt.lang.event-route/set-url,
  :added "4.0",
  :setup
  [(def
    +out+
    {"params" {},
     "path" {"[\"hello\"]" true},
     "type" "route.url",
     "meta"
     {"listener/id" "a1",
      "route/path" ["hello"],
      "listener/type" "route.path"}})]}
(fact
 "sets the url for a route"
 ^{:hidden true}
 [(notify/wait-on
   :lua
   (var r (route/make-route "hello"))
   (route/add-path-listener r ["hello"] "a1" (repl/>notify))
   (route/set-url r "hello/world"))]
 [{"params" {},
   "path" {"[\"hello\"]" true},
   "type" "route.url",
   "meta"
   {"listener/id" "a1",
    "route/path" ["hello"],
    "listener/type" "route.path"}}]
 (notify/wait-on
  :lua
  (var r (route/make-route "hello"))
  (route/add-path-listener r ["hello"] "a1" (repl/>notify))
  (route/set-url r "hello/world"))
 =>
 +out+)

^{:refer xt.lang.event-route/set-path,
  :added "4.0",
  :setup
  [(def
    +out+
    {"params" {},
     "path" {"[\"hello\"]" true},
     "type" "route.path",
     "meta"
     {"listener/id" "a1",
      "route/path" ["hello"],
      "listener/type" "route.path"}})]}
(fact
 "sets the path and param"
 ^{:hidden true}
 (notify/wait-on
  :lua
  (var r (route/make-route "hello"))
  (route/add-path-listener r ["hello"] "a1" (repl/>notify))
  (route/set-path r ["hello" "world"] nil))
 =>
 +out+)

^{:refer xt.lang.event-route/set-segment,
  :added "4.0",
  :setup
  [(def
    +out+
    {"params" {},
     "path" {"[\"hello\"]" true},
     "type" "route.path",
     "meta"
     {"listener/id" "a1",
      "route/path" ["hello"],
      "listener/type" "route.path"}})]}
(fact
 "sets the current segment"
 ^{:hidden true}
 (notify/wait-on
  :lua
  (var r (route/make-route "hello"))
  (route/add-path-listener r ["hello"] "a1" (repl/>notify))
  (route/set-segment r ["hello"] "world"))
 =>
 +out+)

^{:refer xt.lang.event-route/set-param,
  :added "4.0",
  :setup
  [(def
    +out+
    {"params" {"auth" true},
     "path" {},
     "type" "route.params",
     "meta"
     {"listener/id" "a1",
      "route/param" "auth",
      "listener/type" "route.param"}})]}
(fact
 "sets a param in a route"
 ^{:hidden true}
 (notify/wait-on
  :lua
  (var r (route/make-route "hello?auth=sign_in"))
  (route/add-param-listener r "auth" "a1" (repl/>notify))
  (route/set-param r "auth" "register" nil))
 =>
 +out+)

^{:refer xt.lang.event-route/reset-route, :added "4.0"}
(fact
 "resets the route, clearing all params"
 ^{:hidden true}
 (!.lua
  (var r (route/make-route "hello?auth=sign_in"))
  (route/reset-route r "world")
  (route/get-url r))
 =>
 "world")
