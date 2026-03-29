(ns std.lang.base.script-xtalk
  (:require [std.lang.base.impl-entry :as entry]
            [std.lang.base.library :as lib]
            [std.lang.base.grammar-xtalk-system :as xtalk-system]
            [std.lib.foundation :as f]))

(defn xtalk-book?
  "checks if a book participates in the xtalk inheritance path"
  {:added "4.1"}
  [{:keys [lang parent]}]
  (boolean (or (= :xtalk lang)
               (= :xtalk parent))))

(defn validate-xtalk-profiles!
  "fails early when a form uses xtalk capabilities not fully supported by the target grammar"
  {:added "4.1"}
  [lang grammar {:keys [ops profiles]}]
  (let [ops      (or ops #{})
        profiles (or profiles #{})
        missing  (xtalk-system/xtalk-grammar-missing-profiles grammar profiles)]
    (when (not-empty missing)
      (let [supported-ops (xtalk-system/xtalk-grammar-supported-ops grammar)
            missing-ops   (->> ops
                               (remove supported-ops)
                               set)]
        (f/error "Grammar does not support required xtalk profiles."
                 {:lang lang
                  :missing-profiles missing
                  :required-profiles profiles
                  :missing-ops missing-ops
                  :xtalk-ops ops})))))

(defn hydrate-xtalk-scan
  "hydrates an entry input form and scans xtalk usage on the hydrated form"
  {:added "4.1"}
  [entry reserved grammar modules mopts]
  (let [module (assoc (get modules (:module entry))
                      :display :brief)
        [_ form-hydrate] (entry/hydrate-form (:form-input entry)
                                             reserved
                                             grammar
                                             (merge {:module module
                                                     :entry (assoc entry :display :brief)}
                                                    mopts))]
    (xtalk-system/scan-xtalk form-hydrate)))

(defn prepare-entry!
  "hydrates and validates xtalk metadata for a top-level entry when applicable"
  {:added "4.1"}
  [lib {:keys [entry lang module reserved snapshot]
        :or {snapshot (lib/get-snapshot lib)}}]
  (let [book (lib/get-book lib lang)]
    (when (xtalk-book? book)
      (let [{:keys [grammar modules]} book
            {:as xtalk-info}
          (hydrate-xtalk-scan entry
                              reserved
                              grammar
                              modules
                              {:lang lang
                               :snapshot snapshot
                               :module (get modules module)})]
        (validate-xtalk-profiles! lang grammar xtalk-info)
        xtalk-info))))
