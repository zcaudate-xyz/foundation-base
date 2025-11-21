(ns code.heal
  (:require [code.heal.core :as core]
            [code.heal.print :as print]
            [code.heal.parse :as parse]
            [code.heal.level :as level]
            [code.framework :as framework]
            [std.lib :as h :refer [definvoke]]
            [std.task :as task]
            [code.project :as project]
            [code.manage.unit.template :as template]))

(h/intern-in [heal level/heal-content])

(defn heal-code-single
  "helper function for heal-code"
  {:added "4.0"}
  ([ns params lookup project]
   (let [params (assoc params :transform level/heal-content)]
     (framework/transform-code ns params lookup project))))

(definvoke heal-code
  "helper function to fix parents"
  {:added "4.0"}
  [:task {:construct {:input    (fn [_] *ns*)
                      :lookup   (fn [_ project] (project/file-lookup project))
                      :env      (fn [_] (project/project))}
          :template :code
          :params   {:title "Heal Code"
                     :parallel true
                     :no-analysis true
                     :print {:function true :result true :summary true}}
          :main     {:fn #'heal-code-single}
          :result   (template/code-default-columns :changed)}])

(defn print-rainbow
  "prints out the code in rainbow"
  {:added "4.0"}
  [content]
  (print/print-rainbow
   content
   (parse/pair-delimiters
    (parse/parse-delimiters content))))

(definvoke heal-directory
  "helper function to fix parents"
  {:added "4.0"}
  [:task {:construct {:input    (fn [opts] (h/prn :input opts)
                                  *ns*)
                      :lookup   (fn [opts project]
                                  (h/prn :lookup opts)
                                  (project/file-lookup project))
                      :env      (fn [opts]
                                  (h/prn :env opts)
                                  (project/project))}
          :template :code
          :params   {:title "Heal Code"
                     :parallel true
                     :no-analysis true
                     :print {:function true :result true :summary true}}
          :main     {:fn #'heal-code-single}
          :result   (template/code-default-columns :changed)}])
