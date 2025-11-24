(ns code.heal-test
  (:use code.test)
  (:require [code.heal :as heal]
            [code.heal.level :as level]
            [std.lib :as h]))

^{:refer code.heal/heal-code-single :added "4.0"}
(fact "helper function for heal-code"
  ^:hidden

  (code.project/in-context
   (heal/heal-code-single {}))
  => {:changed [], :updated false, :path "test/code/heal_test.clj"})

^{:refer code.heal/heal-code :added "4.0"}
(fact "helper function to fix parents")

^{:refer code.heal/print-rainbox :added "4.0"}
(fact "prints out the code in rainbow"

  (heal/print-rainbow
   (slurp "test/code/heal_test.clj")))


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
  
  
  (heal-code '[code.dev.client.app.components]
             {:write true})
  
  
  (heal-code '[code.dev.client.app.components])
  (heal-code '[code.dev.client.app.components.canvas])
  
  (project/get-path 'code.dev.client.app.components.theme-editor
                    (project/project))
  (std.fs/ns->file 'code.dev.client.app.theme-editor)
  ((project/lookup-ns
    'code.dev.client.app.theme-editor
    )
   )
  )


^{:refer code.heal/print-rainbow :added "4.0"}
(fact "TODO")