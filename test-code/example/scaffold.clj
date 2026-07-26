(ns example.scaffold
  (:require [hara.lib.datomic :as datomic]
            [example.data :as examples]
            [data.family :as family]))

(defn family-db
  ([] (family-db nil))
  ([m]
   (datomic/test-instance (merge {:schema family/family-links}
                                 m))))

(defn account-db
  ([] (account-db nil))
  ([m]
   (datomic/test-instance (merge {:schema examples/account-name-age-sex}
                                 m))))

(defn order-db
  ([] (order-db nil))
  ([m]
   (datomic/test-instance (merge {:schema examples/account-orders-items-image}
                                 m))))

(defn link-db
  ([] (link-db nil))
  ([m]
   (datomic/test-instance (merge {:schema examples/link-value-next}
                                 m))))

(defn node-db
  ([] (node-db nil))
  ([m]
   (datomic/test-instance (merge {:schema examples/node-animal-plant}
                                 m))))

(defn bookstore-db
  ([]
   (doto (datomic/test-instance {:schema examples/bookstore})
     (datomic/insert! [{:db/id [[:AUS]]
                        :address {:country "Australia"}}
                       {:store {:address [[:AUS]]
                                :name "Koala Books"
                                :inventories #{{:count 10
                                                :cover :hard
                                                :book {:name "The Count of Monte Cristo"
                                                       :author "Alexander Dumas"}}
                                               {:count 5
                                                :cover :hard
                                                :book {:name "Tom Sawyer"
                                                       :author "Mark Twain"}}
                                               {:count 3
                                                :cover :soft
                                                :book {:name "Les Miserables"
                                                       :author "Victor Hugo"}}}}}]))))

(comment
  (family-db)
  (link-db)
  (datomic/test-instance {:schema family/family-links}))

