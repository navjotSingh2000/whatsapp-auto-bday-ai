package net.navjotsingh.wabai.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class PostProcessImage {
    @Value("${birthday.image.save-path}")
    private String savePath;
    public String addNameOnImage(String imageUrl, String name) {
        try {
            BufferedImage original = ImageIO.read(new URL(imageUrl));

            int width = original.getWidth();
            int height = original.getHeight();

            // small caption area (7% of height)
            int extraHeight = (int) (height * 0.07);

            BufferedImage newImage = new BufferedImage(
                    width,
                    height + extraHeight,
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D g = newImage.createGraphics();

            // draw original image on top
            g.drawImage(original, 0, 0, null);

            //bBottom black bar
            g.setColor(Color.BLACK);
            g.fillRect(0, height, width, extraHeight);

            // smoothen the text
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // message to display
            String message = "Happy Birthday " + name;

            // font sized proportionally to strip height
            Font font = new Font("SansSerif", Font.PLAIN, (int) (extraHeight * 0.42));
            g.setFont(font);

            g.setColor(Color.WHITE);

            // center the text
            FontMetrics fm = g.getFontMetrics();
            int x = (width - fm.stringWidth(message)) / 2;
            int y = height + (extraHeight + fm.getAscent()) / 2 - 3;

            g.drawString(message, x, y);
            g.dispose();

            // save in the folder
            File folder = new File(savePath);
            if (!folder.exists()) folder.mkdirs();

            String sanitizedName = name.replaceAll("[^a-zA-Z0-9_-]", "_");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            String fileName = sanitizedName + "_" + timestamp + ".png";
            File outputFile = new File(folder, fileName);

            ImageIO.write(newImage, "png", outputFile);

            System.out.println("saved image at: " + outputFile.getAbsolutePath());
            return outputFile.getAbsolutePath();

        } catch (Exception e) {
            throw new RuntimeException("Failed post-processing image", e);
        }
    }

}
