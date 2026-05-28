(ns lib.rabbitmq.api-test
  (:use code.test)
  (:require [lib.rabbitmq.api :refer :all]))

^{:refer lib.rabbitmq.api/classify-args :added "4.1.4"}
(fact "classifies api link fragments"
  [(classify-args "hello")
   (classify-args "{hello}")
   (classify-args "{%1:hello}")]
  => [[:string "hello"]
      [:keyword '(:hello rabbitmq)]
      [:entry [1 "hello"]]])

^{:refer lib.rabbitmq.api/build-args :added "4.1.4"}
(fact "builds link argument vectors"
  (build-args [[:string "hello"]
               [:entry [1 "world"]]
               [:keyword '(:hello rabbitmq)]])
  => ["hello" 'world '(:hello rabbitmq)])

^{:refer lib.rabbitmq.api/link-args :added "4.1.4"}
(fact "parses link templates"
  (link-args "hello/{world}/{%1:foo}/{%2:bar}")
  => {:inputs ["hello" '(:world rabbitmq) 'foo 'bar]
      :vargs ['foo 'bar]})

^{:refer lib.rabbitmq.api/create-link-form :added "4.1.4"}
(fact "creates a link request form"
  (create-link-form '{:inputs ["hello" (:world rabbitmq) foo bar]
                      :vargs [foo bar]}
                    :delete)
  => '([rabbitmq foo bar]
       (lib.rabbitmq.request/request
        rabbitmq
        (std.string/joinl ["hello" (:world rabbitmq) foo bar] "/")
        :delete)))

^{:refer lib.rabbitmq.api/create-body-form :added "4.1.4"}
(fact "creates a body request form"
  (create-body-form '{:inputs ["hello" (:world rabbitmq) foo bar]
                      :vargs [foo bar]}
                    :post)
  => '([rabbitmq foo bar body]
       (lib.rabbitmq.request/request
        rabbitmq
        (std.string/joinl ["hello" (:world rabbitmq) foo bar] "/")
        :post
        {:body body})))

^{:refer lib.rabbitmq.api/create-accessor-form :added "4.1.4"}
(fact "creates accessor forms"
  (create-accessor-form :cluster-name
                        {:link "cluster-name"
                         :methods {:setter :put}})
  => '(clojure.core/defn cluster-name
        ([rabbitmq]
         (lib.rabbitmq.request/request
          rabbitmq (std.string/joinl ["cluster-name"] "/")
          :get))
        ([rabbitmq body]
         (lib.rabbitmq.request/request
          rabbitmq (std.string/joinl ["cluster-name"] "/")
          :put {:body body}))))

^{:refer lib.rabbitmq.api/create-function-forms :added "4.1.4"}
(fact "creates CRUD forms for routes"
  (->> (create-function-forms :vhost
                              {:type :form
                               :link "vhosts/{%1:vhost}"
                               :methods #{:get :put :delete}})
       (map second))
  => '(get-vhost delete-vhost add-vhost))

^{:refer lib.rabbitmq.api/create-api-functions :added "4.1.4"}
(fact "creates the api function set"
  (-> (create-api-functions {:tmp-route {:link "overview"}})
      count)
  => 1)
