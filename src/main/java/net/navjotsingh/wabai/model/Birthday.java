package net.navjotsingh.wabai.model;

import java.time.LocalDate;

public class Birthday {
    private String name;
    private String contactName;
    private LocalDate birthDate;

    public Birthday(String name, String contactName, LocalDate birthDate) {
        this.name = name;
        this.contactName = contactName;
        this.birthDate = birthDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
