(ns std.make.github-test
  (:require [std.make.github :refer :all])
  (:use code.test))

^{:refer std.make.github/with-verbose :added "4.0"}
(fact "with verbose github messages")

^{:refer std.make.github/gh-sh-opts :added "4.0"}
(fact "creates standard shell opts")

^{:refer std.make.github/gh-commit :added "4.0"}
(fact "creates a commit in the repo")

^{:refer std.make.github/gh-push :added "4.0"}
(fact "pushes to github")

^{:refer std.make.github/gh-save :added "4.0"}
(fact "does a commit and push to github")

^{:refer std.make.github/gh-user :added "4.0"}
(fact "gets the github user")

^{:refer std.make.github/gh-token :added "4.0"}
(fact "gets the github token")

^{:refer std.make.github/gh-exists? :added "4.0"}
(fact "checks that github repo exists")

^{:refer std.make.github/gh-setup-remote :added "4.0"}
(fact "creates the github remote repo")

^{:refer std.make.github/gh-setup-local-init :added "4.0"}
(fact "initiates local repo to remote")

^{:refer std.make.github/gh-setup-local-clone :added "4.0"}
(fact "clones `.build` directory from remote")

^{:refer std.make.github/gh-clone :added "4.0"}
(fact "forces a git clone")

^{:refer std.make.github/gh-setup :added "4.0"}
(fact "sets up repo project")

^{:refer std.make.github/gh-local-purge :added "4.0"}
(fact "purges all checked in files from git repo")

^{:refer std.make.github/gh-refresh :added "4.0"}
(fact "regenerates the git repo")

^{:refer std.make.github/gh-dwim-init :added "4.0"}
(fact "gh dwim init")

^{:refer std.make.github/gh-dwim-push :added "4.0"}
(fact "gh dwim push")
