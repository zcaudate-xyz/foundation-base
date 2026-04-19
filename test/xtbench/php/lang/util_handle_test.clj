(ns
 xtbench.php.lang.util-handle-test
 (:require [std.lang :as l] [xt.lang.common-notify :as notify])
 (:use code.test))

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-handle/plugin-timing, :added "4.0"}
(fact
 "plugin timing"
 ^{:hidden true}
 (!.php (handle/plugin-timing {}))
 =>
 {"output" {}, "name" "timing"})

^{:refer xt.lang.util-handle/plugin-counts, :added "4.0"}
(fact
 "plugin counts"
 ^{:hidden true}
 (!.php (handle/plugin-counts {}))
 =>
 {"output" {"success" 0, "error" 0}, "name" "counts"})

^{:refer xt.lang.util-handle/to-handle-callback, :added "4.0"}
(fact
 "adapts a cb map to the handle callback"
 ^{:hidden true}
 (!.php
  (handle/to-handle-callback {:success "A", :error "B", :finally "C"}))
 =>
 {"on_error" "B", "on_teardown" "C", "on_success" "A"})

^{:refer xt.lang.util-handle/new-handle, :added "4.0"}
(fact
 "creates a new handle"
 ^{:hidden true}
 (notify/wait-on
  :php
  (var
   T
   (handle/new-handle
    (fn [] (return 1))
    [handle/plugin-counts handle/plugin-timing]
    {:delay 100}))
  (var
   result
   (handle/run-handle
    T
    []
    {:on-teardown (fn [] (repl/notify result))})))
 =>
 (contains-in
  [{"id" "id-0",
    "counts" {"success" 1, "error" 0},
    "timing" {"start" number?, "end" number?}}]))
