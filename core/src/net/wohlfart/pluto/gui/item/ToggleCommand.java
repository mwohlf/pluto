package net.wohlfart.pluto.gui.item;

public abstract class ToggleCommand {

    private final String i18nKey;

    public ToggleCommand(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    @SuppressWarnings("BooleanParameter")
    public abstract void toggle(boolean state);

}
