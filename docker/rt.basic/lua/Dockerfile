FROM ubuntu:24.04

ARG DEBIAN_FRONTEND=noninteractive

WORKDIR /tmp

ENV OPENRESTY_VERSION=1.27.1.1
ENV LUAROCKS_VERSION=3.12.2
ENV NCHAN_VERSION=1.3.7

RUN apt-get update \
 && apt-get install -y \
      build-essential \
      ca-certificates \
      curl \
      gettext-base \
      git \
      libhiredis-dev \
      libjemalloc-dev \
      libpcre3 \
      libpcre3-dev \
      libreadline-dev \
      libsqlite3-dev \
      libssl-dev \
      perl \
      unzip \
      wget \
      zlib1g-dev \
 && rm -rf /var/lib/apt/lists/*

RUN wget https://openresty.org/download/openresty-${OPENRESTY_VERSION}.tar.gz \
 && tar -xf openresty-${OPENRESTY_VERSION}.tar.gz \
 && wget https://github.com/slact/nchan/archive/refs/tags/v${NCHAN_VERSION}.tar.gz \
 && tar -xf v${NCHAN_VERSION}.tar.gz \
 && wget https://luarocks.github.io/luarocks/releases/luarocks-${LUAROCKS_VERSION}.tar.gz \
 && tar -xf luarocks-${LUAROCKS_VERSION}.tar.gz

RUN cd openresty-${OPENRESTY_VERSION} \
 && ./configure \
      --add-module=../nchan-${NCHAN_VERSION} \
      --with-pcre-jit \
      --with-ipv6 \
      --prefix=/opt/openresty \
 && make -j"$(nproc)" \
 && make install

RUN cd luarocks-${LUAROCKS_VERSION} \
 && ./configure \
      --prefix=/opt/openresty/luajit \
      --with-lua=/opt/openresty/luajit \
      --lua-suffix=jit \
      --with-lua-include=/opt/openresty/luajit/include/luajit-2.1 \
 && make \
 && make install

ENV PATH=/opt/openresty/bin:/opt/openresty/luajit/bin:/opt/openresty/nginx/sbin:$PATH

RUN /opt/openresty/luajit/bin/luarocks install luasocket \
 && /opt/openresty/luajit/bin/luarocks install lustache \
 && /opt/openresty/luajit/bin/luarocks install lua-cjson \
 && /opt/openresty/luajit/bin/luarocks install lua-crypt \
 && /opt/openresty/luajit/bin/luarocks install lua-resty-openssl \
 && /opt/openresty/luajit/bin/luarocks install lua-resty-http \
 && /opt/openresty/luajit/bin/luarocks install lua-resty-uuid \
 && /opt/openresty/luajit/bin/luarocks install lua-resty-mail \
 && curl -fsSLO https://luarocks.org/manifests/dougcurrie/lsqlite3-0.9.6-1.rockspec \
 && /opt/openresty/luajit/bin/luarocks install lsqlite3-0.9.6-1.rockspec \
 && /opt/openresty/luajit/bin/luarocks install multipart \
 && /opt/openresty/luajit/bin/luarocks install pgmoon \
 && /opt/openresty/luajit/bin/luarocks install luaposix

RUN luajit -e "require('cjson'); require('socket'); print('lua-basic-ready')"
