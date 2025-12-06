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
      (require '[js.core :refer [defn.js]]))

    (loader/eval-in-library '(defn.js hello [] (return "world")) lib 'my.test.module)
    (let [book (lib/get-book lib :js)
          module (get-in book [:modules 'my.test.module])
          entry (get-in module [:code 'hello])]
      (:op entry) => 'defn
      (:form entry) => '(defn hello [] (return "world")))))

^{:refer std.lang.base.library-load/load-string-into-library :added "4.1"}
(t/fact "load-string-into-library loads module and code into isolated library"
  (let [lib (impl/clone-default-library)
        code "(ns my.test.module (:require [std.lang :as l] [js.core :as j]))
              (l/script :js {:require [[js.core :as j]]})
              (j/defn.js hello [] (return \"world\"))"]

    (loader/load-string-into-library code lib 'my.test.module)

    (let [book (lib/get-book lib :js)
          module (get-in book [:modules 'my.test.module])
          entry (get-in module [:code 'hello])]

      (:op entry) => 'defn
      (:lang entry) => :js
      (:form entry) => '(defn hello [] (return "world")))))

^{:refer std.lang.base.library-load/load-file-into-library :added "4.1"}
(t/fact "Loads a file into a specific library instance"
  (let [lib (impl/clone-default-library)
        content "(ns my.test.file-load (:require [js.core :as j])) (j/defn.js file-fn [] (return true))"]

    (with-redefs [slurp (constantly content)]
      (loader/load-file-into-library "dummy.clj" lib)

      (let [book (lib/get-book lib :js)
            module (get-in book [:modules 'my.test.file-load])
            entry (get-in module [:code 'file-fn])]
        (:form entry) => '(defn file-fn [] (return true))))))

^{:refer std.lang.base.library-load/clone-and-load :added "4.1"}
(t/fact "Clones the default library and loads the given file into it"
  (let [content "(ns my.test.clone-load (:require [js.core :as j])) (j/defn.js clone-fn [] (return 1))"]
    (with-redefs [slurp (constantly content)]
      (let [lib (loader/clone-and-load "dummy.clj")
            book (lib/get-book lib :js)
            module (get-in book [:modules 'my.test.clone-load])
            entry (get-in module [:code 'clone-fn])]
        (:form entry) => '(defn clone-fn [] (return 1))))))

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
