(ns std.lang.base.registry)

(def +registry+
  (atom {[:postgres :default]          'rt.postgres.base.grammar
         [:postgres :jdbc]             'rt.postgres.base.client
         [:postgres :jdbc.client]      'rt.postgres.base.client
         
         [:redis    :default]          'rt.redis
         [:redis    :redis]            'rt.redis
         
         [:solidity :default]          'rt.solidity.grammar
	 
	 [:bash   :oneshot]            'rt.basic.impl.process-bash
	 [:bash   :basic]              'rt.shell
	 [:bash   :remote]             'rt.shell
         
         [:lua    :oneshot]            'rt.basic.impl.process-lua
         [:lua    :basic]              'rt.basic.impl.process-lua
         [:lua    :interactive]        'rt.basic.impl.process-lua
         [:lua    :websocket]          'rt.basic.impl.process-lua
         [:lua    :nginx]              'rt.nginx
         [:lua    :nginx.instance]     'rt.nginx
         [:lua    :redis]              'rt.redis
         [:lua    :remote-port]        'rt.basic.impl.process-lua
         [:lua    :remote-ws]          'rt.basic.impl.process-lua
         
         [:js     :oneshot]            'rt.basic.impl.process-js
         [:js     :basic]              'rt.basic.impl.process-js
         [:js     :interactive]        'rt.basic.impl.process-js
         [:js     :websocket]          'rt.basic.impl.process-js
         [:js     :javafx]             'rt.javafx
         [:js     :graal]              'rt.graal
         [:js     :browser]            'rt.browser
         [:js     :remote-port]        'rt.basic.impl.process-js
         [:js     :remote-ws]          'rt.basic.impl.process-js
         
         [:python :oneshot]            'rt.basic.impl.process-python
         [:python :basic]              'rt.basic.impl.process-python
         [:python :interactive]        'rt.basic.impl.process-python
         [:python :websocket]          'rt.basic.impl.process-python
         [:python :graal]              'rt.graal
         [:python :jep]                'rt.jep
         [:python :libpython]          'rt.libpython
         [:python :remote-port]        'rt.basic.impl.process-python
         [:python :remote-ws]          'rt.basic.impl.process-python

         [:ruby   :oneshot]            'rt.basic.impl.process-ruby
         [:ruby   :basic]              'rt.basic.impl.process-ruby

         [:perl   :oneshot]            'rt.basic.impl-annex.process-perl
         [:perl   :basic]              'rt.basic.impl-annex.process-perl

         [:php    :oneshot]            'rt.basic.impl-annex.process-php
         [:php    :basic]              'rt.basic.impl-annex.process-php
         
         [:r      :oneshot]            'rt.basic.impl-annex.process-r
         [:r      :basic]              'rt.basic.impl-annex.process-r

         [:julia  :oneshot]            'rt.basic.impl-annex.process-julia
         [:julia  :basic]              'rt.basic.impl-annex.process-julia

         [:erlang :oneshot]            'rt.basic.impl-annex.process-erlang
         [:erlang :basic]              'rt.basic.impl-annex.process-erlang

         [:rust   :twostep]            'rt.basic.impl-annex.process-rust
         
         [:c      :jocl]               'rt.jocl
	 [:c      :oneshot]            'rt.basic.impl.process-c
         [:c      :twostep]            'rt.basic.impl.process-c

         [:dart   :twostep]            'rt.basic.impl.process-dart
         [:go     :twostep]            'rt.basic.impl.process-go
	 
         [:xtalk  :oneshot]            'rt.basic.impl.process-xtalk}))

(def +book-registry+
  (atom {[:xtalk    :default]          {:ns 'std.lang.model.spec-xtalk
                                        :book '+book+}

         [:bash     :default]          {:ns 'std.lang.model.spec-bash
                                        :book '+book+
                                        :parent :xtalk}
         [:c        :default]          {:ns 'std.lang.model.spec-c
                                        :book '+book+
                                        :parent :xtalk}
         [:dart     :default]          {:ns 'std.lang.model.spec-dart
                                        :book '+book+
                                        :parent :xtalk}
         [:glsl     :default]          {:ns 'std.lang.model.spec-glsl
                                        :book '+book+
                                        :parent :xtalk}
         [:go       :default]          {:ns 'std.lang.model.spec-go
                                        :book '+book+
                                        :parent :xtalk}
         [:js       :default]          {:ns 'std.lang.model.spec-js
                                        :book '+book+
                                        :parent :xtalk}
         [:llvm     :default]          {:ns 'std.lang.model.spec-llvm
                                        :book '+book+
                                        :parent :xtalk}
         [:lua      :default]          {:ns 'std.lang.model.spec-lua
                                        :book '+book+
                                        :parent :xtalk}
         [:redis    :default]          {:ns 'std.lang.model.spec-lua
                                        :book '+book-redis+
                                        :parent :lua}
         [:python   :default]          {:ns 'std.lang.model.spec-python
                                        :book '+book+
                                        :parent :xtalk}
         [:scheme   :default]          {:ns 'std.lang.model.spec-scheme
                                        :book '+book+
                                        :parent :xtalk}

         [:postgres :default]          {:ns 'rt.postgres.base.grammar
                                        :book '+book+}
         [:solidity :default]          {:ns 'rt.solidity.grammar
                                        :book '+book+}

         [:circom   :default]          {:ns 'std.lang.model-annex.spec-circom
                                        :book '+book+
                                        :parent :xtalk}
         [:erlang   :default]          {:ns 'std.lang.model-annex.spec-erlang
                                        :book '+book+
                                        :parent :xtalk}
         [:fortran  :default]          {:ns 'std.lang.model-annex.spec-fortran
                                        :book '+book+
                                        :parent :xtalk}
         [:haskell  :default]          {:ns 'std.lang.model-annex.spec-haskell
                                        :book '+book+
                                        :parent :xtalk}
         [:jq       :default]          {:ns 'std.lang.model-annex.spec-jq
                                        :book '+book+
                                        :parent :xtalk}
         [:julia    :default]          {:ns 'std.lang.model-annex.spec-julia
                                        :book '+book+
                                        :parent :xtalk}
         [:perl     :default]          {:ns 'std.lang.model-annex.spec-perl
                                        :book '+book+
                                        :parent :xtalk}
         [:php      :default]          {:ns 'std.lang.model-annex.spec-php
                                        :book '+book+
                                        :parent :xtalk}
         [:r        :default]          {:ns 'std.lang.model-annex.spec-r
                                        :book '+book+
                                        :parent :xtalk}
         [:ruby     :default]          {:ns 'std.lang.model-annex.spec-ruby
                                        :book '+book+
                                        :parent :xtalk}
         [:rust     :default]          {:ns 'std.lang.model-annex.spec-rust
                                        :book '+book+
                                        :parent :xtalk}
         [:verilog  :default]          {:ns 'std.lang.model-annex.spec-verilog
                                        :book '+book+
                                        :parent :xtalk}}))

(defn registry-book-list
  "lists all registered books"
  {:added "4.1"}
  ([] (keys @+book-registry+)))

(defn registry-book-ns
  "gets the namespace for a book registry entry"
  {:added "4.1"}
  ([lang] (registry-book-ns lang :default))
  ([lang key]
   (:ns (get @+book-registry+ [lang key]))))

(defn registry-book-info
  "gets the full book registry entry"
  {:added "4.1"}
  ([lang] (registry-book-info lang :default))
  ([lang key]
   (get @+book-registry+ [lang key])))

(defn registry-book
  "loads the book namespace and returns the book"
  {:added "4.1"}
  ([lang] (registry-book lang :default))
  ([lang key]
   (when-let [{:keys [ns book]} (registry-book-info lang key)]
     (clojure.core/require ns)
     (some-> (ns-resolve (the-ns ns) book)
             var-get))))
