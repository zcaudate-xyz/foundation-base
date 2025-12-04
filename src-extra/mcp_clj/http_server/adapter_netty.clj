(ns mcp-clj.http-server.adapter
  "Adapter for Netty HTTP Server with SSE support"
  (:require
    [clojure.string :as str]
    [mcp-clj.log :as log])
  (:import
    (io.netty.bootstrap ServerBootstrap)
    (io.netty.buffer ByteBuf Unpooled)
    (io.netty.channel
      ChannelFutureListener
      ChannelHandlerContext
      ChannelInitializer
      ChannelOption
      SimpleChannelInboundHandler)
    (io.netty.channel.nio NioEventLoopGroup)
    (io.netty.channel.socket.nio NioServerSocketChannel)
    (io.netty.handler.codec.http
      DefaultFullHttpResponse
      DefaultHttpContent
      DefaultHttpResponse
      DefaultLastHttpContent
      FullHttpRequest
      HttpHeaderNames
      HttpHeaderValues
      HttpMethod
      HttpObjectAggregator
      HttpResponseStatus
      HttpServerCodec
      HttpVersion
      QueryStringDecoder)
    (io.netty.util CharsetUtil)
    (java.net InetSocketAddress)
    (java.nio.charset Charset)))

(defn- set-headers!
  [^DefaultHttpResponse response headers]
  (let [h (.headers response)]
    (doseq [[k v] headers]
      (.set h (name k) (str v)))))

(defn- create-netty-output-stream
  "Create an OutputStream that writes to a Netty ChannelHandlerContext"
  [^ChannelHandlerContext ctx]
  (proxy [java.io.OutputStream] []
    (write
      ([b]
       (if (integer? b)
         (let [buf (.buffer (.alloc ctx) 1)]
           (.writeByte buf b)
           (.writeAndFlush ctx (DefaultHttpContent. buf)))
         (let [buf (.buffer (.alloc ctx) (alength ^bytes b))]
           (.writeBytes buf ^bytes b)
           (.writeAndFlush ctx (DefaultHttpContent. buf)))))
      ([b off len]
       (let [buf (.buffer (.alloc ctx) len)]
         (.writeBytes buf ^bytes b (int off) (int len))
         (.writeAndFlush ctx (DefaultHttpContent. buf)))))
    (flush [] (.flush ctx))
    (close []
      (.writeAndFlush ctx DefaultLastHttpContent/EMPTY_LAST_CONTENT)
      (.close ctx))))

(defn- handle-request
  [^ChannelHandlerContext ctx ^FullHttpRequest netty-req handler]
  (let [uri (.uri netty-req)
        decoder (QueryStringDecoder. uri)
        path (.path decoder)
        method (-> netty-req .method .name .toLowerCase keyword)
        headers (into {} (for [^java.util.Map$Entry entry (.headers netty-req)]
                           [(str/lower-case (.getKey entry)) (.getValue entry)]))
        body-buf (.content netty-req)
        body-bytes (byte-array (.readableBytes body-buf))
        _ (.readBytes body-buf body-bytes)
        request-map {:server-port       (.getPort ^InetSocketAddress (.localAddress (.channel ctx)))
                     :server-name       (.getHostName ^InetSocketAddress (.localAddress (.channel ctx)))
                     :remote-addr       (.getHostString ^InetSocketAddress (.remoteAddress (.channel ctx)))
                     :uri               path
                     :query-string      (let [raw (.rawQuery decoder)] (if (str/blank? raw) nil raw))
                     :query-params      (fn []
                                          (into {} (for [[k v] (.parameters decoder)]
                                                     [k (first v)])))
                     :scheme            :http
                     :request-method    method
                     :headers           headers
                     :body              (java.io.ByteArrayInputStream. body-bytes)
                     ;; Callbacks for SSE/Async handling
                     :on-response-done  (fn [] (.close ctx))
                     :on-response-error (fn [] (.close ctx))
                     :response-body     (create-netty-output-stream ctx)}
        response (handler request-map)]

    (if (fn? (:body response))
      ;; Streaming Response (SSE)
      (let [status (HttpResponseStatus/valueOf (:status response))
            netty-res (DefaultHttpResponse. HttpVersion/HTTP_1_1 status)]
        (set-headers! netty-res (:headers response))
        (.set (.headers netty-res) HttpHeaderNames/TRANSFER_ENCODING HttpHeaderValues/CHUNKED)
        (.writeAndFlush ctx netty-res)
        ;; Execute the body function which writes to the OutputStream
        ((:body response) (create-netty-output-stream ctx)))

      ;; Normal Response
      (let [status (HttpResponseStatus/valueOf (:status response))
            body (:body response)
            content (cond
                      (string? body) (Unpooled/copiedBuffer ^String body CharsetUtil/UTF_8)
                      (bytes? body) (Unpooled/copiedBuffer ^bytes body)
                      :else (Unpooled/buffer 0))
            netty-res (DefaultFullHttpResponse. HttpVersion/HTTP_1_1 status content)]
        (set-headers! netty-res (:headers response))
        (.setInt (.headers netty-res) HttpHeaderNames/CONTENT_LENGTH (.readableBytes content))
        (let [keep-alive? (.contentEqualsIgnoreCase HttpHeaderValues/KEEP_ALIVE
                            (.get (.headers netty-req) HttpHeaderNames/CONNECTION))]
          (if keep-alive?
            (do
              (.set (.headers netty-res) HttpHeaderNames/CONNECTION HttpHeaderValues/KEEP_ALIVE)
              (.writeAndFlush ctx netty-res))
            (.addListener (.writeAndFlush ctx netty-res) ChannelFutureListener/CLOSE)))))))

(defn- create-handler
  [handler]
  (proxy [SimpleChannelInboundHandler] []
    (channelRead0 [ctx msg]
      (when (instance? FullHttpRequest msg)
        (try
          (handle-request ctx msg handler)
          (catch Exception e
            (.printStackTrace e)
            (let [res (DefaultFullHttpResponse.
                        HttpVersion/HTTP_1_1
                        HttpResponseStatus/INTERNAL_SERVER_ERROR
                        (Unpooled/copiedBuffer (.getMessage e) CharsetUtil/UTF_8))]
              (.addListener (.writeAndFlush ctx res) ChannelFutureListener/CLOSE))))))))

(defn run-server
  "Start a Netty HTTP Server with the given Ring handler.
   Returns a server map containing :server and :stop fn."
  [handler {:keys [port] :or {port 8080}}]
  (let [boss-group (NioEventLoopGroup. 1)
        worker-group (NioEventLoopGroup.)
        bootstrap (ServerBootstrap.)
        _ (.group bootstrap boss-group worker-group)
        _ (.channel bootstrap NioServerSocketChannel)
        _ (.childHandler bootstrap
                         (proxy [ChannelInitializer] []
                           (initChannel [ch]
                             (let [p (.pipeline ch)]
                               (.addLast p "codec" (HttpServerCodec.))
                               (.addLast p "aggregator" (HttpObjectAggregator. 65536))
                               (.addLast p "handler" (create-handler handler))))))
        _ (.option bootstrap ChannelOption/SO_BACKLOG (int 128))
        _ (.childOption bootstrap ChannelOption/SO_KEEPALIVE true)
        channel-future (.bind bootstrap port)
        channel (.channel (.sync channel-future))]

    {:server channel
     :port port
     :stop (fn []
             (.close channel)
             (.shutdownGracefully boss-group)
             (.shutdownGracefully worker-group))}))
