package com.gamingmesh.jobs.container;

import java.util.HashMap;
import java.util.Map;

public class BoostMultiplier implements Cloneable {

    private final Map<CurrencyType, Double> map = new HashMap<>();
    private final Map<CurrencyType, Long> timers = new HashMap<>();

    // Constructors
    public BoostMultiplier() {
        for (CurrencyType one : CurrencyType.values()) {
            this.map.put(one, 0D);
        }
    }

    public BoostMultiplier(Map<CurrencyType, Double> map) {
        for (CurrencyType one : CurrencyType.values()) {
            this.map.put(one, map.getOrDefault(one, 0D));
        }
    }

    @Override
    public BoostMultiplier clone() {
        BoostMultiplier boost = new BoostMultiplier();
        for (CurrencyType type : CurrencyType.values()) {
            boost.add(type, this.map.get(type));
            Long time = this.timers.get(type);
            if (time != null) {
                boost.setTime(type, time);
            }
        }
        return boost;
    }

    // Add boost without timer
    public BoostMultiplier add(CurrencyType type, double amount) {
        if (!Double.isNaN(amount))
            this.map.put(type, amount);
        timers.remove(type);
        return this;
    }

    // Add boost with timer
    public BoostMultiplier add(CurrencyType type, double amount, long expiryTimeMillis) {
        add(type, amount);
        timers.put(type, expiryTimeMillis);
        return this;
    }

    // Add uniform boost to all types
    public BoostMultiplier add(double amount) {
        if (amount != 0 && !Double.isNaN(amount)) {
            for (CurrencyType one : CurrencyType.values()) {
                map.put(one, amount);
            }
        }
        return this;
    }

    // Add boost values from another BoostMultiplier
    public void add(BoostMultiplier other) {
        for (CurrencyType one : CurrencyType.values()) {
            this.map.put(one, get(one) + other.get(one));
        }
    }

    public double get(CurrencyType type) {
        if (!isValid(type))
            return 0D;
        return map.getOrDefault(type, 0D);
    }

    public Long getTime(CurrencyType type) {
        return timers.get(type);
    }

    public void setTime(CurrencyType type, long expiry) {
        timers.put(type, expiry);
    }

    public boolean isValid(CurrencyType type) {
        Long time = getTime(type);
        if (time == null)
            return true;

        if (time < System.currentTimeMillis()) {
            map.remove(type);
            timers.remove(type);
            return false;
        }
        return true;
    }

    // ========================
    // Persistence Utilities
    // ========================

    // Export boost values (e.g. for file or DB)
    public Map<String, Double> exportBoosts() {
        Map<String, Double> export = new HashMap<>();
        for (Map.Entry<CurrencyType, Double> entry : map.entrySet()) {
            if (entry.getValue() != null && entry.getValue() != 0) {
                export.put(entry.getKey().name(), entry.getValue());
            }
        }
        return export;
    }

    // Export timers (as milliseconds since epoch)
    public Map<String, Long> exportTimers() {
        Map<String, Long> export = new HashMap<>();
        for (Map.Entry<CurrencyType, Long> entry : timers.entrySet()) {
            if (entry.getValue() != null && entry.getValue() > System.currentTimeMillis()) {
                export.put(entry.getKey().name(), entry.getValue());
            }
        }
        return export;
    }

    // Import boost values
    public void importBoosts(Map<String, Double> imported) {
        if (imported == null) return;
        for (Map.Entry<String, Double> entry : imported.entrySet()) {
            try {
                CurrencyType type = CurrencyType.valueOf(entry.getKey());
                this.map.put(type, entry.getValue());
            } catch (IllegalArgumentException ignored) {}
        }
    }

    // Import timers
    public void importTimers(Map<String, Long> imported) {
        if (imported == null) return;
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : imported.entrySet()) {
            try {
                CurrencyType type = CurrencyType.valueOf(entry.getKey());
                Long expiry = entry.getValue();
                if (expiry != null && expiry > now) {
                    timers.put(type, expiry);
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }
}