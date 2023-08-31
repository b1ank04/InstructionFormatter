package com.example.instructionformatter.repository;

import com.example.instructionformatter.model.InstructionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcNativeRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<InstructionDto> findInstructions() {
        String sql = "select product_id, link_ru, link from v2_product_instructions";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            InstructionDto dto = new InstructionDto();
            dto.setProductId(rs.getString("product_id"));
            dto.setLinkUa(rs.getString("link"));
            dto.setLinkRu(rs.getString("link_ru"));
            return dto;
        });

    }
}
