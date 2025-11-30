(ns verify-add-book-modules
  (:require [std.lang.base.library-snapshot :as snap]
            [std.lang.base.book :as book]
            [std.lang.base.book-module :as module]
            [std.lib :as h]))

(defn run []
  (let [;; 1. Create a snapshot with parent 'xtalk' already installed
        xtalk-book (book/book {:lang :xtalk
                               :meta {:dummy 1}
                               :grammar {:dummy 1}})
        s0 (-> (snap/snapshot {})
               (snap/add-book xtalk-book))

        ;; 2. Create a book with one module
        b1 (book/book {:lang :lua
                       :parent :xtalk
                       :meta {:v 1}
                       :grammar {:v 1}
                       :modules {'L.mod1 {:id 'L.mod1 :lang :lua}}})

        ;; 3. Add book to snapshot
        s1 (snap/add-book s0 b1)

        ;; Verify module exists
        _ (println "After first add-book:")
        _ (println "Modules:" (keys (:modules (snap/get-book-raw s1 :lua))))

        ;; 4. Create "same" book (or updated book) but WITHOUT the module explicitly in input
        ;;    (simulating re-adding a book definition that might not include dynamic modules)
        b2 (book/book {:lang :lua
                       :parent :xtalk
                       :meta {:v 2} ;; changed meta to trigger update
                       :grammar {:v 1}})

        ;; 5. Update book in snapshot
        ;;    This calls install-book -> install-book-update
        [s2 status] (snap/install-book s1 b2)]

    (println "\nAfter install-book (update):")
    (println "Status:" status)
    (println "Modules:" (keys (:modules (snap/get-book-raw s2 :lua))))

    (if (get-in (snap/get-book-raw s2 :lua) [:modules 'L.mod1])
      (println "PASS: Module L.mod1 persisted.")
      (println "FAIL: Module L.mod1 was LOST."))))

(run)
