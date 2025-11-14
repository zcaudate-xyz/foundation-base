(ns std.lang.base.book-module
  (:require [std.lib :as h :refer [defimpl]]))

(defn- book-module-string
  ([{:keys [lang id display] :as module}]
   (let [meta (->> (dissoc module :id :lang :code :fragment :display)
                   (h/filter-vals (fn [e]
                                    (if (coll? e)
                                      (not-empty e)
                                      e))))
         entries (->> (select-keys module [:code :fragment])
                      (h/map-vals (comp sort keys)))]
     (str "#book.module " [lang id] 
          (if (= :brief display)
            ""
            (str " "
                 {:meta meta
                  :entries entries}))))))
  
;;
;; 
;;

(defimpl BookModule [;; required
                     lang
                     id
                     
                     ;; dependency management
                     alias         ;; map of shortcuts to packages (both external and internal)
                     link          ;; map of internal dependencies
                     macro-only    ;; do not link unless done explicity by `:fn` keyword in `:bundle`
                     bundle        ;; bundles native libraries with import (controlled by `:with`)
                     native        ;; packages that are native to the workspace runtime 
                     internal      ;; set of required packages
                     
                     ;; project and packaging 
                     file
                     export
                     
                     ;; actual code
                     
                     fragment   ;; macros
                     code       ;; main code

                     ;; misc (for adding additional data not related to runtime)
                     static]
  :final true
  :string book-module-string)

(defn book-module?
  "checks of object is a book module"
  {:added "4.0"}
  ([x]
   (instance? BookModule x)))

(defn book-module
  "creates a book module"
  {:added "4.0"}
  ([{:keys [lang
            id

            alias
            link
            internal
            native
            native-lu
            
            require-impl
            
            file
            export

            fragment
            code

            static
            display] :as m}]
   (assert (not= nil lang) "Module :lang required")
   (assert (not= nil id)   "Module :id Required")
   (map->BookModule (merge { ;; Link
                            :alias {}
                            :link {}
                            :macro-only false
                            :bundle {}
                            :native {}
                            :suppress {}
                            :require-impl []

                            ;; Code
                            :fragment {}
                            :code {}

                            ;; Misc
                            :static {}
                            :display :default}
                           m))))

(defn module-deps
  "gets dependencies for a given module"
  {:added "4.0"}
  ([module id]
   (h/difference (set (vals (:link module)))
                 (conj (set (keys (:suppress module)))
                       id))))


(comment
  h/atom:get
  #_
  (defn library-install-module
    "installs a module into the library"
    {:added "3.0"}
    ([{:keys [lang store] :as lib} module-id conf]
     (let [{:keys [link native alias code fragment]} conf
           missing (h/difference (set (vals link))
                                 (set (conj (keys @store)
                                            module-id)))
           _  (if (not-empty missing)
                (h/error "Missing namespaces" {:ns missing}))
           module     (library-module (assoc conf
                                             :id   module-id
                                             :lang lang
                                             :alias     (or alias {})
                                             :link      (or link {})
                                             :native    (or native {})
                                             :code      (or code {})
                                             :fragment  (or fragment {})))]))))
