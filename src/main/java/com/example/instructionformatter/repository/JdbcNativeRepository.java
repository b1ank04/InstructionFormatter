package com.example.instructionformatter.repository;

import com.example.instructionformatter.model.InstructionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class JdbcNativeRepository {

    private final JdbcTemplate jdbcTemplate;

//    public List<InstructionDto> findInstructions() {
//        String sql = "select product_id, link_ru, link from v2_product_instructions";
//        return jdbcTemplate.query(sql, (rs, rowNum) -> {
//            InstructionDto dto = new InstructionDto();
//            dto.setProductId(rs.getString("product_id"));
//            dto.setLinkUa(rs.getString("link"));
//            dto.setLinkRu(rs.getString("link_ru"));
//            return dto;
//        });
//    }

    public List<String> findTop100ProductsProd() {
//        String sql = "select vpi.link from v2_product_instructions vpi \n" +
//                "inner join v2_products vp on vp.id = vpi.product_id \n" +
//                "where rating_by_count is not null\n" +
//                "order by rating_by_count asc\n" +
//                "limit 100";
        String sql = "select link from v2_product_instructions vpi where product_id in (3257, 5511, 1062415, 1041158, 270, 1066015, 3545, 36385, 1065004, 1045721, 46244, 1048118, 23004, 1062418, 2772, 1045719, 12211, 1066622, 1052759, 2038, 1043120, 1065549, 1042110, 1044143, 1043250, 3429, 2320, 1063333, 1051581, 1049612, 1067061, 1067431, 46664, 3542, 46996, 1045776, 1060393, 1044685, 46324, 1048876, 39094, 34993, 29795, 1064049, 3198, 1045775, 33096, 1064048, 3199, 4553, 45373, 3260, 50790, 46127, 1659, 388, 5069, 39140, 38282, 1059082, 1035043, 2791, 39095, 11956, 4554, 1053982, 1068303, 1428, 30775, 1064062, 1068151, 1043038, 1041013, 36613, 1056209, 1533, 2064, 306, 1042930, 58509, 44513, 1061464, 1039497, 1179, 2323, 47099, 1061466, 1037569, 1057186, 36814, 1065399, 1035888, 2016, 1064060, 1064061, 46366, 1043138, 1062413, 11200, 1053041)\n";
        return jdbcTemplate.queryForList(sql, String.class).stream().filter(Objects::nonNull).toList();
    }

    public List<String> findTop100ProductsTest() {
        List<String> topProducts = List.of("1054974,63816,1046634,1337,1016369,1044750,3043,24085,46075,45452,1060847,1056762,1037644,51322,23285,61524,1012632,50651,1031845,3347,1046950,1033713,1053394,1061534,65131,46550,1015317,34628,13251,1037711,1067549,48358,1034607,32646,48767,28223,1049617,5623,1060097,1026131,40300,60718,65303,1005035,1013860,1012258,32634,53385,1060681,1034712,42492,25412,1067610,1020227,1062193,1015780,1065447,1032356,1023520,61358,1039764,45321,1057706,4771,42854,1015157,24890,1064661,1022781,1029355,1010060,67819,1213,19554,23723,23361,49074,50695,14396,1022611,35570,9416,58811,1065051,1014862,1064787,1050986,1019954,1050895,51717,1049003,1059230,15195,61721,16409,1061507,24775,1006931,1012546,1030297".split(","));
        String sql = "select link from v2_product_instructions where product_id=";
        List<String> result = new ArrayList<>();
        topProducts.forEach(id -> {
            try {
                String link = jdbcTemplate.queryForObject(sql + id, String.class);
                result.add(link);
            } catch (Exception ignored) {}
        });
        return result;
    }
}
