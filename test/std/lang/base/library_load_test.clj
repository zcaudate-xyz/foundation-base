(ns std.lang.base.library-load-test
  (:require [std.lang.base.library-load :as loader]
            [std.lang.base.library :as lib]
            [std.lang.base.impl :as impl]
            [code.test :as t]
            [js.core]))

^{:refer std.lang.base.library-load/eval-in-library :added "4.1"}
(t/fact "Evaluates a form within the context of a specific library instance"
  (let [lib (impl/clone-default-library)]
    ;; Ensure the namespace exists and has necessary requirements
    (create-ns 'my.test.module)
    (binding [*ns* (find-ns 'my.test.module)]
      (clojure.core/refer-clojure)
      (require '[std.lang :as l])
      ;; We don't require defn.js here because we test standard clojure form or manual setup
    )

    (loader/eval-in-library '(defn hello [] "world") lib 'my.test.module)
    ;; Since we used standard defn, it won't be in the book, but in the namespace
    (binding [*ns* (find-ns 'my.test.module)]
      (eval '(hello))) => "world"))

^{:refer std.lang.base.library-load/load-string-into-library :added "4.1"}
(t/fact "load-string-into-library loads module and code into isolated library"
  (let [lib (impl/clone-default-library)
        ;; Use standard clojure forms or ensure l/script is handled if we want hydration
        ;; For now, testing standard form loading which uses eval-in-library
        code "(ns my.test.module-str (:require [std.lang :as l]))
              (defn hello [] \"world\")"]

    (loader/load-string-into-library code lib 'my.test.module-str)

    (binding [*ns* (find-ns 'my.test.module-str)]
      (eval '(hello))) => "world"))

^{:refer std.lang.base.library-load/load-file-into-library :added "4.1"}
(t/fact "Loads a file into a specific library instance"
  (let [lib (impl/clone-default-library)
        content "(ns my.test.file-load) (defn file-fn [] true)"]

    (with-redefs [slurp (constantly content)]
      (loader/load-file-into-library "dummy.clj" lib)

      (binding [*ns* (find-ns 'my.test.file-load)]
        (eval '(file-fn))) => true)))

^{:refer std.lang.base.library-load/analyze-string :added "4.1"}
(t/fact "analyze-string extracts dependencies"
  (let [code "(ns my.test.module
                (:require [std.lang :as l]
                          [clojure.string :as str]))
              (l/script :js
                {:require [[js.core :as j]
                           [js.react :as r]]})"]
    (loader/analyze-string code)
    => (t/contains {:ns 'my.test.module
                    :requires #{'std.lang 'clojure.string}
                    :std-requires #{'js.core 'js.react}})))

^{:refer std.lang.base.library-load/analyze-file :added "4.1"}
(t/fact "Analyzes a file to find namespace and dependencies"
  (let [content "(ns my.file)"]
    (with-redefs [slurp (constantly content)]
      (loader/analyze-file "my/file.clj")
      => (t/contains {:ns 'my.file :file "my/file.clj"}))))

^{:refer std.lang.base.library-load/create-dependency-graph :added "4.1"}
(t/fact "create-dependency-graph builds graph from files"
  (let [file1 "test-data/dep-graph/a.clj"
        file2 "test-data/dep-graph/b.clj"
        content1 "(ns dep.a (:require [dep.b]))"
        content2 "(ns dep.b)"]

    (with-redefs [loader/analyze-file (fn [f]
                                        (cond
                                          (= f file1) {:ns 'dep.a :requires #{'dep.b} :std-requires #{}}
                                          (= f file2) {:ns 'dep.b :requires #{} :std-requires #{}}))]
      (loader/create-dependency-graph [file1 file2])
      => {'dep.a #{'dep.b}
          'dep.b #{}})))

^{:refer std.lang.base.library-load/load-namespace :added "4.1"}
(t/fact "Recursively loads a namespace and its dependencies"
  (let [lib (impl/clone-default-library)
        loaded-files (atom [])]

    (with-redefs [code.project/get-path (fn [ns _] (str "src/" ns ".clj"))
                  loader/analyze-file (fn [f]
                                        (cond
                                          (= f "src/root.clj") {:ns 'root :requires #{'dep1} :std-requires #{}}
                                          (= f "src/dep1.clj") {:ns 'dep1 :requires #{'dep2} :std-requires #{}}
                                          (= f "src/dep2.clj") {:ns 'dep2 :requires #{} :std-requires #{}}))
                  loader/load-file-into-library (fn [f _] (swap! loaded-files conj f))]

      (loader/load-namespace lib 'root)

      @loaded-files => ["src/dep2.clj" "src/dep1.clj" "src/root.clj"])))
