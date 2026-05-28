package com.example.jobplatform.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.jobplatform.entity.Resume;
import com.example.jobplatform.entity.ResumeParseResult;
import com.example.jobplatform.mapper.ResumeMapper;
import com.example.jobplatform.mapper.ResumeParseResultMapper;
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
@Order(2)
public class ResumeDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ResumeDataInitializer.class);
    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    private final ResumeMapper resumeMapper;
    private final ResumeParseResultMapper parseResultMapper;

    public ResumeDataInitializer(ResumeMapper resumeMapper, ResumeParseResultMapper parseResultMapper) {
        this.resumeMapper = resumeMapper;
        this.parseResultMapper = parseResultMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        Long count = resumeMapper.selectCount(Wrappers.emptyWrapper());
        if (count != null && count > 0) {
            log.info("resume_info already has {} records, skip CSV import.", count);
            return;
        }

        Path resumeCsvPath = locateCsvPath("sample_resumes.csv");
        if (resumeCsvPath == null) {
            log.warn("Resume CSV file not found. Expected data/sample_resumes.csv.");
            return;
        }

        int imported = importResumes(resumeCsvPath);
        log.info("Imported {} resumes from {}.", imported, resumeCsvPath);

        // 导入解析结果
        Path parseCsvPath = locateCsvPath("sample_resume_parse.csv");
        if (parseCsvPath != null) {
            int parseImported = importParseResults(parseCsvPath);
            log.info("Imported {} parse results from {}.", parseImported, parseCsvPath);
        }
    }

    private Path locateCsvPath(String filename) {
        List<Path> candidates = List.of(
            Paths.get("data", filename),
            Paths.get("..", "data", filename),
            Paths.get("..", "..", "data", filename)
        );
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private int importResumes(Path csvPath) throws IOException {
        List<Resume> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // header
            if (line == null) {
                return 0;
            }

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                Resume item = toResume(line);
                rows.add(item);
            }
        }

        for (Resume row : rows) {
            resumeMapper.insert(row);
        }
        return rows.size();
    }

    private int importParseResults(Path csvPath) throws IOException {
        List<ResumeParseResult> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // header
            if (line == null) {
                return 0;
            }

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                ResumeParseResult item = toParseResult(line);
                rows.add(item);
            }
        }

        for (ResumeParseResult row : rows) {
            parseResultMapper.insert(row);
        }
        return rows.size();
    }

    private Resume toResume(String csvLine) {
        String[] values = csvLine.split(CSV_SPLIT_REGEX, -1);
        Resume item = new Resume();

        item.setId(parseLongSafe(getValue(values, 0, "0"), null));
        item.setUserId(parseLongSafe(getValue(values, 1, "0"), 0L));
        item.setResumeName(getValue(values, 2, "简历"));
        item.setFileUrl(getValue(values, 3, ""));
        item.setFileType(getValue(values, 4, "pdf"));
        item.setParsedText(getValue(values, 5, ""));
        item.setTargetJobName(getValue(values, 6, ""));
        item.setStatus(parseIntSafe(getValue(values, 7, "0"), 0));
        item.setIsDefault(parseIntSafe(getValue(values, 8, "0"), 0));

        return item;
    }

    private ResumeParseResult toParseResult(String csvLine) {
        String[] values = csvLine.split(CSV_SPLIT_REGEX, -1);
        ResumeParseResult item = new ResumeParseResult();

        item.setResumeId(parseLongSafe(getValue(values, 0, "0"), 0L));
        item.setParsedName(getValue(values, 1, ""));
        item.setParsedEducation(getValue(values, 2, ""));
        item.setParsedSchool(getValue(values, 3, ""));
        item.setParsedMajor(getValue(values, 4, ""));
        item.setParsedSkillsJson(getValue(values, 5, "[]"));
        item.setModelName(getValue(values, 6, ""));

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

    private Long parseLongSafe(String value, Long defaultValue) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
