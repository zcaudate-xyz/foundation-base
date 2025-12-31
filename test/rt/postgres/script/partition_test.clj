(ns rt.postgres.script.partition-test
  (:require [rt.postgres.script.partition :as partition]
            [code.test :refer :all]
            [std.string :as str]))

(fact "defpartitions.pg generation"
  (with-redefs [partition/get-app-schema (constantly "szn_type")]
    (partition/partition-fn 'RevPartitions
                            '[-/Rev]
                            [{:use :class
                              :in ["user" "org"]}
                             {:use :class-table
                              :in ["ChatChannel" "Feed"]}]))
  => (str/join "\n"
               ["-- 1. Create Router Tables for 'user' (Partitioned by class_table)"
                "CREATE TABLE IF NOT EXISTS \"szn_type\".\"Rev_user\" "
                "    PARTITION OF \"szn_type\".\"Rev\" "
                "    FOR VALUES IN ('user') "
                "    PARTITION BY LIST (\"class_table\");"
                ""
                "-- 2. Create Storage for ChatChannel"
                "CREATE TABLE IF NOT EXISTS \"szn_type\".\"Rev_ChatChannel_user\" "
                "    PARTITION OF \"szn_type\".\"Rev_user\" "
                "    FOR VALUES IN ('ChatChannel');"
                ""
                "-- 3. Create Storage for Feed"
                "CREATE TABLE IF NOT EXISTS \"szn_type\".\"Rev_Feed_user\" "
                "    PARTITION OF \"szn_type\".\"Rev_user\" "
                "    FOR VALUES IN ('Feed');"
                ""
                "-- 1. Create Router Tables for 'org' (Partitioned by class_table)"
                "CREATE TABLE IF NOT EXISTS \"szn_type\".\"Rev_org\" "
                "    PARTITION OF \"szn_type\".\"Rev\" "
                "    FOR VALUES IN ('org') "
                "    PARTITION BY LIST (\"class_table\");"
                ""
                "-- 2. Create Storage for ChatChannel"
                "CREATE TABLE IF NOT EXISTS \"szn_type\".\"Rev_ChatChannel_org\" "
                "    PARTITION OF \"szn_type\".\"Rev_org\" "
                "    FOR VALUES IN ('ChatChannel');"
                ""
                "-- 3. Create Storage for Feed"
                "CREATE TABLE IF NOT EXISTS \"szn_type\".\"Rev_Feed_org\" "
                "    PARTITION OF \"szn_type\".\"Rev_org\" "
                "    FOR VALUES IN ('Feed');"]))

(fact "defpartitions.pg generation - 4 levels"
  (with-redefs [partition/get-app-schema (constantly "public")]
    (partition/partition-fn 'DeepPartitions
                            'Global
                            [{:use :region
                              :in ["NA"]}
                             {:use :country
                              :in ["US"]}
                             {:use :state
                              :in ["CA"]}
                             {:use :city
                              :in ["SF"]}]))
  => (str/join "\n"
               ["-- 1. Create Router Tables for 'NA' (Partitioned by country)"
                "CREATE TABLE IF NOT EXISTS \"public\".\"Global_NA\" "
                "    PARTITION OF \"public\".\"Global\" "
                "    FOR VALUES IN ('NA') "
                "    PARTITION BY LIST (\"country\");"
                ""
                "-- 2. Create Router Tables for 'US' (Partitioned by state)"
                "CREATE TABLE IF NOT EXISTS \"public\".\"Global_US_NA\" "
                "    PARTITION OF \"public\".\"Global_NA\" "
                "    FOR VALUES IN ('US') "
                "    PARTITION BY LIST (\"state\");"
                ""
                "-- 3. Create Router Tables for 'CA' (Partitioned by city)"
                "CREATE TABLE IF NOT EXISTS \"public\".\"Global_CA_US_NA\" "
                "    PARTITION OF \"public\".\"Global_US_NA\" "
                "    FOR VALUES IN ('CA') "
                "    PARTITION BY LIST (\"city\");"
                ""
                "-- 4. Create Storage for SF"
                "CREATE TABLE IF NOT EXISTS \"public\".\"Global_SF_CA_US_NA\" "
                "    PARTITION OF \"public\".\"Global_CA_US_NA\" "
                "    FOR VALUES IN ('SF');"]))
