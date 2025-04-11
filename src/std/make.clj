(ns std.make
  (:require [std.lib :as h]
            [std.string :as str]
            [std.make.common :as common]
            [std.make.compile :as compile]
            [std.make.github :as github]
            [std.make.project :as project]
            [std.make.readme :as readme]
            [std.make.bulk :as bulk]))

(h/intern-in
 compile/with:mock-compile
 [types:add    compile/types-add]
 [types:list   compile/types-list]
 [types:remove compile/types-remove]
 [build        compile/compile]
 
 common/with:internal-shell
 common/triggers-clear
 common/triggers-get
 common/triggers-list
 common/triggers-purge
 common/triggers-set
 common/get-triggered
 common/make-config
 
 [dir               common/make-dir]
 [dir:exists?       common/make-dir-exists?]
 [dir:teardown      common/make-dir-teardown]
 [dir:repo-rebuild  github/gh-refresh]
 [dir:repo-setup    github/gh-setup]
 
 [run:shell    common/make-shell]
 [run          common/make-run]
 [run-close    common/make-run-close]
 [run-internal common/make-run-internal]
 [run:init    common/make-run-init]
 [run:package common/make-run-package]
 [run:dev     common/make-run-dev]
 [run:test    common/make-run-test]
 [run:release common/make-run-release]
 [run:start   common/make-run-start]
 [run:stop    common/make-run-stop]

 [org:tangle  readme/tangle]
 [org:readme  readme/make-readme]
 
 [gh:token    github/gh-token]
 [gh:user     github/gh-user]
 [gh:exists?  github/gh-exists?]
 [gh:commit   github/gh-commit]
 [gh:push     github/gh-push]
 [gh:save     github/gh-save]
 [gh:clone    github/gh-clone]
 [gh:setup    github/gh-setup]
 [gh:setup-init    github/gh-setup-local-init]
 [gh:setup-remote  github/gh-setup-remote]
 [gh:local-purge   github/gh-local-purge]
 [gh:dwim-init     github/gh-dwim-init]
 [gh:dwim-push     github/gh-dwim-push]
 github/with-verbose
 
 project/def.make
 project/build-all
 project/build-at
 project/build-default
 project/build-triggered
 project/is-changed?
 
 [bulk-container-build bulk/make-bulk-container-build]
 [bulk-container-filter bulk/make-bulk-container-filter]
 [bulk-build bulk/make-bulk-build]
 [bulk bulk/make-bulk]
 [bulk-gh-init bulk/make-bulk-gh-init]
 [bulk-gh-push bulk/make-bulk-gh-push])


