(ns code.tool.maven.lein-test
  (:require [code.tool.maven.lein :refer :all]
            [std.lib.os :as os])
  (:use code.test))

^{:refer code.tool.maven.lein/deploy-lein :added "4.0"}
(fact "temporary hack to deploy by shelling out to leiningen"
  (with-redefs [os/sh (constantly "out")]
    (deploy-lein 'foo {} nil {:root "root" :version "1.0.0"}))
  => (contains {:results "out"}))
