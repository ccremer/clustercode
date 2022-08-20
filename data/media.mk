media_source_dir = data/source
media_intermediate_dir = data/intermediate
media_target_dir = data/target

media_filename = $(media_source_dir)/blank_video.mp4

clean_targets += media-clean

.PHONY: blank-media
blank-media: $(media_filename) | $(media_intermediate_dir) $(media_target_dir) ## Creates a blank video file

.PHONY: media-clean
media-clean: ## Cleans the intermediate and target dirs
	rm -rf $(media_source_dir) $(media_intermediate_dir) $(media_target_dir)

###
### Assets
###

$(media_source_dir):
	@mkdir -p $@

$(media_intermediate_dir):
	@mkdir -p $@

$(media_target_dir):
	@mkdir -p $@

$(media_filename): build-docker | $(media_source_dir)
	docker run --rm -u $(shell id -u):$(shell id -g) -v $${PWD}/$(media_source_dir):/data $(FFMPEG_IMG) -y -hide_banner -t 30 -f lavfi -i color=c=black:s=320x240 -c:v libx264 -tune stillimage -pix_fmt yuv420p /data/blank_video.mp4
