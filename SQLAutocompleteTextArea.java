package com.duckdb.autocomplete.components;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;

import java.util.List;

@Tag("sql-autocomplete-textarea")
@JsModule("./sql-autocomplete-textarea.js")
@CssImport("./sql-autocomplete-textarea.css")
public class SQLAutocompleteTextArea extends Component implements HasValue<HasValue.ValueChangeEvent<String>, String>, HasStyle {

    private String value = "";
    private boolean readOnly = false;
    private String placeholder = "";

    public SQLAutocompleteTextArea() {
        getElement().addPropertyChangeListener("value", event -> fireValueChangeEvent());
        getElement().addEventListener("value-changed", event -> {
            this.value = event.getEventData().getString("event.detail.value");
            fireValueChangeEvent();
        });
    }

    public SQLAutocompleteTextArea(String placeholder) {
        this();
        setPlaceholder(placeholder);
    }

    @Override
    public void setValue(String value) {
        this.value = value == null ? "" : value;
        getElement().setProperty("value", this.value);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super HasValue.ValueChangeEvent<String>> valueChangeListener) {
        return addListener(ValueChangeEvent.class, event ->
                valueChangeListener.valueChanged((HasValue.ValueChangeEvent<String>) event));
    }


    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        getElement().setProperty("readOnly", readOnly);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return getElement().getProperty("required", false);
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        getElement().setProperty("required", requiredIndicatorVisible);
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder;
        getElement().setProperty("placeholder", this.placeholder);
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setHeight(String height) {
        getElement().getStyle().set("height", height);
    }

    public void setWidth(String width) {
        getElement().getStyle().set("width", width);
    }

    public void addSchemaTable(String tableName, List<String> columns) {
        getElement().callJsFunction("addSchemaTable", tableName, columns.toArray());
    }

    public void clearSchema() {
        getElement().callJsFunction("clearSchema");
    }

    public void addCustomKeywords(List<String> keywords) {
        getElement().callJsFunction("addCustomKeywords", keywords.toArray());
    }

    private void fireValueChangeEvent() {
        fireEvent(new ValueChangeEvent<>(this, value, true));
    }

    public static class ValueChangeEvent<T> extends ComponentEvent<SQLAutocompleteTextArea> {
        private final T value;

        public ValueChangeEvent(SQLAutocompleteTextArea source, T value, boolean fromClient) {
            super(source, fromClient);
            this.value = value;
        }

        public T getValue() {
            return value;
        }
    }
}