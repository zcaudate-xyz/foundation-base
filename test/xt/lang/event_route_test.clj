(ns xt.lang.event-route-test
  (:require [std.json :as json]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-spec :as xt]
             [xt.lang.event-route :as route]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-spec :as xt]
             [xt.lang.event-route :as route]
             [xt.lang.common-repl :as repl]]})

(l/script- :python
  {:runtime :basic
    :require [[xt.lang.common-lib :as k]
              [xt.lang.common-data :as xtd]
              [xt.lang.common-spec :as xt]
              [xt.lang.event-route :as route]
              [xt.lang.common-repl :as repl]]})

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-spec :as xt]
             [xt.lang.event-route :as route]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.event-route/interim-from-url :added "4.0"}
(fact "creates interim from url"

  (!.js
    [(route/interim-from-url "hello/world?id=1&type=name")
     (route/interim-from-url "?id=1")
     (route/interim-from-url "hello?")])
  => [{"params" {"[\"hello\",\"world\"]" {"id" "1", "type" "name"}},
       "path" ["hello" "world"]}
      {"params" {"[]" {"id" "1"}}, "path" []}
      {"params" {}, "path" ["hello"]}]


  (!.lua
   [(route/interim-from-url "hello/world?id=1&type=name")
    (route/interim-from-url "?id=1")
    (route/interim-from-url "hello?")])
  => [{"params" {"[\"hello\",\"world\"]" {"id" "1", "type" "name"}},
       "path" ["hello" "world"]}
      {"params" {"{}" {"id" "1"}}, "path" {}}
      {"params" {}, "path" ["hello"]}]

  ^{:lang-exceptions
    {:dart {:expect [{"params" {"[\"hello\",\"world\"]" {"id" "1", "type" "name"}},
                      "path" ["hello" "world"]}
                     {"params" {"[]" {"id" "1"}}, "path" []}
                     {"params" {}, "path" ["hello"]}]}}}
  (!.py
    [(route/interim-from-url "hello/world?id=1&type=name")
     (route/interim-from-url "?id=1")
     (route/interim-from-url "hello?")])
  => [{"params" {"[\"hello\", \"world\"]" {"id" "1", "type" "name"}},
       "path" ["hello" "world"]}
      {"params" {"[]" {"id" "1"}}, "path" []}
      {"params" {}, "path" ["hello"]}])

^{:refer xt.lang.event-route/interim-to-url :added "4.0"
  :setup [(def +out+
            [{"params" {"id" "1", "type" "name"}, "path" ["hello" "world"]}
             "?id=1"
             "hello"])]}
(fact "creates url from interim"

  (!.js
   [(route/interim-to-url {"params" {(xt/x:json-encode ["hello" "world"])
                                     {"id" "1", "type" "name"}}, "path" ["hello" "world"]})
    (route/interim-to-url {"params" {"[]" {"id" "1"}}, "path" []})
    (route/interim-to-url {"params" {}, "path" ["hello"]})])
  => ["hello/world?id=1&type=name" "?id=1" "hello"]

  (!.lua
   [(route/interim-to-url {"params" {(xt/x:json-encode ["hello" "world"])
                                     {"type" "name"}}, "path" ["hello" "world"]})
    (route/interim-to-url {"params" {"{}" {"id" "1"}}, "path" []})
    (route/interim-to-url {"params" {}, "path" ["hello"]})])
  => ["hello/world?type=name" "?id=1" "hello"]

  (!.py
   [(route/interim-to-url {"params" {(xt/x:json-encode ["hello" "world"])
                                     {"id" "1", "type" "name"}}, "path" ["hello" "world"]})
    (route/interim-to-url {"params" {"[]" {"id" "1"}}, "path" []})
    (route/interim-to-url {"params" {}, "path" ["hello"]})])
  => ["hello/world?id=1&type=name" "?id=1" "hello"])

^{:refer xt.lang.event-route/path-to-tree :added "4.0"}
(fact "turns a path to tree"

  ^{:lang-exceptions
    {:dart {:form (route/path-to-tree ["hello" "world"] false)}}}
  (!.js
   (route/path-to-tree ["hello" "world"]))
  => {"[]" "hello", "[\"hello\"]" "world"}

  (!.lua
   (route/path-to-tree ["hello" "world"]))
  => {"{}" "hello", "[\"hello\"]" "world"}

  (!.py
   (route/path-to-tree ["hello" "world"] false))
  => {"[]" "hello", "[\"hello\"]" "world"})

^{:refer xt.lang.event-route/interim-to-tree :added "4.0"
  :setup [(def +out+
            [{"params" {"[]" {"id" "1", "type" "name"}},
              "[]" "hello",
              "[\"hello\"]" "world"}
             {"params" {"[]" {"id" "1", "type" "name"}},
              "[]" "hello",
              "[\"hello\",\"world\"]" nil,
              "[\"hello\"]" "world"}])]}
(fact "converts interim to tree"

  ^{:lang-exceptions
    {:dart
     {:form [(route/interim-to-tree
              {"params" {"[]" {"id" "1", "type" "name"}}, "path" ["hello" "world"]}
              false)
             (route/interim-to-tree
              {"params" {"[]" {"id" "1", "type" "name"}}, "path" ["hello" "world"]}
              true)]
      :expect [{"params" {"[]" {"id" "1", "type" "name"}},
                "[]" "hello",
                "[\"hello\"]" "world"}
               {"params" {"[]" {"id" "1", "type" "name"}},
                "[]" "hello",
                "[\"hello\",\"world\"]" nil,
                "[\"hello\"]" "world"}]}}}
  (!.js
   [(route/interim-to-tree
     {"params" {"[]" {"id" "1", "type" "name"}}, "path" ["hello" "world"]})
    (route/interim-to-tree
     {"params" {"[]" {"id" "1", "type" "name"}}, "path" ["hello" "world"]}
     true)])
  => +out+

  (!.lua
   [(route/interim-to-tree
     {"params" {"{}" {"id" "1", "type" "name"}}, "path" ["hello" "world"]})
    (route/interim-to-tree
     {"params" {"{}" {"id" "1", "type" "name"}}, "path" ["hello" "world"]}
     true)])
  => [{"params" {"{}" {"id" "1", "type" "name"}},
       "{}" "hello",
       "[\"hello\"]" "world"}
      {"params" {"{}" {"id" "1", "type" "name"}},
       "{}" "hello",
       "[\"hello\"]" "world"}]

  ^{:lang-exceptions
    {:dart {:expect [{"params" {"[]" {"id" "1", "type" "name"}},
                      "[]" "hello",
                      "[\"hello\"]" "world"}
                     {"params" {"[]" {"id" "1", "type" "name"}},
                      "[]" "hello",
                      "[\"hello\",\"world\"]" nil,
                      "[\"hello\"]" "world"}]}}}
  (!.py
    [(route/interim-to-tree
      {"params" {"[]" {"id" "1", "type" "name"}}, "path" ["hello" "world"]}
      false)
    (route/interim-to-tree
     {"params" {"[]" {"id" "1", "type" "name"}}, "path" ["hello" "world"]}
     true)])
  => [{"params" {"[]" {"id" "1", "type" "name"}},
       "[]" "hello",
       "[\"hello\"]" "world"}
      {"params" {"[]" {"id" "1", "type" "name"}},
       "[]" "hello",
       "[\"hello\", \"world\"]" nil,
       "[\"hello\"]" "world"}])

^{:refer xt.lang.event-route/path-from-tree :added "4.0"}
(fact "gets the path from tree"

  (!.js
   (route/path-from-tree {"[]" "hello", "[\"hello\"]" "world"}))
  => ["hello" "world"]

  (!.lua
   (route/path-from-tree {"{}" "hello", "[\"hello\"]" "world"}))
  => ["hello" "world"]

  (!.py
   (route/path-from-tree {"[]" "hello", "[\"hello\"]" "world"}))
  => ["hello" "world"])

^{:refer xt.lang.event-route/path-params-from-tree :added "4.0"}
(fact "gets path params from tree")

^{:refer xt.lang.event-route/interim-from-tree :added "4.0"}
(fact "converts interim from tree"

  (!.js
   (route/interim-from-tree
    {"params" {"[]" {"id" "1", "type" "name"}}, "[]" "hello", "[\"hello\"]" "world"}))
  => {"params" {"[]" {"id" "1", "type" "name"}}, "path" ["hello" "world"]}

  (!.lua
   (route/interim-from-tree
    {"params" {"{}" {"id" "1", "type" "name"}}, "{}" "hello", "[\"hello\"]" "world"}))
  => {"params" {"{}" {"id" "1", "type" "name"}}, "path" ["hello" "world"]}

  (!.py
   (route/interim-from-tree
    {"params" {"[]" {"id" "1", "type" "name"}}, "[]" "hello", "[\"hello\"]" "world"}))
  => {"params" {"[]" {"id" "1", "type" "name"}}, "path" ["hello" "world"]})

^{:refer xt.lang.event-route/changed-params-raw :added "4.0"}
(fact "checks for changed params"

  (!.js
   [(route/changed-params-raw
     {"id" "1", "type" "name"}
     {"id" "1", "type" "hello"})
    (route/changed-params-raw
     {"id" "1", "type" "name"}
     {"type" "hello"})])
  => [{"type" true} {"id" true, "type" true}]

  (!.lua
   [(route/changed-params-raw
     {"id" "1", "type" "name"}
     {"id" "1", "type" "hello"})
    (route/changed-params-raw
     {"id" "1", "type" "name"}
     {"type" "hello"})])
  => [{"type" true} {"id" true, "type" true}]

  (!.py
   [(route/changed-params-raw
     {"id" "1", "type" "name"}
     {"id" "1", "type" "hello"})
    (route/changed-params-raw
     {"id" "1", "type" "name"}
     {"type" "hello"})])
  => [{"type" true} {"id" true, "type" true}])

^{:refer xt.lang.event-route/changed-params :added "4.0"}
(fact "gets diff between params"

  ^{:lang-exceptions
    {:dart
     {:form [(route/changed-params {:params {"[]" {"id" "1", "type" "name"}}}
                                   {:params {"[]" {"id" "1", "type" "hello"}}}
                                   [])
             (route/changed-params {:params {"[]" {"id" "1", "type" "name"}}}
                                   {:params {"[]" {"type" "hello"}}}
                                   [])]}}}
  (!.js
   [(route/changed-params {:params {"[]" {"id" "1", "type" "name"}}}
                          {:params {"[]" {"id" "1", "type" "hello"}}})
    (route/changed-params {:params {"[]" {"id" "1", "type" "name"}}}
                          {:params {"[]" {"type" "hello"}}})])
  => [{"type" true} {"id" true, "type" true}]

  (!.lua
   [(route/changed-params {:params {"{}" {"id" "1", "type" "name"}}}
                          {:params {"{}" {"id" "1", "type" "hello"}}})
    (route/changed-params {:params {"{}" {"id" "1", "type" "name"}}}
                          {:params {"{}" {"type" "hello"}}})])
  => [{"type" true} {"id" true, "type" true}]

  (!.py
   [(route/changed-params {:params {"[]" {"id" "1", "type" "name"}}}
                          {:params {"[]" {"id" "1", "type" "hello"}}}
                          [])
    (route/changed-params {:params {"[]" {"id" "1", "type" "name"}}}
                          {:params {"[]" {"type" "hello"}}}
                          [])])
  => [{"type" true} {"id" true, "type" true}])

^{:refer xt.lang.event-route/changed-path-raw :added "4.0"}
(fact "checks that path has changed"

  (!.js
   [(route/changed-path-raw
     ["hello" "world"]
     ["hello"])
    (route/changed-path-raw
     ["hello"]
     ["hello" "world"])
    (route/changed-path-raw
     ["hello" "world"]
     ["hello" "again"])])
  => [{} {"[\"hello\"]" true} {"[\"hello\"]" true}]

  (!.lua
   [(route/changed-path-raw
     ["hello" "world"]
     ["hello"])
    (route/changed-path-raw
     ["hello"]
     ["hello" "world"])
    (route/changed-path-raw
     ["hello" "world"]
     ["hello" "again"])])
  => [{} {"[\"hello\"]" true} {"[\"hello\"]" true}]

  (!.py
   [(route/changed-path-raw
     ["hello" "world"]
     ["hello"])
    (route/changed-path-raw
     ["hello" "world"]
     ["hello" "again"])])
  => [{} {"[\"hello\"]" true}])

^{:refer xt.lang.event-route/changed-path :added "4.0"}
(fact "gets changed routes"

  (!.js
   [(route/changed-path
     {"[]" "hello", "[\"hello\"]" "world"}
     {"[]" "hello", "[\"hello\"]" "foo"})
    (route/changed-path
     {"[]" "hello", "[\"hello\"]" "world"}
     {"[]" "world"})])
  => [{"[\"hello\"]" true} {"[]" true}]

  (!.lua
   [(route/changed-path
     {"{}" "hello", "[\"hello\"]" "world"}
     {"{}" "hello", "[\"hello\"]" "foo"})
    (route/changed-path
     {"{}" "hello", "[\"hello\"]" "world"}
     {"{}" "world"})])
  => [{"[\"hello\"]" true} {"{}" true}]

  (!.py
   [(route/changed-path
     {"[]" "hello", "[\"hello\"]" "world"}
     {"[]" "hello", "[\"hello\"]" "foo"})
    (route/changed-path
     {"[]" "hello", "[\"hello\"]" "world"}
     {"[]" "world"})])
  => [{"[\"hello\"]" true} {"[]" true}])

^{:refer xt.lang.event-route/get-url :added "4.0"}
(fact "gets the url for the route"

  (!.js
   [(route/get-url (route/make-route "hello"))
    (route/get-url (route/make-route "hello?id=1"))
    (route/get-url (route/make-route "?id=1"))
    (route/get-url (route/make-route "hello?name=2"))])
  => ["hello" "hello?id=1" "?id=1" "hello?name=2"]

  (!.lua
   [(route/get-url (route/make-route "hello"))
    (route/get-url (route/make-route "hello?id=1"))
    (route/get-url (route/make-route "?id=1"))
    (route/get-url (route/make-route "hello?name=2"))])
  => ["hello" "hello?id=1" "?id=1" "hello?name=2"]

  (!.py
   [(route/get-url (route/make-route "hello"))
    (route/get-url (route/make-route "hello?id=1"))
    (route/get-url (route/make-route "?id=1"))
    (route/get-url (route/make-route "hello?name=2"))])
  => ["hello" "hello?id=1" "?id=1" "hello?name=2"])

^{:refer xt.lang.event-route/get-segment :added "4.0"}
(fact "gets the value for a segment segment"

  (!.js
   [(route/get-segment (route/make-route "hello") [])
    (route/get-segment (route/make-route "hello/a") ["hello"])
    (route/get-segment (route/make-route "hello/a/b/c") ["hello" "a"])
    (route/get-segment (route/make-route "hello/a") ["wrong"])
    (route/get-segment (route/make-route "hello") ["hello"])])
  => ["hello" "a" "b" nil nil]

  (!.lua
   [(route/get-segment (route/make-route "hello") [])
    (route/get-segment (route/make-route "hello/a") ["hello"])
    (route/get-segment (route/make-route "hello/a/b/c") ["hello" "a"])
    (route/get-segment (route/make-route "hello/a") ["wrong"])
    (route/get-segment (route/make-route "hello") ["hello"])])
  => ["hello" "a" "b"]

  (!.py
   [(route/get-segment (route/make-route "hello") [])
    (route/get-segment (route/make-route "hello/a") ["hello"])
    (route/get-segment (route/make-route "hello/a/b/c") ["hello" "a"])
    (route/get-segment (route/make-route "hello/a") ["wrong"])
    (route/get-segment (route/make-route "hello") ["hello"])])
  => ["hello" "a" "b" nil nil])

^{:refer xt.lang.event-route/get-param :added "4.0"}
(fact "gets the param value"

  ^{:lang-exceptions
    {:dart
     {:form (route/get-param (route/make-route "hello?auth=sign_in")
                             "auth"
                             nil)}}}
  (!.js
   (route/get-param (route/make-route "hello?auth=sign_in")
                     "auth"))
  => "sign_in"

  (!.lua
   (route/get-param (route/make-route "hello?auth=sign_in")
                    "auth"))
  => "sign_in"

  (!.py
   (route/get-param (route/make-route "hello?auth=sign_in")
                    "auth"
                    nil))
  => "sign_in")

^{:refer xt.lang.event-route/get-all-params :added "4.0"}
(fact "gets all params in the route"

  (!.js
   (route/get-all-params
    (route/make-route "hello?a=1&b=2")
    ["hello"]))
  => {"a" "1", "b" "2"}

  (!.lua
   (route/get-all-params
    (route/make-route "hello?a=1&b=2")
    ["hello"]))
  => {"a" "1", "b" "2"}

  (!.py
   (route/get-all-params
    (route/make-route "hello?a=1&b=2")
    ["hello"]))
  => {"a" "1", "b" "2"})

^{:refer xt.lang.event-route/make-route :added "4.0"}
(fact "makes a route"

  (!.js
   (route/make-route "hello"))
  => {"::" "event.route", "tree" {"params" {}, "[]" "hello"}, "history" [], "listeners" {}}


  (!.lua
   (route/make-route "hello"))
  => {"::" "event.route", "tree" {"params" {}, "{}" "hello"}, "history" {}, "listeners" {}}

  (!.py
   (route/make-route "hello"))
  => {"::" "event.route", "tree" {"params" {}, "[]" "hello"}, "history" [], "listeners" {}})

^{:refer xt.lang.event-route/add-url-listener :added "4.0"
  :setup [(def +out+
            {"callback" "<function>",
             "pred" "<function>",
             "meta" {"listener/id" "a1",
                     "listener/type" "route.url"}})]}
(fact "adds a url listener"

  ^{:lang-exceptions
    {:dart
     {:form (do
              (var r (route/make-route "hello"))
              (xtd/tree-get-data (route/add-url-listener r "a1" (fn:>) nil)))}}}
  (!.js
   (var r (route/make-route "hello"))
   (var entry (route/add-url-listener r "a1" (fn:>) nil))
   (xtd/tree-get-data entry))
  => +out+

  ^{:lang-exceptions
    {:dart
     {:form (do
              (var r (route/make-route "hello"))
              (xtd/tree-get-data (route/add-url-listener r "a1" (fn:>) nil)))}}}
  (!.js
   (var r (route/make-route "hello"))
   (var entry (route/add-url-listener r "a1" (fn:>) nil))
   (xtd/tree-get-data entry))
  => +out+

  (!.py
   (var r (route/make-route "hello"))
   (var entry (route/add-url-listener r "a1" (fn:>) nil))
   (xtd/tree-get-data entry))
  => +out+)

^{:refer xt.lang.event-route/add-path-listener :added "4.0"
  :setup [(def +out+
            {"callback" "<function>",
             "pred" "<function>",
             "meta"
             {"listener/id" "a1",
              "route/path" [],
              "listener/type" "route.path"}})]}
(fact "adds a path listener"

  ^{:lang-exceptions
    {:lua {:expect {"callback" "<function>",
                    "pred" "<function>",
                    "meta" {"listener/id" "a1",
                            "route/path" {},
                            "listener/type" "route.path"}}}
     :dart {:form (do
                     (var r (route/make-route "hello"))
                     (xtd/tree-get-data
                      (route/add-path-listener r [] "a1" (fn:>) nil)))}}}
  (!.js
   (var r (route/make-route "hello"))
   (var entry (route/add-path-listener r [] "a1" (fn:>) nil))
   (xtd/tree-get-data entry))
  => +out+

  ^{:lang-exceptions
    {:lua {:expect {"callback" "<function>",
                    "pred" "<function>",
                    "meta" {"listener/id" "a1",
                            "route/path" {},
                            "listener/type" "route.path"}}}
     :dart {:form (do
                     (var r (route/make-route "hello"))
                     (xtd/tree-get-data
                      (route/add-path-listener r [] "a1" (fn:>) nil)))}}}
  (!.js
   (var r (route/make-route "hello"))
   (var entry (route/add-path-listener r [] "a1" (fn:>) nil))
   (xtd/tree-get-data entry))
  => +out+

  (!.py
   (var r (route/make-route "hello"))
   (var entry (route/add-path-listener r [] "a1" (fn:>) nil))
   (xtd/tree-get-data entry))
  => +out+)

^{:refer xt.lang.event-route/add-param-listener :added "4.0"
  :setup [(def +out+
            {"callback" "<function>",
             "pred" "<function>",
             "meta"
             {"listener/id" "a1",
              "route/param" "auth",
              "listener/type" "route.param"}})]}
(fact "adds a param listener"

  ^{:lang-exceptions
    {:dart
     {:form (do
              (var r (route/make-route "hello"))
              (xtd/tree-get-data
               (route/add-param-listener r "auth" "a1" (fn:>) nil)))}}}
  (!.js
   (var r (route/make-route "hello"))
   (var entry (route/add-param-listener r "auth" "a1" (fn:>) nil))
   (xtd/tree-get-data entry))
  => +out+

  (!.lua
   (var r (route/make-route "hello"))
   (var entry (route/add-param-listener r "auth" "a1" (fn:>) nil))
   (xtd/tree-get-data entry))
  => +out+

  (!.py
   (var r (route/make-route "hello"))
   (var entry (route/add-param-listener r "auth" "a1" (fn:>) nil))
   (xtd/tree-get-data entry))
  => +out+)

^{:refer xt.lang.event-route/add-full-listener :added "4.0"
  :setup [(def +out+
            {"callback" "<function>",
             "pred" "<function>",
             "meta"
             {"listener/id" "a1",
              "route/param" "auth",
              "route/path" ["hello"],
              "listener/type" "route.full"}})]}
(fact "adds a full listener"

  ^{:lang-exceptions
    {:dart
     {:form (do
              (var r (route/make-route "hello"))
              (xtd/tree-get-data
               (route/add-full-listener r ["hello"] "auth" "a1" (fn:>) nil)))}}}
  (!.js
   (var r (route/make-route "hello"))
   (var entry (route/add-full-listener r ["hello"] "auth" "a1" (fn:>) nil))
   (xtd/tree-get-data entry))
  => +out+

  (!.lua
   (var r (route/make-route "hello"))
   (var entry (route/add-full-listener r ["hello"] "auth" "a1" (fn:>) nil))
   (xtd/tree-get-data entry))
  => +out+

  (!.py
   (var r (route/make-route "hello"))
   (var entry (route/add-full-listener r ["hello"] "auth" "a1" (fn:>) nil))
   (xtd/tree-get-data entry))
  => +out+)

^{:refer xt.lang.event-route/set-url :added "4.0"
  :setup [(def +out+
            {"params" {},
             "path" {"[\"hello\"]" true},
             "type" "route.url",
             "meta"
             {"listener/id" "a1",
              "route/path" ["hello"],
              "listener/type" "route.path"}})]}
(fact "sets the url for a route"

  ^{:lang-exceptions
    {:dart
     {:form [(notify/wait-on-call
              2000
              (fn []
                (!.dt
                 (var r (route/make-route "hello"))
                 (route/add-path-listener
                  r ["hello"] "a1"
                  (fn [val]
                    (return {"::" "notify.task"
                             "task" (repl/notify-socket
                                     "127.0.0.1"
                                     (@! (:socket-port (l/default-notify)))
                                     val
                                     (@! notify/*override-id*)
                                     nil
                                     {})}))
                  nil)
                 (route/set-url r "hello/world" nil))))]
      :expect [{"params" {},
                "path" {"[\"hello\"]" true},
                "type" "route.url",
                "meta"
                {"listener/id" "a1",
                 "route/path" ["hello"],
                 "listener/type" "route.path"}}]}}}
  [
   (notify/wait-on :js
        (var r (route/make-route "hello"))
        (route/add-path-listener r ["hello"] "a1"
                                 (repl/>notify)
                                 nil)
       (route/set-url r "hello/world"))]
  [{"params" {},
    "path" {"[\"hello\"]" true},
    "type" "route.url",
    "meta"
    {"listener/id" "a1",
     "route/path" ["hello"],
     "listener/type" "route.path"}}]

  ^{:lang-exceptions {:dart {:skip true}}}
  (notify/wait-on :lua
   (var r (route/make-route "hello"))
   (route/add-path-listener r ["hello"] "a1"
                            (repl/>notify)
                            nil)
   (route/set-url r "hello/world"))
  => +out+)

^{:refer xt.lang.event-route/set-path :added "4.0"
  :setup [(def +out+
            {"params" {}
             "path" {"[\"hello\"]" true},
             "type" "route.path",
             "meta"
             {"listener/id" "a1",
              "route/path" ["hello"],
              "listener/type" "route.path"}})]}
(fact "sets the path and param"

   ^{:lang-exceptions
      {:dart
       {:form [(notify/wait-on-call
                2000
                (fn []
                  (!.dt
                   (var r (route/make-route "hello"))
                   (route/add-path-listener
                    r ["hello"] "a1"
                    (fn [val]
                      (return {"::" "notify.task"
                               "task" (repl/notify-socket
                                       "127.0.0.1"
                                       (@! (:socket-port (l/default-notify)))
                                       val
                                       (@! notify/*override-id*)
                                       nil
                                       {})}))
                    nil)
                   (route/set-path r ["hello" "world"] nil))))
               (!.dt
                (var r (route/make-route "hello"))
                (route/set-path r ["hello" "world"] nil)
                [(route/get-url r) r])]}
      }}
   [
     (notify/wait-on :js
        (var r (route/make-route "hello"))
        (route/add-path-listener r ["hello"] "a1"
                                 (repl/>notify)
                                 nil)
       (route/set-path r ["hello" "world"] nil))
    (!.js
     (var r (route/make-route "hello"))
     (route/set-path r ["hello" "world"] nil)
     [(route/get-url r) r])]
  => [+out+
      ["hello/world"
       {"::" "event.route",
        "tree"
        {"params" {},
         "[]" "hello",
         "[\"hello\",\"world\"]" nil,
         "[\"hello\"]" "world"},
        "history" ["hello/world"],
        "listeners" {}}]]

   (notify/wait-on :lua
   (var r (route/make-route "hello"))
   (route/add-path-listener r ["hello"] "a1"
                            (repl/>notify)
                            nil)
   (route/set-path r ["hello" "world"] nil))
  => +out+

   [(notify/wait-on :python
      (var r (route/make-route "hello"))
      (route/add-path-listener r ["hello"] "a1"
                               (repl/>notify)
                               nil)
      (route/set-path r ["hello" "world"] nil))
    (!.py
     (var r (route/make-route "hello"))
     (route/set-path r ["hello" "world"] nil)
     [(route/get-url r) r])]
  => [+out+
      ["hello/world"
       {"::" "event.route",
        "tree"
        {"params" {},
         "[]" "hello",
         "[\"hello\", \"world\"]" nil,
         "[\"hello\"]" "world"},
        "history" ["hello/world"],
        "listeners" {}}]])

^{:refer xt.lang.event-route/set-segment :added "4.0"
  :setup [(def +out+
            {"params" {},
             "path" {"[\"hello\"]" true},
             "type" "route.path",
             "meta"
             {"listener/id" "a1",
              "route/path" ["hello"],
              "listener/type" "route.path"}})]}
(fact "sets the current segment"

  ^{:lang-exceptions
    {:dart
     {:form [(notify/wait-on-call
              2000
              (fn []
                (!.dt
                 (var r (route/make-route "hello"))
                 (route/add-path-listener
                  r ["hello"] "a1"
                  (fn [val]
                    (return {"::" "notify.task"
                             "task" (repl/notify-socket
                                     "127.0.0.1"
                                     (@! (:socket-port (l/default-notify)))
                                     val
                                     (@! notify/*override-id*)
                                     nil
                                     {})}))
                  nil)
                 (route/set-segment r ["hello"] "world"))))
             (!.dt
              (var r (route/make-route "hello"))
              (route/set-segment r ["hello"] "world")
              [(route/get-url r) r])]}
    }}
  [
   (notify/wait-on :js
        (var r (route/make-route "hello"))
        (route/add-path-listener r ["hello"] "a1"
                                 (repl/>notify)
                                 nil)
       (route/set-segment r ["hello"] "world"))
   (!.js
    (var r (route/make-route "hello"))
    (route/set-segment r ["hello"] "world")
    [(route/get-url r) r])]
  => [+out+
      ["hello/world"
       {"::" "event.route",
        "tree" {"params" {}, "[]" "hello", "[\"hello\"]" "world"},
        "history" ["hello/world"],
        "listeners" {}}]]

  (notify/wait-on :lua
   (var r (route/make-route "hello"))
   (route/add-path-listener r ["hello"] "a1"
                            (repl/>notify)
                            nil)
   (route/set-segment r ["hello"] "world"))
  => +out+)

^{:refer xt.lang.event-route/set-param :added "4.0"
  :setup [(def +out+
            {"params" {"auth" true},
             "path" {},
             "type" "route.params",
             "meta"
             {"listener/id" "a1",
              "route/param" "auth",
              "listener/type" "route.param"}})]}
(fact "sets a param in a route"

   ^{:lang-exceptions
      {:dart
       {:form [(notify/wait-on-call
                2000
                (fn []
                  (!.dt
                   (var r (route/make-route "hello?auth=sign_in"))
                   (route/add-param-listener
                    r "auth" "a1"
                    (fn [val]
                      (return {"::" "notify.task"
                               "task" (repl/notify-socket
                                       "127.0.0.1"
                                       (@! (:socket-port (l/default-notify)))
                                       val
                                       (@! notify/*override-id*)
                                       nil
                                       {})}))
                    nil)
                   (route/set-param r "auth" "register" nil))))
               (!.dt
                (var r (route/make-route "hello?auth=sign_in"))
                (route/set-param r "auth" "register" nil)
                [(route/get-url r) r])]}
      }}
   [
     (notify/wait-on :js
        (var r (route/make-route "hello?auth=sign_in"))
        (route/add-param-listener r "auth" "a1"
                                  (repl/>notify)
                                  nil)
       (route/set-param r "auth" "register" nil))
    (!.js
     (var r (route/make-route "hello?auth=sign_in"))
     (route/set-param r "auth" "register" nil)
     [(route/get-url r) r])]
  => [+out+
      ["hello?auth=register"
       {"::" "event.route",
        "tree"
        {"params" {"[\"hello\"]" {"auth" "register"}}, "[]" "hello"},
        "history" ["hello?auth=register"],
        "listeners" {}}]]


   (!.js
    (var r (route/make-route "hello?auth=sign_in"))
    (route/set-param r "auth" nil nil)
    (route/get-url r))

   (notify/wait-on :lua
    (var r (route/make-route "hello?auth=sign_in"))
    (route/add-param-listener r "auth"  "a1"
                              (repl/>notify)
                              nil)
    (route/set-param r "auth" "register" nil))
  => +out+)

^{:refer xt.lang.event-route/reset-route :added "4.0"}
(fact "resets the route, clearing all params"

  (!.js
   (var r (route/make-route "hello?auth=sign_in"))
   (route/reset-route r "world")
   (route/get-url r))
  => "world"

  (!.lua
   (var r (route/make-route "hello?auth=sign_in"))
   (route/reset-route r "world")
   (route/get-url r))
  => "world")
