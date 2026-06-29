FROM ubuntu:24.04

# Dependencies needed to download, extract, and run the GIMP AppImage headlessly.
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      ca-certificates \
      wget \
      libfuse2 \
      xvfb \
      xauth \
      libgtk-3-0 \
      libgdk-pixbuf-2.0-0 \
      at-spi2-core \
 && rm -rf /var/lib/apt/lists/*

# Download and extract the official GIMP 3.0 AppImage. The AppImage bundles
# python-fu-eval and the GI repositories (Gimp/Gio/Gegl) that the tutorial
# code requires, neither of which are available in Ubuntu 24.04 packages.
ARG GIMP_APPIMAGE_VERSION=3.0.8-1
ARG GIMP_APPIMAGE_URL=https://download.gimp.org/gimp/v3.0/linux/GIMP-${GIMP_APPIMAGE_VERSION}-x86_64.AppImage
ENV GIMP_APPIMAGE_DIR=/opt/gimp-appimage

RUN mkdir -p ${GIMP_APPIMAGE_DIR} \
 && wget -q ${GIMP_APPIMAGE_URL} -O ${GIMP_APPIMAGE_DIR}/gimp.appimage \
 && chmod +x ${GIMP_APPIMAGE_DIR}/gimp.appimage \
 && cd ${GIMP_APPIMAGE_DIR} \
 && ./gimp.appimage --appimage-extract >/dev/null \
 && rm ${GIMP_APPIMAGE_DIR}/gimp.appimage

# Provide conventional `gimp`/`gimp-console` binaries on PATH that delegate to
# the extracted AppImage. The runtime code invokes `gimp` with the same
# python-fu-eval batch flags used for the old Debian package.
RUN ln -s ${GIMP_APPIMAGE_DIR}/squashfs-root/AppRun /usr/local/bin/gimp \
 && ln -s ${GIMP_APPIMAGE_DIR}/squashfs-root/AppRun /usr/local/bin/gimp-console
