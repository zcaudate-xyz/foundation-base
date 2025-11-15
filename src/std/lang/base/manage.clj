(ns std.lang.base.manage
  (:require [std.task :as task]
            [std.lang.base.impl :as impl]
            [std.lang.base.library :as lib]
            [std.lang.base.util :as ut]
            [std.lang.base.workspace :as workspace]
            [std.string :as str]
            [std.lib :as h :refer [definvoke]]
            [std.print.format.common :as common]
            [std.print.ansi :as ansi]))

(def ^:dynamic *current-lang* nil)

(def ^:dynamic *current-module* nil)

(def ^:dynamic *current-entry* nil)

(def library-template
  {:params    {:print {:function false
                       :item false
                       :result false
                       :summary false}}
   :warning   {:output  :data}
   :error     {:output  :data}
   :item      {:pre     identity
               :post    identity
               :output  identity
               :display identity}
   :result    {:keys    {:count count}
               :ignore  empty?
               :output  identity
               :columns [{:key    :key
                          :align  :left
                          :color  #{:white :bold}}
                         {:key    :data
                          :align  :left
                          :length 60}]}
   :summary   {:aggregate {:total [:count + 0]}}})

(defmethod task/task-defaults :lang.library
  ([_]
   library-template))

(defn lib-overview-format
  "formats the lib overview"
  {:added "4.0"}
  [{:keys [modules] :as book} & []]
  (let [order (sort (keys modules))]
    (->> order
         (map (fn [id]
                (let [module (get modules id)]
                  (str (common/pad:right
                        (ansi/style (str id) #{:bold :white})
                        50)
                       (common/pad:left
                        (str/join
                         (map (fn [[d style]]
                                (if (pos? d)
                                  (ansi/style (common/pad:left (str d) 6) style)
                                  (ansi/style (common/pad:left "." 6) #{:grey})))
                              [[(count (:code module)) #{:green :bold}]
                               [(count (:fragment module)) #{:magenta}]
                               [(dec (count (:link module))) #{:blue}]]))
                        18)))))
         (str/join "\n"))))

(definvoke ^{:arglists '([] [lang])}
  lib-overview
  "specifies lib overview task"
  {:added "4.0"}
  [:task {:template :lang.library
          :construct {:env      (fn [_]
                                  (let [snapshot (lib/get-snapshot (impl/default-library))]
                                    {:snapshot snapshot}))
                      :input    (fn [_] *current-lang*)
                      :lookup   (fn [_ {:keys [snapshot]}]
                                  snapshot)}
          :params {:title "LIBRARY OVERVIEW - [:code :fragment :link]"
                   :print {:function false
                           :item false
                           :result true
                           :summary true}}
          :item    {:list    (fn [snapshot _]  (sort (keys snapshot)))}
          :result  {:format  {:data lib-overview-format}
                    :columns [{:key    :key
                               :align  :left
                               :color  #{:yellow :bold}}
                              {:key    :data
                               :align  :left
                               :length 60}]}
          :main    {:argcount 4
                    :fn  (fn [lang params snapshot _]
                           (get-in snapshot [lang :book]))}}])

(comment
  (./create-tests)
  (lib-overview '[(:js)])
  (lib-overview '#{:js})
  (lib-overview :all))

(defn lib-module-env
  "compiles the lib-module task environment
 
   (lib-module-env nil)
   => map?"
  {:added "4.0"}
  [_]
  {:modules (apply merge
                   (map (fn [[id {:keys [book]}]]
                          (:modules book))
                        (lib/get-snapshot (impl/default-library))))})

(defn lib-module-filter
  "filters modules based on :lang"
  {:added "4.0"}
  [module-id {:keys [lang]} modules _]
  (if-let [module (get modules (or module-id
                                   (h/ns-sym)))]
    (if (or (not lang)
            (= lang (:lang module))) 
      module)))

(defmethod task/task-defaults :lang.module
  ([_]
   (h/merge-nested library-template
                   {:construct {:env      #'lib-module-env
                                :input    (fn [_] *current-lang*)
                                :lookup   (fn [_ {:keys [modules]}] modules)}
                    :params {:print {:function false
                                     :item false
                                     :result true
                                     :summary true}}
                    :item    {:list    (fn [modules _] (sort (keys modules)))}
                    :main    {:argcount 4}})))

;;
;;
;; OVERVIEW
;;
;;

(defn lib-module-overview-format
  "formats the module overview"
  {:added "4.0"}
  [{:keys [lang id code fragment link native suppress export] :as module} & [{:keys [counts
                                                                                     deps]}]]
  (str/join-lines
    [(if (not (false? counts))
       (str (ansi/style (common/pad:left (name lang) 10) #{:yellow :bold})
            "  "
            (ansi/style (common/pad:right (str (count code)) 5)
                        #{:bold :green})
            (ansi/style (common/pad:right (str (count fragment)) 5)
                        #{:magenta})
            "  "
            (:as export)))
     (if deps
       (str/indent  (str/join-lines
                     (concat (map #(ansi/style (str %) #{:blue})
                                  (vals (dissoc link '-)))
                             (map #(ansi/style (str %) #{:cyan :bold})
                                  (keys native))))
                    12))]))

(definvoke ^{:arglists '([] [module-id] [module-id {:keys [lang entry]}])}
  lib-module-overview
  "lists all modules"
  {:added "4.0"}
  [:task {:template :lang.module
          :params {:title "MODULES OVERVIEW"}
          :result  {:format  {:data #'lib-module-overview-format}}
          :main    {:fn  #'lib-module-filter}}])

(comment
  (lib-module-overview :all {:lang :lua})
  (lib-module-overview :all {:lang :js
                             :counts true
                             :deps true})
  (lib-module-overview :all))

;;
;;
;; ENTRIES
;;
;;

(defn lib-module-entries-format-section
  "formats either code or fragment section"
  {:added "4.0"}
  [module section style & [op]]
  (->> (vals (get module section))
       (sort-by (fn [e]
                  (mapv #(get e %) [:namespace :line :time])))
       (map (fn [{:keys [id line] :as e}]
              (str (common/pad:right (str line) 5)
                   (ansi/style
                    (str (common/pad:right 
                          (str id)
                          45))
                    style)
                   (common/pad:left
                    (str (if op (or (:op-key e)
                                    (if (:template e)
                                      :macro
                                      :fragment))))
                    12))))
       (str/join "\n")))

(defn lib-module-entries-format
  "formats the entries of a module"
  {:added "4.0"}
  [{:keys [lang code fragment export] :as module} & [params]]
  (str (str/join "\n--------------------------------------------------------------\n"
         [(str (ansi/style (name lang) #{:yellow :bold}) " " (:as export))
          (->> (lib-module-overview-format module {:counts false
                                                   :deps true})
               (str/split-lines)
               (map str/trim-left)
               (str/join "\n"))
          (str/join "\n--------------------------------------------------------------\n"
            [(if (not (false? (:code params)))
               (lib-module-entries-format-section module :code #{:green :bold} true))
             (if (not (false? (:fragment params)))
               (lib-module-entries-format-section module :fragment #{:magenta} true))])])
       "\n"))

(definvoke ^{:arglists '([] [module-id] [module-id {:keys [lang entry]}])}
  lib-module-entries
  "outputs module entries"
  {:added "4.0"}
  [:task {:template :lang.module
          :params {:title "MODULES OVERVIEW"}
          :result  {:format  {:data #'lib-module-entries-format}}
          :main    {:fn  #'lib-module-filter}}])

(comment (comment
           (lib-module-entries '[js])
           (lib-module-entries '[kmi])
           (lib-module-entries :all {:lang :lua})
           (lib-module-entries :all)))

;;
;;
;; PURGE
;;
;;

(defn lib-module-purge-fn
  "the purge module function"
  {:added "4.0"}
  [module-id params modules env]
  (if-let [{:keys [lang id] :as module} (lib-module-filter module-id params modules env)]
    (second (first (lib/delete-module! (impl/default-library)
                                       (:lang module)
                                       module-id)))))

(definvoke ^{:arglists '([] [module-id] [module-id {:keys [lang]}])}
  lib-module-purge
  "purges modules"
  {:added "4.0"}
  [:task {:template :lang.module
          :params {:title "PURGE MODULES"}
          :result  {:format  {:data  #'lib-module-entries-format}}
          :main    {:fn  #'lib-module-purge-fn}}])

(comment (comment
           (lib-module-purge '[rt.postgres])
           (do (./reset '[rt.postgres])
               (require '[rt.postgres])
               (lib-overview '[:postgres]))
           (lib-module-entries '[statsdb])
           (lib-module-purge '[statsdb])
           (do (./reset '[statsdb])
               (require '[statsdb.core.infra])
               (lib-overview '[:postgres]))))

;;
;;
;; UNUSED DEPS
;;
;;

(defn lib-module-unused-fn
  "analyzes the module for unused links"
  {:added "4.0"}
  [module-id params modules env]
  (if-let [{:keys [code link] :as module}
           (lib-module-filter module-id params modules env)]
    (let [{:keys [ignore]} params
          lookup (h/transpose link)
          unused (volatile! (apply dissoc link '-
                                   (mapcat (fn [ns]
                                             [ns (lookup ns)])
                                           ignore)))]
      (doseq [entry (vals code)]
        (let [{:keys [form-input]} entry]
          (h/prewalk (fn [form]
                       (if-let [ns (and (symbol? form)
                                        (namespace form))]
                         (let [ns (symbol ns)] 
                           (vswap! unused dissoc ns (lookup ns))))
                       form)
                     form-input)))
      (vec (sort (vals @unused))))
    []))

(definvoke ^{:arglists '([] [module-id] [module-id {:keys [lang]}])}
  lib-module-unused
  "lists unused modules"
  {:added "4.0"}
  [:task {:template :lang.module
          :params {:title "UNUSED DEPENDENCIES"}
          :result  {:format  {:data  (fn [result _]
                                       (str/join "\n" result))}}
          :main    {:fn  #'lib-module-unused-fn}}])

(comment (comment
           (lib-module-unused [(fn [s]
                                 (str/ends-with? s "test"))])
           
           (lib-module-unused [(fn [s]
                                 (not (str/ends-with? s "test")))]
                              {:ignore '[xt.lang.base-lib
                                         js.react
                                         js.core]})
           
           (lib-module-unused '[#"test$"])
           (lib-module-unused '[melbourne])))

;;
;;
;; MISSING LINES
;;
;;

(defn lib-module-missing-line-number-fn
  "helper function to `lib-module-missing-line-number`"
  {:added "4.0"}
  [module-id params modules env]
  (if-let [{:keys [code link] :as module}
           (lib-module-filter module-id params modules env)]
    (if (some (comp nil? :line) (vals code))
      module)))

(definvoke ^{:arglists '([] [module-id] [module-id {:keys [lang]}])}
  lib-module-missing-line-number
  "lists modules with entries that are missing line numbers (due to inproper macros)"
  {:added "4.0"}
  [:task {:template :lang.module
          :params {:title "MISSING LINE NUMBER"}
          :result  {:format  {:data  #'lib-module-entries-format}}
          :main    {:fn  #'lib-module-missing-line-number-fn}}])


(comment (comment
           (lib-module-missing-line-number :all)))

;;
;;
;; UNUSED DEPS
;;
;;

(defn lib-module-incorrect-alias-fn
  "helper function to `lib-module-incorrect-alias`"
  {:added "4.0"}
  [module-id params modules env]
  (if-let [{:keys [link] :as module}
           (lib-module-filter module-id params modules env)]
    (let [all (filter (fn [[id ns]]
                        (when (and (< 3 (count (str id)))
                                   (str/includes? (str id) "-")
                                   (not (str/ends-with? (str ns)
                                                        (str id))))
                          [id ns]))
                      link)]
      (when (not-empty all)
        all))))

(definvoke ^{:arglists '([] [module-id] [module-id {:keys [lang]}])}
  lib-module-incorrect-alias
  "lists modules that have an incorrect alias"
  {:added "4.0"}
  [:task {:template :lang.module
          :params {:title "INCORRECT ALIASES"}
          :result  {:format  {:data  (fn [result _]
                                       (str/join "\n" result))}}
          :main    {:fn  #'lib-module-incorrect-alias-fn}}])


(comment (comment
           (lib-module-incorrect-alias :all)))
