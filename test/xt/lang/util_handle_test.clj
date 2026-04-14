(ns xt.lang.util-handle-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :xtalk
  {:require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.util-handle :as handle]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.util-handle :as handle]
             [xt.lang.common-repl :as repl]]})

(defn.xt walk
  [obj pre-fn post-fn]
  (:= obj (pre-fn obj))
  (cond (xt/x:nil? obj)
        (return (post-fn obj))

        (xt/x:is-object? obj)
        (do (var out := {})
            (xt/for:object [[k v] obj]
              (xt/x:set-key out k (-/walk v pre-fn post-fn)))
            (return (post-fn out)))

        (xt/x:is-array? obj)
        (do (var out := [])
            (xt/for:array [e obj]
              (xt/x:arr-push out (-/walk e pre-fn post-fn)))
            (return (post-fn out)))

        :else
        (return (post-fn obj))))

(defn.xt get-spec
  [obj]
  (var spec-fn
       (fn [obj]
         (if (not (or (xt/x:is-object? obj)
                      (xt/x:is-array? obj)))
           (return (k/type-native obj))
           (return obj))))
  (return (-/walk obj k/identity spec-fn)))

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-handle/plugin-timing :added "4.0"}
(fact "plugin timing"
  ^:hidden
  
  (!.js
   (handle/plugin-timing {}))
  => {"output" {}, "name" "timing"}
  
  (!.lua
   (-/get-spec (handle/plugin-timing {})))
  => {"on_setup" "function", "output" {}, "on_reset" "function", "name" "string", "on_teardown" "function"})

^{:refer xt.lang.util-handle/plugin-counts :added "4.0"}
(fact "plugin counts"
  ^:hidden
  
  (!.js
   (handle/plugin-counts {}))
  => {"output" {"success" 0, "error" 0}, "name" "counts"}
  
  (!.lua
   (-/get-spec (handle/plugin-counts {})))
  => {"output" {"success" "number", "error" "number"},
      "on_error" "function",
      "on_reset" "function",
      "name" "string",
      "on_success" "function"})

^{:refer xt.lang.util-handle/to-handle-callback :added "4.0"}
(fact "adapts a cb map to the handle callback"
  ^:hidden
  
  (!.js
   (handle/to-handle-callback {:success "A"
                               :error   "B"
                               :finally "C"}))
  => {"on_error" "B", "on_teardown" "C", "on_success" "A"}

  (!.lua
   (handle/to-handle-callback {:success "A"
                               :error   "B"
                               :finally "C"}))
  => {"on_error" "B", "on_teardown" "C", "on_success" "A"})

^{:refer xt.lang.util-handle/new-handle :added "4.0"}
(fact "creates a new handle"
  ^:hidden
  
  (notify/wait-on :js
    (var T (handle/new-handle (fn [] (return 1))
                            [handle/plugin-counts
                            handle/plugin-timing]
                           {:delay 100}))
    (var result (handle/run-handle T []
                               {:on-teardown (fn []
                                               (repl/notify result))})))
  => (contains-in [{"id" "id-0", "counts" {"success" 1, "error" 0},
                    "timing" {"start" number? "end" number?}}])

  (notify/wait-on :lua
    (var T (handle/new-handle (fn [] (return 1))
                            [handle/plugin-counts
                             handle/plugin-timing]
                            {:delay 100}))
    (var result nil)
    (:= result (handle/run-handle T []
                              {:on-teardown (fn []
                                              (repl/notify (xt/x:first result)))})))
  => (contains-in {"id" "id-0", "counts" {"success" 1, "error" 0},
                   "timing" {"start" number? "end" number?}}))

^{:refer xt.lang.util-handle/run-handle :added "4.0"}
(fact "runs a handle")
