package net.wohlfart.pluto.gui.widget;

// data and label for a select box
public class Choice<T> {
    private final String i18nKey;
    private final T value;
    private String label;

    public Choice(String i18nKey, T value) {
        this.i18nKey = this.label = i18nKey;
        this.value = value;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public T getValue() {
        return value;
    }

    public void setText(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    // don't change the label on language changes
    public static class Static<T> extends Choice<T> {

        public Static(String label, T value) {
            super("null", value);
            super.setText(label);
        }

        @Override
        public void setText(String label) {
            // do nothing
        }

    }

}
