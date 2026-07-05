(ns hara.runtime.basic.impl-annex.process-haskell
  (:require [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-twostep :as twostep]
            [hara.runtime.basic.type-verify :as type-verify]
            [hara.lang.runtime :as rt]
            [hara.model.annex.spec-haskell :as spec]))

(defn transform-form
  "Transforms forms into a standalone Haskell `main` program."
  {:added "4.1"}
  [forms opts]
  (let [forms (if (symbol? (first forms))
                [forms]
                forms)]
    (apply list
           :lines
           (concat (butlast forms)
                   [(list :%
                          (list :raw-str "main = print $\n")
                          (list :indent-body (last forms)))]))))

(def +program-init+
  (common/put-program-options
   :haskell {:default  {:twostep     :ghc
                        :verify      :ghc
                        :interactive false
                        :ws-client   false}
             :env      {:ghc      {:exec "ghc"
                                   :extension   "hs"
                                   :stderr true
                                   :flags  {:twostep []
                                            :verify  ["-fno-code"]
                                            :interactive false
                                             :json false
                                             :ws-client false}
                                    :output-flag "-o"}}}))

(def +haskell-twostep-config+
  (common/set-context-options
   [:haskell :twostep :default]
   {:container {:image "ghcr.io/zcaudate-xyz/foundation-base/rt-twostep-haskell:latest"}
    :container-backup true
    :exec-fn #'twostep/sh-exec-portable
    :main {:in identity}
    :emit {:body {:transform #'transform-form}}
    :json :string}))

(def +haskell-verify-config+
  (common/set-context-options
   [:haskell :verify :default]
   {:main    {}
    :emit    {:body {:transform #'transform-form}}
    :json    false
    :exec-fn #'type-verify/verify-exec-file}))

(def +haskell-twostep+
  [(rt/install-type!
    :haskell :twostep
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])

(def +haskell-verify+
  [(rt/install-type!
    :haskell :verify
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])
