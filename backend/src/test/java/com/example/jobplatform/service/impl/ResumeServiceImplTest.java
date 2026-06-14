package com.example.jobplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.jobplatform.dto.CreateResumeRequestDTO;
import com.example.jobplatform.dto.ResumeSkillInputDTO;
import com.example.jobplatform.entity.AccountUser;
import com.example.jobplatform.entity.JobInfo;
import com.example.jobplatform.entity.JobMatchResult;
import com.example.jobplatform.entity.Resume;
import com.example.jobplatform.entity.ResumeSkill;
import com.example.jobplatform.entity.UserProfile;
import com.example.jobplatform.mapper.AccountUserMapper;
import com.example.jobplatform.mapper.JobInfoMapper;
import com.example.jobplatform.mapper.JobMatchResultMapper;
import com.example.jobplatform.mapper.ResumeMapper;
import com.example.jobplatform.mapper.ResumeParseResultMapper;
import com.example.jobplatform.mapper.ResumeSkillMapper;
import com.example.jobplatform.mapper.UserProfileMapper;
import com.example.jobplatform.service.OperationLogService;
import com.example.jobplatform.service.PdfResumeDocumentParser;
import com.example.jobplatform.vo.JobMatchVO;
import com.example.jobplatform.vo.ResumeCreateVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {

    @Mock
    private ResumeMapper resumeMapper;
    @Mock
    private ResumeSkillMapper resumeSkillMapper;
    @Mock
    private ResumeParseResultMapper resumeParseResultMapper;
    @Mock
    private JobInfoMapper jobInfoMapper;
    @Mock
    private JobMatchResultMapper jobMatchResultMapper;
    @Mock
    private AccountUserMapper accountUserMapper;
    @Mock
    private UserProfileMapper userProfileMapper;
    @Mock
    private PdfResumeDocumentParser pdfResumeDocumentParser;
    @Mock
    private OperationLogService operationLogService;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    @Test
    void triggerMatch_ranksBySkillOverlap() {
        Resume resume = new Resume();
        resume.setId(1L);
        resume.setUserId(10L);
        when(resumeMapper.selectById(1L)).thenReturn(resume);

        ResumeSkill java = skill(1L, "Java");
        ResumeSkill spring = skill(2L, "Spring");
        when(resumeSkillMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(java, spring));

        JobInfo javaJob = job(100L, "Java开发", "Java,Spring");
        JobInfo pythonJob = job(200L, "Python开发", "Python");
        when(jobInfoMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(javaJob, pythonJob));
        when(userProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(jobInfoMapper.selectBatchIds(any())).thenReturn(List.of(javaJob, pythonJob));

        List<JobMatchVO> matches = resumeService.triggerMatch(1L, 5);

        assertThat(matches).hasSize(2);
        assertThat(matches.get(0).jobId()).isEqualTo(100L);
        assertThat(matches.get(0).totalScore()).isGreaterThan(matches.get(1).totalScore());
        verify(jobMatchResultMapper).delete(any(LambdaQueryWrapper.class));
        verify(jobMatchResultMapper, org.mockito.Mockito.times(2)).insert(any(JobMatchResult.class));
    }

    @Test
    void triggerMatch_throwsWhenResumeMissing() {
        when(resumeMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> resumeService.triggerMatch(99L, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("简历不存在");
    }

    @Test
    void triggerMatch_clampsTopN() {
        Resume resume = new Resume();
        resume.setId(2L);
        resume.setUserId(10L);
        when(resumeMapper.selectById(2L)).thenReturn(resume);
        when(resumeSkillMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(userProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        List<JobInfo> jobs = new ArrayList<>();
        for (long i = 1; i <= 5; i++) {
            jobs.add(job(i, "岗位" + i, "Java"));
        }
        when(jobInfoMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(jobs);
        when(jobInfoMapper.selectBatchIds(any())).thenReturn(jobs);

        assertThat(resumeService.triggerMatch(2L, null)).hasSize(5);
        assertThat(resumeService.triggerMatch(2L, 0)).hasSize(1);
        assertThat(resumeService.triggerMatch(2L, 500)).hasSize(5);
    }

    @Test
    void uploadResume_rejectsNullUserId() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "data".getBytes());

        assertThatThrownBy(() -> resumeService.uploadResume(null, "简历", "Java", file, "Java"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("用户ID不能为空");

        verify(resumeMapper, never()).insert(any(Resume.class));
    }

    @Test
    void uploadResume_rejectsBlankResumeName() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "data".getBytes());

        assertThatThrownBy(() -> resumeService.uploadResume(1L, "  ", "Java", file, "Java"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("简历名称不能为空");

        verify(resumeMapper, never()).insert(any(Resume.class));
    }

    @Test
    void uploadResume_rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> resumeService.uploadResume(1L, "简历", "Java", file, "Java"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("请上传简历文件");

        verify(resumeMapper, never()).insert(any(Resume.class));
    }

    @Test
    void createResume_insertsResumeAndSkills() {
        AccountUser user = new AccountUser();
        user.setId(5L);
        when(accountUserMapper.selectById(5L)).thenReturn(user);
        when(userProfileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        doAnswer(invocation -> {
            Resume r = invocation.getArgument(0);
            r.setId(42L);
            return 1;
        }).when(resumeMapper).insert(any(Resume.class));

        CreateResumeRequestDTO request = new CreateResumeRequestDTO();
        request.setUserId(5L);
        request.setResumeName("  我的简历  ");
        request.setParsedText("熟悉 Spring Boot");
        request.setTargetJobName("Java开发");
        request.setTargetCity("北京");
        request.setEducation("本科");

        ResumeSkillInputDTO skillInput = new ResumeSkillInputDTO();
        skillInput.setSkillName(" Java ");
        skillInput.setSkillLevel(3);
        skillInput.setYearsOfExperience(BigDecimal.valueOf(2));
        request.setSkills(List.of(skillInput));

        ResumeCreateVO result = resumeService.createResume(request);

        assertThat(result.resumeId()).isEqualTo(42L);
        verify(resumeMapper).insert(any(Resume.class));
        verify(userProfileMapper).insert(any(UserProfile.class));
        verify(resumeSkillMapper).insert(any(ResumeSkill.class));
    }

    @Test
    void listMatches_throwsWhenResumeMissing() {
        when(resumeMapper.selectById(88L)).thenReturn(null);

        assertThatThrownBy(() -> resumeService.listMatches(88L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("简历不存在");
    }

    private static ResumeSkill skill(Long id, String name) {
        ResumeSkill skill = new ResumeSkill();
        skill.setId(id);
        skill.setSkillName(name);
        return skill;
    }

    private static JobInfo job(Long id, String name, String skillTags) {
        JobInfo job = new JobInfo();
        job.setId(id);
        job.setJobName(name);
        job.setCompanyName("测试公司");
        job.setCity("北京");
        job.setSalaryMin(10000);
        job.setSalaryMax(15000);
        job.setEducation("本科");
        job.setExperience("不限");
        job.setSkillTags(skillTags);
        job.setStatus(1);
        return job;
    }
}
