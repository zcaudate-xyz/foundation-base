(ns std.lib.mustache-test
  (:require [std.lib.mustache :refer :all])
  (:use code.test))

^{:refer std.lib.mustache/render :added "3.0"}
(fact "converts a template with mustache data"

  (render "{{user.name}}" {:user {:name "hara"}})
  => "hara"

  (render "{{# user.account}}{{name}} {{/user.account}}"
          {:user {:account [{:name "admin"}
                            {:name "user"}]}})
  => "admin user "

  (render "{{? user}}hello{{/user}}" {:user true})
  => "hello"

  (render "{{^ user.name}}hello{{/user.name}}" {:user nil})
  => "hello")
