(ns hara.runtime.basic.impl.process-go
  (:require [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-twostep :as twostep]
            [hara.runtime.basic.type-verify :as type-verify]
            [hara.lang.runtime :as rt]
            [hara.model.spec-go]))

(defn default-twostep-wrap
  "Prepends the minimal standalone Go program wrapper."
  {:added "4.1"}
  [body]
  (str "package main\n\n"
       "import \"fmt\"\n\n"
       body))

(defn transform-form
  "Transforms forms into a standalone Go `main` function."
  {:added "4.1"}
  [forms opts]
  (let [forms (if (symbol? (first forms))
                [forms]
                forms)
        body  (concat '[do]
                      (butlast forms)
                      [(list 'var 'out (last forms))
                       (list 'fmt.Println 'out)])]
    `(:- "func main() {\n "
         ~body
         "\n}")))

(def +program-init+
  (common/put-program-options
   :go {:default {:twostep :go
                  :verify  :go}
         :env     {:go {:exec "go"
                        :extension "go"
                        :stderr true
                        :flags {:twostep ["build"]
                                :verify ["build" "-o" "/dev/null"]
                                :interactive false
                                :json false
                                :ws-client false}
                        :output-flag "-o"}}}))

(def +go-twostep-config+
  (common/set-context-options
   [:go :twostep :default]
   {:main {:in #'default-twostep-wrap}
    :emit {:body {:transform #'transform-form}}
    :json :string}))

(def +go-verify-config+
  (common/set-context-options
   [:go :verify :default]
   {:main    {}
    :emit    {}
    :json    false
    :exec-fn #'type-verify/verify-exec-file}))

(def +go-twostep+
  [(rt/install-type!
    :go :twostep
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])

(def +go-verify+
  [(rt/install-type!
    :go :verify
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])
