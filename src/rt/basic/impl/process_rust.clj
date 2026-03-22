(ns rt.basic.impl.process-rust
  (:require [rt.basic.type-common :as common]
            [rt.basic.type-twostep :as twostep]
            [std.lang.base.book :as book]
            [std.lang.base.impl :as impl]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.runtime :as rt]
            [std.lang.base.util :as ut]
            [std.lang.model.spec-c :as spec]))

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
   {:emit  {:body  {:transform #'transform-form}}

    #_#_
    :json :string
    }))

(def +c-twostep+
  [(rt/install-type!
    :rust :twostep
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])
