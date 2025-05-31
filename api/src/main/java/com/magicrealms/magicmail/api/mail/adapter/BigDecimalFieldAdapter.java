package com.magicrealms.magicmail.api.mail.adapter;

import com.magicrealms.magiclib.common.adapt.FieldAdapter;
import java.math.BigDecimal;

/**
 * @author Ryan-0916
 * @Desc BigDecimal 转换器
 * @date 2025-05-17
 */
public class BigDecimalFieldAdapter extends FieldAdapter<BigDecimal, Double> {

    @Override
    public Double write(BigDecimal writer) {
        return writer == null ? 0D : writer.doubleValue();
    }

    @Override
    public BigDecimal read(Double reader) {
        return reader == null ? BigDecimal.ZERO : BigDecimal.valueOf(reader);
    }
}
