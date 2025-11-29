(ns code.framework.cache-test
  (:use code.test)
  (:require [code.framework.cache :refer :all]
            [std.block :as block]
            [std.fs :as fs])
  (:refer-clojure :exclude [update]))

^{:refer code.framework.cache/prepare-out :added "3.0"}
(fact "prepares code block for output"

  (prepare-out
   {'ns {'var
         {:test {:code [(block/block '(1 2 3))]}}}})
  => '{ns {var {:test {:code "(1 2 3)"}}}})

^{:refer code.framework.cache/prepare-in :added "3.0"}
(fact "prepares input string to code blocks"

  (-> (prepare-in
       '{ns {var
             {:test {:code "(+ 1 2 3)"}}}})
      (get-in '[ns var :test :code])
      (first)
      (block/value))
  => '(+ 1 2 3))

^{:refer code.framework.cache/file-modified? :added "3.0"}
(fact "checks if code file has been modified from the cache"

  (file-modified? 'code.manage
                  "src/code/manage.clj")
  => boolean?)

^{:refer code.framework.cache/fetch :added "3.0"}
(fact "reads a the file or gets it from cache"

  (with-redefs [fs/exists? (constantly false)
                fs/create-directory (constantly nil)
                fs/select (constantly [])]
    (fetch 'code.manage
           "src/code/manage.clj"))
  => (any map? nil?))

^{:refer code.framework.cache/update :added "3.0"}
(fact "updates values to the cache"
  (let [memory (atom {})]
    (with-redefs [file-modified? (constantly true)
                  fs/last-modified (constantly 100)
                  fs/path (constantly "cache/path")
                  spit (fn [_ _] nil)]

      (update 'code.manage "src/code/manage.clj" {'code.manage {'var {:test {:code [(block/parse-string "(+ 1 2)")]}}}} memory "cache" ".cache")
      (let [entry (get @memory 'code.manage)]
        {:cache-modified (:cache-modified entry)
         :file-modified (:file-modified entry)
         :code (-> entry (get-in [:data 'code.manage 'var :test :code]) first block/string)}))
    => {:cache-modified 100 :file-modified 100 :code "(+ 1 2)"}))

^{:refer code.framework.cache/init-memory! :added "3.0"}
(fact "initialises memory with cache files"
  (let [memory (atom {})
        loaded (atom nil)]
    (with-redefs [fs/exists? (constantly true)
                  fs/select (constantly ["file1"])
                  slurp (constantly "{:meta {:namespace code.manage :file-modified 100} code.manage {var {:test {:code \"(+ 1 2)\"}}}}")
                  fs/last-modified (constantly 200)
                  *memory-loaded* loaded]
      (init-memory! memory "cache")
      (-> @memory
          (get-in ['code.manage :data 'code.manage 'var :test :code])
          first
          block/string)))
    => "(+ 1 2)")

^{:refer code.framework.cache/purge :added "3.0"}
(fact "clears all entries from the cache"
  (let [memory (atom {:a 1})
        loaded (atom true)]
    (with-redefs [*memory* memory
                  *memory-loaded* loaded]
      (purge)
      [@memory @loaded])
    => [{} nil]))

(comment
  (./incomplete)
  (fs/last-modified "src/hara/string/base/ansi.clj")
  (:file-modified (get @*memory* 'std.print.ansi)
                  (get @*memory* 'std.print.ansi-test))

  (purge)

  (fetch 'code.framework "src/hara/code/framework.clj")
  [(:file-modified (fetch 'code.manage "src/hara/code.clj"))]
  (- (fs/last-modified "src/hara/string.clj")
     (:file-modified
      (get @*memory* 'std.string)))

  (- (fs/last-modified "src/hara/core.clj")
     (:file-modified
      (get @*memory* 'platform)))

  1596196537523

  (keys (prepare-in (read-string (slurp (str +cache-dir+ "/code.framework.docstring.cache")))))

  (code.framework.docstring :meta)
  (fetch 'code.framework "src/hara/code/framework.clj")
  (./incomplete 'code.framework.docstring)
  (+ 1 2 3)

  (dissoc (get @*memory* 'std.string)
          :data)
  {:cache-modified 1596199530735, :file-modified nil}

  (h/bench-ms (init-memory!)))
