package domain.model.entity;

public class ParameteurEntity {
    private Integer id;
    private Integer maximumAlert;

    public ParameteurEntity(Integer id, Integer maximumAlert) {
        this.id = id;
        this.maximumAlert = maximumAlert;
    }

    public ParameteurEntity() {}

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMaximumAlert() {
        return this.maximumAlert;
    }

    public void setMaximumAlert(Integer maximumAlert) {
        this.maximumAlert = maximumAlert;
    }
}
