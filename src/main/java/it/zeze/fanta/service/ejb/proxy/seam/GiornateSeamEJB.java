package it.zeze.fanta.service.ejb.proxy.seam;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.service.bean.SelectItem;
import it.zeze.fanta.service.definition.ejb.GiornateLocal;
import it.zeze.fanta.service.definition.ejb.proxy.seam.GiornateSeamRemote;
import it.zeze.fantaformazioneweb.entity.Giornate;

@Stateless
@LocalBean
public class GiornateSeamEJB implements GiornateSeamRemote {

	private static final Logger log = LogManager.getLogger(GiornateSeamEJB.class);

	@EJB(name = "GiornateEJB")
	private GiornateLocal giornateEJB;

	@Override
	public void unmarshallAndSaveFromHtmlFile(String stagione) {
		giornateEJB.unmarshallAndSaveFromHtmlFile(stagione);
	}

	@Override
	public String getStagione(String stagioneInput) {
		return giornateEJB.getStagione(stagioneInput);
	}

	@Override
	public int getIdGiornata(int numeroGiornata, String stagione) {
		return giornateEJB.getIdGiornata(numeroGiornata, stagione);
	}

	@Override
	public Giornate getGiornataById(int idGiornata) {
		return giornateEJB.getGiornataById(idGiornata);
	}

	@Override
	public int getIdGiornata(String dataString) {
		return giornateEJB.getIdGiornata(dataString);
	}

	@Override
	public Giornate getGiornata(String dataString) {
		return giornateEJB.getGiornata(dataString);
	}

	@Override
	public Giornate getLastGiornata() {
		return giornateEJB.getLastGiornata();
	}

	@Override
	public List<Giornate> getGiornateByStagione(String stagione) {
		return giornateEJB.getGiornateByStagione(stagione);
	}
	
	@Override
	public List<Giornate> getGiornateAll(String stagione){
		return giornateEJB.getGiornateAll(stagione);
	}

	@Override
	public List<SelectItem> getStagioniAll() {
		return giornateEJB.getStagioniAll();
	}
}
