package com.example.jobplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.jobplatform.dto.AdminUserQueryDTO;
import com.example.jobplatform.dto.OperationLogQueryDTO;
import com.example.jobplatform.entity.AccountUser;
import com.example.jobplatform.entity.OperationLog;
import com.example.jobplatform.mapper.AccountUserMapper;
import com.example.jobplatform.mapper.JobInfoMapper;
import com.example.jobplatform.mapper.OperationLogMapper;
import com.example.jobplatform.mapper.ResumeMapper;
import com.example.jobplatform.vo.DashboardStatsVO;
import com.example.jobplatform.vo.OperationLogVO;
import com.example.jobplatform.vo.UserVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private JobInfoMapper jobInfoMapper;
    @Mock
    private AccountUserMapper accountUserMapper;
    @Mock
    private ResumeMapper resumeMapper;
    @Mock
    private OperationLogMapper operationLogMapper;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void dashboard_aggregatesCounts() {
        when(jobInfoMapper.selectCount(isNull())).thenReturn(100L);
        when(accountUserMapper.selectCount(isNull())).thenReturn(50L);
        when(resumeMapper.selectCount(isNull())).thenReturn(30L);
        when(jobInfoMapper.selectCount(any(QueryWrapper.class))).thenReturn(5L);

        DashboardStatsVO stats = adminService.dashboard();

        assertThat(stats.totalJobs()).isEqualTo(100);
        assertThat(stats.totalUsers()).isEqualTo(50);
        assertThat(stats.totalResumes()).isEqualTo(30);
        assertThat(stats.todayNew()).isEqualTo(5);
    }

    @Test
    void pageUsers_returnsPagedRecords() {
        AccountUser user1 = user(1L, "alice", "Alice", "13800000001", "a@example.com");
        AccountUser user2 = user(2L, "bob", "Bob", "13800000002", "b@example.com");
        when(accountUserMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(user1, user2));

        AdminUserQueryDTO query = new AdminUserQueryDTO();
        query.setPageNum(1);
        query.setPageSize(1);

        var page = adminService.pageUsers(query);

        assertThat(page.total()).isEqualTo(2);
        assertThat(page.records()).hasSize(1);
        UserVO vo = page.records().get(0);
        assertThat(vo.getUsername()).isEqualTo("alice");
        assertThat(vo.getRealName()).isEqualTo("Alice");
        assertThat(vo.getEmail()).isEqualTo("a@example.com");
    }

    @Test
    void pageUsers_returnsEmptyWhenBeyondTotal() {
        when(accountUserMapper.selectList(any(QueryWrapper.class)))
            .thenReturn(List.of(user(1L, "alice", "Alice", "1", "a@example.com")));

        AdminUserQueryDTO query = new AdminUserQueryDTO();
        query.setPageNum(99);
        query.setPageSize(10);

        var page = adminService.pageUsers(query);

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.records()).isEmpty();
    }

    @Test
    void updateUserStatus_updatesExistingUser() {
        AccountUser user = user(3L, "carol", "Carol", "1", "c@example.com");
        user.setStatus(1);
        when(accountUserMapper.selectById(3L)).thenReturn(user);

        adminService.updateUserStatus(3L, 0);

        assertThat(user.getStatus()).isZero();
        verify(accountUserMapper).updateById(user);
    }

    @Test
    void updateUserStatus_throwsWhenUserMissing() {
        when(accountUserMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> adminService.updateUserStatus(99L, 0))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("用户不存在");
    }

    @Test
    void deleteUser_callsMapper() {
        adminService.deleteUser(7L);

        verify(accountUserMapper).deleteById(7L);
    }

    @Test
    void pageLogs_returnsPagedRecords() {
        OperationLog log = new OperationLog();
        log.setId(10L);
        log.setUserId(1L);
        log.setModuleName("resume");
        log.setOperationType("上传");
        log.setOperationDesc("上传简历");
        log.setRequestPath("/api/resume/upload");
        log.setRequestMethod("POST");
        log.setIpAddress("127.0.0.1");
        log.setResultStatus("1");
        log.setCreatedAt(LocalDateTime.of(2026, 6, 11, 10, 0));
        when(operationLogMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(log));

        OperationLogQueryDTO query = new OperationLogQueryDTO();
        query.setPageNum(1);
        query.setPageSize(10);

        var page = adminService.pageLogs(query);

        assertThat(page.total()).isEqualTo(1);
        OperationLogVO vo = page.records().get(0);
        assertThat(vo.getModuleName()).isEqualTo("resume");
        assertThat(vo.getOperationType()).isEqualTo("上传");
    }

    @Test
    void cleanData_jobs_deletesJobsOnly() {
        adminService.cleanData("jobs");

        verify(jobInfoMapper).delete(isNull());
    }

    @Test
    void cleanData_all_deletesAllTables() {
        adminService.cleanData("all");

        verify(jobInfoMapper).delete(isNull());
        verify(accountUserMapper).delete(isNull());
        verify(resumeMapper).delete(isNull());
        verify(operationLogMapper).delete(isNull());
    }

    @Test
    void cleanData_throwsOnUnsupportedType() {
        assertThatThrownBy(() -> adminService.cleanData("unknown"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("不支持的清理类型");
    }

    private static AccountUser user(Long id, String username, String realName, String phone, String email) {
        AccountUser user = new AccountUser();
        user.setId(id);
        user.setUsername(username);
        user.setRealName(realName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole("student");
        user.setStatus(1);
        return user;
    }
}
