package com.boxhilltravel.core.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Highlight card payload for country page content.
 */
@Data
public class DestinationPageHighlightVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String icon;

    private String title;

    private String description;

}
