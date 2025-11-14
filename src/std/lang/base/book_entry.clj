(ns std.lang.base.book-entry
  (:require [std.lib :as h :refer [defimpl]]
            [std.lang.base.util :as ut]))

(defn- book-entry-string
  ([{:keys [id lang module section display] :as entry}]
   (if (= display :symbol)
     (str "E:"(ut/sym-full module id))
     (str "#book.entry " [lang section (ut/sym-full module id)]
          (if (= display :brief)
            ""
            (str " "
                 (h/filter-vals identity
                                (dissoc entry
                                        :section
                                        :id
                                        :module
                                        :declared
                                        :form-input
                                        :template
                                        :standalone

                                        :line
                                        :time
                                        :priority
                                        :display))))))))

(defimpl BookEntry [;; required
                    lang
                    id
                    module
                    section
                    
                    ;; stores if code has been declared (line number)
                    declared
                    
                    ;; code
                    form
                    form-input
                    
                    ;; linking
                    deps
                    deps-fragment
                    deps-native
                    
                    ;; required at runtime for dynamic eval to work
                    namespace

                    ;; supporting macros
                    template
                    standalone

                    ;; ordering
                    line
                    time
                    priority

                    ;; 
                    display]
  :final true
  :string book-entry-string)

(defn book-entry?
  "checks if object is a book entry"
  {:added "4.0"}
  ([x]
   (instance? BookEntry x)))

(defn book-entry
  "creates a book entry"
  {:added "4.0"}
  ([{:keys [lang
            id
            module

            section

            form
            form-input
            deps
            deps-native
            
            namespace
            
            template
            standalone
            
            declared] :as m}]
   (map->BookEntry (merge {:display :default}
                          m))))

