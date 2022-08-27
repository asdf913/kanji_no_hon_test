package kanji_no_hon_test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class Main {

	public static class Text {

		private String text, hiragana = null;

		public String getText() {
			return text;
		}

		public String getHiragana() {
			return hiragana;
		}

	}

	public static void main(final String[] args) throws InvalidFormatException, IOException, TemplateException {
		//
		final JFileChooser jfc = new JFileChooser(".");
		//
		File file = null;
		//
		if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			//
			file = jfc.getSelectedFile();
			//
		} // if
			//
		List<Text> texts = null;
		//
		try (final Workbook workbook = new XSSFWorkbook(file)) {
			//
			Text text = null;
			//
			int columnIndex;
			//
			for (final Sheet sheet : workbook) {
				//
				if (sheet == null || sheet.iterator() == null) {
					continue;
				} // if
					//
				for (final Row row : sheet) {
					//
					text = new Text();
					//
					if (row == null || row.iterator() == null) {
						continue;
					} // if
						//
					for (final Cell cell : row) {
						//
						if ((columnIndex = cell.getColumnIndex()) == 2) {// TODO
							text.text = cell.getStringCellValue();
						} else if (columnIndex == 3) {// TODO
							text.hiragana = cell.getStringCellValue();
						} // if
							//
					} // for
						//
					if ((texts = ObjectUtils.getIfNull(texts, ArrayList::new)) != null) {
						//
						texts.add(text);
						//
					} // if
						//
				} // for
					//
			} // for
				//
		} // try
			//
		final Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
		//
		configuration.setTemplateLoader(new ClassTemplateLoader(Main.class, "/"));
		//
		final Template template = configuration.getTemplate("template.html.ftl");
		//
		if (template != null) {
			//
			try (final Writer w = new FileWriter(new File("test.html"))) {// TODO
				//
				final Map<String, Object> data = new LinkedHashMap<>(Collections.singletonMap("texts", texts));
				//
				template.process(data, w);
				//
			} // try
				//
		} // if
			//
	}

}