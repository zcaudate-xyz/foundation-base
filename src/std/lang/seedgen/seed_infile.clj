(ns std.lang.seedgen.seed-infile
  (:require [clojure.set :as set]
            [code.framework :as base]
            [code.project :as project]
            [std.block.base :as block]
            [std.block.navigate :as nav]
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


(defn seedgen-removelang
  "imports unit tests as docstrings
   
   (project/in-context (import {:print {:function true}}))
    => map?"
  {:added "3.0"}
  ([ns params lookup project]
   (let [test-ns   (project/test-ns ns)
         test-file (lookup test-ns)
         params    (task/single-function-print params)]
     (cond (nil? test-file)
           (res/result {:status :error
                        :data :no-test-file})

           :else
           (let [root-lang   (first (common/seedgen-root-langs test-file true))
                 purge-langs (common/seedgen-root-langs test-file false)
                 target-lang (or (:lang params) :all)
                 target-lang (cond (= :all target-lang)
                                   purge-langs

                                   (keyword? target-lang)
                                   [target-lang]

                                   (vector? target-lang)
                                   target-lang

                                   (seq? target-lang)
                                   (vec target-lang)

                                   :else
                                   [target-lang])
                 target-lang (->> target-lang
                                  (map common/seedgen-normalize-runtime-lang)
                                  distinct
                                  vec)]
             (cond (nil? root-lang)
                   (res/result {:status :error
                                :data :no-seedgen-root})

                   (some #{root-lang} target-lang)
                   (res/result {:status :error
                                :data :cannot-purge-root
                                :lang root-lang})

                   :else
                   (letfn [(purge-target?
                             [form]
                             (let [langs (common/seedgen-runtime-dispatch-langs form)]
                               (and (seq langs)
                                    (set/subset? (set langs)
                                                 (set target-lang)))))
                           (meta-block?
                             [zloc]
                             (= :meta (block/block-tag (nav/block zloc))))
                            (meta-nav
                              [zloc]
                              (when (meta-block? zloc)
                                (nav/down zloc)))
                            (body-nav
                              [zloc]
                              (if (meta-block? zloc)
                                (-> zloc nav/down nav/right)
                                zloc))
                            (check-arrow?
                              [form]
                              (and (symbol? form)
                                   (boolean (re-find #"=>" (name form)))))
                            (normalize-container-nav
                              [zloc pred]
                              (cond (nil? zloc)
                                    zloc

                                    (pred (nav/value zloc))
                                    zloc

                                    :else
                                    (nav/up zloc)))
                            (purge-vector-nav
                              [vnav]
                              (loop [state-nav vnav
                                     current   (nav/down vnav)]
                                (cond (nil? current)
                                      (normalize-container-nav state-nav vector?)

                                      (purge-target? (nav/value current))
                                      (let [updated (nav/delete current)
                                            updated (if (nil? (nav/value updated))
                                                      (nav/tighten updated)
                                                      updated)]
                                        (recur updated updated))

                                      :else
                                      (recur state-nav (nav/right current)))))
                            (purge-meta-nav
                              [mnav]
                              (loop [state-nav mnav
                                     current   (nav/down mnav)]
                                (cond (nil? current)
                                      (normalize-container-nav state-nav map?)

                                      :else
                                      (let [key   (nav/value current)
                                            vnav  (nav/right current)]
                                        (cond (not (#{:setup :teardown} key))
                                              (recur state-nav
                                                     (some-> vnav nav/right))

                                              (nil? vnav)
                                              (recur state-nav nil)

                                              :else
                                              (let [updated-vnav (purge-vector-nav vnav)]
                                                (if (empty? (nav/value updated-vnav))
                                                  (let [updated (-> updated-vnav
                                                                    nav/delete-left
                                                                    nav/delete)
                                                        updated (if (nil? (nav/value updated))
                                                                  (nav/tighten updated)
                                                                  updated)]
                                                    (recur updated updated))
                                                  (recur updated-vnav
                                                         (some-> updated-vnav
                                                                 nav/right)))))))))
                            (purge-body-nav
                              [bnav]
                              (loop [state-nav bnav
                                     current   (nav/down bnav)]
                                (cond (nil? current)
                                      (normalize-container-nav state-nav
                                                               #(and (seq? %)
                                                                     (= 'fact (first %))))

                                      (and (purge-target? (nav/value current))
                                           (some-> current nav/right nav/value check-arrow?))
                                      (let [updated (-> current
                                                        nav/delete-right
                                                        nav/delete-right
                                                        nav/delete)
                                            updated (if (nil? (nav/value updated))
                                                      (nav/tighten updated)
                                                      updated)]
                                        (recur updated updated))

                                      :else
                                      (recur state-nav (nav/right current)))))
                            (script-form?
                              [form script-heads]
                              (and (seq? form)
                                   (contains? script-heads (first form))))
                           (purge-string
                             [text]
                             (let [root         (nav/parse-root text)
                                   current0     (nav/down root)
                                   script-heads (common/seedgen-script-heads
                                                 (nav/value current0))]
                               (loop [state-nav root
                                      current   current0]
                                 (cond (nil? current)
                                       (nav/root-string state-nav)

                                       :else
                                       (let [form (nav/value current)]
                                         (cond (and (script-form? form script-heads)
                                                    (contains? (set target-lang)
                                                               (common/seedgen-normalize-runtime-lang
                                                                (second form))))
                                               (let [updated (nav/delete current)]
                                                 (recur updated updated))

                                                (and (seq? form)
                                                     (= 'fact (first form)))
                                                (let [current (if-let [mnav (meta-nav current)]
                                                                (-> mnav
                                                                    purge-meta-nav
                                                                    nav/up)
                                                                current)
                                                      bnav    (body-nav current)
                                                      updated (if (meta-block? current)
                                                                (-> bnav
                                                                    purge-body-nav
                                                                    nav/up)
                                                                (purge-body-nav bnav))]
                                                  (recur updated (nav/right updated)))

                                               :else
                                               (recur current (nav/right current))))))))]
                     (base/transform-code test-ns
                                          (-> params
                                              (assoc :transform purge-string
                                                     :no-analysis true)
                                              (dissoc :lang))
                                          lookup
                                          project))))))))


(comment 
  
  ;; ** seedgen-removelang
  
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
   (std.lang.seedgen.seed-infile/seedgen-removelang '<sample>
                                               {:lang :all}))

  (code.project/in-context ;; only lua
   (std.lang.seedgen.seed-infile/seedgen-removelang '<sample>
                                               {:lang :lua}))
  
  (code.project/in-context ;; lua and python
   (std.lang.seedgen.seed-infile/seedgen-removelang '<sample>
                                               {:lang [:lua :python]}))

  (code.project/in-context ;; ERROR because its seedgen
   (std.lang.seedgen.seed-infile/seedgen-removelang '<sample>
                                               {:lang [:js]}))
  )





(defn seedgen-addlang
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
  
  ;; ** seedgen-addlang
  
  ;; ^{:seedgen/root     {:all true}}
  ;; (l/script- :js
  ;;    {:runtime :basic
  ;;    :require [[xt.lang.common-spec :as xt]]})  
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
   (std.lang.seedgen.seed-infile/seedgen-addlang '<sample>))

  (code.project/in-context ;; only lua
   (std.lang.seedgen.seed-infile/seedgen-addlang '<sample>
                                               {:lang :lua}))
  
  (code.project/in-context ;; lua and python
   (std.lang.seedgen.seed-infile/seedgen-addlang '<sample>
                                               {:lang [:lua :python]})))
