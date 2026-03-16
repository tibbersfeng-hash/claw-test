package com.openclaw.test.service;

import com.openclaw.test.dto.DesignDocCreateRequest;
import com.openclaw.test.dto.DesignDocResponse;
import com.openclaw.test.dto.DesignDocUpdateRequest;
import com.openclaw.test.entity.DesignDoc;
import com.openclaw.test.entity.Identity;
import com.openclaw.test.entity.IdentityType;
import com.openclaw.test.exception.DesignDocNotFoundException;
import com.openclaw.test.repository.DesignDocRepository;
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
class DesignDocServiceTest {

    @Mock
    private DesignDocRepository designDocRepository;

    @InjectMocks
    private DesignDocService designDocService;

    private Identity devIdentity;
    private DesignDoc testDoc;

    @BeforeEach
    void setUp() {
        devIdentity = new Identity();
        devIdentity.setId(1L);
        devIdentity.setType(IdentityType.DEV);

        testDoc = new DesignDoc();
        testDoc.setId(1L);
        testDoc.setTitle("测试设计文档");
        testDoc.setContent("# 设计内容\n\n这是测试内容");
        testDoc.setCreator("DEV-1");
        testDoc.setTaskId(1L);
    }

    @Test
    @DisplayName("创建设计文档 - 成功")
    void createDoc_Success() {
        // Arrange
        DesignDocCreateRequest request = new DesignDocCreateRequest();
        request.setTitle("新设计文档");
        request.setContent("# 设计内容");
        request.setTaskId(1L);

        when(designDocRepository.save(any(DesignDoc.class))).thenAnswer(invocation -> {
            DesignDoc doc = invocation.getArgument(0);
            doc.setId(1L);
            return doc;
        });

        // Act
        DesignDocResponse response = designDocService.createDoc(request, devIdentity);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("新设计文档");
        assertThat(response.getCreator()).isEqualTo("DEV-1");
        assertThat(response.getTaskId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("为任务创建设计文档 - 成功")
    void createDocForTask_Success() {
        // Arrange
        when(designDocRepository.save(any(DesignDoc.class))).thenAnswer(invocation -> {
            DesignDoc doc = invocation.getArgument(0);
            doc.setId(1L);
            return doc;
        });

        // Act
        DesignDocResponse response = designDocService.createDocForTask(
                1L, "任务#1 设计文档", "设计内容", "DEV-1");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("任务#1 设计文档");
        assertThat(response.getTaskId()).isEqualTo(1L);
        assertThat(response.getCreator()).isEqualTo("DEV-1");
    }

    @Test
    @DisplayName("查询设计文档列表 - 无筛选")
    void getDocs_NoFilter() {
        // Arrange
        Page<DesignDoc> docPage = new PageImpl<>(List.of(testDoc));
        when(designDocRepository.findAll(any(Pageable.class))).thenReturn(docPage);

        // Act
        Page<DesignDocResponse> response = designDocService.getDocs(0, 10, null);

        // Assert
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("测试设计文档");
    }

    @Test
    @DisplayName("查询设计文档列表 - 按任务ID筛选")
    void getDocs_ByTaskId() {
        // Arrange
        Page<DesignDoc> docPage = new PageImpl<>(List.of(testDoc));
        when(designDocRepository.findByTaskId(eq(1L), any(Pageable.class))).thenReturn(docPage);

        // Act
        Page<DesignDocResponse> response = designDocService.getDocs(0, 10, 1L);

        // Assert
        assertThat(response.getContent()).hasSize(1);
        verify(designDocRepository, times(1)).findByTaskId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("根据ID查询设计文档 - 成功")
    void getDocById_Success() {
        // Arrange
        when(designDocRepository.findById(1L)).thenReturn(Optional.of(testDoc));

        // Act
        DesignDocResponse response = designDocService.getDocById(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("根据ID查询设计文档 - 不存在")
    void getDocById_NotFound() {
        // Arrange
        when(designDocRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> designDocService.getDocById(999L))
                .isInstanceOf(DesignDocNotFoundException.class);
    }

    @Test
    @DisplayName("更新设计文档 - 修改标题和内容")
    void updateDoc_Success() {
        // Arrange
        DesignDocUpdateRequest request = new DesignDocUpdateRequest();
        request.setTitle("更新后的标题");
        request.setContent("更新后的内容");

        when(designDocRepository.findById(1L)).thenReturn(Optional.of(testDoc));
        when(designDocRepository.save(any(DesignDoc.class))).thenReturn(testDoc);

        // Act
        DesignDocResponse response = designDocService.updateDoc(1L, request);

        // Assert
        assertThat(response).isNotNull();
        verify(designDocRepository, times(1)).save(any(DesignDoc.class));
    }

    @Test
    @DisplayName("更新设计文档 - 不存在")
    void updateDoc_NotFound() {
        // Arrange
        DesignDocUpdateRequest request = new DesignDocUpdateRequest();
        request.setTitle("更新后的标题");

        when(designDocRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> designDocService.updateDoc(999L, request))
                .isInstanceOf(DesignDocNotFoundException.class);
    }

    @Test
    @DisplayName("删除设计文档 - 成功")
    void deleteDoc_Success() {
        // Arrange
        when(designDocRepository.existsById(1L)).thenReturn(true);

        // Act
        designDocService.deleteDoc(1L);

        // Assert
        verify(designDocRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("删除设计文档 - 不存在")
    void deleteDoc_NotFound() {
        // Arrange
        when(designDocRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> designDocService.deleteDoc(999L))
                .isInstanceOf(DesignDocNotFoundException.class);
    }
}
