(ns std.lang.base.book-meta
  (:require [std.lib.collection :as collection]
            [std.lib.impl :as impl]))

(defn- book-meta-string
  ([{:keys [lang] :as m}]
   (str "#book.meta "
        " "
        (vec (sort (keys (collection/filter-vals boolean m)))))))

(impl/defimpl BookMeta [bootstrap 
		   
                   module-import
                   module-export
                   module-link
		   
                   has-module
		   setup-module
		   teardows-module
		   
                   has-ptr
		   setup-ptr
		   teardown-ptr]
  :string book-meta-string)

(defn book-meta?
  "checks if object is a book meta"
  {:added "4.0"}
  ([x]
   (instance? BookMeta x)))

(defn book-meta
  "creates a book meta
 
   (book-meta {:module-export  (fn [{:keys [as]} opts]
                                 (h/$ (return ~as)))
               :module-import  (fn [name {:keys [as]} opts]  
                                 (h/$ (var ~as := (require ~(str name)))))
               :has-ptr        (fn [ptr]
                                 (list 'not= (ut/sym-full ptr) nil))
               :teardown-ptr   (fn [ptr]
                                 (list := (ut/sym-full ptr) nil))})
  => book-meta?"
  {:added "4.0"}
  ([{:keys [bootstrap
            module-import
            module-export
            module-link
	    
            has-module
	    setup-module
	    teardows-module
	    
            has-ptr
	    setup-ptr
	    teardown-ptr] :as m}]
   (map->BookMeta m)))
