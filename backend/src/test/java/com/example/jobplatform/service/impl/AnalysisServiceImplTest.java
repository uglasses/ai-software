package com.example.jobplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.jobplatform.entity.JobInfo;
import com.example.jobplatform.mapper.JobInfoMapper;
import com.example.jobplatform.vo.ChartItemVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceImplTest {

    @Mock
    private JobInfoMapper jobInfoMapper;

    @InjectMocks
    private AnalysisServiceImpl analysisService;

    @Test
    void cityJobCount_mapsSelectMapsResult() {
        Map<String, Object> row = new HashMap<>();
        row.put("name", "北京");
        row.put("value", 5L);
        when(jobInfoMapper.selectMaps(any(QueryWrapper.class))).thenReturn(List.of(row));

        List<ChartItemVO> result = analysisService.cityJobCount();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("北京");
        assertThat(result.get(0).value()).isEqualTo(5);
    }

    @Test
    void topSkillCount_aggregatesAndLimits() {
        JobInfo job1 = new JobInfo();
        job1.setSkillTags("Java,Spring");
        JobInfo job2 = new JobInfo();
        job2.setSkillTags("Java,Python");
        when(jobInfoMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(job1, job2));

        List<ChartItemVO> result = analysisService.topSkillCount(2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Java");
        assertThat(result.get(0).value()).isEqualTo(2);
    }

    @Test
    void topSkillCount_clampsLimit() {
        List<JobInfo> jobs = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            JobInfo job = new JobInfo();
            job.setSkillTags("Skill" + i);
            jobs.add(job);
        }
        when(jobInfoMapper.selectList(any(QueryWrapper.class))).thenReturn(jobs);

        assertThat(analysisService.topSkillCount(0)).hasSize(1);
        assertThat(analysisService.topSkillCount(100)).hasSize(50);
    }

    @Test
    void salaryRangeCount_mapsBuckets() {
        Map<String, Object> row1 = new HashMap<>();
        row1.put("name", "8k-12k");
        row1.put("value", 3);
        Map<String, Object> row2 = new HashMap<>();
        row2.put("name", "16k+");
        row2.put("value", 1);
        when(jobInfoMapper.selectMaps(any(QueryWrapper.class))).thenReturn(List.of(row1, row2));

        List<ChartItemVO> result = analysisService.salaryRangeCount();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("8k-12k");
        assertThat(result.get(0).value()).isEqualTo(3);
        assertThat(result.get(1).name()).isEqualTo("16k+");
        assertThat(result.get(1).value()).isEqualTo(1);
    }
}
