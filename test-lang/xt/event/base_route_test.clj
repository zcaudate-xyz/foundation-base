(ns xt.event.base-route-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:js :python :ruby]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.base-route :as route]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.base-route :as route]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.event.base-route/interim-from-url :added "4.1" :seedgen/base {:lua {:suppress true}}}
(fact "converts between url and tree forms"

  (!.js
   [(route/interim-from-url "hello/world?id=1&type=name")
    (route/interim-to-url {"params" {"[]" {"id" "1"}}
                           "path" []})
    (route/path-to-tree ["hello" "world"])
    (route/path-from-tree {"[]" "hello"
                           "[\"hello\"]" "world"})
    (route/changed-params-raw {"id" "1" "type" "name"}
                              {"type" "hello"})])
  => [{"params" {"[\"hello\",\"world\"]" {"id" "1"
                                          "type" "name"}}
       "path" ["hello" "world"]}
      "?id=1"
      {"[]" "hello"
       "[\"hello\"]" "world"}
      ["hello" "world"]
      {"id" true
       "type" true}]

  (!.py
   [(route/interim-from-url "hello/world?id=1&type=name")
     (route/interim-to-url {"params" {"[]" {"id" "1"}}
                            "path" []})
     (route/path-to-tree ["hello" "world"] nil)
     (route/path-from-tree {"[]" "hello"
                            "[\"hello\"]" "world"})
     (route/changed-params-raw {"id" "1" "type" "name"}
                               {"type" "hello"})])
  => [{"params" {"[\"hello\",\"world\"]" {"id" "1"
                                          "type" "name"}}
       "path" ["hello" "world"]}
      "?id=1"
      {"[]" "hello"
       "[\"hello\"]" "world"}
      ["hello" "world"]
      {"id" true
       "type" true}])

^{:refer xt.event.base-route/interim-to-url :added "4.1"}
(fact "converts interim to url"

  (!.js
   (route/interim-to-url
    {"params" {"[\"hello\"]" {"id" "1"}}
     "path" ["hello"]}))
  => "hello?id=1"

  (!.py
   (route/interim-to-url
    {"params" {"[\"hello\"]" {"id" "1"}}
     "path" ["hello"]}))
  => "hello?id=1")

^{:refer xt.event.base-route/path-to-tree :added "4.1" :seedgen/base {:lua {:suppress true}}}
(fact "converts a path to a tree"

  (!.js
   (route/path-to-tree ["hello" "world"] true))
  => {"[]" "hello"
      "[\"hello\"]" "world"
      "[\"hello\",\"world\"]" nil}

  (!.py
   (route/path-to-tree ["hello" "world"] true))
  => {"[]" "hello"
      "[\"hello\"]" "world"
      "[\"hello\",\"world\"]" nil})

^{:refer xt.event.base-route/interim-to-tree :added "4.1" :seedgen/base {:lua {:suppress true}}}
(fact "converts interim data to a tree"

  (!.js
   (route/interim-to-tree
    {"params" {"[\"hello\"]" {"id" "1"}}
     "path" ["hello"]}
    true))
  => {"[]" "hello"
      "[\"hello\"]" nil
      "params" {"[\"hello\"]" {"id" "1"}}}

  (!.py
   (route/interim-to-tree
    {"params" {"[\"hello\"]" {"id" "1"}}
     "path" ["hello"]}
    true))
  => {"[]" "hello"
      "[\"hello\"]" nil
      "params" {"[\"hello\"]" {"id" "1"}}})

^{:refer xt.event.base-route/path-from-tree :added "4.1" :seedgen/base {:lua {:suppress true}}}
(fact "extracts a path from a tree"

  (!.js
   (route/path-from-tree
    {"[]" "hello"
     "[\"hello\"]" "world"}))
  => ["hello" "world"]

  (!.py
   (route/path-from-tree
    {"[]" "hello"
     "[\"hello\"]" "world"}))
  => ["hello" "world"])

^{:refer xt.event.base-route/path-params-from-tree :added "4.1"}
(fact "gets params for a path from a tree"

  (!.js
   (route/path-params-from-tree
    {"params" {"[\"hello\"]" {"id" "1"}}}
    ["hello"]))
  => {"id" "1"}

  (!.py
   (route/path-params-from-tree
    {"params" {"[\"hello\"]" {"id" "1"}}}
    ["hello"]))
  => {"id" "1"})

^{:refer xt.event.base-route/interim-from-tree :added "4.1" :seedgen/base {:lua {:suppress true}}}
(fact "converts a tree back to interim form"

  (!.js
   (route/interim-from-tree
    {"[]" "hello"
     "[\"hello\"]" nil
     "params" {"[\"hello\"]" {"id" "1"}}}))
  => {"params" {"[\"hello\"]" {"id" "1"}}
      "path" ["hello"]}

  (!.py
   (route/interim-from-tree
    {"[]" "hello"
     "[\"hello\"]" nil
     "params" {"[\"hello\"]" {"id" "1"}}}))
  => {"params" {"[\"hello\"]" {"id" "1"}}
      "path" ["hello"]})

^{:refer xt.event.base-route/changed-params-raw :added "4.1"}
(fact "diffs raw param maps"

  (!.js
   (route/changed-params-raw
    {"id" "1" "type" "name"}
    {"type" "hello"}))
  => {"id" true
      "type" true}

  (!.py
   (route/changed-params-raw
    {"id" "1" "type" "name"}
    {"type" "hello"}))
  => {"id" true
      "type" true})

^{:refer xt.event.base-route/changed-params :added "4.1"}
(fact "diffs scoped route params"

  (!.js
   (route/changed-params
    {"params" {"[\"hello\"]" {"id" "1"}}}
    {"params" {"[\"hello\"]" {"id" "2"}}}
    ["hello"]))
  => {"id" true}

  (!.py
   (route/changed-params
    {"params" {"[\"hello\"]" {"id" "1"}}}
    {"params" {"[\"hello\"]" {"id" "2"}}}
    ["hello"]))
  => {"id" true})

^{:refer xt.event.base-route/changed-path-raw :added "4.1"}
(fact "diffs raw paths"

  (!.js
   (route/changed-path-raw
    ["hello"]
    ["hello" "world"]))
  => {"[\"hello\"]" true}

  (!.py
   (route/changed-path-raw
    ["hello"]
    ["hello" "world"]))
  => {"[\"hello\"]" true})

^{:refer xt.event.base-route/changed-path :added "4.1" :seedgen/base {:lua {:suppress true}}}
(fact "diffs route paths from trees"

  (!.js
   (route/changed-path
    {"[]" "hello"}
    {"[]" "hello"
     "[\"hello\"]" "world"}))
  => {"[\"hello\"]" true}

  (!.py
   (route/changed-path
    {"[]" "hello"}
    {"[]" "hello"
     "[\"hello\"]" "world"}))
  => {"[\"hello\"]" true})

^{:refer xt.event.base-route/get-url :added "4.1"}
(fact "gets the current route url"

  (!.js
   (route/get-url
    (route/make-route "hello/world?id=1")))
  => "hello/world?id=1"

  (!.py
   (route/get-url
    (route/make-route "hello/world?id=1")))
  => "hello/world?id=1")

^{:refer xt.event.base-route/get-segment :added "4.1"}
(fact "gets route segments by path"

  (!.js
   (var r (route/make-route "hello/world"))
   [(route/get-segment r [])
    (route/get-segment r ["hello"])])
  => ["hello" "world"]

  (!.py
   (var r (route/make-route "hello/world"))
   [(route/get-segment r [])
    (route/get-segment r ["hello"])])
  => ["hello" "world"])

^{:refer xt.event.base-route/get-param :added "4.1"}
(fact "gets a route param by name"

  (!.js
   (route/get-param
    (route/make-route "hello?auth=sign_in")
    "auth"
    nil))
  => "sign_in"

  (!.py
   (route/get-param
    (route/make-route "hello?auth=sign_in")
    "auth"
    nil))
  => "sign_in")

^{:refer xt.event.base-route/get-all-params :added "4.1"}
(fact "gets all params for the current path"

  (!.js
   (route/get-all-params
    (route/make-route "hello?auth=sign_in")
    nil))
  => {"auth" "sign_in"}

  (!.py
   (route/get-all-params
    (route/make-route "hello?auth=sign_in")
    nil))
  => {"auth" "sign_in"})

^{:refer xt.event.base-route/make-route :added "4.1" :seedgen/base {:lua {:suppress true}}}
(fact "tracks route state and listeners"

  (!.js
   (var r (route/make-route "hello?auth=sign_in"))
   (var calls [])
   (var removed nil)
   (var missing nil)
   (var entry
   (route/add-url-listener
           r
           "a1"
           (fn [id data t meta]
             (xt/x:arr-push calls (xt/x:get-key data "type")))
           nil))
    (route/set-url r "hello/world?auth=sign_out" nil)
    (:= removed (route/remove-listener r "a1"))
    (:= missing (route/remove-listener r "missing"))
   [(. r ["::"])
    (route/get-url (route/make-route "hello?auth=sign_in"))
    (route/get-segment (route/make-route "hello?auth=sign_in") [])
    (route/get-param (route/make-route "hello?auth=sign_in") "auth" nil)
    (route/get-all-params (route/make-route "hello?auth=sign_in") [])
    [(. entry ["meta"] ["listener/id"])
     (. entry ["meta"] ["listener/type"])]
    ["a1"]
    calls
    [(. removed ["meta"] ["listener/id"])
     missing]])
  => (just-in
      ["event.route"
       "hello?auth=sign_in"
       "hello"
       "sign_in"
       {}
       ["a1" "route.url"]
       (just ["a1"] :in-any-order)
       ["route.url"]
       ["a1" nil]])

  (!.py
   (var r (route/make-route "hello?auth=sign_in"))
   (var calls [])
   (var removed nil)
   (var missing nil)
   (var entry
         (route/add-url-listener
          r
          "a1"
          (fn [id data t meta]
            (xt/x:arr-push calls (xt/x:get-key data "type")))
          nil))
   (route/set-url r "hello/world?auth=sign_out" nil)
   (:= removed (route/remove-listener r "a1"))
   (:= missing (route/remove-listener r "missing"))
   [(. r ["::"])
    (route/get-url (route/make-route "hello?auth=sign_in"))
    (route/get-segment (route/make-route "hello?auth=sign_in") [])
    (route/get-param (route/make-route "hello?auth=sign_in") "auth" nil)
    (route/get-all-params (route/make-route "hello?auth=sign_in") [])
    [(. entry ["meta"] ["listener/id"])
     (. entry ["meta"] ["listener/type"])]
    ["a1"]
    calls
    [(. removed ["meta"] ["listener/id"])
     missing]])
  => (just-in
      ["event.route"
       "hello?auth=sign_in"
       "hello"
       "sign_in"
       {}
       ["a1" "route.url"]
       (just ["a1"] :in-any-order)
       ["route.url"]
       ["a1" nil]]))

^{:refer xt.event.base-route/add-url-listener :added "4.1"}
(fact "adds a url listener entry"

  (!.js
   (. (route/add-url-listener
       (route/make-route "hello")
       "a1"
       (fn:> [id data t meta] nil)
       {:label "hello"})
      ["meta"]))
  => {"label" "hello"
      "listener/id" "a1"
      "listener/type" "route.url"}

  (!.py
   (. (route/add-url-listener
       (route/make-route "hello")
       "a1"
       (fn:> [id data t meta] nil)
       {:label "hello"})
      ["meta"]))
  => {"label" "hello"
      "listener/id" "a1"
      "listener/type" "route.url"})

^{:refer xt.event.base-route/add-path-listener :added "4.1"}
(fact "adds a path listener entry"

  (!.js
   (. (route/add-path-listener
       (route/make-route "hello")
       ["hello"]
       "a1"
       (fn:> [id data t meta] nil)
       {:label "hello"})
      ["meta"]))
  => {"label" "hello"
      "listener/id" "a1"
      "listener/type" "route.path"
      "route/path" ["hello"]}

  (!.py
   (. (route/add-path-listener
       (route/make-route "hello")
       ["hello"]
       "a1"
       (fn:> [id data t meta] nil)
       {:label "hello"})
      ["meta"]))
  => {"label" "hello"
      "listener/id" "a1"
      "listener/type" "route.path"
      "route/path" ["hello"]})

^{:refer xt.event.base-route/add-param-listener :added "4.1"}
(fact "adds a param listener entry"

  (!.js
   (. (route/add-param-listener
       (route/make-route "hello")
       "auth"
       "a1"
       (fn:> [id data t meta] nil)
       {:label "hello"})
      ["meta"]))
  => {"label" "hello"
      "listener/id" "a1"
      "listener/type" "route.param"
      "route/param" "auth"}

  (!.py
   (. (route/add-param-listener
       (route/make-route "hello")
       "auth"
       "a1"
       (fn:> [id data t meta] nil)
       {:label "hello"})
      ["meta"]))
  => {"label" "hello"
      "listener/id" "a1"
      "listener/type" "route.param"
      "route/param" "auth"})

^{:refer xt.event.base-route/add-full-listener :added "4.1"}
(fact "adds a full route listener entry"

  (!.js
   (. (route/add-full-listener
       (route/make-route "hello")
       ["hello"]
       "auth"
       "a1"
       (fn:> [id data t meta] nil)
       {:label "hello"})
      ["meta"]))
  => {"label" "hello"
      "listener/id" "a1"
      "listener/type" "route.full"
      "route/param" "auth"
      "route/path" ["hello"]}

  (!.py
   (. (route/add-full-listener
       (route/make-route "hello")
       ["hello"]
       "auth"
       "a1"
       (fn:> [id data t meta] nil)
       {:label "hello"})
      ["meta"]))
  => {"label" "hello"
      "listener/id" "a1"
      "listener/type" "route.full"
      "route/param" "auth"
      "route/path" ["hello"]})

^{:refer xt.event.base-route/remove-listener :added "4.1" :seedgen/base {:lua {:expect ["a1"]}}}
(fact "removes a listener by id"

  (!.js
   (var r (route/make-route "hello"))
   (route/add-url-listener
    r
    "a1"
    (fn:> [id data t meta] nil)
    {:label "hello"})
   [(. (route/remove-listener r "a1") ["meta"] ["listener/id"])
    (xt/x:get-key (. r ["listeners"]) "a1")
    (route/remove-listener r "missing")])
  => ["a1"
      nil
      nil]

  (!.py
   (var r (route/make-route "hello"))
   (route/add-url-listener
    r
    "a1"
    (fn:> [id data t meta] nil)
    {:label "hello"})
   [(. (route/remove-listener r "a1") ["meta"] ["listener/id"])
    (xt/x:get-key (. r ["listeners"]) "a1")
    (route/remove-listener r "missing")])
  => ["a1"
      nil
      nil])

^{:refer xt.event.base-route/list-listeners :added "4.1"}
(fact "lists all listener ids"

  (!.js
   (var r (route/make-route "hello"))
   (route/add-url-listener r "a1" (fn:> [id data t meta] nil) nil)
   (route/add-path-listener r ["hello"] "a2" (fn:> [id data t meta] nil) nil)
   (route/add-param-listener r "auth" "a3" (fn:> [id data t meta] nil) nil)
   (var before (route/list-listeners r))
   (route/remove-listener r "a2")
   (var after (route/list-listeners r))
   [before after])
  => (just-in
      [(just ["a1" "a2" "a3"] :in-any-order)
       (just ["a1" "a3"] :in-any-order)])

  (!.py
   (var r (route/make-route "hello"))
   (route/add-url-listener r "a1" (fn:> [id data t meta] nil) nil)
   (route/add-path-listener r ["hello"] "a2" (fn:> [id data t meta] nil) nil)
   (route/add-param-listener r "auth" "a3" (fn:> [id data t meta] nil) nil)
   (var before (route/list-listeners r))
   (route/remove-listener r "a2")
   (var after (route/list-listeners r))
   [before after])
  => (just-in
      [(just ["a1" "a2" "a3"] :in-any-order)
       (just ["a1" "a3"] :in-any-order)]))

^{:refer xt.event.base-route/set-url :added "4.1"}
(fact "updates the full route url and notifies listeners"

  (!.js
   (var r (route/make-route "hello?auth=sign_in"))
   (var calls [])
   (route/add-url-listener r "url" (fn [id data t meta] (xt/x:arr-push calls (. data ["type"]))) nil)
   (route/add-path-listener r ["hello"] "path" (fn [id data t meta] (xt/x:arr-push calls "path")) nil)
   (route/add-param-listener r "auth" "param" (fn [id data t meta] (xt/x:arr-push calls "param")) nil)
   (route/add-full-listener r ["hello"] "auth" "full" (fn [id data t meta] (xt/x:arr-push calls "full")) nil)
   [(route/set-url r "hello/world?auth=sign_out")
    (route/get-url r)
    calls])
  => (just-in
      [(just ["url" "path" "param" "full"] :in-any-order)
       "hello/world?auth=sign_out"
       (just ["route.url" "path" "param" "full"] :in-any-order)])

  (!.py
    (var r (route/make-route "hello?auth=sign_in"))
    (var calls [])
    (route/add-url-listener r "url" (fn [id data t meta] (xt/x:arr-push calls (. data ["type"]))) nil)
    (route/add-path-listener r ["hello"] "path" (fn [id data t meta] (xt/x:arr-push calls "path")) nil)
    (route/add-param-listener r "auth" "param" (fn [id data t meta] (xt/x:arr-push calls "param")) nil)
    (route/add-full-listener r ["hello"] "auth" "full" (fn [id data t meta] (xt/x:arr-push calls "full")) nil)
    [(route/set-url r "hello/world?auth=sign_out" nil)
     (route/get-url r)
     calls])
  => (just-in
      [(just ["url" "path" "param" "full"] :in-any-order)
       "hello/world?auth=sign_out"
       (just ["route.url" "path" "param" "full"] :in-any-order)]))

^{:refer xt.event.base-route/set-path :added "4.1"}
(fact "updates the path and scoped params"

  (!.js
   (var r (route/make-route "hello?auth=sign_in"))
   (var calls [])
   (route/add-url-listener r "url" (fn [id data t meta] (xt/x:arr-push calls (. data ["type"]))) nil)
   (route/add-path-listener r [] "path" (fn [id data t meta] (xt/x:arr-push calls "path")) nil)
   (route/add-param-listener r "auth" "param" (fn [id data t meta] (xt/x:arr-push calls "param")) nil)
   (route/add-full-listener r [] "auth" "full" (fn [id data t meta] (xt/x:arr-push calls "full")) nil)
   [(route/set-path r ["world"] {"auth" "sign_out"})
    (route/get-url r)
    calls])
  => (just-in
      [(just ["url" "path" "param" "full"] :in-any-order)
       "world?auth=sign_out"
       (just ["route.path" "path" "param" "full"] :in-any-order)])

  (!.py
   (var r (route/make-route "hello?auth=sign_in"))
   (var calls [])
   (route/add-url-listener r "url" (fn [id data t meta] (xt/x:arr-push calls (. data ["type"]))) nil)
   (route/add-path-listener r [] "path" (fn [id data t meta] (xt/x:arr-push calls "path")) nil)
   (route/add-param-listener r "auth" "param" (fn [id data t meta] (xt/x:arr-push calls "param")) nil)
   (route/add-full-listener r [] "auth" "full" (fn [id data t meta] (xt/x:arr-push calls "full")) nil)
   [(route/set-path r ["world"] {"auth" "sign_out"})
    (route/get-url r)
    calls])
  => (just-in
      [(just ["url" "path" "param" "full"] :in-any-order)
       "world?auth=sign_out"
       (just ["route.path" "path" "param" "full"] :in-any-order)]))

^{:refer xt.event.base-route/set-segment :added "4.1"}
(fact "updates a single route segment"

  (!.js
   (var r (route/make-route "hello/world"))
   (var calls [])
   (route/add-path-listener r ["hello"] "path" (fn [id data t meta] (xt/x:arr-push calls (. data ["type"]))) nil)
   [(route/set-segment r ["hello"] "there")
    (route/get-url r)
    calls])
  => [["path"]
      "hello/there"
      ["route.path"]]

  (!.py
   (var r (route/make-route "hello/world"))
   (var calls [])
   (route/add-path-listener r ["hello"] "path" (fn [id data t meta] (xt/x:arr-push calls (. data ["type"]))) nil)
   [(route/set-segment r ["hello"] "there")
    (route/get-url r)
    calls])
  => [["path"]
      "hello/there"
      ["route.path"]])

^{:refer xt.event.base-route/set-param :added "4.1"}
(fact "updates a scoped route param"

  (!.js
   (var r (route/make-route "hello"))
   (var calls [])
   (route/add-param-listener r "auth" "param" (fn [id data t meta] (xt/x:arr-push calls (. data ["type"]))) nil)
   [(route/set-param r "auth" "sign_in" nil)
    (route/get-url r)
    (route/get-param r "auth" nil)
    calls])
  => [["param"]
      "hello?auth=sign_in"
      "sign_in"
      ["route.params"]]

  (!.py
   (var r (route/make-route "hello"))
   (var calls [])
   (route/add-param-listener r "auth" "param" (fn [id data t meta] (xt/x:arr-push calls (. data ["type"]))) nil)
   [(route/set-param r "auth" "sign_in" nil)
    (route/get-url r)
    (route/get-param r "auth" nil)
    calls])
  => [["param"]
      "hello?auth=sign_in"
      "sign_in"
      ["route.params"]])

^{:refer xt.event.base-route/reset-route :added "4.1"}
(fact "resets route state and history"

  (!.js
   (var r (route/make-route "hello/world?auth=sign_in"))
   (route/set-param r "type" "name" nil)
   (route/reset-route r "index")
   [(route/get-url r)
    (. r ["history"])])
  => ["index"
      ["index"]]

  (!.py
   (var r (route/make-route "hello/world?auth=sign_in"))
   (route/set-param r "type" "name" nil)
   (route/reset-route r "index")
   [(route/get-url r)
    (. r ["history"])])
  => ["index"
      ["index"]])

(comment
  (s/snapto '[xt.event.base-route])
  
  (s/seedgen-benchadd '[xt.event.base-route] {:lang [:ruby :dart] :write true})
  (s/seedgen-langadd '[xt.event.base-route]  {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.event.base-route]  {:lang [:lua :python] :write true}))
