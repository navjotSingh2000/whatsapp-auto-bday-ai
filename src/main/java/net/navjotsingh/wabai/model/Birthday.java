package net.navjotsingh.wabai.model;

import java.time.LocalDate;

public class Birthday {
    private String name;
    private LocalDate birthDate;
    private String personalMessage;

    public Birthday(String name, LocalDate birthDate) {
        this.name = name;
        this.birthDate = birthDate;
    }

    public Birthday(String name, LocalDate birthDate, String personalMessage) {
        this.name = name;
        this.birthDate = birthDate;
        this.personalMessage = personalMessage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getPersonalMessage() {
        return personalMessage;
    }

    public void setPersonalMessage(String personalMessage) {
        this.personalMessage = personalMessage;
    }
}
