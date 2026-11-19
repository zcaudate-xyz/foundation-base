(ns dupasupa.pages.public.index
  (:require [dupasupa.seed :as seed]))
(def SITE-MAP-REQUIREMENTS {:tables [] :rpcs [seed/ping]})
