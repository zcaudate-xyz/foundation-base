(ns rt.basic.impl.process-haskell
  (:require [std.lang.model.spec-haskell :as spec]
            [std.lang.base.impl :as impl]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.runtime :as rt]
            [std.lang.base.pointer :as ptr]
            [std.lib :as h]
            [std.string :as str]
            [rt.basic.type-common :as common]
            [rt.basic.type-twostep :as twostep]))

(def +program-init+
  (common/put-program-options
   :haskell {:default  {:twostep     :ghc
                        :interactive false
                        :ws-client   false}
             :env      {:ghc      {:exec "ghc"
                                   :extension   "hs"
                                   :stderr true
                                   :flags  {:twostep []
                                            :interactive false
                                            :json false
                                            :ws-client false}
                                   :output-flag "-o"}}}))

(def +haskell-twostep+
  [(rt/install-type!
    :haskell :twostep
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])
