(ns std.concurrent
  (:require [std.concurrent.bus :as bus]
            [std.concurrent.executor :as executor]
            [std.concurrent.pool :as pool]
            [std.concurrent.print :as print]
            [std.concurrent.queue :as queue]
            [std.concurrent.relay :as relay]
            [std.concurrent.request :as request]
            [std.concurrent.request-apply :as request-apply]
            [std.concurrent.request-command :as request-command]
            [std.concurrent.thread :as thread]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [take pop take-last peek remove send]))

(f/intern-all std.concurrent.thread
              std.concurrent.queue
              std.concurrent.executor
              std.concurrent.atom)

(f/intern-in  bus/bus
              bus/bus?
              bus/bus:all-ids
              bus/bus:all-threads
              bus/bus:close
              bus/bus:close-all
              bus/bus:create
              bus/bus:get-count
              bus/bus:get-thread
              bus/bus:get-id
              bus/bus:get-queue
              bus/bus:has-id?
              bus/bus:register
              bus/bus:deregister
              bus/bus:wait
              bus/bus:send
              bus/bus:send-all
              bus/bus:kill
              bus/bus:kill-all
              bus/bus:open
              bus/bus:with-temp
              bus/bus:reset-counters

              pool/pool
              pool/pool?
              pool/pool:acquire
              pool/pool:cleanup
              pool/pool:create
              pool/pool:dispose
              pool/pool:dispose-over
              pool/pool:dispose:mark
              pool/pool:dispose:unmark
              pool/pool:release
              pool/pool:resources:busy
              pool/pool:resources:idle
              pool/pool:resources:thread
              pool/pool:with-resource

             [req:call-single    request/request-single]
             [req:call-bulk      request/request-bulk]
             [req:process-single request/process-single]
             [req:process-bulk   request/process-bulk]
             request/bulk
             request/bulk:inputs
             request/bulk:map
             request/bulk:transact
             request/transact

             request/req-fn
             request/req
             request/req:bulk
             request/req:transact
             request/req:in
             request/req:opts-clean
             request/req:opts
             request-apply/req:applicative
             request-command/req:command
             request-command/req:run
             
             relay/send
             relay/relay:create
             relay/relay
             [relay:bus relay/get-bus])
