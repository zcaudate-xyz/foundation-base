(ns lib.redis.impl.generator-test
  (:use code.test)
  (:require [lib.redis.impl.common :as common]
            [lib.redis.impl.generator :refer :all]
            [lib.redis.impl.reference :as ref]))

^{:refer lib.redis.impl.generator/expand-process :added "3.0"}
(fact "expands the argument for the `:process` keys"
  ^:hidden

  (expand-process {:type :data})
  => `common/process:data)

^{:refer lib.redis.impl.generator/expand-prelim :added "3.0"}
(fact "expands the initial argument"
  ^:hidden

  (expand-prelim {:type :data})
  => '{:type :data,
       :enum nil,
       :name nil,
       :process lib.redis.impl.common/process:data,
       :command nil,
       :multiple false, :sym data,
       :optional nil,
       :display nil})

^{:refer lib.redis.impl.generator/expand-enum :added "3.0"}
(fact "expands the enum structure"
  ^:hidden

  (expand-enum {:enum ["async"]})
  => '{:type :flag, :sym async, :name "async"}

  (expand-enum {:enum ["no" "yes"] :name "wait"})
  => '{:type :enum, :sym wait, :name "wait", :values #{:yes :no}}

  (expand-enum {:enum ["no" "yes"] :command "WAIT"})
  => '{:type :command, :command "WAIT", :sym wait, :name "wait", :values #{:yes :no}})

^{:refer lib.redis.impl.generator/expand-argument :added "3.0"}
(fact "expands an argument"
  ^:hidden

  (expand-argument {:type :data :multiple true})
  => (contains {:type :data, :multiple true, :sym 'datas
                :process `common/process:data-multi})

  (expand-argument {:type :key})
  => (contains {:type :key, :multiple false, :sym 'key
                :process `common/process:key}))

^{:refer lib.redis.impl.generator/command-redefs :added "3.0"}
(fact "creates redefinitions for let bindings"
  ^:hidden

  (command-redefs {:sym 'key
                   :process `common/process:key})
  => `[~'key (common/process:key ~'key ~'opts)])

^{:refer lib.redis.impl.generator/command-step :added "3.0"}
(fact "creates a command step"
  ^:hidden

  (command-step {:type :key :sym 'key})
  => '[key (clojure.core/conj key)]

  (command-step {:type :enum :values #{:yes :no}
                 :sym 'async})
  => `[~'async (conj (if (get #{:yes :no} ~'async)
                       (std.string/upper-case (std.lib/strn ~'async))
                       (throw (ex-info "Invalid input" {:arg (quote ~'async),
                                                        :options #{:yes :no}}))))])

^{:refer lib.redis.impl.generator/command-tmpl :added "3.0"}
(fact "creates a command function from data"
  ^:hidden

  (command-tmpl (command-parse (ref/command :ttl)
                               {}))
  => '(clojure.core/defn in:ttl
        #:redis{:return (quote :data), :multiple false, :optionals false}
        ([key] (in:ttl key {}))
        ([key opts]
         (clojure.core/let [key (lib.redis.impl.common/process:key key opts)]
           (clojure.core/-> ["TTL"] (clojure.core/conj key))))))

^{:refer lib.redis.impl.generator/command-type :added "3.0"}
(fact "normalizes a command type"
  ^:hidden

  (command-type "key")
  => :key

  (command-type "string")
  => :string)

^{:refer lib.redis.impl.generator/optional-tmpl :added "3.0"}
(fact "creates the optional form from data"
  ^:hidden

  (-> (command-parse (ref/command :client-tracking)
                     {})
      (optional-tmpl))
  => '(clojure.core/defn optional:client-tracking
        ([{:as optional, :keys [client-id bcast optin optout noloop]} _]
         (clojure.core/cond-> []
           client-id (clojure.core/conj "REDIRECT" client-id)
           bcast (clojure.core/conj "BCAST")
           optin (clojure.core/conj "OPTIN")
           optout (clojure.core/conj "OPTOUT")
           noloop (clojure.core/conj "NOLOOP")))))

^{:refer lib.redis.impl.generator/collect-optional :added "3.0"}
(fact "collect all optional variables"
  (collect-optional [{:optional true}] (volatile! nil) :key)
  => (fn [x] (and (seq? x)
                  (= :optional (:type (first x)))
                  (:multiple (first x)))))

^{:refer lib.redis.impl.generator/command-arguments :added "3.0"}
(fact "function for command arguments"
  (command-arguments [{:type "key"}] :key {} (volatile! nil) (volatile! nil))
  => (fn [x] (and (seq? x)
                  (= :key (:type (first x)))
                  (= 'key (:sym (first x))))))

^{:refer lib.redis.impl.generator/command-parse :added "3.0"}
(fact "parse params for a given skeleton"
  (command-parse (ref/command :ttl) {})
  => (contains {:id :ttl :return :data}))

^{:refer lib.redis.impl.generator/command-params :added "3.0"}
(fact "create command params for form generation"

  (command-params :ttl)
  => '{:id :ttl,
       :arguments ({:enum nil, :name "key",
                    :process lib.redis.impl.common/process:key,
                    :command nil, :type :key, :multiple false, :sym key,
                    :optional nil, :display nil}),
       :prefix ("TTL"), :return :data, :optionals false, :multiple false}

  (command-params [:set {}])
  => map?)

^{:refer lib.redis.impl.generator/command-form :added "3.0"}
(fact "create the command form"
  ^:hidden

  (command-form :ttl)
  => '(clojure.core/defn in:ttl
        #:redis{:return (quote :data), :multiple false, :optionals false}
        ([key] (in:ttl key {}))
        ([key opts]
         (clojure.core/let [key (lib.redis.impl.common/process:key key opts)]
           (clojure.core/-> ["TTL"] (clojure.core/conj key))))))

^{:refer lib.redis.impl.generator/select-commands :added "3.0"}
(fact "select commands and create a list of params"
  ^:hidden

  (select-commands {:group :list})
  => seq?

  (select-commands {:group [:set]})
  => seq?

  (select-commands {:include [:ttl]})
  => seq?)
