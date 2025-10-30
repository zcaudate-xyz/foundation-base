(ns rt.basic.type-basic-test
  (:use code.test)
  (:require [rt.basic.type-basic :refer :all]
            [rt.basic.server-basic :as server]
            [rt.basic.impl.process-lua :as lua]
            [rt.basic.impl.process-js :as js]
            [std.lib :as h]))

^{:refer rt.basic.type-basic/start-basic :added "4.0"}
(fact "starts the basic rt")

^{:refer rt.basic.type-basic/stop-basic :added "4.0"}
(fact "stops the basic rt")

^{:refer rt.basic.type-basic/raw-eval-basic :added "4.0"}
(fact "raw eval for basic rt")

^{:refer rt.basic.type-basic/invoke-ptr-basic :added "4.0"}
(fact "invoke for basic rt")

^{:refer rt.basic.type-basic/rt-basic-string :added "4.0"}
(fact "string for basic rt")

^{:refer rt.basic.type-basic/rt-basic-port :added "4.0"}
(fact "return the basic port of the rt")

^{:refer rt.basic.type-basic/rt-basic:create :added "4.0"}
(fact "creates a basic rt")

^{:refer rt.basic.type-basic/rt-basic :added "4.0"}
(fact "creates and starts a basic rt"

  (def +rt+ (rt-basic {:lang :lua
                       :program :lua}))
  (def +rt+ (rt-basic {:lang :js
                       :runtime :basic}))
  
  (h/stop +rt+))
