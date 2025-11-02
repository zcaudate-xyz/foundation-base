(ns std.lib
  (:require [std.lib.atom :as atom]
            [std.lib.apply :as apply]
            [std.lib.class :as class]
            [std.lib.collection :as collection]
            [std.lib.context.registry :as reg]
            [std.lib.context.pointer :as ptr]
            [std.lib.context.space :as space]
            [std.lib.component :as component]
            [std.lib.component.track :as track]
            [std.lib.foundation :as f]
            [std.lib.deps :as deps]
            [std.lib.enum :as enum]
            [std.lib.env :as env]
            [std.lib.extend :as extend]
            [std.lib.function :as fn]
            [std.lib.future :as future]
            [std.lib.impl :as impl]
            [std.lib.io :as io]
            [std.lib.invoke :as invoke]
            [std.lib.memoize :as memoize]
            [std.lib.network :as network]
            [std.lib.origin :as origin]
            [std.lib.os :as os]
            [std.lib.protocol :as protocol]
            [std.lib.resource :as resource]
            [std.lib.return :as return]
            [std.lib.security :as security]
            [std.lib.signal :as signal]
            [std.lib.sort :as sort]
            [std.lib.template :as template]
            [std.lib.time :as time]
            [std.lib.trace :as trace]
            [std.lib.walk :as walk]
            [clojure.set :as set])
  (:refer-clojure :exclude [-> ->> keyword swap! reset! aget fn
                            ns require pr prn memoize set! future future?
                            parse-long parse-double]))

(f/intern-all std.lib.atom
              std.lib.collection
              std.lib.enum
              std.lib.foundation
              std.lib.function
              std.lib.future)

(f/intern-all std.lib.io
              std.lib.network
              std.lib.os
              std.lib.protocol
              std.lib.time
              std.lib.walk)
 
(f/intern-in apply/apply-as
             apply/apply-in
             apply/invoke-as

             class/ancestor:list
             class/ancestor:tree
             class/class:array-component
             class/class:abstract?
             class/class:inherits?
             class/class:interface?
             class/class:match
             class/class:interfaces
             class/primitive
             class/primitive:array?
             class/primitive?

             component/start
             component/stop
             component/started?
             component/stopped?
             [comp:start component/start]
             [comp:stop component/stop]
             [comp:stopped? component/stopped?]
             [comp:started? component/started?]
             [comp:kill component/kill]
             [comp:info component/info]
             [comp:health component/health]
             [with:component component/with]
             [comp:all-props component/all-props]
             [comp:get-prop component/get-prop]
             [comp:set-prop component/set-prop]
             [comp:remote? component/remote?]
             component/component?
             component/impl:component
	     component/wrap-start
	     component/wrap-stop

             [deps:construct deps/construct]
             [deps:deconstruct deps/deconstruct]
             [deps:resolve deps/deps-resolve]
             [deps:ordered deps/deps-ordered]
             [deps:list deps/list-entries]
             [deps:entry deps/get-entry]
             [deps:direct deps/get-deps]
             [deps:refresh deps/refresh-entry]
             [deps:add deps/add-entry]
             [deps:remove deps/remove-entry]
             [deps:unload deps/unload-entry]
             [deps:reload deps/reload-entry]
             [dependents:ordered deps/dependents-ordered]
             [dependents:direct deps/dependents-direct]
             [dependents:all deps/dependents-all]
             [dependents:refresh deps/dependents-refresh]

             env/ns-get
             env/ns-sym
             env/dev?
             env/require
             env/local
             env/local:set
             env/local:clear
             env/prn
             env/prn:fn
             env/do:prn
             env/do:pl
             env/do:pp
             env/p
             env/pr
             env/pl
             env/pl:fn
             env/pl-add-lines
             env/pp
             env/pp-fn
             env/pp-str
             env/prf
             env/meter
             env/meter-out
             env/throwable-string
             env/explode
             env/close
             env/sys:resource
             env/sys:resource-content
             env/sys:ns-dir
             env/sys:ns-file
             env/sys:ns-url
             env/match-filter
             env/dbg
             env/dbg-print
             env/with:dbg
             env/dbg:add-filters
             env/dbg:remove-filters
             
             resource/res:spec-get
             resource/res:spec-add
             resource/res:spec-remove
             resource/res:spec-list

             impl/defimpl
             impl/extend-impl
             impl/build-impl
             impl/impl:proxy
             impl/impl:doto
             impl/impl:unwrap-sym
             impl/impl:wrap-sym

             invoke/definvoke
             invoke/invoke:arglists
             invoke/multi?
             invoke/multi:add
             invoke/multi:clone
             invoke/multi:get
             invoke/multi:list
             invoke/multi:remove
             invoke/fn
             invoke/fn-body
             
             memoize/memoize
             memoize/memoize:clear
             memoize/memoize:disable
             memoize/memoize:disabled?
             memoize/memoize:enable
             memoize/memoize:enabled?
             memoize/memoize:info
             memoize/memoize:invoke
             memoize/memoize:remove
             memoize/memoize:status

             origin/defn.origin
             origin/get-origin
             origin/set-origin
             origin/unset-origin
             origin/clear-origin
             
             resource/res
             resource/res:active
             resource/res:exists?
             resource/res:mode
             resource/res:path
             resource/res:restart
             resource/res:set
             resource/res:spec-add
             resource/res:spec-get
             resource/res:spec-list
             resource/res:spec-remove
             resource/res:start
             resource/res:stop
             resource/res:variant-add
             resource/res:variant-get
             resource/res:variant-list
             resource/res:variant-remove

             [return:error  return/get-error]
             [return:error? return/has-error?]
             [return:value return/get-value]
             [return:status return/get-status]
             [return:metadata return/get-metadata]
             [return:container? return/is-container?]
             [return:chain return/return-chain]
             [return:resolve return/return-resolve]

             set/difference
             set/union
             set/intersection
             set/subset?
             set/superset?

             signal/signal
             signal/signal:clear
             signal/signal:install
             signal/signal:uninstall
             signal/signal:list
             signal/signal:with-temp

             sort/hierarchical-sort
             sort/topological-sort

             template/$
             template/deftemplate
             template/template:locals
             
             trace/add-base-trace
             trace/add-print-trace
             trace/add-stack-trace
             trace/remove-trace
             trace/output-trace
             trace/get-trace
             trace/trace-ns
             trace/trace-print-ns
             trace/untrace-ns)

(f/intern-in security/sha1)

(f/intern-in
 [protocol-tmpl space/protocol-tmpl]
 [p:registry-list reg/registry-list]
 [p:registry-get reg/registry-get]
 [p:registry-install reg/registry-install]
 [p:registry-uninstall reg/registry-uninstall]
 [p:registry-rt reg/registry-rt]
 [p:registry-rt-add reg/registry-rt-add]
 [p:registry-rt-remove reg/registry-rt-remove]
 [p:registry-rt-list reg/registry-rt-list]
 [p:registry-scratch reg/registry-scratch]
 
 [p:space-create space/space-create]
 [p:space-resolve space/space-resolve]
 [p:space space/space]
 [p:space? space/space?]
 [p:space-context-get space/space:context-get]
 [p:space-context-list space/space:context-list]
 [p:space-context-set space/space:context-set]
 [p:space-context-unset space/space:context-unset]
 [p:space-rt-active space/space:rt-active]
 [p:space-rt-get space/space:rt-get]
 [p:space-rt-start space/space:rt-start]
 [p:space-rt-started? space/space:rt-started?]
 [p:space-rt-stop space/space:rt-stop]
 [p:space-rt-stopped? space/space:rt-stopped?]
 [p:space-rt-current space/space:rt-current]

 [p:rt-raw-eval ptr/rt-raw-eval]
 [p:rt-deref-ptr ptr/rt-deref-ptr]
 [p:rt-display-ptr ptr/rt-display-ptr]
 [p:rt-invoke-ptr ptr/rt-invoke-ptr]
 [p:rt-init-ptr ptr/rt-init-ptr]
 [p:rt-tags-ptr ptr/rt-tags-ptr]
 [p:rt-transform-in-ptr ptr/rt-transform-in-ptr]
 [p:rt-transform-out-ptr ptr/rt-transform-out-ptr]
 [p:rt-has-module? ptr/rt-has-module?]
 [p:rt-has-ptr? ptr/rt-has-ptr?]
 [p:rt-setup-module ptr/rt-setup-module]
 [p:rt-teardown-module ptr/rt-teardown-module]
 [p:rt-setup-ptr ptr/rt-setup-ptr]
 [p:rt-teardown-ptr ptr/rt-teardown-ptr]
 [pointer ptr/pointer]
 [pointer? ptr/pointer?])
