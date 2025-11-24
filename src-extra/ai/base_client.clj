(ns code.ai.base-client
  (:require [std.lib :as h]
            [std.fs :as fs])
  (:import (net.sf.classifier4j.summariser SimpleSummariser)))

(def ^:dynamic *HOME*
  (or (System/getenv "HARA_AI_HOME")
      ".prompts"))

(defn write-log
  [{:keys [type
           name
           input
           output]}]
  (fs/create-directory *HOME* "/logs/" ))
