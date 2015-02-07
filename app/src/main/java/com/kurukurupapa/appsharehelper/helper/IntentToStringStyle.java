package com.kurukurupapa.appsharehelper.helper;

import android.os.Bundle;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * インテント用ToStringStyle
 */
public class IntentToStringStyle extends ToStringStyle {
    private static final long serialVersionUID = 1L;

    /**
     * <p>Constructor.</p>
     *
     * <p>Use the static constant rather than instantiating.</p>
     */
    public IntentToStringStyle() {
        super();
        this.setContentStart("[");
        this.setFieldSeparator(SystemUtils.LINE_SEPARATOR + "  ");
        this.setFieldSeparatorAtStart(true);
        this.setContentEnd(SystemUtils.LINE_SEPARATOR + "]");
    }

    /**
     * <p>Ensure <code>Singleton</code> after serialization.</p>
     *
     * @return the singleton
     */
    private Object readResolve() {
        return ToStringStyle.MULTI_LINE_STYLE;
    }

    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {
        if (value instanceof Bundle) {
            appendDetail(buffer, fieldName, (Bundle) value);
        } else {
            super.appendDetail(buffer, fieldName, value);
        }
    }

    /**
     * Extra項目を文字列化します。
     * @param buffer
     * @param fieldName
     * @param extras
     */
    protected void appendDetail(StringBuffer buffer, String fieldName, Bundle extras) {
        if (extras == null) {
            appendNullText(buffer, fieldName);
        } else {
            buffer.append(getArrayStart());
            boolean sep = false;
            for (String key : extras.keySet()) {
                if (sep) {
                    buffer.append(getArraySeparator());
                }
                buffer.append(key + getFieldNameValueSeparator());
                buffer.append(extras.get(key));
                sep = true;
            }
            buffer.append(getArrayEnd());
        }
    }
}
