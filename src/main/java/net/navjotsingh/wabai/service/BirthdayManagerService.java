package net.navjotsingh.wabai.service;

import net.navjotsingh.wabai.model.Birthday;

import java.io.IOException;
import java.util.List;

public interface BirthdayManagerService {
    List<Birthday> getBirthdays();
}
