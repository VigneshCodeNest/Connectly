package com.connectly.controller;

import com.connectly.dto.response.ApiResponse;
import com.connectly.dto.response.StickerPackResponse;
import com.connectly.dto.response.StickerPackResponse.StickerItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stickers")
public class StickerController {

    @GetMapping
    public ResponseEntity<ApiResponse<List<StickerPackResponse>>> getStickerPacks() {
        List<StickerPackResponse> packs = new ArrayList<>();

        // Pack 1: Connectly Expressions
        List<StickerItem> expressions = Arrays.asList(
                new StickerItem("stk-1", "Laughing Tears", "/api/v1/stickers/assets/laughing.png", "😂"),
                new StickerItem("stk-2", "Heart Eyes", "/api/v1/stickers/assets/heart_eyes.png", "😍"),
                new StickerItem("stk-3", "Thumbs Up", "/api/v1/stickers/assets/thumbs_up.png", "👍"),
                new StickerItem("stk-4", "Fire Reaction", "/api/v1/stickers/assets/fire.png", "🔥"),
                new StickerItem("stk-5", "Party Popper", "/api/v1/stickers/assets/party.png", "🎉"),
                new StickerItem("stk-6", "Mind Blown", "/api/v1/stickers/assets/mind_blown.png", "🤯")
        );
        packs.add(new StickerPackResponse("pack-reactions", "Quick Reactions", "Express yourself instantly", "/api/v1/stickers/assets/heart_eyes.png", expressions));

        // Pack 2: Cute Animals
        List<StickerItem> animals = Arrays.asList(
                new StickerItem("stk-7", "Happy Dog", "/api/v1/stickers/assets/happy_dog.png", "🐶"),
                new StickerItem("stk-8", "Sleepy Cat", "/api/v1/stickers/assets/sleepy_cat.png", "🐱"),
                new StickerItem("stk-9", "Cool Panda", "/api/v1/stickers/assets/cool_panda.png", "🐼"),
                new StickerItem("stk-10", "Cheering Fox", "/api/v1/stickers/assets/fox.png", "🦊")
        );
        packs.add(new StickerPackResponse("pack-animals", "Cute Animals", "Adorable animals for your chats", "/api/v1/stickers/assets/happy_dog.png", animals));

        return ResponseEntity.ok(ApiResponse.ok("Sticker packs retrieved", packs));
    }
}
