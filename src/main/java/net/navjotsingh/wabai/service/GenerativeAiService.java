package net.navjotsingh.wabai.service;

public interface GenerativeAiService {
    boolean init();
    boolean createImage(String name, String personalMessage);
}
