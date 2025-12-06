(ns std.lib.git
  (:require [std.lib.os :as os]
            [std.string.common :as str]
            [std.lib.foundation :as h]))

(defn git-result
  "helper to get result from process"
  [process]
  (let [res (os/sh-output process)]
    (assoc res :out (str/trim (:out res)))))

(defn git-run
  "base function for running git commands"
  ([args]
   (git-run args {}))
  ([args opts]
   (let [opts (merge {:wait true :output false :ignore-errors true} opts)
         opts (if (:root opts)
                (update opts :root str)
                opts)
         args (if (vector? args) args [args])
         process (apply os/sh "git" (concat args [opts]))
         {:keys [exit out err] :as res} (git-result process)]
     (if (and (not= exit 0)
              (not (:ignore-errors opts)))
       (h/error (str "Git Error: " (if (empty? err) out err)) res)
       res))))

(defn git
  "generic git command

   (git \"status\")
   (git [\"commit\" \"-m\" \"hello\"])
   (git \"status\" {:root \"...\"})"
  ([args]
   (git args {}))
  ([args opts]
   (git-run args opts)))

(defn init
  "initializes a git repository

   (git/init)"
  ([]
   (init {}))
  ([opts]
   (git "init" opts)))

(defn clone
  "clones a git repository

   (git/clone \"https://github.com/...\")
   (git/clone \"https://github.com/...\" {:dir \"target\"})"
  ([url]
   (clone url {}))
  ([url opts]
   (let [args ["clone" url]
         args (if-let [dir (:dir opts)]
                (conj args dir)
                args)]
     (git args (dissoc opts :dir)))))

(defn status
  "returns the git status as a map

   (git/status)
   => {:branch \"main\"
       :changed [\"file1.txt\"]
       :staged  [\"file2.txt\"]}"
  ([]
   (status {}))
  ([opts]
   (let [{:keys [out]} (git ["status" "--porcelain" "-b"] opts)
         lines (str/split-lines out)]
     (if (empty? lines)
       {:branch "HEAD" :changed [] :staged [] :untracked []}
       (reduce (fn [acc line]
                 (let [code (subs line 0 2)
                       path (str/trim (subs line 3))]
                   (cond
                     (= "##" code)
                     (let [branch-info (subs line 3)
                           ;; Handle "No commits yet on main"
                           branch (if (str/starts-with? branch-info "No commits yet on ")
                                    (subs branch-info (count "No commits yet on "))
                                    ;; Handle "main...origin/main"
                                    (first (str/split branch-info #"\.\.\.")))]
                       (assoc acc :branch branch))

                     (= "??" code)
                     (update acc :untracked conj path)

                     :else
                     (let [x (first code)
                           y (second code)
                           acc (if (contains? #{\M \A \D \R \C} x)
                                 (update acc :staged conj path)
                                 acc)
                           acc (if (contains? #{\M \A \D \R \C} y)
                                 (update acc :changed conj path)
                                 acc)]
                       acc))))
               {:branch nil :changed [] :staged [] :untracked []}
               lines)))))

(defn add
  "adds files to the index

   (git/add \".\")
   (git/add {:all true})
   (git/add [\"file1\" \"file2\"])"
  ([arg]
   (add arg {}))
  ([arg opts]
   (cond
     (map? arg) (add nil arg)
     :else
     (let [args ["add"]
           args (cond
                  (:all opts) (conj args "--all")
                  (vector? arg) (into args arg)
                  (string? arg) (conj args arg)
                  :else args)]
       (git args opts)))))

(defn commit
  "commits changes

   (git/commit {:message \"hello\"})
   (git/commit \"message\")"
  ([arg]
   (if (map? arg)
     (commit nil arg)
     (commit arg {})))
  ([msg opts]
   (let [opts (if (map? msg) msg opts)
         msg  (if (string? msg) msg (:message opts))
         args ["commit"]
         args (cond-> args
                msg (conj "-m" msg)
                (:amend opts) (conj "--amend")
                (:no-verify opts) (conj "--no-verify")
                (:allow-empty opts) (conj "--allow-empty"))]
     (git args opts))))

(defn log
  "returns the git log

   (git/log)
   (git/log {:n 5})"
  ([]
   (log {}))
  ([opts]
   (let [fmt "--pretty=format:%H|%an|%ae|%at|%s"
         args ["log" fmt]
         args (if-let [n (:n opts)]
                (conj args (str "-" n))
                args)
         {:keys [out]} (git args opts)]
     (->> (str/split-lines out)
          (map (fn [line]
                 (let [parts (str/split line #"\|")]
                   (if (>= (count parts) 5)
                     (let [[hash author email time subject] parts]
                       {:hash hash
                        :author author
                        :email email
                        :time (try (Long/parseLong time) (catch Exception _ 0))
                        :subject (str/join "|" (subvec parts 4))}) ;; rejoin subject in case it has pipes
                     {:raw line}))))))))

(defn branch
  "branch operations

   (git/branch) ;; list branches
   (git/branch \"feature\") ;; create branch
   (git/branch \"feature\" {:delete true})"
  ([]
   (branch nil {}))
  ([name]
   (branch name {}))
  ([name opts]
   (cond
     (nil? name)
     (let [{:keys [out]} (git ["branch"] opts)]
       (->> (str/split-lines out)
            (map (fn [s] (str/trim (str/replace s #"\*" ""))))))

     (:delete opts)
     (git ["branch" "-D" name] opts)

     :else
     (git ["branch" name] opts))))

(defn checkout
  "checkouts a branch

   (git/checkout \"main\")
   (git/checkout \"feature\" {:create true})"
  ([branch]
   (checkout branch {}))
  ([branch opts]
   (let [args ["checkout"]
         args (cond-> args
                (:create opts) (conj "-b")
                (:force opts) (conj "-f")
                true (conj branch))]
     (git args opts))))

(defn merge-branch
  "merges a branch

   (git/merge-branch \"feature\")"
  ([branch]
   (merge-branch branch {}))
  ([branch opts]
   (git ["merge" branch] opts)))

(defn pull
  "pulls changes

   (git/pull)
   (git/pull {:rebase true})"
  ([]
   (pull {}))
  ([opts]
   (let [args ["pull"]
         args (cond-> args
                (:rebase opts) (conj "--rebase")
                (:force opts) (conj "--force"))]
     (git args opts))))

(defn push
  "pushes changes

   (git/push)
   (git/push {:force true})"
  ([]
   (push {}))
  ([opts]
   (let [args ["push"]
         args (cond-> args
                (:force opts) (conj "--force")
                (:upstream opts) (conj "-u" (:upstream opts)))]
     (git args opts))))

(defn fetch
  "fetches changes

   (git/fetch)
   (git/fetch {:all true})"
  ([]
   (fetch {}))
  ([opts]
   (let [args ["fetch"]
         args (cond-> args
                (:all opts) (conj "--all")
                (:prune opts) (conj "--prune"))]
     (git args opts))))

(defn reset
  "resets the repo

   (git/reset \"HEAD~1\" {:mode :soft})"
  ([commit]
   (reset commit {}))
  ([commit opts]
   (let [mode (case (:mode opts)
                :soft "--soft"
                :hard "--hard"
                :mixed "--mixed"
                nil)]
     (git (cond-> ["reset"]
            mode (conj mode)
            true (conj commit))
          opts))))

(defn rev-parse
  "gets the commit hash

   (git/rev-parse \"HEAD\")"
  ([ref]
   (rev-parse ref {}))
  ([ref opts]
   (-> (git ["rev-parse" ref] opts)
       :out
       (str/trim))))

;; Complex / Safe Operations

(defn squash
  "squashes the last n commits

   (git/squash {:n 3 :message \"squashed\"})"
  ([opts]
   (let [{:keys [n base message]} opts
         _ (when (and (not n) (not base))
             (h/error "Must provide :n or :base for squash"))
         target (or base (str "HEAD~" n))]
     (try
       (reset target (assoc opts :mode :soft :ignore-errors false))
       (commit {:message (or message "squashed commit")
                :ignore-errors false
                :root (:root opts)})
       (catch Exception e
         (h/error "Squash failed" e))))))

(defn rebase
  "rebases current branch onto upstream
   AUTO-RECOVERS on failure by aborting.

   (git/rebase \"origin/main\")"
  ([upstream]
   (rebase upstream {}))
  ([upstream opts]
   (let [res (git-run ["rebase" upstream] (assoc opts :ignore-errors true))]
     (if (zero? (:exit res))
       res
       (do
         ;; Auto-recover
         (git-run ["rebase" "--abort"] opts)
         (h/error "Rebase failed and was aborted." res))))))

(defn submodule
  "manages submodules

   (git/submodule :update)
   (git/submodule :init)
   (git/submodule :sync)"
  ([action]
   (submodule action {}))
  ([action opts]
   (let [cmd (case action
               :update ["submodule" "update" "--init" "--recursive"]
               :init   ["submodule" "init"]
               :sync   ["submodule" "sync"]
               (if (vector? action)
                 (into ["submodule"] action)
                 ["submodule" (name action)]))]
     (git cmd opts))))
