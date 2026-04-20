(ns std.lang.seedgen.seed-bench
  (:require [clojure.set :as set]
            [code.framework :as base]
            [code.project :as project]
            [std.block.base :as block]
            [std.block.navigate :as nav]
            [std.lang.seedgen.seed-common :as common]
            [std.lib.result :as res]
            [std.task :as task]))

(defn seedgen-bench
  "imports unit tests as docstrings
  
   (project/in-context (import {:print {:function true}}))
   => map?"
  {:added "3.0"}
  ([ns params lookup project]
   (let [test-ns     (project/test-ns ns)
         test-file   (lookup test-ns)
         params      (task/single-function-print params)]
     
     ;; - look at the namespace form for [std.lang :as <ns>]
     ;; - look for the <ns>/script- form and the `:seedgen/root` tag
     ;; - return the <lang> value in  (<ns>/script- <lang>) 
     
     ;; ^{:seedgen/root     {:all true}}
     ;; (l/script- :js
     ;;            {:runtime :basic
     ;;             :require [[xt.lang.common-spec :as xt]]})
     
      (cond (nil? test-file)
            (res/result {:status :error
                         :data :no-test-file})

            :else
            (first (common/seedgen-root-langs test-file true))))))

(comment

  
  ;; - look at the namespace form for [std.lang :as <ns>]
  ;; - look for the <ns>/script- form and the `:seedgen/root` tag
  ;; - return the <lang> value in  (<ns>/script- <lang>) 
  
  ;; ^{:seedgen/root     {:all true}}
  ;; (l/script- :js
  ;;            {:runtime :basic
  ;;             :require [[xt.lang.common-spec :as xt]]})
  

  (code.project/in-context
   (std.lang.seedgen.seed-infile/seedgen-root 'xt.sample.train-001-test
                                              {}))
  => :js)

