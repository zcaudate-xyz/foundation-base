(ns xt.db.node.example-auth-profile-view-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:langs [:dart]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.view :as view]
             [xt.db.node.example-auth-profile :as example]
             [xt.db.node.example-auth-profile-view :as auth-view]]})

^{:refer xt.db.node.example-auth-profile-view/render :added "4.1"}
(fact "renders auth state as serializable target-neutral IR"
  (!.js
   (var root (auth-view/render
              {"email" "ada@example.com"
               "password" "secret"
               "display_name" "Ada"
               "pending" nil
               "error" nil
               "session" nil
               "profile" nil}))
   [(xt/x:get-key root "component")
    (view/validate
     (view/view-spec auth-view/VIEW_ID {} root))
    (view/json-safe? root)])
  => ["ui/column" true true])

^{:refer xt.db.node.example-auth-profile-view/install :added "4.1"}
(fact "installs auth functionality as substrate handlers"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (auth-view/install node {"space_id" "auth-space"
                             "group_id" "auth-group"})
    (promise/x:promise-then
     (view/dispatch node "auth-space"
                    (view/action (auth-view/action-id "set-email")
                                 (view/event-value ["value"]))
                    {"value" "ada@example.com"}
                    {})
     (fn [_]
       (repl/notify
        [(view/state-get node "auth-space" auth-view/VIEW_ID ["email"] nil)
         (xt/x:has-key? (xt/x:get-key node "handlers")
                        (auth-view/action-id "login"))]))))
  => ["ada@example.com" true])
