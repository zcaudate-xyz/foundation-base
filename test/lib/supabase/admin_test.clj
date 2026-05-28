(ns lib.supabase.admin-test
  (:use code.test)
  (:require [lib.supabase.admin :refer :all]
            [lib.supabase.common :as common]))

(defn sample-client
  []
  (common/create-client "http://localhost:54321" "key-123" {}))

^{:refer lib.supabase.admin/api-signup-create :added "4.1"}
(fact "creates auth users through the admin API"
  (with-redefs [common/api-call (fn [opts body] [opts body])]
    (api-signup-create {"email" "a@a.com"} {:key "key"}))
  => [{:key "key"
       :route "/auth/v1/admin/users"
       :type :service}
      {"email" "a@a.com"}])

^{:refer lib.supabase.admin/api-signup-delete :added "4.1"}
(fact "deletes auth users through the admin API"
  (with-redefs [common/api-call (fn [opts body] [opts body])]
    (api-signup-delete "user-1" {:key "key"}))
  => [{:key "key"
       :method :delete
       :route "/auth/v1/admin/users/user-1"
       :type :service}
      {}])

^{:refer lib.supabase.admin/list-users :added "4.1"}
(fact "lists users through the admin API"
  (with-redefs [common/api-call (fn [opts body] [opts body])]
    (list-users (sample-client) {:page 1}))
  => #(let [[opts body] %]
        (and (= {} body)
             (= :get (:method opts))
             (= :service (:type opts))
             (= "/auth/v1/admin/users" (:route opts)))))

^{:refer lib.supabase.admin/get-user-by-id :added "4.1"}
(fact "gets a user by id"
  (with-redefs [common/api-call (fn [opts body] [opts body])]
    (get-user-by-id (sample-client) "user-1"))
  => #(let [[opts body] %]
        (and (= {} body)
             (= :get (:method opts))
             (= "/auth/v1/admin/users/user-1" (:route opts)))))

^{:refer lib.supabase.admin/create-user :added "4.1"}
(fact "wraps user creation"
  (with-redefs [api-signup-create (fn [body opts] [body opts])]
    (create-user (sample-client) {"email" "a@a.com"} {:invite true}))
  => #(let [[body opts] %]
        (and (= {"email" "a@a.com"} body)
             (= true (:invite opts))
             (map? (:client opts)))))

^{:refer lib.supabase.admin/update-user :added "4.1"}
(fact "updates a user through the admin API"
  (with-redefs [common/api-call (fn [opts body] [opts body])]
    (update-user (sample-client) "user-1" {"ban_duration" "none"}))
  => #(let [[opts body] %]
        (and (= {"ban_duration" "none"} body)
             (= :put (:method opts))
             (= "/auth/v1/admin/users/user-1" (:route opts)))))

^{:refer lib.supabase.admin/delete-user :added "4.1"}
(fact "wraps user deletion"
  (with-redefs [api-signup-delete (fn [uid opts] [uid opts])]
    (delete-user (sample-client) "user-1" {:soft true}))
  => #(let [[uid opts] %]
        (and (= "user-1" uid)
             (= true (:soft opts))
             (map? (:client opts)))))

^{:refer lib.supabase.admin/invite-user-by-email :added "4.1"}
(fact "invites a user by email"
  (with-redefs [common/api-call (fn [opts body] [opts body])]
    (invite-user-by-email (sample-client) {"email" "a@a.com"}))
  => #(let [[opts body] %]
        (and (= {"email" "a@a.com"} body)
             (= :service (:type opts))
             (= "/auth/v1/invite" (:route opts)))))
