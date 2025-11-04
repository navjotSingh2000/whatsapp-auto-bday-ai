package net.navjotsingh.wabai.service;

public interface GenerativeAiService {
    boolean init();
    boolean generateBirthdayCardImage(String name);
    String generateBirthdayWishMessage(String name);
}
