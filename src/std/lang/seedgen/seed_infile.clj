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
                           (purge-meta
                             [m]
                             (let [setup    (some->> (:setup m)
                                                     (remove purge-target?)
                                                     vec
                                                     not-empty)
                                   teardown (some->> (:teardown m)
                                                     (remove purge-target?)
                                                     vec
                                                     not-empty)]
                               (cond-> (dissoc m :setup :teardown)
                                 setup    (assoc :setup setup)
                                 teardown (assoc :teardown teardown))))
                           (check-arrow?
                             [form]
                             (and (symbol? form)
                                  (boolean (re-find #"=>" (name form)))))
                           (purge-body
                             [body]
                             (loop [body body
                                    out  []]
                               (cond (empty? body)
                                     out

                                     (and (<= 3 (count body))
                                          (check-arrow? (second body))
                                          (purge-target? (first body)))
                                     (recur (drop 3 body) out)

                                     :else
                                     (recur (rest body)
                                            (conj out (first body))))))
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
                                                               (nav/up (nav/replace mnav (purge-meta (nav/value mnav))))
                                                               current)
                                                     bnav    (body-nav current)
                                                     updated (if (meta-block? current)
                                                               (nav/up (nav/replace bnav (apply list (first form) (purge-body (rest form)))))
                                                               (nav/replace bnav (apply list (first form) (purge-body (rest form)))))]
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
