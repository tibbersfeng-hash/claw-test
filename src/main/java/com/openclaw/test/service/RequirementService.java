package com.openclaw.test.service;

import com.openclaw.test.dto.RequirementCreateRequest;
import com.openclaw.test.dto.RequirementHistoryResponse;
import com.openclaw.test.dto.RequirementResponse;
import com.openclaw.test.dto.RequirementUpdateRequest;
import com.openclaw.test.entity.Identity;
import com.openclaw.test.entity.IdentityType;
import com.openclaw.test.entity.Requirement;
import com.openclaw.test.entity.RequirementHistory;
import com.openclaw.test.entity.RequirementStatus;
import com.openclaw.test.exception.RequirementNotFoundException;
import com.openclaw.test.repository.RequirementHistoryRepository;
import com.openclaw.test.repository.RequirementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequirementService {

    private final RequirementRepository requirementRepository;
    private final RequirementHistoryRepository historyRepository;

    public RequirementService(RequirementRepository requirementRepository, RequirementHistoryRepository historyRepository) {
        this.requirementRepository = requirementRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public RequirementResponse createRequirement(RequirementCreateRequest request, Identity identity) {
        // 只有PM可以创建需求
        if (identity.getType() != IdentityType.PM) {
            throw new IllegalStateException("只有PM可以创建需求文档");
        }

        Requirement requirement = new Requirement();
        requirement.setTitle(request.getTitle());
        requirement.setContent(request.getContent());
        requirement.setCreator(getCreatorName(identity));
        requirement.setStatus(RequirementStatus.DRAFT);
        requirement.setVersion(1);

        if (request.getRequirementNumber() != null && !request.getRequirementNumber().trim().isEmpty()) {
            if (requirementRepository.existsByRequirementNumber(request.getRequirementNumber())) {
                throw new IllegalArgumentException("需求编号已存在: " + request.getRequirementNumber());
            }
            requirement.setRequirementNumber(request.getRequirementNumber());
        }

        Requirement saved = requirementRepository.save(requirement);

        // 保存历史记录
        saveHistory(saved, "CREATE", getCreatorName(identity), "创建需求文档");

        return RequirementResponse.fromEntity(saved);
    }

    @Transactional
    public RequirementResponse updateRequirement(Long id, RequirementUpdateRequest request, Identity identity) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RequirementNotFoundException(id));

        // 只有PM可以修改需求
        if (identity.getType() != IdentityType.PM) {
            throw new IllegalStateException("只有PM可以修改需求文档");
        }

        // 已提交的需求不能修改
        if (requirement.getStatus() == RequirementStatus.APPROVED) {
            throw new IllegalStateException("已审批的需求不能修改");
        }

        String oldTitle = requirement.getTitle();
        String oldContent = requirement.getContent();

        requirement.setTitle(request.getTitle());
        requirement.setContent(request.getContent());
        requirement.setVersion(requirement.getVersion() + 1);

        Requirement saved = requirementRepository.save(requirement);

        // 保存历史记录
        String remark = request.getRemark() != null ? request.getRemark() : "更新需求文档";
        saveHistory(saved, "UPDATE", getCreatorName(identity), remark);

        return RequirementResponse.fromEntity(saved);
    }

    @Transactional
    public RequirementResponse submitRequirement(Long id, Identity identity) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RequirementNotFoundException(id));

        if (requirement.getStatus() != RequirementStatus.DRAFT && requirement.getStatus() != RequirementStatus.REJECTED) {
            throw new IllegalStateException("只有草稿或已拒绝的需求可以提交");
        }

        requirement.setStatus(RequirementStatus.SUBMITTED);

        Requirement saved = requirementRepository.save(requirement);

        // 保存历史记录
        saveHistory(saved, "SUBMIT", getCreatorName(identity), "提交需求文档");

        return RequirementResponse.fromEntity(saved);
    }

    @Transactional
    public RequirementResponse approveRequirement(Long id, Identity identity) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RequirementNotFoundException(id));

        if (requirement.getStatus() != RequirementStatus.SUBMITTED) {
            throw new IllegalStateException("只有已提交的需求可以审批");
        }

        requirement.setStatus(RequirementStatus.APPROVED);

        Requirement saved = requirementRepository.save(requirement);

        // 保存历史记录
        saveHistory(saved, "APPROVE", getCreatorName(identity), "审批通过");

        return RequirementResponse.fromEntity(saved);
    }

    @Transactional
    public RequirementResponse rejectRequirement(Long id, String remark, Identity identity) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RequirementNotFoundException(id));

        if (requirement.getStatus() != RequirementStatus.SUBMITTED) {
            throw new IllegalStateException("只有已提交的需求可以拒绝");
        }

        requirement.setStatus(RequirementStatus.REJECTED);

        Requirement saved = requirementRepository.save(requirement);

        // 保存历史记录
        saveHistory(saved, "REJECT", getCreatorName(identity), remark != null ? remark : "拒绝需求");

        return RequirementResponse.fromEntity(saved);
    }

    public Page<RequirementResponse> getRequirements(int page, int size, RequirementStatus status, String keyword) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Requirement> requirementPage;
        if (status != null && keyword != null && !keyword.trim().isEmpty()) {
            requirementPage = requirementRepository.findByStatusAndTitleContaining(status, keyword, pageable);
        } else if (status != null) {
            requirementPage = requirementRepository.findByStatus(status, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            requirementPage = requirementRepository.findByTitleContaining(keyword, pageable);
        } else {
            requirementPage = requirementRepository.findAll(pageable);
        }

        return requirementPage.map(RequirementResponse::fromEntity);
    }

    public RequirementResponse getRequirementById(Long id) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RequirementNotFoundException(id));
        return RequirementResponse.fromEntity(requirement);
    }

    public RequirementResponse getRequirementByNumber(String requirementNumber) {
        Requirement requirement = requirementRepository.findByRequirementNumber(requirementNumber)
                .orElseThrow(() -> new RequirementNotFoundException(requirementNumber));
        return RequirementResponse.fromEntity(requirement);
    }

    public List<RequirementHistoryResponse> getRequirementHistory(Long id) {
        if (!requirementRepository.existsById(id)) {
            throw new RequirementNotFoundException(id);
        }
        return historyRepository.findByRequirementIdOrderByCreatedAtDesc(id).stream()
                .map(RequirementHistoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteRequirement(Long id, Identity identity) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RequirementNotFoundException(id));

        // 只有PM可以删除需求
        if (identity.getType() != IdentityType.PM) {
            throw new IllegalStateException("只有PM可以删除需求文档");
        }

        // 已审批的需求不能删除
        if (requirement.getStatus() == RequirementStatus.APPROVED) {
            throw new IllegalStateException("已审批的需求不能删除");
        }

        requirementRepository.deleteById(id);
    }

    private void saveHistory(Requirement requirement, String operationType, String operator, String remark) {
        RequirementHistory history = new RequirementHistory();
        history.setRequirementId(requirement.getId());
        history.setVersion(requirement.getVersion());
        history.setTitle(requirement.getTitle());
        history.setContent(requirement.getContent());
        history.setOperationType(operationType);
        history.setOperator(operator);
        history.setRemark(remark);
        historyRepository.save(history);
    }

    private String getCreatorName(Identity identity) {
        return identity.getType().name() + "-" + identity.getId();
    }
}
