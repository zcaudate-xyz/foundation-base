(ns code.framework.cache-test
  (:use code.test)
  (:require [code.framework.cache :refer :all]
            [std.block :as block])
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

  (fetch 'code.manage
         "src/code/manage.clj")
  => (any map? nil?))

^{:refer code.framework.cache/update :added "3.0"}
(fact "updates values to the cache")

^{:refer code.framework.cache/init-memory! :added "3.0"}
(fact "initialises memory with cache files")

^{:refer code.framework.cache/purge :added "3.0"}
(fact "clears all entries from the cache"))

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
