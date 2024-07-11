package com.iyr.ian.dao.models;


import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SystemConfig {

    public String register_initial_plan_key;
    public int register_initial_plan_duration;
    @NotNull
    public Map<String, Integer> expiration_times = new HashMap<String, Integer>();

    public SystemConfig() {
    }

}
