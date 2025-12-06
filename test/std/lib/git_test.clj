(ns std.lib.git-test
  (:use code.test)
  (:require [std.lib.git :as git]
            [std.fs :as fs]
            [std.lib.foundation :as h]))

(fact "git init and status"
  (let [root (fs/create-tmpdir "git-test")]
    (spit (str root "/file.txt") "hello")

    (git/init {:root root})
    (:branch (git/status {:root root}))
    => (any "master" "main")

    (:untracked (git/status {:root root}))
    => ["file.txt"]

    (fs/delete root)))

(fact "git add and commit"
  (let [root (fs/create-tmpdir "git-test")]
    (git/init {:root root})
    (spit (str root "/file.txt") "hello")

    (git/add "." {:root root})
    (:staged (git/status {:root root}))
    => ["file.txt"]

    (git/commit {:message "first" :root root})
    (:changed (git/status {:root root}))
    => []

    (count (git/log {:root root}))
    => 1

    (fs/delete root)))

(fact "git branch and checkout"
  (let [root (fs/create-tmpdir "git-test")]
    (git/init {:root root})
    (spit (str root "/f1") "1")
    (git/add "." {:root root})
    (git/commit "c1" {:root root})

    (git/checkout "feature" {:create true :root root})
    (:branch (git/status {:root root}))
    => "feature"

    (fs/delete root)))

(fact "git squash"
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

(fact "git rebase abort recovery"
  (let [root (fs/create-tmpdir "git-test")]
    (git/init {:root root})
    (git/git ["config" "user.email" "you@example.com"] {:root root})
    (git/git ["config" "user.name" "Your Name"] {:root root})

    ;; Commit 1
    (spit (str root "/common.txt") "base\n")
    (git/add "." {:root root})
    (git/commit "c1" {:root root})

    ;; Branch feature
    (git/checkout "feature" {:create true :root root})
    (spit (str root "/common.txt") "feature change\n")
    (git/add "." {:root root})
    (git/commit "c2-feature" {:root root})

    ;; Back to main, conflict
    (git/checkout "master" {:root root})
    (spit (str root "/common.txt") "main change\n")
    (git/add "." {:root root})
    (git/commit "c2-main" {:root root})

    ;; Try rebase feature onto master (should conflict)
    (git/checkout "feature" {:root root})

    (try
      (git/rebase "master" {:root root})
      (catch Exception e
        ;; Should fail
        (:out (ex-data e)) => (fn [s] (or (re-find #"Conflict" s)
                                          (re-find #"conflict" s)))

        ;; Should be aborted
        (let [st (git/status {:root root})]
          ;; If aborted, we are back to feature tip, no interactive rebase state
          (:branch st) => "feature")))

    (fs/delete root)))
