package com.andretrindade.batatas.helpers;

import android.annotation.SuppressLint;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Utils {
    @SuppressLint("SdCardPath")
    private static final String CSV_PATH = "/data/data/com.andretrindade.batatas/cache/cache.csv";

    public static HashMap<String, List<String>> addItemToHash(HashMap<String, List<String>> hash, String key, String value) {
        List<String> currentValues = hash.get(key);

        if (currentValues == null || currentValues.size() == 0) {
            hash.put(key, Collections.singletonList(value));
        } else if (!currentValues.contains(value)) {
            currentValues.add(value);
            hash.put(key, currentValues);
        }

        return hash;
    }

    public static void addItemToHash(HashMap<String, List<String>> hash, String key, List<String> values) {
        List<String> currentValues = hash.get(key);

        if (currentValues == null || currentValues.size() == 0) {
            hash.put(key, values);
        } else {
            currentValues.addAll(values);
            hash.put(key, currentValues);
        }
    }

    public static HashMap<String, List<String>> readProductsFromCsv() {
        HashMap<String, List<String>> result = new HashMap<>();

        try {
            CSVReader csvReader = new CSVReader(new FileReader(CSV_PATH));
            String[] line;

            while ((line = csvReader.readNext()) != null) {
                String eanCode = line[0];
                String name = line[1];
                result = addItemToHash(result, eanCode, name);
            }
        } catch (IOException e) {
            saveProductsToCsv(new HashMap<String, List<String>>());
            //e.printStackTrace();
        }
        return result;
    }

    public static void saveProductsToCsv(HashMap<String, List<String>> cache) {
        try {
            File file = new File(CSV_PATH);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            CSVWriter csvWriter = new CSVWriter(new FileWriter(CSV_PATH));

            for (Map.Entry<String, List<String>> entry : cache.entrySet()) {
                String key = entry.getKey();
                for (String item : entry.getValue()) {
                    csvWriter.writeNext(new String[]{key, item});
                }
            }

            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
