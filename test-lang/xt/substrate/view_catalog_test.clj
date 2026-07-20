(ns xt.substrate.view-catalog-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:langs [:dart]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.substrate.view :as view]
             [xt.substrate.view-catalog :as catalog]]})

^{:refer xt.substrate.view-catalog/has-component? :added "4.1"}
(fact "catalog entries carry valid bands, kinds, events and variants"
  (!.js
   (var problems [])
   (xt/for:object [[id entry] catalog/COMPONENTS]
     (var band (xt/x:get-key entry "band"))
     (when (not (or (== band "core") (== band "extended")))
       (xt/x:arr-push problems (xt/x:cat "band:" id)))
     (when (not (xt/x:is-string? (xt/x:get-key entry "kind")))
       (xt/x:arr-push problems (xt/x:cat "kind:" id)))
     (xt/for:object [[ev desc] (or (xt/x:get-key entry "events") {})]
       (var path (xt/x:get-key desc "path"))
       (when (and (xt/x:not-nil? path)
                  (not (xt/x:is-array? path)))
         (xt/x:arr-push problems (xt/x:cat "event:" id ":" ev))))
     (var variants (xt/x:get-key entry "variants"))
     (when (and (xt/x:not-nil? variants)
                (xt/x:nil? (xt/x:get-key (or (xt/x:get-key entry "props") {})
                                         "variant")))
       (xt/x:arr-push problems (xt/x:cat "variants:" id))))
   [problems
    (catalog/has-component? "ui/button")
    (catalog/has-component? "ui/nope")])
  => [[] true false])

^{:refer xt.substrate.view-catalog/platform-id? :added "4.1"}
(fact "classifies catalog and platform component ids"
  (!.js
   [(catalog/platform-id? "fg/button")
    (catalog/platform-id? "ui/button")
    (catalog/band "fg/alert-dialog")
    (catalog/band "ui/button")
    (catalog/portable? "ui/button")
    (catalog/portable? "fg/button")])
  => [true false "platform" "core" true false])

^{:refer xt.substrate.view-catalog/variant-classes :added "4.1"}
(fact "exposes shared variant class bundles"
  (!.js
   [(catalog/variant-classes "ui/button" "destructive")
    (catalog/variant-classes "ui/button" "fancy")
    (catalog/variant-classes "ui/text" "default")])
  => ["bg-red-600 text-white" nil nil])

^{:refer xt.substrate.view-catalog/validate-props :added "4.1"}
(fact "accepts a grammar-legal view tree"
  (!.js
   (view/validate
    (view/view-spec
     "sample" {}
     (view/node
      "ui/column" {"class" "flex flex-col gap-4 p-5"}
      [(view/node "ui/label" {"value" "Email" "for" "email"} [])
       (view/node "ui/input" {"id" "email"
                              "value" ""
                              "placeholder" "you@example.com"
                              "on_change" (view/action "set-email"
                                                       (view/event-value ["value"]))}
                  [])
       (view/node "ui/button" {"variant" "destructive"
                               "pending" true
                               "on_press" (view/action "login" nil)}
                  ["Sign in"])
       (view/node "ui/image" {"src" "/logo.png"
                              "alt" "logo"
                              "hidden" true
                              "aria_label" "Logo"}
                  [])]))))
  => true)

^{:refer xt.substrate.view-catalog/validate-props :id test-validate-rejects-unknown :added "4.1"}
(fact "rejects unknown components and unknown props"
  (!.js
   (var bad-component false)
   (try
    (view/validate (view/view-spec "s" {} (view/node "ui/nope" {} [])))
    (catch err (:= bad-component true)))
   (var bad-prop false)
   (try
    (view/validate (view/view-spec "s" {} (view/node "ui/button" {"colour" "red"} [])))
    (catch err (:= bad-prop true)))
   [bad-component bad-prop])
  => [true true])

^{:refer xt.substrate.view-catalog/validate-props :id test-validate-rejects-types :added "4.1"}
(fact "rejects invalid prop types, variants and classes"
  (!.js
   (var bad-type false)
   (try
    (view/validate (view/view-spec "s" {} (view/node "ui/input" {"value" 5} [])))
    (catch err (:= bad-type true)))
   (var bad-variant false)
   (try
    (view/validate (view/view-spec "s" {} (view/node "ui/button" {"variant" "fancy"} [])))
    (catch err (:= bad-variant true)))
   (var bad-class false)
   (try
    (view/validate (view/view-spec "s" {} (view/node "ui/text" {"class" ["a"]} [])))
    (catch err (:= bad-class true)))
   [bad-type bad-variant bad-class])
  => [true true true])

^{:refer xt.substrate.view-catalog/validate-action :added "4.1"}
(fact "rejects malformed event action descriptors"
  (!.js
   (var not-descriptor false)
   (try
    (view/validate (view/view-spec "s" {} (view/node "ui/button" {"on_press" "login"} [])))
    (catch err (:= not-descriptor true)))
   (var bad-projection false)
   (try
    (view/validate
     (view/view-spec "s" {}
                     (view/node "ui/input"
                                {"on_change" {"action" "set-email"
                                              "payload" {"$" "event"}}}
                                [])))
    (catch err (:= bad-projection true)))
   [not-descriptor bad-projection])
  => [true true])

^{:refer xt.substrate.view/validate-portable :added "4.1"}
(fact "allows fg/ platform ids by default and rejects them when portable"
  (!.js
   (var spec (view/view-spec "s" {} (view/node "fg/button" {"class" "x"} ["Hi"])))
   (var portable-rejected false)
   (try
    (view/validate-portable spec)
    (catch err (:= portable-rejected true)))
   [(view/validate spec) portable-rejected])
  => [true true])
