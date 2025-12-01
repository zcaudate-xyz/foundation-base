(ns std.lang.base.impl-lifecycle-test
  (:use code.test)
  (:require [std.lang.base.impl-lifecycle :refer :all]
            [std.lang.base.compile :as compile]
            [std.lang.base.library :as lib]
            [std.lang.base.impl :as impl]
            [js.blessed :as blessed]
            [js.blessed.ui-core :as ui-core]
            [xt.lang.base-lib :as k]
            [xt.lang]
            [std.lang :as l]
            [std.lib :as h]))

(def +library+
  (let [lib (impl/clone-default-library)]
    (impl/with:library [lib]
      (require '[js.react] :reload)
      (require '[js.blessed] :reload)
      (require '[js.blessed.ui-core] :reload)
      (require '[js.blessed.frame-status] :reload)
      (require '[js.blessed.frame-console] :reload)
      (require '[xt.lang.base-lib] :reload))
    lib))

^{:refer std.lang.base.impl-lifecycle/emit-module-prep :added "4.0"}
(fact "prepares the module for emit"
  ^:hidden
  
  (-> (emit-module-prep 'xt.lang
                        {:lang :lua})
      second
      keys
      sort)
  => '(:code :direct :native :setup :teardown)

  (:direct (second (emit-module-prep 'xt.lang.base-lib
                                   {:lang :lua
                                    :emit {:compile {:type :graph
                                                     :root-ns 'lua}}})))
  => #{}
  
  (:direct (second (emit-module-prep 'js.blessed
                                   {:lang :js
                                    :emit {:compile {:type :graph
                                                     :base    'js
                                                     :root-ns 'js}}})))
  => #{}
  
  (:direct (second (emit-module-prep 'js.blessed.ui-core
                                     {:lang :js
                                      :emit {:compile {:type :graph
                                                       :base    'js
                                                       :root-ns 'js.blessed.ui-core}}})))
  => '#{js.blessed.ui-style xt.lang.base-lib js.react})

^{:refer std.lang.base.impl-lifecycle/emit-module-setup-concat :added "4.0"}
(fact "joins setup raw into individual blocks"
  (emit-module-setup-concat {:setup-body "setup" :native-arr ["native"] :link-arr ["link"] :header-arr ["header"] :code-arr ["code"] :export-body "export"})
  => '("setup" "native" "link" "header" "code" "export"))

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
                                (emit-module-prep 'js.blessed.ui-core
                                                  {:lang :js
                                                   :emit {:compile {:type :graph
                                                                    :base    'js
                                                                    :root-ns 'js.blessed.ui-core}}}))
  => (contains ["import React from 'react'" "import Blessed from 'blessed'"] :in-any-order))

^{:refer std.lang.base.impl-lifecycle/emit-module-setup-link-import :added "4.0"}
(fact "creates a import structure from links"
  ^:hidden

  (impl/with:library [+library+]
    (emit-module-setup-link-import
     :graph
     'js.blessed.ui-core
     'js.react
     (compile/compile-module-create-links
      '[js.blessed.ui-core
        js.blessed.ui-style
        js.react
        xt.lang.base-lib]
      'js
      {})
     (l/get-module
      +library+
      :js
      'js.blessed.ui-core)
     {:path-separator "/"
      :root-prefix "@"}))
  => '{:ns "../react", :suffix "", :as r})

^{:refer std.lang.base.impl-lifecycle/emit-module-setup-link-arr :added "4.0"}
(fact "creates the setup code for internal links"
  ^:hidden
  
  (emit-module-setup-link-arr
   {:emit {:code   {:link {:path-separator "/"
                           :root-prefix "@"}}
           :compile {:links  (compile/compile-module-create-links
                              '[js.blessed.ui-core
                                js.blessed.ui-style
                                js.react
                                xt.lang.base-lib]
                              'js
                              {})}}}
   (emit-module-prep
    'js.blessed.ui-core
    {:lang :js
     :emit {:compile {:type :graph
                      :base    'js
                      :root-ns 'js.blessed.ui-core
                      }}}))

  => (contains ["import * as ui_style from '@/blessed/ui-style'"
                "import * as k from '@/libs/xt/lang/base-lib'"
                "import * as r from '@//react'"] :in-any-order)

  (emit-module-setup-link-arr
   {:emit {:code   {:link {:path-separator "|"
                           :root-prefix "@"}}
           :compile {:links  (compile/compile-module-create-links
                              '[js.blessed.ui-core
                                js.blessed.ui-style
                                js.react
                                xt.lang.base-lib]
                              'js
                              {})}}}
   (emit-module-prep
    'js.blessed.ui-core
    {:lang :js
     :emit {:compile {:type :graph
                      :base    'js
                      :root-ns 'js.blessed.ui-core}}}))
  => (contains ["import * as ui_style from '@|blessed|ui-style'"
                "import * as k from '@|libs/xt/lang|base-lib'"
                "import * as r from '@||react'"] :in-any-order))

^{:refer std.lang.base.impl-lifecycle/emit-module-setup-export-body :added "4.0"}
(fact "the code for exporting the body"
  (emit-module-setup-export-body nil (emit-module-prep 'xt.lang {:lang :lua}))
  => string?)

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
(fact "creates module teardown map of array strings"
  (emit-module-teardown-raw 'xt.lang.base-lib {:lang :lua})
  => map?)

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
