package domain.repository;

import domain.entity.ParameteurEntity;

import java.util.List;

public interface ParameteurRepository {
    public List<ParameteurEntity> verifyAlerts(String macAddress);
}
