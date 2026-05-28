package com.example.jobplatform.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.jobplatform.entity.AccountUser;
import com.example.jobplatform.mapper.AccountUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
public class UserDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserDataInitializer.class);
    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    private final AccountUserMapper accountUserMapper;

    public UserDataInitializer(AccountUserMapper accountUserMapper) {
        this.accountUserMapper = accountUserMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        Long count = accountUserMapper.selectCount(Wrappers.emptyWrapper());
        if (count != null && count > 0) {
            log.info("sys_user already has {} records, skip CSV import.", count);
            return;
        }

        Path csvPath = locateCsvPath();
        if (csvPath == null) {
            log.warn("CSV file not found. Expected data/sample_users.csv.");
            return;
        }

        int imported = importCsv(csvPath);
        log.info("Imported {} users from {}.", imported, csvPath);
    }

    private Path locateCsvPath() {
        List<Path> candidates = List.of(
            Paths.get("data", "sample_users.csv"),
            Paths.get("..", "data", "sample_users.csv"),
            Paths.get("..", "..", "data", "sample_users.csv")
        );
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private int importCsv(Path csvPath) throws IOException {
        List<AccountUser> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // header
            if (line == null) {
                return 0;
            }

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                AccountUser item = toAccountUser(line);
                rows.add(item);
            }
        }

        for (AccountUser row : rows) {
            accountUserMapper.insert(row);
        }
        return rows.size();
    }

    private AccountUser toAccountUser(String csvLine) {
        String[] values = csvLine.split(CSV_SPLIT_REGEX, -1);
        AccountUser item = new AccountUser();

        item.setUsername(getValue(values, 0, "unknown"));
        item.setPasswordHash(getValue(values, 1, ""));
        item.setRealName(getValue(values, 2, ""));
        item.setPhone(getValue(values, 3, ""));
        item.setEmail(getValue(values, 4, ""));
        item.setRole(getValue(values, 5, "student"));
        item.setStatus(parseIntSafe(getValue(values, 6, "1"), 1));

        return item;
    }

    private String getValue(String[] values, int index, String defaultValue) {
        if (index >= values.length) {
            return defaultValue;
        }
        String raw = values[index].trim();
        if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length() >= 2) {
            raw = raw.substring(1, raw.length() - 1);
        }
        return raw.isBlank() ? defaultValue : raw;
    }

    private int parseIntSafe(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
