(ns code.heal
  (:require [code.heal.core :as core]
            [code.heal.print :as print]
            [code.heal.parse :as parse]
            [code.framework :as framework]
            [std.lib :as h :refer [definvoke]]
            [std.task :as task]
            [code.manage.unit.template :as template]))

(h/intern-in core/heal)

(comment
  heal-filenames
  heal-namespaces
  
  )

(defn heal-code-single
  "helper function for heal-code"
  {:added "4.0"}
  ([ns params lookup project]
   (let [params (assoc params :transform core/heal-raw)]
     (framework/transform-code ns params lookup project))))

(definvoke heal-code
  "helper function to fix parents"
  {:added "4.0"}
  [:task {:template :code
          :params   {:title "Heal Code"
                     :parallel true
                     :print {:result true :summary true}}
          :main     {:fn #'heal-code-single}
          :result   (template/code-default-columns :changed)}])

(defn print-rainbox
  "prints out the code in rainbow"
  {:added "4.0"}
  [content]
  (print/print-rainbow
   content
   (parse/pair-delimiters
    (parse/parse-delimiters content))))
