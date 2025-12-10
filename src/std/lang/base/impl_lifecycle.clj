(ns std.lang.base.impl-lifecycle
  (:require [std.lang.base.util :as ut]
            [std.lang.base.emit :as emit]
            [std.lang.base.book :as book]
            [std.lang.base.book-module :as module]
            [std.lang.base.impl :as impl]
            [std.lang.base.impl-deps :as deps]
            [std.lang.base.impl-entry :as entry]
            [std.string :as str]
            [std.fs :as fs]
            [std.lib :as h]
            [std.lang.base.compile-links :as links]))

(defn emit-module-prep
  "prepares the module for emit"
  {:added "4.0"}
  [module-id
   {:keys [library
           lang
           emit] :as meta}]
  (let [_ (assert lang "Lang required.")
        [stage grammar book namespace mopts] (impl/emit-options (merge {:layout :module}
                                                                       meta))
        module (get-in book [:modules module-id])]
    [[stage grammar book namespace (assoc mopts :module module)]
     (deps/collect-module book module)]))

;;
;; SETUP
;; 

(defn emit-module-setup-concat
  "joins setup raw into individual blocks"
  {:added "4.0"}
  [{:keys [setup-body
           native-arr
           link-arr
           header-arr
           code-arr
           export-body]
    :as raw}]
  (->> (concat [setup-body]
               native-arr
               link-arr
               header-arr
               code-arr
               [export-body])
       (filter not-empty)))

(defn emit-module-setup-join
  "joins setup raw into the setup code"
  {:added "4.0"}
  [{:keys [setup-body
           native-arr
           link-arr
           header-arr
           code-arr
           export-body]
    :as raw}]
  (->> [setup-body
        (str/join "\n\n" native-arr)
        (str/join "\n\n" link-arr)
        (str/join "\n\n" header-arr)
        (str/join "\n\n" code-arr)
        export-body]
       (filter not-empty)
       (str/join "\n\n")))

(defn emit-module-setup-native-arr
  "creates the setup code for native imports"
  {:added "4.0"}
  [{:keys [library
           lang
           emit] :as meta}
   prep]
  (let [[[_ grammar book namespace mopts]
         {:keys [native]}] prep
        native-opts  (update mopts
                             :emit
                             merge (:native emit) {:import :native})
        native (if (-> emit :native :suppress)
                 []
                 native)
        native-bundled  (apply merge (map :bundle (vals native)))]

    (keep (fn [[name module]]
            (if-let [form (deps/module-import-form book name module native-opts)]
              (impl/emit-direct grammar
                                form
                                namespace
                                native-opts)))
          (merge native native-bundled))))

(defn emit-module-setup-link-import
  [type curr-ns link-ns links module
   {:keys [root-prefix
           path-separator]
    :or {path-separator "/"}
    :as link-opts}
   root-ns]
  (let [link-as (get (:internal module) link-ns)]
    (case type
      :graph
      (let [entry (get links link-ns)
            {:keys [rel label suffix]} (if (and (:rel entry) (:label entry))
                                         entry
                                         (links/link-attributes root-ns link-ns link-opts))
            curr (get links curr-ns)
            curr (if (and (:rel curr) (:label curr))
                   curr
                   (links/link-attributes root-ns curr-ns link-opts))]
        {:ns (str (fs/relativize (:rel curr)
                                 rel)
                  path-separator
                  label)
         :suffix suffix
         :as link-as})

      (let [entry (get links link-ns)
            {:keys [rel label suffix]} (if (and (:rel entry) (:label entry))
                                         entry
                                         (links/link-attributes root-ns link-ns link-opts))
            root-prefix (if (map? root-prefix)
                          (or (links/get-link-lookup link-ns (dissoc root-prefix :default))
                              (:default root-prefix))
                          root-prefix)]
        {:ns (->> [root-prefix rel label]
                  (filter (fn [s] (and s (not= s ""))))
                  (str/join path-separator))
         :suffix suffix
         :as link-as}))))

(defn emit-module-setup-link-arr
  "creates the setup code for internal links"
  {:added "4.0"}
  [{:keys [library
           lang
           emit] :as meta}
   prep]
  (let [[[_ grammar book namespace mopts]
         {:keys [direct]}]  prep
        {:keys [links type root-ns]} (:compile emit)
        {:keys [module]} mopts]
    (keep (fn [ns]
            (let [import  (emit-module-setup-link-import
                           type
                           (:id module)
                           ns
                           links
                           module
                           (:link (:code emit))
                           root-ns)
                  form (deps/module-import-form book
                                                (:ns import)
                                                import
                                                (assoc-in mopts [:emit :import] :link))]
              (if form
                (impl/emit-direct grammar
                                  form
                                  namespace
                                  mopts))))
          direct)))

(defn emit-module-setup-export-body
  "creates the setup code for internal links"
  {:added "4.0"}
  [{:keys [library
           lang
           emit] :as meta}
   prep]
  (let [[[_ grammar book namespace mopts]]  prep
        {:keys [module]} mopts]
    (if (and (= :module (:layout mopts))
             (not (-> emit :export :suppress))
             (not (false? (-> module :static :export))))
      (let [form (deps/module-export-form book module mopts)]
        (if form
          (impl/emit-direct grammar
                            form
                            namespace
                            mopts))))))

(defn emit-module-setup-raw
  "creates module setup map of array strings"
  {:added "4.0"}
  [module-id
   {:keys [library
           lang
           emit] :as meta}]
  (let [prep   (emit-module-prep module-id meta)
        [[stage grammar book namespace mopts]
         {:keys [setup
                 native
                 link
                 header
                 code
                 export]}] prep
        setup-body   (if (and (not (-> emit :setup :suppress))
                              setup)
                       (impl/emit-direct grammar
                                         setup
                                         namespace
                                         (update mopts :emit merge (:setup emit))))

        native-arr   (emit-module-setup-native-arr meta prep)
        link-arr     (emit-module-setup-link-arr meta prep)
        header-arr   (if (not (-> emit :header :suppress))
                       (let [header-opts  (update mopts :emit merge (:header emit))]
                         (keep (fn [entry]
                                 (binding [*ns* (:namespace entry)]
                                   (entry/emit-entry grammar entry header-opts)))
                               header)))
        code-arr     (if (not (-> emit :code :suppress))
                       (let [code-opts    (update mopts :emit merge (:code emit))]
                         (keep (fn [entry]
                                 (binding [*ns* (:namespace entry)]
                                   (entry/emit-entry grammar entry code-opts)))
                               code)))
        export-body  (emit-module-setup-export-body meta prep)]
    {:setup-body setup-body
     :native-arr native-arr
     :link-arr   link-arr
     :header-arr header-arr
     :code-arr code-arr
     :export-body export-body}))

(defn emit-module-setup
  "emits the entire module as string"
  {:added "4.0"}
  [module-id
   {:keys [library
           lang
           emit] :as meta}]
  (let [raw (emit-module-setup-raw module-id meta)]
    (emit-module-setup-join raw)))

;;
;; TEARDOWN
;; 

(defn emit-module-teardown-concat
  "joins teardown raw into individual blocks
 
   (-> (emit-module-teardown-raw 'xt.lang.base-lib
                                 {:lang :lua})
       (emit-module-teardown-concat))
   => coll?"
  {:added "4.0"}
  [{:keys [teardown-body
           code-arr
           native-arr]
    :as raw}]
  (->> (concat code-arr
               native-arr
               [teardown-body])
       (filter not-empty)))

(defn emit-module-teardown-join
  "joins teardown raw into code
 
   (-> (emit-module-teardown-raw 'xt.lang.base-lib
                                 {:lang :lua})
       (emit-module-teardown-join))
   => string?"
  {:added "4.0"}
  [{:keys [teardown-body
           code-arr
           native-arr]
    :as raw}]
  (->> [(str/join "\n\n" code-arr)
        (str/join "\n\n" native-arr)
        teardown-body]
       (filter not-empty)
       (str/join "\n\n")))

(defn emit-module-teardown-raw
  "creates module teardown map of array strings"
  {:added "4.0"}
  [module-id
   {:keys [library
           lang
           emit] :as meta}]
  (let [[[stage grammar book namespace mopts]
         {:keys [teardown
                 native
                 code]}] (emit-module-prep module-id meta)
        code-arr       (if (not (-> emit :code :suppress))
                         (let [code-opts    (assoc mopts :emit (:code emit))]
                           (keep (fn [entry]
                                   (if-let [form (deps/teardown-ptr-form book entry)]
                                     (impl/emit-direct grammar
                                                       form
                                                       namespace
                                                       code-opts)))
                                 (reverse code))))
        native-arr     (if (not (-> emit :native :suppress))
                         (let [native-opts  (assoc mopts :emit (:native emit))]
                           (keep (fn [[name module]]
                                   (if-let [form (deps/teardown-module-form book module)]
                                     (impl/emit-direct grammar
                                                       form
                                                       namespace
                                                       native-opts)))
                                 native)))
        teardown-body  (if (and (not (-> emit :teardown :suppress))
                                teardown)
                         (impl/emit-direct grammar
                                           teardown
                                           namespace
                                           (assoc mopts :emit (:teardown emit))))]
    {:code-arr code-arr
     :native-arr native-arr
     :teardown-body teardown-body}))

(defn emit-module-teardown
  "creates the teardown script"
  {:added "4.0"}
  [module-id
   {:keys [library
           lang
           emit] :as meta}]
  (let [raw (emit-module-teardown-raw module-id meta)]
    (emit-module-teardown-join raw)))


(comment

  #_(when (and (= :module (:layout mopts))
               (-> mopts :module :export :as)
               (not (-> emit :export :suppress)))
      (when-not (:as export)
        (h/prn   "Missing export `:as` field" {:input export
                                               :module module-id})
        (h/error "Missing export `:as` field" {:input export
                                               :module module-id}))
      (when-not (:entry export)
        (h/prn   "Missing export `:entry` field" {:input export
                                                  :module module-id})
        (h/error "Missing export `:entry` field" {:input export
                                                  :module module-id}))
      (str (entry/emit-entry grammar (:entry export)
                             (update mopts :emit merge (:export emit)))
           "\n\n"
           (impl/emit-direct
            grammar
            (deps/module-export-form book
                                     (-> mopts :module :export)
                                     mopts)
            namespace
            mopts))))
