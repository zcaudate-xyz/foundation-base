(ns std.make.project
  (:require [std.lib :as h]
            [std.string :as str]
            [std.make.common :as common]
            [std.make.compile :as compile]
            [std.make.readme :as readme]
            [std.fs :as fs]))

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
        out  (if curr
               (do (common/make-config-update curr cfg)
                   curr)
               (common/make-config (assoc cfg :id (symbol (name (.getName *ns*)) (name sym)))))
        cvar (if (not curr)
               (intern *ns* sym out)
               cvar)
        {:keys [triggers]} @(:instance out)
        _ (if triggers (common/triggers-set (h/var-sym cvar) triggers))]
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
     (mapv build-default mcfgs))))
