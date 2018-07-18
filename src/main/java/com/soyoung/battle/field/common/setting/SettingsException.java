package com.soyoung.battle.field.common.setting;

import com.soyoung.battle.field.BattlefieldException;

public class SettingsException extends BattlefieldException {

    public SettingsException(String message) {
        super(message);
    }

    public SettingsException(String message, Throwable cause) {
        super(message, cause);
    }
}
