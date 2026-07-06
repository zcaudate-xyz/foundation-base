(ns documentation.xt-lang
  (:use code.test))

[[:hero {:title "xt.lang"
         :subtitle "Portable language primitives and common libraries."
         :lead "`xt.lang` defines reusable xtalk libraries that target JS, Lua, Python, Dart, and other runtimes through hara.lang emission."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Target languages differ in collection APIs, nil handling, string operations, promises, modules, and resource access. `xt.lang` gives generated programs one shared vocabulary for those behaviors."

[[:chapter {:title "How to use it" :link "usage"}]]

"A hara script requires the libraries it needs, then emitted xtalk code calls those portable helpers. Application examples in `src-build/play/*xtalk*` and tests under `test-lang/xt/lang` show this pattern."

(comment
  (l/script :xtalk
    {:require [[xt.lang.spec-base :as xt]
               [xt.lang.common-data :as data]
               [xt.lang.common-string :as string]]}))

[[:chapter {:title "Internal usage" :link "internal"}]]

"The common libraries are used by xt.db, xt.event, xt.net, xt.substrate, and by generated single-source examples. The `test-lang/xtbench` tests exercise cross-target parity for these helpers."

[[:chapter {:title "API" :link "api"}]]

