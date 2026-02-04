package ru.razumoff.courses.dao.enumz;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnrollmentStatus {
    INVITED("Приглашён"),
    ACTIVE("Активный"),
    SUSPENDED("Приостановлен"),
    BLOCKED("Заблокирован"),
    DROPPED("Удалён"),
    NO_ACCESS("Нет доступа");

    private final String label;

    public boolean hasFullAccess() {
        return this == ACTIVE;
    }

    public boolean canView() {
        return this == ACTIVE || this == SUSPENDED;
    }
}

