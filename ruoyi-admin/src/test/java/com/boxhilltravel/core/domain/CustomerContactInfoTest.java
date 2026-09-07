package com.boxhilltravel.core.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * CustomerContactInfo table mapping test.
 */
public class CustomerContactInfoTest {

    @Test
    void shouldMapToHolidaysCustomerContactInfoTable() {
        TableName tableName = CustomerContactInfo.class.getAnnotation(TableName.class);
        Assertions.assertNotNull(tableName);
        Assertions.assertEquals("holidays_customer_contact_info", tableName.value());
    }
}
