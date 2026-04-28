(ns std.lang.model.spec-lua-test
  (:require [std.lang :as l]
             [std.lang.base.script :as script]
             [std.lang.base.util :as ut]
             [std.lang.model.spec-lua :refer :all]
             [std.lang.model.spec-lua.variant-nginx :as nginx])
  (:use code.test))

(script/script- :lua)

^{:refer std.lang.model.spec-lua/tf-counter :added "4.1"}
(fact "compound assignment lowers to valid lua assignments"
  [(l/emit-as :lua '[(:+= a 2)])
   (l/emit-as :lua '[(:-= a 2)])
   (l/emit-as :lua '[(:*= a 2)])
   (l/emit-as :lua '[(:+= circle.x 20)])]
  => ["a = (a + 2)"
      "a = (a - 2)"
      "a = (a * 2)"
      "circle.x = (circle.x + 20)"])

^{:refer std.lang.model.spec-lua/lua-map-key :added "3.0"}
(fact "custom lua map key"

  (lua-map-key 123 +grammar+ {})
  => "[123]"

  (lua-map-key "123" +grammar+ {})
  => "['123']"


  (lua-map-key "abc" +grammar+ {})
  => "abc"

  (lua-map-key :abc +grammar+ {})
  => "abc")

^{:refer std.lang.model.spec-lua/+grammar+ :added "4.1"}
(fact "throw emits lua errors"
  (l/emit-as :lua '[(throw "boom")])
  => "error('boom')")
^{:refer std.lang.model.spec-lua.variant-nginx/tf-for-async :added "4.0"}
(fact "for async transform for nginx"

  (nginx/tf-for-async '(for:async [[ok err] (call (x:callback))]
                                   {:success (return ok)
                                    :error   (return err)
                                    :finally (return true)}))
  => '(ngx.thread.spawn
       (fn []
         (for:try [[ok err] (call (x:callback))]
                  {:success (return ok),
                   :error   (return err)})
         (return true))))

^{:refer std.lang.model.spec-lua.variant-nginx/+grammar-delta+ :added "4.1"}
(fact "nginx promise ops hard-link through nginx common-promise"
  [(get-in nginx/+grammar-delta+ [:x-promise :raw])
   (get-in nginx/+grammar-delta+ [:x-promise-then :raw])
   (get-in nginx/+grammar-delta+ [:x-promise-catch :raw])
   (get-in nginx/+grammar-delta+ [:x-promise-finally :raw])
   (get-in nginx/+grammar-delta+ [:x-promise-native? :raw])
   (get-in nginx/+grammar-delta+ [:x-with-delay :raw])]
  => ['lua.nginx.common-promise/promise
      'lua.nginx.common-promise/promise-then
      'lua.nginx.common-promise/promise-catch
      'lua.nginx.common-promise/promise-finally
      'lua.nginx.common-promise/promise-native?
      'lua.nginx.common-promise/with-delay])

^{:refer std.lang.model.spec-lua/lua-module-link :added "4.0"}
(fact "gets the absolute lua based module"

  (lua-module-link 'kmi.common {:root-ns 'kmi.hello})
  => "./common"

  (lua-module-link 'kmi.exchange
                   {:root-ns 'kmi :target "src"})
  => "./kmi/exchange")

^{:refer std.lang.model.spec-lua/lua-module-export :added "4.0"}
(fact "outputs the lua module export form"

  (lua-module-export 'kmi.common {:root-ns 'kmi.hello})
  => '(return (tab)))
