package com.example.faq;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FAQ 엔티티 클래스
 * - 카테고리, 질문, 답변, 답변 표시 여부 필드 포함
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class FAQ {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // FAQ ID

    private String category; // 카테고리
    private String question; // 질문 내용
    private String answer; // 답변 내용
    private boolean isAnswerVisible; // 답변 표시 여부

    public FAQ(String category, String question, String answer) {
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.isAnswerVisible = false;
    }
}

/**
 * FAQ 컨트롤러
 * - FAQ 데이터 관리 API 제공
 */
@RestController
@RequestMapping("/api/faq")
class FAQController {
    private final FAQRepository faqRepository;

    public FAQController(FAQRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    /**
     * 모든 FAQ 목록을 조회합니다.
     * @return FAQ 리스트
     */
    @GetMapping
    public List<FAQ> getAllFAQs() {
        return faqRepository.findAll();
    }

    /**
     * 새로운 FAQ를 생성합니다.
     * @param faq 저장할 FAQ 객체
     * @return 저장된 FAQ 객체
     */
    @PostMapping
    public FAQ createFAQ(@RequestBody FAQ faq) {
        return faqRepository.save(faq);
    }

    /**
     * 특정 FAQ의 답변 표시 여부를 토글합니다.
     * @param id FAQ ID
     * @return 업데이트된 FAQ 객체
     */
    @PutMapping("/{id}/toggle")
    public FAQ toggleAnswer(@PathVariable Long id) {
        FAQ faq = faqRepository.findById(id).orElseThrow(() -> new RuntimeException("FAQ not found"));
        faq.setAnswerVisible(!faq.isAnswerVisible());
        return faqRepository.save(faq);
    }
}

/**
 * FAQ 저장소 인터페이스
 * - JPA를 사용하여 FAQ 데이터를 관리
 */
interface FAQRepository extends org.springframework.data.jpa.repository.JpaRepository<FAQ, Long> {
}
