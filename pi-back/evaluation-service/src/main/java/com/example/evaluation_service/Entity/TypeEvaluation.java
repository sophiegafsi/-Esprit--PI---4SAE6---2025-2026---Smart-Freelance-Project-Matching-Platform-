package com.example.evaluation_service.Entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TypeEvaluation {
    SOFT_SKILLS,
    TECHNIQUE,
    AUTRE;

    @JsonCreator
    public static TypeEvaluation fromString(String key) {
        return key == null ? null : TypeEvaluation.valueOf(key.toUpperCase());
    }
}