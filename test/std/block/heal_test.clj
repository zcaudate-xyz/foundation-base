(ns std.block.heal-test
  (:use code.test)
  (:require [std.block.heal :refer :all]))

^{:refer std.block.heal/print-rainbow :added "4.1"}
(fact "prints the content with rainbow parens"
  ^:hidden
  
  (std.lib/with-out-str
    (print-rainbow "(+ 12 3)"))
  => "[34m([0m+ 12 3[34m)[0m")

^{:refer std.block.heal/rainbow :added "4.1"}
(fact "formats the content with rainbow parens"
  ^:hidden
  
  (rainbow "(+ 12 3)")
  => "[34m([0m+ 12 3[34m)[0m")


(comment

  

(ns std.block.heal-test
  (:use code.test)
  (:require [code.heal :as heal]
            [code.heal.core :as level]
            [std.lib :as h]))

^{:refer code.heal/heal-code-single :added "4.0"}
(fact "helper function for heal-code"
  ^:hidden

  (code.project/in-context
   (heal/heal-code-single {}))
  => (contains {:changed [], :updated false, :path "test/code/heal_test.clj"}))

^{:refer code.heal/heal-code :added "4.0"}
(fact "helper function to fix parents")

^{:refer code.heal/print-rainbow :added "4.0"}
(fact "prints out the code in rainbow"

  (heal/print-rainbow
   (slurp "test/code/heal_test.clj"))
  => nil)


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

(comment
  (let []
    (doseq [f (keys
               (std.fs/list
                "../Szncampaigncenter/src-translated/"
                {:include [".clj$"]
                 :recursive true}))]
      (try (read-string
            (str "["
                 ((level/wrap-print-diff level/heal-content)
                  (slurp f))
                 "]"))
           (h/p f :SUCCESS)
           (catch Throwable t
             
             (h/p f :FAILED))))))


(comment
  heal-filenames
  heal-namespaces
  
  (heal/heal-directory "hello")
  
  
  (heal-code '[indigo.client.app.components]
             {:write true})
  
  
  (heal-code '[indigo.client.app.components])
  (heal-code '[indigo.client.app.components.canvas])
  
  (project/get-path 'indigo.client.app.components.theme-editor
                    (project/project))
  (std.fs/ns->file 'indigo.client.app.theme-editor)
  ((project/lookup-ns
    'indigo.client.app.theme-editor
    )
   )
  )


