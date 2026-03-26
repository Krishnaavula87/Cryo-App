package com.cryo.export.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class PageNumberFooter extends PdfPageEventHelper {

    @Override
    public void onEndPage(PdfWriter writer, Document document) {

        Font font = FontFactory.getFont(FontFactory.HELVETICA, 8);

        Phrase footer = new Phrase(
                "Page " + writer.getPageNumber(),
                font
        );

        ColumnText.showTextAligned(
                writer.getDirectContent(),
                Element.ALIGN_CENTER,
                footer,
                (document.right() + document.left()) / 2,
                document.bottom() - 10,
                0
        );
    }
}