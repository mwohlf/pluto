package net.wohlfart.pluto.gui.item;

public abstract class ActionCommand {
    private final String i18nKey;

    public ActionCommand(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public abstract void execute();

}
