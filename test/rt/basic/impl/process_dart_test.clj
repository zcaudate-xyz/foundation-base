(ns rt.basic.impl.process-dart-test
  (:require [clojure.string :as str]
             [rt.basic.impl.process-dart :refer :all]
             [std.lang :as l]
             [std.lib.os :as os])
  (:use code.test))

^{:refer rt.basic.impl.process-dart/normalize-dart-source :added "4.1"}
(fact "preserves multiline call continuations when normalizing dart"

  (normalize-dart-source "void main() {\n  print(\n    foo(1)\n  )\n}")
  => "void main() {\n  print(\n    foo(1)\n  );\n}"

  (normalize-dart-source "var OPERATORS = {\n  \"neq\":\"!=\",\n  \"gt\":\">\"\n}")
  => "var OPERATORS = {\n  \"neq\":\"!=\",\n  \"gt\":\">\"\n};"

  (normalize-dart-source "encode_bool(b) {\n  return \"TRUE\";\n}")
  => "encode_bool(b) {\n  return \"TRUE\";\n}"

  (normalize-dart-source "swap_if_entry(rows, table_key, id, f) {\n  if(entry){\n    return new_entry\n  }\n  return entry\n}")
  => "swap_if_entry(rows, table_key, id, f) {\n  if(entry){\n    return new_entry;\n  }\n  return entry;\n}"

  (normalize-dart-source "make_listener_entry(listener_id, listener_type, callback, meta, pred) {\n  return <dynamic, dynamic>{\n    \"callback\":callback,\n    \"pred\":pred,\n    \"meta\":xtd.obj_assign(\n        <dynamic, dynamic>{\"listener/id\":listener_id,\"listener/type\":listener_type},\n        meta\n      )\n  };\n}")
  => "make_listener_entry(listener_id, listener_type, callback, meta, pred) {\n  return <dynamic, dynamic>{\n    \"callback\":callback,\n    \"pred\":pred,\n    \"meta\":xtd.obj_assign(\n        <dynamic, dynamic>{\"listener/id\":listener_id,\"listener/type\":listener_type},\n        meta\n      )\n  };\n}")

  (normalize-dart-source "xt.lang.common_data.arr_sort(xt.lang.event_log.list_listeners(xt.lang.event_log.new_log(<dynamic, dynamic>{\n  \"listeners\":<dynamic, dynamic>{\n    \"test1\":(id, data, t) {\n      \n    },\n    \"test2\":(id, data, t) {\n      \n    }\n  }\n})),xt.lang.common_lib.identity,(x, y) {\n  return (x).toString().compareTo((y).toString()) < 0;\n})")
  => "xt.lang.common_data.arr_sort(xt.lang.event_log.list_listeners(xt.lang.event_log.new_log(<dynamic, dynamic>{\n  \"listeners\":<dynamic, dynamic>{\n    \"test1\":(id, data, t) {\n      \n    },\n    \"test2\":(id, data, t) {\n      \n    }\n  }\n})),xt.lang.common_lib.identity,(x, y) {\n  return (x).toString().compareTo((y).toString()) < 0;\n});"

^{:refer rt.basic.impl.process-dart/ensure-dart-imports :added "4.1"}
(fact "hoists required Dart imports for standalone scripts"
  (ensure-dart-imports "void main() {\n  print(math.max(1, 2));\n  print(jsonEncode({\"a\": 1}));\n}")
  => "import 'dart:convert';\nimport 'dart:math' as math;\n\nvoid main() {\n  print(math.max(1, 2));\n  print(jsonEncode({\"a\": 1}));\n}")


^{:refer rt.basic.impl.process-dart/sh-exec-dart :added "4.1"}
(fact "executes dart twostep pipeline"
  (with-redefs [os/sh (fn [_] {:pid 1})
                os/sh-wait (fn [_] nil)
                os/sh-output (fn [_] {:exit 0 :out "42\n" :err ""})]
    (sh-exec-dart ["dart" "compile" "exe"] "void main() {}"
                  {:extension "dart"
                   :output-flag "-o"}))
  => "42")

^{:refer rt.basic.impl.process-dart/transform-form :added "4.1"}
(fact "wraps forms in standalone dart main"
  (-> (transform-form ['(+ 1 2)] {}) pr-str)
  => #"Future<void> main\(\) async"

  (-> (transform-form ['(+ 1 2)] {}) pr-str)
  => #"print"

  (-> (transform-form ['(notify 1) '(return-wrap (fn:> 1))] {}) pr-str)
  => #"await"

  (l/emit-as :dart [(transform-form ['(return-wrap (fn:> 1))] {})])
  => #"return_wrap"

  (str/includes? (pr-str (transform-form ['(for [(var i := 0) (< i 10) (:++ i)]
                                           (notify i))
                                          '(+ 1 2)]
                                         {}))
                 "await for")
  => false

  (let [out (l/emit-as :dart [(transform-form ['(do (var b 1)
                                                     [b])]
                                                   {})])]
    [(boolean (re-find #"Future\.sync\(thunk_" out))
     (boolean (re-find #"var b = 1;" out))
     (boolean (re-find #"return \[b\];" out))
     (boolean (re-find #"return \(\) \{" out))])
  => [true true true false]

  (let [out (l/emit-as :dart [(transform-form ['[(+ 1 2)
                                                  (+ 3 4)]]
                                                {})])]
    [(boolean (re-find #"var out_.*await Future\.sync" out))
     (boolean (re-find #"return \[1 \+ 2,3 \+ 4\];" out))
     (boolean (re-find #"await Future\.sync\(\) \{" out))])
  => [true true false])


^{:refer rt.basic.impl.process-dart/dart-package-imports :added "4.1"}
(fact "TODO")

^{:refer rt.basic.impl.process-dart/dart-package-root :added "4.1"}
(fact "TODO")

^{:refer rt.basic.impl.process-dart/dart-pubspec :added "4.1"}
(fact "TODO")

^{:refer rt.basic.impl.process-dart/ensure-dart-package-context :added "4.1"}
(fact "TODO")
