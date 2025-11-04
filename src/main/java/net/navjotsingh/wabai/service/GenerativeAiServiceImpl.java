package net.navjotsingh.wabai.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.images.ImageGenerateParams;
import com.openai.models.images.ImageModel;
import org.springframework.stereotype.Service;

@Service
public class GenerativeAiServiceImpl implements GenerativeAiService {

    private OpenAIClient client = OpenAIOkHttpClient.fromEnv();;
    private ImageGenerateParams imageGenerateParams;

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public boolean createImage(String name, String personalMessage) {
        String prompt =
                "Create an elegant, visually appealing birthday card image. " +
                        "Style: modern, classy, soft lighting, pastel colors, premium design. " +
                        "Include decorative confetti, balloons or ribbons BUT keep the composition clean, not overcrowded. " +
                        "Center the text aesthetically.\n\n" +
                        "Main text: 'Happy Birthday " + name + "!' — large, elegant typography.\n\n" +
                        "Add a short inspiring birthday quote in smaller font beneath it. " +
                        "Ensure the quote blends nicely with the overall design.\n";

        if (!personalMessage.isEmpty()) {
            prompt +=
                    "\nAdd this as a small footer note at the bottom of the card, subtle and minimal: '" +
                            personalMessage + "'.\n" +
                            "Make sure this personal message does NOT overpower the main text.";
        }

        prompt +=
                "\nDo NOT place any watermark. Make sure text is centered, readable, and aesthetically balanced.";

        imageGenerateParams = ImageGenerateParams.builder()
                .responseFormat(ImageGenerateParams.ResponseFormat.URL)
                .prompt(prompt)
                .model(ImageModel.DALL_E_3)
                .size(ImageGenerateParams.Size._1024X1024)
                .quality(ImageGenerateParams.Quality.HD)
                .n(1)
                .build();

        client.images().generate(imageGenerateParams).data().orElseThrow().stream()
                .flatMap(image -> image.url().stream())
                .forEach(System.out::println);

        return true;
    }
}
