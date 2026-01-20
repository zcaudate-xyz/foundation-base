(ns std.make.project
  (:require [std.lib :as h]
            [std.string :as str]
            [std.make.common :as common]
            [std.make.compile :as compile]
            [std.make.readme :as readme]
            [std.block :as block]
            [std.fs :as fs]
            [std.fs.watch :as watch]))

(defn makefile-parse
  "parses a makefile for it's sections"
  {:added "4.0"}
  ([mcfg]
   (let [out-dir (common/make-dir mcfg)]
     (-> (h/sh "/bin/bash" "-c"
               "grep -oE '^[a-zA-Z0-9_-]+:([^=]|$)' Makefile | sed 's/[^a-zA-Z0-9_-]*$//'"
               {:root (str out-dir)
                :wrap false})
         (str/trim)
         (str/split-lines)))))

(defn build-default
  "builds the default section"
  {:added "4.0"}
  [mcfg]
  (compile/compile mcfg :default))

(defn changed-files
  "gets changed files from result"
  {:added "4.0"}
  [build-result]
  (vec (filter string? (h/flatten-nested build-result))))

(defn is-changed?
  "checks that project result is changed"
  {:added "4.0"}
  [build-result]
  (not (empty? (filter string? (h/flatten-nested build-result)))))

(defn build-all
  "builds all sections in a make config"
  {:added "4.0"}
  ([{:keys [instance] :as mcfg}]
   (let [{:keys [sections]} @instance
         all-keys (conj (sort (set (keys sections)))
                        :default)
         _ (common/make-dir-setup mcfg)
         _ (when (readme/has-orgfile? mcfg)
             (readme/make-readme mcfg)
             (readme/tangle mcfg))]
     (apply compile/compile mcfg all-keys))))

(defn build-at
  "builds a custom section"
  {:added "4.0"}
  [root section]
  (compile/compile-section {:root root
                            :default section}
                           :default
                           section))


(defn def-make-fn
  "def.make implemention"
  {:added "4.0"}
  [sym cfg]
  (let [cvar (resolve sym)
        curr (if cvar
               (if (common/make-config? @cvar)
                 @cvar))
        cfg  (assoc cfg :id (symbol (name (.getName *ns*)) (name sym)))
        out  (if curr
               (do (common/make-config-update curr cfg)
                   curr)
               (common/make-config cfg))
        cvar (if (not curr)
               (intern *ns* sym out)
               cvar)
        {:keys [triggers]} @(:instance out)
        _ (if triggers (common/triggers-set out triggers))]
    cvar))

(defmacro def.make
  "macro to instantiate a section"
  {:added "4.0"}
  [sym cfg]
  `(def-make-fn (quote ~(with-meta sym
                          (merge (meta sym)
                                 (meta &form))))
     ~cfg))

(defn build-triggered
  "builds for a triggering namespace"
  {:added "4.0"}
  ([]
   (build-triggered (h/ns-sym)))
  ([ns]
   (let [mcfgs (common/get-triggered ns)]
     (compile/with:compile-filter
      #{ns}
      (mapv (juxt (fn [{:keys [instance]}]
                    (:id @instance))
                  build-default) mcfgs)))))

(defn file-watcher-heal
  [path ns]
  (let [content (slurp path)
        healed  (block/heal content)]
    (when (not= content healed)
      (h/p "Healed:" ns)
      (spit path healed))))

(defn file-watcher-handler
  "handler for file changes"
  [path & [{:keys [prehooks]}]]
  (let [path-str (str path)]
    (when (and (or (str/ends-with? path-str ".clj")
                   (str/ends-with? path-str ".cljc"))
               (not (str/includes? path-str ".#"))) ;; Emacs lock files
      (let [ns (fs/file-namespace path-str)]
        (when ns
          (try
            (h/p "\n\nNamespace:" ns)
            (file-watcher-heal path ns)
            (h/map-vals (fn [f]
                          (f ns))
                        prehooks)
            (require ns :reload)
            (let [results (build-triggered ns)
                  files   (changed-files results)]
              (when (seq files)
                (h/p "Changed:" files)))
            (catch Throwable t
              (h/p "Build failed for:" ns)
              (h/p (.getMessage t)))))))))

(defn watch
  "starts watching a directory"
  ([path & [{:keys [prehooks]}]]
   (h/prn "Starting build watcher on:" path)
   (let [cb (fn [type file]
              (when (or (= type :modify)
                        (= type :create))
                (#'file-watcher-handler (.getPath file) {:prehooks prehooks})))]
     (watch/start-watcher (watch/watcher path cb {:recursive true
                                                  :types :all})))))

(defn watch-project
  "watches a project"
  {:added "4.0"}
  [mcfg]
  (let [{:keys [default sections]} @(:instance mcfg)
        tasks (concat default (mapcat val sections))
        paths (->> tasks
                   (mapcat :search)
                   (filter identity)
                   (set)
                   (sort))]
    (mapv watch paths)))

(comment
  
  (code.manage/heal-code 'sznui.v1.screens.onboarding.onboarding-step-2-profile
                         {:write true})
  
  )
