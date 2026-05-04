(ns check-compile
  (:require [hara.runtime.postgres.grammar.common :as common]
            [hara.runtime.postgres.grammar.form-defpartition :as form-defpartition]))

(println "Common pg-deftype-ref-name:" (resolve 'common/pg-deftype-ref-name))
(println "Form-defpartition pg-deftype-partition:" (resolve 'form-defpartition/pg-deftype-partition))
