package com.openclaw.test.service;

import com.openclaw.test.dto.IdentityCreateRequest;
import com.openclaw.test.dto.IdentityResponse;
import com.openclaw.test.entity.Identity;
import com.openclaw.test.entity.IdentityType;
import com.openclaw.test.exception.IdentityNotFoundException;
import com.openclaw.test.repository.IdentityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

    @Mock
    private IdentityRepository identityRepository;

    @InjectMocks
    private IdentityService identityService;

    private Identity testIdentity;

    @BeforeEach
    void setUp() {
        testIdentity = new Identity();
        testIdentity.setId(1L);
        testIdentity.setType(IdentityType.PM);
        testIdentity.setApiKey("sk-test-api-key");
    }

    @Test
    @DisplayName("创建身份 - 成功")
    void createIdentity_Success() {
        // Arrange
        IdentityCreateRequest request = new IdentityCreateRequest();
        request.setType(IdentityType.PM);

        when(identityRepository.save(any(Identity.class))).thenReturn(testIdentity);

        // Act
        IdentityResponse response = identityService.createIdentity(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getType()).isEqualTo(IdentityType.PM);
        assertThat(response.getApiKey()).startsWith("sk-");
        verify(identityRepository, times(1)).save(any(Identity.class));
    }

    @Test
    @DisplayName("查询身份列表 - 无筛选条件")
    void getIdentities_NoFilter() {
        // Arrange
        Page<Identity> identityPage = new PageImpl<>(List.of(testIdentity));
        when(identityRepository.findAll(any(Pageable.class))).thenReturn(identityPage);

        // Act
        Page<IdentityResponse> response = identityService.getIdentities(0, 10, null);

        // Assert
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getType()).isEqualTo(IdentityType.PM);
        verify(identityRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("查询身份列表 - 按类型筛选")
    void getIdentities_WithTypeFilter() {
        // Arrange
        Page<Identity> identityPage = new PageImpl<>(List.of(testIdentity));
        when(identityRepository.findByType(eq(IdentityType.PM), any(Pageable.class))).thenReturn(identityPage);

        // Act
        Page<IdentityResponse> response = identityService.getIdentities(0, 10, IdentityType.PM);

        // Assert
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getType()).isEqualTo(IdentityType.PM);
        verify(identityRepository, times(1)).findByType(eq(IdentityType.PM), any(Pageable.class));
    }

    @Test
    @DisplayName("根据ID查询身份 - 成功")
    void getIdentityById_Success() {
        // Arrange
        when(identityRepository.findById(1L)).thenReturn(Optional.of(testIdentity));

        // Act
        IdentityResponse response = identityService.getIdentityById(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("根据ID查询身份 - 不存在")
    void getIdentityById_NotFound() {
        // Arrange
        when(identityRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> identityService.getIdentityById(999L))
                .isInstanceOf(IdentityNotFoundException.class)
                .hasMessageContaining("身份不存在");
    }

    @Test
    @DisplayName("根据API Key查询身份 - 成功")
    void getIdentityByApiKey_Success() {
        // Arrange
        when(identityRepository.findByApiKey("sk-test-api-key")).thenReturn(Optional.of(testIdentity));

        // Act
        Identity identity = identityService.getIdentityByApiKey("sk-test-api-key");

        // Assert
        assertThat(identity).isNotNull();
        assertThat(identity.getApiKey()).isEqualTo("sk-test-api-key");
    }

    @Test
    @DisplayName("根据API Key查询身份 - 无效")
    void getIdentityByApiKey_Invalid() {
        // Arrange
        when(identityRepository.findByApiKey("invalid-key")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> identityService.getIdentityByApiKey("invalid-key"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("无效的 API Key");
    }

    @Test
    @DisplayName("删除身份 - 成功")
    void deleteIdentity_Success() {
        // Arrange
        when(identityRepository.existsById(1L)).thenReturn(true);

        // Act
        identityService.deleteIdentity(1L);

        // Assert
        verify(identityRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("删除身份 - 不存在")
    void deleteIdentity_NotFound() {
        // Arrange
        when(identityRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> identityService.deleteIdentity(999L))
                .isInstanceOf(IdentityNotFoundException.class);
    }

    @Test
    @DisplayName("重新生成API Key - 成功")
    void regenerateApiKey_Success() {
        // Arrange
        when(identityRepository.findById(1L)).thenReturn(Optional.of(testIdentity));
        when(identityRepository.save(any(Identity.class))).thenReturn(testIdentity);

        // Act
        IdentityResponse response = identityService.regenerateApiKey(1L);

        // Assert
        assertThat(response).isNotNull();
        verify(identityRepository, times(1)).save(any(Identity.class));
    }
}
