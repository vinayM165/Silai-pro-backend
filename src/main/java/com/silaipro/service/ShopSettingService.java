package com.silaipro.service;

import com.silaipro.constant.ShopSettingConstants;
import com.silaipro.entity.ShopSetting;
import com.silaipro.repository.ShopSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopSettingService {

    private final ShopSettingRepository shopSettingRepository;

    public Map<String, String> getAllSettings() {
        return shopSettingRepository.findAll().stream()
                .collect(Collectors.toMap(ShopSetting::getSettingKey, ShopSetting::getSettingValue));
    }

    public String getSetting(String key) {
        return shopSettingRepository.findBySettingKey(key)
                .map(ShopSetting::getSettingValue)
                .orElse(null);
    }

    @Transactional
    public void updateSetting(String key, String value) {
        ShopSetting setting = shopSettingRepository.findBySettingKey(key)
                .orElse(ShopSetting.builder().settingKey(key).build());
        
        setting.setSettingValue(value);
        shopSettingRepository.save(setting);
    }

    @Transactional
    public void updateSettings(Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            updateSetting(entry.getKey(), entry.getValue());
        }
    }

    public Map<String, String> getPublicSettings() {
        Map<String, String> all = getAllSettings();
        Map<String, String> publicSettings = new HashMap<>();
        
        // Define public keys
        String[] publicKeys = {
            ShopSettingConstants.SHOP_NAME,
            ShopSettingConstants.SHOP_LOGO_URL,
            ShopSettingConstants.SHOP_ADDRESS,
            ShopSettingConstants.SHOP_PHONE,
            ShopSettingConstants.CURRENCY_SYMBOL,
            ShopSettingConstants.TAX_RATE
        };

        for (String key : publicKeys) {
            String val = all.get(key);
            if (val != null) {
                publicSettings.put(key, val);
            }
        }
        
        return publicSettings;
    }
}
