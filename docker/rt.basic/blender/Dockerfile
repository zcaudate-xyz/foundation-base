FROM ubuntu:24.04

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      blender \
      python3 \
      libegl1 \
      libgl1 \
      libgles2 \
      libx11-6 \
      libxkbcommon0 \
      libfontconfig1 \
 && rm -rf /var/lib/apt/lists/*
