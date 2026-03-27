(ns rt.chromedriver.connection-test
  (:use code.test)
  (:require [rt.chromedriver.connection :as conn]
            [rt.chromedriver.impl :as impl]
            [std.lib :as h]))

(defonce +scaffold+ (atom nil))

(defn start-scaffold
  []
  (or @+scaffold+
      (reset! +scaffold+
              (let [port    (h/port:check-available 0)
                    process (h/sh {:args [impl/*chrome*
                                          "--headless"
                                          "--no-sandbox"
                                          (str "--remote-debugging-port=" port)
                                          "--remote-debugging-address=0.0.0.0"]
                                   :wait false})
                    result  (h/future (h/sh-wait process))
                    _ (h/wait-for-port "localhost" port
                                       {:timeout 2000})]
                {:port port
                 :process process
                 :result result}))))

(defn stop-scaffold
  []
  (when-let [{:keys [process result]} @+scaffold+]
    (h/sh-kill process)
    @result
    (reset! +scaffold+ nil)))

(defn restart-scaffold
  []
  (stop-scaffold)
  (start-scaffold))

(fact:global
 {:setup [(restart-scaffold)]
  :teardown [(stop-scaffold)]})

^{:refer rt.chromedriver.connection/gen-id :added "4.0"}
(fact "generates an id"
  ^:hidden
  
  (conn/gen-id {})
  => integer?)

^{:refer rt.chromedriver.connection/send :added "4.0"
  :setup [(def +conn+
            (conn/conn-create {:port (:port (start-scaffold))}))]
  :teardown [(conn/conn-close +conn+)]}
(fact "sends a command to the process"
  ^:hidden
  
  @(conn/send +conn+ "Target.detachFromTarget"
              {:targetId  (:target-id +conn+)
               :sessionId (:session-id +conn+)})
  => {}
  

  @(conn/send +conn+ "Target.closeTarget"
              {:targetId (:target-id +conn+)})
  => nil?)

^{:refer rt.chromedriver.connection/ws-url :added "4.0"}
(fact "gets the ws-url"
  ^:hidden
  
  (conn/ws-url {:port (:port (start-scaffold))})
  => string?)

^{:refer rt.chromedriver.connection/conn-process :added "4.0"}
(fact "processes the return call"
  ^:hidden
  
  (conn/conn-process (atom {})
                     (std.json/write {:id 1234
                                      :result [1 2 3 4]}))
  => [nil [1 2 3 4]])

^{:refer rt.chromedriver.connection/conn-attach :added "4.0"
  :setup [(def +conn+
            (conn/conn-create {:attach false
                               :port (:port (start-scaffold))}))]
  :teardown [(conn/conn-close +conn+)]}
(fact "creates a new target and attaches"
  ^:hidden
  
  (conn/conn-attach +conn+)
  => (contains {:session-id string?
                :target-id string?})

  @(conn/send (dissoc +conn+ :session-id)
              "Target.getTargetInfo"
              {})
  => (contains {"targetInfo" map?})
  
  @(conn/send (dissoc +conn+ :session-id)
              "Target.getTargets"
              {})
  => (contains {"targetInfos" vector?}))

^{:refer rt.chromedriver.connection/conn-create-raw :added "4.0"}
(fact "connection function (can error on OSX)")

^{:refer rt.chromedriver.connection/conn-create :added "4.0"
  :setup [(def +conn+
            (conn/conn-create {:port (:port (start-scaffold))
                               :attach :new}))
          (def +conn2+
            (conn/conn-create {:port (:port (start-scaffold))
                               :attach :new}))]}
(fact "creates a devtools connection"
  ^:hidden
  
  ;;
  ;; Tab1
  ;;

  (do (Thread/sleep 100)
      @(conn/send +conn+
                  "Page.navigate"
                  {:url "https://www.bing.com"}
                  3000))
  => (contains {"frameId" string?
                   "loaderId" string?})
  
  (do (Thread/sleep 100)
      @(conn/send (dissoc +conn+ :session-id)
                  "Target.getTargetInfo"))
  => (contains-in {"targetInfo" {"attached" true}})
  
  ;;
  ;; Tab2
  ;;
  
  (def +conn2+
    (conn/conn-create {:port (:port (start-scaffold))
                       :attach :new}))
  (do (Thread/sleep 100)
      @(conn/send (dissoc +conn2+ :session-id)
                  "Target.getTargetInfo"))
  => (contains-in {"targetInfo" {"attached" true}})
  
  (do (Thread/sleep 100)
      @(conn/send +conn2+
                  "Page.navigate"
                  {:url "https://www.baidu.com"}))
  => (contains {"frameId" string?
                "loaderId" string?})

  (do (Thread/sleep 100)
      @(conn/send (dissoc +conn2+ :session-id)
                  "Target.getTargetInfo"))
  => (contains-in {"targetInfo" {"attached" true}}))

^{:refer rt.chromedriver.connection/conn-close :added "4.0"}
(fact "closes the target and disconnects the devtools connection")
