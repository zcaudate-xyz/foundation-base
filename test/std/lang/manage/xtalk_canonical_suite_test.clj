(ns std.lang.manage.xtalk-canonical-suite-test
  (:require [clojure.string :as str]
            [std.lang.manage.xtalk-scaffold :as scaffold])
  (:use code.test))

(def canonical-runtime-source
  "(ns xt.lang.sample-lua-test
     (:require [std.lang :as l]
               [xt.lang.common-lib :as k])
     (:use code.test))
   (l/script- :lua {:runtime :basic})
   ^{:lang-exceptions {:dart {:expect 30}
                       :go {:skip true}}}
   (fact \"simple lua case\"
     (!.lua (+ 10 20))
     => 30)
   (fact \"lua vector case\"
     ^{:lang-exceptions {:dart {:form [(* 2 3) (* 3 4)]}}}
     [(!.lua (+ 1 2))
      (!.lua (+ 3 4))]
     => [3 7])")

(defn with-temp-clj-file
  [content f]
  (let [file (java.io.File/createTempFile "xtalk-canonical-suite" ".clj")
        path (.getAbsolutePath file)]
    (try
      (spit path content)
      (f path)
      (finally
        (.delete file)))))

^{:refer std.lang.manage.xtalk-scaffold/read-top-level-forms :added "4.1"}
(fact "canonical runtime readers preserve line metadata"
  (with-temp-clj-file
    canonical-runtime-source
    (fn [path]
      (let [forms (scaffold/read-top-level-forms path)
            fact-form (some #(when (and (seq? %) (= 'fact (first %))) %) forms)]
        [(integer? (:line (meta fact-form)))
         (integer? (:line (meta (nth fact-form 2))))])))
  => [true true])

^{:refer std.lang.manage.xtalk-scaffold/export-runtime-suite :added "4.1"}
(fact "exports canonical lua runtime tests to EDN cases with line info and exceptions"
  (with-temp-clj-file
    canonical-runtime-source
    (fn [path]
      (let [{:keys [suite count output-path]} (scaffold/export-runtime-suite nil {:input-path path
                                                                                   :lang :lua})]
        [(= 2 count)
         (= :lua (:lang suite))
         (str/ends-with? output-path "_suite.edn")
         (= '(+ 10 20) (:form (first (:cases suite))))
         (integer? (get-in suite [:cases 0 :line :line]))
         (= {:dart {:expect 30}
             :go {:skip true}}
            (get-in suite [:cases 0 :exceptions]))
         (= ['(+ 10 20)
             '[(+ 1 2) (+ 3 4)]]
            (mapv :form (:cases suite)))])))
  => [true true true true true true true])

^{:refer std.lang.manage.xtalk-scaffold/compile-runtime-bulk-suite :added "4.1"}
(fact "compiles canonical EDN suites into batched runtime payloads"
  (with-temp-clj-file
    canonical-runtime-source
    (fn [path]
      (let [{:keys [suite]} (scaffold/export-runtime-suite nil {:input-path path
                                                                :lang :lua})
            dart-bulk (scaffold/compile-runtime-bulk-suite suite :dart)
            go-bulk (scaffold/compile-runtime-bulk-suite suite :go)]
        [(= :batched (:check-mode dart-bulk))
         (= :twostep (:runtime-type dart-bulk))
         (= '(+ 10 20) (get-in dart-bulk [:bulk-form 0 :value]))
         (= [30 [3 7]]
            (mapv :expect (:verify dart-bulk)))
         (= '[(* 2 3) (* 3 4)]
            (get-in dart-bulk [:bulk-form 1 :value]))
         (= 1 (count (:verify go-bulk)))
         (= 1 (count (:skipped go-bulk)))
         (= "xt.lang.sample-lua-test::lua-vector-case::0"
            (get-in go-bulk [:verify 0 :id]))])))
  => [true true true true true true true true])

^{:refer std.lang.manage.xtalk-scaffold/compile-runtime-bulk :added "4.1"}
(fact "compiles a written canonical suite file into a bulk EDN payload"
  (with-temp-clj-file
    canonical-runtime-source
    (fn [path]
      (let [{:keys [output-path]} (scaffold/export-runtime-suite nil {:input-path path
                                                                      :lang :lua
                                                                      :write true})
            {:keys [bulk count]} (scaffold/compile-runtime-bulk nil {:input-path output-path
                                                                     :lang :dart})]
        [(str/ends-with? output-path "_suite.edn")
         (= 2 count)
         (= :dart (:lang bulk))
         (= :batched (:check-mode bulk))])))
  => [true true true true])
