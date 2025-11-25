(ns std.dispatch.hooks-test
  (:use code.test)
  (:require [std.dispatch.hooks :refer :all]
            [std.concurrent :as cc]))

^{:refer std.dispatch.hooks/counter :added "3.0"}
(fact "creates the executor counter"

  (counter)
  => (contains {:submit atom? :error atom?}))

^{:refer std.dispatch.hooks/inc-counter :added "3.0"}
(fact "increment the executor counter"
  (def -c- (counter))
  (inc-counter {:options {} :runtime {:counter -c-}} :submit)
  @(:submit -c-) => 1)

^{:refer std.dispatch.hooks/update-counter :added "3.0"}
(fact "updates the executor counter"
  (def -c- (counter))
  (update-counter {:options {} :runtime {:counter -c-}} :submit + 5)
  @(:submit -c-) => 5)

^{:refer std.dispatch.hooks/handle-entry :added "3.0"}
(fact "processes the hook on each stage"
  (handle-entry {:hooks {:on-test (fn [_] :ok)}} :on-test)
  => :ok)

^{:refer std.dispatch.hooks/on-submit :added "3.0"}
(fact "helper for the submit stage"
  (def -c- (counter))
  (on-submit {:options {} :runtime {:counter -c-} :hooks {}} {})
  @(:submit -c-) => 1)

^{:refer std.dispatch.hooks/on-queued :added "3.0"}
(fact "helper for the queued stage"
  (def -c- (counter))
  (on-queued {:options {} :runtime {:counter -c-} :hooks {}} {})
  @(:queued -c-) => 1)

^{:refer std.dispatch.hooks/on-batch :added "3.0"}
(fact "helper for the on-batch stage"
  (def -c- (counter))
  (on-batch {:options {} :runtime {:counter -c-} :hooks {}})
  @(:batch -c-) => 1)

^{:refer std.dispatch.hooks/on-process :added "3.0"}
(fact "helper for the process stage"
  (def -c- (counter))
  (on-process {:options {} :runtime {:counter -c-} :hooks {}} {})
  @(:process -c-) => 1)

^{:refer std.dispatch.hooks/on-process-bulk :added "3.0"}
(fact "helper for the process stage"
  (def -c- (counter))
  (on-process-bulk {:options {} :runtime {:counter -c-} :hooks {}} [1 2])
  @(:process -c-) => 2)

^{:refer std.dispatch.hooks/on-skip :added "3.0"}
(fact "helper for the skip stage"
  (def -c- (counter))
  (on-skip {:options {} :runtime {:counter -c-} :hooks {}} {})
  @(:skip -c-) => 1)

^{:refer std.dispatch.hooks/on-poll :added "3.0"}
(fact "helper for the poll stage"
  (def -c- (counter))
  (on-poll {:options {} :runtime {:counter -c-} :hooks {}} {})
  @(:poll -c-) => 1)

^{:refer std.dispatch.hooks/on-error :added "3.0"}
(fact "helper for the error stage"
  (def -c- (counter))
  (on-error {:options {} :runtime {:counter -c-} :hooks {}} {} (Exception.))
  @(:error -c-) => 1)

^{:refer std.dispatch.hooks/on-complete :added "3.0"}
(fact "helper for the complete stage"
  (def -c- (counter))
  (on-complete {:options {} :runtime {:counter -c-} :hooks {}} {} :result)
  @(:complete -c-) => 1)

^{:refer std.dispatch.hooks/on-complete-bulk :added "3.0"}
(fact "helper for the complete stage"
  (def -c- (counter))
  (on-complete-bulk {:options {} :runtime {:counter -c-} :hooks {}} [1 2] :result)
  @(:complete -c-) => 2)

^{:refer std.dispatch.hooks/on-shutdown :added "3.0"}
(fact "helper for the shutdown stage"
  (on-shutdown {:hooks {:on-shutdown (fn [_] :done)}})
  => :done)

^{:refer std.dispatch.hooks/on-startup :added "3.0"}
(fact "helper for the startup stage"
  (on-startup {:hooks {:on-startup (fn [_] :done)}})
  => :done)
