package domain.entity;

public class ParameteurEntity {
    private Integer id;
    private Integer normalAlert;
    private Integer criticAlert;
    private String macAddress;
    private String component;
    private String name;

    public ParameteurEntity(Integer id, Integer normalAlert, Integer criticAlert, String macAddress, String component, String name) {
        this.id = id;
        this.normalAlert = normalAlert;
        this.criticAlert = criticAlert;
        this.macAddress = macAddress;
        this.component = component;
        this.name = name;
    }

    public ParameteurEntity() {}

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCriticAlert() {
        return this.criticAlert;
    }

    public void setCriticAlert(Integer criticAlert) {
        this.criticAlert = criticAlert;
    }

    public Integer getNormalAlert() {
        return this.normalAlert;
    }

    public void setNormalAlert(Integer normalAlert) {
        this.normalAlert = normalAlert;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
}
