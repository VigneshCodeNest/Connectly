package com.connectly.dto.response;

import java.util.List;

public class StickerPackResponse {
    private String id;
    private String name;
    private String description;
    private String previewUrl;
    private List<StickerItem> stickers;

    public StickerPackResponse() {}

    public StickerPackResponse(String id, String name, String description, String previewUrl, List<StickerItem> stickers) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.previewUrl = previewUrl;
        this.stickers = stickers;
    }

    public static class StickerItem {
        private String id;
        private String name;
        private String url;
        private String emoji;

        public StickerItem() {}

        public StickerItem(String id, String name, String url, String emoji) {
            this.id = id;
            this.name = name;
            this.url = url;
            this.emoji = emoji;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getEmoji() { return emoji; }
        public void setEmoji(String emoji) { this.emoji = emoji; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }

    public List<StickerItem> getStickers() { return stickers; }
    public void setStickers(List<StickerItem> stickers) { this.stickers = stickers; }
}
