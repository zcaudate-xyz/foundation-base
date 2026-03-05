(ns bb.lang.dev
  (:require [bb.lib.link :as l]))

(defn reload-specs
  "reloads the specs"
  {:added "4.0"}
  []
  (require 'bb.lang.base.grammar-spec :reload)
  (require 'bb.lang.base.grammar-xtalk :reload)
  (require 'bb.lang.base.grammar :reload)
  (require 'bb.lang.model.spec-xtalk.com-js :reload)
  (require 'bb.lang.model.spec-xtalk.com-lua :reload)
  (require 'bb.lang.model.spec-xtalk.com-python :reload)
  (require 'bb.lang.model.spec-xtalk.com-r :reload)
  (require 'bb.lang.model.spec-xtalk.fn-js :reload)
  (require 'bb.lang.model.spec-xtalk.fn-lua :reload)
  (require 'bb.lang.model.spec-xtalk.fn-python :reload)
  (require 'bb.lang.model.spec-xtalk.fn-r :reload)
  (require 'bb.lang.model.spec-js :reload)
  (require 'bb.lang.model.spec-lua :reload)
  (require 'bb.lang.model.spec-python :reload)
  (require 'bb.lang.model.spec-r :reload)
  (require 'xt.lang.base-lib :reload))

(comment
  (s/reload-specs)
  )
