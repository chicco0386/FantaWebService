package it.zeze.servlet;

import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.LogManager;

import it.zeze.util.ConfigurationUtil;

public class FileConfigurationServlet extends HttpServlet {

	private static final long serialVersionUID = -219613647151336985L;

	private static org.apache.log4j.Logger log = LogManager.getLogger(FileConfigurationServlet.class);

	private static final String NOME_FILE_PROPS = "application.properties";

	@Override
	public void init() {
		String tipoConfigurazione = getServletConfig().getInitParameter("tipoConfigurazione");
		log.info("Inizializzo il file di properties [" + NOME_FILE_PROPS + "] per tipo [" + tipoConfigurazione + "]");
		try {
			ConfigurationUtil.initializeConfiguration(tipoConfigurazione + "." + NOME_FILE_PROPS);
		} catch (ConfigurationException e) {
			log.error("errore durante l'inizializzazione del file di properties [" + tipoConfigurazione + "." + NOME_FILE_PROPS + "]", e);
		}
	}

	@Override
	public void destroy() {
		log.info("Chiudo il file di properties");
		ConfigurationUtil.clearConfiguration();
	}

}
