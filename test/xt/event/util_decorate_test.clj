(ns xt.event.util-handle-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:python :lua]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
              [xt.lang.spec-promise :as spec-promise]
              [xt.lang.common-repl :as repl]
             [xt.event.util-handle :as handle]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
             [xt.event.util-handle :as handle]
             [python.core.common-promise]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
             [xt.event.util-handle :as handle]
             [lua.core.common-promise]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.util-handle/incr-fn :added "4.1"}
(fact "TODO")

^{:refer xt.event.util-handle/plugin-timing :added "4.1"}
(fact "plugin timing"

  (!.js
   (handle/plugin-timing {}))
  => {"output" {}, "name" "timing"}

  (!.py
   (handle/plugin-timing {}))
  => {"output" {}, "name" "timing"}

  (!.lua
   (handle/plugin-timing {}))
  => {"output" {}, "name" "timing"})

^{:refer xt.event.util-handle/plugin-counts :added "4.1"}
(fact "plugin counts"

  (!.js
   (handle/plugin-counts {}))
  => {"output" {"success" 0, "error" 0}, "name" "counts"}

  (!.py
   (handle/plugin-counts {}))
  => {"output" {"success" 0, "error" 0}, "name" "counts"}

  (!.lua
   (handle/plugin-counts {}))
  => {"output" {"success" 0, "error" 0}, "name" "counts"})

^{:refer xt.event.util-handle/to-handle-callback :added "4.1"}
(fact "adapts callback maps"

  (!.js
   (handle/to-handle-callback {:success "A"
                               :error "B"
                               :finally "C"}))
  => {"on_error" "B", "on_teardown" "C", "on_success" "A"}

  (!.py
   (handle/to-handle-callback {:success "A"
                               :error "B"
                               :finally "C"}))
  => {"on_error" "B", "on_teardown" "C", "on_success" "A"}

  (!.lua
   (handle/to-handle-callback {:success "A"
                               :error "B"
                               :finally "C"}))
  => {"on_error" "B", "on_teardown" "C", "on_success" "A"})

^{:refer xt.event.util-handle/promise-wrap :added "4.1"}
(fact "TODO")

^{:refer xt.event.util-handle/new-handle :added "4.1"}
(fact "TODO")

^{:refer xt.event.util-handle/run-handle :added "4.1"}
(fact "rejects failed handle runs with the receipt"

  (notify/wait-on :js
    (var T (handle/new-handle (fn []
                                (xt/x:err "boom"))
                              [handle/plugin-counts]
                              {}))
    (spec-promise/x:promise-catch
     (handle/run-handle T [] nil)
     (fn [receipt]
       (repl/notify {"id" (. receipt ["id"])
                     "status" (. receipt ["status"])
                     "counts" (. receipt ["counts"])}))))
  => {"id" "id-0"
      "status" "error"
      "counts" {"success" 0, "error" 1}}

  (notify/wait-on :python
    (var T (handle/new-handle (fn []
                                (xt/x:err "boom"))
                              [handle/plugin-counts]
                              {}))
    (spec-promise/x:promise-catch
     (handle/run-handle T [] nil)
     (fn [receipt]
       (repl/notify {"id" (. receipt ["id"])
                     "status" (. receipt ["status"])
                     "counts" (. receipt ["counts"])}))))
  => {"id" "id-0"
      "status" "error"
      "counts" {"success" 0, "error" 1}}

  (notify/wait-on :lua
    (var T (handle/new-handle (fn []
                                (xt/x:err "boom"))
                              [handle/plugin-counts]
                              {}))
    (spec-promise/x:promise-catch
     (handle/run-handle T [] nil)
     (fn [receipt]
       (repl/notify {"id" (. receipt ["id"])
                     "status" (. receipt ["status"])
                     "counts" (. receipt ["counts"])}))))
  => {"id" "id-0"
      "status" "error"
      "counts" {"success" 0, "error" 1}})

(comment
  
  (s/seedgen-benchadd '[xt.event.util-handle] {:lang [:dart] :write true})
  
  (s/seedgen-langadd '[xt.event.util-handle] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.event.util-handle] {:lang [:lua :python] :write true}))

(comment
  
  )
