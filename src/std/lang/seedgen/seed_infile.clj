(ns std.lang.seedgen.seed-infile
  (:require [code.framework :as base]
            [code.project :as project]
            [std.lang.seedgen.seed-common :as common]
            [std.lib.result :as res]
            [std.task :as task]))

(defn seedgen-root
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



(defn seedgen-list
  "imports unit tests as docstrings
 
   (project/in-context (import {:print {:function true}}))
   => map?"
  {:added "3.0"}
  ([ns params lookup project]
   (let [test-ns     (project/test-ns ns)
         test-file   (lookup test-ns)
         params      (task/single-function-print params)]
     
     (cond (nil? test-file)
            (res/result {:status :error
                         :data :no-test-file})
            
            :else
            (common/seedgen-root-langs test-file false)))))



(comment 

  ;; ** seedgen-list
  ;; - look at the namespace form for [std.lang :as <ns>]
  ;; - look at all the fac <ns>/script- that does not have the `:seedgen/root` tag
  ;; - return the <lang> value in  (<ns>/script- <lang>) 
  
  ;; ^{:seedgen/root     {:all true}}
  ;; (l/script- :js
  ;;    {:runtime :basic
  ;;    :require [[xt.lang.common-spec :as xt]]})
  ;; 
  ;; (l/script- :lua
  ;;    {:runtime :basic
  ;;    :require [[xt.lang.common-spec :as xt]]})
  ;;
  ;; (l/script- :python
  ;;    {:runtime :basic
  ;;    :require [[xt.lang.common-spec :as xt]]})
  
  
  (code.project/in-context
   (std.lang.seedgen.seed-infile/seedgen-list '<sample>
                                              {}))
  => [:lua :python])



(defn seedgen-incomplete
  "imports unit tests as docstrings
  
   (project/in-context (import {:print {:function true}}))
   => map?"
  {:added "3.0"}
  ([ns params lookup project]
   (let [test-ns     (project/test-ns ns)
         test-file   (lookup test-ns)
         params      (task/single-function-print params)]
     
      (cond (nil? test-file)
            (res/result {:status :error
                         :data :no-test-file})

            :else
            (let [analysis (base/analyse-file [:test test-file])]
              (cond (res/result? analysis)
                    analysis

                    :else
                    (let [fact-forms (common/seedgen-fact-forms test-file)
                          root-lang  (first (common/seedgen-root-langs test-file true))]
                      (->> analysis
                           vals
                           (mapcat seq)
                           (keep (fn [[_ {:keys [ns var test]}]]
                                   (let [refer (symbol (str ns) (str var))
                                         form  (get fact-forms refer (:sexp test))
                                         langs (common/seedgen-coverage-langs form)]
                                     (when (not (some #{root-lang} langs))
                                       [refer
                                        {:status :incomplete
                                         :line (:line test)}]))))
                           (into (sorted-map))))))))))

(comment 

  ;; ** seedgen-incomplete
  ;; use framework/analyse to go through the test fact forms
  ;; for each fact form:
  ;; - there if there is one or more (!.<lang> ...) forms, then it is covered.
  ;; - additionally, there can be (!.<lang> ...) forms in :setup and :teardown of each fact form
  ;; 
  ;; seedgen-incomplete acts a bit like code.manage/incomplete but instead of looking src/test pairs, it will look at (!.<lang> ...)
  ;; coverage in the test file
  ;;
  ;; 

  ;; ** example
  ;;
  ;; ^{:refer xt.lang.common-spec/example.A :added "4.1"
  ;;   :setup [(!.js (+ 1 2 3 ))]}
  ;; (fact "iterates arrays in order"
  ;;       (!.js
  ;;        (var out [])
  ;;        (xt/for:array [e [1 2 3 4]]
  ;;                      (when (> e 3)
  ;;                        (break))
  ;;                      (xt/x:arr-push out e))
  ;;        out)
  ;;       => [1 2 3])
  ;;
  ;; ^{:refer xt.lang.common-spec/example.B :added "4.1"
  ;;   :setup [(!.js (+ 1 2 3 ))]}
  ;; (fact "TODO")
  ;;     

  (code.project/in-context
   (std.lang.seedgen.seed-infile/seedgen-incomplete <sample>
                                                    {}))
  => '{xt.lang.common-spec/example.B {:status ... }})


(defn seedgen-purge
  "imports unit tests as docstrings
  
   (project/in-context (import {:print {:function true}}))
   => map?"
  {:added "3.0"}
  ([ns params lookup project]
   (let [test-ns     (project/test-ns ns)
         test-file   (lookup test-ns)
         params      (task/single-function-print params)]
     
     (cond (nil? test-file)
           (res/result {:status :error
                        :data :no-test-file})

           :else
           ()))))


(comment 
  
  ;; ** seedgen-purge
  
  ;; ^{:seedgen/root     {:all true}}
  ;; (l/script- :js
  ;;    {:runtime :basic
  ;;    :require [[xt.lang.common-spec :as xt]]})
  ;; 
  ;; (l/script- :lua
  ;;    {:runtime :basic
  ;;    :require [[xt.lang.common-spec :as xt]]})
  ;;
  ;; (l/script- :python
  ;;    {:runtime :basic
  ;;    :require [[xt.lang.common-spec :as xt]]})
  

  ;; ** example
  ;;
  ;; ^{:refer xt.lang.common-spec/example.A :added "4.1"
  ;;   :setup [(!.js (+ 1 2 3 ))
  ;;           (!.lua (+ 1 2 3 ))
  ;;           (!.python (+ 1 2 3 ))]}
  ;; (fact "iterates arrays in order"
  ;;       (!.js
  ;;        (var out [])
  ;;        (xt/for:array [e [1 2 3 4]]
  ;;                      (when (> e 3)
  ;;                        (break))
  ;;                      (xt/x:arr-push out e))
  ;;        out)
  ;;       => [1 2 3]
  ;;
  ;;       (!.lua
  ;;        (var out [])
  ;;        (xt/for:array [e [1 2 3 4]]
  ;;                      (when (> e 3)
  ;;                        (break))
  ;;                      (xt/x:arr-push out e))
  ;;        out)
  ;;       => [1 2 3])
  ;;
  ;;
  ;; ^{:refer xt.lang.common-spec/example.B :added "4.1"
  ;;   :setup [(!.js (+ 1 2 3 ))]}
  ;; (fact "TODO")
  ;;     
  
  (code.project/in-context ;; removes all but :js
   (std.lang.seedgen.seed-infile/seedgen-purge '<sample>
                                               {:lang :all}))

  (code.project/in-context ;; only lua
   (std.lang.seedgen.seed-infile/seedgen-purge '<sample>
                                               {:lang :lua}))
  
  (code.project/in-context ;; lua and python
   (std.lang.seedgen.seed-infile/seedgen-purge '<sample>
                                               {:lang [:lua :python]}))

  (code.project/in-context ;; ERROR because its seedgen
   (std.lang.seedgen.seed-infile/seedgen-purge '<sample>
                                               {:lang [:js]}))
  )
