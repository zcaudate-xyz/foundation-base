(ns rt.postgres.base.grammar.form-defconst
  (:require [rt.postgres.base.grammar.common :as common]
            [rt.postgres.base.application :as app]
            [rt.postgres.base.grammar.common-tracker :as tracker]
            [rt.postgres.runtime.impl-base :as base]
            [rt.postgres.runtime.impl-insert :as insert]
            [rt.postgres.runtime.impl-main :as main]
            [std.lang :as l]
            [std.lang.base.emit-preprocess :as preprocess] [std.lang.base.preprocess-base :as preprocess-base]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.util :as ut]
            [std.lib.foundation :as f]))

(defn pg-defconst-hydrate
  "creates the"
  {:added "4.0"}
  ([[op sym [spec-sym] data] grammar {:keys [module
                                             snapshot]
                                      :as mopts}]
   (let [[data]  (preprocess/to-staging data
                                        grammar
                                        (:modules (snap/get-book snapshot :postgres))
                                        mopts)
         mmeta (common/pg-hydrate-module-static module)
         [entry tsch mopts] (base/prep-table spec-sym true mopts)
         {:static/keys [tracker]} entry
         {:keys [track] :as msym} (meta sym)
         _   (if (and tracker (nil? track))
               (f/error "Requires a track meta" {:symbol sym
                                                 :table (ut/sym-full entry)
                                                 :meta (meta sym)}))
         link  (select-keys entry [:id :module :section :lang])
         hmeta  (cond (not track)
                      {:static/table link
                       :static/form  (list 'do:block
                                      (list :% (insert/t-upsert-raw [entry tsch mopts]
                                                                    data
                                                                    {:as :raw})
                                            \;))}
                      
                      :else
                      (let [{:keys [schema]} mopts

                            [track-entry
                             track-form]  (cond (not (symbol? track))
                                                [nil track]
                                                
                                                :else
                                                (let [[_ track-entry] (base/prep-entry track mopts)
                                                      [_ track-sym track-data] (:form track-entry)
                                                      {:static/keys [table]} (meta track-sym)
                                                      track-tsch  (or (get-in schema [:tree (keyword (name (:id table)))])
                                                                     (f/error "Not found." {:input table
                                                                                            :keys (keys (:tree schema))}))]
                                                  [track-entry (main/t-select-raw
                                                                [table track-tsch mopts]
                                                                {:where {:id (:id track-data)}})]))
                            upsert-form (insert/t-upsert-raw
                                         [entry tsch mopts]
                                         data
                                         {:as :raw
                                          :track 'o-track})
                            form  (list 'let ['o-track track-form]
                                        upsert-form)]
                        {:static/table link
                         :static/track (if track-entry
                                         (select-keys track-entry [:id :module :section :lang]))
                         :static/form (list 'do:block form)}))]
     [(merge hmeta mmeta) (list op (with-meta sym
                                     (merge (dissoc msym :track)
                                            hmeta
                                            mmeta))
                                data)])))

(defn pg-defconst
  "emits the static form"
  {:added "4.0"}
  ([[_ sym link data]]
   (:static/form (meta sym))))
