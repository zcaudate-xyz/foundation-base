FROM debian:buster-slim

# Debian buster is EOL; use the archive mirrors so apt still works.
RUN sed -i 's|http://deb.debian.org/debian|http://archive.debian.org/debian|g' /etc/apt/sources.list \
 && sed -i 's|http://security.debian.org/debian-security|http://archive.debian.org/debian-security|g' /etc/apt/sources.list \
 && apt-get update \
 && apt-get install -y --no-install-recommends \
      gimp \
      gimp-python \
      python-gtk2 \
      xvfb \
      xauth \
 && rm -rf /var/lib/apt/lists/*
