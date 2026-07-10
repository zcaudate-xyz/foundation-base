#!/usr/bin/env bash
set -euo pipefail

case "$(uname -s)" in
  Darwin)
    if ! command -v tlmgr >/dev/null 2>&1; then
      curl -L https://yihui.org/tinytex/install-unx.sh | sh
      export PATH="$HOME/Library/TinyTeX/bin/universal-darwin:$PATH"
    fi
    tlmgr install latexmk fontspec geometry xcolor microtype graphics booktabs tools enumitem titlesec fancyhdr hyperref pgf listings fontawesome5
    ;;
  Linux)
    sudo apt-get update
    sudo apt-get install -y --no-install-recommends \
      texlive-xetex \
      texlive-latex-extra \
      texlive-fonts-recommended \
      texlive-pictures \
      latexmk \
      fonts-inter \
      fonts-roboto
    sudo fc-cache -fv
    ;;
  *)
    echo "Unsupported operating system: $(uname -s)" >&2
    exit 1
    ;;
esac
