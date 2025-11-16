(ns std.lang.base.book-module-test
  (:use code.test)
  (:require [std.lang.base.book-module :refer :all]
            [std.lang :as l]))

^{:refer std.lang.base.book-module/book-module? :added "4.0"}
(fact "checks of object is a book module")

^{:refer std.lang.base.book-module/book-module :added "4.0"}
(fact "creates a book module"
  ^:hidden
  
  (book-module {:id 'L.core
                :lang :lua
                :link '{- L.core}
                :declare {}
                :alias '{cr coroutine
                         t  table}
                :native   {}
                :code   '{identity {}}})
  => book-module?)

^{:refer std.lang.base.book-module/module-code-deps :added "4.0"}
(fact "gets dependencies for a given module"
  ^:hidden
  
  (-> (book-module {:id 'L.nginx
                    :lang :lua
                    :link '{-  L.nginx
                            u  L.core}
                    :declare {}
                    :alias '{cr coroutine
                             t  table}
                    :native   {}
                    :code   '{identity {}}})
      (module-deps-code))
  => '#{})

^{:refer std.lang.base.book-module/module-deps-code :added "4.0"}
(fact "gets the code link dependencies"
  ^:hidden
  
  (module-code-deps
   (l/get-module
    (l/default-library)
    :js
    'js.blessed.ui-core))
  => '#{js.blessed.ui-style xt.lang.base-lib js.react})

^{:refer std.lang.base.book-module/module-deps-native :added "4.0"}
(fact "gets the native link dependencies"
  ^:hidden
  
  (module-deps-native
   (std.lang/get-module
    (std.lang/default-library)
    :js
    'js.react))
  => '{"react" #{React}})

^{:refer std.lang.base.book-module/module-deps-fragment :added "4.0"}
(fact "gets all fragments that have beeen used in js.react"
  ^:hidden
  
  (module-deps-fragment
   (std.lang/get-module
    (std.lang/default-library)
    :js
    'js.react))
  => '#{js.core/floor
        js.core/map
        js.react/ref
        xt.lang.base-lib/json-encode
        js.react/curr:set
        xt.lang.base-lib/now-ms
        xt.lang.base-lib/for:array
        js.react/lazy
        js.react/watch
        js.core/delayed
        xt.lang.base-lib/nil?
        js.react/Component
        js.react/init
        xt.lang.base-lib/get-key
        js.core/min
        js.react/local
        js.core/identity
        js.core/repeating
        js.core/round
        xt.lang.base-lib/len
        js.react/run
        js.react/curr
        js.core/randomId
        js.core/future
        xt.lang.base-lib/first
        js.core/indexOf
        xt.lang.base-lib/not-nil?
        xt.lang.base-lib/LOG!
        js.core/clearTimeout
        js.core/clearInterval
        js.core/future-delayed
        js.core/max
        js.react/const})
