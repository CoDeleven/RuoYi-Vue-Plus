package com.boxhilltravel.core.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Practical info item payload for country page content.
 */
@Data
public class DestinationPagePracticalInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String title;

    private String content;

}
