(ns std.lang.base.compile-links-test
  (:use code.test)
  (:require [std.lang.base.compile-links :refer :all]))

^{:refer std.lang.base.compile-links/get-link-lookup :added "4.0"}
(fact "gets a lookup value"
  ^:hidden

  (get-link-lookup 'indigo
                   {'indigo "hello"})
  => "hello"

  (get-link-lookup 'indigo
                   {"code" "hello"})
  => nil

  (get-link-lookup 'indigo
                   {'code "hello"})
  => nil

  (get-link-lookup 'indigo
                   {#"code$" "hello"})
  => nil)

^{:refer std.lang.base.compile-links/link-attributes :added "4.0"}
(fact "gets link attributes"
  ^:hidden

  (link-attributes 'code
                   'indigo.server
                   {:path-suffix ".js"})
  => {:is-lib? true, :rel "libs/indigo", :suffix ".js", :label "server", :path "libs/indigo/server.js"}


  (link-attributes 'util
                   'indigo.server
                   {:root-libs "tools"
                    :path-suffix ".js"})
  => {:is-lib? true, :rel "tools/indigo", :suffix ".js", :label "server", :path "tools/indigo/server.js"})
