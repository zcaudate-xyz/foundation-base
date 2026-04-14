(ns rt.basic.impl-annex.process-haskell
  (:require [rt.basic.type-common :as common]
            [rt.basic.type-twostep :as twostep]
            [std.lang.base.runtime :as rt]
            [std.lang.model-annex.spec-haskell :as spec]))

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

(def +haskell-twostep-config+
  (common/set-context-options
   [:haskell :twostep :default]
   {:container {:image "foundation-base/rt-twostep-haskell:latest"}
    :container-backup true
    :exec-fn #'twostep/sh-exec-portable
    :main {:in identity}
    :emit {:body {:transform #'transform-form}}
    :json :string}))

(def +haskell-twostep+
  [(rt/install-type!
    :haskell :twostep
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])
