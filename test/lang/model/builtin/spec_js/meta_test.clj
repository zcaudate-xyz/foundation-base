(ns lang.model.builtin.spec-js.meta-test
  (:require [code.test :refer [=> fact]]
            [lang.core :as l]
            [lang.model.builtin.spec-js.meta :refer [js-module-import
                                                       js-module-export
                                                       js-module-link
                                                       js-transform-entry]]))

^{:refer lang.model.builtin.spec-js.meta/js-module-import-async :added "4.0"}
(fact "helper for import")

^{:refer lang.model.builtin.spec-js.meta/js-module-import :added "4.0"}
(fact "outputs the js module import from"

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
                             :refer [hello world]} {:emit {:lang/format :commonjs}})
  => '(const React := (require "react"))

  (js-module-import 'react '{:as [:* React]
                             :refer [hello world]} {:emit {:lang/format :global}})
  => '(Object.defineProperty !:G "React" {:value (require "react")})

  (js-module-import
   "@xtalk/lang/common-data"
   '{:as xtd :suffix ".js"}
   {:emit {:import :link :lang/format :commonjs}})
  => '(const xtd := (require "@xtalk/lang/common-data.js")))

^{:refer lang.model.builtin.spec-js.meta/js-module-export :added "4.0"}
(fact "outputs the js module export form"

  (js-module-export '{} {:emit {:lang/export true}})
  => '(:- :export :default (tab))

  (js-module-export '{} {:emit {:lang/format :commonjs}})
  => '(:= module.exports (tab)))

^{:refer lang.model.builtin.spec-js.meta/js-module-link :added "4.0"}
(fact "gets the relative js based module"

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

^{:refer lang.model.builtin.spec-js.meta/js-transform-entry :added "4.0"}
(fact "function for transforming :type :module entries"

  (js-transform-entry
   "function Page(){}"
   {:entry {:op-key :defn}
    :mopts {:emit {:lang/format :module}}})
  => "export function Page(){}"

  (js-transform-entry
   "function Page(){}"
   {:entry {:op-key :defn}
    :mopts {:emit {:lang/format :module}
            :module {:static {:per-entry [:export]}}}})
  => "export function Page(){}"

  (js-transform-entry
   "function Page(){}"
   {:entry {:op-key :defn}
    :mopts {:emit {:lang/format :module}
            :module {:static {:per-entry [:none]}}}})
  => "function Page(){}"

  (js-transform-entry
   "const Page = 1"
   {:entry {:op-key :def}
    :mopts {:emit {:lang/format :module}
            :module {:static {:per-entry [:none]}}}})
  => "const Page = 1"

  (js-transform-entry
   "class Page {}"
   {:entry {:op-key :defclass}
    :mopts {:emit {:lang/format :module}
            :module {:static {:per-entry [:none]}}}})
  => "class Page {}"

  (js-transform-entry
   "Page = 1"
   {:entry {:op-key :set}
    :mopts {:emit {:lang/format :module}
            :module {:static {:per-entry [:export]}}}})
  => "Page = 1"

  (js-transform-entry
   "function Page(){}"
   {:entry {:op-key :defn}
    :mopts {:emit {:type :script :lang/format :module}
            :module {:static {:per-entry [:none]}}}})
  => "function Page(){}")
