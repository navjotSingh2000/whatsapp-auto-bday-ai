package net.navjotsingh.wabai.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.images.ImageGenerateParams;
import com.openai.models.images.ImageModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import net.navjotsingh.wabai.utils.PostProcessImage;
import org.springframework.stereotype.Service;

@Service
public class GenerativeAiServiceImpl implements GenerativeAiService {

    private OpenAIClient client = OpenAIOkHttpClient.fromEnv();;
    private ImageGenerateParams imageGenerateParams;

    private final PostProcessImage postProcessImage;

    public GenerativeAiServiceImpl(PostProcessImage postProcessImage) {
        this.postProcessImage = postProcessImage;
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public String generateBirthdayCardImage(String name) {
        String prompt = "Create a simple birthday card image. Note that, i do not want any texts on the image.";
        System.out.println("Image generation started");

        imageGenerateParams = ImageGenerateParams.builder()
                .responseFormat(ImageGenerateParams.ResponseFormat.URL)
                .prompt(prompt)
                .model(ImageModel.DALL_E_3)
                .size(ImageGenerateParams.Size._1024X1024)
                .quality(ImageGenerateParams.Quality.HD)
                .n(1)
                .build();

        String imageUrl = client.images().generate(imageGenerateParams)
                .data()
                .orElseThrow()
                .get(0)
                .url()
                .orElseThrow();

        // after image is generated, add the caption of birthday wish in the post processing
        String finalImagePath = postProcessImage.addNameOnImage(imageUrl, name);

        return finalImagePath;
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
