(ns rt.postgres.v2.infer.core-test
    (:use code.test)
    (:require [rt.postgres.v2.infer.core :as core]
              [rt.postgres.v2.infer.types :as types]
              [gwdb.test.scratch :as scratch]))

^{:refer rt.postgres.v2.infer.core/make-openapi :added "0.1"}
(fact "make-openapi generates correct OpenAPI spec for gwdb.test.scratch"
  (let [spec (core/make-openapi 'gwdb.test.scratch (constantly true))
        paths (:paths spec)]

    ;; 1. Check response schema for insert_entry (should be a $ref to Entry)
       (get-in paths ["/rpc/insert_entry" "post" :responses "200" :content "application/json" :schema])
       => {:$ref "#/components/schemas/Entry"}

    ;; 2. Check request body for insert_task_wrapped (should be complex, same as insert_task_raw)
       (let [wrapped-props (get-in paths ["/rpc/insert_task_wrapped" "post" :requestBody :content "application/json" :schema :properties "m" :properties])
             raw-props (get-in paths ["/rpc/insert_task_raw" "post" :requestBody :content "application/json" :schema :properties "m" :properties])]
            wrapped-props => raw-props
            (contains? (set (keys wrapped-props)) "status") => true)

    ;; 3. Check response schema for update_entry_tags (should be a $ref to Entry)
       (get-in paths ["/rpc/update_entry_tags" "post" :responses "200" :content "application/json" :schema])
       => {:$ref "#/components/schemas/Entry"}))
