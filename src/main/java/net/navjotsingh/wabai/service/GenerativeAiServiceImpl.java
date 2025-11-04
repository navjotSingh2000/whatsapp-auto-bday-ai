package net.navjotsingh.wabai.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.images.ImageGenerateParams;
import com.openai.models.images.ImageModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
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
    public boolean generateBirthdayCardImage(String name) {
        String prompt =
                "Create an elegant, visually appealing birthday card image. " +
                        "Style: modern, classy, soft lighting, pastel colors, premium design. " +
                        "Include decorative confetti, balloons or ribbons BUT keep the composition clean, not overcrowded. " +
                        "Center the text aesthetically.\n\n" +
                        "Main text: 'Happy Birthday " + name + "!' — large, elegant typography.\n\n" +
                        "Add a short inspiring birthday quote in smaller font beneath it. " +
                        "Ensure the quote blends nicely with the overall design.\n\n" +
                        "Do NOT place any watermark. Make sure text is centered, readable, and aesthetically balanced.";

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

    @Override
    public String generateBirthdayWishMessage(String name) {
        String prompt = "Write a short birthday quote for " + name + ". Note that, do not write anything else.";

        ResponseCreateParams params = ResponseCreateParams.builder()
                .input(prompt)
                .model(ChatModel.GPT_4_1)
                .build();
        Response response = client.responses().create(params);

        String wish = response
                .output()
                .get(0)
                .message()
                .orElseThrow(() -> new RuntimeException("No Message found"))
                .content()
                .get(0)
                .outputText()
                .orElseThrow(() -> new RuntimeException("No Output text found"))
                .text();

        return wish;
    }
}
