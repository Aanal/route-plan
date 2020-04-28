package com.example;

import org.apache.commons.text.RandomStringGenerator;

public class Utils {

  public static String randomString() {
    RandomStringGenerator generator = new RandomStringGenerator.Builder()
        .withinRange('a', 'z')
        .build();
    return generator.generate(20);
}
}