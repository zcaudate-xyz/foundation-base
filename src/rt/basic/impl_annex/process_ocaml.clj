(ns rt.basic.impl-annex.process-ocaml
  (:require [rt.basic.type-common :as common]
            [rt.basic.type-twostep :as twostep]
            [std.lang.base.runtime :as rt]
            [std.lang.model-annex.spec-ocaml :as spec]))

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
   :ocaml {:default {:twostep :ocamlc}
           :env     {:ocamlc {:exec "ocamlc"
                              :extension "ml"
                              :stderr true
                              :flags {:twostep []
                                      :interactive false
                                      :json false
                                      :ws-client false}
                              :output-flag "-o"}}}))

(def +ocaml-twostep-config+
  (common/set-context-options
   [:ocaml :twostep :default]
   {:container {:image "foundation-base/rt-twostep-ocaml:latest"}
    :container-backup true
    :exec-fn #'twostep/sh-exec-portable
    :main {:in identity}
    :emit {:body {:transform #'transform-form}}
    :json :string}))

(def +ocaml-twostep+
  [(rt/install-type!
    :ocaml :twostep
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])
