package org.codewith3h.finmateapplication.specification;

import org.codewith3h.finmateapplication.entity.Feature;
import org.springframework.data.jpa.domain.Specification;

public class FeatureSpecification {
    public static Specification<Feature> hasCode(String code){
        return (root, criteriaQuery, criteriaBuilder)
                -> code == null ? null : criteriaBuilder.like(criteriaBuilder.upper(root.get("code")),"%"+code.toUpperCase()+"%");
    }

    public static Specification<Feature> hasName(String name){
        return (root, query, cb) ->
                name  == null ? null : cb.like(cb.upper(root.get("name")), "%"+name.toUpperCase()+"%");
    }

    public static Specification<Feature> hasActive(Boolean active){
        return (root, query, cb) ->
                active == null ? null : cb.and(cb.equal(root.get("isActive"), active));
    }
}
