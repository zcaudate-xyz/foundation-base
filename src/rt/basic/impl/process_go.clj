(ns rt.basic.impl.process-go
  (:require [rt.basic.type-common :as common]
            [rt.basic.type-twostep :as twostep]
            [std.lang.base.runtime :as rt]
            [std.lang.model.spec-go]))

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
   :go {:default {:twostep :go}
         :env     {:go {:exec "go"
                        :extension "go"
                        :stderr true
                        :flags {:twostep ["build"]
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

(def +go-twostep+
  [(rt/install-type!
    :go :twostep
    {:type :hara/rt.twostep
     :instance {:create twostep/rt-twostep:create}})])
