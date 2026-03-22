(ns std.config.ext.toml-test
  (:require [script.toml :as toml]
            [std.config.ext.toml :refer :all])
  (:use code.test))

^{:refer std.config.ext.toml/resolve-type-toml :added "3.0"}
(fact "resolves toml config"

  (resolve-type-toml nil (toml/write {:a 1 :b 2}))
  => {:a 1, :b 2})
