(ns std.log.template-test
  (:use code.test)
  (:require [std.log.template :refer :all]))

^{:refer std.log.template/add-template :added "3.0"}
(fact "adds a template to the registry"

  (add-template :error/test "The error is {{error/value}}")
  => map?)

^{:refer std.log.template/remove-template :added "3.0"}
(fact "removes a template from the registry"

  (remove-template :error/test)
  => map?)

^{:refer std.log.template/has-template? :added "3.0"}
(fact "checks if template is registered"

  (add-template :error/test "The error is {{error/value}}")
  (has-template? :error/test)
  => true)

^{:refer std.log.template/list-templates :added "3.0"}
(fact "lists all registered templates"

  (list-templates)
  => map?)

^{:refer std.log.template/render-message :added "3.0"}
(fact "returns a message given a :log/class or :log/template"

  (render-message {:log/class :error/test
                   :error/value "HELLO"})
  => "The error is HELLO")
