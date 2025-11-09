(ns code.ai.session
  (:require [std.fs :as fs]
            [std.lib :as h :use [defimpl]]
            [std.concurrent.relay :as relay]))

(defonce *instance*
  (relay/relay {:type :process
                :args ["bash"]}))

