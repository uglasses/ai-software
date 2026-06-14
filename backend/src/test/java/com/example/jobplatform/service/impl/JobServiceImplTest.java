package com.example.jobplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.jobplatform.dto.AdminJobQueryDTO;
import com.example.jobplatform.dto.JobQueryDTO;
import com.example.jobplatform.entity.JobInfo;
import com.example.jobplatform.mapper.JobInfoMapper;
import com.example.jobplatform.vo.JobSummaryVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobInfoMapper jobInfoMapper;

    @InjectMocks
    private JobServiceImpl jobService;

    @Test
    void importFromCsv_parsesValidRowsAndSkipsInvalid() throws Exception {
        String csv = """
            jobName,company,city,salaryMin,salaryMax,education
            Java开发,测试公司,北京,10000,15000,本科

            坏行,公司
            Python开发,另一公司,上海,12000,18000,硕士
            """;
        MockMultipartFile file = new MockMultipartFile(
            "file", "jobs.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        int count = jobService.importFromCsv(file);

        assertThat(count).isEqualTo(2);
        verify(jobInfoMapper, times(2)).insert(any(JobInfo.class));
    }

    @Test
    void importFromCsv_setsSalaryZeroOnBadNumber() throws Exception {
        String csv = """
            jobName,company,city,salaryMin,salaryMax,education
            坏薪资岗,测试公司,北京,abc,def,本科
            """;
        MockMultipartFile file = new MockMultipartFile(
            "file", "jobs.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        jobService.importFromCsv(file);

        ArgumentCaptor<JobInfo> captor = ArgumentCaptor.forClass(JobInfo.class);
        verify(jobInfoMapper).insert(captor.capture());
        assertThat(captor.getValue().getSalaryMin()).isZero();
        assertThat(captor.getValue().getSalaryMax()).isZero();
    }

    @Test
    void updateJob_throwsWhenJobMissing() {
        when(jobInfoMapper.selectById(999L)).thenReturn(null);

        JobInfo update = new JobInfo();
        update.setJobName("新岗位");

        assertThatThrownBy(() -> jobService.updateJob(999L, update))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("岗位不存在");
    }

    @Test
    void pageJobsForAdmin_returnsEmptyWhenPageBeyondTotal() {
        JobInfo job1 = job(1L, "岗位A");
        JobInfo job2 = job(2L, "岗位B");
        JobInfo job3 = job(3L, "岗位C");
        when(jobInfoMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(job1, job2, job3));

        AdminJobQueryDTO query = new AdminJobQueryDTO();
        query.setPageNum(99L);
        query.setPageSize(10L);

        var page = jobService.pageJobsForAdmin(query);

        assertThat(page.total()).isEqualTo(3);
        assertThat(page.records()).isEmpty();
    }

    @Test
    void listJobs_mapsToSummaryVO() {
        JobInfo job = job(10L, "Java开发");
        job.setCompanyName("测试公司");
        job.setCity("上海");
        job.setSalaryMin(8000);
        job.setSalaryMax(12000);
        job.setEducation("本科");
        job.setSkillTags("Java,Spring");
        when(jobInfoMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(job));

        List<JobSummaryVO> result = jobService.listJobs(new JobQueryDTO());

        assertThat(result).hasSize(1);
        JobSummaryVO vo = result.get(0);
        assertThat(vo.id()).isEqualTo(10L);
        assertThat(vo.jobName()).isEqualTo("Java开发");
        assertThat(vo.companyName()).isEqualTo("测试公司");
        assertThat(vo.city()).isEqualTo("上海");
        assertThat(vo.salary()).isEqualTo("8000-12000");
        assertThat(vo.education()).isEqualTo("本科");
        assertThat(vo.skillTags()).isEqualTo("Java,Spring");
    }

    private static JobInfo job(Long id, String name) {
        JobInfo job = new JobInfo();
        job.setId(id);
        job.setJobName(name);
        job.setStatus(1);
        return job;
    }
}
