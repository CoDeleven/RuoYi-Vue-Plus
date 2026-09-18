package com.boxhilltravel.core.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * CustomerContactUs table mapping test.
 */
public class CustomerContactUsTest {

    @Test
    void shouldMapToHolidaysCustomerContactUsTable() {
        TableName tableName = CustomerContactUs.class.getAnnotation(TableName.class);
        Assertions.assertNotNull(tableName);
        Assertions.assertEquals("holidays_customer_contact_us", tableName.value());
    }
}
