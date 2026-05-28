(ns lib.supabase.query-test
  (:use code.test)
  (:require [lib.supabase.query :refer :all]
            [lib.supabase.common :as common]))

(defn sample-client
  []
  (common/create-client "http://localhost:54321" "key-123" {:schema_name "scratch"}))

(def sample-table
  (atom {:id 'Entry :static/schema "scratch"}))

^{:refer lib.supabase.query/url-encode :added "4.1"}
(fact "URL-encodes query values"
  (url-encode "hello world")
  => "hello+world")

^{:refer lib.supabase.query/append-query :added "4.1"}
(fact "appends encoded query params"
  (append-query "/rest/v1/Entry" [["select" "*"] ["limit" 1]])
  => "/rest/v1/Entry?select=*&limit=1")

^{:refer lib.supabase.query/entry-meta :added "4.1"}
(fact "extracts table metadata from derefable entries"
  (entry-meta sample-table)
  => {:id 'Entry :schema "scratch"})

^{:refer lib.supabase.query/table-name :added "4.1"}
(fact "resolves the table name"
  (table-name sample-table)
  => "Entry")

^{:refer lib.supabase.query/schema-headers :added "4.1"}
(fact "builds schema profile headers"
  (schema-headers sample-table)
  => {"Content-Profile" "scratch"})

^{:refer lib.supabase.query/filter-clause :added "4.1"}
(fact "serializes filter clauses"
  [(filter-clause [:eq "id"])
   (filter-clause "gt.1")]
  => ["eq.id" "gt.1"])

^{:refer lib.supabase.query/query-params :added "4.1"}
(fact "builds select, filter, order, limit, and offset params"
  (query-params {:select "id,name"
                 :filters {:id [:eq 1]}
                 :order [[:id :desc]]
                 :limit 10
                 :offset 5})
  => [["select" "id,name"]
      ["id" "eq.1"]
      ["order" "id.desc"]
      ["limit" 10]
      ["offset" 5]])

^{:refer lib.supabase.query/api-select-all :added "4.1"}
(fact "selects all rows through api-call"
  (with-redefs [common/api-call (fn [opts body] [opts body])]
    (api-select-all sample-table {:key "key"}))
  => [{:key "key"
       :group :rest
       :method :get
       :headers {"Content-Profile" "scratch"}
       :route "/Entry?select=*"}
      {}])

^{:refer lib.supabase.query/select :added "4.1"}
(fact "builds filtered select requests"
  (with-redefs [common/api-call (fn [opts body] [opts body])]
    (select (sample-client)
            sample-table
            {:select "id"
             :filters {:id [:eq 1]}
             :limit 1}))
  => #(let [[opts body] %]
        (and (= {} body)
             (= :get (:method opts))
             (= :rest (:group opts))
             (= "http://localhost:54321" (:base_url (:client opts)))
             (= "/Entry?select=id&id=eq.1&limit=1" (:route opts)))))

^{:refer lib.supabase.query/prefer-header :added "4.1"}
(fact "builds Prefer header values"
  (prefer-header {:returning :representation
                  :count :exact
                  :upsert? true})
  => "return=representation,count=exact,resolution=merge-duplicates")

^{:refer lib.supabase.query/write-op :added "4.1"}
(fact "builds write requests with Prefer headers"
  (with-redefs [common/api-call (fn [opts body] [opts body])]
    (write-op (sample-client)
              :post
              sample-table
              [{"id" 1}]
              {:returning :representation}))
  => #(let [[opts body] %]
        (and (= [{"id" 1}] body)
             (= :post (:method opts))
             (= "return=representation" (get-in opts [:headers "Prefer"])))))

^{:refer lib.supabase.query/insert :added "4.1"}
(fact "wraps inserts through write-op"
  (with-redefs [write-op (fn [client method table rows opts]
                           [client method table rows opts])]
    (insert (sample-client) sample-table [{"id" 1}] {:returning :representation}))
  => #((fn [[client method table rows opts]]
         (and (map? client)
              (= :post method)
              (= sample-table table)
              (= [{"id" 1}] rows)
              (= :representation (:returning opts))))
      %))

^{:refer lib.supabase.query/upsert :added "4.1"}
(fact "marks upserts with merge-duplicates resolution"
  (with-redefs [write-op (fn [_client _method _table _rows opts] opts)]
    (upsert (sample-client) sample-table [{"id" 1}] {}))
  => {:upsert? true})

^{:refer lib.supabase.query/update :added "4.1"}
(fact "builds patch requests with filters"
  (with-redefs [common/api-call (fn [opts body] [opts body])]
    (update (sample-client)
            sample-table
            {"name" "updated"}
            {:filters {:id [:eq 1]}}))
  => #(let [[opts body] %]
        (and (= {"name" "updated"} body)
             (= :patch (:method opts))
             (= :rest (:group opts))
             (= "/Entry?id=eq.1" (:route opts))
             (nil? (get-in opts [:headers "Prefer"])))))

^{:refer lib.supabase.query/delete :added "4.1"}
(fact "builds delete requests with filters"
  (with-redefs [common/api-call (fn [opts body] [opts body])]
    (delete (sample-client)
            sample-table
            {:filters {:id [:eq 1]}}))
  => #(let [[opts body] %]
        (and (= {} body)
             (= :delete (:method opts))
             (= :rest (:group opts))
             (= "/Entry?id=eq.1" (:route opts)))))
