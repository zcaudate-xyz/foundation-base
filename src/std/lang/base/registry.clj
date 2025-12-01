(ns std.lang.base.registry)

(def +registry+
  (atom {[:postgres :default]          'rt.postgres.grammar
         [:postgres :jdbc]             'rt.postgres.client
         [:postgres :jdbc.client]      'rt.postgres.client
         
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

         [:perl   :oneshot]            'rt.basic.impl.process-perl
         [:perl   :basic]              'rt.basic.impl.process-perl

         [:php    :oneshot]            'rt.basic.impl.process-php
         [:php    :basic]              'rt.basic.impl.process-php
         
         [:r      :oneshot]            'rt.basic.impl.process-r
         [:r      :basic]              'rt.basic.impl.process-r

         [:rust   :twostep]            'rt.basic.impl.process-rust
         
         [:c      :jocl]               'rt.jocl
         [:c      :oneshot]            'rt.basic.impl.process-c
         [:c      :twostep]            'rt.basic.impl.process-c
	 
         [:xtalk  :oneshot]            'rt.basic.impl.process-xtalk}))
