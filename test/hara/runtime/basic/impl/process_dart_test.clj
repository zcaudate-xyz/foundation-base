(ns hara.runtime.basic.impl.process-dart-test
  (:require [clojure.string :as str]
             [hara.runtime.basic.impl.process-dart :refer :all]
             [hara.lang :as l]
             [std.lib.os :as os]
             [std.lib.env :as env]
             [std.fs :as fs])
  (:use code.test))

(fact:global
 {:skip (not (env/program-exists? "dart"))})

^{:refer hara.runtime.basic.impl.process-dart/normalize-dart-source :added "4.1"}
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

^{:refer hara.runtime.basic.impl.process-dart/ensure-dart-imports :added "4.1"}
(fact "hoists required Dart imports for standalone scripts"
  (ensure-dart-imports "void main() {\n  print(math.max(1, 2));\n  print(jsonEncode({\"a\": 1}));\n}")
  => "import 'dart:convert';\nimport 'dart:math' as math;\n\nvoid main() {\n  print(math.max(1, 2));\n  print(jsonEncode({\"a\": 1}));\n}")


^{:refer hara.runtime.basic.impl.process-dart/sh-exec-dart :added "4.1"}
(fact "executes dart twostep pipeline"
  (with-redefs [os/sh (fn [_] {:pid 1})
                os/sh-wait (fn [_] nil)
                os/sh-output (fn [_] {:exit 0 :out "42\n" :err ""})]
    (sh-exec-dart ["dart" "compile" "exe"] "void main() {}"
                  {:extension "dart"
                   :output-flag "-o"}))
  => "42")

^{:refer hara.runtime.basic.impl.process-dart/transform-form :added "4.1"}
(fact "wraps forms in standalone dart main"
  (-> (transform-form ['(+ 1 2)] {:bulk true}) pr-str)
  => #"Future<void> main\(\) async"

  (-> (transform-form ['(+ 1 2)] {:bulk true}) pr-str)
  => #"print"

  (-> (transform-form ['(notify 1) '(return-wrap (fn:> 1))] {:bulk true}) pr-str)
  => #"await"

  (l/emit-as :dart [(transform-form ['(return-wrap (fn:> 1))] {:bulk true})])
  => #"return_wrap"

  (str/includes? (pr-str (transform-form ['(for [(var i := 0) (< i 10) (:++ i)]
                                            (notify i))
                                           '(+ 1 2)]
                                          {:bulk true}))
                 "await for")
  => false

  (let [out (l/emit-as :dart [(transform-form ['(do (var b 1)
                                                      [b])]
                                                    {:bulk true})])]
    [(boolean (re-find #"Future\.sync\(thunk_" out))
     (boolean (re-find #"var b = 1;" out))
     (boolean (re-find #"return \[b\];" out))
     (boolean (re-find #"return \(\) \{" out))])
  => [true true true false]

  (let [out (l/emit-as :dart [(transform-form '[(+ 1 2)
                                                 (+ 3 4)]
                                               {})])]
    [(boolean (re-find #"var out_.*await Future\.sync" out))
     (boolean (re-find #"return \[1 \+ 2,3 \+ 4\];" out))
     (boolean (re-find #"await Future\.sync\(\) \{" out))])
  => [true true false]

  (let [out (l/emit-as :dart [(transform-form '[[(do (var b 1)
                                                       b)
                                                      (+ 1 2)]]
                                                 {})])]
    [(boolean (re-find #"var b = 1;" out))
     (boolean (re-find #"var expr_\d+ = b;" out))
     (boolean (re-find #"return \[\(do" out))])
  => [true true false]

  (let [out (l/emit-as :dart [(transform-form ['(throw "boom")] {:bulk true})])]
    [(boolean (re-find #"catch\(err\)" out))
     (boolean (re-find #"\"type\":\"error\"" out))
     (boolean (re-find #"err\.toString\(\)" out))])
  => [true true true])


^{:refer hara.runtime.basic.impl.process-dart/dart-package-imports :added "4.1"}
(fact "finds package imports referenced by generated Dart source"
  (dart-package-imports "")
  => []

  (dart-package-imports "import 'dart:io';")
  => []

  (dart-package-imports "package:foo/bar.dart")
  => ["foo"]

  (dart-package-imports "import 'package:foo/bar.dart';\nimport 'package:baz/qux.dart';\nimport 'package:foo/other.dart';")
  => ["baz" "foo"]

  (dart-package-imports "x package:abc/def x package:ghi/jkl")
  => ["abc" "ghi"])

^{:refer hara.runtime.basic.impl.process-dart/dart-package-root :added "4.1"}
(fact "returns cached Dart package root for twostep scripts"
  (dart-package-root "/tmp/root")
  => "/tmp/root/target/dart-twostep"

  (dart-package-root nil)
  => (str (java.io.File. (System/getProperty "user.dir") "target/dart-twostep")))

^{:refer hara.runtime.basic.impl.process-dart/dart-pubspec :added "4.1"}
(fact "creates minimal pubspec for generated twostep scripts"
  (dart-pubspec [])
  => (str "name: foundation_base_dart_twostep\n"
          "publish_to: 'none'\n"
          "environment:\n"
          "  sdk: '>=3.0.0 <4.0.0'\n")

  (dart-pubspec ["foo" "bar"])
  => (str "name: foundation_base_dart_twostep\n"
          "publish_to: 'none'\n"
          "environment:\n"
          "  sdk: '>=3.0.0 <4.0.0'\n"
          "dependencies:\n"
          "  foo: any\n"
          "  bar: any\n"))

^{:refer hara.runtime.basic.impl.process-dart/ensure-dart-package-context :added "4.1"}
(fact "creates cached package root and resolves dependencies"
  (ensure-dart-package-context nil "")
  => nil

  (let [root (str (fs/create-tmpdir "dart-ctx-test-"))]
    (try
      (with-redefs [os/sh (fn [_] {:pid 1})
                    os/sh-wait (fn [_] nil)
                    os/sh-output (fn [_] {:exit 0 :out "" :err ""})]
        (let [source "import 'package:foo/bar.dart';"
              result (ensure-dart-package-context root source)
              pubspec (str root "/target/dart-twostep/pubspec.yaml")]
          [result
           (.exists (java.io.File. pubspec))
           (slurp pubspec)]))
      => [(str root "/target/dart-twostep")
          true
          (str "name: foundation_base_dart_twostep\n"
               "publish_to: 'none'\n"
               "environment:\n"
               "  sdk: '>=3.0.0 <4.0.0'\n"
               "dependencies:\n"
               "  foo: any\n")]
      (finally
        (try (fs/delete root)
             (catch Throwable _))))))
