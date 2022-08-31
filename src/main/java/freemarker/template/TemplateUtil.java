package freemarker.template;

import java.io.IOException;
import java.io.Writer;

public interface TemplateUtil {

	static void process(final Template instance, final Object dataModel, final Writer out)
			throws TemplateException, IOException {
		//
		if (instance != null) {
			//
			instance.process(dataModel, out);
			//
		} // if
			//
	}

}