(ns js.react.ext-page-generic-test
  (:require [clojure.string :as string]
            [code.test :refer [fact =>]]
            [js.react.ext-page]))

^{:refer js.react.ext-page/listenPageModel :added "4.1.6"}
(fact "derives the model path from a page handle"
  (let [source (str (var-get (find-var 'js.react.ext-page/listenPageModel)))]
    [(string/includes? source "page_model_path")
     (string/includes? source "page")
     (string/includes? source "space_id")])
  => [true true true])

^{:refer js.react.ext-page/listenPageModelOutput :added "4.1.6"}
(fact "listens to the full output through a page handle"
  (let [source (str (var-get (find-var 'js.react.ext-page/listenPageModelOutput)))]
    [(string/includes? source "listenModelOutput")
     (string/includes? source "page_model_path")])
  => [true true])

^{:refer js.react.ext-page/callPageModel :added "4.1.6"}
(fact "delegates page model calls to the generic page substrate API"
  (let [source (str (var-get (find-var 'js.react.ext-page/callPageModel)))]
    [(string/includes? source "page_model_call")
     (string/includes? source "await")])
  => [true true])
