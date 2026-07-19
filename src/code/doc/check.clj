(ns code.doc.check
  (:require [clojure.string :as string]
            [code.doc.executive :as executive]
            [code.doc.link.test :as link.test]
            [code.doc.prepare :as prepare]
            [code.project :as project]
            [std.config :as config]))

(defn make-check-project
  "makes an env for code.doc check tasks"
  {:added "4.1"}
  ([]
   (let [project (project/project)]
     (assoc project
            :lookup  (project/file-lookup project)
            :publish (config/load "config/publish.edn")))))

(defn check-api-element
  "checks an `:api` element for unresolved namespaces and entries

   (check-api-element {:lookup {}}
                      {:type :api :namespace \"std.lib.missing\"})
   => [{:type :missing-namespace :namespace 'std.lib.missing}]"
  {:added "4.1"}
  [project {:keys [namespace table only] :as elem}]
  (let [ns-sym (symbol namespace)]
    (cond-> []
      (not ((:lookup project) ns-sym))
      (conj {:type :missing-namespace :namespace ns-sym})

      :always
      (into (mapcat (fn [[var entry]]
                      (cond-> []
                        (and (nil? (get-in entry [:source :code]))
                             (not (get-in entry [:source :generated])))
                        (conj {:type :missing-source :namespace ns-sym :var var})

                        (nil? (get-in entry [:test :code]))
                        (conj {:type :missing-example :namespace ns-sym :var var})))
                    table))

      (seq only)
      (into (keep (fn [v]
                    (when-not (contains? table (symbol v))
                      {:type :missing-only-var :namespace ns-sym :var (symbol v)}))
                  only)))))

(defn check-reference-element
  "checks a `:reference` element for the missing reference placeholder"
  {:added "4.1"}
  [{:keys [code refer] :as elem}]
  (when (and (string? code)
             (string/starts-with? code "MISSING REFERENCE"))
    [{:type :missing-reference :refer refer}]))

(defn check-element
  "checks a single prepared element, returning a vector of issues"
  {:added "4.1"}
  [project {:keys [type] :as elem}]
  (case type
    :api       (check-api-element project elem)
    :reference (check-reference-element elem)
    :related   (when (:error elem)
                 [{:type :unknown-data-group :error (:error elem)}])
    :links     (when (:error elem)
                 [{:type :unknown-data-group :error (:error elem)}])
    (when (:failed elem)
      [{:type :failed-test
        :line (get-in elem [:line :row])
        :failures (count (get-in elem [:failed :output]))}])))

(defn check-page
  "checks a single documentation page for unresolved and failing elements

   issues are returned as a vector of maps with :page, :type and details.
   when `:eval` is set in params, page facts are executed and failures
   are reported as `:failed-test` issues"
  {:added "4.1"}
  ([key params lookup project]
   (try
     (let [interim  (if (:eval params)
                      (binding [link.test/*run-tests* true]
                        (prepare/prepare key params lookup project))
                      (prepare/prepare key params lookup project))
           name     (:name (lookup key))
           elements (get-in interim [:articles name :elements])]
       (mapv #(assoc % :page key)
             (mapcat #(check-element project %) elements)))
     (catch Throwable e
       [{:page key :type :prepare-error :error (.getMessage e)}]))))

(defn select-pages
  "selects page keys from the lookup given task input"
  {:added "4.1"}
  [lookup input]
  (let [ks (sort (keys lookup))]
    (cond (= :all input)
          ks

          (sequential? input)
          (let [prefixes (map str input)]
            (filter (fn [k]
                      (some #(string/starts-with? (str k) %) prefixes))
                    ks))

          :else [input])))

(defn check-failures
  "checks pages and returns the total number of issues found

   (check-failures :all)
   (check-failures '[core xt])"
  {:added "4.1"}
  ([input] (check-failures input {}))
  ([input params]
   (let [project (make-check-project)
         lookup  (executive/all-pages project)]
     (->> (select-pages lookup input)
          (mapcat #(check-page % params lookup project))
          count))))
