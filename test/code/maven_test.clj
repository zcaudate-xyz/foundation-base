(ns code.maven-test
  (:use code.test)
  (:require [code.maven :refer :all]
            [code.maven.command :as command]
            [code.maven.package :as package]
            [code.project :as project]))

^{:refer code.maven/install.secure :added "3.0" :adopt true}
(fact "installs signed packages to the local `.m2` repository"

  (install '[xyz.zcaudate/std.lib]
           {:tag :all
            :secure true :digest true}))

^{:refer code.maven/linkage :added "3.0"}
(fact "creates linkages for project"

  (linkage :all {:tag :all
                 :print {:item false :result false :summary false}}))

^{:refer code.maven/package :added "3.0"}
(fact "packages files in the interim directory"

  (package '[xyz.zcaudate]
           {:tag :all
            :print {:item true :result false :summary false}}))

^{:refer code.maven/infer :added "4.0"}
(fact "infers all variables"
  
  (infer '[xyz.zcaudate]
           {:tag :all
            :print {:item true :result false :summary false}}))

^{:refer code.maven/clean :added "3.0"}
(fact "cleans the interim directory of packages"

  (clean :all {:tag :all
               :print {:item false :result false :summary false}}))

^{:refer code.maven/install :added "3.0"}
(fact "installs packages to the local `.m2` repository"

  (install '[xyz.zcaudate] {:tag :all :print {:item true}})
  
  (install 'xyz.zcaudate/std.lib
           {:tag :all
            :print {:item true}}))

^{:refer code.maven/deploy :added "3.0"}
(comment "deploys packages to a maven repository"

  (deploy '[xyz.zcaudate] {:tag :all}))


^{:refer code.maven/deploy-lein :added "4.0"}
(comment "deploys packages to clojars using lein"
  
  (deploy-lein '[xyz.zcaudate] {:tag :all}))

(comment
  (code.java.compile/javac )
  (:packages (std.config/load))
  (:public (:release (:deploy (config/load))))
  (linkage {:tag :dev})
  (package '[hara] {:tag :public})

  (./code:arrange)
  (install-secure '[hara])
  (deploy '[hara] {:tag :dev})
  (install '[hara] {:tag :public})
  (install '[hara] {:tag :all})
  (install 'hara/base {:tag :all})
  (package '[hara]  {:tag :all})
  (linkage '[hara])
  (clean '[hara]))

