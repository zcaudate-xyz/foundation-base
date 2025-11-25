(ns code.heal
  (:require [code.heal.core :as core]
            [code.heal.print :as print]
            [code.heal.parse :as parse]
            [code.framework :as framework]
            [std.lib :as h :refer [definvoke]]
            [std.task :as task]
            [code.project :as project]
            [code.manage.unit.template :as template]))

(h/intern-in [heal core/heal-content])

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
          :result template/base-transform-result}])

(defn print-rainbow
  "prints out the code in rainbow"
  {:added "4.0"}
  [content]
  (print/print-rainbow
   content
   (parse/pair-delimiters
    (parse/parse-delimiters content))))

(defn -main
  "main entry point for leiningen
 
   (task/-main)"
  {:added "3.0"}
  ([& args]
   (let [opts (task/process-ns-args args)]
     ()
     )))
