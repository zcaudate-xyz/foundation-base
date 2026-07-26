(ns tahto.runtime.basic.impl-annex.process-ocaml
  (:require [tahto.runtime.basic.type-common :as common]
            [tahto.runtime.basic.type-twostep :as twostep]
            [tahto.runtime.basic.type-verify :as type-verify]
            [tahto.core.runtime :as rt]
            [tahto.model.annex.spec-ocaml :as spec]))

(defn transform-form
  "Transforms forms into a standalone OCaml program."
  {:added "4.1"}
  [forms opts]
  (let [forms (if (symbol? (first forms))
                [forms]
                forms)]
    (apply list
           :lines
           (concat (butlast forms)
                   [(list :- "let () = print_int (" (last forms) "); print_newline ()")]))))

(def +program-init+
  (common/put-program-options
   :ocaml {:default {:twostep :ocamlc
                     :verify  :ocamlc}
           :env     {:ocamlc {:exec "ocamlc"
                              :extension "ml"
                              :stderr true
                              :flags {:twostep []
                                      :verify  ["-i"]
                                      :interactive false
                                      :json false
                                      :ws-client false}
                              :output-flag "-o"}}}))

(def +ocaml-twostep-config+
  (common/set-context-options
   [:ocaml :twostep :default]
   {:container {:image "ghcr.io/zcaudate-xyz/foundation-base/rt-twostep-ocaml:latest"
                :flags ["--user" "root"]}
    :container-backup true
    :exec-fn #'twostep/sh-exec-portable
    :main {:in identity}
    :emit {:body {:transform #'transform-form}}
    :json :string}))

(def +ocaml-verify-config+
  (common/set-context-options
   [:ocaml :verify :default]
   {:main    {}
    :emit    {:body {:transform #'transform-form}}
    :json    false
    :exec-fn #'type-verify/verify-exec-file}))

(def +ocaml-twostep+
  [(rt/install-type!
    :ocaml :twostep
    {:type :tahto/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])

(def +ocaml-verify+
  [(rt/install-type!
    :ocaml :verify
    {:type :tahto/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])
