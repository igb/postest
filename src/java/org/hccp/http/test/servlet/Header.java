package org.hccp.http.test.servlet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Header {
    String name;
    Set<String> values = new HashSet<>();

    public Header(String name, List<String> value) {
        this.name = name;
        for (int i = 0; i < value.size(); i++) {
            String valueValue = value.get(i);
            values.add(valueValue);

        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getValue() {
        return values;
    }

    public void addValue(List<String> value) {
        for (int i = 0; i < value.size(); i++) {
            String valueValue =  value.get(i);
            values.add(valueValue);

        }
    }
}
