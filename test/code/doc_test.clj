(ns code.doc-test
  (:require [code.doc :refer :all])
  (:use code.test))

^{:refer code.doc/make-project :added "3.0"}
(fact "makes a env for the publish task"

  (make-project)
  => map?)

^{:refer code.doc/make-audit-project :added "4.1"}
(fact "makes an env for code.doc coverage tasks"
  (make-audit-project)
  => (contains {:code.doc/source-namespaces coll?
                :code.doc/coverage map?}))

^{:refer code.doc/publish :added "3.0"}
(comment "main publish method"

  (publish 'hara/hara-code {}))

^{:refer code.doc/init-template :added "3.0"}
(comment "initialises the theme template for a given site"

  (init-template "hara"))

^{:refer code.doc/deploy-template :added "3.0"}
(comment "deploys the theme for a given site"

  (deploy-template "hara"))

^{:refer code.doc/missing :added "4.1"}
(comment "checks for namespaces not yet referenced by code.doc pages"

  (missing))

(comment
  (publish :all {:write true})

  (publish 'hara/hara-publish {:write true})

  (publish 'hara/index {:write true})

  (publish 'spirit/spirit-io-datomic {:write true}))
