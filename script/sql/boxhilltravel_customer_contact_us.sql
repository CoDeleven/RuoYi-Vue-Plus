CREATE TABLE holidays_customer_contact_us (
                                              id bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
                                              full_name varchar(100) NOT NULL COMMENT 'Full name',
                                              email_address varchar(100) NOT NULL COMMENT 'Email address',
                                              phone_number varchar(30) NOT NULL COMMENT 'Phone number',
                                              enquiry_type varchar(50) NOT NULL COMMENT 'Enquiry type',
                                              message text NOT NULL COMMENT 'Message',
                                              ip_address varchar(64) DEFAULT NULL COMMENT 'Client IP address',
                                              read_status tinyint NOT NULL DEFAULT 0 COMMENT '0 unread, 1 read',
                                              read_time datetime DEFAULT NULL COMMENT 'Read time',
                                              create_time datetime DEFAULT NULL COMMENT 'Create time',
                                              update_time datetime DEFAULT NULL COMMENT 'Update time',
                                              PRIMARY KEY (id),
                                              KEY idx_read_status (read_status),
                                              KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Customer contact us records'
