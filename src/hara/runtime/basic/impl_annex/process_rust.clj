(ns hara.runtime.basic.impl-annex.process-rust
  (:require [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-twostep :as twostep]
            [hara.lang.base.book :as book]
            [hara.lang.base.impl :as impl]
            [hara.lang.base.pointer :as ptr]
            [hara.lang.base.runtime :as rt]
            [hara.common.util :as ut]
            [hara.model.spec-c :as spec]))

(def +program-init+
  (common/put-program-options
   :rust  {:default  {:twostep     :rustc
                      :interactive false
                      :ws-client   false}
           :env      {:rustc    {:exec "rustc"
                                 :extension   "rs"
                                 :stderr true
                                 :flags  {:twostep []
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
   {:container {:image "foundation-base/rt-twostep-rust:latest"}
    :container-backup true
    :exec-fn #'twostep/sh-exec-portable
    :emit  {:body  {:transform #'transform-form}}

     #_#_
     :json :string
     }))

(def +c-twostep+
  [(rt/install-type!
    :rust :twostep
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])
