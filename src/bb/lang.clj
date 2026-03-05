(ns bb.lang
  (:require [bb.lang.base.pointer :as ptr]
            [bb.lang.base.compile :as compile]
            [bb.lang.base.impl-lifecycle :as lifecycle]
            [bb.lang.base.impl-entry :as entry]
            [bb.lang.base.impl :as impl]
            [bb.lang.base.library :as lib]
            [bb.lang.base.manage :as manage]
            [bb.lang.base.registry :as registry]
            [bb.lang.base.runtime :as runtime]
            [bb.lang.base.emit :as emit]
            [bb.lang.base.emit-preprocess :as preprocess]
            [bb.lang.base.emit-common :as common]
            [bb.lang.base.emit-helper :as helper]
            [bb.lang.base.util :as ut]
            [bb.lang.base.book :as book]
            [bb.lang.base.script-def :as script-def]
            [bb.lang.base.script-control :as script-control]
            [bb.lang.base.script-lint :as lint]
            [bb.lang.base.script-annex :as annex]
            [bb.lang.base.script-macro :as macro]
            [bb.lang.base.script :as script]
            [bb.lang.base.workspace :as workspace]
            [bb.lang.interface.type-notify :as notify]
            [bb.lang.model.spec-c]
            [bb.lang.model.spec-bash]
            [bb.lang.model.spec-glsl]
            [bb.lang.model.spec-js]
            [bb.lang.model.spec-lua]
            [bb.lang.model.spec-python]
            [bb.lang.model.spec-perl]
            [bb.lang.model.spec-php]
            [bb.lang.model.spec-r]
            [bb.lang.model.spec-ruby]
            [bb.lang.model.spec-rust]
            [bb.lang.model.spec-xtalk]
            [bb.lang.interface.type-shared :as shared]
            [std.lib :as h])
  (:refer-clojure :exclude [test]))

(h/intern-in
 ut/sym-full
 ut/sym-id
 ut/sym-module
 ut/sym-pair
 ut/sym-default-str
 [ptr ut/lang-pointer]

 common/with:explode
 common/with-trace
 emit/with:emit
 [emit* emit/emit-main]
 helper/basic-typed-args
 helper/emit-type-record

 preprocess/macro-form
 preprocess/macro-opts
 preprocess/macro-grammar
 preprocess/with:macro-opts

 impl/emit-script
 impl/emit-str
 impl/emit-as
 impl/emit-symbol
 entry/emit-entry
 impl/emit-entry-deps

 impl/default-library
 impl/default-library:reset
 impl/runtime-library
 impl/with:library
 impl/grammar

 notify/default-notify
 notify/default-notify:reset
 [notify-get   notify/get-sink]
 [notify-clear notify/clear-sink]
 [notify-add-listener    notify/add-listener]
 [notify-remove-listener notify/remove-listener]

 ptr/with:print
 ptr/with:print-all
 ptr/with:clip
 ptr/with:input
 ptr/with:raw
 ptr/with:rt
 ptr/with:rt-wrap
 [rt:macro-opts ptr/rt-macro-opts]

 entry/with:cache-none
 entry/with:cache-force

 script/script
 script/script-
 script/script+

 script/!
 #_#_
 script/!.async
 script/!.run
 macro/defmacro.!

 annex/annex-current
 annex/annex-reset
 script/annex:get
 script/annex:start
 script/annex:stop
 script/annex:restart-all
 script/annex:start-all
 script/annex:stop-all
 script/annex:list
 script-def/tmpl-entry
 script-def/tmpl-macro

 lib/get-book-raw
 lib/get-book
 lib/get-module
 lib/get-snapshot
 lib/delete-module!
 lib/delete-modules!
 lib/delete-entry!
 lib/purge-book!

 lint/lint-set
 lint/lint-clear

 [rt ut/lang-rt]
 [rt:list ut/lang-rt-list]
 [rt:default ut/lang-rt-default]
 [rt:restart script-control/script-rt-restart]
 [rt:stop script-control/script-rt-stop]

 workspace/sym-entry
 workspace/module-entries
 workspace/emit-ptr
 workspace/emit-module
 workspace/print-module

 workspace/ptr-clip
 workspace/ptr-display-str
 workspace/ptr-print
 workspace/ptr-setup
 workspace/ptr-teardown
 workspace/ptr-setup-deps
 workspace/ptr-teardown-deps
 workspace/rt:module
 workspace/rt:module-meta
 workspace/rt:module-purge
 workspace/rt:inner
 workspace/rt:restart
 workspace/rt:setup
 workspace/rt:setup-to
 workspace/rt:setup-single
 workspace/rt:scaffold
 workspace/rt:scaffold-to
 workspace/rt:scaffold-imports
 workspace/rt:teardown
 workspace/rt:teardown-at
 workspace/rt:teardown-single
 workspace/rt:teardown-to
 workspace/intern-macros

 [lib:overview manage/lib-overview]
 [lib:module   manage/lib-module-overview]
 [lib:entries  manage/lib-module-entries]
 [lib:purge    manage/lib-module-purge]
 [lib:unused   manage/lib-module-unused])

(defn rt:space
  "will return space if not found (no default space)"
  {:added "4.0"}
  [lang & [namespace]]
  (std.lib.context.space/space:rt-get
   (std.lib.context.space/space (or namespace *ns*))
   (ut/lang-context lang)))

(defn get-entry
  "gets the entry if pointer"
  {:added "4.0"}
  [m]
  (if (book/book-entry? m)
    m
    (ptr/get-entry m)))

(defn as-lua
  "change `[]` to `{}`"
  {:added "4.0"}
  [input]
  (h/prewalk (fn [form]
               (if (and (vector? form)
                        (empty? form))
                 {}
                 form))
             input))

(defn rt:invoke
  "invokes code in the given namespace"
  {:added "4.0"}
  [ns lang code]
  (h/p:rt-invoke-ptr
   (ut/lang-rt ns lang)
   (ptr lang {:module ns})
   code))

(defn force-reload
  "forces reloading of all dependent namespaces"
  {:added "4.0"}
  ([ns lang]
   (doseq [ns (h/deps:ordered (get-book (default-library)
                                        lang)
                             [ns])]
     (lib:purge ns)
     (eval (list 'jvm.namespace/clear ns))
     (require ns :reload)
     (h/p :RELOADED ns))))


(comment

  (lib:module '[statsdb])
  (lib:entries '[statsdb])
  (lib:purge '[lua])
  (./reset '[lua])
  (do (./reset '[statsdb])
      (delete-modules!
       (default-library)
       :postgres
       (->> (:modules (get-book (default-library)
                                  :postgres
                                  ))
            (keys)
            (filter (fn [n]
                      (std.string/starts-with? (str n) "stats")))))
      (require ['statsdb.core.execute])
      (std.make/build play.tui-counter-basic.main/PROJECT
                      :statsdb))

  (emit-as
   :js '[(if (->> a b c)
           a b)])

  (get (:reserved (grammar :xtalk))
       '->>))
