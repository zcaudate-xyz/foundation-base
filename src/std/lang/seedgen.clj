(ns std.lang.seedgen
  (:require [code.manage :as manage]
            [code.manage.unit.template :as template]
            [std.lang.seedgen.common-infile :as common-infile]
            [std.lang.seedgen.form-infile :as form-infile]
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

(invoke/definvoke seedgen-root
  [:task {:template :code
          :params {:title "SEEDGEN ROOT"
                   :parallel true
                   :sorted true
                   :print {:result false :summary false}}
          :main {:fn #'common-infile/seedgen-root}
          :item {:display identity}
          :result {:ignore scalar-result-ignore?
                   :keys {:count scalar-result-count}
                   :columns (template/code-default-columns :data #{:bold})}}])

(comment (std.lang.seedgen/seedgen-root
          ['xt.sample.train-001-test]
          {:print {:item true :result true :summary true}})

         (std.lang.seedgen/seedgen-root
          ['xt.sample.train]
          {:print {:item true :result true :summary true}}))

(invoke/definvoke seedgen-list
  [:task {:template :code
          :params {:title "SEEDGEN LIST"
                   :parallel true
                   :sorted true
                   :print {:result false :summary false}}
          :main {:fn #'common-infile/seedgen-list}
          :item {:display identity}
          :result {:columns (template/code-default-columns :data #{:bold})}}])

(comment (std.lang.seedgen/seedgen-list
          ['xt.sample]
          {:print {:item true :result true :summary true}}))

(invoke/definvoke seedgen-incomplete
  [:task {:template :code
          :params {:title "INCOMPLETE TESTS"
                   :parallel true
                   :sorted true
                   :print {:item false}}
          :main {:fn #'seedgen-incomplete-summary}
          :item {:display identity}
          :result {:columns (template/code-default-columns #{:bold :green})}}])

(comment (std.lang.seedgen/seedgen-incomplete
          ['xt.sample]
          {:print {:item true :result true :summary true}}))

(invoke/definvoke seedgen-removelang
  [:task {:template :code
          :params {:title "SEEDGEN REMOVELANG"
                   :parallel true
                   :sorted true
                   :print {:result false :summary false}}
          :main {:fn #'form-infile/seedgen-removelang}
          :item {:display identity}
          :result {:columns (template/code-default-columns :data #{:bold})}}])

(invoke/definvoke seedgen-addlang
  [:task {:template :code
          :params {:title "SEEDGEN ADDLANG"
                   :parallel true
                   :sorted true
                   :print {:result false :summary false}}
          :main {:fn #'form-infile/seedgen-addlang}
          :item {:display identity}
          :result {:columns (template/code-default-columns :data #{:bold})}}])
