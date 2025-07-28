(ns std.lang.base.impl-lifecycle-test
  (:use code.test)
  (:require [std.lang.base.impl-lifecycle :refer :all]
            [js.blessed :as blessed]
            [js.blessed.ui-core :as ui-core]
            [xt.lang.base-lib :as k]
            [xt.lang]
            [std.lang :as l]
            [std.lib :as h]))

^{:refer std.lang.base.impl-lifecycle/emit-module-prep :added "4.0"}
(fact "prepares the module for emit"
  ^:hidden
  
  (-> (emit-module-prep 'xt.lang
                        {:lang :lua})
      second
      keys
      sort)
  => '(:code :export :link :native :setup :teardown)

  (:link (second (emit-module-prep 'xt.lang.base-lib
                                   {:lang :lua
                                    :emit {:compile {:type :graph
                                                     :root-ns 'lua}}})))
  => ()
  
  (:link (second (emit-module-prep 'js.blessed
                                   {:lang :js
                                    :emit {:compile {:type :graph
                                                     :root-ns 'js}}})))
  => ()
  
  (:link (second (emit-module-prep 'js.blessed.ui-core
                                   {:lang :js
                                    :emit {:compile {:type :graph
                                                     :base    'js
                                                     :root-ns 'js.blessed.ui-core}}})))
  => '(["./xt/lang/base-lib" {:as k, :ns xt.lang.base-lib}]
       ["./js/react" {:as r, :ns js.react}]
       ["./js/blessed/ui-style" {:as ui-style, :ns js.blessed.ui-style}])

  (emit-module-prep 'js.blessed.ui-core
                    {:lang :js
                     :emit {:compile {:type :directory
                                      :base    'js
                                      :root-ns 'js.blessed.ui-core}}}))

^{:refer std.lang.base.impl-lifecycle/emit-module-setup-concat :added "4.0"}
(fact "joins setup raw into individual blocks")

^{:refer std.lang.base.impl-lifecycle/emit-module-setup-join :added "4.0"}
(fact "joins setup raw into the setup code"
  ^:hidden
  
  (-> (emit-module-setup-raw 'js.blessed.ui-core
                             {:lang :js
                              :emit {:export {:suppress true}
                                     :code   {:label true}}})
      (emit-module-setup-join))
  => string?)

^{:refer std.lang.base.impl-lifecycle/emit-module-setup-native-arr :added "4.0"}
(fact "creates the setup code for native imports"
  ^:hidden
  
  (emit-module-setup-native-arr 'js.blessed.ui-core
                                {:lang :js}
                                (emit-module-prep 'js.blessed.ui-core
                                                  {:lang :js
                                                   :emit {:compile {:type :graph
                                                                    :base    'js
                                                                    :root-ns 'js.blessed.ui-core}}}))
  => '("import nodeUtil from 'util'"
       "import React from 'react'"
       "import blessed from 'blessed'"
       "import reactBlessed from 'react-blessed'"))

^{:refer std.lang.base.impl-lifecycle/emit-module-setup-link-arr :added "4.0"}
(fact "creates the setup code for internal links"
  ^:hidden
  
  (emit-module-setup-link-arr 'js.blessed.ui-core
                              {:lang :js}
                              (emit-module-prep 'js.blessed.ui-core
                                                {:lang :js
                                                 :emit {:compile {:type :graph
                                                                  :base    'js
                                                                  :root-ns 'js.blessed.ui-core}}}))
  => '("import k from './xt/lang/base-lib'"
       "import r from './js/react'"
       "import ui_style from './js/blessed/ui-style'")

  (emit-module-setup-link-arr 'js.blessed.ui-core
                              {:lang :js}
                              (emit-module-prep 'js.blessed.ui-core
                                                {:lang :js
                                                 :emit {:compile {:type :directory
                                                                  :base    'js
                                                                  :root-ns 'js
                                                                  :root-prefix "@"
                                                                  }}}))
  => '("import k from '@/libs/xt/lang/base-lib'"
       "import r from '@/react'"
       "import ui_style from '@/blessed/ui-style'")
  
  (emit-module-setup-link-arr 'js.blessed.ui-core
                              {:lang :js}
                              (emit-module-prep 'js.blessed.ui-core
                                                {:lang :js
                                                 :emit {:compile {:type :directory
                                                                  :base    'js
                                                                  :root-ns 'js
                                                                  :root-prefix "@"
                                                                  :path-separator "|"
                                                                  :path-suffix ".tsx"}}}))  
  => '("import k from '@|libs|xt|lang|base-lib.tsx'"
       "import r from '@|react.tsx'"
       "import ui_style from '@|blessed|ui-style.tsx'"))

^{:refer std.lang.base.impl-lifecycle/emit-module-setup-raw :added "4.0"}
(fact "creates module setup map of array strings"
  ^:hidden
  
  (emit-module-setup-raw 'js.blessed.ui-core
                         {:lang :js})
  => map?)

^{:refer std.lang.base.impl-lifecycle/emit-module-setup :added "4.0"}
(fact "emits the entire module as string"
  ^:hidden
  
  (:link (second (emit-module-prep 'xt.lang.base-lib
                                   {:lang :lua
                                    :graph {:root-ns 'lua}})))
  => '{}
  
  (emit-module-setup 'xt.lang.base-lib
                     {:lang :lua})
  => string?)

^{:refer std.lang.base.impl-lifecycle/emit-module-teardown-concat :added "4.0"}
(fact "joins teardown raw into individual blocks"

  (-> (emit-module-teardown-raw 'xt.lang.base-lib
                                {:lang :lua})
      (emit-module-teardown-concat))
  => coll?)

^{:refer std.lang.base.impl-lifecycle/emit-module-teardown-join :added "4.0"}
(fact "joins teardown raw into code"

  (-> (emit-module-teardown-raw 'xt.lang.base-lib
                                {:lang :lua})
      (emit-module-teardown-join))
  => string?)

^{:refer std.lang.base.impl-lifecycle/emit-module-teardown-raw :added "4.0"}
(fact "creates module teardown map of array strings")

^{:refer std.lang.base.impl-lifecycle/emit-module-teardown :added "4.0"}
(fact "creates the teardown script"
  ^:hidden
  
  (emit-module-teardown 'xt.lang.base-lib
                        {:lang :lua
                         :layout :full})
  => string?
  
  (emit-module-teardown 'xt.lang.base-lib
                        {:lang :lua
                         :layout :full
                         :emit {:code {:suppress true}}})
  => "")

(comment
  (./import))
