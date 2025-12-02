(ns code.maven.lein-test
  (:use code.test)
  (:require [code.maven.lein :refer :all]))

^{:refer code.maven.lein/deploy-lein :added "4.0"}
(fact "temporary hack to deploy by shelling out to leiningen"
  (with-redefs [std.lib/sh (constantly "out")]
    (deploy-lein 'foo {} nil {:root "root" :version "1.0.0"}))
  => (contains {:results "out"}))
