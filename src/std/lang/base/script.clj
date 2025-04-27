(ns std.lang.base.script
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.book :as book]
            [std.lang.base.impl :as impl]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.runtime :as rt]
            [std.lang.base.book-entry :as e]
            [std.lang.base.util :as ut]
            [std.string :as str]
            [std.json :as json]
            [std.lib :as h]
            [std.lang.base.registry :as reg]
            [std.lang.base.script-annex :as annex]
            [std.lang.base.script-control :as control]
            [std.lang.base.script-macro :as macro]))

(defn install
  "installs a language"
  {:added "4.0"}
  [{:keys [lang grammar] :as book}]
  (let [lib (impl/default-library)]
    [(lib/install-book! lib book)
     (rt/install-lang! lang)
     (macro/intern-grammar lang
                           grammar)]))

(def +module-keys+
  [;; builtin
   :lang
   :id

   ;; top-level
   :require
   :import
   :macro-only
   :bundle
   :file
   :export
   :static])

(def +runtime-keys+
  [:runtime
   :config
   :layout
   :emit
   
   ;; auto
   :lang
   :context
   :module
   :namespace

   ;; unique
   :id])

(defn script-ns-import
  "imports the namespace and sets a primary flag"
  {:added "4.0"}
  ([{:keys [require require-impl] :as config}]
   (let [current (h/ns-sym)]
     (alias '- current)
     (->> require-impl
          (mapv (fn [ns] (clojure.core/require ns :reload))))
     (->> require
          (keep (fn [[ns & {:keys [as with primary]}]]
                  (clojure.core/require
                   (cond-> [ns]
                     as    (conj :as as)
                     with  (conj :refer with)))
                  (if primary ns)))
          set))))

(defn script-macro-import
  "import macros into the namespace"
  {:added "4.0"}
  ([book]
   (script-macro-import book identity))
  ([book restrict]
   (let [macros (->> (flatten (:macros book))
                     (filter restrict)
                     (concat (:highlights book)))
         syms   (map h/var-sym macros)
         mns    (ut/sym-module (first syms))
         ids    (set (map ut/sym-id syms))
         curr   (h/ns-sym)
         ignore (h/intersection ids
                                (set (concat (keys (ns-refers curr))
                                             (keys (ns-interns curr)))))
         refers (h/difference ids ignore)]
     (refer mns :only (vec refers))
     [refers ids])))


;; script-base
;; - installs a module to the library (book should be installed)
;;   - most of the work done in lib/install-module
;;
;; - initialises the embedded runtime. (checks config and restarts if necessary)
;;   - most of the work done in script/runtime
;;
;;
;;
;; script-workflow
;; - setup clojure namespaces (require and alias)
;; - aliases current namespace as `-`
;;
;; - import language specific macros into current namespace
;;

(defn script-fn-base
  "setup for the runtime"
  {:added "4.0"}
  ([lang module-id config lib]
   (let [primary    (script-ns-import config)
         [snapshot] (lib/install-module! lib lang module-id (dissoc config
                                                                    :runtime
                                                                    :config
                                                                    :layout
                                                                    :emit))
         book    (snap/get-book-raw snapshot lang)
         module  (get-in book [:modules module-id])
         macros  (script-macro-import book)]
     (merge (:config config)
            {:module module-id
             :module/internal (get module :internal)
             :module/primary primary}
            (select-keys config [:layout
                                 :emit])))))

(defn script-fn
  "calls the regular setup script for the namespace"
  {:added "4.0"}
  ([lang]
   (script-fn lang (h/ns-sym) {}))
  ([lang module]
   (if (map? module)
     (script-fn lang (h/ns-sym) module)
     (script-fn lang  module {})))
  ([lang module config]
   (let [;; loading runtime
         _ (if-let [ns (get @reg/+registry+ [lang :default])] (require ns))
         ;; _ (h/prn lang module config)
         rt-config (script-fn-base lang module config (impl/default-library))]
     (control/script-rt-get lang
                            (:runtime config)
                            rt-config))))

(defmacro ^{:style/indent 1}
  script
  "script macro"
  {:added "4.0"}
  ([lang]
   `(script-fn ~lang))
  ([lang module]
   `(script-fn ~lang (quote ~module)))
  ([lang module config]
   `(script-fn ~lang
               (quote ~module)
               (quote ~config))))

(defn script-test-prep
  "preps the current namespace"
  {:added "4.0"}
  ([lang config]
   (let [module-id (h/ns-sym)
         book      (annex/get-annex-book module-id lang)
         lib       (annex/get-annex-library module-id)]
     (binding [book/*skip-check* true]
       (script-fn-base lang module-id config lib)))))

(defn script-test
  "the `script-` function call"
  {:added "4.0"}
  ([lang config]
   (let [rt-config (script-test-prep lang config)]
     (control/script-rt-get lang
                            (:runtime config)
                            rt-config))))

(defmacro ^{:style/indent 1}
  script-
  "macro for test setup"
  {:added "4.0"}
  ([lang]
   `(script-test ~lang {}))
  ([lang config]
   `(script-test ~lang (quote ~config))))


;;
;; script extend
;;

(defn script-ext
  "the `script+` function call"
  {:added "4.0"}
  ([[tag lang] config]
   (let [{:keys [runtime]} config
         rt-config (script-test-prep lang config)
         ns (:module rt-config)
         _  (control/script-rt-get lang
                                   (:runtime config)
                                   rt-config)
         _  (annex/register-annex-tag ns tag lang runtime config)
         rt (annex/get-annex-runtime ns tag)]
     (if (or (not rt)
             (not (annex/same-runtime? rt lang (or runtime :default) rt-config)))
       (annex/add-annex-runtime ns tag
                                (annex/start-runtime lang
                                                     (or runtime :default)
                                                     rt-config))
       [rt]))))

(defmacro ^{:style/indent 1}
  script+
  "macro for test extension setup"
  {:added "4.0"}
  ([[tag lang]]
   `(script-ext ~[tag lang] {}))
  ([[tag lang] config]
   `(script-ext ~[tag lang] (quote ~config))))

;;
;;
;;

(defn script-ext-run
  "function to call with the `!` macro"
  {:added "4.0"}
  ([ns tag body meta]
   (let [{:keys [lang module]
          :as rt}    (or (annex/get-annex-runtime ns tag)
                         (h/error "Annex Not found"
                                  {:available [(keys @(:runtimes (annex/get-annex)))]}))]
     (macro/call-thunk
      meta
      (fn []
        (h/p:rt-invoke-ptr
         rt
         (ut/lang-pointer lang {:module module})
         [body]))))))

(defmacro ^{:style/indent 1}
  !
  "switch between defined annex envs"
  {:added "4.0"}
  ([tag body]
   (if (vector? tag)
     `(script-ext-run (quote ~(h/ns-sym))
                      ~(first tag)
                      (quote ~body)
                      ~(meta &form)))))


(comment

  (defmacro
    !.async
    "switch between defined annex envs"
    {:added "4.0"}
    ([tag body]
     `(h/future:run
       (bound-fn
         []
         (script-ext-run (quote ~(h/ns-sym))
                         ~tag
                         (quote ~body)
                         ~(meta &form))))))

  (defmacro 
    !.run
    "switch between defined annex envs"
    {:added "4.0"}
    ([lang body]
     `(control/script-rt-oneshot-eval
       :oneshot
       ~lang
       [(quote ~body)]))))

(defn annex:start
  "starts an annex tag"
  {:added "4.0"}
  ([tag]
   (annex:start tag (h/ns-sym)))
  ([tag ns]
   (let [{:keys [registry
                 runtimes]} (annex/get-annex ns)]
     (if-let [{:keys [lang
                      runtime
                      config]} (get @registry tag)]
       (script-ext [tag lang] config)))))

(defn annex:get
  "gets the runtime associated with an annex"
  {:added "4.0"}
  ([tag]
   (annex:get tag (h/ns-sym)))
  ([tag ns]
   (get @(:runtimes (annex/get-annex ns))
        tag)))

(defn annex:stop
  "stops an annex tag"
  {:added "4.0"}
  ([tag]
   (annex:stop tag (h/ns-sym)))
  ([tag ns]
   (let [{:keys [runtimes]} (annex/get-annex ns)]
     (h/swap-return! runtimes
       (fn [m]
         (if-let [rt (get m tag)]
           [(h/stop rt) (dissoc m tag)]))))))

(defn annex:start-all
  "starts all the annex tags"
  {:added "4.0"}
  ([]
   (annex:start-all (h/ns-sym)))
  ([ns]
   (let [{:keys [registry]} (annex/get-annex ns)]
     (h/map-entries (fn [[tag {:keys [lang
                                      runtime
                                      config]}]]
                      [tag (script-ext [tag lang] config)])
                    @registry))))

(defn annex:stop-all
  "stops all annexs"
  {:added "4.0"}
  ([]
   (annex:stop-all (h/ns-sym)))
  ([ns]
   (annex/clear-annex ns)))

(defn annex:restart-all
  "stops and starts all annex runtimes"
  {:added "4.0"}
  ([]
   (annex:restart-all (h/ns-sym)))
  ([ns]
   (annex:stop-all ns)
   (annex:start-all ns)))

(defn annex:list
  "lists all annexs"
  {:added "4.0"}
  ([]
   (annex:list (h/ns-sym)))
  ([ns]
   (let [{:keys [runtimes
                 registry]} (annex/get-annex ns)]
     {:registered (set (keys @registry))
      :active (set (keys @runtimes))})))
