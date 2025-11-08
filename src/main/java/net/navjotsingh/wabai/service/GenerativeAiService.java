package net.navjotsingh.wabai.service;

public interface GenerativeAiService {
    boolean init();
    String generateBirthdayCardImage(String name);
    String generateBirthdayWishMessage(String name);
}
