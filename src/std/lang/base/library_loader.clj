(ns std.lang.base.library-loader
  (:require [std.lang.base.impl :as impl]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.registry :as reg]
            [std.lib.foundation :as f]))

(defn local-book-raw
  "returns the locally installed raw book for a library, excluding inherited parent books"
  {:added "4.1"}
  [library lang]
  (snap/get-book-raw @(:instance library) lang))

(defn ensure-book!
  "ensures a book is installed in the target library"
  {:added "4.1"}
  ([library lang]
   (ensure-book! library lang :default))
  ([library lang key]
   (or (local-book-raw library lang)
       (let [{:keys [parent] :as info} (or (reg/registry-book-info lang key)
                                           (f/error "Book not found in registry"
                                                    {:lang lang
                                                     :key key
                                                     :available (reg/registry-book-list)}))
             default-lib (impl/default-library)
             _ (when parent
                 (ensure-book! default-lib parent :default)
                 (when-not (identical? library default-lib)
                   (ensure-book! library parent :default)))
             _ (or (reg/registry-book lang key)
                   (f/error "Book namespace failed to load"
                            {:lang lang
                             :key key
                             :info info}))
             book (or (local-book-raw library lang)
                      (local-book-raw default-lib lang)
                      (reg/registry-book lang key))]
         (lib/install-book! library book)
         (or (local-book-raw library lang)
             (lib/get-book library lang))))))
