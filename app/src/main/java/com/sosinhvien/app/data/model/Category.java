package com.sosinhvien.app.data.model;

public class Category {
    private final String id;
    private final String name;
    private final String iconName;
    private final boolean isDefault;
    private final boolean visible;

    public Category(String id, String name, String iconName, boolean isDefault, boolean visible) {
        this.id = id;
        this.name = name;
        this.iconName = iconName;
        this.isDefault = isDefault;
        this.visible = visible;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIconName() {
        return iconName;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public boolean isVisible() {
        return visible;
    }
}
