(ns std.lib.git-test
  (:use code.test)
  (:require [std.lib.git :as git]
            [std.fs :as fs]
            [std.lib.foundation :as h]
            [std.lib.os :as os]))

^{:refer std.lib.git/git-result :added "4.1"}
(fact "helper to get result from process"
  (with-redefs [os/sh-output identity]
    (git/git-result {:out " hello \n" :err "" :exit 0}))
  => {:out "hello" :err "" :exit 0})

^{:refer std.lib.git/git-run :added "4.1"}
(fact "base function for running git commands"
  (let [root (fs/create-tmpdir "git-test")]
    (git/git-run ["init"] {:root root})
    => (contains {:exit 0})
    (fs/delete root)))

^{:refer std.lib.git/git :added "4.1"}
(fact "generic git command"
  (let [root (fs/create-tmpdir "git-test")]
    (git/git "init" {:root root})
    => (contains {:exit 0})
    (fs/delete root)))

^{:refer std.lib.git/init :added "4.1"}
(fact "initializes a git repository"
  (let [root (fs/create-tmpdir "git-test")]
    (git/init {:root root})
    (:branch (git/status {:root root}))
    => (any "master" "main")
    (fs/delete root)))

^{:refer std.lib.git/clone :added "4.1"}
(fact "clones a git repository"
  (let [root (fs/create-tmpdir "git-origin")
        target (fs/create-tmpdir "git-target")]
    (git/init {:root root})
    (spit (str root "/file.txt") "hello")
    (git/add "." {:root root})
    (git/git ["config" "user.email" "you@example.com"] {:root root})
    (git/git ["config" "user.name" "Your Name"] {:root root})
    (git/commit "initial" {:root root})

    (fs/delete target)
    (git/clone (str "file://" root) {:dir (str target)})
    => (contains {:exit 0})

    (fs/exists? (str target "/file.txt"))
    => true

    (fs/delete root)
    (fs/delete target)))

^{:refer std.lib.git/status :added "4.1"}
(fact "returns the git status as a map"
  (let [root (fs/create-tmpdir "git-test")]
    (git/init {:root root})
    (:branch (git/status {:root root}))
    => (any "master" "main")

    (spit (str root "/file.txt") "hello")
    (:untracked (git/status {:root root}))
    => ["file.txt"]

    (git/add "." {:root root})
    (:staged (git/status {:root root}))
    => ["file.txt"]
    (fs/delete root)))

^{:refer std.lib.git/add :added "4.1"}
(fact "adds files to the index"
  (let [root (fs/create-tmpdir "git-test")]
    (git/init {:root root})
    (spit (str root "/file.txt") "hello")
    (git/add "." {:root root})
    (:staged (git/status {:root root}))
    => ["file.txt"]
    (fs/delete root)))

^{:refer std.lib.git/commit :added "4.1"}
(fact "commits changes"
  (let [root (fs/create-tmpdir "git-test")]
    (git/init {:root root})
    (git/git ["config" "user.email" "you@example.com"] {:root root})
    (git/git ["config" "user.name" "Your Name"] {:root root})
    (spit (str root "/file.txt") "hello")
    (git/add "." {:root root})

    (git/commit {:message "first" :root root})
    (:changed (git/status {:root root}))
    => []

    (count (git/log {:root root}))
    => 1
    (fs/delete root)))

^{:refer std.lib.git/log :added "4.1"}
(fact "returns the git log"
  (let [root (fs/create-tmpdir "git-test")]
    (git/init {:root root})
    (git/git ["config" "user.email" "you@example.com"] {:root root})
    (git/git ["config" "user.name" "Your Name"] {:root root})
    (spit (str root "/file.txt") "hello")
    (git/add "." {:root root})
    (git/commit "first" {:root root})

    (count (git/log {:root root}))
    => 1
    (fs/delete root)))

^{:refer std.lib.git/branch :added "4.1"}
(fact "branch operations"
  (let [root (fs/create-tmpdir "git-test")]
    (git/init {:root root})
    (git/git ["config" "user.email" "you@example.com"] {:root root})
    (git/git ["config" "user.name" "Your Name"] {:root root})
    (spit (str root "/f1") "1")
    (git/add "." {:root root})
    (git/commit "c1" {:root root})

    (git/checkout "feature" {:create true :root root})
    (git/branch {:root root})
    => (contains "feature")
    (fs/delete root)))

^{:refer std.lib.git/checkout :added "4.1"}
(fact "checkouts a branch"
  (let [root (fs/create-tmpdir "git-test")]
    (git/init {:root root})
    (git/git ["config" "user.email" "you@example.com"] {:root root})
    (git/git ["config" "user.name" "Your Name"] {:root root})
    (spit (str root "/f1") "1")
    (git/add "." {:root root})
    (git/commit "c1" {:root root})

    (git/checkout "feature" {:create true :root root})
    (:branch (git/status {:root root}))
    => "feature"
    (fs/delete root)))

^{:refer std.lib.git/merge-branch :added "4.1"}
(fact "merges a branch"
  (let [root (fs/create-tmpdir "git-test")]
    (git/init {:root root})
    (git/git ["config" "user.email" "you@example.com"] {:root root})
    (git/git ["config" "user.name" "Your Name"] {:root root})
    (spit (str root "/f1") "1")
    (git/add "." {:root root})
    (git/commit "c1" {:root root})

    (let [main-branch (:branch (git/status {:root root}))]
      (git/checkout "feature" {:create true :root root})
      (spit (str root "/f2") "2")
      (git/add "." {:root root})
      (git/commit "c2" {:root root})

      (git/checkout main-branch {:root root})
      (git/merge-branch "feature" {:root root})
      (fs/exists? (str root "/f2"))
      => true)
    (fs/delete root)))

^{:refer std.lib.git/pull :added "4.1"}
(fact "pulls changes"
  (let [root (fs/create-tmpdir "git-origin")
        target (fs/create-tmpdir "git-target")]
    (git/init {:root root})
    (git/git ["config" "user.email" "you@example.com"] {:root root})
    (git/git ["config" "user.name" "Your Name"] {:root root})
    (spit (str root "/file.txt") "hello")
    (git/add "." {:root root})
    (git/commit "initial" {:root root})

    (fs/delete target)
    (git/clone (str "file://" root) {:dir (str target)})

    (spit (str root "/file2.txt") "world")
    (git/add "." {:root root})
    (git/commit "second" {:root root})

    (git/pull {:root target})
    (fs/exists? (str target "/file2.txt"))
    => true

    (fs/delete root)
    (fs/delete target)))

^{:refer std.lib.git/push :added "4.1"}
(fact "pushes changes"
  (let [root (fs/create-tmpdir "git-origin")
        target (fs/create-tmpdir "git-target")]
    (git/init {:root root})
    (git/git ["config" "receive.denyCurrentBranch" "ignore"] {:root root})
    (git/git ["config" "user.email" "you@example.com"] {:root root})
    (git/git ["config" "user.name" "Your Name"] {:root root})
    (spit (str root "/file.txt") "hello")
    (git/add "." {:root root})
    (git/commit "initial" {:root root})

    (fs/delete target)
    (git/clone (str "file://" root) {:dir (str target)})
    (git/git ["config" "user.email" "you@example.com"] {:root target})
    (git/git ["config" "user.name" "Your Name"] {:root target})

    (spit (str target "/file2.txt") "world")
    (git/add "." {:root target})
    (git/commit "second" {:root target})

    (git/push {:root target})
    => (contains {:exit 0})

    (git/reset "HEAD" {:mode :hard :root root})
    (fs/exists? (str root "/file2.txt"))
    => true

    (fs/delete root)
    (fs/delete target)))

^{:refer std.lib.git/fetch :added "4.1"}
(fact "fetches changes"
  (let [root (fs/create-tmpdir "git-origin")
        target (fs/create-tmpdir "git-target")]
    (git/init {:root root})
    (git/git ["config" "user.email" "you@example.com"] {:root root})
    (git/git ["config" "user.name" "Your Name"] {:root root})
    (spit (str root "/file.txt") "hello")
    (git/add "." {:root root})
    (git/commit "initial" {:root root})

    (fs/delete target)
    (git/clone (str "file://" root) {:dir (str target)})

    (spit (str root "/file2.txt") "world")
    (git/add "." {:root root})
    (git/commit "second" {:root root})

    (git/fetch {:root target})
    => (contains {:exit 0})

    (fs/delete root)
    (fs/delete target)))

^{:refer std.lib.git/reset :added "4.1"}
(fact "resets the repo"
  (let [root (fs/create-tmpdir "git-test")]
    (git/init {:root root})
    (git/git ["config" "user.email" "you@example.com"] {:root root})
    (git/git ["config" "user.name" "Your Name"] {:root root})
    (spit (str root "/f1") "1")
    (git/add "." {:root root})
    (git/commit "c1" {:root root})

    (spit (str root "/f2") "2")
    (git/add "." {:root root})
    (git/commit "c2" {:root root})

    (git/reset "HEAD~1" {:mode :hard :root root})
    (fs/exists? (str root "/f2"))
    => false
    (fs/delete root)))

^{:refer std.lib.git/rev-parse :added "4.1"}
(fact "gets the commit hash"
  (let [root (fs/create-tmpdir "git-test")]
    (git/init {:root root})
    (git/git ["config" "user.email" "you@example.com"] {:root root})
    (git/git ["config" "user.name" "Your Name"] {:root root})
    (spit (str root "/f1") "1")
    (git/add "." {:root root})
    (git/commit "c1" {:root root})

    (count (git/rev-parse "HEAD" {:root root}))
    => 40
    (fs/delete root)))

^{:refer std.lib.git/squash :added "4.1"}
(fact "squashes the last n commits"
  (let [root (fs/create-tmpdir "git-test")]
    (git/init {:root root})
    (git/git ["config" "user.email" "you@example.com"] {:root root})
    (git/git ["config" "user.name" "Your Name"] {:root root})

    (dotimes [i 3]
      (spit (str root "/f" i) (str i))
      (git/add "." {:root root})
      (git/commit (str "c" i) {:root root}))

    (count (git/log {:root root})) => 3

    (git/squash {:n 2 :message "squashed" :root root})

    (count (git/log {:root root})) => 2

    (-> (git/log {:root root}) first :subject)
    => "squashed"

    (fs/delete root)))

^{:refer std.lib.git/rebase :added "4.1"}
(fact "rebases current branch onto upstream"
  (let [root (fs/create-tmpdir "git-test")]
    (git/init {:root root})
    (git/git ["config" "user.email" "you@example.com"] {:root root})
    (git/git ["config" "user.name" "Your Name"] {:root root})

    ;; Commit 1
    (spit (str root "/common.txt") "base\n")
    (git/add "." {:root root})
    (git/commit "c1" {:root root})

    (let [main-branch (:branch (git/status {:root root}))]
      ;; Branch feature
      (git/checkout "feature" {:create true :root root})
      (spit (str root "/common.txt") "feature change\n")
      (git/add "." {:root root})
      (git/commit "c2-feature" {:root root})

      ;; Back to main, conflict
      (git/checkout main-branch {:root root})
      (spit (str root "/common.txt") "main change\n")
      (git/add "." {:root root})
      (git/commit "c2-main" {:root root})

      ;; Try rebase feature onto master (should conflict)
      (git/checkout "feature" {:root root})

      (try
        (git/rebase main-branch {:root root})
        (catch Exception e
          ;; Should fail
          (:out (ex-data e)) => (fn [s] (or (re-find #"Conflict" s)
                                            (re-find #"conflict" s)))

          ;; Should be aborted
          (let [st (git/status {:root root})]
            ;; If aborted, we are back to feature tip, no interactive rebase state
            (:branch st) => "feature"))))

    (fs/delete root)))

^{:refer std.lib.git/submodule :added "4.1"}
(fact "manages submodules"
  (let [root (fs/create-tmpdir "git-test")]
    (git/init {:root root})
    (git/submodule :init {:root root})
    => (contains {:exit 0})
    (fs/delete root)))
