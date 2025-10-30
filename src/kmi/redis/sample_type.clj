(ns kmi.redis.sample-type
  (:require [kmi.redis.compile :as r]
            [std.lang :as l])
  (:refer-clojure :exclude [>]))

(l/script :lua
  {:macro-only true})

(def <BOOK>            {:name  :book
                        :type  :string})

(def <META>            {:type      :map
                        :fields    {:__type__   #{"TYPE/BOOK"}
                                    :__status__ #{"BOOK/ACTIVE" "BOOK/INACTIVE"}}
                        :subscribe ":EVENTS"})

(def <SIDE>            {:type :enum :data #{:ask :bid}})

(def <DIFF_DATA>       {:type    :map
                        :fields  {:frame       :integer
                                  :added       :set
                                  :removed     :set
                                  :modified    :map}})

(def <STAGE>           {:name  :stage
                        :type  #{"I"  ;; Initial
                                 "R"  ;; Registration
                                 "T"  ;; Trade
                                 "X"  ;; Closed
                                 }})
(def <FRAME>           {:name  :frame
                        :type  :integer})
(def <POSITION>        {:name  :position
                        :type  :integer})
(def <VOLUME>          {:name  :volume
                        :type  :integer})
(def <AMOUNT>          {:name  :amount
                        :type  :integer})
(def <ORDER_ID>        {:name  :order-id
                        :type  :string})

(def <ORDER_DATA>      {:type    :map
                        :fields  {:id           <ORDER_ID>
                                  :side         #{:ask :bid}
                                  :position     <POSITION>
                                  :frame        <FRAME>
                                  :amount       <AMOUNT>
                                  :meta         :string}})

(def <TX_DATA>        {:type    :map
                       :fields  {:frame    <FRAME>
                                 :sequence :integer
                                 :ask-id   <ORDER_ID>
                                 :ask-meta :string
                                 :bid-id   <ORDER_ID>
                                 :bid-meta :string
                                 :strike-amount    <AMOUNT>
                                 :strike-position  <POSITION>}})

(def <FRAME_DATA>     {:type     :map
                       :fields   {:type         #{:place :cancel :modify}
                                  :frame        <FRAME>
                                  :order        :json}})

(def <ERROR_DATA>    {:type     :map
                      :fields   {:time        :integer
                                 :command     :string
                                 :data        :string}})

(def <EVENTS>        {:type     :map
                      :impl     :key
                      :fields   {:_:error    {:type :stream
                                              :element <ERROR_DATA>}
                                 :_:frame    {:type :stream
                                              :element <FRAME_DATA>}
                                 :_:tx       {:type :stream
                                              :element <TX_DATA>}
                                 :_:history  {:type :stream
                                              :element {:start  <POSITION>
                                                        :end    <POSITION>
                                                        :distribution :json
                                                        :volume <VOLUME>}}
                                 :_:volume   {:type :stream
                                              :element {:added   <VOLUME>
                                                        :removed <VOLUME>}}
                                 :_:simulate {:type :stream
                                              :element {:signal <POSITION>}}}})

(def <HISTORY>      {:type   :map
                     :impl   :key
                     :fields {:time     :integer
                              :number   :integer
                              :start_frame       <FRAME>
                              :final_frame       <FRAME>
                              :start_position    <POSITION>
                              :final_position    <POSITION>
                              :distribution {:type :map
					     :impl :hash
                                             :element [<POSITION> <VOLUME>]}
                              :volume   <VOLUME>}})

(def <ORDER_VOLUME> {:type   :map
                     :impl   :key
                     :fields {:time     :integer
                              :number   :integer
                              :start_frame       <FRAME>
                              :final_frame       <FRAME>
                              :added   <VOLUME>
                              :removed <VOLUME>}})

(def <DIFF>          {:type :map
                      :impl :hash
                      :element [<FRAME> <DIFF_DATA>]})                  

(def <ORDER>         {:type     :map
                      :impl     :key
                      :element  [<ORDER_ID>   (-> (assoc <ORDER_DATA> :impl :hash)
                                                  (update-in
                                                   [:fields] assoc
                                                   :unfilled         <AMOUNT>
                                                   :frame-updated    <FRAME>
                                                   :frame-created    <FRAME>
                                                   :frame-completed  <FRAME>
                                                   :priority         <FRAME>))]})

(def <ACTIVE_LU>    {:type :zset
                     :element [<ORDER_ID> <POSITION>]})

(def <ACTIVE>       {:type    :map
                     :impl    :key
                     :element [<SIDE> <ACTIVE_LU>]})

(def <PRIORITY_LU>  {:type :map
                     :impl :key
                     :element [<POSITION>  {:type :zset
                                            :element [<ORDER_ID> <FRAME>]}]})

(def <PRIORITY>     {:type    :map
                     :impl    :key
                     :element  [<SIDE> <PRIORITY_LU>]})

(def <UNFILLED_LU>  {:type :map
                     :impl :hash
                     :element [<POSITION> {:type :integer :name :volume-unfilled}]})

(def <UNFILLED>     {:type    :map
                     :impl    :key
                     :element [<SIDE> <UNFILLED_LU>]})

(def <UNFILLED_POSITION>  {:type    :map
                           :impl    :key
                           :element [<SIDE> {:type :zset
                                             :element [<POSITION> <POSITION>]}]})

(def <COMPLETED>    {:type :zset
                     :element [<ORDER_ID> <FRAME>]})


(def <SIMULATE>     {:type :map
                     :impl :key
                     :fields  {:position  <POSITION>
                               :frame     <FRAME>
                               :risk      {:type :integer}}})

(def <SPEC>     {:type   :map
                 :impl   :key
                 :fields {:__meta__    (assoc <META> :impl :hash)
                          :frame       <FRAME>
                          :stage       <STAGE>
                          
                          :_:order     <ORDER>
                          :events      <EVENTS>
                          :diff        <DIFF>

                          :active      <ACTIVE>                          
                          :priority    <PRIORITY>
                          :unfilled    <UNFILLED>
                          :position    <UNFILLED_POSITION>
                          :completed   <COMPLETED>
                          :*:history  <HISTORY>
                          :*:volume   <ORDER_VOLUME>
                          :*:simulate <SIMULATE>}})

(def X> (partial r/compile <SPEC>))

(defmacro.lua >
  "shortcut for data access"
  {:added "4.0"}
  ([& blocks]
   (apply r/compile <SPEC> blocks)))

;;
;; Public Meta
;;

(def <PUBLIC-META>     {:type      :map
                        :fields    {:__type__   #{"TYPE/PUBLIC"}}})

(def <PUBLIC-POSITION> {:type    :map
                        :impl    :hash
                        :element [<BOOK> <POSITION>]})

(def <PUBLIC-POSITION-UPDATED> {:type    :map
                                :impl    :hash
                                :element [<BOOK> :integer]})

(def <PUBLIC-STATUS>           {:type    :map
                                :impl    :hash
                                :element [<BOOK> :string]})

(def <PUBLIC-STATUS-FRAME>     {:type    :map
                                :impl    :hash
                                :element [<BOOK> :integer]})


(def <PUBLIC-PRIORITY>           {:type    :map
                                  :impl    :hash
                                  :element [<BOOK> :string]})

(def <PUBLIC-PRIORITY-FRAME>     {:type    :map
                                  :impl    :hash
                                  :element [<BOOK> :integer]})


(def <PUBLIC-ACTIVE>             {:type    :map
                                  :impl    :hash
                                  :element [<BOOK> :string]})

(def <PUBLIC-ACTIVE-FRAME>       {:type    :map
                                  :impl    :hash
                                  :element [<BOOK> :integer]})

(def <PUBLIC-SPEC>     {:type   :map
                        :impl   :key
                        :fields {:__meta__    (assoc <PUBLIC-META> :impl :hash)
                                 :position               <PUBLIC-POSITION>
                                 :position-frame         <PUBLIC-STATUS-FRAME>
                                 :position-updated       <PUBLIC-POSITION-UPDATED>
                                 :status                 <PUBLIC-STATUS>
                                 :status-frame           <PUBLIC-STATUS-FRAME>
                                 :active                 <PUBLIC-ACTIVE>
                                 :active-frame           <PUBLIC-ACTIVE-FRAME>
                                 :priority               <PUBLIC-PRIORITY>
                                 :priority-frame         <PUBLIC-PRIORITY-FRAME>}})

(defmacro.lua P>
  "shortcut for data access"
  {:added "4.0"}
  ([& blocks]
   (apply r/compile <PUBLIC-SPEC> blocks)))
