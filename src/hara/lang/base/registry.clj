(ns hara.lang.base.registry)

(def +registry+
  (atom {[:postgres :default]          'hara.rt.postgres.base.grammar
         [:postgres :jdbc]             'hara.rt.postgres.base.client
         [:postgres :jdbc.client]      'hara.rt.postgres.base.client
         
         [:solidity :default]          'hara.rt.solidity.grammar
	 
         [:bash   :oneshot]            'hara.rt.basic.impl.process-bash
         [:bash   :basic]              'hara.rt.shell
         [:bash   :remote]             'hara.rt.shell
         
         [:lua    :oneshot]            'hara.rt.basic.impl.process-lua
         [:lua    :basic]              'hara.rt.basic.impl.process-lua
         [:lua    :interactive]        'hara.rt.basic.impl.process-lua
         [:lua    :websocket]          'hara.rt.basic.impl.process-lua
         [:lua    :nginx]              'hara.rt.nginx
         [:lua    :nginx.instance]     'hara.rt.nginx
         [:lua    :redis]              'hara.rt.redis
         [:lua.redis :default]         'hara.rt.redis
         [:lua.redis :redis]           'hara.rt.redis
         [:lua.nginx :oneshot]         'hara.rt.basic.impl.process-lua
         [:lua.nginx :basic]           'hara.rt.basic.impl.process-lua
         [:lua.nginx :interactive]     'hara.rt.basic.impl.process-lua
         [:lua.nginx :websocket]       'hara.rt.basic.impl.process-lua
         [:lua.nginx :nginx]           'hara.rt.nginx
         [:lua.nginx :nginx.instance]  'hara.rt.nginx
         [:lua.nginx :remote-port]     'hara.rt.basic.impl.process-lua
         [:lua.nginx :remote-ws]       'hara.rt.basic.impl.process-lua
         [:lua    :remote-port]        'hara.rt.basic.impl.process-lua
         [:lua    :remote-ws]          'hara.rt.basic.impl.process-lua
         
         [:js     :oneshot]            'hara.rt.basic.impl.process-js
         [:js     :basic]              'hara.rt.basic.impl.process-js
         [:js     :interactive]        'hara.rt.basic.impl.process-js
         [:js     :websocket]          'hara.rt.basic.impl.process-js
         [:js     :javafx]             'hara.rt.javafx
         [:js     :graal]              'hara.rt.graal
         [:js     :browser]            'hara.rt.browser
         [:js     :remote-port]        'hara.rt.basic.impl.process-js
         [:js     :remote-ws]          'hara.rt.basic.impl.process-js
         
         [:python :oneshot]            'hara.rt.basic.impl.process-python
         [:python :basic]              'hara.rt.basic.impl.process-python
         [:python :interactive]        'hara.rt.basic.impl.process-python
         [:python :websocket]          'hara.rt.basic.impl.process-python
         [:python :graal]              'hara.rt.graal
         [:python :jep]                'hara.rt.jep
         [:python :libpython]          'hara.rt.libpython
         [:python :remote-port]        'hara.rt.basic.impl.process-python
         [:python :remote-ws]          'hara.rt.basic.impl.process-python

         [:scheme :oneshot]            'hara.rt.basic.impl.process-scheme
         [:scheme :basic]              'hara.rt.basic.impl.process-scheme

         [:elisp  :oneshot]            'hara.rt.basic.impl.process-elisp
         [:elisp  :basic]              'hara.rt.basic.impl.process-elisp

         [:ruby   :oneshot]            'hara.rt.basic.impl.process-ruby
         [:ruby   :basic]              'hara.rt.basic.impl.process-ruby

         [:perl   :oneshot]            'hara.rt.basic.impl-annex.process-perl
         [:perl   :basic]              'hara.rt.basic.impl-annex.process-perl

         [:php    :oneshot]            'hara.rt.basic.impl-annex.process-php
         [:php    :basic]              'hara.rt.basic.impl-annex.process-php
         
         [:r      :oneshot]            'hara.rt.basic.impl-annex.process-r
         [:r      :basic]              'hara.rt.basic.impl-annex.process-r

         [:julia  :oneshot]            'hara.rt.basic.impl-annex.process-julia
         [:julia  :basic]              'hara.rt.basic.impl-annex.process-julia

         [:erlang :oneshot]            'hara.rt.basic.impl-annex.process-erlang
         [:erlang :basic]              'hara.rt.basic.impl-annex.process-erlang
         
         [:haskell :twostep]           'hara.rt.basic.impl-annex.process-haskell
         [:lean    :twostep]           'hara.rt.basic.impl-annex.process-lean
         [:ocaml   :twostep]           'hara.rt.basic.impl-annex.process-ocaml
         
         [:rust   :twostep]            'hara.rt.basic.impl-annex.process-rust
         
         [:c      :jocl]               'hara.rt.jocl
         [:c      :oneshot]            'hara.rt.basic.impl.process-c
         [:c      :twostep]            'hara.rt.basic.impl.process-c

         [:dart   :twostep]            'hara.rt.basic.impl.process-dart
         [:go     :twostep]            'hara.rt.basic.impl.process-go
	 
         [:xtalk  :oneshot]            'hara.rt.basic.impl.process-xtalk}))

(def +book-registry+
  (atom {[:xtalk    :default]          {:ns 'hara.lang.model.spec-xtalk
                                        :book '+book+}

         [:bash     :default]          {:ns 'hara.lang.model.spec-bash
                                        :book '+book+
                                        :parent :xtalk}
         [:c        :default]          {:ns 'hara.lang.model.spec-c
                                        :book '+book+
                                        :parent :xtalk}
         [:dart     :default]          {:ns 'hara.lang.model.spec-dart
                                        :book '+book+
                                        :parent :xtalk}
         [:glsl     :default]          {:ns 'hara.lang.model.spec-glsl
                                        :book '+book+
                                        :parent :xtalk}
         [:go       :default]          {:ns 'hara.lang.model.spec-go
                                        :book '+book+
                                        :parent :xtalk}
         [:js       :default]          {:ns 'hara.lang.model.spec-js
                                        :book '+book+
                                        :parent :xtalk}
         [:llvm     :default]          {:ns 'hara.lang.model.spec-llvm
                                        :book '+book+
                                        :parent :xtalk}
         [:lua      :default]          {:ns 'hara.lang.model.spec-lua
                                        :book '+book+
                                        :parent :xtalk}
         [:lua.redis :default]         {:ns 'hara.lang.model.spec-lua.variant-redis
                                        :book '+book+
                                        :parent :lua}
         [:lua.nginx :default]         {:ns 'hara.lang.model.spec-lua.variant-nginx
                                        :book '+book+
                                        :parent :lua}
         [:python   :default]          {:ns 'hara.lang.model.spec-python
                                        :book '+book+
                                        :parent :xtalk}
         [:elisp    :default]          {:ns 'hara.lang.model.spec-elisp
                                        :book '+book+
                                        :parent :xtalk}
         [:scheme   :default]          {:ns 'hara.lang.model.spec-scheme
                                        :book '+book+
                                        :parent :xtalk}
         
         [:postgres :default]          {:ns 'hara.rt.postgres.base.grammar
                                        :book '+book+}
         [:solidity :default]          {:ns 'hara.rt.solidity.grammar
                                        :book '+book+}

         [:circom   :default]          {:ns 'hara.lang.model-annex.spec-circom
                                        :book '+book+
                                        :parent :xtalk}
         [:erlang   :default]          {:ns 'hara.lang.model-annex.spec-erlang
                                        :book '+book+
                                        :parent :xtalk}
         [:fortran  :default]          {:ns 'hara.lang.model-annex.spec-fortran
                                        :book '+book+
                                        :parent :xtalk}
         [:haskell  :default]          {:ns 'hara.lang.model-annex.spec-haskell
                                        :book '+book+
                                        :parent :xtalk}
         [:lean     :default]          {:ns 'hara.lang.model-annex.spec-lean
                                        :book '+book+
                                        :parent :xtalk}
         [:ocaml    :default]          {:ns 'hara.lang.model-annex.spec-ocaml
                                        :book '+book+
                                        :parent :xtalk}
         [:jq       :default]          {:ns 'hara.lang.model-annex.spec-jq
                                        :book '+book+
                                        :parent :xtalk}
         [:julia    :default]          {:ns 'hara.lang.model-annex.spec-julia
                                        :book '+book+
                                        :parent :xtalk}
         [:perl     :default]          {:ns 'hara.lang.model-annex.spec-perl
                                        :book '+book+
                                        :parent :xtalk}
         [:php      :default]          {:ns 'hara.lang.model-annex.spec-php
                                        :book '+book+
                                        :parent :xtalk}
         [:r        :default]          {:ns 'hara.lang.model-annex.spec-r
                                        :book '+book+
                                        :parent :xtalk}
         [:ruby     :default]          {:ns 'hara.lang.model-annex.spec-ruby
                                        :book '+book+
                                        :parent :xtalk}
         [:rust     :default]          {:ns 'hara.lang.model-annex.spec-rust
                                        :book '+book+
                                        :parent :xtalk}
         [:verilog  :default]          {:ns 'hara.lang.model-annex.spec-verilog
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
