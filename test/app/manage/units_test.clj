(ns app.manage.units-test
  (:require [app.manage.units :as units]
            [app.manage.server :as server]
            [app.manage.server-test :as http]
            [clojure.java.io :as io])
  (:use code.test))
(defn with-fixture
  [run]
  (let [root (.toFile (java.nio.file.Files/createTempDirectory
                       "unit-viewer-"
                       (make-array java.nio.file.attribute.FileAttribute 0)))
        project {:root (str root) :unit-analysis {:sources ["src"] :tests ["test"]}}]
    (try
      (.mkdirs (io/file root "src"))
      (.mkdirs (io/file root "test"))
      (spit
       (io/file root "src/logic.clj")
       "(ns sample.logic)\n(defn.js sum [x] (+ x 1))\n(defn.js pointer [] 1)\n(defn.js missing [] 2)\n")
      (spit
       (io/file root "test/logic_test.clj")
       "(ns sample.logic-test (:use code.test))\n(l/script- :js {:runtime :basic :require [[sample.logic :as t]]})\n^{:refer sample.logic/sum :id real}\n(fact \"semantic\" (!.js (t/sum 2)) => 3)\n^{:refer sample.logic/pointer}\n(fact \"pointer\" (l/ptr? t/pointer) => true)\n")
      (run project root)
      (finally (doseq [file (reverse (file-seq root))] (.delete file))))))
^{:refer app.manage.units/report :id runtime-evidence}
(fact "runtime evidence distinguishes semantic invocations and pointer-only checks"
  (with-fixture (fn [p _]
                  (let [rows (into {} (map (juxt :id identity) (:units (units/report p {}))))
                        detail (units/report p {:unit "sample.logic/sum"})]
                    [(get-in rows ["sample.logic/sum" :status])
                     (contains? (set (get-in rows ["sample.logic/pointer" :flags]))
                                :pointer-only-tests) (get-in rows ["sample.logic/missing" :flags])
                     (get-in detail [:tests 0 :runtime-evidence :invokes-unit])
                     (get-in detail [:tests 0 :runtime-evidence :scripts 0 :language])
                     (boolean (re-find #"defn.js sum" (get-in detail [:source :code])))])))
  =>
  [:linked-not-run true [:missing-test] true "js" true])
^{:refer app.manage.units/report :id refresh-and-duplicates}
(fact "duplicate fact IDs are visible and analysis updates after edits"
  (with-fixture (fn [p root]
                  (spit (io/file root "test/logic_test.clj")
                        "\n^{:refer sample.logic/sum :id real}\n(fact \"TODO\")\n"
                        :append
                        true)
                  (let [before (units/report p {:unit "sample.logic/sum"})]
                    (spit (io/file root "src/logic.clj")
                          "(ns sample.logic)\n(defn.js changed [] 3)\n")
                    [(set (:flags before)) (mapv :id (:units (units/report p {})))
                     (count (:diagnostics (units/report p {})))])))
  =>
  [#{:todo :no-assertions :duplicate-fact-id} ["sample.logic/changed"] 3])
^{:refer app.manage.units/files :id path-safety}
(fact "analysis rejects paths outside the project and ignores quoted assertions"
  (with-fixture (fn [p _] (try (units/files p ["../outside"]) false (catch Exception _ true))))
  =>
  true
  (units/assertion-count '(fact "quoted"
                            '(x
                               =>
                               y)))
  =>
  0)

^{:refer app.manage.units/report :id http-detail}
(fact "HTTP search and detail expose source, assertions and the actual runtime scaffold"
  (with-fixture
   (fn [p _]
     (let [s (server/session!
              (constantly (assoc p :id 'sample :targets {:app {:sources ["src"] :outputs []}})))]
       (try (let [[status report] (http/http s "GET" "/api/units?q=sum" nil nil)
                  [detail-status detail]
                  (http/http s "GET" "/api/units?unit=sample.logic%2Fsum" nil nil)
                  [missing _] (http/http s "GET" "/api/units?unit=absent" nil nil)]
              [status (mapv :id (:units report)) detail-status
               (get-in detail [:tests 0 :assertions])
               (get-in detail [:tests 0 :runtime-evidence :scripts 0 :runtime]) missing])
            (finally (server/stop! s))))))
  =>
  [200 ["sample.logic/sum"] 200 1 "basic" 404])

^{:refer app.manage.units/analyse-file :id read-only}
(fact "scanning never executes application or test forms and flags absent runtime scaffolds"
  (with-fixture (fn [p root]
                  (let [marker (io/file root "executed")]
                    (spit (io/file root "test/logic_test.clj")
                          (str "(ns sample.logic-test (:use code.test))\n"
                               "(spit " (pr-str (str marker))
                               " \"bad\")\n"
                               "^{:refer sample.logic/sum}\n(fact (sample.logic/sum 2) => map?)\n"))
                    (let [detail (units/report p {:unit "sample.logic/sum"})]
                      [(.exists marker) (set (:flags detail))]))))
  =>
  [false #{:missing-script-runtime :type-only-tests}])

^{:refer app.manage.units/report :id inferred-links}
(fact "facts without refer metadata remain inspectable as inferred call evidence"
  (with-fixture
    (fn [p root]
      (spit (io/file root "test/logic_test.clj") "\n(fact \"without metadata\" (!.js (t/missing)) => 2)\n" :append true)
      (let [detail (units/report p {:unit "sample.logic/missing"})]
        [(:flags detail) (get-in detail [:tests 0 :link-kind])
         (get-in detail [:tests 0 :runtime-evidence :invokes-unit])])) )
  => [[:missing-test-link] :inferred true])
