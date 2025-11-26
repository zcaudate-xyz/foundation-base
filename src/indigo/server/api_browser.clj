(ns indigo.server.api-browser
  (:require [std.lang :as l]
            [std.lang.base.book :as book]
            [std.lib :as h]
            [std.string :as str]))

;; Existing endpoints -------------------------------------------------------

(defn list-namespaces
  "lists all namespaces for a given language"
  {:added "4.0"}
  [lang]
  (let [book (l/get-book (l/default-library) (keyword lang))
        modules (book/list-entries book :module)]
    (->> modules
         (map str)
         (sort))))

(defn list-components
  "lists all components for a given namespace and language"
  {:added "4.0"}
  [lang ns]
  (let [book (l/get-book (l/default-library) (keyword lang))
        entry (book/get-module book (symbol ns))]
    (if entry
      (->> (:code entry)
           (keys)
           (map str)
           (sort))
      [])))

(defn get-component
  "gets the component source code"
  {:added "4.0"}
  [lang ns component]
  (let [book (l/get-book (l/default-library) (keyword lang))
        entry (book/get-code-entry book (symbol (str ns "/" component)))]
    (if entry
      (str (:form entry))
      "")))

;; New endpoints -----------------------------------------------------------

(defn list-libraries
  "returns the set of languages for which a std.lang library is available"
  {:added "4.0"}
  []
  (let [libs (l/default-library)]
    (->> libs
         (keys)
         (map name)
         (sort))))

(defn component-metadata
  "returns metadata for a specific component (if any). Returns a map with keys such as :doc, :meta, :type etc."
  {:added "4.0"}
  [lang ns component]
  (let [book (l/get-book (l/default-library) (keyword lang))
        entry (book/get-code-entry book (symbol (str ns "/" component)))]
    (if entry
      (select-keys entry [:doc :meta :type :form])
      {})))

(defn component-preview
  "returns the compiled JavaScript source for a component, suitable for live rendering.
   For now it simply returns the DSL source (same as get-component) – the client can evaluate it.
   In the future this could invoke the std.lang compiler.
  "
  {:added "4.0"}
  [lang ns component]
  (get-component lang ns component))

(defn search-components
  "searches component names across all namespaces for a given language.
   Returns a vector of maps {:ns <namespace> :component <name>} matching the query (case‑insensitive substring).
  "
  {:added "4.0"}
  [lang query]
  (let [book (l/get-book (l/default-library) (keyword lang))
        modules (book/list-entries book :module)
        q (str/lower-case query)
        matches (for [mod modules
                      :let [ns (name mod)
                            entry (book/get-module book (symbol ns))
                            comps (keys (:code entry))]
                      comp comps
                      :let [cname (name comp)]
                      :when (str/includes? (str/lower-case cname) q)]
                  {:ns ns :component cname})]
    (vec matches)))
