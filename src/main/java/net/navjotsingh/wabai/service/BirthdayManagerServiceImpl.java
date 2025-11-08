package net.navjotsingh.wabai.service;

import net.navjotsingh.wabai.model.Birthday;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class BirthdayManagerServiceImpl implements BirthdayManagerService {

    @Value("classpath:data/birthdays.json")
    private Resource file;

    public String getFileContent() {
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Birthday> getBirthdays() {
        List<Birthday> birthdays = new ArrayList<>();

        String rawJson = getFileContent();
        if(rawJson.isEmpty()) {
            throw new RuntimeException("Error while parsing json");
        }

        JSONArray jsonarray = new JSONArray(rawJson);
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            String name = jsonobject.getString("name");
            String contactName = jsonobject.getString("contactName");
            String birthDateRaw = jsonobject.getString("birthDate");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate birthDate = LocalDate.parse(birthDateRaw, formatter);

            birthdays.add(new Birthday(name, contactName, birthDate));
        }

        return birthdays;
    }
}
