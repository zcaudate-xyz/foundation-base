(ns js.cell.runtime.browser
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.cell.kernel :as cl]
             [js.cell.runtime.link :as runtime-link]]})

(defn.js make-webworker-cell
  "creates a kernel cell backed by a browser WebWorker"
  [script]
  (return (cl/make-cell (runtime-link/make-webworker-link script))))

(defn.js make-sharedworker-cell
  "creates a kernel cell backed by a browser SharedWorker"
  [script]
  (return (cl/make-cell (runtime-link/make-sharedworker-link script))))
