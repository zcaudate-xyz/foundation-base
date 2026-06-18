(ns hara.seedgen
  (:require [code.manage :as manage]
            [code.project :as project]
            [code.manage.unit.template :as template]
            [hara.seedgen.common-infile :as common-infile]
            [hara.seedgen.common-util :as common]
            [hara.seedgen.form-bench :as form-bench]
            [hara.seedgen.form-parse :as form-parse]
            [hara.seedgen.form-infile :as form-infile]
            [std.lib.result :as res]
            [std.lib.invoke :as invoke]
            [std.task :as task]))

(defn- scalar-result-ignore?
  [data]
  (or (nil? data)
      (and (seqable? data)
           (empty? data))))

(defn- scalar-result-count
  [data]
  (cond (nil? data)
        0

        (seqable? data)
        (count data)

        :else
        1))

(defn- seedgen-incomplete-summary
  [ns params lookup project]
  (let [output (common-infile/seedgen-incomplete ns params lookup project)]
    (if (res/result? output)
      output
      (->> output
           (map (fn [[refer {:keys [line]}]]
                  (with-meta refer line)))
           sort
           vec))))

(defn- seedgen-readforms-vars
  [output]
  (->> output
       :entries
       vals
       (mapcat keys)
       sort
       vec))

(defn- bench-output-targets
  [outputs pred]
  (->> outputs
       (filter pred)
       (map :ns)
       vec))

(defn- seedgen-test-namespaces
  [lookup _]
  (->> lookup
       keys
       (filter #(= % (project/test-ns %)))
       (remove #(common/seedgen-skip? (lookup %)))
       sort
       vec))

(defn- seedgen-benchadd-summary
  [ns params lookup project]
  (let [output (form-bench/seedgen-benchadd ns params lookup project)]
    (if (res/result? output)
      output
      (assoc output
             :new (bench-output-targets (:outputs output)
                                         #(or (:updated %)
                                              (pos? (+ (or (:inserts %) 0)
                                                       (or (:deletes %) 0)))))
              :functions (:functions output)))))

(defn- seedgen-benchremove-summary
  [ns params lookup project]
  (let [output (form-bench/seedgen-benchremove ns params lookup project)]
    (if (res/result? output)
      output
      (assoc output
             :changed (bench-output-targets (:outputs output)
                                            :exists)))))

(invoke/definvoke seedgen-root
  [:task {:template :code
          :params {:title "SEEDGEN ROOT"
                   :parallel true
                   :sorted true
                   :print {:result false :summary false}}
          :main {:fn #'common-infile/seedgen-root}
          :item {:list seedgen-test-namespaces
                 :display identity}
          :result {:ignore scalar-result-ignore?
                   :keys {:count scalar-result-count}
                   :columns (template/code-default-columns :data #{:bold})}}])

(comment (hara.seedgen/seedgen-root
          ['xt.sample.train-001-test]
          {:print {:item true :result true :summary true}})

         (hara.seedgen/seedgen-root
          ['xt.sample.train]
          {:print {:item true :result true :summary true}}))

(invoke/definvoke seedgen-list
  [:task {:template :code
          :params {:title "SEEDGEN LIST"
                   :parallel true
                   :sorted true
                   :print {:result false :summary false}}
          :main {:fn #'common-infile/seedgen-list}
          :item {:list seedgen-test-namespaces
                 :display identity}
          :result {:columns (template/code-default-columns :data #{:bold})}}])

(comment (hara.seedgen/seedgen-list
          ['xt.sample]
          {:print {:item true :result true :summary true}}))

(invoke/definvoke seedgen-readforms
  [:task {:template :code
          :params {:title "SEEDGEN READFORMS"
                   :parallel true
                   :print {:result false :summary false}
                   :sorted true}
          :main {:fn #'form-parse/seedgen-readforms}
          :item {:list seedgen-test-namespaces
                 :display seedgen-readforms-vars}
          :result {:ignore nil
                   :keys {:count (comp count seedgen-readforms-vars)
                          :functions seedgen-readforms-vars}
                   :columns (template/code-default-columns :functions #{:bold})}}])

(comment (hara.seedgen/seedgen-readforms
          ['xt.sample.train-002]
          {:print {:item true :result true :summary true}}))

(invoke/definvoke seedgen-benchlist
  [:task {:template :code
          :params {:title "SEEDGEN BENCHLIST"
                   :parallel true
                   :print {:result false :summary false}
                   :sorted true}
          :main {:fn #'form-bench/seedgen-benchlist}
          :item {:list seedgen-test-namespaces
                 :display identity}
          :result {:columns (template/code-default-columns :data #{:bold})}}])

(comment (hara.seedgen/seedgen-benchlist
          ['xt.sample]
          {:print {:item true :result true :summary true}}))

(invoke/definvoke seedgen-incomplete
  [:task {:template :code
          :params {:title "INCOMPLETE TESTS"
                   :parallel true
                   :sorted true
                   :print {:item false}}
          :main {:fn #'seedgen-incomplete-summary}
          :item {:list seedgen-test-namespaces
                 :display identity}
          :result {:columns (template/code-default-columns #{:bold :green})}}])

(comment (hara.seedgen/seedgen-incomplete
          ['xt.sample]
          {:print {:item true :result true :summary true}}))

(invoke/definvoke seedgen-langremove
  [:task {:template :code.transform
          :params {:title "SEEDGEN LANGREMOVE"
                   :parallel true
                   :print {:function true}}
          :main {:fn #'form-infile/seedgen-langremove}
          :item {:list seedgen-test-namespaces
                 :pre project/sym-name
                 :display (template/empty-result :changed :info :no-change)}
          :result (assoc (template/code-transform-result :changed)
                         :columns (template/code-transform-columns #{:bold :red}))}])

(comment (hara.seedgen/seedgen-langremove
          ['xt.sample]
          {:lang :js
           :print {:function true :item true :result true :summary true}})

         (hara.seedgen/seedgen-langremove
          ['xt.sample]
          {:lang :lua
           :print {:function true :item true :result true :summary true}}))

(invoke/definvoke seedgen-langadd
  [:task {:template :code.transform
          :params {:title "SEEDGEN LANGADD"
                   :parallel true
                   :print {:function true}}
          :main {:fn #'form-infile/seedgen-langadd}
          :item {:list seedgen-test-namespaces
                 :pre project/sym-name
                 :display (template/empty-result :changed :info :no-change)}
          :result (template/code-transform-result :changed)}])

(comment (hara.seedgen/seedgen-langadd
          ['xt.sample]
          {:lang :lua
           :print {:function true :item true :result true :summary true}})

         (hara.seedgen/seedgen-langadd
          ['xt.lang.spec-base-test]
          {:lang :lua
           :print {:function true :item true :result true :summary true}})
         
         (hara.seedgen/seedgen-langremove
          '[xt.sample]
          {:lang :lua :write false}))

(invoke/definvoke seedgen-benchadd
  [:task {:template :code.transform
          :params {:title "SEEDGEN BENCHADD"
                   :parallel false
                   :print {:function true}}
          :main {:fn #'seedgen-benchadd-summary}
          :item {:list seedgen-test-namespaces
                 :pre project/sym-name
                 :display (template/empty-result :new :info :no-new)}
          :result (assoc (template/code-transform-result :new)
                         :keys {:count (comp count :new)
                                :functions (fn [{:keys [functions new]}]
                                             (or functions new))})}])

(comment (hara.seedgen/seedgen-benchadd
          ['xt.sample]
          {:lang :python
           :print {:function true :item true :result true :summary true}})

         (hara.seedgen/seedgen-benchadd
          'xt.lang.spec-base
          {:lang :python
           :print {:function true :item true :result true :summary true}}))

(invoke/definvoke seedgen-benchremove
  [:task {:template :code.transform
          :params {:title "SEEDGEN BENCHREMOVE"
                   :parallel true
                   :print {:function true}}
          :main {:fn #'seedgen-benchremove-summary}
          :item {:list seedgen-test-namespaces
                 :pre project/sym-name
                 :display (template/empty-result :changed :info :no-change)}
          :result (assoc (template/code-transform-result :changed)
                         :columns (template/code-transform-columns #{:bold :red}))}])

(comment (hara.seedgen/seedgen-benchremove
          ['xt.sample]
          {:lang :python
           :print {:function true :item true :result true :summary true}}))

(comment
  
  )
