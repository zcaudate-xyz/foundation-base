.PHONY: all business-analyst senior-developer publish clean distclean

PDFS := business-analyst.pdf senior-developer.pdf
DIST_DIR := ../../target/documents/application-backbone

all: $(PDFS)

business-analyst: business-analyst.pdf

senior-developer: senior-developer.pdf

%.pdf: %.tex foundation-backbone.sty
	latexmk -xelatex -interaction=nonstopmode -halt-on-error $<

publish: all
	mkdir -p $(DIST_DIR)
	cp $(PDFS) README.md $(DIST_DIR)/
	printf '%s\n' \
	  '# Foundation application backbone documents' \
	  '' \
	  '- `business-analyst.pdf` — delivery and business-value overview.' \
	  '- `senior-developer.pdf` — architecture, testing and ecosystem comparison.' \
	  > $(DIST_DIR)/INDEX.md

clean:
	latexmk -c business-analyst.tex
	latexmk -c senior-developer.tex

distclean:
	latexmk -C business-analyst.tex
	latexmk -C senior-developer.tex
	rm -rf $(DIST_DIR)
