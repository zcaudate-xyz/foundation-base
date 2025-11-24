(ns code.refactor
  (:require [code.framework :as base]
            [code.manage.ns-format :as ns-format]
            [code.manage.var :as var]
            [code.manage.unit.template :as template]
            [std.task :as task]
            [std.lib :as h :refer [definvoke]]))

(defmethod task/task-defaults :code
  ([_]
   template/code-default))

(defmethod task/task-defaults :code.transform
  ([_]
   template/code-transform))

;; The following tasks are moved from code.manage but rely on functions
;; that are currently missing from the codebase (e.g. var/replace-usages, lint/lint).
;; They are kept here for reference/future implementation but are disabled
;; to prevent compilation/load errors.

(comment
  (definvoke replace-usages
    "replace usages of a var

   (replace-usages '[code.manage]
                   {:var 'code.framework/analyse
                   :new 'analyse-changed})"
    {:added "3.0"}
    [:task {:template :code.transform
            :params {:title "REPLACE VAR USAGES"
                     :print {:item false
                             :function true}}
            :main   {:fn 'var/replace-usages} ;; Missing function
            :result (template/code-transform-result :changed)}])

  (definvoke replace-refers
    "TODO"
    {:added "3.0"}
    [:task {:template :code.transform
            :params {:title "REPLACE REFERS"
                     :print {:item false
                             :function true}}
            :main   {:fn 'var/replace-refers} ;; Missing function
            :result (template/code-transform-result :changed)}])

  (definvoke replace-errors
    "TODO"
    {:added "3.0"}
    [:task {:template :code.transform
            :params {:title "REPLACE ERRORS"
                     :print {:item false
                             :function true}}
            :main   {:fn 'var/replace-errors} ;; Missing function
            :result (template/code-transform-result :changed)}])

  (definvoke list-ns-unused
    "TODO"
    {:added "3.0"}
    [:task {:template :code
            :params {:title "LIST UNUSED NS ENTRIES"
                     :print {:item true}}
            :item {:list template/source-namespaces}
            :main   {:fn 'var/list-ns-unused}}]) ;; Missing function

  (definvoke remove-ns-unused
    "TODO"
    {:added "3.0"}
    [:task {:template :code.transform
            :params {:title "REMOVE UNUSED NS ENTRIES"
                     :print {:function true}}
            :main   {:fn 'var/remove-ns-unused} ;; Missing function
            :result template/base-transform-result}])

  (definvoke rename-ns-abbrevs
    "TODO"
    {:added "3.0"}
    [:task {:template :code.transform
            :params {:title "RENAME NS ABBREVS"
                     :print {:function true}}
            :main {:fn 'var/rename-ns-abbrevs} ;; Missing function
            :result template/base-transform-result}])

  (definvoke refactor-ns-forms
    "refactors and reorganises ns forms

   (refactor-ns-forms '[code.manage])"
    {:added "3.0"}
    [:task {:template :code.transform
            :params {:title "TRANSFORMING NS FORMS"
                     :print {:function true}}
            :main {:fn 'ns-format/refactor} ;; Missing function (ns-format exists but refactor does not)
            :result template/base-transform-result}])

  (comment
    (refactor-ns-forms '[code.manage] {:write true}))

  ;; Missing lint namespace entirely
  (definvoke lint
    [:task {:template :code.transform
            :params {:title "LINTING CODE"
                     :print {:function true}}
            :main   {:fn 'lint/lint} ;; Missing namespace/function
            :result template/base-transform-result}])

  (comment
    (lint 'code.manage {:write true}))

  (definvoke line-limit
    [:task {:template :code
            :params {:title "LINES EXCEEDING LIMIT"
                     :print {:function true}}
            :main   {:fn 'lint/line-limit}}]) ;; Missing namespace/function

  (comment
    (time (analyse 'code.framework-test))
    (time (def a (analyse 'code.framework-test)))
    (time (def a (analyse 'code.manage)))
    (code.framework.cache/purge)
    (./reset '[code.manage])
    (./reset '[code.manage])
    (./incomplete '[code.manage])

    (vars 'code.manage)
    (vars '[thing] {:print {:item true}})
    (./import)
    (line-limit ['hara] {:length 110}))
  )
