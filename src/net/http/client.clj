(ns net.http.client
  (:require [net.http.common :as common]
            [std.lib.collection]
            [std.lib.enum :as enum]
            [std.object :as object]
            [std.object.framework.read :as read]
            [std.string.coerce]
            [std.string.common])
  (:import (java.net.http HttpRequest HttpRequest$Builder HttpRequest$BodyPublishers HttpResponse HttpResponse$BodySubscribers HttpResponse$BodyHandlers HttpClient HttpClient$Builder HttpClient$Redirect HttpClient$Version) (jdk.internal.net.http HttpClientBuilderImpl) (java.util.function Function Supplier) (java.net URI))
  (:refer-clojure :exclude [get]))

(defn- ->function ^Function [f]
  (reify Function
    (apply [this x] (f x))))

(defn- ->supplier ^Supplier [obj]
  (reify Supplier
    (get [this] obj)))


(object/map-like
 HttpClientBuilderImpl
 {:tag "http.client.builder" :read :class})


(defn wrap-get-optional
  "wraps function, nil-punning outputs for java.util.Optional
 
   ((wrap-get-optional identity) (java.util.Optional/of \"hello\"))
   => \"hello\""
  {:added "3.0"}
  ([f]
   (fn [obj]
     (let [res (f obj)]
       (if (instance? java.util.Optional res)
         (if-not (.isEmpty ^java.util.Optional res)
           (.get ^java.util.Optional res))
         res)))))

(def +getter-opts+
  (assoc read/+read-get-opts+ :prefix ""))

(defn getter-methods
  "returns methods of one argument
 
   (keys (getter-methods java.net.http.HttpResponse))
   => [:body :headers :previous-response :request
       :ssl-session :status-code :uri :version]"
  {:added "3.0"}
  ([cls]
   (->> (object/read-getters-form cls +getter-opts+ object/query-hierarchy)
        (eval)
        (std.lib.collection/map-vals #(update % :fn wrap-get-optional)))))

(object/map-like
 HttpClient
 {:tag "http.client"
  :read {:methods (-> (getter-methods HttpClient)
                      (select-keys [:authenticator :connect-timeout
                                    :cookie-handler :executor :follow-redirects
                                    :proxy :ssl-context :ssl-parameters :version]))}}

 HttpRequest
 {:tag "http.request"
  :read {:methods (-> (getter-methods HttpRequest)
                      (select-keys [:method :headers :uri :timeout :version]))}}

 HttpResponse
 {:tag "http.response"
  :read {:methods (-> (getter-methods HttpResponse))}})

(def +http-version+
  (delay (enum/enum-map> HttpClient$Version)))

(def +http-redirect+
  (delay (enum/enum-map> HttpClient$Redirect)))

(defn- ms->duration
  ([ms]
   (java.time.Duration/ofMillis ms)))

(defn http-client-builder
  "constructor for java.net.http.HttpClientBuilder
 
   (http-client-builder)
   => java.net.http.HttpClient$Builder"
  {:added "3.0"}
  (^HttpClient$Builder []
   (http-client-builder {}))
  (^HttpClient$Builder [opts]
   (let [{:keys [connect-timeout
                 cookie-handler
                 executor
                 follow-redirects
                 priority
                 proxy
                 ssl-context
                 ssl-parameters
                 version]} opts]
     (cond-> (HttpClient/newBuilder)
       connect-timeout  (.connectTimeout (ms->duration connect-timeout))
       cookie-handler   (.cookieHandler cookie-handler)
       executor         (.executor executor)
       follow-redirects (.followRedirects (@+http-redirect+ follow-redirects))
       priority         (.priority priority)
       proxy            (.proxy proxy)
       ssl-context      (.sslContext ssl-context)
       ssl-parameters   (.sslParameters ssl-parameters)
       version          (.version (@+http-version+ version))))))

(defn ^HttpClient http-client
  "constructor for java.net.http.HttpClient
 
   (http-client)
   => java.net.http.HttpClient"
  {:added "3.0"}
  ([] (http-client {}))
  ([opts]
   (if (map? opts)
     (.build (http-client-builder opts))
     opts)))

(defonce +default-client+ (delay (http-client)))

(defn request-body
  "constructs a relevant body publisher
 
   (request-body \"\")
   => java.net.http.HttpRequest$BodyPublisher"
  {:added "3.0"}
  ([body]
   (cond (instance? java.io.InputStream body)
         (HttpRequest$BodyPublishers/ofInputStream (->supplier body))

         (bytes? body)
         (HttpRequest$BodyPublishers/ofByteArray body)

         :else
         (HttpRequest$BodyPublishers/ofString (or body "")))))

(defn request-headers
  "constructs a relevant request header
 
   (vec (request-headers {\"Content\" \"html\"
                          \"Tags\" [\"a\" \"b\" \"c\"]}))
   => [\"Content\" \"html\"
       \"Tags\" \"a\" \"Tags\" \"b\" \"Tags\" \"c\"]"
  {:added "3.0"}
  ([headers]
   (->> headers
        (mapcat (fn [[k v :as p]]
                  (let [k (std.string.coerce/to-string k)]
                    (if (sequential? v)
                      (interleave (repeat k) v)
                      [k v]))))
        (into-array String))))

(defn http-request-builder
  "constructor for java.net.http.HttpRequestBuilder
 
   (http-request-builder {:uri \"http://www.yahoo.com\"})
   => java.net.http.HttpRequest$Builder"
  {:added "3.0"}
  (^HttpRequest$Builder
   [opts]
   (let [{:keys [expect-continue?
                 headers
                 method
                 timeout
                 uri
                 version
                 body]} opts]
     (cond-> (HttpRequest/newBuilder)
       (some? expect-continue?) (.expectContinue expect-continue?)
       (seq headers)            (.headers (request-headers headers))
       method                   (.method (std.string.common/upper-case (name method))
                                         (request-body body))
       timeout                  (.timeout (ms->duration timeout))
       uri                      (.uri (URI/create uri))
       version                  (.version (@+http-version+ version))))))

(defn http-request
  "constructor for java.net.http.HttpRequest
 
   (http-request \"http://www.yahoo.com\")
   => java.net.http.HttpRequest"
  {:added "3.0"}
  ([req]
   (http-request req :get nil))
  ([req method {:keys [expect-continue?
                       headers
                       timeout
                       version
                       body]
                :as opts}]
   (let [req (if (string? req)
               {:uri req}
               req)
         req (if (map? req)
               (.build (http-request-builder (merge opts
                                                    (assoc req :method method))))
               req)]
     req)))

(def +http-response-handler+
  (delay
   {:string (HttpResponse$BodyHandlers/ofString)
    :stream (HttpResponse$BodyHandlers/ofInputStream)
    :bytes  (HttpResponse$BodyHandlers/ofByteArray)
    :lines  (HttpResponse$BodyHandlers/ofLines)}))

(defn response-map
  "constucts a map from java.net.http.HttpResponse"
  {:added "3.0"}
  ([^HttpResponse resp]
   {:status  (.statusCode resp)
    :body    (.body resp)
    :version (-> resp .version .name)
    :headers (into {}
                   (map (fn [[k v]] [(keyword k) (if (> (count v) 1) (vec v) (first v))]))
                   (.map (.headers resp)))}))

(defn request
  "performs a http request"
  {:added "3.0"}
  ([req]
   (cond (map? req)
         (request (:uri req) req)

         (string? req)
         (request req {})

         :else
         (throw (ex-info "Input not valid" {:input req}))))
  ([uri {:keys [client as raw type callback error method handler] :as opts
         :or {type   :sync
              as     :string
              method :get
              client @+default-client+}}]
   (let [client (http-client client)
         req    (http-request uri method opts)
         handler (or handler (@+http-response-handler+ as))]
     (case type
       :sync   (let [res    (.send client req handler)]
                 (if raw
                   res
                   (response-map res)))
       :async  (cond-> (.sendAsync client req handler)
                 (not raw)  (.thenApply     (->function response-map))
                 callback   (.thenApply     (->function callback))
                 error      (.exceptionally (->function error)))))))

(defmacro create-http-methods
  "creates the standard http get, post, etc requests"
  {:added "3.0"}
  ([ks]
   (mapv (fn [k]
           (let [name (symbol (name k))]
             `(defn ~name
                ([~'url]
                 (~name ~'url nil))
                ([~'url ~'opts]
                 (request ~'url (assoc ~'opts :method ~k))))))
         ks)))

(def +init+ (create-http-methods [:get :post :patch :put :delete :head]))

(defn endpoint-data
  "gets the data returned from endpoint
 
   (endpoint-data {:status 200
                   :body (str {:data [1 2 3 4]
                               :status :return})})
   => [1 2 3 4]"
  {:added "3.0"}
  ([result]
   (if (= 200 (:status result))
     (let [body (common/-read-value (:body result) format)]
       (if (= :return (:status body))
         (:data body)
         (throw (ex-info "Error" body))))
     (throw (ex-info "Non 200 status" result)))))

(defn remote
  "creates a remote access to an endpoint"
  {:added "3.0"}
  ([url {:keys [id args format] :as opts
         :or {format :edn}}]
   (let [result (request url {:method :post
                              :body (common/-write-value {:id id :args args} format)})]
     (endpoint-data result))))

(defn stream-lines
  "reads lines from a stream"
  {:added "4.0"}
  ([is callback]
   (let [br (java.io.BufferedReader.
             (java.io.InputStreamReader. is))]
     (future
       (loop [l (.readLine ^java.io.BufferedReader br)]
         (when l
           (callback l)
           (recur (.readLine ^java.io.BufferedReader br))))))))

(defn mock-endpoint
  "creates a mock-endpoint for testing
 
   ((mock-endpoint (fn [req] {:status 200
                              :body (str {:status :return
                                          :data req})}) \"/endpoint\")
    {:id   :add
     :args [1 2 3]})
   => {:method :post, :route \"/endpoint\", :body \"{:id :add, :args [1 2 3]}\"}"
  {:added "3.0"}
  ([handler route]
   (mock-endpoint handler route {}))
  ([handler route {:keys [format]
                   :or {format :edn}}]
   (fn [{:keys [id args]}]
     (let [body (common/-write-value {:id id :args args} format)
           result (handler {:method :post
                            :route route
                            :body body})]
       (endpoint-data result)))))
