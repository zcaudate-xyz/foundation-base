(ns app.manage.process-test
  (:require [app.manage.process :as process] [clojure.java.io :as io])
  (:use code.test))

^{:refer app.manage.process/run :added "4.1"}
(fact "captures command failure and terminates a verification step on timeout"
  (let [root (.toFile (java.nio.file.Files/createTempDirectory "app-process-"
                       (make-array java.nio.file.attribute.FileAttribute 0)))
        java (str (io/file (System/getProperty "java.home") "bin/java"))
        source (io/file root "Wait.java")]
    (try
      (spit source "class Wait { public static void main(String[] args) throws Exception { Thread.sleep(10000); } }")
      (let [failed (process/run {:root root :command [java "--invalid-app-manage-option"]})
            timed (process/run {:root root :command [java (str source)] :timeout-ms 100})]
        [(:status failed) (pos? (:exit failed)) (:timed-out failed)
         (:status timed) (:timed-out timed)])
      (finally (.delete source) (.delete root))))
  => [:error true false :error true])
