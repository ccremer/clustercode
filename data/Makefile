# Set Shell to bash, otherwise some targets fail with dash/zsh etc.
SHELL := /bin/bash

# Disable built-in rules
MAKEFLAGS += --no-builtin-rules
MAKEFLAGS += --no-builtin-variables
.SUFFIXES:
.SECONDARY:

include ../Makefile.vars.mk

media_source_dir = source
media_intermediate_dir = intermediate
media_target_dir = target

media_filename = $(media_source_dir)/blank_video.mp4

.PHONY: blank-media
blank-media: $(media_filename) $(media_intermediate_dir) $(media_target_dir) ## Creates a blank video file

.PHONY: clean
clean: ## Cleans the intermediate and target dirs
	rm -rf $(media_source_dir) $(media_intermediate_dir) $(media_target_dir) || true

###
### Assets
###

$(media_source_dir):
	@mkdir -p $@

$(media_intermediate_dir):
	@mkdir -p $@

$(media_target_dir):
	@mkdir -p $@

$(media_filename): $(media_source_dir)
	docker run --rm -u $(shell id -u) -v $${PWD}/$(media_source_dir):/data $(FFMPEG_IMG) -y -hide_banner -t 30 -f lavfi -i color=c=black:s=320x240 -c:v libx264 -tune stillimage -pix_fmt yuv420p /data/blank_video.mp4
	@docker run --rm -v $${PWD}/$(media_source_dir):/data --entrypoint /bin/sh $(FFMPEG_IMG) -c "chmod -R 664 /data/*"
