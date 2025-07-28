(ns std.lang.model.spec-js.meta-test
  (:use code.test)
  (:require [std.lang.model.spec-js.meta :refer :all]
            [std.lang :as l]))

^{:refer std.lang.model.spec-js.meta/js-module-import :added "4.0"}
(fact "outputs the js module import from"
  ^:hidden
  
  (js-module-import 'react '{:as React} {})
  => '(:- :import (quote [React]) :from "'react'")

  (js-module-import 'react '{:as [:* React]
                             :refer [hello world]} {})
  => '(:- :import (quote [(:- :* :as React) #{hello world}]) :from "'react'")

  (l/emit-as
   :js [(js-module-import 'react '{:as [:* React]
                                   :refer [hello world]} {})])
  => "import * as React,{hello,world} from 'react'"
  
  (js-module-import 'react '{:as [:* React]
                             :refer [hello world]} {:emit {:lang/imports :commonjs}})
  => '(const React := (require "react"))

  (js-module-import 'react '{:as [:* React]
                             :refer [hello world]} {:emit {:lang/imports :global}})
  => '(Object.defineProperty !:G "React" {:value (require "react")}))

^{:refer std.lang.model.spec-js.meta/js-module-export :added "4.0"}
(fact "outputs the js module export form"
  ^:hidden
  
  (js-module-export '{:as main} {})
  => '(:- :export :default main)

  (js-module-export '{:as main} {:emit {:lang/exports :commonjs}})
  => '(:= module.exports main))

^{:refer std.lang.model.spec-js.meta/js-module-link :added "4.0"}
(fact "gets the relative js based module"
  ^:hidden
  
  (js-module-link 'kmi.common {:base 'kmi.hello})
  => "./common"

  (js-module-link 'kmi.exchange
                    {:base 'kmi :target "src"})
  => "./kmi/exchange"


  (js-module-link 'kmi.exchange
                    {:base 'kmi.other.namespace :target "src"})
  => "../exchange"

  (js-module-link 'js.core
                    {:base 'kmi.other.main :target "src"})
  => "../../js/core")
