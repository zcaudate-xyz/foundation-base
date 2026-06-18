(ns code.mcp.tool.common-test
  (:use code.test)
  (:require [code.mcp.tool.common :refer :all]))

^{:refer code.mcp.tool.common/read-edn :added "4.1"}
(fact "reads an edn string into a value"
  (read-edn "{:a 1 :b [1 2 3]}")
  => {:a 1 :b [1 2 3]})

^{:refer code.mcp.tool.common/read-edn :added "4.1"}
(fact "returns default when input is nil"
  (read-edn nil "default")
  => "default"

  (read-edn nil)
  => nil)

^{:refer code.mcp.tool.common/merge-print-options :added "4.1"}
(fact "merges provided options over defaults with shallow merge"
  (merge-print-options {:print {:result true}})
  => {:print {:result true}}

  (merge-print-options {:print {:function false
                                :summary false
                                :result true
                                :item false}})
  => {:print {:function false
              :summary false
              :result true
              :item false}}

  (merge-print-options nil)
  => default-print-options)

^{:refer code.mcp.tool.common/render-result :added "4.1"}
(fact "returns strings unchanged"
  (render-result "hello")
  => "hello")

^{:refer code.mcp.tool.common/render-result :added "4.1"}
(fact "prints non-strings with prn"
  (render-result {:a 1})
  => "{:a 1}\n")

^{:refer code.mcp.tool.common/response :added "4.1"}
(fact "wraps a raw value in an mcp text response"
  (response "hello")
  => {:content [{:type "text" :text "hello"}]
      :isError false})

^{:refer code.mcp.tool.common/response :added "4.1"}
(fact "marks response as error when raw result is a map with :error"
  (response {:error "boom"})
  => {:content [{:type "text" :text "{:error \"boom\"}\n"}]
      :isError true})

^{:refer code.mcp.tool.common/error-response :added "4.1"}
(fact "creates an error response with the given message"
  (error-response "something went wrong")
  => {:content [{:type "text" :text "something went wrong"}]
      :isError true})