(ns std.lang.seedgen
  (:require [code.manage :as manage]
            [code.manage.unit.template :as template]
            [std.lang.seedgen.seed-addlang :as addlang]
            [std.lang.seedgen.seed-infile :as infile]
            [std.lang.seedgen.seed-removelang :as removelang]
            [std.lib.invoke :as invoke]
            [std.task :as task]))

(invoke/definvoke seedgen-root
  [:task {:template :code
          :params {:title "SEEDGEN ROOT"
                   :parallel true
                   :sorted true
                   :print {:result false :summary false}}
          :main {:fn #'infile/seedgen-root}
          :item {:display identity}
          :result {:columns (template/code-default-columns :data #{:bold})}}])

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
          :main {:fn #'infile/seedgen-list}
          :item {:display identity}
          :result {:columns (template/code-default-columns :data #{:bold})}}])

(comment (std.lang.seedgen/seedgen-list
          ['xt.sample]
          {:print {:item true :result true :summary true}}))

(invoke/definvoke seedgen-removelang
  [:task {:template :code
          :params {:title "SEEDGEN REMOVELANG"
                   :parallel true
                   :sorted true
                   :print {:result false :summary false}}
          :main {:fn #'removelang/seedgen-removelang}
          :item {:display identity}
          :result {:columns (template/code-default-columns :data #{:bold})}}])

(invoke/definvoke seedgen-addlang
  [:task {:template :code
          :params {:title "SEEDGEN ADDLANG"
                   :parallel true
                   :sorted true
                   :print {:result false :summary false}}
          :main {:fn #'addlang/seedgen-addlang}
          :item {:display identity}
          :result {:columns (template/code-default-columns :data #{:bold})}}])
