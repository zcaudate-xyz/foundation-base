(ns postgres.typed.export.portable-edn-test
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [clojure.string :as string]
            [postgres.typed :as typed]
            [postgres.typed.export.portable-edn :as portable-edn]
            [postgres.typed.typed-common :as types])
  (:use code.test))

^{:refer postgres.typed.export.portable-edn/export-edn :added "4.1"}
(fact "round-trips a registry context and exposes the same API through postgres.typed"
  (let [table (types/make-table-def "demo" "Entry" [] :id)
        ctx (typed/load-registry {'demo/Entry table})
        snapshot (portable-edn/export-edn ctx)]
    [(map? snapshot)
     (= snapshot (edn/read-string (pr-str snapshot)))
     (= ctx (portable-edn/import-edn snapshot))
     (= ctx (portable-edn/import-edn (edn/read-string (pr-str snapshot))))
     (= snapshot (typed/export-edn ctx))
     (= ctx (typed/import-edn snapshot))])
  => [true true true true true true])

^{:refer postgres.typed.export.portable-edn/export-edn-string :added "4.1"}
(fact "writes metadata followed by one stable form for each typed entry"
  (let [table (types/make-table-def "demo" "Entry" [] :id)
        ctx {:app-name "demo"
             :registry (with-meta {'demo/Entry table} {:source :fixture})
             :typed {:enums {'demo/Role #{:admin :member}}
                     :functions {'demo/run {:inputs [{:name :id :type :uuid}]
                                            :body-meta {:raw-body
                                                        [(list 'quote '(sample value))]}}}
                     :tables {'demo/Entry table}}
             :namespaces '[demo.schema]
             :registration-order {:functions ["demo/run"]}}
        text (typed/export-edn-string ctx)
        forms (with-open [reader (java.io.PushbackReader.
                                  (java.io.StringReader. text))]
                (loop [out []]
                  (let [form (edn/read {:eof ::eof} reader)]
                    (if (= ::eof form)
                      out
                      (recur (conj out form))))))
        metadata (:postgres.typed/metadata (first forms))]
    [(count forms)
     (not (string/includes? text "#:postgres.typed{"))
     (string/includes? text ":postgres.typed/format")
     (mapv (fn [[_ entry-header _]]
             [(:path entry-header) (:key entry-header)])
           (rest forms))
     (:postgres.typed/entry-counts (first forms))
     (mapv #(get-in metadata (into [:postgres.typed/context] %))
           [[:registry] [:typed :enums] [:typed :functions] [:typed :tables]])
     (= text (typed/export-edn-string ctx))])
  => [5
      true
      true
      [[[:registry] 'demo/Entry]
       [[:typed :enums] 'demo/Role]
       [[:typed :functions] 'demo/run]
       [[:typed :tables] 'demo/Entry]]
      {[:registry] 1
       [:typed :enums] 1
       [:typed :functions] 1
       [:typed :tables] 1}
      [{} {} {} {}]
      true])

^{:refer postgres.typed.export.portable-edn/import-edn-string :added "4.1"}
(fact "reconstructs contexts, preserves metadata, and reads legacy snapshots"
  (let [table (types/make-table-def "demo" "Entry" [] :id)
        ctx {:app-name "demo"
             :registry (with-meta {'demo/Entry table} {:source :fixture})
             :typed {:enums {'demo/Role #{:admin :member}}
                     :functions {'demo/run {:inputs [{:name :id :type :uuid}]
                                            :body-meta {:raw-body
                                                        [(list 'quote '(sample value))]}}}
                     :tables {'demo/Entry table}}
             :namespaces '[demo.schema]
             :registration-order {:functions ["demo/run"]}}
        text (typed/export-edn-string ctx)
        snapshot (portable-edn/export-edn ctx)
        legacy-pprint (with-out-str (pprint/pprint snapshot))
        forms (with-open [reader (java.io.PushbackReader.
                                  (java.io.StringReader. text))]
                (loop [out []]
                  (let [form (edn/read {:eof ::eof} reader)]
                    (if (= ::eof form)
                      out
                      (recur (conj out form))))))
        header (first forms)
        entry (second forms)
        render (fn [items]
                 (string/join "\n" (map pr-str items)))
        bad-version (render (cons (assoc header :postgres.typed/version 2)
                                  (rest forms)))
        duplicate (render (concat [header entry entry] (drop 2 forms)))
        bad-path (render [header
                          [:postgres.typed/entry {:path [:missing] :key :x} 1]])
        truncated (render [header])
        imported (typed/import-edn-string text)
        error-type (fn [source]
                     (:type (try
                              (typed/import-edn-string source)
                              (catch clojure.lang.ExceptionInfo error
                                (ex-data error))))) ]
    [(= ctx imported)
     (= {:source :fixture} (meta (:registry imported)))
     (= text (typed/export-edn-string imported))
     (= ctx (typed/import-edn-string (pr-str snapshot)))
     (and (string/includes? legacy-pprint "'(")
          (= ctx (typed/import-edn-string legacy-pprint)))
     (mapv error-type [bad-version duplicate bad-path truncated])])
  => [true true true true true
      [:postgres.typed/unsupported-edn-stream-version
       :postgres.typed/duplicate-edn-stream-entry
       :postgres.typed/invalid-edn-stream-entry
       :postgres.typed/incomplete-edn-stream]])
