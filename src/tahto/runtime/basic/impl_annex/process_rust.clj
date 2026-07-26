(ns tahto.runtime.basic.impl-annex.process-rust
  (:require [tahto.runtime.basic.type-common :as common]
            [tahto.runtime.basic.type-twostep :as twostep]
            [tahto.runtime.basic.type-verify :as type-verify]
            [tahto.base.book :as book]
            [tahto.core.impl :as impl]
            [tahto.core.pointer :as ptr]
            [tahto.core.runtime :as rt]
            [tahto.common.util :as ut]
            [tahto.model.spec-c :as spec]))

(def +program-init+
  (common/put-program-options
   :rust  {:default  {:twostep     :rustc
                      :verify      :rustc
                      :interactive false
                      :ws-client   false}
           :env      {:rustc    {:exec "rustc"
                                 :extension   "rs"
                                 :stderr true
                                 :flags  {:twostep []
                                          :verify  ["--emit=metadata"]
                                          :interactive false
                                          :json false
                                          :ws-client false}}}}))

(defn transform-form
  "transforms the rust form"
  {:added "4.0"}
  [forms opts]
  (let [forms (if (symbol? (first forms))
                [forms]
                forms)
        body (concat
              '[do]
              (butlast forms)
              [(list (list :- "println!") "{}"
                     (last forms))])]
    `(:- "fn main() {\n "
         ~body
         "\n}")))

(def +c-twostep-config+
  (common/set-context-options
   [:rust :twostep :default]
   {:container {:image "ghcr.io/zcaudate-xyz/foundation-base/rt-twostep-rust:latest"}
    :container-backup true
    :exec-fn #'twostep/sh-exec-portable
    :emit  {:body  {:transform #'transform-form}}

     #_#_
     :json :string
     }))

(def +rust-verify-config+
  (common/set-context-options
   [:rust :verify :default]
   {:main    {}
    :emit    {:body {:transform #'transform-form}}
    :json    false
    :exec-fn #'type-verify/verify-exec-file}))

(def +c-twostep+
  [(rt/install-type!
    :rust :twostep
    {:type :tahto/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])

(def +rust-verify+
  [(rt/install-type!
    :rust :verify
    {:type :tahto/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])
