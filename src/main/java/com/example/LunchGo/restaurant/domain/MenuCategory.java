package com.example.LunchGo.restaurant.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum MenuCategory {

    MAIN("주메뉴"),
    SUB("서브메뉴"),
    OTHER("기타(디저트, 음료)");

    private final String displayName;

    MenuCategory(String displayName) {
        this.displayName = displayName;
    }

    @JsonProperty("value")
    public String getDisplayName() {
        return displayName;
    }

    @JsonProperty("code")
    public String getCode() {
        return name();
    }

    //
    @JsonCreator
    public static MenuCategory fromCode(Map<String, String> obj) {
        if (obj != null && obj.containsKey("code")) {
            return MenuCategory.valueOf(obj.get("code"));
        }
        return null;
    }
}
