(ns std.timeseries
  (:require [std.lib.foundation :as f]
            [std.timeseries.common :as common]
            [std.timeseries.journal :as journal]
            [std.timeseries.process :as process])
  (:refer-clojure :exclude [merge derive]))

(f/intern-in common/create-template

             journal/journal
             journal/add-bulk
             journal/add-entry
             journal/update-meta
             journal/derive
             journal/merge
             journal/select

             process/process)
