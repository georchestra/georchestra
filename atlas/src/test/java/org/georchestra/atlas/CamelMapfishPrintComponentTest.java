package org.georchestra.atlas;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

public class CamelMapfishPrintComponentTest {

	@Test
	public void generateErrorPdftest() throws DocumentException, IOException {
		CamelMapfishPrintComponent comp = new CamelMapfishPrintComponent();
		Throwable e = new RuntimeException("something is messed up");

		ByteArrayOutputStream out = comp.generateErrorPdf(e);

		// re-read pdf
		PdfReader reader = new PdfReader(out.toByteArray());
		PdfReaderContentParser p = new PdfReaderContentParser(reader);
		TextExtractionStrategy strategy = p.processContent(1, new SimpleTextExtractionStrategy());
		assertTrue("Missing exception in the generated PDF",
				strategy.getResultantText().contains("something is messed up"));
	}
}
