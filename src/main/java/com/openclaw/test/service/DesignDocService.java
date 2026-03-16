package com.openclaw.test.service;

import com.openclaw.test.dto.DesignDocCreateRequest;
import com.openclaw.test.dto.DesignDocResponse;
import com.openclaw.test.dto.DesignDocUpdateRequest;
import com.openclaw.test.entity.DesignDoc;
import com.openclaw.test.entity.Identity;
import com.openclaw.test.exception.DesignDocNotFoundException;
import com.openclaw.test.repository.DesignDocRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DesignDocService {

    private final DesignDocRepository designDocRepository;

    public DesignDocService(DesignDocRepository designDocRepository) {
        this.designDocRepository = designDocRepository;
    }

    @Transactional
    public DesignDocResponse createDoc(DesignDocCreateRequest request, Identity identity) {
        DesignDoc doc = new DesignDoc();
        doc.setTitle(request.getTitle());
        doc.setContent(request.getContent());
        doc.setTaskId(request.getTaskId());
        doc.setCreator(getCreatorName(identity));

        DesignDoc savedDoc = designDocRepository.save(doc);
        return DesignDocResponse.fromEntity(savedDoc);
    }

    @Transactional
    public DesignDocResponse createDocForTask(Long taskId, String title, String content, String creator) {
        DesignDoc doc = new DesignDoc();
        doc.setTitle(title);
        doc.setContent(content);
        doc.setTaskId(taskId);
        doc.setCreator(creator);

        DesignDoc savedDoc = designDocRepository.save(doc);
        return DesignDocResponse.fromEntity(savedDoc);
    }

    private String getCreatorName(Identity identity) {
        return identity.getType().name() + "-" + identity.getId();
    }

    public Page<DesignDocResponse> getDocs(int page, int size, Long taskId) {
        Pageable pageable = PageRequest.of(page, size);

        Page<DesignDoc> docPage;
        if (taskId != null) {
            docPage = designDocRepository.findByTaskId(taskId, pageable);
        } else {
            docPage = designDocRepository.findAll(pageable);
        }

        return docPage.map(DesignDocResponse::fromEntity);
    }

    public DesignDocResponse getDocById(Long id) {
        DesignDoc doc = designDocRepository.findById(id)
                .orElseThrow(() -> new DesignDocNotFoundException(id));
        return DesignDocResponse.fromEntity(doc);
    }

    @Transactional
    public DesignDocResponse updateDoc(Long id, DesignDocUpdateRequest request) {
        DesignDoc doc = designDocRepository.findById(id)
                .orElseThrow(() -> new DesignDocNotFoundException(id));

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            doc.setTitle(request.getTitle());
        }

        if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            doc.setContent(request.getContent());
        }

        DesignDoc savedDoc = designDocRepository.save(doc);
        return DesignDocResponse.fromEntity(savedDoc);
    }

    @Transactional
    public void deleteDoc(Long id) {
        if (!designDocRepository.existsById(id)) {
            throw new DesignDocNotFoundException(id);
        }
        designDocRepository.deleteById(id);
    }
}
