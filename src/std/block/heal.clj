(ns std.block.heal
  (:require [std.block.heal.core :as core]
            [std.block.heal.print :as print]
            [std.block.heal.parse :as parse]
            [std.lib :as h :refer [definvoke]]))

(h/intern-in [heal core/heal-content])

(defn print-rainbow
  "prints out the code in rainbow"
  {:added "4.0"}
  [content]
  (print/print-rainbow
   content
   (parse/pair-delimiters
    (parse/parse-delimiters content))))

(defn rainbow
  "prints out the code in rainbow"
  {:added "4.0"}
  [content]
  (h/with-out-str
    (print-rainbow content)))

(comment
  [std.task :as task]
  [code.project :as project]
            [code.manage.unit.template :as template]
  [code.framework :as framework]
  [:line]
  (defn heal-code-single
  "helper function for heal-code"
  {:added "4.0"}
  ([ns params lookup project]
   (let [params (assoc params :transform core/heal-content)]
     (framework/transform-code ns params lookup project))))

  (definvoke heal-code
    "helper function to fix parents"
    {:added "4.0"}
    [:task {:construct {:input    (fn [_] *ns*)
                        :lookup   (fn [_ project] (project/file-lookup project))
                        :env      (fn [_] (project/project))}
            :template :code.transform
            :params   {:title "Heal Code"
                       :parallel true
                       :no-analysis true
                       :print {:function true :result true :summary true}}
            :main     {:fn #'heal-code-single}
            :result template/base-transform-result}]))

  
